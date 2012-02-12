package ca.site3.ssf.gamemodel;

import java.util.ArrayList;
import java.util.Iterator;

import ca.site3.ssf.common.MultiLerp;

/**
 * Abstract superclass for any move/action taken by participants
 * in the Super Street Fire spectacle.
 * @author Callum
 *
 */
abstract class Action {
	
	protected FireEmitterModel fireEmitterModel = null;
	protected ArrayList<ArrayList<FireEmitterSimulator>> wavesOfOrderedFireSims =
			new ArrayList<ArrayList<FireEmitterSimulator>>(2);
	
	Action(FireEmitterModel fireEmitterModel) {
		this.fireEmitterModel = fireEmitterModel;
		assert(this.fireEmitterModel != null);
	}
	
	boolean addFireEmitterBurst(FireEmitterIterator emitterIter, int width, int numBursts, MultiLerp intensityLerp) {
		// Make sure the parameters are at least moderately correct
		if (intensityLerp == null || emitterIter == null || width <= 0 || numBursts <= 0) {
			assert(false);
			return false;
		}
		
		ArrayList<FireEmitterSimulator> newSimWave = new ArrayList<FireEmitterSimulator>(width);
		
		int count = 0;

		// Go through the full wave of simulations required for what has been specified
		// by the parameters and add a simulator for each emitter in the wave
		for (int i = 0; i < width; i++) {
			assert(emitterIter.hasNext());
			FireEmitter currEmitter = emitterIter.next();
			if (currEmitter == null) {
				assert(false);
				return false;
			}
			
			newSimWave.add(new FireEmitterSimulator(this, currEmitter, this.wavesOfOrderedFireSims.size(),
					count, 0.0, numBursts, (MultiLerp)intensityLerp.clone()));
		}

		// Successfully generated a new wave of fire emitter simulators, add it to this action and exit with success!
		this.wavesOfOrderedFireSims.add(newSimWave);
		return true;
	}
	
	/**
	 * Adds a new 'wave' of simulation to this action, for example if a player did a hadouken there would be
	 * two 'waves' of simulation: one on the left rail and one on the right rail going all the way from the first
	 * emitter though to the last emitter of each rail.
	 * 
	 * @param emitterIter The fire emitter iterator that is set to the starting emitter where the wave will begin and
	 * will iterate in the direction of the action.
	 * @param travelLength The total travelling length of the wave (e.g, if there are 8 emitters to a rail and you want
	 * a wave to travel the entire length of the rail, this value would be 8).
	 * @param width The width of the wave (how many emitters will be on, at maximum, simultaneously during the wave's simulation).
	 * @param intensityLerp The multi-linear interpolation that defines what a single fire emitter burst in the wave looks like,
	 * this is a piece-wise linear function describing what each flame will do when it goes on/off. This must meet certain criteria:
	 * - It must start and end with an interpolant value of zero.
	 * - It must start at a time of zero and have a total time length greater than zero
	 * - During the interpolation the interpolant value should hit a value of one at least once.
	 * 
	 * @return true on success, false on failure.
	 */
	boolean addFireEmitterWave(FireEmitterIterator emitterIter, int travelLength, int width, MultiLerp intensityLerp) {
		
		// Make sure the parameters are at least moderately correct
		if (intensityLerp == null || emitterIter == null || travelLength <= 0 || width <= 0 || width > travelLength) {
			assert(false);
			return false;
		}
		
		ArrayList<FireEmitterSimulator> newSimWave = new ArrayList<FireEmitterSimulator>(travelLength);
		
		int count = 0;
		double initialDelayTimeCounter = 0.0;
		
		// Go through the full wave of simulations required for what has been specified
		// by the parameters and add a simulator for each emitter in the wave
		for (int i = 0; i < travelLength; i++) {
			assert(emitterIter.hasNext());
			FireEmitter currEmitter = emitterIter.next();
			if (currEmitter == null) {
				assert(false);
				return false;
			}
			
			newSimWave.add(new FireEmitterSimulator(this, currEmitter, this.wavesOfOrderedFireSims.size(),
					count, initialDelayTimeCounter, width, (MultiLerp)intensityLerp.clone()));
			
			initialDelayTimeCounter += intensityLerp.getTotalTimeLength();
		}

		// Successfully generated a new wave of fire emitter simulators, add it to this action and exit with success!
		this.wavesOfOrderedFireSims.add(newSimWave);
		return true;
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
		for (ArrayList<FireEmitterSimulator> simulatorWave : this.wavesOfOrderedFireSims) {
			for (FireEmitterSimulator simulator : simulatorWave) {
				if (!simulator.isFinished()) {
					return false;
				}
			}
		}
		
		return true;
	}
	
	void kill() {
		for (ArrayList<FireEmitterSimulator> simulatorWave : this.wavesOfOrderedFireSims) {
			for (FireEmitterSimulator simulator : simulatorWave) {
				simulator.kill();
			}
		}
		this.wavesOfOrderedFireSims.clear();
	}
	
	void tick(double dT) {
		for (ArrayList<FireEmitterSimulator> simulatorWave : this.wavesOfOrderedFireSims) {
			for (FireEmitterSimulator simulator : simulatorWave) {
				this.tickSimulator(dT, simulator);
			}
		}
	}
	
	abstract void tickSimulator(double dT, FireEmitterSimulator simulator);
	abstract GameModel.Entity getContributorEntity();
	abstract FireEmitter.FlameType getActionFlameType();
	
}
