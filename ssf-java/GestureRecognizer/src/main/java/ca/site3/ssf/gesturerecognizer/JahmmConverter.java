package ca.site3.ssf.gesturerecognizer;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import be.ac.ulg.montefiore.run.jahmm.Hmm;
import be.ac.ulg.montefiore.run.jahmm.ObservationVector;
import be.ac.ulg.montefiore.run.jahmm.OpdfMultiGaussianFactory;
import be.ac.ulg.montefiore.run.jahmm.learn.BaumWelchScaledLearner;
import be.ac.ulg.montefiore.run.jahmm.learn.KMeansLearner;

/**
 * Static class for holding methods for converting native gesturerecognizer package
 * objects into jahmm (Java Hidden Markov Model library) types.
 * 
 * @author Callum
 *
 */
public class JahmmConverter {

	private static Logger logger = LoggerFactory.getLogger(JahmmConverter.class);
	
	private JahmmConverter() {
	}
	
	/**
	 * Converts the given gesture instance into a sequence of observation vectors for the Jahmm library.
	 * @param gestureInst The gesture instance to use in creating the observation sequence.
	 * @return The resulting sequence of observation vectors for the given gesture instance, null on failure.
	 */
	public static List<ObservationVector> gestureInstanceToObservationSequence(GestureInstance gestureInst) {
		assert(gestureInst != null);
		
		double[][] trainingSeq = gestureInst.getTrainingSequence();
		if (trainingSeq == null || trainingSeq.length == 0) {
			assert(false);
			return null;
		}
		
		List<ObservationVector> result = new ArrayList<ObservationVector>(trainingSeq.length);
		for (int i = 0; i < trainingSeq.length; i++) {
			result.add(new ObservationVector(trainingSeq[i]));
		}
		return result;
	}
	
	/**
	 * Builds a list of sequences of observation vectors for the jahmm library.
	 * @param dataSet The gesture's data set - set of all instances for a particular gesture.
	 * @return The resulting sequences of observations for the given gesture data set, null on failure.
	 */
	public static List<List<ObservationVector>> gestureDataSetToObservationSequences(GestureDataSet dataSet) {
		assert(dataSet != null);
		
		// Go through each gesture instance in the data set and add the observation sequence
		// generated for that instance to the list of lists of observations
		List<List<ObservationVector>> result = new ArrayList<List<ObservationVector>>(dataSet.getNumGestureInstances());
		for (int i = 0; i < dataSet.getNumGestureInstances(); i++) {
			List<ObservationVector> currSequence = JahmmConverter.gestureInstanceToObservationSequence(dataSet.getGestureInstanceAt(i));
			if (currSequence == null || currSequence.size() == 0) {
				assert(false);
				return null;
			}
			result.add(currSequence);
		}
		
		return result;
	}
	
	/**
	 * Builds a HMM using a K-Means seperation algorithm combined with Baum-Welch Learning.
	 * @param dataSet The data set used to build the initial HMM.
	 * @param numStates The number of states to use in the built HMM.
	 * @return The newly built HMM, null on failure to build (dataSet is invalid: linear or constant).
	 */
	public static Hmm<ObservationVector> buildKMeansHMMWithTraining(GestureDataSet dataSet, int numStates) {
		List<List<ObservationVector>> sequences = JahmmConverter.gestureDataSetToObservationSequences(dataSet);
		
		int dimension = sequences.get(0).get(0).dimension();
		KMeansLearner<ObservationVector> kMeansLearner =
				new KMeansLearner<ObservationVector>(numStates, 
						new OpdfMultiGaussianFactory(dimension), sequences);
		try {
			Hmm<ObservationVector> kMeansHmm = kMeansLearner.iterate();
			BaumWelchScaledLearner bwl = new BaumWelchScaledLearner();
			bwl.setNbIterations(15);
			return bwl.learn(kMeansHmm, sequences);
		}
		catch (IllegalArgumentException e) {
			logger.warn("Failed to learn from gesture data set: " + e.getMessage());
		}
		return null;
	}
	
	/**
	 * Train an existing HMM with the given data set.
	 * @param hmm The existing HMM to train.
	 * @param dataSet The new data set used to add training/learning to the given HMM.
	 * @return The HMM, now smarter than ever before! (or not, if learning failed)
	 */
	public static Hmm<ObservationVector> trainHMM(Hmm<ObservationVector> hmm, GestureDataSet dataSet) {
		List<List<ObservationVector>> sequences = JahmmConverter.gestureDataSetToObservationSequences(dataSet);
		try {
			BaumWelchScaledLearner bwl = new BaumWelchScaledLearner();
			bwl.setNbIterations(15);
			return bwl.learn(hmm, sequences);
		}
		catch (IllegalArgumentException e) {
			logger.warn("Failed to learn from gesture data set: " + e.getMessage());
		}
		return hmm;
	}
	
	public static void main(String[] args) {
		
		GestureInstance[] gestureInstances = new GestureInstance[20];
		for (int i = 0; i < 20; i++) {
			ArrayList<GloveData> leftGloveData  = new ArrayList<GloveData>(10);
			ArrayList<GloveData> rightGloveData = new ArrayList<GloveData>(10);
			ArrayList<Double> timeData          = new ArrayList<Double>(10);
			
			for (int j = 0; j < 10; j++) {
				
				double aX = (j+1) + Math.random();
				double aY = (j+1) + Math.random();
				double aZ = (j+1) + Math.random() * Math.random();
				
				// doesn't work when you use the same data for both left and right glove....
				// need to be able to define null glove data!!!
				
				leftGloveData.add(new GloveData(
						j, j, j,
						aX, aY, aZ,
						j, j, j));
				//rightGloveData.add(new GloveData(
				//		j, j, j,
				//		aX, aY, aZ,
				//		j, j, j));
				
				timeData.add(new Double(j*0.1));
			}
			
			gestureInstances[i] = new GestureInstance(leftGloveData, rightGloveData, timeData);
		}

		GestureDataSet dataSet = new GestureDataSet(gestureInstances);
		List<List<ObservationVector>> observationSeqs = JahmmConverter.gestureDataSetToObservationSequences(dataSet);
		System.out.println(observationSeqs.toString());
		System.out.println();
		
		Hmm<ObservationVector> hmm = JahmmConverter.buildKMeansHMMWithTraining(dataSet, 3);
		if (hmm == null) {
			System.out.print("Failed to build HMM.");
			return;
		}
		System.out.println();
		System.out.println(hmm);
		System.out.println();
		for (List<ObservationVector> seq : observationSeqs) {
			System.out.println(hmm.probability(seq));
		}		
		System.out.println();
		for (List<ObservationVector> seq : observationSeqs) {
			System.out.println(hmm.lnProbability(seq));
		}	
		//MarkovGenerator<ObservationVector> mg = new MarkovGenerator<ObservationVector>(hmm);
		//System.out.println(mg.observationSequence(10));

		
	}
}
