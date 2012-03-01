package ca.site3.ssf.gesturerecognizer;

import java.util.Arrays;
import java.util.InputMismatchException;
import java.util.Scanner;

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
		this.leftGloveData  = null;
		this.rightGloveData = null;
		this.timePts = null;
	}
	
	public GestureInstance(GloveData[] leftGloveData, GloveData[] rightGloveData, double[] timePts) {
		super();
		this.leftGloveData  = leftGloveData;
		this.rightGloveData = rightGloveData;
		this.timePts = timePts;
		assert(this.isValid());
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
	public String toString() {
		return this.toDataString();
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
	}

}
