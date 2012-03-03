package ca.site3.ssf.gesturerecognizer;

import be.ac.ulg.montefiore.run.jahmm.Hmm;
import be.ac.ulg.montefiore.run.jahmm.ObservationVector;

/**
 * Contains the structures and functionality to recognize one particular type of gesture
 * (after sufficient training has occurred).
 * 
 * @author Callum
 *
 */
class Recognizer {

	final private GestureType gestureType;
	private Hmm<ObservationVector> recognizer;
	
	Recognizer(GestureType gestureType) {
		this.gestureType = gestureType;
		this.recognizer  = null;
	}
	
	Recognizer(GestureType gestureType, GestureDataSet dataSet) {
		this.gestureType = gestureType;
		this.train(dataSet);
	}
	
	GestureType getGestureType() {
		return this.gestureType;
	}
	
	boolean hasTrainedRecognizer() {
		return (this.recognizer != null);
	}
	
	/**
	 * Resets the recognizer for this gesture and trains a new one with the given data set.
	 * @param dataSet The data set used to train the recognizer.
	 */
	void train(GestureDataSet dataSet) {
		assert(dataSet != null);
		assert(dataSet.getGestureInstanceAt(0).getTrainingDataObservationWidth() == this.gestureType.getNumHands()*3);
		if (this.hasTrainedRecognizer()) {
			this.trainMore(dataSet);
		}
		else {
			this.recognizer = JahmmConverter.buildKMeansHMMWithTraining(dataSet, this.gestureType.getNumHmmNodes());
			assert(this.recognizer != null);
		}
	}
	
	/**
	 * Gets a probability [0,1] of the given gesture instance being the same type of gesture
	 * as the one recognized by this.
	 * @param inst The gesture instance to test against.
	 * @return The [0,1] probability of the gesture being the same as the one recognized by this.
	 */
	double probability(GestureInstance inst) {
		assert(inst != null);
		
		if (this.recognizer == null) {
			return 0.0;
		}
		if (inst.getTrainingDataObservationWidth() != this.gestureType.getNumHands()*3) {
			return 0.0;
		}
		
		return this.recognizer.probability(JahmmConverter.gestureInstanceToObservationSequence(inst));
	}
	
	/**
	 * Allows the existing recognizer for the gesture to learn more from another data set.
	 * @param dataSet The data set to learn more from.
	 */
	private void trainMore(GestureDataSet dataSet) {
		assert(this.recognizer != null);
		this.recognizer = JahmmConverter.trainHMM(this.recognizer, dataSet);
	}
}
