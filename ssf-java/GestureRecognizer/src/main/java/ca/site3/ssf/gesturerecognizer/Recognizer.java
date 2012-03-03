package ca.site3.ssf.gesturerecognizer;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import be.ac.ulg.montefiore.run.jahmm.Hmm;
import be.ac.ulg.montefiore.run.jahmm.ObservationVector;
import be.ac.ulg.montefiore.run.jahmm.io.FileFormatException;
import be.ac.ulg.montefiore.run.jahmm.io.HmmReader;
import be.ac.ulg.montefiore.run.jahmm.io.HmmWriter;
import be.ac.ulg.montefiore.run.jahmm.io.OpdfMultiGaussianReader;
import be.ac.ulg.montefiore.run.jahmm.io.OpdfMultiGaussianWriter;

/**
 * Contains the structures and functionality to recognize one particular type of gesture
 * (after sufficient training has occurred).
 * 
 * @author Callum
 *
 */
class Recognizer {

	private GestureType gestureType;
	private Hmm<ObservationVector> recognizer;
	
	/**
	 * Default constructor for Recognizer. Creates an invalid Recognizer.
	 * This should only be used right before reading in a recognizer.
	 */
	Recognizer() {
		this.gestureType = null;
		this.recognizer  = null;
	}
	
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
	 * Saves this recognizer to the given writer.
	 * @param writer The writer to save this to.
	 * @throws IOException Occurs when there's an error while writing.
	 */
	void save(Writer writer) throws IOException {
		writer.write(this.gestureType.name());
		HmmWriter.write(writer, new OpdfMultiGaussianWriter(), this.recognizer);
	}
	
	/**
	 * Loads a recognizer from the given reader.
	 * @param reader The reader to read this from.
	 * @throws IOException Occurs when there's an I/O error while reading.
	 * @throws FileFormatException Occurs when there's a format error while reading.
	 */
	void load(Reader reader) throws IOException, FileFormatException {
		
		String temp = "";
		char[] charArray = new char[1];

		while (GestureType.valueOf(temp) == null) {
			if (reader.read(charArray) == -1) {
				throw new FileFormatException("Reader reached end of stream before recognizer could be read.");
			}
			temp += charArray[0];
		}
		
		this.gestureType = GestureType.valueOf(temp); 
		this.recognizer = HmmReader.read(reader, new OpdfMultiGaussianReader());
		
		assert(this.gestureType != null);
		assert(this.recognizer  != null);
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
