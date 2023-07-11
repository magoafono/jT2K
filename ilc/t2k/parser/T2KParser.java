/**
 * $Id: T2KParser.java,v 1.4 2007/11/23 12:06:49 simone Exp $
 * 
 */
package ilc.t2k.parser;


/**
 * Classe astratta per l'implementazione di un parser
 * 
 * @author simone
 * @version $Revision: 1.4 $ $Date: 2007/11/23 12:06:49 $
 */

public abstract class T2KParser {

	/**
	 * Inizializza il parser per il file <i>fileName</i>
	 * @param fileName Nome del file da parsare
	 */
	public abstract void init(String fileName);
	
	/**
	 * @return il prossimo token, null se non ce ne sono altri
	 */
	public abstract Object[] getNext();
	
	

}
