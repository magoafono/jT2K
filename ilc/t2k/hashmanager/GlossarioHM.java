/**
 *  $Id: GlossarioHM.java,v 1.46 2008/06/20 12:37:09 simone Exp $
 *  
 */
package ilc.t2k.hashmanager;

import ilc.t2k.Config;
import ilc.t2k.Consts;
import ilc.t2k.Utils;
import ilc.t2k.bean.FormFreq;
import ilc.t2k.database.DBManager;
import ilc.t2k.parser.T2KParser;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GlossarioHM {


	HashMap glossarioTermini = new HashMap(Consts.HASH_CAPACITY);
	/**
	 * Rappresenta l'associazione tra lemma e forme che compaiono nel testo.
	 * Si noti che questo Hash ha come chiave il lemma e come valore la coppia
	 * <forma,frequenza della forma>
	 */
	HashMap glossarioLemmaForme  = new HashMap(Consts.HASH_CAPACITY);
	HashMap lemmaIdentificativo = new HashMap(Consts.HASH_CAPACITY);
	HashMap termineId;
	Vector stopWordPattern = null; /* vettore di Pattern*/
	protected T2KParser t2kParser = null;
	//Connection conn = null;
	private static Logger logger = Logger.getLogger("jt2k");
	HashMap nomiPropri = null;
	HashMap prepHashMap = preparePrepHashMap ();

	//private static int idBT = 0;
	//private static int idNT = 0;
	
	//hashmap che memorizza la forma e la prep con cui �� comparsa nella collezione
	//la prep �� diversa da NULL solo se la forma compare nei testo sempre con la stessa prep
	//es: se AI �� l'unica prep di SENSI DELLA LEGGE => nell'hashmap per SENSI DELLA LEGGE
	// ci sara' AI. Se invece SENSI DELLA LEGGE occorresse nella collezione con + prep => alla fine
	//prima di inserire nel glossario avrei per SENSI DELLA LEGGE prep = NULL.
	
	HashMap formaPrep = new HashMap(Consts.HASH_CAPACITY);
	
	public void init(/*HashMap _glossarioTermini, HashMap _glossarioTerminiForma,*/
			/*HashMap _formaIdentificativo, HashMap _termineId,*/ T2KParser _t2kParser/*,
			Connection _conn*/){
		termineId = new HashMap(Consts.HASH_CAPACITY);
		t2kParser = _t2kParser; //parser dell'uscita del t2c (ex Daniela+)
		//conn = _conn;
	}
	/**
	 * Costruisce i glossari di termini, di NT e di BT
	 * Opera la ricerca tramite un parser che restituisce elementi da elaborare.
	 * In particolare, inserisce in un hashmap i bigrammi <termine,forma> e un altro
	 * hash inserisce solo i termini.
	 */
	public  void creaGlossario(){
		String[] row;
		int freqLemma=0;
		FormFreq formFreq = null;
		FormFreq ff = null;
		Vector forms=null;
		String[] prep = null;
		String preposition = null;
		

		while ((row = (String[]) t2kParser.getNext())!= null) {
			//il primo elemento di row e' il termine => lo devo mettere nell'hash dei termini
			//con frequenza la somma delle frequenze delle sue forme
			String[] forma = null;
			//quando recupero il termine da termfreq posso avere __ (caso del coord)
			String lemma = row[0].replaceAll("_+"," ");

			/*System.err.print("termine "+termine);
			System.err.println("; row.length: "+row.length);
			for (int jj=0; jj < row.length; jj++) {
				System.err.println("row["+jj+"]: "+row[jj]);
			}*/

			freqLemma=0;
			//int maxFreq = 0; //Non ho capito a che serve... 05.10.2007			
			//row= {19_LUGLIO_1993=1}, {LEGGE=106}, {LEGGI=7}
			formFreq = new FormFreq();
			for (int i = 1; i < row.length; i++) {
			//	System.err.print("ROW "+i+" "+row[i]+" ");
				try{

					//splitto sulla | per eliminare (per il momento) l'eventuale PREP trovata
					//da t2cmapper

					prep = row[i].split("\\*"); //
					/*System.err.println(" prep.length: "+prep.length);
					for (int jj=0; jj < prep.length; jj++) {
						System.err.println("prep["+jj+"]: "+prep[jj]);
					}*/
					if ( prep.length > 1) {
						//vuol dire che c'era la prep => la elimino
						//System.err.print("PREP prep: *"+prep[1]+"*");
						preposition = prep[0].toUpperCase();
						forma = prep[1].split("=");
						//System.err.println(" forma: *"+forma[0]+"*");
					} else{
						//System.err.print("PREP prep: *"+prep[0]+"*");
						forma = prep[0].split("=");
						preposition = null;
						//System.err.println(" forma: *"+forma[0]+"*");
					}

					if(forma==null){
//						System.err.println("AAAAAAAAAAAA forma: null");
						logger.fine("forma is null!");
						continue;
					}
					if(forma.length<2){
						//logger.finer("forma: *"+forma[0]+"*");
//						System.err.println("AAAAAAAAAAAA forma: *"+forma[0]+"*");
						continue;
					}
					/* Non ho capito a che serve... 05.10.2007	 	
					 * 	if (maxFreq < Integer.parseInt(forma[1])){
						formFreq.setForm(forma[0]);
						maxFreq = Integer.parseInt(forma[1]);
					}
					 */
					freqLemma += Integer.parseInt(forma[1]);
					if(forms == null){
						forms = new Vector();
					}
					ff = new FormFreq();
					ff.setForm(forma[0]);
					ff.setFreq(new Integer((forma[1])));
					forms.addElement(ff);
					
					if ( Config.enableStop ) {
						putInHashFormaPrep(forma[0], preposition);
					}

				}catch (Exception e) {
					logger.info("Riga malformata... skip");
				}
			}
			//System.err.println();
			if (formFreq != null) {
				/*if(formFreq.getForm().("INCENTIVI PER LA")){
					System.err.print(termine+", ");
					System.err.println(formFreq.getForm()+": "+freqTermine);
				}*/
				formFreq.setFreq(new Integer(freqLemma));
				//row.length mi serve per calcolare la idf
				putInHashComplexValue(glossarioLemmaForme, lemma/*,formFreq*/, forms);
			}
			putInHash(glossarioTermini, lemma, freqLemma);
			formFreq = null;
			if (forms != null) {
				forms.clear();
				forms = null;
			}
		}
	}

	/**
	 * Gestore della hashmap che associa per ogni forma la prep con la quale occorre nel testo.
	 * Il valore associato alla chiave (forma) e' diverso da null e dal valore speciale prepdiverse
	 * sse la forma compare nel testo con almeno 2 preposizioni diverse (si considera prep diversa
	 * anche l'assenza della prep stessa)
	 * 
	 * @param forma 
	 * @param preposition
	 */
	private void putInHashFormaPrep(String forma, String preposition) {
		
		//se la preposition != null => devo verificare se nella hashmap compare con una prep diversa
		//da quella in input. Se diverse => metto il valore speciale prepdiverse per quella forma
		//System.err.println("1 - forma="+forma+", preposition="+preposition);
		if ( null != preposition) {
			if ( prepHashMap.containsKey(preposition.toLowerCase()) ) { 
				//System.err.println("2 - la prep e' diversa da " + Consts.INVALID_PREP);
				logger.finer("forma="+forma+", preposition="+preposition);
				String prep = (String) formaPrep.get(forma);
				logger.finer("nella hashmap trovo prep="+prep);
				if (null != prep) {
					if (! "prepdiverse".equals(prep)) {
						if (!prep.equals(preposition)) {
							//se sono diversi => il termine compare con prep diverse nei testi => non lo butto via.
							formaPrep.put(forma, "prepdiverse");
						} else {
							//rimane nella hashmap il vecchio valore associato alla prep, termine non e' candidato a entrare
							//nel glossario.
							//System.err.println("putInHashFormaPrep(): le prep sono uguali: "+ preposition + "="+prep);
						}
					}
				} else {
					formaPrep.put(forma, preposition);
				}
//				System.err.println("putInHashFormaPrep(): formaPrep.containsKey("+termine+")="+formaPrep.containsKey(termine));
			}else {
				logger.fine("forma="+forma+", preposition ="+preposition);
				formaPrep.put(forma, "prepdiverse");
			}
		} else {
			formaPrep.put(forma, "prepdiverse");
		}
		//System.err.println("putInHashFormaPrep(): termine="+termine+", preposition ="+preposition+ ", in hash: "+ formaPrep.get(termine));
	}
	
	
	//ordina i termini del glossario in base alla misura selezionata nel file di configurazione
	public Object[] orderGlossarioTerms(int numOfDocuments){
		
		Set entries = glossarioLemmaForme.entrySet();
	    Iterator it = entries.iterator();
	    System.out.println("orderGlossarioTerms(): select measure is " + Config.measureType);
	    while (it.hasNext()) {
	    	//Map.Entry: getKey()=lemma, getValue()=FormaFreq
	      Map.Entry entry = (Map.Entry) it.next();
	      FormFreq ff = (FormFreq) entry.getValue();
	      if (Config.measureType.equals("tf")) {
	    	  ff.setMeasure(ff.getFreq().intValue() );
	      } else if (Config.measureType.equals("idf")) {
		      ff.setMeasure(Math.log(((double)numOfDocuments)/ff.getDf())  );
	      } else if (Config.measureType.equals("tfidf")) {
		      ff.setMeasure(Math.log(((double)numOfDocuments)/ff.getDf()) * ff.getFreq().intValue() );
	      }
	      System.out.println("MEASURE: " + ff.getForm() + "-->" + ff.getMeasure() + " -- " + ff.getDf());
	    }

	    Object[] entrySet = entries.toArray();
		Arrays.sort (entrySet, new Comparator() {
			public int compare(Object o1, Object o2) {
				//ordinamento decrescente
				double a = ((FormFreq) ((Map.Entry)o1).getValue()).getMeasure();
				double b = ((FormFreq) ((Map.Entry)o2).getValue()).getMeasure();
				return ((b - a) > 0) ? 1 :-1 ;
			}
		});

		return entrySet;
		
	}
	
	
	public Object[] termFrequency2 (HashMap hash) {
		
		Object[] entrySet = hash.entrySet().toArray();
		//Ordino in maniera decrescente di valore tutto l'EntrySet
		Arrays.sort (entrySet, new Comparator() {
			public int compare(Object o1, Object o2) {
				//ordinamento decrescente
				double a = ((FormFreq) ((Map.Entry)o1).getValue()).getFreq().intValue();
				double b = ((FormFreq) ((Map.Entry)o2).getValue()).getFreq().intValue();
				return ((b - a) > 0) ? 1 :-1 ;
			}
		});
		return entrySet;
	}
	
	public Object[] inverseDocumentFrequency2 (HashMap hash, int numOfDocuments) {
		
		Set entries = hash.entrySet();
	    Iterator it = entries.iterator();
	    while (it.hasNext()) {
	    	//Map.Entry: getKey()=lemma, getValue()=FormaFreq
	      Map.Entry entry = (Map.Entry) it.next();
	      FormFreq ff = (FormFreq) entry.getValue();
	      ff.setMeasure(Math.log(((double)numOfDocuments)/ff.getDf()) );
	      
	      System.out.println("IDF: " + ff.getForm() + "-->" + ff.getMeasure() + " -- " + ff.getDf());
	    }
	    Object[] entrySet = entries.toArray();
		Arrays.sort (entrySet, new Comparator() {
			public int compare(Object o1, Object o2) {
				//ordinamento decrescente
				double a = ((FormFreq) ((Map.Entry)o1).getValue()).getMeasure();
				double b = ((FormFreq) ((Map.Entry)o2).getValue()).getMeasure();
				return ((b - a) > 0) ? 1 :-1 ;
			}
		});
	    
		return entrySet;

	}
	
		
	/**
	 * Inserisce il glossario <termine,frequenza> nel DB (ex glossarioA.txt)
	 *
	 */
	public void putGlossarioIntoDB(int numOfDocuments){
		//prima di tutto si deve inserire in wbt_app_glossario
/*		
 * 14.07.2009: separato l'ordinamento dei termini: introdotte le misure
 * Object[] entrySet = glossarioLemmaForme.entrySet().toArray();
		//Ordino in maniera decrescente di valore tutto l'EntrySet
		Arrays.sort (entrySet, new Comparator() {
			public int compare(Object o1, Object o2) {
				//ordinamento decrescente
				int a = ((FormFreq) ((Map.Entry)o1).getValue()).getFreq().intValue();
				int b = ((FormFreq) ((Map.Entry)o2).getValue()).getFreq().intValue();
				return b - a ;
			}
		});
	*/	
		
//		System.err.println("putGlossarioIntoDB size=" + formaPrep.size());
		
		/*Iterator it = formaPrep.keySet().iterator();
		Iterator it2 = formaPrep.values().iterator();
	    while (it.hasNext()) {
	        // Get key
	        String key = (String) it.next();
	        String value = (String) it2.next();
	        System.err.println("putGlossarioIntoDB(): key: "+key+ ", value="+value );
	    }*/

		Object[] entrySet = orderGlossarioTerms( numOfDocuments);		

	    String prep = null;
	    int idTerm = 0;
		for (int i = 0; i < entrySet.length; i++) {
			//se la frequenza del termine e' maggiore della soglia del glossario
			//int freq = ((Integer)((Map.Entry)entrySet[i]).getValue()).intValue();
			FormFreq formaFreq = (FormFreq) ((Map.Entry)entrySet[i]).getValue();
			String lemma = (String) ((Map.Entry)entrySet[i]).getKey();
			int freq = formaFreq.getFreq().intValue();
			String forma = ((formaFreq.getForm()).replaceAll("_+"," ")).trim();
			//System.err.print("formaFreq: "+formaFreq.getForm());
			//System.err.println(": "+formaFreq.getFreq().intValue());
			
			if ( Config.enableStop ) {
				prep = (String) formaPrep.get(forma);
			//	System.err.println("Il termine: "+forma+" compare con la prep: " + prep);
				if ( null != prep ) {
					//System.err.println("Il termine: "+forma+" compare con la prep: " + prep );
					if (!"prepdiverse".equals(prep)){
						//vuol dire che la forma compare nel testo solo con quella prep
					//	System.err.println("Il termine: "+forma+" compare solo con la prep:" + prep + ", skip!");
						//continue;
						prep = prep.replaceAll("'", "''");
					} else {
						prep = null;
					}
				}
			}
			if (formaFreq.getForm()==null) { 
				continue; 
			}
			if (freq >= Config.soglia_glossario){
				//QUI VA FATTO L'EVENTUALE RIMOZIONE DI STOP-WORD
				if(stopWordPattern!=null){
					//System.err.println("Candidata x il glossario:" +forma);
					if (isStopWord(forma)){
						logger.fine("STOP WORD: " +forma);
						continue;
					}
				} else {
					//logger.fine("No stop word file!");
				}
				logger.fine("DB: "+lemma+" "+forma);
				//System.err.println("lemma: " + lemma +", forma: " + forma + ", freq: " + freq);
				if ( Config.enableStop ) {
					//System.err.println("lemma: " + lemma +", forma: " + forma + ", freq: " + freq + ", prep: "+prep);
					idTerm = insertIntoGlossarioDB(lemma, forma, freq, prep);
				} else {
					idTerm = insertIntoGlossarioDB(lemma, forma, freq);
				}
				if(idTerm == -1){
					logger.severe("In inserting term "+forma);
					//	break;
				}
				lemmaIdentificativo.put(lemma, Integer.toString(idTerm));
//				formaIdentificativo.put(forma,Integer.toString(idTerm));
				//TermineId tid = new TermineId((String)(((Map.Entry)entrySet[i]).getKey()),new Integer(idTerm));
				//termineId.addElement(tid);
				//termineId.put(((Map.Entry)entrySet[i]).getKey(),Integer.toString(idTerm));
				termineId.put(((Map.Entry)entrySet[i]).getKey(),Integer.toString(idTerm));
			}else{
				/* poiche' i termini successivi a questo (che non supera la soglia) hanno
				 * una frequenza che non supera la soglia e non entrerebbero nel glossario
				 * => non proseguo nel for ma esco
				 */
				break;
			}
		}
	}

	/**
	 * Costruisce i glossari BT e NT.
	 * Presuppone che sia gia' stato inserito nel DB il glossario <termine,frequenza> 
	 * (ex glossarioA.txt) tramite la successione di chiamate a <i>creaGlossario()</i> e
	 * <i>putGlossarioIntoDB()</i> per ottenere gli IDs dei termini dal DBMS.
	 */
	public void creaGlossarioBTNT(){
		Vector ids = null;
		//Ordino l'Hash dei <termine,idTermine> sul termine

		Object[] entrySet = lemmaIdentificativo.entrySet().toArray();
		int entryLenth = entrySet.length;
		Arrays.sort(entrySet,new Comparator(){
			public int compare(Object o1, Object o2) {
				//ordinamento decrescente
				return ((String)((Map.Entry)o1).getKey()).compareTo(((String)((Map.Entry)o2).getKey()));
			}
		});
		for (int i = 0; i < entryLenth; i++) {

			String term = (String)((Map.Entry)entrySet[i]).getKey();
			String id = (String)((Map.Entry)entrySet[i]).getValue();
			//System.err.println("creaGlossarioBTNT: termine: "+term);
			//si controlla se il termine e' un nome proprio: se si => si salta e non si cercano bt/nt
			if(nomiPropri!=null){
				if(nomiPropri.containsKey(term)){
					//se il termine e' un nome => lo salto e vado avanti
					logger.finer("NOME  : "+term);
					continue;
				}
			}
			//logger.info("creaGlossarioBTNT: *"+term+" ");
			String testa = estraiTesta(term);
			//logger.info("creaGlossarioBTNT: *"+id+"* *"+term+"* *" + testa+"* "+i+ " "+entryLenth);
			while(true){
				if( (i+1) < entryLenth){
					String nextTerm = (String)((Map.Entry)entrySet[i+1]).getKey();
					String nextId = (String)((Map.Entry)entrySet[i+1]).getValue();
					//se il termine successivo ha in comune la testa con il termine
					//principale => sono in relazione BT/NT
					try{
						if(nextTerm.matches(testa)){
							if(ids==null){
								//System.err.println("ids is null, creating vector" );
								ids = new Vector();
							}
							//System.err.println("creaGlossarioBTNT: nextId: "+nextId+" "+nextTerm);
							ids.addElement(nextId);
							i++;
						}else{
							//System.err.println("creaGlossarioBTNT: nextTerm: "+nextTerm+" not matches with "+testa);
							//se non hanno la testa in comune => nemmeno i successivi la avranno
							//xche' sono ordinati alfabeticamente
							break;
						}
					}catch (Exception e) {
						logger.info("Testa malformata: "+testa+" Non puo' essere usata! Skip...");
						//System.err.println("testa: "+testa);
						//i++;
						break;
					}
				}else{
					break;
				}
			}
			if(ids!=null){
				//c'era almeno un BT/NT
				//System.err.println("ids: "+ids.size());
				for(int j=0; j < ids.size();j++){
					//vanno fatte 2 insert: una x i BT e una per gli NT
					DBManager.insertInBTtable(id,(String)ids.elementAt(j)/*,conn*/);
					DBManager.insertInNTtable(id,(String)ids.elementAt(j)/*,conn*/);
				}
				ids=null;
			}/*else{
				System.err.println("ids is null!");
			}*/
		}
	}



	private String estraiTesta(String termine){
		if(termine==null){
			return null;
		}
		String testa = termine;
		//seleziono la prima parola del termine: es. "BANCA DATI" seleziono "BANCA"
		//testa = testa.replaceFirst("(.+)\\s.+","$1"); 
		/* 23/01/2006: commentata per ovviare a problemi del tipo 
		 * AGENTE INTELLIGENTE bt di AGENTI SOFTWARE
		 */
		if ( testa.matches("(CA|CHE)$") ){
			testa = testa.replaceAll("(CA|CHE)$","(CA|CHE) .+");
		}else{
			testa = testa.replaceAll("(.)$","[AEIOU'] .+");
		}
		return testa;
	}
	/**
	 * Inserisce in wbt_app_glossario la coppia <termine,frequenza>
	 * e raccoglie l'ID assegnato dal DBMS a quel termine.
	 * @param forma termine
	 * @param freq numero di volte che il termine occorre nella collezione
	 * @return l'ID del termine inserito (auto generato dal DBMS)
	 * @throws SQLException 
	 */
	int insertIntoGlossarioDB(String termine, String forma, int freq, String prep) {
		//TODO deve creare la stringa di inserimento nel DB
		//INSERT INTO wbt_app_glossario VALUES (id[autoincrement],term, freq);
		/*
		 * TODO:NB: controllare se il metodo getGeneratedKeys() eseguito
		 * su uno statement di INSERT restituisce il campo autogenerated (che corrisponde
		 * al id del termine nel glossario.
		 * Se cosi' fosse si puo' costruire una nuova HashMap che ha come key il termine
		 * e come value l'ID del termine nella tabella wbt_app_glossario.
		 * HashMap.put(term, Statement.getGeneratedKeys())
		 * SI!!! getGeneratedKeys() restituisce l'ID del l'ultima insert fatta!!
		 */
		if(forma.lastIndexOf('\'')!= -1 ){
			forma = Utils.reaccent(forma);
		}
		//escaping apostrophes
		termine = termine.replaceAll("'", "''");
		forma = forma.replaceAll("'","''");
		return DBManager.insertIntoGlossarioTable (termine, forma, freq, prep/*, conn*/);

	}
	
	int insertIntoGlossarioDB(String termine, String forma, int freq) {
		return  insertIntoGlossarioDB(termine, forma, freq, null) ;
	}
	/**
	 * Inserisce <i>key</i> nella HashMap <i>hash</i>
	 * Se l'elemento inserito non e' gia' presente, il valore e' posto
	 * a <i>freq</i>. Se al contrario l'elemento e' gia' presente allora viene
	 * incrementato di <i>freq</i> il valore associato.
	 * @param hash HashMap nella quale si vuole inserire l'elemento
	 * @param key elemento da inserire
	 * @param value valore associato all'elemento da inserire
	 */
	private void putInHash( HashMap hash, Object key, int value){
		Integer oldValue = (Integer) hash.get(key);
		if(oldValue != null){
			hash.put(key, new Integer (oldValue.intValue()+value));
			oldValue = null;
		}else{
			hash.put(key,  new Integer(value));
		}
	}

	/**
	 * 
	 * @param hash
	 * @param key
	 * @param value
	 */
	private void putInHashComplexValue( HashMap hash, Object key,/*FormFreq value,*/Vector forms){
		if(forms != null){
			Vector oldValue = (Vector) hash.get(key);
			if (oldValue != null) {
				for (int i=0; i < forms.size(); i++) {
					FormFreq ff = (FormFreq) forms.elementAt(i);
					//System.err.print("ff["+i+"]: "+ff.getForm() + " ");
					int pos = searchElement(oldValue, ff.getForm());
					if(pos != -1){
						FormFreq elem = (FormFreq)oldValue.elementAt(pos);
						Integer newFreq = new Integer(elem.getFreq().intValue()+ff.getFreq().intValue());
						elem.setDf(elem.getDf() + 1);
						elem.setFreq(newFreq);
						oldValue.remove(pos);
						oldValue.addElement(elem);
						//System.err.println("putInHashComplexValue():" + elem.getForm() + ": "+elem.getFreq() + ": "+elem.getDf());
					}else{
						FormFreq elem = new FormFreq();
						elem.setForm(ff.getForm());
						elem.setFreq(ff.getFreq());
						oldValue.addElement(elem);
						//System.err.println("putInHashComplexValue():" + elem.getForm() + ": "+elem.getFreq()+ ": "+elem.getDf());
					}
				}
				//System.err.println();

				hash.put(key,oldValue);

			} else {
				//se non c'era il vecchio valore, inserisco il nuovo
				Vector newVect = new Vector();
				/*FormFreq elem = new FormFreq();
			elem.setForm(value.getForm());
			elem.setFreq(value.getFreq());
			newHash.addElement(elem);*/
				newVect.addAll(forms);
				hash.put(key,  newVect);
			}
		}
	}

	private int searchElement(Vector vect, String str){

		if(vect == null){
			return -1;
		}
		FormFreq elem = null; 
		for (int i = 0; i < vect.size(); i++) {
			elem = (FormFreq) vect.elementAt(i);
			if(elem.getForm().equals(str)){
				return i;
			}
		}
		return -1;
	}

	//inserisce nel DB le varianti afferenti una forma lemmatizzata
	public void putVariantsDB(HashMap variantiHM) {

		Set set = null;
		Iterator it = null;
		Map.Entry elem = null;
		HashMap vect = null;
		String termine = null;
		Iterator itVariants = null;
		int id;
		String freq = null;
		Integer occorrenze = null;
		int soglia = 0;
		
		if (null != variantiHM) {
			set = variantiHM.entrySet();
			if ( null != (it = set.iterator()) ) {
				while (it.hasNext()) {
					elem = (Map.Entry) it.next();
					if ( null != (vect = (HashMap) elem.getValue()) ) {
						if ( null != ( termine =  (String) elem.getKey()) )  {
							if ( null != ( freq = (String) lemmaIdentificativo.get(termine))) {
								id = Integer.parseInt(freq);
								itVariants = vect.entrySet().iterator();
								soglia = calcolaSogliaVarianti(vect.entrySet());
								while (itVariants.hasNext()) {
									elem = (Map.Entry) itVariants.next();
									//NOTA: per il momento si eliminano le varianti che hanno frequenza = 1 : introducono molto rumore nei risultati
									// 			dovuti a probabili ptoblemi di disallineamento.
									if ( null != (occorrenze = (Integer) elem.getValue()) ){
										if ( occorrenze.intValue() >= soglia)  {
											DBManager.insertInVariatsTable(id, (String) elem.getKey(), occorrenze/*, conn*/);
										}
									}
								} /*else {
								System.err.println ("Lemma: " + termine + " non trovato!");
							}*/
							}
						}
					}
				}
			}
		}
	}




	private int calcolaSogliaVarianti(Set entrySet) {

		Iterator itVariants = entrySet.iterator();
		int soglia = 0;
		int occorrenze = 0;
		Map.Entry elem = null;

		while (itVariants.hasNext()) {
			elem = (Map.Entry) itVariants.next();
			occorrenze += ((Integer) elem.getValue()).intValue();
		}
		soglia = Math.round (occorrenze * Config.sogliaVarianti / 100.0f);
		//System.err.println("Soglia: " + soglia + ", occorrenze: " + occorrenze + ", soglia: "+ Config.sogliaVarianti);
		if ( soglia < 2) {
			soglia = 2;
		}
		return soglia;

	}


	public void clear(){

		if(null != glossarioTermini){
			glossarioTermini.clear();	
		}
		if(null != glossarioLemmaForme){
			glossarioLemmaForme.clear();
		}
		if(null != lemmaIdentificativo){
			lemmaIdentificativo.clear();
		}
		if(null != termineId){
			termineId.clear();
		}
		if(null != stopWordPattern){
			stopWordPattern.clear();
		}
		if(null != nomiPropri){
			nomiPropri.clear();
		}

		glossarioTermini = null;
		glossarioLemmaForme = null;
		lemmaIdentificativo = null;
		termineId = null;
		stopWordPattern = null;
		nomiPropri = null;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		/*	T2KParserT2C t2c = new T2KParserT2C();
		t2c.init(args[0]);
		LinkedHashMap terms = new LinkedHashMap();
		LinkedHashMap termsforms = new LinkedHashMap();
		LinkedHashMap formIds = new LinkedHashMap();
		Vector termIds = new Vector();

		GlossarioHM bighm = new GlossarioHM();
		bighm.init(terms,termsforms,formIds,termIds,t2c,null);
		long start = System.currentTimeMillis();
		bighm.creaGlossario();

		long finish = System.currentTimeMillis();
		System.out.println("time: "+(finish-start));
/**
		Iterator it = bighm.glossarioTermini.entrySet().iterator();
		while (it.hasNext()){
			Map.Entry elem = (Map.Entry)it.next();
			System.out.println(elem.getKey()+" "+elem.getValue());
		}
		/*/
		/*Iterator it2 = bighm.glossarioTerminiForma.entrySet().iterator();
		System.out.println("TERMINI-FORMA");
		while (it2.hasNext()){
			Map.Entry elem = (Map.Entry)it2.next();
			System.err.print(((String)elem.getKey())+"|");
			System.err.println(((FormFreq)elem.getValue()).getFreq()+"|"+
					((FormFreq)elem.getValue()).getForm());
		}*/
	}

	protected boolean isStopWord(String word){
		int sizeOfStopWordPattern = stopWordPattern.size();
		Pattern patt;
		//System.err.println("isStopWord(): *"+word+"*");
		for (int i=0 ; i < sizeOfStopWordPattern ; i++){
			patt = (Pattern) stopWordPattern.elementAt(i);
			Matcher match = patt.matcher(word);
			if (match.matches()){
				//if (match.find()){
				//System.err.println("STOP: "+word);
				return true;
			}
		}
		return false;
	}

	/**
	 * @return Returns the glossarioTerminiForma.
	 */
	public HashMap getGlossarioLemmaForme() {
		return glossarioLemmaForme;
	}
	/**
	 * @param glossarioTerminiForma The glossarioTerminiForma to set.
	 */
	public void setGlossarioTerminiForma(HashMap glossarioTerminiForma) {
		this.glossarioLemmaForme = glossarioTerminiForma;
	}
	/**
	 * @return Returns the stopWordPattern.
	 */
	public Vector getStopWordPattern() {
		return stopWordPattern;
	}
	/**
	 * @param stopWordPattern The stopWordPattern to set.
	 */
	public void setStopWordPattern(Vector stopWordPattern) {
		this.stopWordPattern = stopWordPattern;
	}
	/**
	 * @return the nomiPropri
	 */
	public HashMap getNomiPropri() {
		return nomiPropri;
	}
	/**
	 * @param nomiPropri the nomiPropri to set
	 */
	public void setNomiPropri(HashMap nomiPropri) {
		this.nomiPropri = nomiPropri;
	}
	/**
	 * @return the formaIdentificativo
	 */
	public HashMap getFormaIdentificativo() {
		return lemmaIdentificativo;
	}
	/**
	 * @param formaIdentificativo the formaIdentificativo to set
	 */
	public void setFormaIdentificativo(HashMap formaIdentificativo) {
		this.lemmaIdentificativo = formaIdentificativo;
	}

	
	/**
	 * Metodo veramente brutto! e' solo x evitare di aggiungere un nuovo file 
	 * alla distribuzione jt2k. Le prep sono cablate nel codice! :-(
	 */
	HashMap preparePrepHashMap () {
		HashMap map = new HashMap(Consts.HASH_CAPACITY);
		map.put("tipo",new Integer(1));
		map.put("contro",new Integer(1));
		map.put("avanti",new Integer(1));
		map.put("davanti",new Integer(1));
		map.put("vicino",new Integer(1));
		map.put("dinanzi",new Integer(1));
		map.put("dinnanzi",new Integer(1));
		map.put("fuor",new Integer(1));
		map.put("lontano",new Integer(1));
		map.put("anzi",new Integer(1));
		map.put("malgrado",new Integer(1));
		map.put("lungo",new Integer(1));
		map.put("salvo",new Integer(1));
		map.put("attraverso",new Integer(1));
		map.put("circa",new Integer(1));
		map.put("dentro",new Integer(1));
		map.put("dietro",new Integer(1));
		map.put("durante",new Integer(1));
		map.put("eccetto",new Integer(1));
		map.put("entro",new Integer(1));
		map.put("ex",new Integer(1));
		map.put("extra",new Integer(1));
		map.put("fino",new Integer(1));
		map.put("fin",new Integer(1));
		map.put("fuori",new Integer(1));
		map.put("infra",new Integer(1));
		map.put("innanzi",new Integer(1));
		map.put("mediante",new Integer(1));
		map.put("meno",new Integer(1));
		map.put("men",new Integer(1));
		map.put("oltre",new Integer(1));
		map.put("piu'",new Integer(1));
		map.put("presso",new Integer(1));
		map.put("pro",new Integer(1));
		map.put("rasente",new Integer(1));
		map.put("secondo",new Integer(1));
		map.put("senza",new Integer(1));
		map.put("senz'",new Integer(1));
		map.put("sino",new Integer(1));
		map.put("sin",new Integer(1));
		map.put("sopra",new Integer(1));
		map.put("sotto",new Integer(1));
		map.put("sott'",new Integer(1));
		map.put("sovra",new Integer(1));
		map.put("tramite",new Integer(1));
		map.put("tranne",new Integer(1));
		map.put("verso",new Integer(1));
		map.put("a",new Integer(1));
		map.put("ad",new Integer(1));
		map.put("da",new Integer(1));
		map.put("d'",new Integer(1));
		map.put("di",new Integer(1));
		map.put("de",new Integer(1));
		map.put("fra",new Integer(1));
		map.put("in",new Integer(1));
		map.put("per",new Integer(1));
		map.put("su",new Integer(1));
		map.put("tra",new Integer(1));
		map.put("con",new Integer(1));
		map.put("com'",new Integer(1));
		map.put("dopo",new Integer(1));
		map.put("come",new Integer(1));
		map.put("al",new Integer(1));
		map.put("allo",new Integer(1));
		map.put("alla",new Integer(1));
		map.put("ai",new Integer(1));
		map.put("agli",new Integer(1));
		map.put("alle",new Integer(1));
		map.put("all'",new Integer(1));
		map.put("dal",new Integer(1));
		map.put("dallo",new Integer(1));
		map.put("dalla",new Integer(1));
		map.put("dai",new Integer(1));
		map.put("dagli",new Integer(1));
		map.put("dalle",new Integer(1));
		map.put("dall'",new Integer(1));
		map.put("del",new Integer(1));
		map.put("dello",new Integer(1));
		map.put("della",new Integer(1));
		map.put("dei",new Integer(1));
		map.put("degli",new Integer(1));
		map.put("delle",new Integer(1));
		map.put("dell'",new Integer(1));
		map.put("nel",new Integer(1));
		map.put("nello",new Integer(1));
		map.put("nella",new Integer(1));
		map.put("nei",new Integer(1));
		map.put("negli",new Integer(1));
		map.put("nelle",new Integer(1));
		map.put("nell'",new Integer(1));
		map.put("sul",new Integer(1));
		map.put("sullo",new Integer(1));
		map.put("sulla",new Integer(1));
		map.put("sui",new Integer(1));
		map.put("sugli",new Integer(1));
		map.put("sulle",new Integer(1));
		map.put("sull'",new Integer(1));
		map.put("col",new Integer(1));
		map.put("collo",new Integer(1));
		map.put("colla",new Integer(1));
		map.put("coll'",new Integer(1));
		map.put("cogli",new Integer(1));
		map.put("colle",new Integer(1));
		map.put("coi",new Integer(1));
		
		return map;
	}


}


