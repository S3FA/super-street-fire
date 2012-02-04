package ca.site3.ssf.gamemodel;

import java.util.ArrayList;

/**
 * Abstract superclass for any move/action taken by participants
 * in the Super Street Fire spectacle.
 * @author Callum
 *
 */
abstract class Action {
	
	
	protected FireEmitterModel fireEmitterModel = null;
	protected ArrayList<FireEmitterSimulator> orderedFireSims = null;
	
	
	//protected double totalDurationInSecs; // How long the action takes effect for, from start to finish
	//protected int width;				    // The width, (in # of emitters) of the effect
	
	//protected double burstDelayInSecs;    // The delay between an emitter turns off and then on again, only applicable when the width is > 1
	                                        // NOTE: This should be a percentage (5-20%?) of the duration per-emitter
	
	
	Action(FireEmitterModel fireEmitterModel) {
		this.fireEmitterModel = fireEmitterModel;
		assert(this.fireEmitterModel != null);
	}
	
	FireEmitterModel getFireEmitterModel() {
		return this.fireEmitterModel;
	}
	
	/**
	 * Whether this action is completed or not.
	 * @return true if the action is done executing, false if not.
	 */
	boolean isFinished() {
		// If all the fire emitter simulations are done then this action is done
		return this.orderedFireSims.isEmpty();
	}
	
	void kill() {
		
	}
	
	//double getDurationPerEmitterWithDelay() { return (this.totalDurationInSecs / (double)totalNumEmitters); }
	//double getDurationPerEmitterWithoutDelay() { return this.getDurationPerEmitterWithDelay() - this.burstDelayInSecs; }
	
	
	abstract void tick(double dT);
	
}
