/**
 * $Id: UnigramsHM.java,v 1.9 2007/10/15 14:38:47 simone Exp $
 * 
 */
package ilc.t2k.hashmanager;

import ilc.t2k.Consts;
import ilc.t2k.parser.T2KParser;
import ilc.t2k.parser.T2KParserChunk;

import java.util.HashMap;

public class UnigramsHM {

	protected HashMap unigramHash = new HashMap(Consts.HASH_CAPACITY);
	protected T2KParser t2kParser;
	protected int numOfUnigrams;

	public UnigramsHM(/*HashMap unigrams*/) {
		//unigramHash = unigrams;
		// TODO Auto-generated constructor stub
	}

	public void init(/*HashMap _unigramHash,*/ T2KParser _t2kParser){
		//unigramHash = _unigramHash;
		t2kParser = _t2kParser;
		numOfUnigrams = 0;
	}
	
	/* Inserisce nell' HashMap i lemmi estratti dai chuggati (che sono restituiti
	 * dal parser dei chuggati) */
	public void findNGrams(){
		String[] unigrams;
		Integer oldValue;
		Integer newValue;
		/* prendo un chunk */
		while((unigrams = (String[]) t2kParser.getNext())!= null){
			/* rimuovo, se esiste, l'unigramma dall'hash */
			for (int i = 0; i < unigrams.length; i++) {
				numOfUnigrams++;
				oldValue = (Integer) unigramHash.get(unigrams[i]);
				//System.out.println(i+" oldValue "+oldValue);
				if(oldValue != null){
					// se l'unigramma era prensente nell'hash, lo reinserisco
					// con il valore incrementato di 1
					newValue = new Integer (oldValue.intValue()+1);
					unigramHash.put(unigrams[i], newValue);
				}else{
					// se l'unigramma NON era prensente nell'hash, lo inserisco
					// con il valore uguale a 1
					unigramHash.put(unigrams[i], new Integer(1));
				}
			}
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		T2KParserChunk pc = new T2KParserChunk();
		pc.init(args[0]);
		UnigramsHM uhm = new UnigramsHM();
		uhm.init(pc);
		long start = System.currentTimeMillis();
 
		uhm.findNGrams();

		/*Iterator it = uhm.unigramHash.entrySet().iterator();
		while (it.hasNext()){
			Map.Entry elem = (Map.Entry)it.next();
			System.out.println(elem.getKey()+" "+elem.getValue());
		}*/

		long finish = System.currentTimeMillis();
		System.out.println("time: "+(finish-start));
		if(args.length>=2){
			start = System.currentTimeMillis();
			pc.init(args[1]);
			uhm.init(pc);
			uhm.findNGrams();
			finish = System.currentTimeMillis();
			
			System.out.println("time2: "+(finish-start));
		}
	}

	/**
	 * @return the unigramHash
	 */
	public HashMap getUnigramHash() {
		return unigramHash;
	}

	/**
	 * @param unigramHash the unigramHash to set
	 */
	public void setUnigramHash(HashMap unigramHash) {
		this.unigramHash = unigramHash;
	}

}
