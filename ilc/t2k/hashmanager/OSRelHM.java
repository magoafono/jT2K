package ilc.t2k.hashmanager;

import ilc.t2k.Consts;
import ilc.t2k.bean.Bigrams;
import ilc.t2k.parser.T2KParser;

import java.util.HashMap;
import java.util.Vector;
import java.util.logging.Logger;

public class OSRelHM {

	protected HashMap nounFreq = new HashMap(Consts.HASH_CAPACITY); //sino2.noun.freq
	protected HashMap verbFreq = new HashMap(Consts.HASH_CAPACITY);
	protected HashMap verbFreqNoRel = new HashMap(Consts.HASH_CAPACITY); //sino2.verb.prep.freq
	protected HashMap nounVerbFreq = new HashMap(Consts.HASH_CAPACITY);
	protected HashMap nounVerb = new HashMap(Consts.HASH_CAPACITY); //	sino2.inv (hashmap di vector)
	protected HashMap verbNoun = new HashMap(Consts.HASH_CAPACITY); //	sino2.inv (hashmap di vector)
	protected HashMap stopPronVerb;
	protected T2KParser t2kParser;
	protected int numOfBigrams;
	//Connection conn;
	private static Logger logger = Logger.getLogger("jt2k");
	
	//private static int idBT = 0;
	//private static int idNT = 0;
	
	public  void init(/*HashMap _nounFreq, HashMap _verbFreq, HashMap _verbFreqNoRel,
			HashMap _nounVerbFreq, HashMap _nounVerb, HashMap  _verbNoun, */
			T2KParser _t2kParser, HashMap _stopVerb /*, Connection _conn*/){
		//nounFreq = _nounFreq; //sino2.noun.freq
		//verbFreq = _verbFreq; 
		//verbFreqNoRel = _verbFreqNoRel; //sino2.verb.prep.freq
		//nounVerbFreq = _nounVerbFreq; //sino2.inv (hashmap di vector)
		//nounVerb = _nounVerb;
		//verbNoun = _verbNoun;
		t2kParser = _t2kParser; //parser dell'uscita del t2c (ex Daniela+)
		stopPronVerb = _stopVerb;
		numOfBigrams = 0;
		//conn = _conn;
	}

	
	public  int findNGrams(){
		String[] bigram;
		
		/* prendo un chunk */
		while ((bigram = (String[]) t2kParser.getNext())!= null) {
			/* rimuovo, se esiste, l'unigramma dall'hash */
			/* il primo e' il bigramma */
			if(bigram == null){
				//System.err.println("errore nel parsing del file di ideal");
			}else if (bigram.length != 2){
				logger.fine("Malformed row... skip");
				//System.err.println("errore nel parsing del file di ideal");
				continue;
			}
			/* I bigrammi sono della seguente forma:
			 * Verb/Rel Noun */
			//va trasformato il noun togliendo sostituendo i doppi _ con i singoli _
			//s/__/_/g
			//inoltre vanno tolti le preposizioni articolate
			/*String noun = bigram[1].replaceAll("__","_");
			noun = noun.replaceAll(Consts.PREP_ARTICOLATE,"$1_$4");*/
			
			/* Qui va fatto il controllogli degli stop verbs*/
			/* Controllo subito che nel bigramma il verbo non sia uno stop verb*/

			//logger.fine("bigram[0]: "+bigram[0]);
			if(bigram[0].contains("|")){
				//String temp = bigram[0];
				bigram[0] = bigram[0].replaceFirst("\\|.+/", "/");
				//bigram[0] = temp;
				//logger.fine("	ripulito: "+temp);
				//logger.fine("	ripulito: "+bigram[0]);
			}
			String verb = (bigram[0].split("/"))[0];
			//logger.fine("verb: "+verb);
			//System.err.println("verb: "+verb);
			if(null != stopPronVerb)
				if(stopPronVerb.containsKey(verb)){
					//logger.info("StopPronVerb: "+verb);
					continue;
				}
			
			/* Controllo sui pronomi da staltare*/
			String noun = bigram[1].replaceAll("_+"," ");
			if(null != stopPronVerb)
				if(stopPronVerb.containsKey(noun)){
					//logger.info("StopPronVerb: "+noun);
					continue;
				}
			
			//System.err.println("noun: "+noun);
			noun = noun.replaceAll(Consts.PREP_ARTICOLATE,"$1 $4");
			//System.err.println("noun: "+noun);
			
			/*nounFreq e verbFreq sono x lo statistico che per RT()*/
			putInHash(nounFreq,noun );
			
			putInHash( verbFreqNoRel,verb );

			//putInHash( verbFreq,verb );
			putInHash( verbFreq,bigram[0] );
			Bigrams bigramma = new Bigrams();
			//bigramma.setLeft(verb);
			bigramma.setLeft(bigram[0]);
			bigramma.setRight(noun);
			/*x lo statistico (no RT) (11.05.2006: non mi torna questo commento, perche' OSRelHM e' usato solo x gli RT)*/
			putInHash(nounVerbFreq,bigramma);
			
			//costruisco sino2.inv x RT()
			//putInHashComplexValue(nounVerb,noun, verb);
			//logger.fine("putInHashComplexValue(nounVerb,"+noun+","+bigram[0]+")");

			putInHashComplexValue(nounVerb,noun, bigram[0]);
			
			//inserisco nell'hash(nome,[verbi])
			//MA NON E' gia' nounVerb?
			
			//inserisco nell'hash(verbo,[nomi]) x RT()
			//putInHashComplexValue(verbNoun,verb,noun);
			logger.fine("putInHashComplexValue(verbNoun,"+bigram[0]+","+noun+")");
			putInHashComplexValue(verbNoun,bigram[0],noun);

			numOfBigrams++;
		//	logger.warning("verbNoun.size(): " + verbNoun.size());
			//logger.warning("nounVerb.size(): " + nounVerb.size());
		}
		//logger.severe("PERIODO: "+((Vector) nounVerb.get("PERIODO")).size());
		return numOfBigrams;
	}


