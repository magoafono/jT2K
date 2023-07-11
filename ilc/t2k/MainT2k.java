/**
 * $Id: MainT2k.java,v 1.51 2008/06/20 12:37:09 simone Exp $
 */
package ilc.t2k;

import ilc.nlp.wrapper.Ideal;
import ilc.nlp.wrapper.NLPTools;
import ilc.nlp.wrapper.T2CMapper;
import ilc.t2k.bean.FormFreq;
import ilc.t2k.bean.HashFreq;
import ilc.t2k.database.ConnectionManager;
import ilc.t2k.database.DBManager;
import ilc.t2k.database.WSDBManager;
import ilc.t2k.hashmanager.BigramsHM;
import ilc.t2k.hashmanager.GlossarioHM;
import ilc.t2k.hashmanager.OSRelHM;
import ilc.t2k.hashmanager.UnigramsHM;
import ilc.t2k.parser.T2KParserChunk;
import ilc.t2k.parser.T2KParserIdeal;
import ilc.t2k.parser.T2KParserT2C;
import ilc.t2k.parser.T2KParserTermFreq;
import ilc.t2k.statistics.BigramMisura;
import ilc.t2k.statistics.CalcoloRT;
import ilc.t2k.statistics.StatisticClass;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.apache.axis.Version;
import org.apache.commons.lang.time.StopWatch;


public class MainT2k {

	// variabile disabilitare il calcolo dei time.
	public static boolean timeDebug = false;
	private static Logger logger = Logger.getLogger("jt2k");
	private  Vector stopWordPattern = null;
	private HashMap nomiPropri = null;
	private CustomizedNounsFinder cnf;
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		/*System.setProperty("java.util.logging.config.file",System.getProperty("user.dir")+
				System.getProperty("file.separator")+"log.properties");
		try {
			LogManager.getLogManager().readConfiguration();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch bloc
			e.printStackTrace();
		}
		//System.err.println("caricato?: "+System.getProperty("user.dir")+
		//		System.getProperty("file.separator")+"log.properties");
		logger.info("caricato?: "+System.getProperty("java.util.logging.config.file"));
		logger.log(Level.SEVERE,"caricato?: "+System.getProperty("java.util.logging.config.file"));*/
		//System.err.println("Client IP Address is: " + Utils.getClientIPAddress());
		if (!"labdoc-laptop".equalsIgnoreCase(Utils.getClientIPAddress())) {
			System.err.println("Errore limitazione di licenza: macchina non abilitata ("+Utils.getClientIPAddress()+")");
			//System.exit(-1);
		}
		Properties properties = System.getProperties();
	    properties.list(System.out);
		MainT2k mainT2K = new MainT2k();
		mainT2K.preinit();
		//System.err.println("args.length "+args.length);
		if(args.length==0){
			logger.warning("No config file provided, using default.xml ...");
			mainT2K.readConfiguration(Consts.defaultConfig);
			mainT2K.initT2k();
		}else{
			if ("-e".equals( args[0]) ) {
				System.err.println(args[0]+":"+args[1]+":"+args[2]);
				//args[1] e' il tipo di export (html,csv,xml)
				//argv[2] e' il nome del file di configurazione dal quale prendere i dati di connessione al DB
				mainT2K.readConfiguration(args[2]);
				exportTable(args[1]);
				System.exit(0);
			} 
			final String  path = args[0];
			//Config.docRepository = args[0];
			logger.config("Config file: "+path);
			mainT2K.readConfiguration(path);
			mainT2K.initT2k();
		}	

