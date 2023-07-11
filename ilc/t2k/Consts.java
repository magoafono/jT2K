/**
 * $Id: Consts.java,v 1.33 2009/03/27 15:22:22 simone Exp $
 */
package ilc.t2k;

import java.util.logging.Logger;

public class Consts {

	public static Logger logger = Logger.getLogger("jt2k");
	public static final String defaultConfig ="default.xml"; 
	public static final String DELIM = " ";
	public static final String SSP_CHUNK = "(.)+#S[P]?@(.)+";
//	public static final String ASSP_CHUNK = "(.)+#(A|S|SP|V)@(.)+"; //I verbi presi dal chunkato sono "vuoti" di significato
	public static final String ASSP_CHUNK = "(.)+#(A|S|SP)@(.)+";
//	public static final String SSP_CHUNK = "(.)+#V@(.)+";
	public static final String PIPE_RX = "\\|";
	public static final String HASH_SIGN = "#";

	/* default */
	public static final String CHUNK_SELECT_RX = "(.)+CC: (di_C|P_C|N_C)(.)+";

	//public static final String CHUNK_SELECT_RX = "(.)+CC: (di_C|P_C|N_C|NA_C|PNA_C)(.)+";
	//public static final String CHUNK_SELECT_RX = "(.)+CC: (di_C|P_C|N_C|NA_C|PNA_C|FV_C|G_C|I_C|PART_C)(.)+";
	//con verbi
	//public static final String CHUNK_SELECT_RX = "(.)+CC: (di_C|P_C|N_C|FV_C|G_C|I_C|PART_C)(.)+";
	//public static final String CHUNK_SELECT_RX = "(.)+CC: (FV_C|G_C|I_C|PART_C)(.)+";
	public static final String PREP_ARTICOLATE = "(.+)_(PER|DI|DA|CON|A|IN|SU|TRA|FRA)_(IL_|LO_|UN_|UNA_)?(.+)"; 
	//TAG name
	public static final String THRESHOLD = "threshold";
	public static final String DATABASE = "database"; 
	public static final String INFO = "info";
	//TAG type
	public static final String GLOSSARIO = "glossario";
	public static final String BIGRAMS = "bigrammi";
	public static final String T_SIMPLE = "termsemplici";
	public static final String T_COMPLEX = "termcomplessi";
	public static final String T_RELATED = "termrelated";
	public static final String SOGLIA_VARIANTS = "varianti";
	public static final String MEASURE_TYPE = "measuretype";
	
	public static final String DB_SERVERNAME = "servername"; 
	public static final String DB_PORT = "port"; 
	public static final String DB_NAME = "name"; 
	public static final String DB_LOGIN = "login"; 
	public static final String DB_PASSWD = "password"; 
	public static final String DB_DRIVER = "driver"; 
	public static final String EMAIL = "email"; 
	public static final String EMAILADM = "emailadmin"; 
	public static final String MAILSERVER = "mailserver"; 
	public static final String ID_REQ = "idrequest"; 
	public static final String TABLE_SUFFIX = "tablesuffix"; 
	public static final String DOC_REPOSITORY = "docrepository"; 
	public static final String DATA_REPOSITORY = "datarepository"; 
	public static final String TMPDIR = "tempdirectory"; 
	
	public static final String ENABLE_COORD = "enablecoord";
	public static final String ENABLE_RELATED = "enablerelated";
	public static final String ENABLE_UPDATE = "enableupdate";
	public static final String ENABLE_STOP = "enablestopprep";
	public static final String RELATED_ON_CHUG = "relatedonchug";
	public static final String ENABLE_CUSTOMIZED_NOUNS = "customizednouns";
	public static final String ENABLE_DOCINDEX = "enabledocindex";

	public static final String RULE_COORD = "rule_coord.txt";
	public static final String LESSICO_RT = "LESSICO.txt";
	public static final String MACRO_RT = "MACRO.txt";
	public static final String RULE_RT = "RULE-RT.txt";
	
	public static final String STOP_WORD = "stop-word.txt";
	public static final String NOMI_PROPRI = "nomi_propri.txt";
	public static final String STOP_PRON_VERB = "stopPronVerb.txt";

	//public static final String CUSTOMIZED_NOUNS = "customized-nouns.txt";
	public static final int HASH_CAPACITY = 1024 * 1024;
	public static final int HASH_HIGH_CAPACITY = 131456;
	public static final String ENABLE_VARIANTS = "enablevariants";
	public static final String ADJ_POTGOV = "adjpotgov";
	
	public static final String INVALID_PREP = "[\"\\.;:]";
	

}
 