	/**
	 * Inserisce <i>element</i> nella HashMap <i>hash</i>
	 * Se l'elemento inserito non e' gia' presente, il valore e' posto
	 * a 1. Se al contrario l'elemento e' gia' presente allora viene
	 * incrementato di 1 il valore associato.
	 * @param hash HashMap nella quale si vuole inserire l'elemento
	 * @param element elemento da inserire
	 */
	private void putInHash( HashMap hash, Object element){
		Integer oldValue = (Integer) hash.get(element);
		if(oldValue != null){
			//controllo se è un verbo (cerco lo / )
			//se è un verbo => devo aggiornare la frequenza del verbo con SUBJ
			//che con OBJ.
			//logger.finest(element.toString()+" "+oldValue.intValue());
			hash.put(element, new Integer (oldValue.intValue()+1));
			oldValue = null;
		}else{
			// se l'unigramma NON era prensente nell'hash, lo inserisco
			// con il valore uguale a 1
			hash.put(element,  new Integer(1));
		}
	}

	/*
	
	if(element instanceof String){
		String str = (String) element;
		if (str.contains("/")){
			String verb = (str.split("/"))[0];
			logger.fine("verb: "+str +" "+verb+" "+oldValue.intValue());
			hash.put(verb+"/SUBJ", new Integer (oldValue.intValue()+1));
			hash.put(verb+"/OBJ",  new Integer (oldValue.intValue()+1));
		}
	}
*/
	/* la key e' il noun e il valore e' verb/rel */
	private void putInHashComplexValue( HashMap hash, Object key, String value){
		Vector oldValue = (Vector) hash.get(key);
		if(oldValue != null){
			//int pos = searchElement(oldValue,value);
			if(!oldValue.contains(value)){
				//System.err.println("pos: "+pos);
				//if(pos == -1){//se non c'e'
				oldValue.addElement(value);
			}else{// se c'e non faccio nulla
			}
			hash.put(key,oldValue);
		}else{
			//se non c'era il vecchio valore, inserisco il nuovo
			Vector newHash = new Vector();
			newHash.addElement(value);
			hash.put(key, newHash);
		}
	}


