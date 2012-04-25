package ca.site3.ssf.gesturerecognizer;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import be.ac.ulg.montefiore.run.jahmm.CentroidObservationVector;
import be.ac.ulg.montefiore.run.jahmm.ForwardBackwardCalculator;
import be.ac.ulg.montefiore.run.jahmm.ForwardBackwardScaledCalculator;
import be.ac.ulg.montefiore.run.jahmm.Hmm;
import be.ac.ulg.montefiore.run.jahmm.KMeansCalculator;
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
		
		logger.debug("Probabilities of data set in recognizer: ");
		for (int i = 0; i < dataSet.getNumGestureInstances(); i++) {
			logger.debug("Index " + i + ": "+ this.probability(dataSet.getGestureInstanceAt(i)));
		}
		
		return true;
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
		int observationVecSize = this.gestureType.getNumHands()*3;
		if (inst.getTrainingDataObservationWidth() != observationVecSize) {
			return 0.0;
		}
		
		List<ObservationVector> sequence = JahmmConverter.gestureInstanceToObservationSequence(inst);
		//return this.recognizer.probability(sequence, this.recognizer.mostLikelyStateSequence(sequence));
		
		KMeansCalculator<ObservationVector> kMeansCalc = new KMeansCalculator<ObservationVector>(this.gestureType.getNumHmmNodes(), sequence);
		List<ObservationVector> centroidSequence = new ArrayList<ObservationVector>(kMeansCalc.nbClusters());
		for (int i = 0; i < kMeansCalc.nbClusters(); i++) {
			Collection<ObservationVector> cluster = kMeansCalc.cluster(i);
			
			ObservationVector avg = new ObservationVector(observationVecSize);
			for (ObservationVector v : cluster) {
				avg = avg.plus(v);
			}
			avg = avg.times(1.0 / (double)cluster.size());
			centroidSequence.add(avg);
		}
		return this.recognizer.probability(centroidSequence, this.recognizer.mostLikelyStateSequence(centroidSequence));
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
			writer.write(" 1\n");
			HmmWriter.write(writer, new OpdfMultiGaussianWriter(), this.recognizer);
		}
		else {
			writer.write(" 0\n");
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
				if (!Character.isSpaceChar(charArray[0]) && charArray[0] != '\n') {
					temp += charArray[0];
				}
			}
		}
		while (readType == null);
		this.gestureType = readType;
		
		// Skip the space character
		reader.skip(1);
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
