package ca.site3.ssf.gesturerecognizer;

import java.util.Arrays;
import java.util.InputMismatchException;
import java.util.Scanner;

public class GestureInstance {

	static final private String LEFT_GLOVE_DATA_STR  = "LEFT_GLOVE_DATA";
	static final private String RIGHT_GLOVE_DATA_STR = "RIGHT_GLOVE_DATA";
	
	private GloveData[] leftGloveData;
	private GloveData[] rightGloveData;
	
	public GestureInstance() {
		super();
		this.leftGloveData  = null;
		this.rightGloveData = null;
	}
	
	public GestureInstance(GloveData[] leftGloveData, GloveData[] rightGloveData) {
		super();
		this.leftGloveData  = leftGloveData;
		this.rightGloveData = rightGloveData;
	}
	
	public boolean hasLeftGloveData() {
		return this.leftGloveData != null;
	}
	public boolean hasRightGloveData() {
		return this.rightGloveData != null;
	}
	
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (other == null) {
			return false;
		}
		if (!(other instanceof GloveData)) {
			return false;
		}
		
		GestureInstance otherGestureInstance = (GestureInstance)other;
		if (!Arrays.equals(this.leftGloveData, otherGestureInstance.leftGloveData)) {
			return false;
		}
		if (!Arrays.equals(this.rightGloveData, otherGestureInstance.rightGloveData)) {
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
		
		for (int i = 0; i < count; i++) {
			if (!this.gloveHeaderAndDataFromScanner(scanner)) {
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
	
	
	private boolean gloveHeaderAndDataFromScanner(Scanner scanner) {
		try {
			String gloveHeaderStr;
			gloveHeaderStr = scanner.next();

			if (gloveHeaderStr.equals(LEFT_GLOVE_DATA_STR)) {
				if (!this.hasLeftGloveData()) {
					return false;
				}
				return this.gloveDataFromString(scanner, this.leftGloveData);
			}
			else if (gloveHeaderStr.equals(RIGHT_GLOVE_DATA_STR)) {
				if (!this.hasRightGloveData()) {
					return false;
				}
				return this.gloveDataFromString(scanner, this.rightGloveData);
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
	
	public String toString() {
		return this.toDataString();
	}
	
	
	public static void main(String[] args) {
		GloveData[] leftGloveData = new GloveData[10];
		GloveData[] rightGloveData = new GloveData[10];
		
		for (int i = 0; i < 10; i++) {
			leftGloveData[i] = new GloveData(
					Math.random(), Math.random(), Math.random(),
					Math.random(), Math.random(), Math.random(),
					Math.random(), Math.random(), Math.random());
			rightGloveData[i] = new GloveData(
					Math.random(), Math.random(), Math.random(),
					Math.random(), Math.random(), Math.random(),
					Math.random(), Math.random(), Math.random());
		}
		
		GestureInstance toStrInstance = new GestureInstance(leftGloveData, rightGloveData);
		String dataStr = toStrInstance.toDataString();
		System.out.println(dataStr);
		
		GestureInstance fromStrInstance = new GestureInstance(null, null);
		fromStrInstance.fromDataString(dataStr);
	}

}
