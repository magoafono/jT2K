package ilc.t2k.database;

import ilc.t2k.Config;
import ilc.t2k.Utils;
import ilc.t2k.bean.FormFreq;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

//import com.fdsapi.DataAccess;

public class DBManager {


	private static Logger logger = Logger.getLogger("jt2k");

	/**
	 * Recupera il numero di documenti per i quali sono gia' stati individuati dei termini indicizzati
	 * @param conn connessione al DB
	 * @return numero di documenti
	 */
	public static  int retrieveNoOfDoc(Connection conn){
		String sql = null;
		int count = 0;
		try {
			sql = "SELECT count(*) FROM "+ Config.dbTableDocument +" WHERE kw is not null";
			logger.finer(sql);

			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(sql);

			if (rs.next()){
				count = rs.getInt(1);
				logger.finer(String.valueOf(count));
			}

		} catch (SQLException e) {
			logger.log(Level.SEVERE,"SELECT count(*) FROM "+ Config.dbTableDocument +" WHERE kw is not null");
			//e.printStackTrace();
			logger.severe("SQL: "+sql);
			logger.severe("SQL: "+e.getMessage());
		}finally{
			//conn.close();
		}
		return count;
	}	
	/**
	 * Recupera i termini che costituiscono il glossario
	 * Il risultato e' messo in una hashmap in cui la chiave e' l'id del termine (kwid) e il valore e'
	 * un oggetto di tipo TermMisura con termine la forma e valore il numero di occorrenze del termine 
	 * nella collezione precedentemente elaborata. 
	 * @param conn connessione al DB
	 * @return glossario mappato in una hashmap
	 */
	public static LinkedHashMap retrieveFromGlossario(Connection conn, Vector lemmi, HashMap glossario){

		String sql = null;
		LinkedHashMap hm = null;
		FormFreq value = null;
		String key = null;
		//forme = new Vector();
		try {
			// Prepare a statement to insert a record
			//sql = "SELECT KWID, lemma, valore FROM "+ Config.dbTableGlossario;
			sql = "SELECT KWID, termine, valore FROM "+ Config.dbTableGlossario;
			logger.fine(sql);
			conn = ConnectionManager.getConnection();
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			//recupero il numero di righe per saepre quanto grade deve essere l'hashmap
			rs.last();
			int noOfRows = rs.getRow();
			rs.beforeFirst();
			hm = new LinkedHashMap( noOfRows * 1,25);
			//stringa,stringa,integer
			if (rs.next()) {
				do {
					//System.err.println(rs.getString(1)+" "+rs.getString(2)+" "+rs.getString(3));
					value = new FormFreq();
					value.setForm(rs.getString(2));
					key = rs.getString(1);
					value.setFreq(new Integer(rs.getInt(3)));
					//hm.put(key, value);
					hm.put(rs.getString(2), key);
					//aggiungo al vettore "forme" la forma lemmatizzata trovata
					lemmi.add(value.getForm());
					//costruisco una hashmap che mi rappresenta il glossario attuale e che dovra' essere aggiornata
					//e poi nuovamente memorizzata nel db
					//                        <id, freq>
					glossario.put(rs.getString(1), rs.getString((3)));
				} while (rs.next());
			}
			else {
				logger.severe("Glossario is empty! Impossible to update");
				return null;
			}	      
			/*
	        while (rs.next()) {
	        	System.err.println(rs.getString(1)+" "+rs.getString(2)+" "+rs.getString(3));
	        	value = new FormFreq();
	            value.setForm(rs.getString(2));
	            key = rs.getString(1);
	            value.setFreq(new Integer(rs.getInt(3)));
	            //hm.put(key, value);
	            hm.put(rs.getString(2), key);
	            //aggiungo al vettore "forme" la forma lemmatizzata trovata
	            forme.add(value.getForm());
	            //costruisco una hashmap che mi rappresenta il glossario attuale e che dovra' essere aggiornata
	            //e poi nuovamente memorizzata nel db
	            //                        <id, freq>
	            glossario.put(rs.getString(1), rs.getString((3)));
	        }
			 */
		} catch (SQLException e) {
			logger.severe("SELECT KWID, lemma, valore FROM "+ Config.dbTableGlossario);
			//e.printStackTrace();
			logger.severe("SQL: "+sql);
			logger.severe("SQL: "+e.getMessage());
		}finally{
			//conn.close();
		}
		// System.err.println(" retrieveFromGlossario() "+ hm==null?"null":"not null" );
		return hm;

	}

