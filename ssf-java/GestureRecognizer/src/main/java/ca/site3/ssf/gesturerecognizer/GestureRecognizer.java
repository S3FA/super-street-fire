package ca.site3.ssf.gesturerecognizer;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.site3.ssf.gamemodel.Action;
import ca.site3.ssf.gamemodel.ActionFactory;

/**
 * Public facade for the gesturerecognizer module. Use this class to perform the following...
 * 
 * 1. Train the recognizer engine for the Super Street Fire game.
 * 2. Write the recognizer engine to file.
 * 3. Read the recognizer engine from file.
 * 4. Execute in real-time using gesture instance data in order to recognize novel gestures.
 * 
 * NOTE: Performing actions 1, 2 and 3 should be done offline (likely in a separate program from the
 * running game).
 * 
 * This class is mostly a wrapper for the RecognizerManager but it also offers some added functionality
 * for dealing with types that are external to the gesturerecognizer package.
 * 
 * @author Callum
 *
 */
public class GestureRecognizer {
	
	public final static double MINIMUM_GESTURE_RECOGNITION_TIME_IN_SECS  = 0.011;
	public final static double MAXIMUM_GESTURE_RECOGNITION_TIME_IN_SECS  = 4.0;
	
	private RecognizerManager recognizerMgr = null;
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	public GestureRecognizer() {
		this.recognizerMgr = new RecognizerManager();
	}
	
	// GESTURE TRAINING FUNCTIONALITY ***********************************************************************
	
	/**
	 * Adds training using the given data set for the given gesture to the gesture recognition engine.
	 * @param gestureType The gesture type to train.
	 * @param gestureDataSet The gesture data that will be used for training.
	 * @return true on successful training, false on failure.
	 */
	public boolean trainGesture(GestureType gestureType, GestureDataSet gestureDataSet) {
		return this.recognizerMgr.train(gestureType, gestureDataSet);
	}
	public void untrainAndClearGesture(GestureType gestureType) {
		this.recognizerMgr.untrain(gestureType);
	}
	
	// GESTURE VALIDITY FUNCTIONALITY ***********************************************************************
	public static boolean isAcceptableGesture(GestureInstance gestureInstance) {
		return RecognizerManager.isAcceptableGesture(gestureInstance);
	}
	public static boolean isAcceptableGestureForGivenType(GestureInstance gestureInstance, GestureType expectedType) {
		if (!RecognizerManager.isAcceptableGesture(gestureInstance)) {
			return false;
		}
		return RecognizerManager.isAcceptableGestureForGivenType(gestureInstance, expectedType);
	}
	// GESTURE RECOGNIZER I/O (LOADING/SAVING) FUNCTIONALITY ************************************************
	
	/**
	 * Saves the entire gesture recognition 'engine' to the given writer.
	 * @param writer The writer to save to.
	 * @return true on success false on failure.
	 */
	public boolean saveRecognizerEngine(Writer writer) {
		return this.recognizerMgr.writeRecognizers(writer);
	}
	
	/**
	 * Loads the entire gesture recognition 'engine' from the given reader.
	 * @param reader The reader to load from.
	 * @return true on success false on failure.
	 */
	public boolean loadRecognizerEngine(Reader reader) {
		return this.recognizerMgr.readRecognizers(reader);
	}
	
	/**
	 * Clears all of the currently loaded training data.
	 */
	public void clearEngine() {
		this.recognizerMgr.clearRecognizers();
	}
	
	// REAL-TIME GESTURE RECOGNITION FUNCTIONALITY **********************************************************
	
	/**
	 * Use the gesture recognizer to recognize the given player gesture as it would if the game were
	 * being played.
	 * @param gestureInstance The gesture instance to recognize.
	 * @return The winning gesture that would be executed in-game, null if no gesture was suitable.
	 */
	public GestureType recognizePlayerGestureAsGameWould(GestureInstance gestureInstance) {
		return this.recognizerMgr.recognize(gestureInstance, false);
	}
	
	/**
	 * Use the gesture recognizer to recognize the given ringmaster gesture as it would if the game were being played.
	 * @param gestureInstance The gesture instance to recognize.
	 * @return The winning gesture that would be executed in-game, null if no gesture was suitable.
	 */
	public GestureType recognizeRingmasterGestureAsGameWould(GestureInstance gestureInstance) {
		return this.recognizerMgr.recognize(gestureInstance, true);
	}
	
