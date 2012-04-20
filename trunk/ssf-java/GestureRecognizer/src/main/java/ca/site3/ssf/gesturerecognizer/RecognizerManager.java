package ca.site3.ssf.gesturerecognizer;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import be.ac.ulg.montefiore.run.jahmm.io.FileFormatException;

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
	
	private final static double MINIMUM_PROBABILITY_THRESHOLD = 0.1;
	
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
	 * @return true on successful training, false on failure to train.
	 */
	boolean train(GestureType gesture, GestureDataSet dataSet) {
		Recognizer gestureRecog = this.recognizerMap.get(gesture);
		assert(gestureRecog != null);
		return gestureRecog.train(dataSet);
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
	
	/**
	 * Performs gesture recognition on the given gesture instance for all gestures and
	 * places the full result into the returned data.
	 * @param inst The gesture instance to identify/recognize.
	 * @return The full result of the recognition procedure, with data for all gestures.
	 */
	GestureRecognitionResult recognizeWithFullResult(GestureInstance inst) {
		Map<GestureType, Double> resultMapping = new HashMap<GestureType, Double>();
		for (Entry<GestureType, Recognizer> entry : this.recognizerMap.entrySet()) {
			resultMapping.put(entry.getKey(), entry.getValue().probability(inst));
		}
		
		return new GestureRecognitionResult(resultMapping);
	}
	
	/**
	 * Writes/Saves all of the recognizers in this manager.
	 * @param writer The writer to write the recognizers to.
	 * @return true on success, false on failure.
	 */
	boolean writeRecognizers(Writer writer) {
		try {
			for (Recognizer recognizer : this.recognizerMap.values()) {
				recognizer.save(writer);
			}
		}
		catch (IOException ex) {
			System.err.println(ex.toString());
			return false;
		}

		return true;
	}
	
	/**
	 * Reads/Loads all of the recognizers in this manager.
	 * @param reader The reader for reading the recognizers from.
	 * @return true on success, false on failure.
	 */
	boolean readRecognizers(Reader reader) {
		try {
			Recognizer newRecognizer = new Recognizer();
			newRecognizer.load(reader);
			this.recognizerMap.put(newRecognizer.getGestureType(), newRecognizer);
		}
		catch (IOException ex) {
			System.err.println(ex.toString());
			return false;
		}
		catch (FileFormatException ex) {
			System.err.println(ex.toString());
			return false;
		}
		
		return true;
	}
	
}
