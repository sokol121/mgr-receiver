package pl.edu.pw.dbWriters;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import pl.edu.pw.MoteMsg;
import pl.edu.pw.Sensors.Mts400;

public class MoteMsgDbWriter {
	private static String connectionSting = "jdbc:mysql://localhost:3306/temps";
	private static String databaseUserName = "root";
	private static String databaseUserPassword = "nzozpoz.";
	
	public int writeReadingToDb(MoteMsg message){
		int[] taosCalcData = null;
		double[] sensirionCalcData=null;
		Connection c = null;
		Mts400 mts400 = new Mts400();
		
		Integer moteId = message.get_nodeId();
		

		
		try {
			//check if mote exists in database, if not crate inserts mote
			if(moteExistInDB(moteId) < 1)
				insertMoteIntoDb(moteId);
			c = DriverManager.getConnection(connectionSting, databaseUserName, databaseUserPassword);
			//get current timestamp	
			long time = System.currentTimeMillis();
			
			writeReadingToDb(message.get_nodeId(), "ADXL202JE-X", (double) message.get_AccelX_data(),c, time);
			writeReadingToDb(message.get_nodeId(), "ADXL202JE-Y", (double) message.get_AccelY_data(),c, time);
			writeReadingToDb(message.get_nodeId(), "Intersema-temp",(double)message.getElement_Intersema_data(0)/10,c, time);
			writeReadingToDb(message.get_nodeId(), "Intersema-press",(double)message.getElement_Intersema_data(1)/10,c, time);
			sensirionCalcData=mts400.calculateSensirion(message.get_tempVal(),message.get_humVal());
			writeReadingToDb(message.get_nodeId(), "SHT11-temp", sensirionCalcData[0],c, time);
			writeReadingToDb(message.get_nodeId(), "SHT11-hum", sensirionCalcData[1],c, time);
			taosCalcData=mts400.calculateTaos(message.get_VisLight_data(),message.get_InfLight_data());
			writeReadingToDb(message.get_nodeId(), "Taos ch1", (double) taosCalcData[0],c, time);
			writeReadingToDb(message.get_nodeId(), "Taos ch2", (double) taosCalcData[1],c, time);
			
			c.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}finally {
			try {
				c.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return 0;
	}
	private void insertMoteIntoDb(Integer moteId) {
		Connection c = null;
		PreparedStatement stmt = null;
	    try {
			c = DriverManager.getConnection(connectionSting, databaseUserName, databaseUserPassword);
			String sql = "insert into mote (id, sensorboard_id, name) values (?, ?, ?);" ;
			stmt = c.prepareStatement(sql);
			stmt.setInt(1, moteId);
			stmt.setString(2, "mts400");
			stmt.setString(3, "Mote " + moteId);
			stmt.execute();
			
			stmt.close();
	      
	    } catch ( Exception e ) {
	      System.err.println( e.getClass().getName() + ": " + e.getMessage() );
	    }finally{
	    	if (c != null)
				try {
					c.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
	    }
	}
	private int moteExistInDB(Integer moteId){
		Connection c = null;
		PreparedStatement stmt = null;
		int numberOfMotes = -1;
		try {
			Class.forName("com.mysql.jdbc.Driver");
			c = DriverManager.getConnection("jdbc:mysql://localhost:3306/temps","root", "nzozpoz.");
			c.setAutoCommit(false);
			 
			String sql = "select count(*) as number from mote where id = ?;";
			  
			stmt = c.prepareStatement(sql);
			stmt.setInt(1, moteId);
			ResultSet rs = stmt.executeQuery();
			
			while ( rs.next() ) {
				numberOfMotes = rs.getInt("number");
			}
			  
		} catch (Exception e ) {
			System.err.println( e.getClass().getName() + ": " + e.getMessage() );
			return -1;
		}
		finally{
			try {
				if(stmt != null)
					stmt.close();
				if(c != null)
					c.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
	    }
	    
	    return numberOfMotes;
	}
	public int writeReadingToDb(Integer moteId, String sensorId, Double value, Connection c , Long timeUTC){
		PreparedStatement stmt = null;
	    try {
	      
	    	String sql = "insert into reading (mote_id, sensor_id, value, timestamp) "
	      				+ "values (?, ?, ?,?); " ;
	    	stmt = c.prepareStatement(sql);
	    	stmt.setInt(1, moteId);
	    	stmt.setString(2, sensorId);
	    	stmt.setDouble(3, value);
	    	stmt.setLong(4, timeUTC);
	    	stmt.execute();
	    } catch ( Exception e ) {
	      System.err.println( e.getClass().getName() + ": " + e.getMessage() );
	      return -1;
	    }
	    return 0;
	  }
}
