package ca.site3.ssf.gesturerecognizer;

import java.util.InputMismatchException;
import java.util.Scanner;

import org.apache.commons.math.geometry.Vector3D;

import ca.site3.ssf.common.Algebra;

/**
 * A class for representing a single point of a single glove's data for Super Street Fire.
 * A glove's data consists of three 3-vectors of information:
 * - Gyroscope data on x,y,z
 * - Accelerometer data on x,y,z
 * - Magnetometer data on x,y,z
 * 
 * This class also provides methods to quickly read/write all of a gloves data to and from
 * a comma separated string (see fromDataString and toDataString).
 * 
 * @author Callum
 *
 */
public class GloveData {
	public enum DataType { ACCEL_DATA, GYRO_DATA, MAGNETO_DATA };
	
	private Vector3D gyroData;
	private Vector3D accelData;
	private Vector3D magnetoData;
	
	public GloveData() {
		this.gyroData    = new Vector3D(0, 0, 0);
		this.accelData   = new Vector3D(0, 0, 0);
		this.magnetoData = new Vector3D(0, 0, 0);
	}
	
	public GloveData(double gyroX, double gyroY, double gyroZ,
					 double accelX, double accelY, double accelZ,
					 double magnetoX, double magnetoY, double magnetoZ) {
		
		this.gyroData    = new Vector3D(gyroX, gyroY, gyroZ);
		this.accelData   = new Vector3D(accelX, accelY, accelZ);
		this.magnetoData = new Vector3D(magnetoX, magnetoY, magnetoZ);
	}
	
	public Vector3D getGyroData() {
		return this.gyroData;
	}
	
	public Vector3D getAccelData() {
		return this.accelData;
	}
	
	public Vector3D getMagnetoData() {
		return this.magnetoData;
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
		
		GloveData otherGloveData = (GloveData)other;
		return Algebra.Approx(this.getAccelData(),   otherGloveData.getAccelData(),   Algebra.FLT_EPSILON) &&
			   Algebra.Approx(this.getGyroData(),    otherGloveData.getGyroData(),    Algebra.FLT_EPSILON) &&
			   Algebra.Approx(this.getMagnetoData(), otherGloveData.getMagnetoData(), Algebra.FLT_EPSILON);
	}
	
	public boolean fromDataString(String str) {
		Scanner scanner = new Scanner(str);
		scanner.useDelimiter(",\\s*");
		
		this.gyroData    = this.readDataVector(scanner);
		this.accelData   = this.readDataVector(scanner);
		this.magnetoData = this.readDataVector(scanner);
		
		return (this.gyroData != null && this.accelData != null && this.magnetoData != null);
	}

	private Vector3D readDataVector(Scanner scanner) {
		double tempX, tempY, tempZ;
		
		try {
			tempX = scanner.nextDouble();
			tempY = scanner.nextDouble();
			tempZ = scanner.nextDouble();
		}
		catch (InputMismatchException ex) {
			return null;
		}
		
		return new Vector3D(tempX, tempY, tempZ);
	}
	
	public String toDataString() {
		String gyroStr  = this.gyroData.getX() + ", "  + this.gyroData.getY() + ", " + this.gyroData.getZ();
		String accelStr = this.accelData.getX() + ", "  + this.accelData.getY() + ", " + this.accelData.getZ();
		String magnetoStr = this.magnetoData.getX() + ", "  + this.magnetoData.getY() + ", " + this.magnetoData.getZ();
		return gyroStr + ", " + accelStr + ", " + magnetoStr;
	}

	public String toString() {
		return this.toDataString();
	}
	
	public static void main(String[] args) {
		
		// Test to/from string
		GloveData writeData = new GloveData(1.0f, 2.0f, 3.0f, 4.0f, 5.0f, 6.0f, 7.0f, 8.0f, 9.0f);
		String toString = writeData.toDataString();
		
		GloveData readData = new GloveData();
		readData.fromDataString(toString);
		
		boolean equals = writeData.equals(readData);
		System.out.println(equals);
	}
	
}