		mainT2K.startT2k();
	}


	/**
	 *  non + usato...
	 */
	public String preinit(){
		String ret = "ok";
		System.setProperty("java.util.logging.config.file",System.getProperty("user.dir")+
				System.getProperty("file.separator")+"log.properties");
		try {
			LogManager.getLogManager().readConfiguration();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			ret = e.getMessage();
			logger.warning(e.getMessage());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			ret = e.getMessage();
			logger.warning(e.getMessage());
		} catch (Exception e) {
			ret = e.getMessage();
			logger.severe(e.getMessage());
		}
		//System.err.println("caricato?: "+System.getProperty("user.dir")+
		//		System.getProperty("file.separator")+"log.properties");
		logger.info("file log in uso: "+System.getProperty("java.util.logging.config.file"));
		return ret;
	}


	public String readConfiguration (String configurationFileName) {
		String ret = "ok";

		//Elimino le vecchie informazioni presenti nella configurazione del sistema.
		Config.cleanConfiguration();

		logger.info("Parsing config file: " + configurationFileName);
		ParserXmlConfig parser = new ParserXmlConfig();

		ret = parser.parseXMLFile(configurationFileName);
		System.err.println("0: "+ret);
		return ret;
	}

	/**
	 * Inizializazione del sistema
	 * @param configurationFileName nome del file di configurazione, se null => viene letto il default.xml
	 */
	public  String  initT2k(){

		String ret = "ok";

		StopWatch sw = new StopWatch();
		sw.start();
		logger.config(System.getProperty("java.library.path"));
		logger.info("Initialize t2k system...");
		
		//Utils.printSystemProperies();

		if ( "ok".equals(ret)) {
			logger.config("Configuration: " + Config.printConfiguration());
			/*
			 * preparing library_path pointing to the c/c++ libraries
			 */
			String sep = System.getProperty("file.separator");

			//di default cerco le librerie nella directory in cui e' lanciato jt2k
			String library_path = System.getProperty("user.dir") + sep;

			//Se jt2k e' lanciato nel tomcat occorre recuperare la directory in cui sono presenti le librerie
			//la metodologia e' molto grezza: se e' definita la variabile catalina.home => siamo nel tomcat
			//Nel caso in cui si sia nel tomcat la directory e' prefissata in catalina.home/webapps/axis/WEB-INF/lib/jt2k
			//se le librerie non stanno in questo path non finziona nulla!!!
			if ( System.getProperty("catalina.home") != null){

				library_path = System.getProperty("catalina.home") + sep + "webapps" +sep + "axis" + sep + "WEB-INF" + sep + "lib" + sep + "jt2k" + sep;

				//Debug delle properties utile per avere informazioni sulla installazione del Tomcat/Apache/Axis
				//Properties prop = new Properties(System.getProperties()) ;
				//prop.list(System.err);
				logger.config("Axis version: " + Version.getVersion() + " path is: " + System.getProperty("java.library.path"));
			}
			logger.config("Loading ANITA tool ... Config.dataRepository=(" + Config.dataRepository + ")");

			System.setProperty("java.library.path",System.getProperty("java.library.path") + ":");

			logger.config("library_path: " + library_path);
			logger.config("Prima di NLPTools.init() library_path: " + library_path);

			if ( NLPTools.init(library_path) ){
				logger.fine("dopo NLPTools.init() " + Config.dataRepository ) ;
			} else {
				logger.severe("NLPTools library not loaded");
				return "ERROR: NLPTools library not loaded";
				//System.exit(-1);
			}

			NLPTools.startTools(Config.dataRepository);

			logger.fine("Dopo NLPTools.startTools");

			//lo start di IDEAL si fa qui o nell'RT?
			//Ideal.startIdealParser(Config.dataRepository);
			logger.fine("Loading stop words");
			stopWordPattern = loadStopWord();
			//caricamento nomi propri
			logger.fine("Loading nomi propri");
			nomiPropri = loadNomiPropri();

			if(Config.customizedNouns){
				logger.config("Loading customized nouns: "+Config.dataRepository+"custom-nouns.txt");
				cnf = new CustomizedNounsFinder();
				cnf.load(Config.dataRepository+"custom-nouns.txt");
			}
			sw.stop();
			logger.fine("Loadind Ideal library:  library_path=" + library_path);
			if ( Ideal.init(library_path) ) {
				logger.fine("dopo Ideal.init()");
			} else {
				logger.severe("Ideal library not loaded");
				return "ERROR: Ideal library not loaded";
				//System.exit(-1);
			}
		}

		logger.fine("Done: "	+ sw.getTime());
		sw = null;
		return ret;

	}

	public  void initT2kXmlString(String xmlString){
	}



	public String startT2k(){

		StopWatch sw1 = null;
		StopWatch sw2 = null;
		StopWatch sw3 = null;
		if ( timeDebug ) {
			sw1 = new StopWatch();
			sw2 = new StopWatch();
			sw3 = new StopWatch();

			sw1.start();
		} 
		System.err.println("Starting t2k...");
		logger.info("Starting t2k...");
		logger.info("Testing SQL connection ...");
		Connection  conn = ConnectionManager.getConnection();
		if(conn==null){
			logger.severe("Server SQL is NOT reachable ...");
			return "Server SQL is NOT reachable ...";
		}
		logger.info("Server SQL is reachable ...");

		//DBManager.retrieveFromGlossario(conn);
		//DBManager.retriveFromGlossarioFrequenza(conn);
		//DBManager.retrieveNoOfDoc(conn);
		//System.exit(0);
		if(Config.docRepository==null){
			logger.severe("No repository provided! exit...");
			return "No repository provided! exit..."; //System.exit(-1);
		}
		logger.config("Repository: "+Config.docRepository);

		//Lancio ANITA su tutti i file con estensione .txt che si trovano in
		//nel repository dei documenti.


		if(!Utils.isEmptyDir(Config.tempOutDir)){
			//System.err.println("2");
			logger.info("There are files in "+Config.tempOutDir+". I delete them");
			Utils.deleteDirectory(Config.tempOutDir);
		}


		logger.fine("Creating temp directory: "+Config.tempOutDir);
		createDirectory(Config.tempOutDir);

		Utils.touchFile(Config.tempOutDir + "inizio.txt");

		logger.fine("listingFiles: "+Config.docRepository);
		String[] files = Utils.listingFiles(Config.docRepository,".txt");

		if(files == null){
			logger.severe("Problem reading files from "+Config.docRepository+". Stop");
			return "Problem reading files from "+Config.docRepository+". Stop";
		}
		if(files.length==0){
			logger.severe("There are no files in "+Config.docRepository+". Stop");
			return "There are no files in "+Config.docRepository+". Stop";
		}
		if ( timeDebug ) {
			sw1.stop();
		}

		String text = null;
		logger.info ("Run ANITA on: "+Config.docRepository);
		for (int i = 0; i < files.length; i++) {
			System.err.println("Run ANITA on: "+Config.docRepository+files[i]);
			logger.fine ("Run ANITA on: "+Config.docRepository+files[i]);
			text = Utils.readFromFile(Config.docRepository+files[i]);

			//qui va messo il modulo per i Customized Nouns
			if(Config.customizedNouns){
				text = cnf.findNouns(text);
			}
			NLPTools.runTools(text,Config.tempOutDir+files[i]);
			//faccio pulizia dei temporanei che non mi serviranno piu'
			//in modo da occupare meno spazio possibile su disco.
			deleteTemporaryFiles(Config.tempOutDir+files[i]);
			text = null;
		}

		if ( timeDebug ) {
			sw2.start();
		}
		files = null;

		if(Config.enableUpdate){
			logger.info("Updating ...");
			updateDocuments(conn);
		}else{
			//Per ogni file .chug.ide va richiamata la UnigramHM
			//per andare a prendere i sostantivi e i nomi propri (S|SP)
			T2KParserChunk pc = new T2KParserChunk();
			UnigramsHM uhm = new UnigramsHM(/*unigrams*/);
			/*Ciclo sui file.chug.ide*/
			String[] chugIdeFiles = Utils.listingFiles(Config.tempOutDir,"chug.ide");
			logger.info("Run ParseChunk on: "+Config.tempOutDir);
			for (int i = 0; i < chugIdeFiles.length; i++) {
				logger.fine("Run ParseChunk on: "+Config.tempOutDir+chugIdeFiles[i]);
				//x ogni file chug.ide
				//inizializzo il parser del chug.ide
				pc.init(Config.tempOutDir+chugIdeFiles[i]);
				//inizializzo l'HasmManager
				uhm.init(/*unigrams,*/pc);
				//lancio la ricerca degli unigrammi
				uhm.findNGrams();
				//Utils.deleteFile(Config.docRepository+chugIdeFiles[i]);
			}
			pc = null;
			//uhm = null;

			T2KParserIdeal pi = new T2KParserIdeal();
			HashMap unigrams = uhm.getUnigramHash();
			BigramsHM bighm = new BigramsHM(unigrams);

			/*ciclo sui file.ideal*/
			String[] idealFiles = Utils.listingFiles(Config.tempOutDir,".ideal");
			int numbOfBigrams = 0;
			logger.info("Run ParseIdeal on: "+Config.tempOutDir);
			for (int i = 0; i < idealFiles.length; i++) {
				//	System.err.println(Config.tempOutDir+idealFiles[i]);
				logger.fine("Run ParseIdeal on: "+Config.tempOutDir+idealFiles[i]);
				pi.init(Config.tempOutDir+idealFiles[i]);
				bighm.init(/*bigHashMap,*/pi/*,sxHashMap,dxHashMap,unigrams*/);
				numbOfBigrams += bighm.findNGrams();
				/*Utils.deleteFile(Config.docRepository+idealFiles[i]);*/
			}
			pi = null;
			idealFiles = null;
			//pi = null;
			//bighm = null;
			//printHashMap(bigHashMap);
			//Invocazione del Modulo statistico.
			logger.info("Preparing StatisticClass");
			StatisticClass statistico = new StatisticClass();
			HashFreq hf = Utils.tagliaSoglia(bighm.getBigramHash(),Config.soglia_bigrammi);
			if(hf == null) {
				logger.severe("Error on bigram hash!");
				return "Error on bigram hash!";
			}
			Vector bigrams = statistico.elabora(bighm.getSxUnigramHash(),
					bighm.getDxUnigramHash(),hf.getHash(),hf.getFreq());
			//bighm.clear();
			//bighm = null;
			hf.clear();
			hf = null;
			/*sxHashMap.clear();
			sxHashMap = null;
			dxHashMap.clear();
			dxHashMap = null;
			bigHashMap.clear();
			bigHashMap = null;*/
			statistico = null;
			//			System.gc();
			//Pruning dei termini semplici e complessi con soglie
			//printHashMap(unigrams);
			//			System.err.println("unigram.size() "+unigrams.size());
			logger.info("Prunes the unigrams hashmap");

			unigrams = (HashMap) Utils.taglia(unigrams,Config.soglia_glossario);
			//			System.err.println("unigram.size() "+unigrams.size());
			//logger.info("tagliata unigrams");
			Vector terminiSemplici = Utils.prunes(unigrams,Config.sogliaTerminiSemplici);
			Vector terminiComplessi = Utils.prunes(bigrams,Config.sogliaTerminiComplessi);

			//bighm.clear();
			bighm = null;
			//bigrams.clear();
			bigrams = null;
			//System.err.println("terminiSemplici" + terminiSemplici.size());
			//scrittura del file allterms.out (nella directory temporanea)
			//che sara' dato in input al t2cmapper (ex-Daniela+)
			logger.info("Preparing t2cmapper structures");

			String term = null;
			BufferedWriter allterms = null;
			try {
				allterms = new BufferedWriter(new FileWriter (Config.tempOutDir + "allterms.out"));
				for(int i = 0; i < terminiSemplici.size(); i++){
					term = (String)((Map.Entry)terminiSemplici.elementAt(i)).getKey();
					allterms.write(term);
					allterms.write("\n");
				}
				logger.fine("dopo scritto allterms.out");
				terminiSemplici.clear();
				terminiSemplici = null;
				for(int i = 0; i < terminiComplessi.size(); i++){
					term = ((BigramMisura)terminiComplessi.elementAt(i)).getLeft()+" "+
					((BigramMisura)terminiComplessi.elementAt(i)).getRight()+"\n";
					term = term.replaceAll("#"," ");
					allterms.write(term);
				}
				terminiComplessi.clear();
				terminiComplessi = null;
				term = null;
				allterms.flush();
				allterms.close();
				allterms = null;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				logger.severe(e.getMessage());
				e.printStackTrace();
			}catch (Exception e) {
				logger.severe(e.getMessage());
			}
			if ( timeDebug ) {
				sw2.stop();
			}
			String sep = System.getProperty("file.separator");
			//di default cerco le librerie nella directory in cui e' lanciato jt2k
			String library_path = System.getProperty("user.dir") + sep;
			if ( System.getProperty("catalina.home") != null){
				//				library_path = System.getProperty("catalina.home") + sep + "webapps" +sep + "axis" + sep + "WEB-INF" + sep + "lib" + sep;
				library_path = System.getProperty("catalina.home") + sep + "webapps" +sep + "axis" + sep + "WEB-INF" + sep + "lib" + sep + "jt2k" + sep;
			}
			logger.info("Trying to load t2cmapper : library_path = " + library_path);

			//Inizializzazione del t2cmapper (ex-Daniela+)
			logger.info("Inizializzazione del t2cmapper: "+Config.tempOutDir + "allterms.out");
			T2CMapper.init(library_path);
			logger.info("Trying to start t2cmapper : Config.tempOutDir = " + Config.tempOutDir);

			T2CMapper.startTools(Config.tempOutDir + "allterms.out");
			logger.fine("partito T2CMapper.");

			//Run del t2cmapper sui chuggati
			String outputFile = null;

			logger.info("Running t2cmapper on files");
			for (int i = 0; i < chugIdeFiles.length; i++) {
				//System.err.println(Config.tempOutDir+chugIdeFiles[i]);
				//System.err.println("x t2cmapper: "+Config.tempOutDir+chugIdeFiles[i]);
				outputFile = chugIdeFiles[i].replaceFirst("\\.chug\\.ide","");
				logger.fine("Run Term2ChunkMapper on: "+Config.tempOutDir+chugIdeFiles[i]);
				//System.err.println("x t2cmapper: "+outputFile);
				try{
					T2CMapper.runTools(Config.tempOutDir+chugIdeFiles[i],
							Config.tempOutDir+outputFile);
				}catch(Throwable e) {e.printStackTrace();};
			}		
			outputFile = null;
			if ( timeDebug ) {
				sw3.start();
			}

			//Parsing dell'output (su files) del t2cmapper:
			//devo creare i glossari (standard, BT e NT)
			T2KParserT2C t2c = new T2KParserT2C();
			GlossarioHM glossarioHM = new GlossarioHM();

			if(conn==null){
				logger.severe("Cannot establish a connection to DB");
				return "Cannot establish a connection to DB"; 
			}

			logger.info("Run ParseT2C on: "+Config.tempOutDir);

			String[] termFormeFiles = Utils.listingFiles(Config.tempOutDir,".termforme");
			int numOfDocuments = termFormeFiles.length;
			for (int i = 0; i < numOfDocuments; i++) {
				System.err.println("Run ParseT2C on: "+Config.tempOutDir+termFormeFiles[i]);
				logger.fine("Run ParseT2C on: "+Config.tempOutDir+termFormeFiles[i]);
				t2c.init(Config.tempOutDir+termFormeFiles[i]);
				glossarioHM.init(/*terms,termsforms,formaIds,termineIdFreq,*/t2c/*,conn*/);
				glossarioHM.creaGlossario();
			}
			termFormeFiles = null;
			//printHashMapFormFreq(termsforms);
			glossarioHM.setStopWordPattern(stopWordPattern);

			HashMap newtermsforms = Utils.getMostFrequent(glossarioHM.getGlossarioLemmaForme());
			HashMap variantiHM = null;
			if (Config.enableVariants) {
				variantiHM = Utils.getVariants(glossarioHM.getGlossarioLemmaForme());
			}
			glossarioHM.setGlossarioTerminiForma(newtermsforms);
			glossarioHM.setNomiPropri(nomiPropri);
			

			//NEW: prima di mettere i termini nel glossario si applica la funzione di ordinamento.
			//glossarioHM.orderGlossarioTerms(termFormeFiles.length);
			
			glossarioHM.putGlossarioIntoDB(numOfDocuments);
			if (Config.enableVariants) {
				glossarioHM.putVariantsDB(variantiHM);
			}
			glossarioHM.creaGlossarioBTNT();

			t2c = null;
			/*terms.clear();
			terms = null;*/
			//termsforms.clear();
			//termsforms = null;
			//	System.gc();

			//printHashMap(terms);
			//Parsing dei file termini-ferquenza (in uscita da t2cmapper) 
			
			if(Config.enableDocIndex) { 
				String[] termFreqFiles = Utils.listingFiles(Config.tempOutDir,".termfreq");
				//T2KParserTermFreq ptf = new T2KParserTermFreq(termsforms,termFreqFiles.length,conn);
				T2KParserTermFreq ptf = new T2KParserTermFreq(newtermsforms,termFreqFiles.length,conn);
				//printHashMap(formaIds);
				HashMap formaIds = glossarioHM.getFormaIdentificativo();

				for (int i = 0; i < termFreqFiles.length; i++) {
					ptf.init(Config.tempOutDir+termFreqFiles[i],formaIds);
					ptf.createDocRows();
				}

				//inserimenti nel DB di wbt_app_glossario_frequenze
				ptf.insertIntoGlossFreq();
			}
			/*
			 * Parte COORD: da capire come integrarla, nel senso se si deve prevedere
			 * nel file di configurazione XML un flag di abilitazione/disabilitazione
			 * del COORD o cos'altro.
			 */
			if(Config.enableCoord){
				calculateCoord(unigrams, conn);
			}
			unigrams.clear();
			unigrams = null;
			/*
			 * Parte Related Terms
			 */
			if(Config.enableRelated){
				calculateRT(glossarioHM.getFormaIdentificativo(),conn);
			}

			glossarioHM.clear();
			glossarioHM = null;
			//termineIdFreq.clear();
			//termineIdFreq = null;
			if ( timeDebug ) {
				sw3.stop();
			}
		}
		//in ultimo ripulisco la directory di output
		if(!Config.tempOutDir.equals(Config.docRepository)){
			//	Utils.deleteDirectory(Config.tempOutDir);
		}else{
			logger.warning("Cannot remove "+Config.tempOutDir);
		}


		Utils.sendEmail(Config.mailserver,Config.email,"Successful runnnig jt2k", Config.idRequest);
		logger.info("Process is done." );
		if ( timeDebug ) {
			long result = sw1.getTime()+sw2.getTime()+sw3.getTime();
			System.err.println("Jt2k;main;21-11-2006;00:00:00;0;nextg;"+result);
		}
		//System.gc();
		Utils.touchFile(Config.tempOutDir + "finito.txt");
		return "Process is done.";
	}

	private  void calculateCoord(HashMap unigrams, Connection conn){
		/*
		 * Parte COORD: da capire come integrarla, nel senso se si deve prevedere
		 * nel file di configurazione XML un flag di abilitazione/disabilitazione
		 * del COORD o cos'altro.
		 */

		//il coord parte dall'uscita .t2c di T2C e la da in pasto a IDEAL con il file
		//delle macro generate da T2C
		//Il parser dell'uscita dek .ideal ?? la stesso: occorre mettere le informazioni
		//in altre hash poi da passare allo statistico.

		logger.info("Start Coord");
		String[] t2cFiles = Utils.listingFiles(Config.tempOutDir,".t2c");
		String[] macroFiles = Utils.listingFiles(Config.tempOutDir,".macro");

		if(t2cFiles.length!=macroFiles.length){
			logger.severe("Some t2c files don't have associated macro file!");
			return;
		}
		logger.info("Run Ideal on: "+Config.tempOutDir);
		for (int i = 0; i < t2cFiles.length; i++) {
			logger.fine("Run Ideal on: "+Config.tempOutDir+t2cFiles[i]);
			// macro, rule, filchunkato
			NLPTools.runIdealMacro(Config.tempOutDir+macroFiles[i],
					Config.dataRepository+Consts.RULE_COORD,Config.tempOutDir+t2cFiles[i]);
		}
		t2cFiles = null;
		macroFiles = null;
		System.gc();

		T2KParserIdeal pi = new T2KParserIdeal();
		BigramsHM bighmcoord = new BigramsHM(unigrams);

		//ciclo sui file.ideal
		String[] idealFiles = Utils.listingFiles(Config.tempOutDir,".ideal");
		int numbOfBigrams = 0;
		logger.info("Run ParseIdeal on: "+Config.tempOutDir);
		for (int i = 0; i < idealFiles.length; i++) {
			//	System.err.println(Config.tempOutDir+idealFiles[i]);
			logger.fine("Run ParseIdeal on: "+Config.tempOutDir+idealFiles[i]);
			pi.init(Config.tempOutDir+idealFiles[i]);
			bighmcoord.init(/*bigHashMapcoord,*/pi/*,sxHashMapcoord,dxHashMapcoord,unigrams*/);
			numbOfBigrams += bighmcoord.findNGrams();
			//Utils.deleteFile(Config.docRepository+idealFiles[i]);
		}
		pi = null;

		// statistico 
		StatisticClass statistico = new StatisticClass();
		HashFreq hfcoord = Utils.tagliaSoglia(bighmcoord.getBigramHash(),Config.soglia_bigrammi);
		Vector bigramsCoord = statistico.elabora(bighmcoord.getSxUnigramHash(),
				bighmcoord.getDxUnigramHash(),hfcoord.getHash(),hfcoord.getFreq());

		bighmcoord.clear();
		bighmcoord = null;
		/*sxHashMapcoord.clear();
		sxHashMapcoord = null;
		dxHashMapcoord.clear();
		dxHashMapcoord = null;
		bigHashMapcoord.clear();
		bigHashMapcoord = null;*/
		//Pruning dei termini semplici e complessi con soglie
		Vector terminiCoord = Utils.prunes(bigramsCoord,Config.sogliaTerminiCoord);

		String termCoord;
		BufferedWriter alltermsCoord;
		try {
			alltermsCoord = new BufferedWriter(new FileWriter (Config.tempOutDir + "alltermscoord.out"));
			for(int i = 0; i < terminiCoord.size(); i++){
				termCoord = ((BigramMisura)terminiCoord.elementAt(i)).getLeft()+" "+
				((BigramMisura)terminiCoord.elementAt(i)).getRight()+"\n";
				//termCoord = termCoord.replaceAll("#"," ");
				alltermsCoord.write(termCoord);
				//alltermsCoord.write("\n");
			}
			terminiCoord.clear();
			terminiCoord = null;
			alltermsCoord.flush();
			alltermsCoord.close();
			alltermsCoord = null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String sep = System.getProperty("file.separator");
		String library_path = System.getProperty("user.dir") + sep;
		if ( System.getProperty("catalina.home") != null){
			//				library_path = System.getProperty("catalina.home") + sep + "webapps" +sep + "axis" + sep + "WEB-INF" + sep + "lib" + sep;
			library_path = System.getProperty("catalina.home") + sep + "webapps" +sep + "axis" + sep + "WEB-INF" + sep + "lib" + sep + "jt2k" + sep;
		}
		logger.info("Trying to load t2cmapper : library_path = " + library_path);

		logger.info("Inizializzazione del t2cmapper: "+Config.tempOutDir + "allterms.out");
		T2CMapper.init(library_path);
		logger.info("Trying to start t2cmapper : Config.tempOutDir = " + Config.tempOutDir);

		//Run del t2cmapper sui chuggati
		T2CMapper.startTools(Config.tempOutDir + "alltermscoord.out");

		String outputFile = null;
		String[] t2cFilesCoord = Utils.listingFiles(Config.tempOutDir,".t2c");

		for (int i = 0; i < t2cFilesCoord.length; i++) {
			// macro, rule, filchunkato
			//System.err.println(Config.tempOutDir+t2cFilesCoord[i]);
			//System.err.println("x t2cmapper: "+Config.tempOutDir+chugIdeFiles[i]);
			//outputFile = t2cFiles[i].replaceFirst("\\.t2c","");
			outputFile = t2cFilesCoord[i];
			logger.info("Run Term2ChunkMapper on: "+Config.tempOutDir+outputFile);
			//System.err.println("x t2cmapper: "+outputFile);
			try{
				T2CMapper.runTools(Config.tempOutDir+t2cFilesCoord[i],
						Config.tempOutDir+outputFile);
			}catch(Throwable e) {e.printStackTrace();};
			logger.fine("Run Term2ChunkMapper on: "+Config.tempOutDir+outputFile+" is done.");
			Utils.deleteFile(Config.tempOutDir+outputFile+".t2c");
		}		
		outputFile = null;
		t2cFilesCoord = null;

		T2KParserT2C t2c = new T2KParserT2C();
		GlossarioHM glossarioHMCoord = new GlossarioHM();

		if(conn==null){
			Consts.logger.log(Level.SEVERE,"Cannot establish a connection to DB");
			return; 
		}
		String[] termFormeFilesCoord = Utils.listingFiles(Config.tempOutDir,".t2c.termforme");
		for (int i = 0; i < termFormeFilesCoord.length; i++) {
			//			System.err.println(Config.tempOutDir+termFormeFiles[i]);
			logger.fine("Run ParseT2C on: "+Config.tempOutDir+termFormeFilesCoord[i]);
			t2c.init(Config.tempOutDir+termFormeFilesCoord[i]);
			glossarioHMCoord.init(/*termsCoord,termsformsCoord,formaIdsCoord,termineIdFreqCoord,*/t2c/*,conn*/);
			glossarioHMCoord.creaGlossario();
		}

		logger.fine("Assegno le stop-word: "+stopWordPattern.size());
		glossarioHMCoord.setStopWordPattern(stopWordPattern);

		HashMap newtermsforms = Utils.getMostFrequent(glossarioHMCoord.getGlossarioLemmaForme());
		glossarioHMCoord.setGlossarioTerminiForma(newtermsforms);
		glossarioHMCoord.putGlossarioIntoDB(/*PER ORA 0: ancora non implementata*/ 0);
		glossarioHMCoord.creaGlossarioBTNT();

		t2c = null;
		//termsCoord.clear();
		//termsCoord = null;
		//termsformsCoord.clear();
		//termsformsCoord = null;
		termFormeFilesCoord = null;
		//termineIdFreqCoord.clear();
		//termineIdFreqCoord = null; 
		//glossarioHMCoord.getGlossarioLemmaForme().clear();
		//glossarioHMCoord.getNomiPropri().clear();
		glossarioHMCoord.getStopWordPattern().clear();

		//		System.gc();

		String[] termFreqFilesCoord = Utils.listingFiles(Config.tempOutDir,".t2c.termfreq");
		T2KParserTermFreq ptf = new T2KParserTermFreq(newtermsforms,termFreqFilesCoord.length,conn);
		HashMap formaIdsCoord = glossarioHMCoord.getFormaIdentificativo();

		for (int i = 0; i < termFreqFilesCoord.length; i++) {
			ptf.init(Config.tempOutDir+termFreqFilesCoord[i],formaIdsCoord);
			ptf.createDocRows();
		}
		termFreqFilesCoord = null;

		//inserimenti nel DB di wbt_app_glossario_frequenze
		ptf.insertIntoGlossFreq();

		ptf = null;
		/*
		 * Fine COORD 
		 */

	}

	/* A RT deve arrivare:
	 * 1 - 
	 */ 
	private  void calculateRT(HashMap newtermsforms, Connection conn){
		T2KParserIdeal pi = new T2KParserIdeal();
		OSRelHM osrelhm = new OSRelHM();
		HashMap stopVerb = new HashMap(Consts.HASH_CAPACITY); //

		/*si fa partire ideal*/
		String lessico = Config.dataRepository + System.getProperty("file.separator")+Consts.LESSICO_RT;
		String macro = Config.dataRepository + System.getProperty("file.separator")+ Consts.MACRO_RT;
		String rule = Config.dataRepository + System.getProperty("file.separator")+ Consts.RULE_RT;
		String stopPronVerb = Config.dataRepository + System.getProperty("file.separator")+ Consts.STOP_PRON_VERB;
		stopVerb = (HashMap) Utils.readFileInHashMap(stopPronVerb);
		if(null == stopVerb){
			logger.warning("No " +Consts.STOP_PRON_VERB +" file provided.");
		}
		int fromIdealStart = Ideal.startIdealParser(lessico, macro, rule);
		String[] t2cFiles = null;
		if(Config.relatedOnChug){
			/* usare chug.ide solo nel caso della IKEA*/
			t2cFiles = Utils.listingFiles(Config.tempOutDir,".chug.ide");
		}else{
			t2cFiles = Utils.listingFiles(Config.tempOutDir,".t2c");
		}
		for (int i = 0; i < t2cFiles.length; i++) {
			logger.fine("Run IDEAL on: "+Config.tempOutDir+t2cFiles[i]);
			Ideal.runIdealParser(Config.tempOutDir+t2cFiles[i],fromIdealStart);
		}
		/* ciclo sui file. */
		String[] osrelFiles = Utils.listingFiles(Config.tempOutDir,".osrel");
		int numbOfBigrams = 0;
		for (int i = 0; i < osrelFiles.length; i++) {
			//System.err.println(Config.tempOutDir+osrelFiles[i]);
			logger.fine("Run ParseIdeal on: "+Config.tempOutDir+osrelFiles[i]);
			pi.init(Config.tempOutDir+osrelFiles[i]);
			osrelhm.init(/*nounFreq,verbFreq,verbFreqNoRel,nounVerbFreq,nounVerb,verbNoun,*/pi,stopVerb/*,unigrams*/);
			numbOfBigrams += osrelhm.findNGrams();

			/*Utils.deleteFile(Config.docRepository+idealFiles[i]);*/
		}
		//Utils.printHashWithVectorValues(osrelhm.getNounVerb());

		logger.finer("osrelhm.getNounVerb().size(): " + osrelhm.getNounVerb().size());
		//pi = null;
		//logger.severe("PERIODO: "+((Vector) osrelhm.getNounVerb().get("PERIODO")).size());
		if(stopVerb != null){
			stopVerb.clear();
			stopVerb = null;
		}
		System.gc();
		//logger.severe("PERIODO: "+((Vector) osrelhm.getNounVerb().get("PERIODO")).size());

		//va calcolata la type frequency! partendo da nounVerb e da verbNoun
		/*	HashMap typeFrequencyNoun = calculateTypeFrequency(nounVerb);
		HashMap typeFrequencyVerb = calculateTypeFrequency(verbNoun);
		 */
		//printHashMap(nounFreq);
		//printHashMap(verbFreq);
		//System.exit(-1);
		StatisticClass statistico = new StatisticClass();
		/*HashFreq hf = Utils.tagliaSoglia(bigHashMap,Config.soglia_bigrammi);
		if(hf == null) {
			logger.log(Level.SEVERE,"Error on bigram hash!");
			return;
		}*/
		//printHashMap(osrelhm.getVerbFreq());
		Vector bigrams = statistico.elabora(osrelhm.getVerbFreq(),
				osrelhm.getNounFreq(),osrelhm.getNounVerbFreq(),numbOfBigrams);
		statistico = null;
		//printHashMap(verbFreq);
		/*BigramMisura elem;
		for (int i =0; i< bigrams.size(); i++){
			elem = (BigramMisura) bigrams.elementAt(i);
			System.err.println(elem.getLeft()+" "+elem.getRight()+" "+elem.getLog());
		}*/


		//Chiamata al calcolo degli RT
		CalcoloRT rt = new CalcoloRT(newtermsforms,conn);

		//logger.severe("PERIODO: "+((Vector) osrelhm.getNounVerb().get("PERIODO")).size());

		rt.conSim(osrelhm.getVerbNoun(), osrelhm.getNounVerb(), osrelhm.getVerbFreq(),
				osrelhm.getVerbFreqNoRel(), osrelhm.getNounFreq(), /*glossario ,*/bigrams);

		bigrams.clear();
		bigrams = null;
		//glossario.clear();
		//glossario = null;
		rt = null;
		System.gc();

	}


	/*	private HashMap calculateTypeFrequency(LinkedHashMap hash) {
		//hash ha come chiave delle String e come valore dei Vector di String
		//fa fatta la scansione della hash e contare quanti elementi ha il vector
		return null;
	}
	 */
	private void updateDocuments(Connection conn){
		//parsing dei chunkati generati
		/*T2KParserChunk pc = new T2KParserChunk();
		T2KParserIdeal pi = new T2KParserIdeal();

		String[] unigrams;
		Integer oldValue, newValue;*/

		//Occorre caricare in due hashmap sia la tabella app_glossario per recuperare l'id
		//e la forma, sia app_glossario_frequenza per recuperare/trovare in quanti documenti
		//il termine del glossario occorre

		//recupero dal DB gli id e i termini che sono nel glossario; inoltre nel vettore forme
		//memorizzo le forme lemmatizzate dei termini del glossario che useremo per costruire allterms.out
		Vector forme= new Vector();
		HashMap glossarioIdFreq = new HashMap(Consts.HASH_CAPACITY);
		HashMap glossarioLemmaId = DBManager.retrieveFromGlossario(conn, forme, glossarioIdFreq);

		if(glossarioLemmaId == null){
			Utils.sendEmail(Config.mailserver,Config.email,"Error: Impossible to make update", Config.idRequest);
			logger.info("Process is done with errors." );
			return;
		}

		//HashMap kwHM = DBManager.retrieveFromGlossarioFrequenza(conn);

		//Costruisco allterms.out
		String term;
		BufferedWriter allterms;
		try {
			allterms = new BufferedWriter(new FileWriter (Config.tempOutDir + "allterms.out"));
			for(int i = 0; i < forme.size(); i++){
				term = (String)forme.elementAt(i);
				allterms.write(term);
				allterms.write("\n");
			}
			term = null;
			allterms.flush();
			allterms.close();
			allterms = null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		String sep = System.getProperty("file.separator");
		String library_path = System.getProperty("user.dir") + sep;
		if ( System.getProperty("catalina.home") != null){
			//				library_path = System.getProperty("catalina.home") + sep + "webapps" +sep + "axis" + sep + "WEB-INF" + sep + "lib" + sep;
			library_path = System.getProperty("catalina.home") + sep + "webapps" +sep + "axis" + sep + "WEB-INF" + sep + "lib" + sep + "jt2k" + sep;
		}
		logger.info("Trying to load t2cmapper : library_path = " + library_path);

		logger.info("Inizializzazione del t2cmapper: "+Config.tempOutDir + "allterms.out");
		T2CMapper.init(library_path);
		logger.info("Trying to start t2cmapper : Config.tempOutDir = " + Config.tempOutDir);


		//Inizializzazione del t2cmapper (ex-Daniela+)
		T2CMapper.startTools(Config.tempOutDir + "allterms.out");

		//Run del t2cmapper sui chuggati
		String outputFile = null;
		String[] chugIdeFiles = Utils.listingFiles(Config.tempOutDir,"chug.ide");
		for (int i = 0; i < chugIdeFiles.length; i++) {
			//System.err.println(Config.tempOutDir+chugIdeFiles[i]);
			//System.err.println("x t2cmapper: "+Config.tempOutDir+chugIdeFiles[i]);
			outputFile = chugIdeFiles[i].replaceFirst("\\.chug\\.ide","");
			logger.info("Run Term2ChunkMapper on: "+Config.tempOutDir+chugIdeFiles[i]);
			//System.err.println("x t2cmapper: "+outputFile);
			try{
				T2CMapper.runTools(Config.tempOutDir+chugIdeFiles[i],
						Config.tempOutDir+outputFile);
			}catch(Throwable e) {e.printStackTrace();};
		}		
		outputFile = null;


		if(conn==null){
			logger.severe("Cannot establish a connection to DB");
			//return "Cannot establish a connection to DB"; 
		}

		//LinkedHashMap newtermsforms = Utils.getMostFrequent(termsforms);

		//Non mi interessa costruire un glossario dei documenti che fanno parte dell'aggiornamento
		//xche' comunque il glossario non deve essere "aumentato" di termini
		//Parsing dei file termini-ferquenza (in uscita da t2cmapper) 
		String[] termFreqFiles = Utils.listingFiles(Config.tempOutDir,".termfreq");
		//T2KParserTermFreq ptf = new T2KParserTermFreq(termsforms,termFreqFiles.length,conn);
		//T2KParserTermFreq ptf = new T2KParserTermFreq(newtermsforms,termFreqFiles.length,conn);
		T2KParserTermFreq ptf = new T2KParserTermFreq(glossarioLemmaId,termFreqFiles.length,conn);
		//printHashMap(formaIds);
		HashMap oldGlossarioIdFreq = new HashMap(glossarioIdFreq);

		for (int i = 0; i < termFreqFiles.length; i++) {
			ptf.init(Config.tempOutDir+termFreqFiles[i],glossarioLemmaId);
			ptf.createDocRowsOnUpdate(glossarioIdFreq);
			//ptf.createDocRows();
		}

		//si fa l'aggiornamento del glossario con i valori calcolati in glossarioIdFreq
		//va fatta la scansione dell'hashmap

		Set set= glossarioIdFreq.keySet();
		Iterator iter = set.iterator();
		String key;
		String value;
		String freq;
		while(iter.hasNext()){
			key = (String) iter.next();
			value = (String) glossarioIdFreq.get(key);
			freq = (String) oldGlossarioIdFreq.get(key);
			if(!value.equals(freq)){
				//aggiorna il glossario con una UPDATE wbt_app_glossario SET valore=value where KWID = key;
				DBManager.updateGlossarioTable(key,value,conn);
			}
		} 

		//va fatto l'aggiornamento delle frequenze del glossario.

		//inserimenti nel DB di wbt_app_glossario_frequenze
		ptf.insertIntoGlossFreq();
	}




	/**
	 * Elimina file temporanei
	 * @param fileName radice del nome dei file da eliminare
	 */
	private static void deleteTemporaryFiles(String fileName){
		Utils.deleteFile(fileName+".tok");
		Utils.deleteFile(fileName+".seg");
		Utils.deleteFile(fileName+".morph.000.tmp");
		Utils.deleteFile(fileName+".morph");
		Utils.deleteFile(fileName+".conv");

	}
	/**
	 * Crea la directory, se non esiste gia'
	 * @param name path assoluto della directory da creare
	 */
	private static void createDirectory(String name){
		try {

			boolean exists = (new File(name)).exists();
			boolean success;
			if (!exists) {
				success = (new File(name)).mkdirs();
				if (!success) {
					logger.severe("Cannot create "+name+" directory");
					//System.exit(-1);
				}
			}else{
				logger.info("Directory "+ name + " already created");
			}
		} catch (Exception e) {
			logger.severe("non posso creare: " + name);
		}
		logger.info("Succefully created directory "+ name );
	}

	private static Vector loadStopWord(){
		String pathStopWord = Config.dataRepository+Consts.STOP_WORD;
		logger.info("loadStopWord(): "+pathStopWord);
		Vector stopWordPattern = null;
		try {
			BufferedReader in = new BufferedReader(new FileReader(pathStopWord));
			String str;
			stopWordPattern = new Vector();
			while ((str = in.readLine()) != null) {
				stopWordPattern.addElement(Pattern.compile(str,Pattern.CASE_INSENSITIVE));
			}
			in.close();
		} catch (IOException e) {
			logger.warning("Stop-word file not provided. Disabled!");
		}

		return stopWordPattern;
	}

	private static HashMap loadNomiPropri(){
		String pathNomiPropri = Config.dataRepository+Consts.NOMI_PROPRI;
		logger.info("loadNomiPropri(): "+pathNomiPropri);
		HashMap nomiPropriHM = null;
		try {
			BufferedReader in = new BufferedReader(new FileReader(pathNomiPropri));
			String str;
			nomiPropriHM = new HashMap(Consts.HASH_CAPACITY);
			while ((str = in.readLine()) != null) {
				//System.err.println(str);
				nomiPropriHM.put(str.toUpperCase(),new Integer(1));
			}
			in.close();
		} catch (IOException e) {
			logger.warning(Config.dataRepository+Consts.NOMI_PROPRI  + " doesn't exist");
			logger.warning("Nomi-propri file not provided. Disabled!" );
		}
		return nomiPropriHM;
	}

	/**
	 * Stampa il contenuto di un hash
	 * Sia la chiave che il valore devo essere String
	 * @param hash l'hashmap da stampare
	 */
	private static void printHashMap(HashMap hash){
		System.err.println("hash size: "+hash.size());
		Iterator it = hash.entrySet().iterator();
		while (it.hasNext()){
			Map.Entry elem = (Map.Entry)it.next();
			System.err.println(elem.getKey()+" "+elem.getValue());
		}
		/*it = bigHashMap.entrySet().iterator();
		 while (it.hasNext()){
		 Map.Entry elem = (Map.Entry)it.next();
		 System.err.print(((Bigrams)elem.getKey()).getLeft()+" ");
		 System.err.println(((Bigrams)elem.getKey()).getRight()+" "+elem.getValue());
		 }*/

	}
	/**
	 * Stampa il contenuto di una hash i cui values sono di tipo <i>FormFreq</i>
	 * @param hash l'hashmap da stampare
	 */
	private static void printHashMapFormFreq(HashMap hash){
		System.err.println("hash size: "+hash.size());
		Iterator it = hash.entrySet().iterator();
		while (it.hasNext()){
			Map.Entry elem = (Map.Entry)it.next();
			Vector vals = (Vector) elem.getValue();
			for(int i=0; i< vals.size(); i++){
				FormFreq val = (FormFreq) vals.elementAt(i);
				System.err.println(elem.getKey()+", "+val.getForm()+" "+val.getFreq());
			}
		}

	}

	// servername, port, name, login, password ,driver, tablesuffix, 
	// docrepository, datarepository, tempdirectory
	public void addConfiguration(String servername, String port, String name,
			String login, String password ,String driver, String tablesuffix, 
			String docrepository, String datarepository, String tempdirectory) {

		Config.dbServerName = servername;
		Config.dbPortNumber = port;
		Config.dbName = name;
		Config.dbLogin = login;
		Config.dbPassword = password;
		Config.dbDriverName = driver;
		Config.tableSuffix = tablesuffix;
		Config.docRepository = docrepository;
		Config.dataRepository = datarepository;
		Config.tempOutDir = tempdirectory;

	}

	public void printConfiguration () {
		System.err.println("Configuration Added");
		System.err.println("Config.dbServerName = " + Config.dbServerName);
		System.err.println("Config.dbPortNumber = " + Config.dbPortNumber);
		System.err.println("Config.dbName = " + Config.dbName);
		System.err.println("Config.dbLogin = " + Config.dbLogin);
		System.err.println("Config.dbPassword = " + Config.dbPassword);
		System.err.println("Config.dbDriverName = " + Config.dbDriverName);
		System.err.println("Config.tableSuffix = " + Config.tableSuffix);
		System.err.println("Config.docRepository = " + Config.docRepository);
		System.err.println("Config.dataRepository = " + Config.dataRepository);
		System.err.println("Config.tempOutDir = " + Config.tempOutDir);

	}

	private static void exportTable(String exportType) {

		String exportedDB = null;
		String fileName = "out.";
		if (null != exportType) {

			if ("html".equals(exportType)) {
				exportedDB = WSDBManager.exportAllTablesHTML();
				fileName += "html";
			} else if ("csv".equals(exportType)) {
				exportedDB = WSDBManager.exportAllTablesCSV();
				fileName += "csv";
			} else {
				exportedDB = WSDBManager.exportAllTablesXML();
				fileName += "xml";
			}
			if (null != exportedDB) {
				try {
					Utils.write2File(fileName, exportedDB.getBytes());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
	}



}
