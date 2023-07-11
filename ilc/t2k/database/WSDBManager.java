package ilc.t2k.database;

import ilc.t2k.Config;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import com.fdsapi.DataAccess;
import com.fdsapi.DataAccessClient;
import com.fdsapi.FormattedDataSet;
import com.fdsapi.ResultSetConverter;

public class WSDBManager {

	private static Logger logger = Logger.getLogger("jt2k");

	public static String exportAllTablesXML () {

		System.err.println("exportAllTablesXML 1");
		String ret = DBManager.getHeadXML();
		ret += exportAllTables("xml1");
		ret += DBManager.getEndXML();
		System.err.println("exportAllTablesXML 2");
		return ret;
	}

	public static String exportAllTablesCSV() {

		return exportAllTables("csv");
	}

	public static String exportAllTablesHTML() {

		return exportAllTables("htmlTable");
	}

	public static String exportAllTables (String exportType) {

		String ret = exportTableGlossario(exportType);
		ret += exportTableGlossarioFreq(exportType);
		ret += exportTableGlossarioBT(exportType);
		ret += exportTableGlossarioNT(exportType);
		if ( Config.enableVariants ) {
			ret += exportTableGlossarioVariants(exportType);
		}
		if ( Config.enableRelated ) {
			ret += exportTableGlossarioRT(exportType);
		}

		return ret;
	}

	public static String exportTableGlossario (String  exportType) {

		String ret = exportTable(Config.dbTableGlossario, exportType);
		return ret;
	}
	public static String exportTableGlossarioBT (String  exportType) {

		String ret = exportTable(Config.dbTableGlossarioBT, exportType);
		return ret;
	}

	public static String exportTableGlossarioNT (String  exportType) {

		String ret = exportTable(Config.dbTableGlossarioNT, exportType);
		return ret;
	}

	public static String exportTableGlossarioVariants (String  exportType) {

		String ret = exportTable(Config.dbTableGlossarioVariants, exportType);
		return ret;
	}

	public static String exportTableGlossarioRT (String  exportType) {

		String ret = exportTable(Config.dbTableGlossarioRT, exportType);
		return ret;
	}

	public static String exportTableGlossarioFreq (String  exportType) {

		String ret = exportTable(Config.dbTableGlossarioFreq, exportType);
		return ret;
	}




	public static String exportTable (String tableName, String exportType){

		String ret = null;
		Statement stmt = DBManager.getStatement();
		logger.info("exportType: " + exportType);
		try {
			String query = DBManager.createSelectAllSqlString (tableName);
			ResultSet rs = stmt.executeQuery (query);
			Map map=new HashMap();
			map.put("rootElement", tableName);
			if ("csv".equalsIgnoreCase(exportType)) {
				ret = exportCSVSqlResultSet(rs, query, map);
			} else if ("htmlTable".equalsIgnoreCase(exportType)){
				ret = exportHTMLSqlResultSet(rs, query, map);
			}else {
				ret = exportXMLSqlResultSet(rs, query, map);
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			ret = null;
		}
		return ret;
	}


	public static String exportXMLSqlResultSet ( ResultSet resultSet, String query, Map map) {

		return exportSqlResultSet (  resultSet,  query,  map, "xml1");
	}

	public static String exportHTMLSqlResultSet ( ResultSet resultSet, String query, Map map) {

		return exportSqlResultSet (  resultSet,  query,  map, "htmlTable");
	}

	public static String exportCSVSqlResultSet ( ResultSet resultSet, String query, Map map) {

		String result = null;
		//System.err.println("exportCSVSqlResultSet(): 1" );
		result = getMetaDataCSV(resultSet);
		result += exportSqlResultSet (  resultSet,  query,  map, "csv" ); 
		//System.err.println("exportCSVSqlResultSet(): " + result);
		return result;
	}

	private static String getMetaDataCSV(ResultSet resultSet) {

		String header = "";

		ResultSetMetaData rsmd;
		try {
			rsmd = resultSet.getMetaData();
			logger.info("rsmd.getColumnCount() = " + rsmd.getColumnCount());
			for ( int col = 1 ; col < rsmd.getColumnCount()-1; col++) {
				header += rsmd.getColumnName(col);
				header += ",";
			}
			header += rsmd.getColumnName(rsmd.getColumnCount());
			header += "\n";
			//System.err.print("*** "+header);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return header;
	}

	public static String exportSqlResultSet ( ResultSet resultSet, String query, Map map, String exportType) {

		DataAccess da = null;
		String output = null;
		try {
			logger.info("Start exportSqlResultSet " + Config.dbDriverName+"://"+Config.dbServerName+"/"+Config.dbName);

			da = new DataAccessClient("com.mysql.jdbc.Driver", Config.dbDriverName+"://"+Config.dbServerName+"/"+Config.dbName, Config.dbLogin, Config.dbPassword, false);

			FormattedDataSet fds=FormattedDataSet.createInstance();

			fds.setDataAccessFactory(da);

			ResultSetConverter rsc = da.getResultSetConverter (query); 

			output = fds.getFormattedDataSet(rsc, map, exportType);
			//xml = fds.getFormattedDataSet(rsc, map, "htmlTable");

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return output;
	}

	public static void cleanDataBase() {

		cleanGlossario () ;
		cleanGlossarioBTTable();
		cleanGlossarioNTTable();
		cleanGlossarioRTTable();
		cleanGlossarioVariantTable();
		cleanGlossarioFreqTable();

	}


	public static void cleanGlossarioBTTable () {
		cleanTable (Config.dbTableGlossarioBT);
	}
	public static void cleanGlossarioNTTable () {
		cleanTable (Config.dbTableGlossarioNT);
	}
	public static void cleanGlossarioRTTable () {
		cleanTable (Config.dbTableGlossarioRT);
	}
	public static void cleanGlossarioVariantTable () {
		cleanTable (Config.dbTableGlossarioVariants);
	}
	public static void cleanGlossarioFreqTable () {
		cleanTable (Config.dbTableGlossarioFreq);
	}

	public static void cleanTable (String tableName) {
		Statement stmt = DBManager.getStatement();
		try {
			stmt.execute("Delete from  " + tableName);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static void cleanGlossario () {
		logger.info("Start cleanGlossario");
		try {
			Connection conn = ConnectionManager.getConnection();
			Statement stmt = conn.createStatement();
			stmt.execute("Drop table wbt_app_glossario");
			stmt.execute("CREATE TABLE wbt_app_glossario (" +
					" KWID int not null AUTO_INCREMENT PRIMARY KEY, "+
					" termine varchar(255), "+
					" valore int, "+
					" lemma varchar(255), "+
			" stop varchar(255));" );
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			logger.severe(e.getMessage());
		}
	}


}
