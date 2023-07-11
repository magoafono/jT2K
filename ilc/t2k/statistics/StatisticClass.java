/**
 * $Id: StatisticClass.java,v 1.17 2007/11/22 09:00:45 simone Exp $
 * 
 */
package ilc.t2k.statistics;


import ilc.t2k.Consts;
import ilc.t2k.bean.Bigrams;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.Math;
import java.util.Iterator;
import java.util.logging.Logger;

public class StatisticClass implements Statistic {
	
	private static Logger logger = Logger.getLogger("jt2k");
	
	public Vector elabora( HashMap hashSinistro, HashMap hashDestro, HashMap hashBigram, int totBigram){
		Vector vettoreOrdinato = new Vector();
		Bigrams bigramma = null; //new Bigrams();
		String parolaSinistra, parolaDestra;
		BigramMisura bigrammaMisura = new BigramMisura();
		int errore=0;
		double n11, n12, n21, n22, n1p, np1, n2p, np2, npp, m11, m12, m21, m22;
		double a,b,c,d,misura;
		double FreqBigram=0, FreqParolaSx=0, FreqParolaDx=0;
		Iterator scandisci = hashBigram.entrySet().iterator();
		
		while(scandisci.hasNext()) 
		{
			errore=0;
			Map.Entry entry = (Map.Entry) scandisci.next();
			bigramma = (Bigrams) entry.getKey();
			parolaSinistra = bigramma.getLeft();
			parolaDestra = bigramma.getRight();
			bigramma = null;
			//System.out.println(parolaSinistra + " " +parolaDestra);
			
			if (totBigram==0)
			{
				logger.severe("La frequenza totale dei bigrammi risulta uguale a 0!!");
				return null;
			}
			
			//Estraggo le frequenze dagli Hash
			FreqBigram=((Integer)entry.getValue()).doubleValue();
			
			if ( ((Integer)hashSinistro.get(parolaSinistra)) == null )
			{
				logger.warning("Parola Sinistra del Bigramma non trovata!! "+parolaSinistra );
				errore=1;
				//return null;
			}
			FreqParolaSx=((Integer)hashSinistro.get(parolaSinistra)).doubleValue();
			if ( ((Integer)hashDestro.get(parolaDestra)) == null )
			{
				logger.warning("Parola Destra del Bigramma non trovata!! "+parolaDestra );
				errore=1;
				//return null;
			}
			FreqParolaDx=((Integer)hashDestro.get(parolaDestra)).doubleValue();	
			
			//Controllo la correttezza "semantica" delle misure estratte
			if (FreqBigram > totBigram)
			{
				logger.warning("La frequenze del bigramma <"+parolaSinistra+" "+parolaDestra + "> risulta maggiore della frequenza totale dei bigrammi!!");
				errore=1;
				//return null;
			}
			if (FreqBigram == 0)
			{
				logger.warning("La frequenze del bigramma <"+parolaSinistra+" "+parolaDestra + "> risulta uguale a 0!!");
				errore=1;
				//return null;
			}
			if (FreqParolaSx > totBigram)
			{
				logger.warning("La frequenze della parola sinistra del bigramma <"+parolaSinistra+" "+parolaDestra + "> risulta maggiore della frequenza totale dei bigrammi!!");
				errore=1;
				//return null;
			}
			if (FreqParolaSx < FreqBigram)
			{
				logger.warning("La frequenze della parola sinistra del bigramma <"+parolaSinistra+" "+parolaDestra + "> risulta minore della frequenza del bigramma!!");
				errore=1;
				//return null;
			}
			if (FreqParolaDx > totBigram)
			{
				logger.warning("La frequenze della parola destra del bigramma <"+parolaSinistra+" "+parolaDestra + "> risulta maggiore della frequenza totale dei bigrammi!!");
				errore=1;
				//return null;
			}
			if (FreqParolaDx < FreqBigram)
			{
				logger.warning("La frequenze della parola destra del bigramma <"+parolaSinistra+" "+parolaDestra + "> risulta minore della frequenza del bigramma!!");
				errore=1;
				//return null;
			}
			
			//Calcolo per ogni Bigramma
			if (errore!=1)
			{
				n11=FreqBigram;
				n1p=FreqParolaSx;
				np1=FreqParolaDx;
				n12=n1p-n11;
				n21=np1-n11;
				np2=totBigram-np1;
				n2p=totBigram-n1p;
				n22=np2-n12;
				npp=totBigram;
				
				m11=(n1p*np1)/npp;
				m12=(n1p*np2)/npp;
				m21=(n2p*np1)/npp;
				m22=(n2p*np2)/npp;
				a=0;
				b=0;
				c=0;
				d=0;
				if (n11!=0) {
					a=Math.log(n11/m11);
				}
				if (n12!=0) {
					b=Math.log(n12/m12);
				}
				if (n21!=0) {
					c=Math.log(n21/m21);
				}
				if (n22!=0){
					d=Math.log(n22/m22);
				}
				
				
				misura = 2 * ((n11*a)+(n12*b)+(n21*c)+(n22*d)); 
				//Crea BigrammaMisura (Parola1,Parola2,Misura)
				
				bigrammaMisura = new BigramMisura();
				bigrammaMisura.setLeft(parolaSinistra);
				bigrammaMisura.setRight(parolaDestra);
				bigrammaMisura.setLog(misura);
				//Aggiunge l'elemento BigrammaMisura appena calcolato nel vettore;
				vettoreOrdinato.add(bigrammaMisura);
				logger.fine("LL: "+parolaSinistra+" "+parolaDestra+ " "+misura);
			}
		}
		//ordinare il vettore
		Collections.sort(vettoreOrdinato);
		//restituisco il vettore Ordinato!
		return vettoreOrdinato;
	}	
	public static String readFromFile(String fileName){
		File file = new File (fileName);
		InputStream is;
		try {
			is = new FileInputStream(file);
			// Get the size of the file
			long length = file.length();
			
			// You cannot create an array using a long type.
			// It needs to be an int type.
			// Before converting to an int type, check
			// to ensure that file is not larger than Integer.MAX_VALUE.
			if (length > Integer.MAX_VALUE) {
				// File is too large
			}
			
			// Create the byte array to hold the data
			byte[] bytes = new byte[(int)length];
			
			// Read in the bytes
			int offset = 0;
			int numRead = 0;
			while (offset < bytes.length
					&& (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
				offset += numRead;
			}
			
			// Ensure all the bytes have been read in
			if (offset < bytes.length) {
				throw new IOException("Could not completely read file "+file.getName());
			}
			// Close the input stream and return bytes
			is.close();
			return new String(bytes);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
		
	}
	public static void main(String[] args) {
		StatisticClass statistico = new StatisticClass();
		Vector risultato = new Vector();
		HashMap hashDestro = new HashMap(Consts.HASH_CAPACITY);
		HashMap hashSinistro = new HashMap(Consts.HASH_CAPACITY);
		HashMap hashBigrammi = new HashMap(Consts.HASH_CAPACITY);
		Bigrams bigrammaP = new Bigrams();
		Integer freqBigramma,freqParolaSx,freqParolaDx;
		String parolaSx, parolaDx;
		String txt = readFromFile(args[0]);
		StringTokenizer st = new StringTokenizer(txt,"\n");
		String row;
		String[] tokens;
		String[] freq;
		int freqTOT=Integer.parseInt(st.nextToken());
		while(st.hasMoreTokens()){
			row = st.nextToken();
			/* A<>B<>x y z */
			tokens = row.split("<>");
			parolaSx=tokens[0];
			parolaDx=tokens[1];
			freq = tokens[2].split(" ");
			//freqInteger=Integer.parseInt(freq[0]);
			freqBigramma=Integer.decode(freq[0]);
			freqParolaSx=Integer.decode(freq[1]);
			freqParolaDx=Integer.decode(freq[2]);
			//inserisco i dati negli HashMap per eseguire il TEST!!
			if (!hashSinistro.containsKey(parolaSx)) {
				hashSinistro.put(parolaSx,freqParolaSx);
			}
			if (!hashDestro.containsKey(parolaDx)) {
				hashDestro.put(parolaDx,freqParolaDx);
			}
			bigrammaP = new Bigrams();
			bigrammaP.setLeft(parolaSx);
			bigrammaP.setRight(parolaDx);
			
			hashBigrammi.put(bigrammaP,freqBigramma);
		}
		//System.out.println("finito di caricare!!");
		
		risultato=statistico.elabora(hashSinistro,hashDestro,hashBigrammi,freqTOT);
		BigramMisura bigrammaM;

		for (Iterator iter = risultato.iterator(); iter.hasNext();) {
			bigrammaM = (BigramMisura) iter.next();
			System.err.println("LogLike: " + bigrammaM.getLeft()+" "+bigrammaM.getRight()+" "+bigrammaM.logLikelihood);
		}
	}
}