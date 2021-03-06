package ca.site3.ssf.gesturerecognizer;

import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

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
 * Similar to the GloveData, the GestureInstance can be written to and read from string using
 * its toDataString and fromDataString methods.
 * 
 * @author Callum
 *
 */
public class GestureInstance {
	static final public int NUM_COORDS_PER_GLOVE = 3;
	
	static final private String LEFT_GLOVE_DATA_STR  = "LEFT_GLOVE_DATA";
	static final private String RIGHT_GLOVE_DATA_STR = "RIGHT_GLOVE_DATA";
	static final private String TIME_PTS_DATA_STR    = "TIME_PTS";
	
	private List<GloveData> leftGloveData  = null;
	private List<GloveData> rightGloveData = null;
	private List<Double>    timePts        = null;

	public GestureInstance() {
		super();
		this.leftGloveData  = new ArrayList<GloveData>();
		this.leftGloveData.add(new GloveData());
		this.rightGloveData = new ArrayList<GloveData>();
		this.timePts = new ArrayList<Double>();
		this.timePts.add(new Double(0.0));
	}

	public GestureInstance(List<GloveData> leftGloveData, List<GloveData> rightGloveData, List<Double> timePts) {
		super();
		this.leftGloveData  = leftGloveData;
		this.rightGloveData = rightGloveData;
		this.timePts = timePts;
		assert(this.isValid());
	}
	
	public int getNumDataPts() {
		assert(this.timePts != null);
		return this.timePts.size();
	}
	
	public GloveData getLeftGloveDataAt(int index) {
		assert(index >= 0 && index < this.leftGloveData.size());
		return this.leftGloveData.get(index);
	}
	
	public GloveData getRightGloveDataAt(int index) {
		assert(index >= 0 && index < this.rightGloveData.size());
		return this.rightGloveData.get(index);
	}
	
	public double getTimeAt(int index) {
		assert(index >= 0 && index < this.timePts.size());
		return this.timePts.get(index);
	}
	
	public double getMaxTimeDiff() {
		if (this.timePts.isEmpty()) {
			return 0.0;
		}
		return this.timePts.get(this.timePts.size()-1) - this.timePts.get(0);
	}
	
	/**
	 * Gets the maximum absolute acceleration for this entire gesture instance
	 * (both gloves if possible). This is meant to be an indicator for how 'fierce'
	 * this gesture is.
	 * @return The maximum absolute acceleration.
	 */
	public double getMaxAbsAccel() {
		double maxAbsAccel = 0.0;

		for (GloveData data : this.leftGloveData) {
			Vector3D accelData = data.getAccelData();
			maxAbsAccel = Math.max(maxAbsAccel, Math.max(Math.abs(accelData.getX()),
					Math.max(Math.abs(accelData.getY()), Math.abs(accelData.getZ()))));
		}

		for (GloveData data : this.rightGloveData) {
			Vector3D accelData = data.getAccelData();
			maxAbsAccel = Math.max(maxAbsAccel, Math.max(Math.abs(accelData.getX()),
					Math.max(Math.abs(accelData.getY()), Math.abs(accelData.getZ()))));
		}

		return maxAbsAccel;
	}
	
	/**
	 * Gets the maximum acceleration for this entire gesture instance
	 * (both gloves if possible). 
	 * @return The maximum acceleration.
	 */
	public double getMaxAccel() {
		double maxAbsAccel = 0.0;

		for (GloveData data : this.leftGloveData) {
			Vector3D accelData = data.getAccelData();
			maxAbsAccel = Math.max(maxAbsAccel, Math.max(accelData.getX(),
					Math.max(accelData.getY(), accelData.getZ())));
		}

		for (GloveData data : this.rightGloveData) {
			Vector3D accelData = data.getAccelData();
			maxAbsAccel = Math.max(maxAbsAccel, Math.max(accelData.getX(),
					Math.max(accelData.getY(), accelData.getZ())));
		}

		return maxAbsAccel;
	}
	
