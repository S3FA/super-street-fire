package ca.site3.ssf.fireemittermodel;

/**
 * Contains configuration information for the FireEmitterModel.
 * 
 * @author Callum
 * @author Greg
 * 
 */
final public class FireEmitterConfig {
	
	final private boolean outerRingEnabled;
	final private int numEmittersPerRail;
	final private int numOuterRingEmitters;
	
	public FireEmitterConfig(boolean outerRingEnabled, int numOuterRingEmitters, int numEmittersPerRail) {
		this.outerRingEnabled     = outerRingEnabled;
		this.numOuterRingEmitters = numOuterRingEmitters;
		this.numEmittersPerRail   = numEmittersPerRail;
	}
	
	public boolean isOuterRingEnabled() {
		return this.outerRingEnabled;
	}
	public int getNumOuterRingEmitters() {
		return this.numOuterRingEmitters;
	}
	public int getNumEmittersPerRail() {
		return this.numEmittersPerRail;
	}
	
}
