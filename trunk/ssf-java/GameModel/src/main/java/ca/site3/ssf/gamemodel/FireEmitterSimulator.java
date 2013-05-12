package ca.site3.ssf.gamemodel;

import java.util.Collection;
import java.util.Deque;

import ca.site3.ssf.common.MultiLerp;


class FireEmitterSimulator {
	
	final private FireEmitter emitter;
	final private Action action;
	
	private final double initialDelayInSecs;
	private double initialDelayCounterInSecs = 0.0;
	
	final private int waveIndex;
	final private int simulatorIndex;
	private Deque<MultiLerp> intensityLerps = null;							
	
	FireEmitterSimulator(Action action, FireEmitter emitter, int waveIndex, int simulatorIndex,
						 double initialDelayInSecs, Deque<MultiLerp> intensityLerps) {
		
		this.emitter = emitter;
		assert(emitter != null);
		
		this.action = action;
		assert(action != null);
		
		this.waveIndex = waveIndex;
		this.simulatorIndex = simulatorIndex;
		assert(waveIndex >= 0);
		assert(simulatorIndex >= 0);
		
		this.initialDelayInSecs        = initialDelayInSecs;
		this.initialDelayCounterInSecs = initialDelayInSecs;
		assert(initialDelayInSecs >= 0.0);

		this.intensityLerps = intensityLerps;
		assert(intensityLerps != null);
	}
	
	FireEmitter getEmitter() {
		return this.emitter;
	}
	
	double getInitialDelayInSecs() {
		return this.initialDelayInSecs;
	}
	
	boolean merge(FireEmitterSimulator simToMerge) {

		if (this.waveIndex != simToMerge.waveIndex ||
			this.emitter != simToMerge.emitter ||
			this.simulatorIndex != simToMerge.simulatorIndex) {
			return false;
		}
		
		this.intensityLerps.addAll(simToMerge.intensityLerps);
		return true;
	}
	
	/**
	 * Query whether this simulator is finished simulating.
	 * @return true if finished simulating, false if not.
	 */
	boolean isFinished() {
		return (this.intensityLerps.isEmpty());
	}
	
	/**
	 * Kill this simulator - causes isFinished() to be true and removes all
	 * contributions for the associated action from the simulated emitter.
	 */
	void kill() {
		this.intensityLerps.clear();
		this.emitter.setIntensity(this.action, 0.0f);
	}
	
	/**
	 * Inform this simulator that the next flame that it will simulate has been blocked and
	 * will therefore not be simulated. What this will do is remove one of the 'plays' of the
	 * emitter in this simulator and extend the initial delay of this simulator.
	 */
	void flameBlocked() {
		if (!this.intensityLerps.isEmpty()) {
			MultiLerp intensityLerp = this.intensityLerps.pop();
			this.initialDelayCounterInSecs += intensityLerp.getTimeLeft();
		}
	}
	
	// The tick function works like the visitor pattern, based on the specific type of action
	// that this simulator is executed with, the appropriate actions will be taken based on
	// the state of the emitter being simulated...
	
	void tick(PlayerAttackAction action, double dT) {
		assert(this.action == action);
		if (!this.intensityLerps.isEmpty()) {

			MultiLerp intensityLerp = this.intensityLerps.peek();
			boolean currLerpWasNotFinishedBeforeTick = !intensityLerp.isFinished();
			this.tickSim(dT);
	
			// Check for special issues that are specific to player attacks...
			if (currLerpWasNotFinishedBeforeTick && intensityLerp.isFinished()) {
				

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
		if (!this.intensityLerps.isEmpty()) {
			this.tickSim(dT);
		}
		this.updateLerp();
	}
	
	void tick(RingmasterAction action, double dT) {
		assert(this.action == action);
		
		this.tickSim(dT);
		this.updateLerp();
		// Don't have to worry about side effects or other such consequences, the ring master's flames
		// do not interact with the game state in any significant way - they're just for show.
	}
	
	void tick(CrowdPleaserAction action, double dT) {
		assert(this.action == action);
		
		this.tickSim(dT);
		this.updateLerp();
		// Don't have to worry about side effects or other such consequences, flames
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

		if (this.initialDelayCounterInSecs > 0.0) {
			this.initialDelayCounterInSecs -= dT;
			return;
		}
		
		// This simulator will be keeping track of (and simulating) the linear interpolation of the flame intensity
		// value. That value is then fed to the FireEmitter object (which is responsible for sending events and
		// basic book-keeping).
		MultiLerp intensityLerp = this.intensityLerps.peek();
		float simulatedFlameIntensity = (float)intensityLerp.getInterpolantValue();
		this.emitter.setIntensity(this.action, simulatedFlameIntensity);
		
		intensityLerp.tick(dT);
	}
	
	/**
	 * Update the linear interpolation, should be called sometime soon after tickSim.
	 */
	private void updateLerp() {
		if (this.isFinished()) {
			this.emitter.setIntensity(this.action, 0.0f);
			return;
		}
		
		MultiLerp intensityLerp = this.intensityLerps.peek();
		if (intensityLerp.isFinished()) {
			
			this.intensityLerps.pop();
			if (this.isFinished()) {
				this.emitter.setIntensity(this.action, 0.0f);
			}
		}
	}
	
}
