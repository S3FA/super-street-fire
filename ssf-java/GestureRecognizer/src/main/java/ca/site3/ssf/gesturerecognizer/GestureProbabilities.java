package ca.site3.ssf.gesturerecognizer;

/**
 * Immutable container class for representing the probability results of gesture
 * recognition.
 * 
 * @author Callum
 *
 */
final public class GestureProbabilities {
	private final double baseProbability;
	private final double kMeansProbability;
	
	public GestureProbabilities(double baseProbability, double kMeansProbability) {
		this.baseProbability   = baseProbability;
		this.kMeansProbability = kMeansProbability;
	}
	
	public double getBaseProbability() {
		return this.baseProbability;
	}
	
	public double getKMeansProbability() {
		return this.kMeansProbability;
	}
}