	/**
	 * @return the nounFreq
	 */
	public HashMap getNounFreq() {
		return nounFreq;
	}


	/**
	 * @param nounFreq the nounFreq to set
	 */
	public void setNounFreq(HashMap nounFreq) {
		this.nounFreq = nounFreq;
	}


	/**
	 * @return the nounVerb
	 */
	public HashMap getNounVerb() {
		return nounVerb;
	}


	/**
	 * @param nounVerb the nounVerb to set
	 */
	public void setNounVerb(HashMap nounVerb) {
		this.nounVerb = nounVerb;
	}


	/**
	 * @return the nounVerbFreq
	 */
	public HashMap getNounVerbFreq() {
		return nounVerbFreq;
	}


	/**
	 * @param nounVerbFreq the nounVerbFreq to set
	 */
	public void setNounVerbFreq(HashMap nounVerbFreq) {
		this.nounVerbFreq = nounVerbFreq;
	}


	/**
	 * @return the verbFreq
	 */
	public HashMap getVerbFreq() {
		return verbFreq;
	}


	/**
	 * @param verbFreq the verbFreq to set
	 */
	public void setVerbFreq(HashMap verbFreq) {
		this.verbFreq = verbFreq;
	}


	/**
	 * @return the verbFreqNoRel
	 */
	public HashMap getVerbFreqNoRel() {
		return verbFreqNoRel;
	}


	/**
	 * @param verbFreqNoRel the verbFreqNoRel to set
	 */
	public void setVerbFreqNoRel(HashMap verbFreqNoRel) {
		this.verbFreqNoRel = verbFreqNoRel;
	}


	/**
	 * @return the verbNoun
	 */
	public HashMap getVerbNoun() {
		return verbNoun;
	}


	/**
	 * @param verbNoun the verbNoun to set
	 */
	public void setVerbNoun(HashMap verbNoun) {
		this.verbNoun = verbNoun;
	}
	
/*	private int searchElement(Vector vect, String str){
		if(vect == null){
			return -1;
		}
		String elem = null;
	//	System.err.println("str: "+str);
		for (int i = 0; i < vect.size(); i++) {
			elem = (String) vect.elementAt(i);
		//	System.err.println("elem: "+elem);
			if(elem.equals(str)){
				return i;
			}
		}
		return -1;
	}*/

	/**
	 * @param args
	 */
	/*
	public static void main(String[] args) {
		T2KParserIdeal pi = new T2KParserIdeal();
		pi.init(args[0]);

		LinkedHashMap nounVerbFreq = new LinkedHashMap();
		LinkedHashMap verb = new LinkedHashMap();
		LinkedHashMap noun = new LinkedHashMap();
		LinkedHashMap nounVerb = new LinkedHashMap();
		LinkedHashMap verbNoun = new LinkedHashMap();
		LinkedHashMap stopVerb = new LinkedHashMap();
 		long start = System.currentTimeMillis();

		long finish = System.currentTimeMillis();
		System.out.println("time: "+(finish-start));
		start = System.currentTimeMillis();

		OSRelHM bighm = new OSRelHM();
		bighm.init(noun,verb,nounVerbFreq,nounVerb,verbNoun,pi,stopVerb);
		System.out.println("bigrammi: " +bighm.findNGrams());
		finish = System.currentTimeMillis();
		System.out.println("time: "+(finish-start));
		Iterator it = nounVerbFreq.entrySet().iterator();
		while (it.hasNext()){
			Map.Entry elem = (Map.Entry)it.next();
			System.out.println(((Bigrams)elem.getKey()).getLeft()
					+" "+ ((Bigrams)elem.getKey()).getRight()+
					" "+elem.getValue());
		}
		Iterator it2 = nounVerb.entrySet().iterator();
		while (it2.hasNext()){
			Map.Entry elem = (Map.Entry)it2.next();
			String nome = (String) elem.getKey();
			Vector element = (Vector) elem.getValue();
			System.out.print(nome+" ");
			for(int i = 0; i < element.size(); i++)
				System.out.print(element.elementAt(i)+" ");
			System.out.println();
		}
	}*/

}
