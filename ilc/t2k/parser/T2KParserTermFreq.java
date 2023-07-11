/**
 * $Id: T2KParserTermFreq.java,v 1.17 2007/11/23 12:06:49 simone Exp $
 */
package ilc.t2k.parser;

import ilc.t2k.Config;
import ilc.t2k.Consts;
import ilc.t2k.bean.FormFreq;
import ilc.t2k.bean.KW;
import ilc.t2k.database.ConnectionManager;
import ilc.t2k.database.DBManager;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Esegue il parsing dei file contenenti le coppie <termine,frequenza> e 
 * crea l'informazione necessaria per l'aggiornamento di wbt_app_documenti
 * nella quale ci sono indicati, per ogni documento, gli ID dei termini (selezionati
 * nel glossario) che compaiono nel testo.
 * Inoltre vengono calcolate le informazioni necessarie per la tabella wbt_app_glossario_frequenze
 * 
 * Presuppone che la tabella wbt_app_glossario sia stata riempita perche' ha bisogno
 * degli IDs dei termini.
 * @author simone
 *
 */
public class T2KParserTermFreq  {

	private BufferedReader in = null;
	private FileReader fr = null;
	private String idDocumento;

	private HashMap glossarioTerminiForma;
	private double numberOfDocs; //numero di documenti della collezione
	private HashMap termineIdentificativo;
	private Connection conn = null;
	//memorizza per ogni termine in quanti documenti il dato termine occorre (non in quali!)
	private HashMap formeDocument = new HashMap(Consts.HASH_CAPACITY);
	//private Vector glossFreq = new Vector();
	private Vector  glossFreq = new Vector();
	/**
	 * idsTerminiGlossario memorizza gli ID dei termini che compaiono il un determinato
	 * testo, che andranno ad aggiornare il campo KW relativo al documento considerato
	 * della tabella wbt_app_documenti 
	 */
	private Vector idsTerminiGlossario;
	private static Logger logger = Logger.getLogger("jt2k"); 
	//private static int idFrequenza = 0;
	
	
	public T2KParserTermFreq (HashMap _glossarioTerminiForma, int _numberOfDocs, Connection _conn){
		glossarioTerminiForma = _glossarioTerminiForma;
		numberOfDocs = new Double(_numberOfDocs).doubleValue();
		conn = _conn;
		
	}
	

