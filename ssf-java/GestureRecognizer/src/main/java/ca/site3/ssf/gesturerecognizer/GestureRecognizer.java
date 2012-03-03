package ca.site3.ssf.gesturerecognizer;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import ca.site3.ssf.gamemodel.IGameModel;

/**
 * Public facade for the gesturerecognizer module. Use this class to perform the following...
 * 
 * 1. Train recognizer(s) for the Super Street Fire game.
 * 2. Write recognizer(s) to file.
 * 3. Read recognizer(s) from file.
 * 4. Execute in real-time using gesture instance data in order to recognizer novel gestures.
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
	
	private RecognizerManager recognizerMgr = null;
	
	public GestureRecognizer() {
		this.recognizerMgr = new RecognizerManager();
	}
	
	// GESTURE TRAINING FUNCTIONALITY ***********************************************************************
	
	/**
	 * Adds training using the given data set for the given gesture to the gesture recognition engine.
	 * @param gestureType The gesture type to train.
	 * @param gestureDataSet The gesture data that will be used for training.
	 */
	public void trainGesture(GestureType gestureType, GestureDataSet gestureDataSet) {
		this.recognizerMgr.train(gestureType, gestureDataSet);
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
	
	// REAL-TIME GESTURE RECOGNITION FUNCTIONALITY **********************************************************
	
	/**
	 * Use the gesture recognizer to recognize a given gesture instance executed by the given player.
	 * This function will both recognize the gesture and, if the gesture is identified, it will execute
	 * that gesture within the given gamemodel.
	 * @param gameModel The game model to execute any identified gesture in.
	 * @param playerNum The player who is executing the gesture.
	 * @param gestureInstance The gesture instance data to recognize.
	 */
	public void recognizePlayerGesture(IGameModel gameModel, int playerNum, GestureInstance gestureInstance) {
		assert(gameModel != null);
		assert(gestureInstance != null);

		// Attempt to recognize the gesture as one of the archetypal SSF gestures...
		GestureType result = this.recognizerMgr.recognize(gestureInstance);
		if (result == null) {
			// No gesture was recognized
			return;
		}
		
		// We have a gesture! Tell the gamemodel about it in order to execute that gesture within
		// the context of the current game
		gameModel.executeGenericAction(gameModel.getActionFactory().buildPlayerAction(playerNum,
				result.getActionFactoryType(), result.getUsesLeftHand(), result.getUsesRightHand()));
	}
	
}
