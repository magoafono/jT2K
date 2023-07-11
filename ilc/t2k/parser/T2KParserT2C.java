/**
 * $Id: T2KParserT2C.java,v 1.4 2007/09/20 13:28:31 simone Exp $
 */
package ilc.t2k.parser;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class T2KParserT2C extends T2KParser {

	private BufferedReader in = null;
	private FileReader fr = null;
	public T2KParserT2C (){

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
		// TODO Auto-generated method stub
		if(in != null){
			try {
				String row = in.readLine();
				if(row == null){
					fr.close();
					in.close();
					in = null;
					return null;
				}
				//ogni row e' della forma
				//T=n;F1=n1;F2=n2;...;
				//la splitto sul ";" e poi da gestore finisco
				// il parsing.
				return row.split(";");

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		return null;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