	/**
	 * Use the gesture recognizer to recognize a given gesture instance executed by the given player.
	 * This function will both recognize the gesture and, if the gesture is identified, it will build
	 * the appropriate Action for the gamemodel to consume.
	 * @param actionFactory The game model's action factory, used to construct the gesture action if one is identified.
	 * @param playerNum The player who is executing the gesture.
	 * @param gestureInstance The gesture instance data to recognize.
	 * @return The Action for the game model to consume if one was recognized, if not then null is returned.
	 */
	public Action recognizePlayerGesture(ActionFactory actionFactory, int playerNum, GestureInstance gestureInstance) {
		assert(actionFactory   != null);
		assert(gestureInstance != null);

		// Attempt to recognize the gesture as one of the archetypal SSF gestures...
		GestureType result = this.recognizePlayerGestureAsGameWould(gestureInstance);
		if (result == null) {
			// No gesture was recognized
			this.logger.info("Failed to recognize player gesture.");
			return null;
		}
		
		// We have a gesture! Build the appropriate gesture and return it
		return actionFactory.buildPlayerAction(playerNum, result.getActionFactoryType(),
				result.getUsesLeftHand(), result.getUsesRightHand());
	}
	
	/**
	 * Use the gesture recognizer to recognize a given gesture instance executed by the ringmaster.
	 * This function will both recognize the gesture and, if the gesture is identified, it will build
	 * the appropriate Action for the gamemodel to consume.
	 * @param actionFactory The game model's action factory, used to construct the gesture action if one is identified.
	 * @param gestureInstance The gesture instance data to recognize.
	 * @return The Action for the game model to consume if one was recognized, if not then null is returned.
	 */
	public Action recognizeRingmasterGesture(ActionFactory actionFactory, GestureInstance gestureInstance) {
		// Attempt to recognize the gesture as one of the archetypal SSF gestures...
		GestureType result = this.recognizeRingmasterGestureAsGameWould(gestureInstance);
		if (result == null) {
			// No gesture was recognized
			this.logger.info("Failed to recognize ringmaster gesture.");
			return null;
		}
		
		// We have a gesture! Tell the gamemodel about it in order to execute that gesture within
		// the context of the current game
		return actionFactory.buildRingmasterAction(result.getActionFactoryType(),
				result.getUsesLeftHand(), result.getUsesRightHand());
	}
	
	/**
	 * Use the gesture recognizer to recognize the given gesture and produce a full gesture recognition
	 * result for every gesture that was tested.
	 * @param gestureInstance The gesture instance data to recognize.
	 * @return The full result data from the recognition process.
	 */
	public GestureRecognitionResult recognizePlayerGesture(GestureInstance gestureInstance) {
		assert(gestureInstance != null);

		// Attempt to recognize the gesture as one of the archetypal SSF gestures...
		GestureRecognitionResult result = this.recognizerMgr.recognizeWithFullResult(gestureInstance, false);
		return result;
	}
	public GestureRecognitionResult recognizeRingmasterGesture(GestureInstance gestureInstance) {
		assert(gestureInstance != null);

		// Attempt to recognize the gesture as one of the archetypal SSF gestures...
		GestureRecognitionResult result = this.recognizerMgr.recognizeWithFullResult(gestureInstance, true);
		return result;
	}
	
	public static void main(String[] args) {
		
		// Build a nonsense test data set
		GestureInstance[] gestureInstances = new GestureInstance[20];
		for (int i = 0; i < 20; i++) {
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
			
			gestureInstances[i] = new GestureInstance(leftGloveData, rightGloveData, timeData);
		}
		GestureDataSet dataSet = new GestureDataSet(gestureInstances);		
		
		GestureRecognizer writeRecognizer = new GestureRecognizer();
		for (GestureType type : GestureType.values()) {
			writeRecognizer.trainGesture(type, dataSet);
		}
		
		// Attempt writing the recognizer engine...
		final String fileName = "recognizer_engine.txt";
		try {
			FileWriter fileWriter = new FileWriter(fileName);
			boolean success = writeRecognizer.saveRecognizerEngine(fileWriter);
			fileWriter.close();
			System.out.println("Write success: " + success);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Attempt reading the recognizer engine...
		GestureRecognizer readRecognizer = new GestureRecognizer();
		try {
			FileReader fileReader = new FileReader(fileName);
			boolean success = readRecognizer.loadRecognizerEngine(fileReader);
			System.out.println("Read success: " + success);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
}
