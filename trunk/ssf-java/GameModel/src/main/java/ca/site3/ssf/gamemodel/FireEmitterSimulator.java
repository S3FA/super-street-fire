package ca.site3.ssf.gamemodel;

import java.util.Collection;

import ca.site3.ssf.common.MultiLerp;


class FireEmitterSimulator {
	
	final private FireEmitter emitter;
	final private Action action;
	
	private double initialDelayCounterInSecs = 0.0;
	
	final private int waveIndex;
	final private int simulatorIndex;
	private int numPlays            = 0;
	private int currNumberOfPlays   = 0;
	private MultiLerp intensityLerp = null;
	
	// Used to track whether an attack from one player went off at the same time as a block
	// from the other player during the last lerp-per-play
	private boolean blockAttackCancellationOccurredOnLastLerp; 
								
	
	FireEmitterSimulator(Action action, FireEmitter emitter, int waveIndex, int simulatorIndex,
						 double initialDelayInSecs, int numPlays, MultiLerp intensityLerpPerPlay) {
		
		this.emitter = emitter;
		assert(emitter != null);
		
		this.action = action;
		assert(action != null);
		
		this.waveIndex = waveIndex;
		this.simulatorIndex = simulatorIndex;
		assert(waveIndex >= 0);
		assert(simulatorIndex >= 0);
		
		this.initialDelayCounterInSecs = initialDelayInSecs;
		assert(initialDelayInSecs >= 0.0);
		
		this.numPlays = numPlays;
		assert(numPlays > 0);
		
		this.intensityLerp = intensityLerpPerPlay;
		assert(intensityLerpPerPlay != null);
		assert(intensityLerpPerPlay.getFirstInterpolantValue() == 0.0);
		assert(intensityLerpPerPlay.getLastInterpolantValue()  == 0.0);
		assert(intensityLerpPerPlay.getFirstTimeValue() == 0.0);
		
		this.currNumberOfPlays = 0;
		this.blockAttackCancellationOccurredOnLastLerp = false;
	}
	
	/**
	 * Query whether this simulator is finished simulating.
	 * @return true if finished simulating, false if not.
	 */
	boolean isFinished() {
		return (this.currNumberOfPlays >= this.numPlays);
	}
	
	/**
	 * Kill this simulator - causes isFinished() to be true and removes all
	 * contributions for the associated action from the simulated emitter.
	 */
	void kill() {
		this.currNumberOfPlays = this.numPlays;
		this.emitter.setIntensity(this.action, 0.0f);
	}
	
	/**
	 * Inform this simulator that the next flame that it will simulate has been blocked and
	 * will therefore not be simulated. What this will do is remove one of the 'plays' of the
	 * emitter in this simulator and extend the initial delay of this simulator.
	 */
	void flameBlocked() {
		this.initialDelayCounterInSecs += this.intensityLerp.getTotalTimeLength();
		this.currNumberOfPlays++;
	}
	
	// The tick function works like the visitor pattern, based on the specific type of action
	// that this simulator is executed with, the appropriate actions will be taken based on
	// the state of the emitter being simulated...
	
	void tick(PlayerAttackAction action, double dT) {
		assert(this.action == action);
		
		boolean currLerpWasNotFinishedBeforeTick = !this.intensityLerp.isFinished();
		this.tickSim(dT);

		// Check for special issues that are specific to player attacks...
		if (currLerpWasNotFinishedBeforeTick && this.intensityLerp.isFinished()) {
			
			// If one player's attack flame was on the same emitter as the other player's block flame
			// then we need to cancel out one of the attack flames...
			// CONSIDERATIONS:
			// - We should wait for the full flame intensity lerp to complete before indicating the cancellation,
			// by doing this we ensure that the full flame occurs for the attack on the emitter where it was blocked
			// - We check the 'blockAttackCancellationOccurredOnLastLerp' flag, which indicates whether a block
			// occurred during the entire interval of the current attack flame's lerp
		    if (this.blockAttackCancellationOccurredOnLastLerp) {
		    	action.blockOccurred(this.waveIndex, this.simulatorIndex);
		    }
			else {
				// If an attack flame succeeded in getting to the opposing player then we need to tell the
				// action about it so that the attackee will be damaged
				
				// Figure out if this.emitter is an emitter that will cause damage to the attackee...
				Collection<FireEmitter> atkDmgEmitters = action.getFireEmitterModel().getDamageEmitters(action.getAttackee().getPlayerNumber());
				for (FireEmitter atkDmgEmitter : atkDmgEmitters) {
					if (this.emitter == atkDmgEmitter) {
						action.attackFlameHitOccurred();
						break;
					}
				}
			}
		}

		this.updateLerp();
	}
	
	void tick(PlayerBlockAction action, double dT) {
		assert(this.action == action);
		
		boolean currLerpWasNotFinishedBeforeTick = !this.intensityLerp.isFinished();
		this.tickSim(dT);
		
		// Check for special issues that are specific to player blocks...
		if (currLerpWasNotFinishedBeforeTick && this.intensityLerp.isFinished()) {
			
			// If the block took place on an emitter that already had an attack on it then a
			// block/attack cancellation occurs...
		    if (this.blockAttackCancellationOccurredOnLastLerp) {
		    	action.blockOccurred();
		    }
		}
		
		this.updateLerp();
	}
	
	void tick(RingMasterAction action, double dT) {
		assert(this.action == action);
		
		this.tickSim(dT);
		this.updateLerp();
		// Don't have to worry about side effects or other such consequences, the ring master's flames
		// do not interact with the game state in any significant way - they're just for show.
	}
	
	/**
	 * Tick the multi-linear interpolation for simulating the flame intensity and then
	 * apply it to the emitter being simulated. This will do nothing if the simulation
	 * is already finished.
	 * @param dT The delta time since the last frame/tick.
	 */
	private void tickSim(double dT) {
		// If we're finished the simulation for this emitter then exit immediately
		if (this.isFinished()) {
			this.emitter.setIntensity(this.action, 0.0f);
			return;
		}
		
		// 
		if (this.initialDelayCounterInSecs >= 0.0) {
			this.initialDelayCounterInSecs -= dT;
			return;
		}
		
		
		float simulatedFlameIntensity = (float)this.intensityLerp.getInterpolantValue();
		this.emitter.setIntensity(this.action, simulatedFlameIntensity);
		this.blockAttackCancellationOccurredOnLastLerp |= this.emitter.hasAttackBlockConflict();
		
		this.intensityLerp.tick(dT);
	}
	
	/**
	 * Update the linear interpolation, should be called sometime soon after tickSim.
	 */
	private void updateLerp() {
		if (this.isFinished()) {
			return;
		}
		
		if (this.intensityLerp.isFinished()) {
			this.currNumberOfPlays++;
			this.intensityLerp.resetLerp();
			this.blockAttackCancellationOccurredOnLastLerp = false;
			
			if (this.isFinished()) {
				this.emitter.setIntensity(this.action, 0.0f);
			}
		}
	}
	
}
