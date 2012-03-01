package ca.site3.ssf.gesturerecognizer;

import java.util.EnumSet;

import org.apache.commons.math.geometry.Vector3D;

import ca.site3.ssf.common.Algebra;

import net.sf.javaml.distance.fastdtw.timeseries.PAA;
import net.sf.javaml.distance.fastdtw.timeseries.TimeSeries;
import net.sf.javaml.distance.fastdtw.timeseries.TimeSeriesPoint;

/**
 * A class that centralizes conversions from the gesturerecognizer package's classes into
 * JavaML classes for later use by JavaML.
 * 
 * @author Callum
 *
 */
class JavaMLConverter {

	private JavaMLConverter() {
	}
	
	/**
	 * Converts the given gesture into a javaml library TimeSeries type and constrains the
	 * number of time point samples to the given fixed length.
	 * @param gestureInst The gesture instance to convert.
	 * @param minTimeSeriesLength The minimum number of time point samples in the returned TimeSeries.
	 * @return The resulting TimeSeries.
	 */
	public static TimeSeries ConvertGestureInstanceToTimeSeries(GestureInstance gestureInst,
																int minTimeSeriesLength) {
		assert(minTimeSeriesLength > 0);
		assert(gestureInst != null);
		assert(gestureInst.isValid());
		
		TimeSeries gestureTimeSeries = null;
		
		if (gestureInst.hasLeftGloveData()) {
			
			if (gestureInst.hasRightGloveData()) {
				// Two handed gesture...
				gestureTimeSeries = new TimeSeries(6);
				double[] values = new double[6];
				
				for (int i = 0; i < gestureInst.getNumDataPts(); i++) {
					
					// Right now we only consider accelerometer data...
					GloveData leftGloveData  = gestureInst.getLeftGloveDataAt(i);
					GloveData rightGloveData = gestureInst.getRightGloveDataAt(i);
					assert(leftGloveData != null && rightGloveData != null);
					
					Vector3D leftGloveAccelData  = leftGloveData.getAccelData();
					Vector3D rightGloveAccelData = rightGloveData.getAccelData();
					assert(leftGloveAccelData != null && rightGloveAccelData != null);
					
					values[0] = leftGloveAccelData.getX();
					values[1] = leftGloveAccelData.getY();
					values[2] = leftGloveAccelData.getZ();
					values[3] = rightGloveAccelData.getX();
					values[4] = rightGloveAccelData.getY();
					values[5] = rightGloveAccelData.getZ();
					
					gestureTimeSeries.addLast(gestureInst.getTimeAt(i), new TimeSeriesPoint(values));
				}
			}
			else {
				// Left glove data exists but not right...
				// One-handed (Left) gesture
				gestureTimeSeries = new TimeSeries(3);
				double[] values = new double[3];
				
				for (int i = 0; i < gestureInst.getNumDataPts(); i++) {
					// Right now we only consider accelerometer data...
					GloveData leftGloveData  = gestureInst.getLeftGloveDataAt(i);
					assert(leftGloveData != null);
					Vector3D leftGloveAccelData  = leftGloveData.getAccelData();
					assert(leftGloveAccelData != null);
					
					values[0] = leftGloveAccelData.getX();
					values[1] = leftGloveAccelData.getY();
					values[2] = leftGloveAccelData.getZ();
					gestureTimeSeries.addLast(gestureInst.getTimeAt(i), new TimeSeriesPoint(values));
				}
			}
		}
		else {
			// Right glove data must exist...
			assert(gestureInst.hasRightGloveData());
		
			// One-handed (Right) gesture
			gestureTimeSeries = new TimeSeries(3);
			double[] values = new double[3];
			
			for (int i = 0; i < gestureInst.getNumDataPts(); i++) {
				// Right now we only consider accelerometer data...
				GloveData rightGloveData  = gestureInst.getRightGloveDataAt(i);
				assert(rightGloveData != null);
				Vector3D rightGloveAccelData  = rightGloveData.getAccelData();
				assert(rightGloveAccelData != null);
				
				values[0] = rightGloveAccelData.getX();
				values[1] = rightGloveAccelData.getY();
				values[2] = rightGloveAccelData.getZ();
				gestureTimeSeries.addLast(gestureInst.getTimeAt(i), new TimeSeriesPoint(values));
			}
		}

		assert(gestureTimeSeries != null);
		PAA result = new PAA(gestureTimeSeries, Math.min(gestureTimeSeries.numOfPts(), minTimeSeriesLength));
		return result;
	}
	
	public static void printTimeSeries(TimeSeries timeSeries) {
		System.out.println(timeSeries.toString());
        for (int i = 0; i < timeSeries.numOfPts(); i++) {
        	System.out.println(timeSeries.getTimeAtNthPoint(i));
        }
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
			timeData[i] = i*0.1;
		}
		
		GestureInstance instanceBothHands = new GestureInstance(leftGloveData, rightGloveData, timeData);
		GestureInstance instanceLeftHand = new GestureInstance(leftGloveData, null, timeData);
		GestureInstance instanceRightHand = new GestureInstance(null, rightGloveData, timeData);

		printTimeSeries(JavaMLConverter.ConvertGestureInstanceToTimeSeries(instanceBothHands, 6));
		printTimeSeries(JavaMLConverter.ConvertGestureInstanceToTimeSeries(instanceLeftHand,  6));
		printTimeSeries(JavaMLConverter.ConvertGestureInstanceToTimeSeries(instanceRightHand, 6));
	}
}
