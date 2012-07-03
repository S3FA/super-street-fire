package ca.site3.ssf.gesturerecognizer;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import be.ac.ulg.montefiore.run.jahmm.Hmm;
import be.ac.ulg.montefiore.run.jahmm.ObservationVector;
import be.ac.ulg.montefiore.run.jahmm.OpdfMultiGaussian;
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

	private static Logger logger = LoggerFactory.getLogger(JahmmConverter.class);
	private GestureType gestureType;
	private Hmm<ObservationVector> recognizer;
	
	private double lowestLnProbability  = -Double.MAX_VALUE;
	private double highestLnProbability = -Double.MAX_VALUE;
	
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
	
	double getLowestAcceptableLnProbability() {
		return this.lowestLnProbability + ((this.highestLnProbability - this.lowestLnProbability) / 4.0);
	}
	
	/**
	 * Trains the recognizer for this gesture with the given data set.
	 * @param dataSet The data set used to train the recognizer.
	 * @return true on successful training, false on failure.
	 */
	boolean train(GestureDataSet dataSet) {
		if (dataSet.getNumGestureInstances() == 0) {
			return false;
		}
		
		assert(dataSet != null);
		assert(dataSet.getGestureInstanceAt(0).getTrainingDataObservationWidth() == this.gestureType.getNumHands()*3);
		
		// Clean up the data set based on the gesture type...
		dataSet.cleanUpForGestureType(this.gestureType);
		
		if (this.recognizer != null) {
			this.trainMore(dataSet);
		}
		else {
			this.recognizer = JahmmConverter.buildKMeansHMMWithTraining(dataSet, this.gestureType.getNumHmmNodes());
			if (this.recognizer == null) {
				return false;
			}
			else {
				for (int i = 0; i < this.recognizer.nbStates(); i++) {
					OpdfMultiGaussian obsvec = (OpdfMultiGaussian)this.recognizer.getOpdf(i);
					double mean[] = obsvec.mean();
					for (int j = 0; j < mean.length; j++) {
						if (Double.isNaN(mean[j]) || Double.isInfinite(mean[j])) {
							this.recognizer = null;
							return false;
						}
					}
				}
			}
		}
		
		// Output the probabilities based on all the training data and also acquire the
		// lowest probability of all training data
		logger.debug("Probabilities of data set in recognizer: ");
		this.lowestLnProbability  = Double.MAX_VALUE;
		this.highestLnProbability = -Double.MAX_VALUE;
		for (int i = 0; i < dataSet.getNumGestureInstances(); i++) {
			
			double lnProb = this.lnProbability(dataSet.getGestureInstanceAt(i));
			if (lnProb < this.lowestLnProbability) {
				this.lowestLnProbability = lnProb;
			}
			if (lnProb > this.highestLnProbability) {
				this.highestLnProbability = lnProb;
			}
			
			logger.debug("Index " + i + ": (Base Probability: "+ this.probability(dataSet.getGestureInstanceAt(i)) +
					", ln Probability: " + lnProb);
		}
		
		return true;
	}
	
	private boolean failsBasicTestBeforeProbabilityCheck(GestureInstance inst) {
		assert(inst != null);
		
		if (this.recognizer == null) {
			return true;
		}
		if (inst.getNumDataPts() < this.gestureType.getNumHmmNodes()) {
			return true;
		}

		if (inst.getTrainingDataObservationWidth() != this.gestureType.getNumHands()*3) {
			return true;
		}
		
		return false;
	}
	
	double probability(GestureInstance inst) {
		assert(inst != null);
		
		if (this.failsBasicTestBeforeProbabilityCheck(inst)) {
			return 0.0;
		}

		List<ObservationVector> sequence = JahmmConverter.gestureInstanceToObservationSequence(inst);
		return this.recognizer.probability(sequence, this.recognizer.mostLikelyStateSequence(sequence));
	}
	
	double lnProbability(GestureInstance inst) {
		assert(inst != null);
		
		if (this.failsBasicTestBeforeProbabilityCheck(inst)) {
			return 0.0;
		}

		List<ObservationVector> sequence = JahmmConverter.gestureInstanceToObservationSequence(inst);
		return this.recognizer.lnProbability(sequence);
	}
	
	/**
	 * Saves this recognizer to the given writer.
	 * @param writer The writer to save this to.
	 * @throws IOException Occurs when there's an error while writing.
	 */
	void save(Writer writer) throws IOException {
		assert(this.gestureType != null);
		
		writer.write(this.gestureType.name());
		
		if (this.recognizer != null) {
			writer.write(" " + this.lowestLnProbability + " " + this.highestLnProbability + " 1 \n");
			HmmWriter.write(writer, new OpdfMultiGaussianWriter(), this.recognizer);
		}
		else {
			writer.write(" " + this.lowestLnProbability + " " + this.highestLnProbability + " 0 \n");
		}
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

		// Attempt to read the gesture type
		GestureType readType = null;
		do {
			try {
				readType = GestureType.valueOf(GestureType.class, temp);
			}
			catch (IllegalArgumentException ex) {
				if (reader.read(charArray) == -1) {
					throw new FileFormatException("Reader reached end of stream before recognizer could be read.");
				}
				if (!Character.isSpaceChar(charArray[0]) && charArray[0] != '\n' && charArray[0] != '\r') {
					temp += charArray[0];
				}
			}
		}
		while (readType == null);
		this.gestureType = readType;
		
		// Skip the space character
		reader.skip(1);
		
		// Read the lowest ln probability for this recognizer
		temp = "";
		for(;;) {
			if (reader.read(charArray) == -1) {
				throw new FileFormatException("Reader reached end of stream before recognizer could be read.");
			}
			if (Character.isSpaceChar(charArray[0])) {
				break;
			}
			
			temp += charArray[0];
		}
		
		try {
			this.lowestLnProbability = Double.parseDouble(temp);
		}
		catch (NumberFormatException e) {
			throw new FileFormatException("Failed to parse highest probability in recognizer.");
		}
		// Read the highest ln probability for this recognizer
		temp = "";
		for(;;) {
			if (reader.read(charArray) == -1) {
				throw new FileFormatException("Reader reached end of stream before recognizer could be read.");
			}
			if (Character.isSpaceChar(charArray[0])) {
				break;
			}
			
			temp += charArray[0];
		}
		
		try {
			this.highestLnProbability = Double.parseDouble(temp);
		}
		catch (NumberFormatException e) {
			throw new FileFormatException("Failed to parse highest probability in recognizer.");
		}
		
		// Read whether or not there is a HMM for this recognizer in the file
		if (reader.read(charArray)== -1) {
			throw new FileFormatException("Reader reached end of stream before recognizer could be read.");
		}
		reader.skip(1); // skip the newline
		
		if (charArray[0] == '0') {
			this.recognizer = null;
		}
		else {
			// Attempt to read the recognizer...
			try {
				this.recognizer = HmmReader.read(reader, new OpdfMultiGaussianReader());
			}
			catch (FileFormatException ex) {
				this.recognizer = null;
				throw ex;
			}
		}
	}
	
	/**
	 * Allows the existing recognizer for the gesture to learn more from another data set.
	 * @param dataSet The data set to learn more from.
	 */
	private void trainMore(GestureDataSet dataSet) {
		assert(this.recognizer != null);
		this.recognizer = JahmmConverter.trainHMM(this.recognizer, dataSet);
		assert(this.recognizer != null);
	}
}
