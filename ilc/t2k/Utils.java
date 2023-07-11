/**
 * $Id: Utils.java,v 1.27 2008/06/20 12:37:09 simone Exp $
 * Utility varie per il funzionamento di jT2k
 */
package ilc.t2k;

import ilc.t2k.bean.FormFreq;
import ilc.t2k.bean.HashFreq;
import ilc.t2k.bean.TermMisura;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Logger;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.axis.Constants;
import org.apache.axis.MessageContext;

public class Utils {

	private static Logger logger = Logger.getLogger("jt2k");

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
				logger.severe("File too long: "+fileName);
				return(null);
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
			is = null;
			file=null;
			return new String(bytes,"ISO8859_1");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;

	}

	/**
	 * Elenca i file con estensione <i>extension</i> presenti nella directory <i>directoryName</i>
	 * @param directoryName nome della directory
	 * @param extension estensione dei file da cercare
	 * @return Array dei nomi dei file trovati ordinato alfabeticamente
	 */
	public static String [] listingFiles(String directoryName, final String extension){

		try {
			logger.fine("listingFiles: "+directoryName);
			File dir = new File(directoryName);

			FilenameFilter filter = new FilenameFilter() {
				public boolean accept(File dir, String name) {
					return ((!name.startsWith("."))&&(name.endsWith(extension)));
				}
			};

			String[] children = dir.list(filter);
			if (children == null) {
				logger.warning("directory "+directoryName+" doen't exist!\n");
				return null;
				// Either dir does not exist or is not a directory
			} 
			Arrays.sort(children);
			filter = null;
			dir = null;
			return children;
		} catch (Exception e) {
			System.err.println(e.getMessage());
			logger.severe(e.getMessage());
		}
		return null;
	}

	/**
	 * Elimina il file <i>fileName</i>
	 * @param filename nome del file da eliminare
	 */
	public static void deleteFile(String filename){
		boolean success = (new File(filename)).delete();
		if (!success) {
			logger.fine("File "+filename+" doesn't exists!");
		}
	}

	/**
	 * Elimina la directory <i>dirname</i> e tutti i file in essa contenuti
	 * @param dirname nome della directory da eliminare
	 */
	public static void deleteDirectory(String dirname){
		try {

			logger.fine("Deleting: " + dirname);
			File dir = new File(dirname);
			//System.err.println("Deleting: " + dirname);
			if (dir.isDirectory()) {
				String[] children = dir.list();
				for (int i=0; i<children.length; i++) {
					boolean success = (new File(dir, children[i])).delete();
					if (!success) {
						//return false;
					}
				}
			}
			dir.delete();
			dir = null;
		} catch (Exception e) {
			System.err.println("deleteDirectory: " + e.getMessage());
			logger.severe(e.getMessage());
		}
		logger.fine("Deleted: " + dirname);
	}

	/**
	 * Esegue il "pruning" degli elementi di un HashMap.
	 * Si noti che il valore associato ad una generica chiave deve 
	 * essere di tipo Integer.
	 * L'HashMap viene ordinato in maniera decrescente sul Value e vengono
	 * selezionati i primi <i>percentulale</i>% elementi
	 * 
	 * @param hash HashMap sulla quale effettuare il pruning
	 * @param percentuale percentuale degli elementi da prendere 
	 * @return vettore di elementi (sono Map.Entry).
	 */
	public static Vector prunes(HashMap hash, int percentuale){

		Object[] entrySet = hash.entrySet().toArray();
		//Ordino in maniera decrescente di valore tutto l'EntrySet
		int value = entrySet.length * percentuale / 100;
		logger.fine(entrySet.length+"; percentuale: " + percentuale);
		System.err.println("prunes() percentuale: " + value + ", entrySet.length="+entrySet.length +"), value=(" +value+")");
		Arrays.sort(entrySet,new Comparator(){
			public int compare(Object o1, Object o2) {
				//ordinamento decrescente
				return -((Integer)((Map.Entry)o1).getValue()).intValue()
				+((Integer)((Map.Entry)o2).getValue()).intValue();
			}
		});
		Vector pruned = new Vector();
		for (int i = 0; i < value; i++) {
			//logger.fine((String) ((Map.Entry)entrySet[i]).getKey()+" "+((Map.Entry)entrySet[i]).getValue());
			pruned.addElement(entrySet[i]);
		}
		entrySet = null;
		return pruned;
	}

	/**
	 * Effettua i pruning su un vettore di BrigramMisura
	 * @param vect vettore contenente dei BigramMisura
	 * @param percentuale percentuale degli elementi da prendere 
	 * @return vettore di elementi (sono String).
	 */
	public static Vector prunes(Vector vect, int percentuale){

		logger.fine(vect.capacity() + "; percentuale: " + percentuale);
		int value = vect.size() * percentuale / 100;
		//System.err.println(vect.size() +" " +value);
		Vector output = new Vector();
		for(int i=0; i < value; i++){
			output.addElement(vect.elementAt(i));
		}
		return output;
	}

	/**
	 * Effettua il "pruning" degli elementi dell'hash il cui "value"
	 * non supera la <i>soglia</i>. Gli elementi il cui valore ? uguale
	 * alla soglia sono restituiti. 
	 * @param hash HashMap da "tagliare"
	 * @param soglia valore minimo del value degli elementi da restituire
	 * @return HashMap di elementi il cui valore supera la soglia 
	 */
	public static HashFreq tagliaSoglia(HashMap hash, int soglia){

		HashMap 	pruned = new HashMap(Consts.HASH_CAPACITY);
		int numOfBigrams = 0;
		if((hash==null)||(hash.entrySet()==null)){
			logger.warning("Error on hashmap");
			return null;
		}
		Iterator scandisci = hash.entrySet().iterator();
		int freq = 0;
		while(scandisci.hasNext()) 
		{
			Map.Entry entry = (Map.Entry) scandisci.next();
			freq = ((Integer)entry.getValue()).intValue(); 
			if (freq >= soglia){
				pruned.put(entry.getKey(),entry.getValue());
				numOfBigrams += freq;
			}
		}
		scandisci = null;

		return new HashFreq(pruned,numOfBigrams);
	}

	public static LinkedHashMap taglia(HashMap hash, int soglia){

		if((hash==null)||(hash.entrySet()==null)){
			logger.warning("Error on hashmap");
			return null;
		}
		LinkedHashMap pruned = new LinkedHashMap(hash.size() * 1,25);
		Iterator scandisci = hash.entrySet().iterator();
		int freq = 0;
		while(scandisci.hasNext()) 
		{
			Map.Entry entry = (Map.Entry) scandisci.next();
			freq = ((Integer)entry.getValue()).intValue(); 
			if (freq >= soglia){
				//	logger.info(entry.getKey()+" "+entry.getValue());
				pruned.put(entry.getKey(),entry.getValue());
			}
		}
		scandisci = null;
		//logger.info("done!");
		return pruned;
	}
	/**
	 * Controlla se <i>dir</i> contiene file oppure no
	 * @param dir directory da controllare
	 * @return true se la directory non contiene file, false altrimenti
	 */
	public static  boolean isEmptyDir(String dir){
		//System.err.println("isEmptyDir: "+dir);
		logger.fine("isEmptyDir: "+dir);
		if(listingFiles(dir,"")==null){
			return true;	
		}else{
			return false;
		}
	}

	public static void sendEmail(String mailserver, String to, String mess, String id){

		if(mailserver==null){
			logger.severe("I cannot send any email: mailserver not specified");
			return;
		}
		if(to==null){
			logger.severe("I cannot send any email: receiver not specified");
			return;
		}
		if(mess==null){
			logger.severe("I cannot send any email: message not specified");
			return;
		}
		if(id==null){
			logger.severe("I cannot send any email: id not specified");
		}
		//		Get system properties
		Properties props = System.getProperties();

//		Setup mail server
		props.put("mail.smtp.host", mailserver);

//		Get session
		Session session = Session.getDefaultInstance(props, null);

//		Define message
		MimeMessage message = new MimeMessage(session);
		try {
			message.setFrom(new InternetAddress("jt2k"));
			message.addRecipient(Message.RecipientType.TO, 
					new InternetAddress(to));
			if(id!=null){
				message.setSubject("jt2k response for id request: "+id);
			}else{
				message.setSubject("jt2k response for unspecified id request");
			}
			message.setText(mess);

//			Send message
			Transport.send(message);
		} catch (AddressException e) {
			// TODO Auto-generated catch block
			logger.warning(e.getMessage());
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			logger.warning(e.getMessage());
		}

	}


	public static String reaccent(String word){
		if(word == null){
			return null;
		}
		word = word.replaceAll("A'","\u00c0");
		word = word.replaceAll("E'","\u00c8");
		word = word.replaceAll("I'","\u00cc");
		word = word.replaceAll("O'","\u00d2");
		word = word.replaceAll("U'","\u00d9");
		return word;
		//return replaceAllInsensitive(word); 
	}


	public static String reaccentWithEscapeQuote(String word){
		String ret = null;
		if ( null != word ) {
			ret  = reaccent(word);
			ret = ret.replaceAll("'", "''");
		}
		return ret;
	}	
	/*
	public static String replaceAllInsensitive(String word){
		word = Pattern.compile("a'",Pattern.CASE_INSENSITIVE).matcher(word).replaceAll("\u00c0");
		word = Pattern.compile("e'",Pattern.CASE_INSENSITIVE).matcher(word).replaceAll("\u00c8");
		word = Pattern.compile("i'",Pattern.CASE_INSENSITIVE).matcher(word).replaceAll("\u00cc");
		word = Pattern.compile("o'",Pattern.CASE_INSENSITIVE).matcher(word).replaceAll("\u00d2");
		word = Pattern.compile("u'",Pattern.CASE_INSENSITIVE).matcher(word).replaceAll("\u00d9");
		System.out.println(word);
		return word;
	}*/

	/**
	 * Prende da ogni vettore di <i>FormFreq</i> l'elemento con fequenza
	 * maggiore (tra quelli contenuti nel vettore) 
	 * @param hm 
	 * @return hashmap di elementi 
	 */
	public static LinkedHashMap getMostFrequent(HashMap hm){

		Set es = hm.entrySet();
		int size = es.size();
		LinkedHashMap resultHashMap = new LinkedHashMap (size * 1,3);
		Iterator it = es.iterator();
		Map.Entry elem = null;
		Vector forms = null;
		FormFreq fr = null;	
		while (it.hasNext()) {
			// Get element
			elem = (Map.Entry) it.next();
			forms =(Vector) elem.getValue();
			fr = getMostFrequentForm(forms);
			resultHashMap.put(elem.getKey(),fr);
			elem = null;
			forms = null;
		}
		return resultHashMap;
	}
	public static HashMap getVariants (HashMap hm) {

		Set es = hm.entrySet();
		int size = es.size();
		HashMap resultHashMap = new HashMap (size * 1,25);
		Iterator it = es.iterator();
		Map.Entry elem = null;
		Vector forms = null;
		HashMap variants = null;	
		while (it.hasNext()) {
			// Get element
			elem = (Map.Entry) it.next();
			forms = (Vector) elem.getValue();
			variants = getVariantsForm(forms);
			resultHashMap.put (elem.getKey(),variants);
		}
		return resultHashMap;

	}
	public static FormFreq getMostFrequentForm(Vector forms){
		int maxFreq = 0;
		int totFreq = 0;
		String form = null;
		int df = 0;
		for(int i=0; i < forms.size(); i++){
			FormFreq elem = (FormFreq) forms.elementAt(i);
			totFreq += elem.getFreq().intValue();
			if(elem.getFreq().intValue()>maxFreq){
				form = elem.getForm();
				maxFreq = elem.getFreq().intValue();
				df = elem.getDf();
			}
			//System.out.println("  forma: "+elem.getForm()+", freq: "+elem.getFreq().intValue());
		}
		FormFreq mostFreqForm = new FormFreq();
		mostFreqForm.setForm(form);
		mostFreqForm.setFreq(new Integer(totFreq));
		mostFreqForm.setDf(df);
		//System.out.println("forma: "+form+", freq: "+totFreq);
		return mostFreqForm;
	}

	public static HashMap getVariantsForm(Vector forms){

		/*
		 * Exception in thread "main" java.lang.OutOfMemoryError: Java heap space
	at java.util.HashMap.<init>(HashMap.java:187)
	at java.util.HashMap.<init>(HashMap.java:199)
	at ilc.t2k.Utils.getVariantsForm(Utils.java:451)
	at ilc.t2k.Utils.getVariants(Utils.java:423)
	at ilc.t2k.MainT2k.startT2k(MainT2k.java:514)
	at ilc.t2k.MainT2k.main(MainT2k.java:104)

		 */
		HashMap variants = new HashMap(1024/*Consts.HASH_CAPACITY*/); // le varianti saranno poche: togliere Consts.HASH_CAPACITY?
		Integer oldValue = null;

		for (int i=0; i < forms.size(); i++) {
			FormFreq ff = (FormFreq) forms.elementAt(i);
			if (null != ff) {
				String elem = ff.getForm();
				int freq = ff.getFreq().intValue();
				if ( null != (oldValue = (Integer) variants.get (elem))) {
					variants.put(elem, new Integer (oldValue.intValue() + freq));
				} else {
					variants.put(elem, freq);
				}
			}
			//System.out.println("  forma: "+elem.getForm()+", freq: "+elem.getFreq().intValue());
		}
		return variants;
	}

	public static HashMap readFileInHashMap(String fileName){
		HashMap hash = new HashMap(Consts.HASH_CAPACITY);
		BufferedReader in = null;
		FileReader fr = null;
		try {
			fr = new FileReader(fileName);
			in = new BufferedReader(fr);
			String str;
			while ((str = in.readLine()) != null) {
				logger.finer(str);
				hash.put(str,null);
			}
			in.close();
			in = null;
		} catch (FileNotFoundException e) {
			logger.warning("File "+fileName+" doesn't exist");
			//e.printStackTrace();
			return null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		return hash;
	}

	public static void printStringVector(Vector v){
		Iterator iter = v.iterator();
		while (iter.hasNext()){
			System.err.print((String)iter.next()+" ");
		}
		System.err.println();
	}
	public static void printTermMisuraVector(Vector v){
		Iterator iter = v.iterator();
		TermMisura tm;
		while (iter.hasNext()){
			tm = (TermMisura)iter.next();
			System.err.println(tm.getTermine()+" "+tm.getMisura());
		}
		System.err.println();
	}

	public static void printHashWithVectorValues(HashMap hash){

		Iterator it = hash.entrySet().iterator();
		while (it.hasNext()){
			Map.Entry elem = (Map.Entry)it.next();
			System.err.println("Valori di "+elem.getKey());
			Vector v = (Vector) elem.getValue();
			int size = v.size();
			for (int i = 0; i < size ; i++)
				System.err.println("    "+v.elementAt(i));

		}
	}


	/*public static Vector merge(Vector destVect, Vector srcVect){
		TermMisura src, dest;

		for (Iterator iteraS = srcVect.iterator(); iteraS.hasNext();) {
			src = (TermMisura) iteraS.next();
			for (Iterator iteraD = destVect.iterator(); iteraD.hasNext();) {
				dest = (TermMisura) iteraD.next();
				if(src.getTermine().equals(dest.getTermine())){
					dest.set
				}
			}
		}

	}*/
	/*
	public static Vector subtotals(Vector v){
		Iterator iter = v.iterator();
		TermMisura currentTM, nextTM;
		double subtotal = 0.0;
		//prendo il primo
		currentTM = (TermMisura) iter.next();
		subtotal = currentTM.getMisura();

		while (iter.hasNext()){
			nextTM = (TermMisura)iter.next();
			if(nextTM.getTermine().equals(currentTM.getTermine())){
				subtotal += nextTM.getMisura();
			}else{

			}
		}
			System.err.print(((TermMisura)iter.next()).getTermine()+" ");

		System.err.println();

	}*/

	public static void encrypt() throws Exception{

		KeyGenerator kgen = KeyGenerator.getInstance("Blowfish");
		//SecretKey skey = kgen.generateKey();
		//byte[] raw = skey.getEncoded();
		byte[] raw = {'a','a','3','4','6','4','c','2','c','7','4','8','3','1','e','5'};
		SecretKeySpec skeySpec = new SecretKeySpec(raw, "Blowfish");
		Cipher cipher = Cipher.getInstance("Blowfish");
		cipher.init(Cipher.ENCRYPT_MODE, skeySpec);

		String str = readFromFile("chiave.txt");
		byte[] encrypted = 
			cipher.doFinal(str.getBytes());
		if (null != encrypted) {
			write2File("encrypted.txt",encrypted);
		}

	}

	public static void decrypt(String filename) throws Exception{

		KeyGenerator kgen = KeyGenerator.getInstance("Blowfish");
		//SecretKey skey = kgen.generateKey();
		//byte[] raw = skey.getEncoded();
		byte[] raw = {'a','a','3','4','6','4','c','2','c','7','4','8','3','1','e','5'};
		SecretKeySpec skeySpec = new SecretKeySpec(raw, "Blowfish");
		Cipher cipher = Cipher.getInstance("Blowfish");
		cipher.init(Cipher.DECRYPT_MODE, skeySpec);

		byte[] str = readBinaryFile("encrypted.txt");
		System.err.println(str.length);

		byte[] decrypted =	cipher.doFinal(str);
		if (null != decrypted) {
			write2File("decrypted.txt",decrypted);
		}

	}



	public static void write2File(String filename, byte[] content) throws IOException{
		File outFile = new File(filename);
		PrintStream ps = new PrintStream(new FileOutputStream(outFile));
		ps.write(content);
		ps.close();
	}

	public static void touchFile(String filename){
		try {
			File outFile = new File(filename);
			PrintStream ps = new PrintStream(new FileOutputStream(outFile));
			ps.print("");
			ps.close();
			System.err.println("scritto file: "+ outFile);
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
	}

	public static byte[] readBinaryFile(String filename) throws IOException {

		// Open the file that is the first 
		// command line parameter
		FileInputStream fstream = new	FileInputStream(filename);

		// Convert our input stream to a
		// DataInputStream
		DataInputStream in = 
			new DataInputStream(fstream);

		// Continue to read lines while 
		// there are still some left to read
		byte[] content = new byte[in.available()];

		try{
			in.readFully(content);
		}catch (Exception e) {
			// TODO: handle exception
		}
		in.close();

		return  content;

	}

	public static String readFile (String filename) throws IOException{
		File file = new File(filename);
		FileInputStream fis = new FileInputStream(file);

		String criptLine = "";
		int c;
		while ((c = fis.read()) != -1) {
			criptLine = criptLine + (char)c;
		}
//		criptLine += "\n";
		fis.close();
		return criptLine;

	}

	public static void printSystemProperies(){

		// Get all system properties
		Properties props = System.getProperties();

		// Enumerate all system properties
		Enumeration enu = props.propertyNames();
		for (; enu.hasMoreElements(); ) {
			// Get property name
			String propName = (String)enu.nextElement();

			// Get property value
			String propValue = (String)props.get(propName);
			System.err.println(propName+" "+propValue); 
		}
	}

	public static String getClientIPAddress () {
		String hostname = null;
		try {
	        InetAddress addr = InetAddress.getLocalHost();
	    
	        // Get hostname
	         hostname = addr.getHostName();
	        //System.err.println(hostname);
	    } catch (UnknownHostException e) {
	    }
		return hostname;
	}
	
	public static void main(String[] args) throws Exception {
		//sendEmail(Config.mailserver,Config.email,"test", Config.idRequest);
		//sendEmail(args[0],args[1], "test","1");
		/*String[] files = listingFiles("/home/simone/",".doc");
		for (int i = 0; i < files.length; i++) {
			System.out.println(files[i]);
		}*/
		//reaccent("AUTORITA'");


		encrypt() ;
		decrypt("encrypted.txt") ;

	}

}
