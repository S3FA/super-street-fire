package ca.site3.ssf.gesturerecognizer;

/**
 * Immutable container class for representing the probability results of gesture
 * recognition.
 * 
 * @author Callum
 *
 */
final public class GestureProbabilities {
	private final double minLnProbability;
	private final double baseProbability;
	private final double lnProbability;
	
	public GestureProbabilities(double minLnProbability, double baseProbability, double lnProbability) {
		this.minLnProbability = minLnProbability;
		this.baseProbability = baseProbability;
		this.lnProbability = lnProbability;
	}
	public double getMinLnProbability() {
		return this.minLnProbability;
	}
	public double getBaseProbability() {
		return this.baseProbability;
	}
	
	public double getLnProbability() {
		return this.lnProbability;
	}
}
