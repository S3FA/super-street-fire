package ca.site3.ssf.gesturerecognizer;

import java.util.HashMap;
import java.util.Map;

/**
 * Manager of all the recognizers in the GestureRecognizer system. This brings
 * all the recognizers together so that they can easily be trained based on each gesture's
 * enumeration and also so that they can all be tested when evaluating novel data for
 * recognition across all gesture types.
 * 
 * @author Callum
 *
 */
class RecognizerManager {
	
	final static double MINIMUM_PROBABILITY_THRESHOLD = 0.1;
	
	private Map<GestureType, Recognizer> recognizerMap =
			new HashMap<GestureType, Recognizer>(GestureType.values().length);
	
	RecognizerManager() {
		// Initialize the map of gesture recognizers
		for (GestureType gesture : GestureType.values()) {
			this.recognizerMap.put(gesture, new Recognizer(gesture));
		}
	}
	
	/**
	 * Train the given gesture's recognizer with the given data set.
	 * @param gesture The gesture whose recognizer will be trained.
	 * @param dataSet The data set to train the gesture's recognizer with.
	 */
	void train(GestureType gesture, GestureDataSet dataSet) {
		Recognizer gestureRecog = this.recognizerMap.get(gesture);
		assert(gestureRecog != null);
		gestureRecog.train(dataSet);
	}
	
	/**
	 * Attempts to recongize the given, novel gesture instance among all of the gesture
	 * recognizers in this manager.
	 * @param inst The novel gesture instance to identify/recognize.
	 * @return The gesture type that was recognized, null on no recognized gesture.
	 */
	GestureType recognize(GestureInstance inst) {
		double currProbability = -1.0;
		double bestProbability =  0.0;
		GestureType bestGesture = null;
		
		for (Recognizer recognizer : this.recognizerMap.values()) {
			currProbability = recognizer.probability(inst);
			if (currProbability > bestProbability) {
				bestProbability = currProbability;
				bestGesture = recognizer.getGestureType();
			}
		}
		
		// Test for some minimum threshold on the probability
		// for this to actually be a proper gesture...
		if (bestProbability < RecognizerManager.MINIMUM_PROBABILITY_THRESHOLD) {
			return null;
		}
		
		return bestGesture;
	}
	
	
	
}
