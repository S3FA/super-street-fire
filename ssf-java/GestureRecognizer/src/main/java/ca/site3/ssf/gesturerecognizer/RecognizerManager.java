package ca.site3.ssf.gesturerecognizer;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;

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
	
	private final static double MINIMUM_PROBABILITY_THRESHOLD               = 1E-300;
	private final static double BASIC_SPECIAL_PROB_COMPARISON_THRESHOLD     = 1E-30;
	private final static double SPECIAL_EASTEREGG_PROB_COMPARISON_THRESHOLD = 1E-30;
	private final static double BASIC_EASTEREGG_PROB_COMPARISON_THRESHOLD   = 1E-25;
	
	private static Logger logger = LoggerFactory.getLogger(RecognizerManager.class);
	
	private Map<GestureType, Recognizer> recognizerMap =
			new HashMap<GestureType, Recognizer>(GestureType.values().length);
	
	private Map<GestureGenre, Double> bestProbabilityMap =
			new Hashtable<GestureGenre, Double>(GestureGenre.values().length);
	private Map<GestureGenre, GestureType> bestGestureTypeMap =
			new Hashtable<GestureGenre, GestureType>(GestureGenre.values().length);	
	
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
	
	static boolean isAcceptableGestureForGivenType(GestureInstance gestureInstance, GestureType expectedType) {
		
		// Make sure we only evaluate recognizers for the appropriate hands
		if (expectedType.getUsesLeftHand() && !gestureInstance.hasLeftGloveData()   ||
			expectedType.getUsesRightHand() && !gestureInstance.hasRightGloveData() ||
			!expectedType.getUsesLeftHand() && gestureInstance.hasLeftGloveData()   ||
			!expectedType.getUsesRightHand() && gestureInstance.hasRightGloveData()) {
			return false;
		}
		
		// Check the fierceness of the gesture to ensure it's meeting it's minimum threshold...
		if (gestureInstance.getTotalFierceness() < expectedType.getMinFierceDiffThreshold()) {
			RecognizerManager.logger.info("Fireceness was not great enough to recognize gesture (" + expectedType.toString() + "): " +
				"Required fierceness: " + expectedType.getMinFierceDiffThreshold() +
				", fierceness found: " + gestureInstance.getTotalFierceness());
			return false;
		}
		
		// Check the number of required data points for the gesture type, if the instance doesn't have enough
		// data points for the gesture then we don't count it
		if (gestureInstance.getNumDataPts() < expectedType.getMinNumDataPts()) {
			RecognizerManager.logger.info("Number of data points was not great enough to recognize gesture (" + expectedType.toString() + "): " +
					"Required data pts: " + expectedType.getMinNumDataPts() +
					", data points found: " + gestureInstance.getNumDataPts());
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
		
		this.bestGestureTypeMap.clear();
		this.bestProbabilityMap.clear();
		
		// Setup variables for tracking the probabilities of various gestures
		double currProbability = -1.0;
		for (GestureGenre genre : GestureGenre.values()) {
			this.bestProbabilityMap.put(genre, new Double(-1.0));
		}
		
		// Go through each recognizer - only use recognizers that are built for the correct glove data
		// as is being provided by the gesture instance being recognized. We split the recognition 'bests'
		// into categories based on the genre of the gesture (i.e., "basic", "special", "easter-egg" gestures)
		// Later on, we favour basic gestures over special gestures and special gestures over easter-egg gestures.
		for (Recognizer recognizer : this.recognizerMap.values()) {
			GestureType gestureType = recognizer.getGestureType();
			
			// Make sure the instance meets all the criteria for the current recognizable gesture type
			if (!RecognizerManager.isAcceptableGestureForGivenType(inst, gestureType)) {
				continue;
			}
			
			// Make sure the probability meets the minimum threshold
			currProbability = Math.max(recognizer.probability(inst), recognizer.kMeansProbability(inst));
			if (currProbability < MINIMUM_PROBABILITY_THRESHOLD) {
				this.logger.info("Failed to recognize gesture (" + gestureType.toString() + 
						"), did not meet minimum probability threshold of either base or k-means recognitions.");
				continue;
			}
			
			Double bestProbability = this.bestProbabilityMap.get(gestureType.getGenre());
			if (currProbability > bestProbability) {
				this.bestProbabilityMap.put(gestureType.getGenre(), currProbability);
				this.bestGestureTypeMap.put(gestureType.getGenre(), gestureType);
			}
		}
		
		
		// The best gesture map will now contain all the best candidates for the given instance, from
		// each gesture genre. We need to determine what the differences are so that we make a reasonable
		// choice as to whether we should be using a basic, special or easter-egg type move...

		GestureType bestGesture = null;
		if (this.bestGestureTypeMap.containsKey(GestureGenre.BASIC)) {
			
			double basicGestureBestProb = this.bestProbabilityMap.get(GestureGenre.BASIC);
			
			if (this.bestGestureTypeMap.containsKey(GestureGenre.SPECIAL)) {
				double specialGestureBestProb = this.bestProbabilityMap.get(GestureGenre.SPECIAL);
				if (specialGestureBestProb > basicGestureBestProb && 
					specialGestureBestProb - basicGestureBestProb <= BASIC_SPECIAL_PROB_COMPARISON_THRESHOLD) {
					
					if (this.bestGestureTypeMap.containsKey(GestureGenre.EASTER_EGG)) {
						double easterEggGestureBestProb = this.bestProbabilityMap.get(GestureGenre.EASTER_EGG);
						
						if (easterEggGestureBestProb > specialGestureBestProb &&
							easterEggGestureBestProb - specialGestureBestProb <= SPECIAL_EASTEREGG_PROB_COMPARISON_THRESHOLD) {
							
							bestGesture = this.bestGestureTypeMap.get(GestureGenre.EASTER_EGG);
						}
						else {
							bestGesture = this.bestGestureTypeMap.get(GestureGenre.SPECIAL);
						}
					}
					else {
						bestGesture = this.bestGestureTypeMap.get(GestureGenre.SPECIAL);
					}
				}
				else {
					if (this.bestGestureTypeMap.containsKey(GestureGenre.EASTER_EGG)) {
						double easterEggGestureBestProb = this.bestProbabilityMap.get(GestureGenre.EASTER_EGG);
						
						if (easterEggGestureBestProb > basicGestureBestProb &&
							easterEggGestureBestProb - basicGestureBestProb <= BASIC_EASTEREGG_PROB_COMPARISON_THRESHOLD) {
							
							bestGesture = this.bestGestureTypeMap.get(GestureGenre.EASTER_EGG);
						}
						else {
							bestGesture = this.bestGestureTypeMap.get(GestureGenre.BASIC);
						}
					}
					else {
						bestGesture = this.bestGestureTypeMap.get(GestureGenre.BASIC);
					}
				}
			}
			else {
				if (this.bestGestureTypeMap.containsKey(GestureGenre.EASTER_EGG)) {
					double easterEggGestureBestProb = this.bestProbabilityMap.get(GestureGenre.EASTER_EGG);
					
					if (easterEggGestureBestProb > basicGestureBestProb &&
						easterEggGestureBestProb - basicGestureBestProb <= BASIC_EASTEREGG_PROB_COMPARISON_THRESHOLD) {
						
						bestGesture = this.bestGestureTypeMap.get(GestureGenre.EASTER_EGG);
					}
					else {
						bestGesture = this.bestGestureTypeMap.get(GestureGenre.BASIC);
					}
				}
				else {
					bestGesture = this.bestGestureTypeMap.get(GestureGenre.BASIC);
				}
			}
			
		}
		else if (this.bestGestureTypeMap.containsKey(GestureGenre.SPECIAL)) {
			double specialGestureBestProb = this.bestProbabilityMap.get(GestureGenre.SPECIAL);

			if (this.bestGestureTypeMap.containsKey(GestureGenre.EASTER_EGG)) {
				double easterEggGestureBestProb = this.bestProbabilityMap.get(GestureGenre.EASTER_EGG);
				
				if (easterEggGestureBestProb > specialGestureBestProb &&
					easterEggGestureBestProb - specialGestureBestProb >= SPECIAL_EASTEREGG_PROB_COMPARISON_THRESHOLD) {
					
					bestGesture = this.bestGestureTypeMap.get(GestureGenre.EASTER_EGG);
				}
				else {
					bestGesture = this.bestGestureTypeMap.get(GestureGenre.SPECIAL);
				}
			}
			else {
				bestGesture = this.bestGestureTypeMap.get(GestureGenre.SPECIAL);
			}
			
		}
		else if (this.bestGestureTypeMap.containsKey(GestureGenre.EASTER_EGG)) {
			bestGesture = this.bestGestureTypeMap.get(GestureGenre.EASTER_EGG);
		}
		
		if (bestGesture == null) {
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
			this.logger.info("Ignoring gesture - too short from beginning to end!");
			for (Entry<GestureType, Recognizer> entry : this.recognizerMap.entrySet()) {
				resultMapping.put(entry.getKey(), new GestureProbabilities(0.0, 0.0));
			}
			return new GestureRecognitionResult(inst, resultMapping);
		}
		
		for (Entry<GestureType, Recognizer> entry : this.recognizerMap.entrySet()) {
			
			if (entry.getKey().getUsesLeftHand() && !inst.hasLeftGloveData() ||
				entry.getKey().getUsesRightHand() && !inst.hasRightGloveData() ||
				!entry.getKey().getUsesLeftHand() && inst.hasLeftGloveData() ||
				!entry.getKey().getUsesRightHand() && inst.hasRightGloveData()) {
				
				continue;
			}
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
