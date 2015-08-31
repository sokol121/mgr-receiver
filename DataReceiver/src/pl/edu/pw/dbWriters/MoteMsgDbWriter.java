package pl.edu.pw.dbWriters;

import static java.lang.System.out;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import pl.edu.pw.MoteMsg;
import pl.edu.pw.Sensors.Mts400;

public class MoteMsgDbWriter {
	public int writeReadingToDb(MoteMsg message){
		Mts400 mts400 = new Mts400();
		Integer moteId = message.get_nodeId();
		int[] taosCalcData = null;
		double[] sensirionCalcData=null;
		if(moteExistInDB(moteId) < 1)
			insertMoteIntoDb(moteId);
		writeReadingToDb(message.get_nodeId(), "ADXL202JE-X", (double) message.get_AccelX_data());
		writeReadingToDb(message.get_nodeId(), "ADXL202JE-Y", (double) message.get_AccelY_data());
		writeReadingToDb(message.get_nodeId(), "Intersema-temp",(double)message.getElement_Intersema_data(0)/10);
		writeReadingToDb(message.get_nodeId(), "Intersema-press",(double)message.getElement_Intersema_data(1)/10);
		sensirionCalcData=mts400.calculateSensirion(message.get_tempVal(),message.get_humVal());
		writeReadingToDb(message.get_nodeId(), "SHT11-temp", sensirionCalcData[0]);
		writeReadingToDb(message.get_nodeId(), "SHT11-hum", sensirionCalcData[1]);
		taosCalcData=mts400.calculateTaos(message.get_VisLight_data(),message.get_InfLight_data());
		writeReadingToDb(message.get_nodeId(), "Taos ch1", (double) taosCalcData[0]);
		writeReadingToDb(message.get_nodeId(), "Taos ch2", (double) taosCalcData[1]);
		return 0;
	}
	private void insertMoteIntoDb(Integer moteId) {
		Connection c = null;
		PreparedStatement stmt = null;
	    try {
	    	Class.forName("com.mysql.jdbc.Driver");
	      c = DriverManager.getConnection("jdbc:mysql://localhost:3306/temps","root", "nzozpoz.");
	      
	      String sql = "insert into mote (id, sensorboard_id, name) "
	      				+ "values (?, ?, ?); " ;
	      stmt = c.prepareStatement(sql);
	      stmt.setInt(1, moteId);
	      stmt.setString(2, "mts400");
	      stmt.setString(3, "Mote " + moteId);
	      stmt.execute();
	
	      stmt.close();
	      c.close();
	    } catch ( Exception e ) {
	      System.err.println( e.getClass().getName() + ": " + e.getMessage() );
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
	public int writeReadingToDb(Integer moteId, String sensorId, Double value){
		Connection c = null;
		PreparedStatement stmt = null;
	    try {
	    	Class.forName("com.mysql.jdbc.Driver");
	      c = DriverManager.getConnection("jdbc:mysql://localhost:3306/temps","root", "nzozpoz.");
	      
	      String sql = "insert into reading (mote_id, sensor_id, value) "
	      				+ "values (?, ?, ?); " ;
	      stmt = c.prepareStatement(sql);
	      stmt.setInt(1, moteId);
	      stmt.setString(2, sensorId);
	      stmt.setDouble(3, value);
	      stmt.execute();
	
	      stmt.close();
	      c.close();
	    } catch ( Exception e ) {
	      System.err.println( e.getClass().getName() + ": " + e.getMessage() );
	      return -1;
	    }
	    return 0;
	  }

}