	/**
	 * Recupera, per ogni termine indicizzato, il numero di documenti nei quali un termine dato occorre
	 * Il risultato e' una hashmap dove la key e' l'id del termine (kwid) e il value e' il numero dei doc nei 
	 * quali il termine occorre.
	 * @param conn connessione al DB
	 * @return hashmap
	 */
	public static HashMap retrieveFromGlossarioFrequenza(Connection conn){

		String sql = null;
		HashMap hm = null;
		String kwid = null;
		String prev =null;
		int count = 0;
		try {
			// Strategia: Recupero dalla tabella glossariofrequenza tutti i kwid (ordinati un maniera crescente)
			// Contando i kwid uguali, ottengo il numero di documenti per i quali quel dato kwid occorre
			sql = "SELECT KWID FROM "+ Config.dbTableGlossarioFreq +" ORDER BY KWID asc";
			logger.finer(sql);
			conn = ConnectionManager.getConnection();
			Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
			ResultSet rs = stmt.executeQuery(sql);
			//recupero il numero di righe per saepre quanto grade deve essere l'hashmap
			rs.last();
			int noOfRows = rs.getRow();
			rs.beforeFirst();

			hm = new HashMap(noOfRows * 1,25);
			//stringa,stringa,integer
			if (rs.next()){
				prev = rs.getString(1);
//				System.err.println(rs.isFirst() + " "+rs.getString(1));
				rs.beforeFirst();
			}
			while (rs.next()) {
				//System.err.println(rs.getString(1) + " " +prev);
				kwid = rs.getString(1);
				if(kwid.equals(prev)){
					count++;
					continue;
				}
				//System.err.println(prev + " " +count);
				hm.put(prev, new Integer( count));
				prev = kwid;
				count = 1;
			}
			hm.put(prev, new Integer( count));
			//System.err.println(kwid + " " +count);

		} catch (SQLException e) {
			logger.severe("SELECT KWID FROM "+ Config.dbTableGlossarioFreq +" ORDER BY KWID asc");
			//e.printStackTrace();
			logger.severe("SQL: "+sql);
			logger.severe("SQL: "+e.getMessage());
		}finally{
			//conn.close();
		}
		return hm;
	}


	public static void insertInBTtable(String child, String parent/*, Connection conn*/){

		//INSERT INTO wbt_app_glossatio_bt VALUES (parent,child)
		try {
			// Prepare a statement to insert a record

			String sql = "INSERT INTO "+ Config.dbTableGlossarioBT
			+" VALUES("+parent+","+child+",0)";
			if(Config.dbDriverName.contains("mysql")){
				sql = "INSERT INTO "+ Config.dbTableGlossarioBT +" (bt_parent,bt_child,stop) "
				+" VALUES("+parent+","+child+",0)";
			}

			logger.finer(sql);
			Connection conn = ConnectionManager.getConnection();
			Statement stmt = conn.createStatement();
			stmt.executeUpdate(sql,Statement.NO_GENERATED_KEYS);

			child = null;
			parent = null;
		} catch (SQLException e) {
			logger.severe("In inserting row to "+Config.dbTableGlossarioBT);
			logger.severe("SQL: "+e.getMessage());
		}finally{
			//conn.close();
		}

	}
	public static void insertInNTtable(String child, String parent/*, Connection conn*/){

		//INSERT INTO wbt_app_glossatio_bt VALUES (child,parent)
		try {
			// Prepare a statement to insert a record
			String sql = "INSERT INTO "+ Config.dbTableGlossarioNT
			+" VALUES("+child+","+parent+",0)";
			if(Config.dbDriverName.contains("mysql")){
				sql = "INSERT INTO "+ Config.dbTableGlossarioNT +" (nt_parent,nt_child,stop) "
				+" VALUES("+child+","+parent+",0)";
			}

			logger.finer(sql);
			Connection conn = ConnectionManager.getConnection();
			Statement stmt = conn.createStatement();
			stmt.executeUpdate(sql,Statement.NO_GENERATED_KEYS);
			child = null;
			parent = null;
		} catch (SQLException e) {
			logger.severe("In inserting row to "+Config.dbTableGlossarioNT);
			logger.severe("SQL: "+e.getMessage());
		}finally{
			//conn.close();
		}

	}

