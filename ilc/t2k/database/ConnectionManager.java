/**
 * $Id: ConnectionManager.java,v 1.8 2008/06/20 12:37:09 simone Exp $
 */
package ilc.t2k.database;

import ilc.t2k.Config;
import ilc.t2k.Utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Logger;

public class ConnectionManager {

	private static Logger logger = Logger.getLogger("jt2k");
	private static Connection conn = null;

	private ConnectionManager(){
	}

	public static Connection getConnection(){
		try {
			if( (conn == null) || (conn.isClosed() )){
				String 	url = Config.dbDriverName+"://"+Config.dbServerName+"/"+Config.dbName;

				try {
					if(Config.dbDriverName.contains("mysql")){
						Class.forName("com.mysql.jdbc.Driver");
					} else {
						Class.forName("net.sourceforge.jtds.jdbc.Driver");
					}
					logger.info("DB connection URL: "+url);
					conn = DriverManager.getConnection(url,Config.dbLogin,Config.dbPassword);
				} catch (SQLException e1) {
					// TODO Auto-generated catch block
					logger.severe(e1.getMessage());
					e1.printStackTrace();
					//Utils.sendEmail(Config.mailserver,Config.emaildmin,"Error on running jt2k:\n"+e1.getMessage(), Config.idRequest);
					return null;
					//e1.printStackTrace();
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return null;
				}
				//	}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			//connessione persa con il server
			logger.warning(e.getMessage());
			return null;
		} catch (Exception e) {
			// TODO: handle exception
			logger.warning(e.getMessage());
		}

		
		return conn;
	}
	
	
	public static void resetConnection(){
		
		if (conn != null) {
			try {
				conn.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		conn = null;
		
	}
	
	public static Connection getoldConnection(){
		try {
			//if( (conn == null) || (conn.isClosed() )){
			String 	url = Config.dbDriverName+"://"+Config.dbServerName+"/"+Config.dbName;
			/*if ( (conn != null) &&	(conn.isClosed()) )  {
				logger.warning("Connection is closed! Reopen it...");
			}*/
			if ( null != conn ) {
				logger.fine("Closing connection and recreating new one");
				conn.close();
				conn = null;
			}

			try {
				if(Config.dbDriverName.contains("mysql")){
					Class.forName("com.mysql.jdbc.Driver");
				} else {
					Class.forName("net.sourceforge.jtds.jdbc.Driver");
				}
				logger.finer("DB connection URL: "+url);
				conn = DriverManager.getConnection(url,Config.dbLogin,Config.dbPassword);
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				logger.severe(e1.getMessage());
				Utils.sendEmail(Config.mailserver,Config.emaildmin,"Error on running jt2k:\n"+e1.getMessage(), Config.idRequest);
				return null;
				//e1.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
			//	}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			//connessione persa con il server
			logger.warning(e.getMessage());
			return null;
		} catch (Exception e) {
			// TODO: handle exception
			logger.warning(e.getMessage());
		}
		
		return conn;
	}

	/*	public Connection getConnection(){

		String 	url = Config.dbDriverName+"://"+Config.dbServerName+"/"+Config.dbName;
		Connection conn = null;
		try {
			Class.forName("net.sourceforge.jtds.jdbc.Driver");
			logger.info("DB connection URL: "+url);
			conn = DriverManager.getConnection(url,Config.dbLogin,Config.dbPassword);
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			logger.log(Level.SEVERE,e1.getMessage());
			Utils.sendEmail(Config.mailserver,Config.emaildmin,"Error on running jt2k:\n"+e1.getMessage(), Config.idRequest);
			//e1.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return conn;

	}
	 */


	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
