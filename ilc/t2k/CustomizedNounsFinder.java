package ilc.t2k;

import java.util.StringTokenizer;
import java.util.Vector;
import java.util.logging.Logger;

public class CustomizedNounsFinder {

	private static Logger logger = Logger.getLogger("jt2k");
	private Vector matches;
	private Vector substitutions;
	
	public void load(String fileName){
		matches = new Vector();
		substitutions = new Vector();
		String file = Utils.readFromFile(fileName);
		StringTokenizer st = new StringTokenizer(file,"\n");
		while (st.hasMoreTokens()){
			String token = st.nextToken();
			//logger.finest(token.trim().replaceAll(" ", "( )+"));
			matches.add(token.trim().replaceAll(" ", "( )+"));
			String tok = (token.trim().replaceAll("\\s+", "_")); /*.concat("_");*/
			substitutions.add(tok.replaceAll("'", "'_").toUpperCase());
		}
	}
	
	public String findNouns(String text){
		for(int i=0; i<matches.size();i++){
			//logger.fine((String)substitutions.elementAt(i));
			text = text.replaceAll((String)matches.elementAt(i), (String)substitutions.elementAt(i));
		}
		return text;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		CustomizedNounsFinder cn = new CustomizedNounsFinder();
		logger.info("carico il glossario");
		cn.load("/home/simone/glossario_odontoiatria.txt");
		logger.info("glossario caricato");
		logger.info("carico file di testo");
		String text = Utils.readFromFile("/home/simone/glossario_odontoiatria.txt");
		String out = cn.findNouns(text);
		logger.info(out);


	}

}
