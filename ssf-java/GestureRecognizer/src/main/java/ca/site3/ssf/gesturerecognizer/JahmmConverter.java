package ca.site3.ssf.gesturerecognizer;

import java.util.ArrayList;
import java.util.List;

import be.ac.ulg.montefiore.run.jahmm.Hmm;
import be.ac.ulg.montefiore.run.jahmm.ObservationVector;
import be.ac.ulg.montefiore.run.jahmm.OpdfMultiGaussianFactory;
import be.ac.ulg.montefiore.run.jahmm.learn.BaumWelchLearner;
import be.ac.ulg.montefiore.run.jahmm.learn.KMeansLearner;
import be.ac.ulg.montefiore.run.jahmm.toolbox.MarkovGenerator;

/**
 * Static class for holding methods for converting native gesturerecognizer package
 * objects into jahmm (Java Hidden Markov Model library) types.
 * 
 * @author Callum
 *
 */
public class JahmmConverter {

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
	 * @return 
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
	
	public static Hmm<ObservationVector> buildKMeansHMM(GestureDataSet dataSet) {
		List<List<ObservationVector>> sequences = JahmmConverter.gestureDataSetToObservationSequences(dataSet);
		KMeansLearner<ObservationVector> kMeansLearner =
				new KMeansLearner<ObservationVector>(10, new OpdfMultiGaussianFactory(sequences.get(0).get(0).dimension()), sequences);
		return kMeansLearner.learn();
	}
	
	/*
	public static ObservationVector gestureInstanceToObservation(GestureInstance gestureInst) {
		assert(gestureInst != null);
		
		double[][] trainingSeq = gestureInst.getTrainingSequence();
		if (trainingSeq == null || trainingSeq.length == 0) {
			assert(false);
			return null;
		}
		
		double[] resultValues = new double[trainingSeq.length * trainingSeq[0].length];
		int count = 0;
		for (int i = 0; i < trainingSeq.length; i++) {
			for (int j = 0; j < trainingSeq[i].length; j++)
			resultValues[count++] = trainingSeq[i][j];
		}
		
		return new ObservationVector(resultValues);
	}
	
	public static List<ObservationVector> gestureDataSetToObservationSequence(GestureDataSet dataSet) {
		assert(dataSet != null);
		
		// Go through each gesture instance in the data set and add the observation sequence
		// generated for that instance to the list of lists of observations
		List<ObservationVector> result = new ArrayList<ObservationVector>(dataSet.getNumGestureInstances());
		for (int i = 0; i < dataSet.getNumGestureInstances(); i++) {
			ObservationVector currObservation = JahmmConverter.gestureInstanceToObservation(dataSet.getGestureInstanceAt(i));
			if (currObservation == null || currObservation.dimension() == 0) {
				assert(false);
				return null;
			}
			result.add(currObservation);
		}
		
		return result;
	}
	

	public static Hmm<ObservationVector> BuildKMeansHMM(List<GestureDataSet> dataSets) {
		List<List<ObservationVector>> sequences = new ArrayList<List<ObservationVector>>(dataSets.size());
		for (GestureDataSet dataSet : dataSets) {
			List<ObservationVector> seq = JahmmConverter.gestureDataSetToObservationSequence(dataSet);
			sequences.add(seq);
		}
		
		KMeansLearner<ObservationVector> kMeansLearner =
				new KMeansLearner<ObservationVector>(nbStates, opdfFactory, sequences)
	}
	*/
	
	public static void main(String[] args) {
		
		GestureInstance[] gestureInstances = new GestureInstance[20];
		for (int i = 0; i < 20; i++) {
			GloveData[] leftGloveData = new GloveData[10 + i];
			GloveData[] rightGloveData = new GloveData[10 + i];
			double[] timeData = new double[10 + i];
			
			for (int j = 0; j < 10 + i; j++) {
				leftGloveData[j] = new GloveData(
						j, j, j,
						(i+j+1) * Math.random(), (i+j+1) + Math.random() * Math.random(), (i+j+1) + Math.random(),
						j, j, j);
				rightGloveData[j] = new GloveData(
						j, j, j,
						(i+j+1) + Math.random() * Math.random(), (i+j+1) + Math.random(), (i+j+1) + Math.random() * Math.random(),
						j, j, j);
				timeData[j] = j*0.1;
			}
			
			gestureInstances[i] = new GestureInstance(leftGloveData, rightGloveData, timeData);
			
			
		}

		GestureDataSet dataSet = new GestureDataSet(gestureInstances);
		List<List<ObservationVector>> observationSeqs = JahmmConverter.gestureDataSetToObservationSequences(dataSet);
		System.out.println(observationSeqs.toString());
		System.out.println();
		
		
		Hmm<ObservationVector> hmm = JahmmConverter.buildKMeansHMM(dataSet);
		System.out.println();
		System.out.println(hmm);
		
		BaumWelchLearner bwl = new BaumWelchLearner();
		Hmm<ObservationVector> furtherTrainedHmm = bwl.learn(hmm, observationSeqs);
		for (List<ObservationVector> seq : observationSeqs) {
			System.out.println(furtherTrainedHmm.probability(seq));
		}
		
		System.out.println(furtherTrainedHmm);
		
		//MarkovGenerator<ObservationVector> mg = new MarkovGenerator<ObservationVector>(hmm);
		//System.out.println(mg.observationSequence(10));

		
	}
}