	/**
	 * Gets the minimum absolute acceleration for this entire gesture instance
	 * (both gloves if possible).
	 * @return The minimum absolute acceleration.
	 */
	public double getMinAbsAccel() {
		double minAbsAccel = 0.0;

		for (GloveData data : this.leftGloveData) {
			Vector3D accelData = data.getAccelData();
			minAbsAccel = Math.min(minAbsAccel, Math.min(Math.abs(accelData.getX()),
					Math.min(Math.abs(accelData.getY()), Math.abs(accelData.getZ()))));
		}

		for (GloveData data : this.rightGloveData) {
			Vector3D accelData = data.getAccelData();
			minAbsAccel = Math.min(minAbsAccel, Math.min(Math.abs(accelData.getX()),
					Math.min(Math.abs(accelData.getY()), Math.abs(accelData.getZ()))));
		}

		return minAbsAccel;
	}
	
	/**
	 * Gets the minimum acceleration for this entire gesture instance
	 * (both gloves if possible). This may also be an indicator of the 'fireceness'
	 * of a gesture.
	 * @return The minimum acceleration.
	 */
	public double getMinAccel() {
		double minAbsAccel = 0.0;

		for (GloveData data : this.leftGloveData) {
			Vector3D accelData = data.getAccelData();
			minAbsAccel = Math.min(minAbsAccel, Math.min(accelData.getX(),
					Math.min(accelData.getY(), accelData.getZ())));
		}

		for (GloveData data : this.rightGloveData) {
			Vector3D accelData = data.getAccelData();
			minAbsAccel = Math.min(minAbsAccel, Math.min(accelData.getX(),
					Math.min(accelData.getY(), accelData.getZ())));
		}

		return minAbsAccel;
	}
	
	/**
	 * Get the difference between the highest and lowest acceleration values.
	 * This is the best determining factor for how 'extreme/fierce' a gesture is.
	 * @return The positive difference between the highest and lowest acceleration values.
	 */
	public double getMinMaxAccelDiff() {
		return this.getMaxAccel() - this.getMinAccel();
	}
	
	public double getTotalFierceness() {
		return this.getMinMaxAccelDiff();
	}
	
	public boolean hasLeftGloveData() {
		return !this.leftGloveData.isEmpty();
	}
	
	public boolean hasRightGloveData() {
		return !this.rightGloveData.isEmpty();
	}
	
	public boolean isValid() {
		boolean isValid = (this.hasLeftGloveData() || this.hasRightGloveData()) && !this.timePts.isEmpty();
		if (this.hasLeftGloveData() && this.hasRightGloveData()) {
			isValid &= (this.leftGloveData.size() == this.rightGloveData.size());
			isValid &= (this.leftGloveData.size() == this.timePts.size());
			return isValid;
		}
		
		if (this.hasLeftGloveData()) {
			isValid &= (this.leftGloveData.size() == this.timePts.size());
		}
		else if (this.hasRightGloveData()) {
			isValid &= (this.rightGloveData.size() == this.timePts.size());
		}
		
		return isValid;
	}
	
