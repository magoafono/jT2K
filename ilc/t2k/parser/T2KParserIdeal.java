/**
 * $Id: T2KParserIdeal.java,v 1.9 2007/11/23 12:06:49 simone Exp $
 * 
 */
package ilc.t2k.parser;

import ilc.t2k.Consts;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class T2KParserIdeal extends T2KParser {

	//private StringTokenizer st;
	private BufferedReader in = null;
	private FileReader fr = null;
	
	public T2KParserIdeal(/*String idealFile*/) {
	/*	if(idealFile != null){
			st = new StringTokenizer(idealFile,"\n");
		}else{
			System.err.println("File di ideal null");
		}*/
	}

	/**
	 * Inizializza il parser per il file <i>fileName</i>.
	 * 
	 * @param fileName Nome del file contenente l'analisi funzionale,
	 * prodotta da IDEAL, da parsare
	 */
//	public void init(String idealFile) {
	public void init(String fileName) {
			/*if(idealFile != null){
			st = new StringTokenizer(idealFile,"\n");
		}else{
			System.err.println("File di ideal null");
		}*/
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
					/* Si legge una riga dal .ideal*/
					String line;
					line = in.readLine();
					/*
					 while (st.hasMoreTokens()) {
					 String line = st.nextToken();
					 */				
					if(line != null){
						//System.out.println("*"+line+"*");
						if(line.trim().equals("")){
							continue;
						}
						return line.split(Consts.DELIM);
					}else{
						/* raggiunta la fine del file */	
						try {
							in.close();
							fr.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						in = null;
						return null;
					}
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			
		return null;
	}
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		T2KParserIdeal pi = new T2KParserIdeal();
		pi.init(args[0]);
		long start = System.currentTimeMillis();
		while (pi.getNext()!=null);
		long finish = System.currentTimeMillis();
		
		System.out.println("time: "+(finish-start));

	}


}
