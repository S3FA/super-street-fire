package ca.site3.ssf.gamemodel;

/**
 * Abstract superclass for any move/action taken by participants
 * in the Super Street Fire game.
 * @author Callum
 *
 */
public abstract class Action {
	
	protected FireEmitterModel fireEmitterModel = null;
	
	//protected double totalDurationInSecs; // How long the action takes effect for, from start to finish
	//protected int width;				    // The width, (in # of emitters) of the effect
	
	//protected double burstDelayInSecs;    // The delay between an emitter turns off and then on again, only applicable when the width is > 1
	                                        // NOTE: This should be a percentage (5-20%?) of the duration per-emitter
	
	
	public Action(FireEmitterModel fireEmitterModel) {
		this.fireEmitterModel = fireEmitterModel;
		assert(this.fireEmitterModel != null);
	}
	
	//double getDurationPerEmitterWithDelay() { return (this.totalDurationInSecs / (double)totalNumEmitters); }
	//double getDurationPerEmitterWithoutDelay() { return this.getDurationPerEmitterWithDelay() - this.burstDelayInSecs; }
	
	
	abstract void tick(double dT);
	
}