	/**
	 * Inizializzazione del parser per i file <termine,fequenza> 
	 * @param fileName nome del file
	 * @param _termineIdentificativo HashMap dei termini inseriti nel glossario contenente come
	 * valore l'ID del termine associatogli dal DBMS (come campo autoincrement)
	 *
	 */
	public void init(String fileName, HashMap _termineIdentificativo) {
		try {
			fr = new FileReader(fileName);
			in = new BufferedReader(fr);
			//l'ID del documento e' (per convenzione) nel nome del file stesso
			//fileName e' della forma .+\\_(\\d+)\\..+ es. file_20.txt.termfreq
			idDocumento = fileName.replaceFirst(".+\\_(\\d+)\\..+","$1");
			termineIdentificativo = _termineIdentificativo;
			idsTerminiGlossario = new Vector();
			logger.finer(idDocumento);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void close() {
		try {
			if(in!=null){
				fr.close();
				in.close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/*
	 * Crea la riga da mettere nella tabella Documenit (indice inverso)
	 */
	public void createDocRows(){
		String termine = null;
		if(in != null){
			try {
				while(in.ready()){
					String row = in.readLine();
					if(row == null){
						fr.close();
						in.close();
						return ;
					}
					//ogni row e' della forma termine;fequenza
					//System.err.print("row: "+row);
					String[] splittedRow = row.split(";");
					if(splittedRow.length != 2){
						logger.log(Level.WARNING,"Malformed row: "+row);
						//System.err.println("Malformed row: "+row);
						continue;
					}
					/**
					 * @todo da rivedere... perche' si rischia di bloccare riferimenti
					 * all'interno dei documenti... per ora la commento
					 */
					/*if(Integer.parseInt(splittedRow[1]) < Config.soglia_glossario){
						logger.log(Level.WARNING,"term: "+splittedRow[0] +" freq: " + splittedRow[1]+ " is too low");
						continue;
					}*/
					//System.err.println("  splittedRow[0]: "+splittedRow[0]);
					//splittedRow[0] e' il termine, splittedRow[1] e' la frequenza
					//System.err.println("splittedRow[0] "+splittedRow[0]);
					//System.err.println("splittedRow[0] "+splittedRow[0].replaceAll("__"," "));
					termine = splittedRow[0].replaceAll("__"," ");
					String[] idFreq = getIdFreqFormaFromGlossario(termine);
					termine = null;
					//formaFreq[0] e' KWID, formaFreq[1] e' IRG di KWID
					if(idFreq==null){
						//logger.log(Level.WARNING,"Term: "+termine+" not found in glossario");
						continue;
					}
					if((idFreq[0]==null)||(idFreq[1])==null){
						//logger.log(Level.WARNING,"Error in retriving from glossario for "+splittedRow[0]);
						//System.err.println(termine + " there isn't in glossario");
						continue;
					}
					//aggiungo l'id trovato all'elenco degli ids per quel documento
					idsTerminiGlossario.addElement(idFreq[0]);
					puntIntoFormeDocument(idFreq[0]);
					//aggiorno la tabella wbt_app_glossario_frequenza
					//                         idDoc,      KWID           IRG          IRD
					prepareToInsertGlossFreq(idDocumento,idFreq[0],idFreq[1],splittedRow[1]);
				}
				//Ho creato il vettore di IDs da mettere in wbt_app_documenti
				updateDocumentDB(idDocumento,idsTerminiGlossario);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return ;
	}
	
	/**
	 * Crea il KW per i file che fanno parte dell'aggiornamento
	 *
	 * 11/10/2006 - Ci puo' essere piu' volte lo stesso termine... e' corretto?
	 * 
	 */
	public void createDocRowsOnUpdate(HashMap glossarioIdFreq){
		if(in != null){
			try {
				while(in.ready()){
					String row = in.readLine();
					if(row == null){
						return ;
					}
					//ogni row e' della forma termine;fequenza
					//System.err.println("createDocRowsOnUpdate() row: "+row);
					String[] splittedRow = row.split(";");
					if(splittedRow.length != 2){
						logger.log(Level.WARNING,"Malformed row: "+row);
						//System.err.println("Malformed row: "+row);
						continue;
					}

					String termine = splittedRow[0].replaceAll("__"," ");
					String idTerm = getIdFromGlossario(termine);
					//formaFreq[0] e' KWID, formaFreq[1] e' IRG di KWID
					if(idTerm==null){
						//logger.log(Level.WARNING,"Term: "+termine+" not found in glossario");
						continue;
					}
					//aggiungo l'id trovato all'elenco degli ids per quel documento
					idsTerminiGlossario.addElement(idTerm);
					puntIntoFormeDocument(idTerm);
					//aggiorno la tabella wbt_app_glossario_frequenza
					//System.err.println("createDocRowsOnUpdate() "+idDocumento + " " +idFreq + " " + splittedRow[1]);
					//prepareToInsertGlossFreq(idDocumento,idFreq[0],idFreq[1],splittedRow[1]);
					
					//aggiorno l'hashmap contente il glossario
					String freq = (String) glossarioIdFreq.get(idTerm);
					int newFreq = Integer.parseInt(splittedRow[1]);
					if (freq != null){
						newFreq += Integer.parseInt(freq);
						//System.err.println(newFreq);
					}else{
						logger.log(Level.SEVERE,"BUG: Il termine " + idTerm + "non e' nel glossario");
					}
					glossarioIdFreq.put(idTerm, Integer.toString(newFreq));
					
				}
				//Ho creato il vettore di IDs da mettere in wbt_app_documenti
				updateDocumentDB(idDocumento,idsTerminiGlossario);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return ;
	}
	
	private String[] getIdFreqFormaFromGlossario(String term){
		//Cerco la forma (+ frequente) associata al termine
		if(term==null){
			logger.info("Term is null!");
			return null;
		}
		FormFreq formFreq = (FormFreq) glossarioTerminiForma.get(term);
		
		if(formFreq==null){
			logger.info("Term "+term+" not found!");
			return null;
		}
		//
		//String id = (String) termineIdentificativo.get(formFreq.getForm());
		/* 11.10.2006 - modifica: ora si lavora con i lemmi e non con le forme! */
		String id = (String) termineIdentificativo.get(term);
//		System.err.println("getIdFreqFormaFromGlossario: id "+id+", termine " +term+", forma "+formFreq.getForm()+", frequenza "+formFreq.getFreq().toString());
		return new String[]{id,formFreq.getFreq().toString()};
	}
	
	private String getIdFromGlossario(String term){
		//Cerco la forma (+ frequente) associata al termine
		if(term==null){
			logger.info("Term in null!");
			return null;
		}
		String id = (String) glossarioTerminiForma.get(term);
		
		if(id==null){
			logger.info("Term "+term+" not found!");
			return null;
		}
		//System.err.println("getIdFromGlossario: id "+id+", termine " +term);
		return id;
	}

	/*
	 * Questo metodo deve fare l'update di wbt_app_documenti
	 * mettendo gli ids dei termini che occorrono nel documento 
	 * identificato da idDoc 
	 * UPDATE wbt_app_documenti SET KW = ids WHERE DID=idDoc; 
	 */
	private void updateDocumentDB (String idDoc, Vector idTerms){
		if((idDoc==null)||(idTerms == null)){
			logger.info("idDoc or idTerms is null!");
		}

		StringBuffer ids = getOldIds(idDoc);
//System.err.println("ids: "+ids);
		int vectorLenght = idTerms.size();
		if(vectorLenght > 0 && ids.length()>0){
			ids.append(",");
		}
		for (int i = 0; i < vectorLenght; i++){
			ids.append(idTerms.elementAt(i));
			if(i<vectorLenght-1){
				ids.append(",");
			}
		}
		
		DBManager.updateDocumentTable(ids.toString(), idDoc, conn);
		ids = null;
		/*
		try {
	        // Prepare a statement to insert a record
	        String sql = "UPDATE "+ Config.dbTableDocument
	        			+" SET KW = \'"+ids.toString()+"\' WHERE DID = "+idDoc;
	        logger.log(Level.FINE,sql);
	        
	        Statement stmt = conn.createStatement();
	        stmt.executeUpdate(sql,Statement.NO_GENERATED_KEYS);
	        
		} catch (SQLException e) {
	        logger.log(Level.SEVERE,"In inserting row to "+Config.dbTableDocument);
	        //e.printStackTrace();
	        System.err.println("SQL: "+e.getMessage());
	    }finally{
	    	//conn.close();
	    }*/
	}
	
	private void prepareToInsertGlossFreq(String idDoc, String kwid, String irg,  String ird){
		KW kw = new KW(idDoc,kwid,irg,ird);
		glossFreq.addElement(kw);
	}
	
	/**
	 * Esegue l'inserimento in wbt_app_glossario_fequenza
	 */
	public void insertIntoGlossFreq(){
		
		//double measure = calculate(Double.parseDouble(ird),kwid);
		/**
		 * TODO questo metodo deve fare una insert in wbt_app_glossario_fequenza
		 * con frequenza=measure
		 * INSERT INTO wbt_app_glossario_fequenza VALUES (idDocumento, kwid, measure, ird, irg);
		 */
		int count = 0;
		Iterator iter = glossFreq.iterator(); 
		try {
			//conn.setAutoCommit(false);
			conn = ConnectionManager.getConnection();
			
			PreparedStatement pstmt = conn.prepareStatement(
					"INSERT INTO "+ Config.dbTableGlossarioFreq+" VALUES(?, ?, ?, ?, ?)");
			
			if(Config.dbDriverName.contains("mysql")){
				 pstmt = conn.prepareStatement(
							"INSERT INTO "+ Config.dbTableGlossarioFreq+" (DID,KWID,funzione,IRD,IRG)  VALUES(?, ?, ?, ?, ?)");
			}
			
			while (iter.hasNext()) {
				KW element = (KW) iter.next();
				// Prepare a statement to insert a record
				/*	String sql = "INSERT INTO "+ Config.dbTableGlossarioFreq
				 +" VALUES("+idDoc+","+kwid+","+String.valueOf(measure)+","+ird+","+irg+")";
				 logger.log(Level.FINE,sql);
				 stmt.executeUpdate(sql,Statement.NO_GENERATED_KEYS);
				 */
				pstmt.setString(1, element.getIdDoc() );
				pstmt.setString(2, element.getKwid());
				pstmt.setDouble(3, calculate(Double.parseDouble(element.getIrd()),element.getKwid()));
				pstmt.setString(4, element.getIrd());
				pstmt.setString(5, element.getIrg());
				pstmt.addBatch();
			}

			pstmt.executeBatch();
			pstmt.close();

			//conn.commit();
			//conn.setAutoCommit(true);
		} catch (SQLException e) {
			logger.log(Level.SEVERE,"In inserting row to "+Config.dbTableGlossarioFreq);
			e.printStackTrace();
			//System.err.println("SQL: "+e.getMessage());
			//System.exit(-1);
			return;
		}finally{
			//conn.close();
		}
	}
	
	public double calculate (double ird, String kwid){
		/*
		 * TODO da controllare che con numberOfDocs come int non venga effettuato
		 * automaticamente una conversione ad int (che non andrebbe bene)  
		 */
		if(ird<=0){
			logger.log(Level.WARNING,"ird: "+ird);
		}
		Integer value;
		if( (value = (Integer) formeDocument.get(kwid))!=null){
			double howManyDocs = new Double(value.intValue()).doubleValue();
		/*	if(howManyDocs!=numberOfDocs){
				System.err.println("kwid "+kwid +",howManyDocs "+howManyDocs +"; numberOfDocs "+numberOfDocs +"; ird "+ird);
				System.err.print(numberOfDocs/howManyDocs + " "+Math.log(numberOfDocs/howManyDocs)+" ");
				System.err.println(": "+(Math.log(ird)+1)*(Math.log(numberOfDocs/howManyDocs)));
			}*/
			/* //Stampa x  Stefano Vuono 
			logger.info("kwid: " + kwid + " ird: " + ird + " numberOfDocs: " + numberOfDocs + " howManyDocs: "+howManyDocs + " - " + ( Math.log(ird)+1.0)*(Math.log(numberOfDocs/howManyDocs) ) );
			*/
			return (Math.log(ird)+1.0)*(Math.log(numberOfDocs/howManyDocs));
		}else{
			logger.log(Level.SEVERE,"Term id "+kwid+" not found!");
		}
		return -1000; //Double.NaN;
	}
	
	private void puntIntoFormeDocument (Object key){
		Integer oldValue = (Integer) formeDocument.remove(key);
		if(oldValue != null){
			formeDocument.put(key, new Integer (oldValue.intValue()+1));
			oldValue = null;
		}else{
			formeDocument.put(key,  new Integer(1));
		}
	}

	
	private StringBuffer getOldIds(String idDoc){
		String sql = null;
		try {
	        // Prepare a statement to insert a record
	        sql = "SELECT KW FROM "+ Config.dbTableDocument
	        			+" WHERE DID = "+idDoc;
	        logger.finer(sql);
	        conn = ConnectionManager.getConnection();
	        Statement stmt = conn.createStatement();
	        ResultSet rs = stmt.executeQuery(sql);
	        StringBuffer kws = new StringBuffer();

	        while (rs.next()) {
	        	//System.err.println("getOldIds() "+rs.getString(1));
	        	if(rs.getString(1)!=null){
	        		kws.append(rs.getString(1));
	        		if(!rs.isLast()){
	        			kws.append(",");
	        		}
	        	}
	        }
	        return kws;
		} catch (SQLException e) {
	        logger.log(Level.SEVERE,"In select from "+Config.dbTableDocument);
	        //e.printStackTrace();
	        logger.severe("SQL: "+sql);
	        logger.severe("SQL: "+e.getMessage());
	    }finally{
	    	//conn.close();
	    }
		return null;

	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		logger.log(Level.INFO,"sono partito!");
		logger.log(Level.WARNING,"sono partito1!");
		logger.log(Level.SEVERE,"sono partito2!");
		
		System.out.println(10/0.0);
		/*T2KParserTermFreq ptf = new T2KParserTermFreq();
		ptf.init(args[0]);
		ptf.createDocRows();*/
	}

}
