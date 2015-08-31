package pl.edu.pw.Sensors;

public class Mts400 {
	public int[] calculateTaos(int VisibleLight,int InfraredLight){
		final int CHORD_VAL[]={0,16,49,115,247,511,1039,2095};
		final int STEP_VAL[]={1,2,4,8,16,32,64,128};
		int chordVal,stepVal;
		int[] lightVal=new int[2];
		
		chordVal=(VisibleLight>>4) & 7;
		stepVal=VisibleLight & 15;
		lightVal[0]=CHORD_VAL [chordVal]+stepVal*STEP_VAL[chordVal];
		chordVal=(InfraredLight>>4)&7;
		stepVal=VisibleLight & 15;
		lightVal[1]=CHORD_VAL[chordVal]+stepVal*STEP_VAL[chordVal];
		return lightVal;
	}
	
	public double[] calculateSensirion(int Temperature,int Humidity){
		double [] converted = new double[2]; 
		
		converted[0]=-39.4+(0.01*(double)Temperature);
		converted[1]=(-2.0468+0.0367*(double)Humidity-0.0000015955*Math.pow((double)Humidity,(double )2))+(converted[0]-25)*(0.01+0.00008*(double)Humidity);
			
		return converted;
	}
}
