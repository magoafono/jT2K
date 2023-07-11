package ilc.t2k.statistics;

import ilc.t2k.Config;
import ilc.t2k.Consts;
import ilc.t2k.bean.TermMisura;
import ilc.t2k.database.ConnectionManager;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CalcoloRT {

	private static Logger logger = Logger.getLogger("jt2k");

	//double soglia=Config.soglia_RT;
	double soglia=0.0;
	HashMap glossarioHash;
	Connection conn;
	
	public CalcoloRT(HashMap _glossarioHash, Connection _conn){
		glossarioHash = _glossarioHash;
		conn = _conn;
	}
	
	public int conSim(HashMap hashVerbo, HashMap hashNome, HashMap hashfreqVerbo, 
			HashMap hashfreqVerboNoRel,HashMap hashfreqNome, /*Vector glossario,*/ Vector statistico){
		Vector vettoreV = null;
		Vector terminiSimili = null;
		Vector risultatoFinale = new Vector();
		Vector similarTermsOnAVerb;
		String lemma;
		String verboSino2;
		TermMisura tm;
		HashMap RTTerms;
		Double oldValue;
		int conta=0, i=0;
		//logger.severe("PERIODO: "+((Vector) hashNome.get("PERIODO")).size());
		//Utils.printHashWithVectorValues(hashNome);

		Set glossarioKeyEntry =  glossarioHash.keySet();
		for (Iterator itera = glossarioKeyEntry.iterator(); itera.hasNext();) {
			lemma = (String) itera.next();
			vettoreV = (Vector) hashNome.get(lemma) ;
			if ( vettoreV == null )
			{
				logger.fine("Il Vettore dei verbi collegati al nome "+lemma+ " non e' stato trovato!!" );
				continue;
				//return null;
			}

			terminiSimili = new Vector();
			RTTerms = new HashMap(Consts.HASH_CAPACITY);
			
			conta=0;
			for (Iterator itera2 = vettoreV.iterator(); itera2.hasNext();) {
				verboSino2 = (String) itera2.next();
				if (conta<=9) {
					if (esisteBV(lemma, verboSino2, statistico)==1){
						//logger.fine("BV: "+lemma+" "+verboSino2 );
						
						/* Stampa x Simonetta: */
						//System.err.println("BV: "+lemma+"\t"+verboSino2 );
						/* Fine stampa x Simonetta */
						conta++;
						//singleContextSim restituisce un Vector di TermMisura
						//terminiSimili.addAll(singleContextSim(verboSino2, lemma, hashNome, hashVerbo, 
						//	hashfreqNome, hashfreqVerbo));
						similarTermsOnAVerb = singleContextSim(verboSino2, lemma, hashNome, 
								hashVerbo,hashfreqVerboNoRel, 
								hashfreqNome, hashfreqVerbo, statistico);

						for(int j=0; j< similarTermsOnAVerb.size();j++){
							tm = (TermMisura)similarTermsOnAVerb.get(j);
							/* Stampa x Simonetta: */
							//System.err.println("\t"+tm.getTermine()+ " "+tm.getMisura());
							/* Fine stampa x Simonetta */
							logger.fine(tm.getTermine());

							if(RTTerms.containsKey(tm.getTermine())){
								oldValue = (Double)RTTerms.get(tm.getTermine());
								RTTerms.put(tm.getTermine(), new Double(oldValue.doubleValue() + tm.getMisura()));
							}else{
								RTTerms.put(tm.getTermine(), new Double(tm.getMisura()));
							}
						}
						similarTermsOnAVerb.clear();
						similarTermsOnAVerb = null;
					}
				} else { 
					/*esci fuori dal ciclo dei verbi e prendi il nuovo lemma!*/
					break;
				}
			}
			//logger.severe("PERIODO: "+((Vector) hashNome.get("PERIODO")).size());

			//vettoreV.clear();
			//vettoreV = null;
			//System.err.println("Trovati gli RT, ora li ordino");
			Iterator iter = RTTerms.entrySet().iterator();
			Map.Entry me;
			while(iter.hasNext()){
				me = (Map.Entry) iter.next();
				//System.err.println((String)me.getKey());
				tm = new TermMisura( (String) me.getKey(),((Double) me.getValue()).doubleValue() );
				terminiSimili.add(tm);
			}
			RTTerms.clear();
			RTTerms = null;
			//ordino il vettore terminiSimili sulla misura associata ad ogni termine
			Collections.sort(terminiSimili);
			//logger.severe("PERIODO: "+((Vector) hashNome.get("PERIODO")).size());
			//logger.fine("RT di: "+lemma);
//			Utils.printTermMisuraVector(terminiSimili);
			//seleziono i primi 5 termini (distinti)
			String term; 
			double misura;
			conta = 0;
			//System.err.println("Ora li aggancio al risultato finale");

			for (i=0; i< terminiSimili.size(); i++){
				term = ((TermMisura)terminiSimili.elementAt(i)).getTermine();
				misura = ((TermMisura)terminiSimili.elementAt(i)).getMisura();
				if(!risultatoFinale.contains(term)){
//					System.err.println("Simile a "+lemma +": "+term+" "+misura);
					//System.err.println(lemma +";"+term+";"+misura);
					risultatoFinale.add(term);
					conta++;
				}else{
					logger.warning("Duplicato??? "+lemma +": "+term+" "+misura);
				}
				if( conta >= Config.sogliaTerminiRelated ){
					break;
				}
			}
			terminiSimili.clear();
			terminiSimili = null;
			/*A questo punto in -risultatoFinale- ho tutte le parole "Simili" alla parola Lemma!!!*/
			/*Invio i risultati alla base di dati!!! <- Vedere con Simone*/
			/*for(int i=0; i < risultatoFinale.size(); i++){
				//logger.log(Level.FINE,"simile a "+lemma+ ": " +risultatoFinale.elementAt(i) );
			System.err.println("simile a "+lemma+ ": " +risultatoFinale.elementAt(i));
			}*/
			logger.fine("Inserisco nel DB");
			insertInRTTable(risultatoFinale,lemma);
			risultatoFinale.clear();
		}
		return 1;
	}
	
	/**
	 * 
	 * @param verbo verbo con cui entro
	 * @param nome nome con cui entro
	 * @param hashNome hashmap <nome,vettore_verbi_di_nome>
	 * @param hashVerbo hashmap <verbo,vettore_nomi_di_verbo>
	 * @param hashfreqNome hashmap nome, frequenza
	 * @param hashfreqVerbo hashmap verbo, frequenza
	 * @return
	 */
	private Vector singleContextSim(String verbo, String nome, HashMap hashNome, 
			HashMap hashVerbo, HashMap hashfreqVerboNoRel, HashMap hashfreqNome, HashMap hashfreqVerbo,
			Vector statistico){
		
		double misura=0;
		double freqVerboColl=0,freqNomeColl=0;
		String nomeCollegato, verboCollegato=null;
		Vector vettoreNomi = null;
		Vector vettoreVerbi = null;
		Vector risultato = new Vector();
		TermMisura tm = null;
		/*prendo il vettore dei nomi collegati allo specifico verbo dato*/
		vettoreNomi=((Vector)hashVerbo.get(verbo));
		if ( vettoreNomi == null )
		{
			logger.fine("Vettore dei nomi di " + verbo + " non c'e'!!" );
			//return null;
		}
		/*prendo il vettore dei verbi collegati allo specifico nome dato*/
		vettoreVerbi=((Vector)hashNome.get(nome));
		if ( vettoreVerbi == null )
		{
			logger.fine("Vettore dei verbi di " + nome + " non c'e'!!" );
			//return null;
		}
		logger.fine("Entrato con: "+nome + ", " + verbo);
		//double freqNome=((Integer)hashfreqNome.get(nome)).doubleValue();
		//double freqVerbo=((Integer)hashfreqVerbo.get(verbo)).doubleValue();
		//logger.severe("PERIODO: "+((Vector) hashNome.get("PERIODO")).size());

		for (Iterator iter = vettoreNomi.iterator(); iter.hasNext();) {
			nomeCollegato = (String) iter.next();
			//se il nome collegato al mio lemma non e' nel glossario lo salto
			if(!glossarioHash.containsKey(nomeCollegato)){
				continue;
			}
			misura=0.0;
			logger.fine("Nome: "+nome +" potenziale RT: "+nomeCollegato + " tramite "+verbo);
			if (!nomeCollegato.equals(nome)){
				//modifico la token frequency con la type frequncy.
				//freqNomeColl=((Integer)hashfreqNome.get(nomeCollegato)).doubleValue();
				for (Iterator iter2 = vettoreVerbi.iterator(); iter2.hasNext();) {
					verboCollegato = (String) iter2.next();
					//Rilassato il controllo tra verbo con cui entro e il verbo collegato
					//Ora il confronto si fa a meno della relazione
					//if (!equalWithoutRel(verboCollegato,verbo)){
					//logger.fine("verboCollegato "+verboCollegato);
					if (!verboCollegato.equals(verbo)) { 
						if ( (esiste(nomeCollegato,verboCollegato,hashNome))== 1){ 
							//if(esisteBV(nomeCollegato, verbo,  statistico) == 1){
							/* Aggiunte x controllare che il verbo collegato sia un BV di NOME
							 * e anche di nomeCollegato */
							/*if(esisteBV(nome, verboCollegato,  statistico) == 1)
								if(esisteBV(nomeCollegato, verboCollegato,  statistico) == 1){*/
							//Allineamento del calcolo a quello di Class di Paolo:
							//Ora si prende la frequenza del verbo a meno della relazione
							//che è stata calcolata prima.
							//calcolo della type frequency sia x il nomeCollegato che per il verboCollegato
							logger.fine("nomeCollegato: "+nomeCollegato+ ", verboCollegato: "+verboCollegato);
							freqNomeColl = ((Vector) hashNome.get(nomeCollegato)).size();
							if(freqNomeColl == 0 ){
								logger.fine("1 + "+nomeCollegato+": "+((Vector) hashNome.get(nomeCollegato)).size());
								continue;
							}

							//freqVerboColl=((Integer)hashfreqVerboNoRel.get((verboCollegato.split("/"))[0])).doubleValue();
							freqVerboColl = ((Vector)hashVerbo.get(verboCollegato)).size();
							if(freqVerboColl == 0 ){
								logger.fine("2 + "+verboCollegato+": "+freqVerboColl);
								continue;
							}
							misura += 1.0/(freqVerboColl*freqNomeColl);
							logger.fine(nome+";"+verboCollegato+";"+nomeCollegato+";"+(1.0/(freqVerboColl*freqNomeColl)));
							//System.err.println(nome + ";"+verbo + ";"+nomeCollegato+";"+verboCollegato);
							//}
						}
					}
				}
				/* formula che tiene conto delle frequenze del nome e del verbo con cui sono entrato in conSim() */
				//SIM:21.07.2006
				//misura = misura * 1/freqNome * 1/freqVerbo;
				if ( misura > soglia ) {
					//risultato.add(nomeCollegato);
					logger.fine("Tot: "+nome+" => "+nomeCollegato+" "+misura);
					tm = new TermMisura(nomeCollegato,misura);
					risultato.add(tm);
					//System.err.println("c'e' gia' "+nomeCollegato+"? "+risultato.containsKey(nomeCollegato));
					//risultato.put(nomeCollegato, new Double(misura));
					//logger.fine(nome+";"+verboCollegato+";"+nomeCollegato+";"+misura);
					//System.err.println(misura+";"+nome + ";"+verbo + ";"+nomeCollegato+";"+verboCollegato);
				}
			}
		}

		/*TermMisura termMisura;
		System.err.println("Simili a "+nome);
		for (Iterator itResult = risultato.iterator(); itResult.hasNext();) {
			termMisura = (TermMisura) itResult.next();
			System.err.println("        "+termMisura.getTermine()+" "+termMisura.getMisura());
		}*/
		//logger.fine("fine");

		return risultato;

	}

	private boolean equalWithoutRel(String verboCollegato, String verbo) {
		String a = verboCollegato.split("/")[0];
		String b = verbo.split("/")[0];
		return a.equals(b);
	}

	/*Controlla se la coppia data esiste in sino2.inv*/
	private  int esiste(String nome1, String verbo1, HashMap hashNome){
		
		Vector EvettoreVerbi = ( (Vector) hashNome.get(nome1) );
		if ( EvettoreVerbi == null )
		{
			logger.fine("Il Vettore dei verbi collegati al nome "+nome1+ " non e' stato trovato!!");
			//return null;
		}

		for (Iterator iter = EvettoreVerbi.iterator(); iter.hasNext();) {
			String EverboCollegato = (String) iter.next();
			if ( EverboCollegato.equals(verbo1)) {return 1;}
		}
		return 0;
	}
	/*Controlla se il verbo dato e' tra i primi dieci verbi dello Statistico!*/
	private int esisteBV(String nome1, String verbo1, Vector statistico){
		String verboF;
		String nome;
		int rilevanza=0;
		BigramMisura bigrammaM;
		for (Iterator iteraStat = statistico.iterator(); iteraStat.hasNext();) {
			bigrammaM = (BigramMisura) iteraStat.next();	
			verboF = bigrammaM.getLeft();
			nome = bigrammaM.getRight();
			
			if ((nome1.equals(nome)) && (!verbo1.equals(verboF))){
				rilevanza++;
				if (rilevanza == 10) {return 0;}
				//if (rilevanza == 20) {return 0;}
			}
			if ((nome1.equals(nome)) && (verbo1.equals(verboF))){
				//System.err.println(verboF +" "+nome+" ok, esco con 1");
				return 1;
			}
		}
		
		return 0;
	}
	
	private void insertInRTTable(Vector relatedTerms, String nome){
		//INSERT INTO wbt_app_glossatio_bt VALUES (parent,child)
		int size = relatedTerms.size();
		String term = null;
		String idTerm;
		String idNome = (String) glossarioHash.get(nome);
		
	//	System.err.print("Simili a "+nome+": ");
		for (int i=0; i < size; i++){
			try {
				term = (String) relatedTerms.elementAt(i);
				//System.err.print(" "+term+";");
				idTerm = (String) glossarioHash.get(term);
				// Prepare a statement to insert a record
				String sql = "INSERT INTO "+ Config.dbTableGlossarioRT
				+" VALUES("+idNome+","+idTerm+",0)";
				if(Config.dbDriverName.contains("mysql")){
					sql = "INSERT INTO "+ Config.dbTableGlossarioRT + " (rt_parent, rt_child, stop)"
					+" VALUES("+idNome+","+idTerm+",0)";
				}
				//logger.log(Level.FINE,sql);
				logger.finer(sql);
				conn = ConnectionManager.getConnection();
				Statement stmt = conn.createStatement();
				stmt.executeUpdate(sql,Statement.NO_GENERATED_KEYS);
				
			} catch (SQLException e) {
				//logger.log(Level.SEVERE,"In inserting row to "+Config.dbTableGlossarioBT);
				e.printStackTrace();
			}finally{
				//conn.close();
			}
		}
		//System.err.println();
			
	}
	
	
}
