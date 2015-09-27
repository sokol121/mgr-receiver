package pl.edu.pw;

import static java.lang.System.out;

import java.io.IOException;

import net.tinyos.message.Message;
import net.tinyos.message.MessageListener;
import net.tinyos.message.MoteIF;
import net.tinyos.packet.BuildSource;
import net.tinyos.packet.PhoenixSource;
import net.tinyos.util.PrintStreamMessenger;
import pl.edu.pw.Sensors.Mts400;
import pl.edu.pw.dbWriters.MoteMsgDbWriter;

class DataReceiver implements MessageListener{
	private PhoenixSource phoenix;
	private MoteIF mif;
	private static DataReceiver hy;
	private Mts400 mts400 = new Mts400();
	MoteMsgDbWriter writer = new MoteMsgDbWriter();
	Integer i = 0;
		
	public DataReceiver(final String source){
		phoenix=BuildSource.makePhoenix(source, PrintStreamMessenger.err);
		mif = new MoteIF(phoenix);
		mif.registerListener(new MoteMsg(),this);
		System.out.println("done");
	}
	
	public void messageReceived(int dest_addr,Message msg){
		if(msg instanceof MoteMsg){
			MoteMsg results = (MoteMsg)msg;
			writer.writeReadingToDb(results);
//			printValues(results);
		}
	}
	
	private void printValues(MoteMsg results) {
		int[] taosCalcData = null;
		double[] sensirionCalcData=null;
		results.get_nodeId();
		System.out.println("The measured results are " + i++);
		
		System.out.println("Mote id:   "+results.get_nodeId());
		System.out.println("Accelerometer X axis:   "+results.get_AccelX_data());
		System.out.println("Accelerometer Y axis:   "+results.get_AccelY_data());
		System.out.println("Intersema temperature:  "+(double)results.getElement_Intersema_data(0)/10);
		System.out.println("Intersema pressure:     "+(double)results.getElement_Intersema_data(1)/10);
		sensirionCalcData= mts400.calculateSensirion(results.get_tempVal(),results.get_humVal());
		System.out.printf("Sensirion temperature:  %.2f\n",sensirionCalcData[0]);
		System.out.printf("Sensirion humidity:     %.2f\n",sensirionCalcData[1]);
		taosCalcData=mts400.calculateTaos(results.get_VisLight_data(),results.get_InfLight_data());
		System.out.println("Taos visible light:     "+taosCalcData[0]);
		System.out.println("Taos infrared light:    "+taosCalcData[1]);
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