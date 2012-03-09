package ca.site3.ssf.gesturerecognizer;

import java.util.ArrayList;
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
	 * @return The resulting TimeSeries, null on failure.
	 */
	public static TimeSeries gestureInstanceToTimeSeries(GestureInstance gestureInst,
														 int minTimeSeriesLength) {
		assert(minTimeSeriesLength > 0);
		assert(gestureInst != null);
		assert(gestureInst.isValid());
		
		double[][] trainingSeq = gestureInst.getTrainingSequence();
		if (trainingSeq == null || trainingSeq.length == 0) {
			assert(false);
			return null;
		}
		
		TimeSeries gestureTimeSeries = new TimeSeries(trainingSeq[0].length);
		for (int i = 0; i < trainingSeq.length; i++) {
			gestureTimeSeries.addLast(gestureInst.getTimeAt(i), new TimeSeriesPoint(trainingSeq[i]));
		}

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

		ArrayList<GloveData> leftGloveData  = new ArrayList<GloveData>(10);
		ArrayList<GloveData> rightGloveData = new ArrayList<GloveData>(10);
		ArrayList<Double> timeData          = new ArrayList<Double>(10);
		
		for (int j = 0; j < 10; j++) {
			leftGloveData.add(new GloveData(
					j, j, j,
					(j+1) + Math.random(), (j+1) + Math.random(), (j+1) + Math.random(),
					j, j, j));
			rightGloveData.add(new GloveData(
					j, j, j,
					(j+1) + Math.random(), (j+1) + Math.random(), (j+1) + Math.random() * Math.random(),
					j, j, j));
			timeData.add(new Double(j*0.1));
		}
			
		GestureInstance instanceBothHands = new GestureInstance(leftGloveData, rightGloveData, timeData);
		GestureInstance instanceLeftHand = new GestureInstance(leftGloveData, null, timeData);
		GestureInstance instanceRightHand = new GestureInstance(null, rightGloveData, timeData);

		printTimeSeries(JavaMLConverter.gestureInstanceToTimeSeries(instanceBothHands, 6));
		printTimeSeries(JavaMLConverter.gestureInstanceToTimeSeries(instanceLeftHand,  6));
		printTimeSeries(JavaMLConverter.gestureInstanceToTimeSeries(instanceRightHand, 6));
	}
}