	public static int insertIntoGlossarioTable(String termine, String lemma, int freq, String prep/*, Connection conn*/){
		int id = -1;
		String sql = null;
		try {
			//termine, forma, freq, conn
			if ( null != prep ) {
				sql = "INSERT INTO "+ Config.dbTableGlossario
				+" VALUES(\'"+lemma+"\',"+freq+","+"\'"+termine+"\',\'"+prep+"\')";

				if(Config.dbDriverName.contains("mysql")){
					sql = "INSERT INTO "+ Config.dbTableGlossario +" (termine,valore,lemma, stop) "
					+" VALUES(\'"+lemma+"\',"+freq+","+"\'"+termine+"\',\'"+prep+"\')";
				}
			} else {
				sql = "INSERT INTO "+ Config.dbTableGlossario
				+" VALUES(\'"+lemma+"\',"+freq+","+"\'"+termine+"\',\'NULL\')";

				if(Config.dbDriverName.contains("mysql")){
					sql = "INSERT INTO "+ Config.dbTableGlossario +" (termine,valore,lemma,stop) "
					+" VALUES(\'"+lemma+"\',"+freq+","+"\'"+termine+"\',\'NULL\')";
				}
			}

			logger.fine("Config.dbDriverName: "+Config.dbDriverName);
			logger.fine("SQL: " + sql);
			Connection conn = ConnectionManager.getConnection();
			Statement stmt = conn.createStatement();
			stmt.executeUpdate(sql,Statement.RETURN_GENERATED_KEYS);

			ResultSet rs = stmt.getGeneratedKeys();
			if(rs.next()){
				//faccio solo una insert => solo un ID
				id = rs.getInt(1);
			}
			//se sono qui, non ho inserito nulla!
			termine = null;
			lemma = null;
			return id;
		} catch (SQLException e) {
			logger.severe("In inserting row to "+Config.dbTableGlossario + ", SQL: " + sql);
			logger.severe("SQL: "+e.getMessage());
		}finally{
			//conn.close();
		}
		return id;
	}

	public static void updateDocumentTable(String ids,String idDoc,Connection conn){
		try {
			// Prepare a statement to insert a record
			String sql = "UPDATE "+ Config.dbTableDocument
			+" SET KW = \'"+ids+"\' WHERE DID = "+idDoc;
			logger.finer(sql);
			conn = ConnectionManager.getConnection();
			Statement stmt = conn.createStatement();
			stmt.executeUpdate(sql,Statement.NO_GENERATED_KEYS);
			ids = null;
			idDoc = null;
		} catch (SQLException e) {
			logger.severe("In inserting row to "+Config.dbTableDocument + ", ids: "+ids);
			logger.severe("SQL: "+e.getMessage());
		}finally{
			//conn.close();
		}
	}

	public static void updateGlossarioTable(String kwid,String freq,Connection conn){
		try {
			// Prepare a statement to insert a record
			String sql = "UPDATE "+ Config.dbTableGlossario
			+" SET valore = \'"+freq+"\' WHERE KWID = "+kwid;
			logger.finer(sql);
			conn = ConnectionManager.getConnection();
			Statement stmt = conn.createStatement();
			stmt.executeUpdate(sql,Statement.NO_GENERATED_KEYS);
		} catch (SQLException e) {
			logger.severe("In updating row to "+Config.dbTableGlossario);
			logger.severe("SQL: "+e.getMessage());
		}finally{
			//conn.close();
		}
	}

	public static void insertInVariatsTable(int id, String variante, Integer freq/*, Connection conn*/) {

		//INSERT INTO wbt_app_varianti VALUES (kwid, variante, frequenza variante);
		String sql = null;
		try {

			String frequenza = freq.toString();
			variante = Utils.reaccentWithEscapeQuote(variante);
			// Prepare a statement to insert a record
			sql = "INSERT INTO "+ Config.dbTableGlossarioVariants
			+" VALUES("+id+",\'"+variante+"\',"+frequenza+")";
			if (Config.dbDriverName.contains("mysql")) {
				sql = "INSERT INTO "+ Config.dbTableGlossarioVariants +" (kwid, forma_variante , freq) "
				+" VALUES("+id+",'"+variante+"',"+frequenza+")";
			}

			//System.err.println("sql: " + sql);
			Connection conn = ConnectionManager.getConnection();
			Statement stmt = conn.createStatement();
			stmt.executeUpdate(sql,Statement.NO_GENERATED_KEYS);
		} catch (SQLException e) {
			logger.severe("In inserting row to "+Config.dbTableGlossarioVariants + ", SQL : " + sql);
			logger.severe("SQL: "+e.getMessage());
		}finally{
			//conn.close();
		}

	}



	
	





	public static Statement getStatement () {
		Statement stmt = null;
		try {
			Connection conn = ConnectionManager.getConnection();
			stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return stmt;
	}


	public static String createSelectAllSqlString (String fromTable) {

		return "Select * From " +fromTable;
	}

	public static String getHeadXML () {

		return "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n<DBExport>\n" ;
	}
	
	public static String getEndXML () {

		return "</DBExport>" ;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		//	DBManager.exportTable();
	}

}
