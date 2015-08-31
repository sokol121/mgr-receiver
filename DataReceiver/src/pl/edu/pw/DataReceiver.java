package pl.edu.pw;

import static java.lang.System.out;
import net.tinyos.message.*;
import net.tinyos.util.*;
import pl.edu.pw.Sensors.Mts400;
import pl.edu.pw.dbWriters.MoteMsgDbWriter;
import net.tinyos.packet.*;

class DataReceiver implements MessageListener{
	private PhoenixSource phoenix;
	private MoteIF mif;
	private static DataReceiver hy;
	private Mts400 mts400 = new Mts400();
		
	public DataReceiver(final String source){
		phoenix=BuildSource.makePhoenix(source, PrintStreamMessenger.err);
		mif = new MoteIF(phoenix);
		mif.registerListener(new MoteMsg(),this);
	}
	
	public void messageReceived(int dest_addr,Message msg){
		if(msg instanceof MoteMsg){
			MoteMsg results = (MoteMsg)msg;
			MoteMsgDbWriter writer = new MoteMsgDbWriter();
			writer.writeReadingToDb(results);
			int[] taosCalcData = null;
			double[] sensirionCalcData=null;
			results.get_nodeId();
			out.println("The measured results are ");
			out.println();
			out.println("Accelerometer X axis:   "+results.get_AccelX_data());
			out.println("Accelerometer Y axis:   "+results.get_AccelY_data());
			out.println("Intersema temperature:  "+(double)results.getElement_Intersema_data(0)/10);
			out.println("Intersema pressure:     "+(double)results.getElement_Intersema_data(1)/10);
			sensirionCalcData= mts400.calculateSensirion(results.get_tempVal(),results.get_humVal());
			out.printf("Sensirion temperature:  %.2f\n",sensirionCalcData[0]);
			out.printf("Sensirion humidity:     %.2f\n",sensirionCalcData[1]);
			taosCalcData=mts400.calculateTaos(results.get_VisLight_data(),results.get_InfLight_data());
			out.println("Taos visible light:     "+taosCalcData[0]);
			out.println("Taos infrared light:    "+taosCalcData[1]);
		}
	}
	
	public static void main (String[] args) {
		if ( args.length == 2 && args[0].equals("-comm") ) {
			out.println(args[1]);
			hy = new DataReceiver(args[1]);
		} else {
			System.err.println("usage: java DataReceiver [-comm <source>]");
			System.exit(1);
		}
		
	}

}