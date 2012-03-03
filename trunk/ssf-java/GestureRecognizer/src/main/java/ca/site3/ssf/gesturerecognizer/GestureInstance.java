package ca.site3.ssf.gesturerecognizer;

import java.util.Arrays;
import java.util.InputMismatchException;
import java.util.Scanner;

import net.sf.javaml.distance.fastdtw.timeseries.TimeSeriesPoint;

import org.apache.commons.math.geometry.Vector3D;

/**
 * The GestureInstance is a fully recorded 'instance' of a single gesture for
 * the Super Street Fire game. A gesture can be a one or two handed action/movement
 * of a player or ringmaster. The record is kept as two arrays (one for each hand)
 * of GloveData objects, which act as the discrete data points that define the gesture
 * over some time period (which is also recorded as an array of double values held by
 * the instance). 
 * 
 * If a given glove data array is null within this object then the gesture is considered
 * one-handed (NOTE: The gesture is not valid if both arrays are null).
 * 
 * Similar to the GloveData the GestureInstance and be written to and read from string using
 * its toDataString and fromDataString methods.
 * 
 * @author Callum
 *
 */
public class GestureInstance {

	static final private String LEFT_GLOVE_DATA_STR  = "LEFT_GLOVE_DATA";
	static final private String RIGHT_GLOVE_DATA_STR = "RIGHT_GLOVE_DATA";
	static final private String TIME_PTS_DATA_STR    = "TIME_PTS";
	
	private GloveData[] leftGloveData;
	private GloveData[] rightGloveData;
	private double[] timePts;
	
	public GestureInstance() {
		super();
		this.leftGloveData  = new GloveData[1];
		this.leftGloveData[0] = new GloveData();
		this.rightGloveData = null;
		this.timePts = new double[1];
	}
	
	public GestureInstance(GloveData[] leftGloveData, GloveData[] rightGloveData, double[] timePts) {
		super();
		this.leftGloveData  = leftGloveData;
		this.rightGloveData = rightGloveData;
		this.timePts = timePts;
		assert(this.isValid());
	}
	

	public int getNumDataPts() {
		assert(this.timePts != null);
		return this.timePts.length;
	}
	public GloveData getLeftGloveDataAt(int index) {
		assert(this.hasLeftGloveData());
		assert(index >= 0 && index < this.leftGloveData.length);
		return this.leftGloveData[index];
	}
	public GloveData getRightGloveDataAt(int index) {
		assert(this.hasRightGloveData());
		assert(index >= 0 && index < this.rightGloveData.length);
		return this.rightGloveData[index];
	}
	public double getTimeAt(int index) {
		assert(this.timePts != null);
		assert(index >= 0 && index < this.timePts.length);
		return this.timePts[index];
	}
	
	public boolean hasLeftGloveData() {
		return this.leftGloveData != null;
	}
	public boolean hasRightGloveData() {
		return this.rightGloveData != null;
	}
	public boolean isValid() {
		boolean isValid = (this.hasLeftGloveData() || this.hasRightGloveData()) && this.timePts != null;
		if (this.hasLeftGloveData() && this.hasRightGloveData()) {
			isValid &= (this.leftGloveData.length == this.rightGloveData.length);
			isValid &= (this.leftGloveData.length == this.timePts.length);
			return isValid;
		}
		
		if (this.hasLeftGloveData()) {
			isValid &= (this.leftGloveData.length == this.timePts.length);
		}
		else if (this.hasRightGloveData()) {
			isValid &= (this.rightGloveData.length == this.timePts.length);
		}
		
		return isValid;
	}
	
	public int getTrainingDataObservationWidth() {
		assert(this.isValid());
		if (this.hasLeftGloveData() && this.hasRightGloveData()) {
			return 6;
		}
		return 3;
	}
	
