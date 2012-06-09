package ca.site3.ssf.gesturerecognizer;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	
	private final static double MINIMUM_BASE_PROBABILITY_THRESHOLD       = 1E-280;
	private final static double MINIMUM_KMEANS_PROBABILITY_THRESHOLD     = 1E-85;
	private final static double EPSILON_BASE_PROBABILITY_THRESHOLD       = 1E-300;
	private final static double LAST_CHANCE_KMEANS_PROBABILITY_THRESHOLD = 1E-60;
	
	private Logger logger = null;
	
	private Map<GestureType, Recognizer> recognizerMap =
			new HashMap<GestureType, Recognizer>(GestureType.values().length);
	
	RecognizerManager() {
		this.logger = LoggerFactory.getLogger(this.getClass());
		
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
	 * Untrain the given gesture's recognizer to a blank state.
	 * @param gesture The gesture whose recognizer will be untrained / cleared.
	 */
	void untrain(GestureType gesture) {
		this.recognizerMap.put(gesture, new Recognizer(gesture));
	}
	
	/**
	 * Tests whether a gesture would even be REMOTELY considered acceptable for testing.
	 * @param gestureInstance The gesture to test.
	 * @return true if acceptable, false if not.
	 */
	static boolean isAcceptableGesture(GestureInstance gestureInstance) {
		if (!gestureInstance.isValid()) {
			return false;
		}
		
		// The gesture shouldn't be too too short...
		if (gestureInstance.getMaxTimeDiff() < GestureRecognizer.MINIMUM_GESTURE_RECOGNITION_TIME_IN_SECS) {
			return false;
		}
		else if (gestureInstance.getMaxTimeDiff() > GestureRecognizer.MAXIMUM_GESTURE_RECOGNITION_TIME_IN_SECS) {
			return false;
		}
		
		return true;
	}
	
	/**
	 * Attempts to recongize the given, novel gesture instance among all of the gesture
	 * recognizers in this manager.
	 * @param inst The novel gesture instance to identify/recognize.
	 * @return The gesture type that was recognized, null on no recognized gesture.
	 */
	GestureType recognize(GestureInstance inst) {
		// Weed out strange and anomalous data
		if (!RecognizerManager.isAcceptableGesture(inst)) {
			this.logger.info("Ignoring gesture - too short from beginning to end!");
			return null;
		}
		
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
		
		if (bestProbability < MINIMUM_BASE_PROBABILITY_THRESHOLD) {
			
			double bestBaseProbability = bestProbability;
			
			for (Recognizer recognizer : this.recognizerMap.values()) {
				currProbability = recognizer.kMeansProbability(inst);
				if (currProbability > bestProbability) {
					bestProbability = currProbability;
					bestGesture = recognizer.getGestureType();
				}
			}
			
			if (bestProbability < MINIMUM_KMEANS_PROBABILITY_THRESHOLD) {
				this.logger.info("Failed to recognize gesture, did not meet minimum probability threshold of either base or k-means.");
				return null;
			}
			else if (bestBaseProbability < EPSILON_BASE_PROBABILITY_THRESHOLD) {
				if (bestProbability < LAST_CHANCE_KMEANS_PROBABILITY_THRESHOLD) {
					this.logger.info("Failed to recognize gesture, did not meet minimum base epsilon probability threshold.");
					return null;
				}
			}
			
		}
		
		// Inline: We have a gesture that is a 'best candidate', we still have a bit more checking to
		// do to see if it is truly worthy of being a proper gesture...
		double fierceness = inst.getTotalFierceness();
		if (bestGesture.getMinFierceDiffThreshold() > fierceness) {
			this.logger.info("Gesture was recognized, but fireceness was not great enough to execute: " +
					"Required fierceness: " + bestGesture.getMinFierceDiffThreshold() + ", fierceness found: " + fierceness);
			return null;
		}
		
		this.logger.info("Best found matching gesture: " + bestGesture.toString()); 
		
		return bestGesture;
	}
	
	/**
	 * Performs gesture recognition on the given gesture instance for all gestures and
	 * places the full result into the returned data.
	 * @param inst The gesture instance to identify/recognize.
	 * @return The full result of the recognition procedure, with data for all gestures.
	 */
	GestureRecognitionResult recognizeWithFullResult(GestureInstance inst) {
		Map<GestureType, GestureProbabilities> resultMapping = new HashMap<GestureType, GestureProbabilities>();
		
		// Weed out strange and anomalous data
		if (!RecognizerManager.isAcceptableGesture(inst)) {
			this.logger.info("Ignoring gesture - way too short from beginning to end!");
			for (Entry<GestureType, Recognizer> entry : this.recognizerMap.entrySet()) {
				resultMapping.put(entry.getKey(), new GestureProbabilities(0.0, 0.0));
			}
			return new GestureRecognitionResult(inst, resultMapping);
		}
		
		for (Entry<GestureType, Recognizer> entry : this.recognizerMap.entrySet()) {
			resultMapping.put(entry.getKey(), new GestureProbabilities(entry.getValue().probability(inst), entry.getValue().kMeansProbability(inst)));
		}
		
		return new GestureRecognitionResult(inst, resultMapping);
	}
	
	/**
	 * Clears all of the currently loaded recognizers to blank states.
	 * WARNING: Clears all loaded training data.
	 */
	void clearRecognizers() {
		this.recognizerMap.clear();
		// Initialize the map of gesture recognizers
		for (GestureType gesture : GestureType.values()) {
			this.recognizerMap.put(gesture, new Recognizer(gesture));
		}
	}
	
	/**
	 * Writes/Saves all of the recognizers in this manager.
	 * @param writer The writer )to write the recognizers to.
	 * @return true on success, false on failure.
	 */
	boolean writeRecognizers(Writer writer) {

		try {
			writer.write("" + this.recognizerMap.size() + "\n");
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
			
			// Begin by reading the number of recognizers to read in from the file...

			char[] charArray = new char[1];
 			int numRecognizers = 0;
			String numRecognizersStr = "";
			
			reader.read(charArray);
			while (charArray[0] != '\n') {
				if (charArray[0] == '\r') {
					reader.read(charArray);
					continue;
				}
				numRecognizersStr += charArray[0];
				reader.read(charArray);
			}
			try {
				numRecognizers = Integer.valueOf(numRecognizersStr);
			}
			catch (NumberFormatException ex) {
				System.err.println(ex.toString());
				return false;
			}
	
			for (int i = 0; i < numRecognizers; i++) {
				Recognizer newRecognizer = new Recognizer();
				newRecognizer.load(reader);
				this.recognizerMap.put(newRecognizer.getGestureType(), newRecognizer);
			}
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
