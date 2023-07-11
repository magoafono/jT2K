package ilc.t2k.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class TestConnection {

	/**
	 * @param args
	 * @throws SQLException 
	 */
	public static void main(String[] args) throws SQLException {
		// TODO Auto-generated method stub
		String url = "jdbc:jtds:sqlserver://portsimone/t2k";
		Connection con = null;
		try { 
			Class.forName("net.sourceforge.jtds.jdbc.Driver");
			/*List drivers = Collections.list(DriverManager.getDrivers());
			System.out.println("SQL: "+drivers);
		    for (int i=0; i<drivers.size(); i++) {
		        Driver driver = (Driver)drivers.get(i);
		    
		        // Get name of driver
		        String name = driver.getClass().getName();
		        System.out.print("SQL: "+name);
		        // Get version info
		        int majorVersion = driver.getMajorVersion();
		        int minorVersion = driver.getMinorVersion();
		        boolean isJdbcCompliant = driver.jdbcCompliant();
		        System.out.println(" jdbcCompliant?: "+(isJdbcCompliant?"yes":"no"));

		    }*/

			con = DriverManager.getConnection(url,"t2k","t2k");
			Statement st = con.createStatement();
			
			String ttt = "/tmp/tmp/file_20.txt.termfreq";
			System.err.println(ttt.replaceFirst(".+\\_(\\d+)\\..+","$1"));

			
			st.executeUpdate("INSERT INTO wbt_app_glossario VALUES('"+ttt+"',11)",
					Statement.RETURN_GENERATED_KEYS);
			ResultSet ss = st.getGeneratedKeys();
			
			while (ss.next()) {
				System.out.println("keys: "+ss.getInt(1));
			}
			/* ResultSet rs = st.executeQuery("SELECT * FROM wbt_app_glossario;");
			 // Get result set meta data
			  while (rs.next()) {
			  // Get the data from the row using the column index
			   String s = rs.getString(1);
			   System.out.print(s);
			   s = rs.getString(2);
			   System.out.print(" "+s);
			   s = rs.getString(3);
			   System.out.println(" "+s);
			   }
			   */
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if(con != null) con.close(); 
		}

	}

}