	/**
	 * Obtains the training sequence for this gesture instance. This will provide
	 * and in-order sequence of tuples that can be fed to a classification/machine learning
	 * algorithm for identifying gestures.
	 * @return A sequential array of tuples, each tuple will be the same size.
	 */
	public double[][] getTrainingSequence() {
		
		if (this.hasLeftGloveData()) {
			if (this.hasRightGloveData()) {
				double[][] result = new double[this.getNumDataPts()][6];
				
				// Two handed gesture, each tuple is size 6 (for two glove's worth of accelerometer data)
				for (int i = 0; i < this.getNumDataPts(); i++) {
					
					// Right now we only consider accelerometer data...
					GloveData leftGloveData  = this.getLeftGloveDataAt(i);
					GloveData rightGloveData = this.getRightGloveDataAt(i);
					assert(leftGloveData != null && rightGloveData != null);
					
					Vector3D leftGloveAccelData  = leftGloveData.getAccelData();
					Vector3D rightGloveAccelData = rightGloveData.getAccelData();
					assert(leftGloveAccelData != null && rightGloveAccelData != null);
					
					result[i][0] = leftGloveAccelData.getX();
					result[i][1] = leftGloveAccelData.getY();
					result[i][2] = leftGloveAccelData.getZ();
					result[i][3] = rightGloveAccelData.getX();
					result[i][4] = rightGloveAccelData.getY();
					result[i][5] = rightGloveAccelData.getZ();
				}
				return result;
			}
			else {
				// Left handed gesture, each tuple is size 3 (for one glove's worth of accelerometer data)
				double[][] result = new double[this.getNumDataPts()][3];
				
				// Two handed gesture, each tuple is size 6 (for two glove's worth of accelerometer data)
				for (int i = 0; i < this.getNumDataPts(); i++) {
					
					// Right now we only consider accelerometer data...
					GloveData leftGloveData  = this.getLeftGloveDataAt(i);
					assert(leftGloveData != null);
					
					Vector3D leftGloveAccelData  = leftGloveData.getAccelData();
					assert(leftGloveAccelData != null);
					
					result[i][0] = leftGloveAccelData.getX();
					result[i][1] = leftGloveAccelData.getY();
					result[i][2] = leftGloveAccelData.getZ();
				}
				return result;
			}
		}
		else {
			// Right handed gesture, each tuple is size 3 (for one glove's worth of accelerometer data)
			double[][] result = new double[this.getNumDataPts()][3];
			
			// Two handed gesture, each tuple is size 6 (for two glove's worth of accelerometer data)
			for (int i = 0; i < this.getNumDataPts(); i++) {
				
				// Right now we only consider accelerometer data...
				GloveData rightGloveData = this.getRightGloveDataAt(i);
				assert(rightGloveData != null);
				
				Vector3D rightGloveAccelData = rightGloveData.getAccelData();
				assert(rightGloveAccelData != null);
				
				result[i][0] = rightGloveAccelData.getX();
				result[i][1] = rightGloveAccelData.getY();
				result[i][2] = rightGloveAccelData.getZ();
			}
			return result;
		}
	}
	
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (other == null) {
			return false;
		}
		if (!(other instanceof GestureInstance)) {
			return false;
		}
		
