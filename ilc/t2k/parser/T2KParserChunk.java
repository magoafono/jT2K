/**
 * $Id: T2KParserChunk.java,v 1.15 2007/11/23 12:06:49 simone Exp $
 * 
 */
package ilc.t2k.parser;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Vector;

import ilc.t2k.Config;
import ilc.t2k.Consts;
 
/**
 * Implementa il parser per un file chunkato.
 * Estrae da un file chunkato i termini che sono classificati
 * come S o SP (nome comune e nome proprio) dal chunker di ANITA
 * 
 * @author simone
 * @version $Revision: 1.15 $ $Date: 2007/11/23 12:06:49 $
 */

public class T2KParserChunk extends T2KParser {


	private BufferedReader in = null;
	private FileReader fr = null;
	/**
	 * Inizializza il parser per il file <i>fileName</i>.
	 * 
	 * @param fileName Nome del file chunkato da parsare
	 */
	public  T2KParserChunk(/*String fileName*/) {
		// TODO Auto-generated method stub
		/*try {
			in = new BufferedReader(new FileReader(fileName));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
	}
	
	public void init(String fileName) {
		
		try {
			fr = new FileReader(fileName);
			in = new BufferedReader(fr);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	

	public Object[] getNext() {
		try {
			if(in != null){
				while(true){
					/* Si legge una riga dal chug.ide*/
					String line = in.readLine();
					/* le righe possono iniziare con { o con [ solamente.
					 * Ma a noi interessano quelle che sono della forma:
					 * [ [ CC: di_C], [ [ CC: P_C], [ [ CC: N_C]*/
					if(line != null){
						//System.out.println(line);
						//if (line.matches("(.)+CC: (di_C|P_C|N_C)(.)+")){
						if(line.indexOf("POTGOV")==-1){
							continue;
						}
						/* selezione le linee che hanno come CC quello specificato in Consts.CHUNK_SELECT_RX */
						/* NON SI SELEZIONANO GLI AGGETTIVI UTILI X LA DISAMBIGUAZIONE DEGLI AGGETTIVI
						 * nelle relazioni N_P_N_A trovate da ideal!!! */
						
						if (line.matches(Consts.CHUNK_SELECT_RX)){
							//System.out.println(line);
							return getLemma(line);
						}
					}else{
						/* raggiunta la fine del file */
						//System.out.println("Chiudo il file"/*getLemma(line)[0]*/);
						fr.close();
						in.close();
						in = null;
						return null;
					}
				}
			}else
				return null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	/* prende in input un linea del chug.ide*/
	private String[] getLemma(String chunkLine){
		
		//System.out.println(chunkLine);
		String[] splittedLine = chunkLine.split("POTGOV:");
		/* In slittedLine[1] ci sono le varie letture: 
		 * e' quello che ci interessa.
		 * Si devono ora cercare i lemmi che hanno lettura S o SP
		 * verificando che, se ci sono piu' lemmi con S/SP se ne deve prendere uno
		 * nel caso di lemmi identici, o tutti nel caso siano diversi
		 * */
		/*Es. MARSIGLIA#SP@MS MARSIGLIA#SP@NN]] */
		//System.err.println(chunkLine); 
		String[] pots = splittedLine[1].split(" ");
		String[] aPot;
		//System.out.println(pots.length);
		/*if(pots.length<3){

			if (pots[0].matches(".+#S[P]?@.+")){
				aPot =  pots[0].split("#");
				String[] p = {aPot[0].toString()} ; 
				return p;
			}
		}*/
		Vector lemma = new Vector(); 
		String chunkRE = null;
		for (int i = 0; i < pots.length; i++) {
			//System.out.println("pots["+i+"]: "+pots[i]);
			if (Config.adjPotgov) {
				chunkRE = Consts.ASSP_CHUNK;
			} else {
				chunkRE = Consts.SSP_CHUNK;
			}
			if (pots[i].matches(chunkRE)){
				aPot = pots[i].split("#");
				if( ! lemma.contains(aPot[0])){
					//System.err.println(i+" aPot[0]: "+aPot[0]);
					/*if(Config.customizedNouns){
						//System.err.println(i+" aPot[0]: "+(aPot[0].replaceAll("_+"," ")).trim());
						lemma.add((aPot[0].replaceAll("_+"," ")).trim());
					}else {*/
						lemma.add(aPot[0]/*.toLowerCase()*/);
					//}
				}
			}
		}
		return (String[]) lemma.toArray(new String[0]);
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		T2KParserChunk pc = new T2KParserChunk();
		pc.init(args[0]);
		long start = System.currentTimeMillis();
		while (pc.getNext()!=null);
		long finish = System.currentTimeMillis();
		
		System.out.println("time: "+(finish-start));
	}

}