	int getTrainingDataObservationWidth() {
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
	double[][] getTrainingSequence() {
		
		if (this.hasLeftGloveData()) {
			if (this.hasRightGloveData()) {
				double[][] result = new double[this.getNumDataPts()][2*NUM_COORDS_PER_GLOVE];
				
				// Two handed gesture, each tuple is size 2*NUM_COORDS_PER_GLOVE
				// (for two glove's worth of accelerometer data)
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
					/*
					Vector3D leftGloveGyroData  = leftGloveData.getGyroData();
					Vector3D rightGloveGyroData = rightGloveData.getGyroData();
					assert(leftGloveGyroData != null && rightGloveGyroData != null);
					
					result[i][6] = leftGloveGyroData.getX();
					result[i][7] = leftGloveGyroData.getY();
					result[i][8] = leftGloveGyroData.getZ();
					result[i][9] = rightGloveGyroData.getX();
					result[i][10] = rightGloveGyroData.getY();
					result[i][11] = rightGloveGyroData.getZ();
					*/
				}
				return result;
			}
			else {
				// Left handed gesture, each tuple is size 3 (for one glove's worth of accelerometer data)
				double[][] result = new double[this.getNumDataPts()][NUM_COORDS_PER_GLOVE];
				
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
					/*
					Vector3D leftGloveGyroData  = leftGloveData.getGyroData();
					assert(leftGloveGyroData != null);
					
					result[i][3] = leftGloveGyroData.getX();
					result[i][4] = leftGloveGyroData.getY();
					result[i][5] = leftGloveGyroData.getZ();
					*/
				}
				return result;
			}
		}
		else {
			// Right handed gesture, each tuple is size 3 (for one glove's worth of accelerometer data)
			double[][] result = new double[this.getNumDataPts()][NUM_COORDS_PER_GLOVE];
			
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
				
				/*
				Vector3D rightGloveGyroData = rightGloveData.getGyroData();
				assert(rightGloveGyroData != null);
				
				result[i][3] = rightGloveGyroData.getX();
				result[i][4] = rightGloveGyroData.getY();
				result[i][5] = rightGloveGyroData.getZ();
				*/
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
		if (!this.leftGloveData.equals(otherGestureInstance.leftGloveData)) {
			return false;
		}
		if (!this.rightGloveData.equals(otherGestureInstance.rightGloveData)) {
			return false;
		}
		if (!this.timePts.equals(otherGestureInstance.timePts)) {
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
			this.leftGloveData = new ArrayList<GloveData>(amtOfData);
			for (int i = 0; i < amtOfData; i++) { this.leftGloveData.add(null); }
			count++;
		}
		else {
			this.leftGloveData = new ArrayList<GloveData>(0);
		}
		if (header.contains("R")) {
			this.rightGloveData = new ArrayList<GloveData>(amtOfData);
			for (int i = 0; i < amtOfData; i++) { this.rightGloveData.add(null); }
			count++;
		}
		else {
			this.rightGloveData = new ArrayList<GloveData>(0);
		}
		
		this.timePts = new ArrayList<Double>(amtOfData);
		for (int i = 0; i < amtOfData; i++) { this.timePts.add(null); }
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
			amtOfData = this.leftGloveData.size();
			
			if (this.hasRightGloveData()) {
				result += "R";
			}
		}
		else {
			result += "R";
			amtOfData = this.rightGloveData.size();
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
	
	
	private String gloveDataToString(String gloveTitle, List<GloveData> data) {
		if (data == null || data.isEmpty()) {
			return "";
		}
		
		String result = gloveTitle + "\n";
		for (int i = 0; i < data.size(); i++) {
			result += data.get(i).toDataString() + "\n";
		}
		return result;
	}
	
	private String timeDataToString(String timeTitle) {
		if (this.timePts == null) {
			return "";
		}
		String result = timeTitle + "\n";
		for (int i = 0; i < this.timePts.size(); i++) {
			result += this.timePts.get(i) + "\n";
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
	
	private boolean gloveDataFromString(Scanner scanner, List<GloveData> data) {
		for (int i = 0; i < data.size(); i++) {
			String nextLine = scanner.nextLine();
			GloveData gloveData = new GloveData();
			data.set(i, gloveData);
			if (!gloveData.fromDataString(nextLine)) {
				return false;
			}
		}
		return true;
	}
	private boolean timeDataFromString(Scanner scanner, List<Double> data) {
		try {
			for (int i = 0; i < data.size(); i++) {
				String nextLine = scanner.nextLine();
				data.set(i, Double.parseDouble(nextLine));
			}
		}
		catch (NumberFormatException ex) {
			return false;
		}
		return true;
	}

	public static void main(String[] args) {
		ArrayList<GloveData> leftGloveData  = new ArrayList<GloveData>(10);
		ArrayList<GloveData> rightGloveData = new ArrayList<GloveData>(10);
		ArrayList<Double> timeData = new ArrayList<Double>(10);
		
		for (int i = 0; i < 10; i++) {
			leftGloveData.add(new GloveData(
					Math.random(), Math.random(), Math.random(),
					Math.random(), Math.random(), Math.random(),
					Math.random(), Math.random(), Math.random()));
			rightGloveData.add(new GloveData(
					Math.random(), Math.random(), Math.random(),
					Math.random(), Math.random(), Math.random(),
					Math.random(), Math.random(), Math.random()));
			timeData.add(new Double(i));
		}
		
		GestureInstance toStrInstance = new GestureInstance(leftGloveData, rightGloveData, timeData);
		String dataStr = toStrInstance.toDataString();
		System.out.println(dataStr);
		
		GestureInstance fromStrInstance = new GestureInstance();
		fromStrInstance.fromDataString(dataStr);
		
		System.out.println(fromStrInstance.equals(toStrInstance));
	}

}