		GestureInstance otherGestureInstance = (GestureInstance)other;
		if (!Arrays.equals(this.leftGloveData, otherGestureInstance.leftGloveData)) {
			return false;
		}
		if (!Arrays.equals(this.rightGloveData, otherGestureInstance.rightGloveData)) {
			return false;
		}
		if (!Arrays.equals(this.timePts, otherGestureInstance.timePts)) {
			return false;
		}
		return true;
	}
	
	
	public boolean fromDataString(String str) {
		// Start by reading what gloves are in the instance data of the gesture
		Scanner scanner = new Scanner(str);
		
		String header;
		int amtOfData;
		
		try {
			header = scanner.next();
			amtOfData = scanner.nextInt();
		}
		catch (InputMismatchException ex) {
			return false;
		}			
			
		if (amtOfData <= 0) {
			return false;
		}
		
		// Initialize the glove data...
		this.leftGloveData  = null;
		this.rightGloveData = null;
		int count = 0;
		if (header.contains("L")) {
			this.leftGloveData = new GloveData[amtOfData];
			count++;
		}
		if (header.contains("R")) {
			this.rightGloveData = new GloveData[amtOfData];
			count++;
		}
		
		this.timePts = new double[amtOfData];
		count++;
		
		for (int i = 0; i < count; i++) {
			if (!this.gloveAndTimeHeaderAndDataFromScanner(scanner)) {
				return false;
			}
		}

		
		return true;
	}
	
	public String toDataString() {
		// Start with a brief header indicating what data is contained within this gesture
		String result = new String();
		int amtOfData = 0;
		if (this.hasLeftGloveData()) {
			result += "L";
			amtOfData = this.leftGloveData.length;
			
			if (this.hasRightGloveData()) {
				result += "R";
			}
		}
		else {
			result += "R";
			amtOfData = this.rightGloveData.length;
		}
		
		result += " " + amtOfData + "\n";
		result += this.gloveDataToString(LEFT_GLOVE_DATA_STR,  this.leftGloveData);
		result += this.gloveDataToString(RIGHT_GLOVE_DATA_STR, this.rightGloveData);
		result += this.timeDataToString(TIME_PTS_DATA_STR);
		
		return result;
	}
	
	public String toString() {
		return this.toDataString();
	}
	
	
	private String gloveDataToString(String gloveTitle, GloveData[] data) {
		if (data == null) {
			return "";
		}
		
		String result = gloveTitle + "\n";
		for (int i = 0; i < data.length; i++) {
			result += data[i].toDataString() + "\n";
		}
		return result;
	}
	
	private String timeDataToString(String timeTitle) {
		if (this.timePts == null) {
			return "";
		}
		String result = timeTitle + "\n";
		for (int i = 0; i < this.timePts.length; i++) {
			result += this.timePts[i] + "\n";
		}
		return result;
	}
	
	
	private boolean gloveAndTimeHeaderAndDataFromScanner(Scanner scanner) {
		try {
			String headerStr;
			headerStr = scanner.next();
			scanner.nextLine();

			if (headerStr.equals(LEFT_GLOVE_DATA_STR)) {
				if (!this.hasLeftGloveData()) {
					return false;
				}
				return this.gloveDataFromString(scanner, this.leftGloveData);
			}
			else if (headerStr.equals(RIGHT_GLOVE_DATA_STR)) {
				if (!this.hasRightGloveData()) {
					return false;
				}
				return this.gloveDataFromString(scanner, this.rightGloveData);
			}
			else if (headerStr.equals(TIME_PTS_DATA_STR)) {
				if (this.timePts == null) {
					return false;
				}
				return this.timeDataFromString(scanner, this.timePts);
			}
			
		}
		catch (InputMismatchException ex) {
			return false;
		}
		
		return false;
	}
	
	private boolean gloveDataFromString(Scanner scanner, GloveData[] data) {
		for (int i = 0; i < data.length; i++) {
			String nextLine = scanner.nextLine();
			data[i] = new GloveData();
			if (!data[i].fromDataString(nextLine)) {
				return false;
			}
		}
		return true;
	}
	private boolean timeDataFromString(Scanner scanner, double[] data) {
		try {
			for (int i = 0; i < data.length; i++) {
				String nextLine = scanner.nextLine();
				data[i] = Double.parseDouble(nextLine);
			}
		}
		catch (NumberFormatException ex) {
			return false;
		}
		return true;
	}

	public static void main(String[] args) {
		GloveData[] leftGloveData = new GloveData[10];
		GloveData[] rightGloveData = new GloveData[10];
		double[] timeData = new double[10];
		
		for (int i = 0; i < 10; i++) {
			leftGloveData[i] = new GloveData(
					Math.random(), Math.random(), Math.random(),
					Math.random(), Math.random(), Math.random(),
					Math.random(), Math.random(), Math.random());
			rightGloveData[i] = new GloveData(
					Math.random(), Math.random(), Math.random(),
					Math.random(), Math.random(), Math.random(),
					Math.random(), Math.random(), Math.random());
			timeData[i] = i;
		}
		
		GestureInstance toStrInstance = new GestureInstance(leftGloveData, rightGloveData, timeData);
		String dataStr = toStrInstance.toDataString();
		System.out.println(dataStr);
		
		GestureInstance fromStrInstance = new GestureInstance();
		fromStrInstance.fromDataString(dataStr);
		
		System.out.println(fromStrInstance.equals(toStrInstance));
		
		System.out.println(JavaMLConverter.gestureInstanceToTimeSeries(fromStrInstance, 10).toString());
		System.out.println();
		
		double[][] trainingSeq = fromStrInstance.getTrainingSequence();
		for (int i = 0; i < trainingSeq.length; i++) {
			for (int j = 0; j < trainingSeq[i].length; j++) {
				System.out.print(trainingSeq[i][j] + " ");
			}
			System.out.println();
		}
		
	}

}
