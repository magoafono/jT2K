/**
 * $Id: Config.java,v 1.20 2007/11/15 15:52:43 simone Exp $
 * 
 */
package ilc.t2k;

public class Config {
	
	public static String email;
	public static String emaildmin;
	public static String mailserver;
	
	public static String idRequest;

	public static String tableSuffix;
	/* Soglia per il pruning del glossario (oovero si prendono i termini
	 * che hanno una frequenza maggiore di soglia_glossario*/
	public static int soglia_glossario;

	public static int soglia_bigrammi = 3;

	public static int sogliaTerminiSemplici = 10; /* default 10%*/
	public static int sogliaTerminiComplessi = 70; /* default 70%*/
	public static int sogliaTerminiCoord = 100; /*default 100%*/
	public static int sogliaTerminiRelated = 5; /*numero massimo di RT restituiti */
	public static int sogliaVarianti = 100;
	public static String measureType = "tf";
	
	public static boolean enableCoord = false;
	public static boolean enableRelated = false;
	public static boolean enableUpdate = false;
	public static boolean relatedOnChug = false;
	public static boolean enableVariants = false;
	public static boolean adjPotgov = false;
	public static boolean enableStop = false;
	public static boolean enableDocIndex = true;

	public static boolean customizedNouns = false;
	
	//directory nella quale si trovano i documenti da analizzare 
	public static String docRepository = null; 
	//direcory nella quale si trovano i lessici vari per ANITA
	public static String dataRepository = null;
	//directory nella quale si andranno i file di output di ANITA 
	public static String tempOutDir = null; 
	
	/* DB informations */
	public static String dbServerName = null;
	public static String dbPortNumber ;
	public static String dbName = null;
	public static String dbLogin = null;
	public static String dbPassword = null;
	public static String dbDriverName = "jdbc:jtds:sqlserver" ;
	public static String tablePrefix="wbt_app_"; 

	/*DB tables*/
	public static String dbTableDocument = "wbt_app_documenti";
	public static String dbTableGlossario = "wbt_app_glossario";
	public static String dbTableGlossarioBT = "wbt_app_glossario_bt";
	public static String dbTableGlossarioNT = "wbt_app_glossario_nt";
	public static String dbTableGlossarioRT = "wbt_app_glossario_rt";
	public static String dbTableGlossarioFreq = "wbt_app_glossario_frequenza";
	public static String dbTableGlossarioVariants = "wbt_app_glossario_varianti";
	
	
	public static String printConfiguration(){
		StringBuffer conf = new StringBuffer();
		conf.append("\ndataRepository: ");
		conf.append(dataRepository);
		conf.append("\ndbDriverName: ");
		conf.append(dbDriverName);
		conf.append("\ndbLogin: ");
		conf.append(dbLogin);
		conf.append("\ndbPassword: ");
		conf.append(dbPassword);
		conf.append("\ndbDriverName: ");
		conf.append(dbDriverName);
		conf.append("\ndocRepository: ");
		conf.append(docRepository);
		conf.append("\ndataRepository: ");
		conf.append(dataRepository);
		conf.append("\nCoord?: ");
		conf.append(enableCoord);
		conf.append("\nRelated terms?: ");
		conf.append(enableRelated);
		return conf.toString();
	}

	public static void cleanConfiguration(){
		email = "";
		emaildmin = "";
		mailserver = "";
		
		idRequest = "";

		 tableSuffix = "";
		/* Soglia per il pruning del glossario (oovero si prendono i termini
		 * che hanno una frequenza maggiore di soglia_glossario*/
		soglia_glossario = 0;

		soglia_bigrammi = 3;

		 sogliaTerminiSemplici = 10; /* default 10%*/
		 sogliaTerminiComplessi = 70; /* default 70%*/
		sogliaTerminiCoord = 100; /*default 100%*/
		sogliaTerminiRelated = 5; /*numero massimo di RT restituiti */
		
		enableCoord = false;
		enableRelated = false;
		enableUpdate = false;
		relatedOnChug= false;
		
		customizedNouns= false;
		
		//directory nella quale si trovano i documenti da analizzare 
		docRepository= ""; 
		//direcory nella quale si trovano i lessici vari per ANITA
		dataRepository= "";
		//directory nella quale si andranno i file di output di ANITA 
		tempOutDir= ""; 
		
		/* DB informations */
		 dbServerName= "";
		dbPortNumber= "";
		dbName= "";
		dbLogin= "";
		dbPassword= "";
		dbDriverName = "jdbc:jtds:sqlserver" ;
		tablePrefix="wbt_app_"; 
		

		 dbTableDocument = "wbt_app_documenti";
		 dbTableGlossario = "wbt_app_glossario";
		dbTableGlossarioBT = "wbt_app_glossario_bt";
		dbTableGlossarioNT = "wbt_app_glossario_nt";
		 dbTableGlossarioRT = "wbt_app_glossario_rt";
		dbTableGlossarioFreq = "wbt_app_glossario_frequenza";

	}
	


}
