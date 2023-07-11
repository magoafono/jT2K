/**
 * $Id: BigramsHM.java,v 1.16 2007/11/23 12:06:49 simone Exp $
 */
package ilc.t2k.hashmanager;

import ilc.t2k.Consts;
import ilc.t2k.bean.Bigrams;
import ilc.t2k.parser.T2KParser;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public  class BigramsHM {

	protected HashMap bigramHash = new HashMap(Consts.HASH_CAPACITY);
	protected HashMap sxUnigramHash = new HashMap(Consts.HASH_CAPACITY);
	protected HashMap dxUnigramHash = new HashMap(Consts.HASH_CAPACITY);
	protected HashMap unigramHash; //Hash degli unigrammi trovati dai chuggati
	protected T2KParser t2kParser;
	protected int numOfBigrams;

	private static Logger logger = Logger.getLogger("jt2k");

	public BigramsHM(HashMap _unigrams) {
		unigramHash = _unigrams;
	}
	/**
	 * Inizializza il manager dell'HashMap dei bigrammi
	 * @param _ngramHash hashmap contenente i bigrammi
	 * @param _t2kParser parser per l'output di ideal
	 * @param _sxUnigramHash hashmap per i costituenti SX di ogni bigramma
	 * @param _dxUnigramHash hashmap per i costituenti DX di ogni bigramma
	 * @param _unigramHash hashmap degli unigrammi individuati dal parsing del chunkato
	 */
	public  void init(/*HashMap _ngramHash, */T2KParser _t2kParser/*, 
			HashMap _sxUnigramHash, HashMap _dxUnigramHash, HashMap _unigramHash*/){
		//bigramHash = _ngramHash;
		//sxUnigramHash = _sxUnigramHash;
		//dxUnigramHash = _dxUnigramHash;
		//unigramHash = _unigramHash;
		t2kParser = _t2kParser;
		numOfBigrams = 0;
	}
	/**
	 * Ricerca gli N-Grammi.
	 * Opera la ricerca tramite un parser che restituisce elementi da elaborare.
	 * In particolare, inserisce in un hashmap i bigrammi, e in altre 2 hashmap
	 * distinte i termini che compomgono il bigramma differenziando tra componente sx
	 * e componente dx del bigramma.
	 */
	public  int findNGrams(){
		String[] bigram;
		
		/* prendo un chunk */
		while ((bigram = (String[]) t2kParser.getNext())!= null) {
			/* rimuovo, se esiste, l'unigramma dall'hash */
			/* il primo e' il bigramma */
			if(bigram == null){
				//System.err.println("errore nel parsing del file di ideal");
			}else if (bigram.length != 2){
				Consts.logger.log(Level.INFO,"Malformed row... skip");
				//System.err.println("errore nel parsing del file di ideal");
				continue;
			}
			/* I bigrammi possono essere di una delle seguenti forme:
			 * A B : con A lemma sinistro e B lemma dx (semplice eventualmente con una preposizione)
			 * A B#C : con A lemma sinistro, B lemma sx della parte dx e C lemma dx della parte dx
			 * Si noti che, in questo ultimo caso, il lemma risultato della congiunzione B#C deve essere
			 * considerato con un altro bigramma da inserire quindi nell'hash dei bigrammi (nella forma B#C)*/
			
			/* ogni bigramma ha tre elementi (stringa)
			 * bigram[0] e' il lemma sinistro
			 * bigram[1] e' il lemma sx della parte destra del bigramma
			 * bigram[2] e' il lemma dx della parte destra del bigramma (se esiste)*/			
			
			/* si disambigua */
			String primoLemma = disambigua(bigram[0]);
			if (primoLemma==null) {
				Consts.logger.fine("lemma "+bigram[0]+ " non disambiguabile!");
				//System.err.println("lemma "+bigram[0]+ " non disambiguabile!");
				continue;
				//System.exit(-1);
			}
			//System.err.print("bigram[0] :"+bigram[0]);
			Bigrams bigramma = new Bigrams();
			bigramma.setLeft(primoLemma);
			putInHash(sxUnigramHash,primoLemma );
			//tolgo l'underscore (se prensente)
			//bigram[1] = bigram[1].replace('_',' ');
			String[] dxGram = null;
			if(bigram[1].matches(".+#.+")){
				dxGram = bigram[1].split("#");
				if ( dxGram.length == 2 ) {
					String secondoLemma = disambigua(dxGram[0]);
					if (secondoLemma==null) {
						Consts.logger.fine("lemma "+dxGram[0]+ " non disambiguabile!");
						//System.err.println("lemma "+bigram[1]+ " non disambiguabile!");
						continue;
						//System.exit(-1);
					}
					String terzoLemma = disambigua(dxGram[1]);
					if(terzoLemma==null){
						Consts.logger.fine("lemma "+dxGram[1]+ " non disambiguabile!");
						//System.err.println("lemma "+bigram[2]+ " non disambiguabile!");
						continue;
						//System.exit(-1);
					}
					putInHash(sxUnigramHash,secondoLemma );
					putInHash(dxUnigramHash,terzoLemma );
					//putInHash(bigramHash, secondoLemma+Consts.HASH_SIGN+terzoLemma);
					//secondoLemma = secondoLemma.concat(Consts.HASH_SIGN+terzoLemma);
					Bigrams secondBigr = new Bigrams();
					secondBigr.setLeft(secondoLemma);
					secondBigr.setRight(terzoLemma);
					putInHash(bigramHash,secondBigr);
					numOfBigrams++;

					bigramma.setRight(secondoLemma+Consts.HASH_SIGN+terzoLemma);
					putInHash(dxUnigramHash,secondoLemma+Consts.HASH_SIGN+terzoLemma );
				}else{
					//errore! no si dovrebbe essere qui!
					continue;
				}
			}else{
				//non c'è il terzo lemma
				String secondoLemma = disambigua(bigram[1]);
				if (secondoLemma==null) {
					Consts.logger.fine("lemma "+bigram[1]+ " non disambiguabile!");
					//System.err.println("lemma "+bigram[1]+ " non disambiguabile!");
					continue;
					//System.exit(-1);
				}
				putInHash(dxUnigramHash,secondoLemma );
				bigramma.setRight(secondoLemma);
			}
			//System.err.print("bigram[1] :"+bigram[1]);
			
			//System.out.println(primoLemma+" "+secondoLemma);
			putInHash(bigramHash,bigramma );
			numOfBigrams++;
		}
		return numOfBigrams;
	}
	/**
	 * Data una stringa, effettua la disambiguazione
	 * @param tok stringa contenente possibili ambiguita' (del tipo A|B|C...)
	 * @return il lemma disambuiguato, null se nessuna delle opzioni e' presente tra gli unigrammi
	 */
	private String disambigua(String tok){

		try {
			if (tok.indexOf("|") == -1){
				/* la stringa non e' ambigua*/
				return tok;
			}
			String disambiguata = null;
			/* prendo i lemmi alternativi*/
			String[] opz = tok.split(Consts.PIPE_RX);
			//System.err.println("disambigua() " + tok);
			if ( !opz[0].contains("__")){
				Integer freq = null;
				int max = 0;
				/* per ogni lemma alternativo vado a vedere la sua frequenza negli unigrammi
				 * e scelgo, tra tutti, quello con frequenza maggiore */
				for (int i = 0; i < opz.length; i++) {
					freq = (Integer) unigramHash.get(opz[i]);
					if(freq == null){
						continue;
					}
					if(freq.intValue() > max){
						disambiguata = opz[i];
						max = freq.intValue();
					}
				}
				if(disambiguata != null ) {
					if (disambiguata.indexOf("|") > 0){
						logger.fine(disambiguata + " e' rimasta ambigua");
					}
				}else {
					logger.fine("disambiguata e' null e tok era " +  tok);

				}
			} else {
				//disambiguata = (opz[0].split(Consts.PIPE_RX)[0]);
				//System.err.println("disambigua(): "+opz[0]+ " da cui prendo: " + disambiguata);
				disambiguata = tok;
			}
			return disambiguata;
		}catch (Exception e) {
			System.err.println(e.getMessage());
		}
		return null;
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
			hash.put(element, new Integer (oldValue.intValue()+1));
			oldValue = null;
		}else{
			// se l'unigramma NON era prensente nell'hash, lo inserisco
			// con il valore uguale a 1
			hash.put(element,  new Integer(1));
		}
	}
	
	public void clear(){
		bigramHash.clear();
		sxUnigramHash.clear();
		dxUnigramHash.clear();
		//unigramHash.clear();
		bigramHash = null;
		sxUnigramHash = null;
		dxUnigramHash = null;
		//unigramHash = null;
	}
	/**
	 * @return the bigramHash
	 */
	public HashMap getBigramHash() {
		return bigramHash;
	}
	/**
	 * @param bigramHash the bigramHash to set
	 */
	public void setBigramHash(HashMap bigramHash) {
		this.bigramHash = bigramHash;
	}
	/**
	 * @return the dxUnigramHash
	 */
	public HashMap getDxUnigramHash() {
		return dxUnigramHash;
	}
	/**
	 * @param dxUnigramHash the dxUnigramHash to set
	 */
	public void setDxUnigramHash(HashMap dxUnigramHash) {
		this.dxUnigramHash = dxUnigramHash;
	}
	/**
	 * @return the sxUnigramHash
	 */
	public HashMap getSxUnigramHash() {
		return sxUnigramHash;
	}
	/**
	 * @param sxUnigramHash the sxUnigramHash to set
	 */
	public void setSxUnigramHash(HashMap sxUnigramHash) {
		this.sxUnigramHash = sxUnigramHash;
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
