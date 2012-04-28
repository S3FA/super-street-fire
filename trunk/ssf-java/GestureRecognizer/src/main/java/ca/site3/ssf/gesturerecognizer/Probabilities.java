package ca.site3.ssf.gesturerecognizer;

final public class Probabilities implements Comparable<Double>{
	private final double baseProbability;
	private final double kMeansProbability;
	
	public Probabilities(double baseProbability, double kMeansProbability) {
		this.baseProbability   = baseProbability;
		this.kMeansProbability = kMeansProbability;
	}
	
	public double getBaseProbability() {
		return this.baseProbability;
	}
	
	public double getKMeansProbability() {
		return this.kMeansProbability;
	}

	public int compareTo(Double o) {
		if (this.baseProbability < o.doubleValue()) {
			return -1;
		}
		else if (this.baseProbability > o.doubleValue()) {
			return 1;
		}
		else {
			return 0;
		}
	}
}
