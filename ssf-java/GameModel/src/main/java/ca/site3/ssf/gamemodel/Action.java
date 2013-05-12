package ca.site3.ssf.gamemodel;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;

import ca.site3.ssf.common.MultiLerp;
import ca.site3.ssf.gamemodel.FireEmitter.FlameType;

/**
 * Abstract superclass for any move/action taken by participants
 * in Super Street Fire.
 * @author Callum
 *
 */
public abstract class Action {
	
	private boolean firstTickDone = false;
	protected FireEmitterModel fireEmitterModel = null;
	protected ArrayList<ArrayList<FireEmitterSimulator>> wavesOfOrderedFireSims =
			new ArrayList<ArrayList<FireEmitterSimulator>>(2);
	
	Action(FireEmitterModel fireEmitterModel) {
		this.fireEmitterModel = fireEmitterModel;
		assert(this.fireEmitterModel != null);
	}
	
	/**
	 * Merges the given actionToMerge into the collection of activeActions. It does so in an 'intelligent'
	 * way, ensuring that players can't be attacking and blocking simultaneously and other such
	 * game rules/stipulations.
	 * @param activeActions The currently active actions in play.
	 * @param actionToMerge The action that we want to add to play by merging with the existing, active actions.
	 */
	static void mergeAction(Collection<Action> activeActions, Action actionToMerge) {
		
		// An attack flame made by a player that is currently blocking will stop the block flames from showing,
		// A block flame made by a player that is currently blocking will just overwrite the previous block...
		// Regardless, we're going to kill any previous block action made by the player and add the new actionToMerge
		Iterator<Action> iter = activeActions.iterator();
		int numBlocksCancelled = 0;
		while (iter.hasNext()) {
			
			Action action = iter.next();
			if (action.getActionFlameType() == FlameType.BLOCK_FLAME &&
				action.getContributorEntity() == actionToMerge.getContributorEntity()) {
				
				// This is to make sure the block executes at least once (probably not necessary since no
				// one can block and then attack faster than a simulation frame/tick, right?)
				action.tick(0);
				
				// Completely remove the block...
				action.kill();
				iter.remove();
				
				numBlocksCancelled++;
			}
		}
		
		// It shouldn't be possible for multiple blocks to ever be active for a single player!!!
		assert(numBlocksCancelled <= 1);
		
		// If we're dealing with merging a block then we need to inform the block of all the relevant,
		// active attacks that are coming from the other player...
		if (actionToMerge.getActionFlameType() == FlameType.BLOCK_FLAME) {
			iter = activeActions.iterator();
			while (iter.hasNext()) {
				
				Action action = iter.next();
				if (action.getActionFlameType() == FlameType.ATTACK_FLAME &&
					action.getContributorEntity() != actionToMerge.getContributorEntity()) {
					// The action is an attack from the opposite player and is relevant to the block...
					((PlayerBlockAction)actionToMerge).addRelevantIncomingAttackToBlock((PlayerAttackAction)action);
				}
			}
		}
		
		activeActions.add(actionToMerge);
	}
	
	boolean addFireEmitterBurst(FireEmitterIterator emitterIter, int width, int numBursts,
			                    MultiLerp intensityLerp, double delayInSecs) {
		
		// Make sure the parameters are at least moderately correct
		if (intensityLerp == null || emitterIter == null || width <= 0 || numBursts <= 0 || delayInSecs < 0.0) {
			assert(false);
			return false;
		}
		
		ArrayList<FireEmitterSimulator> newBurstSims = new ArrayList<FireEmitterSimulator>(width);
		
		// Go through the full wave of simulations required for what has been specified
		// by the parameters and add a simulator for each emitter in the wave
		for (int i = 0; i < width; i++) {
			assert(emitterIter.hasNext());
			FireEmitter currEmitter = emitterIter.next();
			if (currEmitter == null) {
				assert(false);
				return false;
			}
			
			Deque<MultiLerp> intensityLerps = new ArrayDeque<MultiLerp>(numBursts);
			for (int j = 0; j < numBursts; j++) {
				intensityLerps.add((MultiLerp) intensityLerp.clone());
			}
	
			newBurstSims.add(new FireEmitterSimulator(this, currEmitter,
					this.wavesOfOrderedFireSims.size(), i, delayInSecs, intensityLerps));
		}

		// Successfully generated a new wave of fire emitter simulators, add it to this action and exit with success!
		this.wavesOfOrderedFireSims.add(newBurstSims);
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
	 * @param delayInSecs The delay in seconds before the wave starts.
	 * @return true on success, false on failure.
	 */
	boolean addConstantVelocityFireEmitterWave(FireEmitterIterator emitterIter, int travelLength, int width,
										       MultiLerp intensityLerp, double delayInSecs) {
		
		// Make sure the parameters are at least moderately correct
		if (intensityLerp == null || emitterIter == null || travelLength <= 0 || width <= 0 || width > travelLength || delayInSecs < 0.0) {
			assert(false);
			return false;
		}
		
		ArrayList<FireEmitterSimulator> newSimWave = new ArrayList<FireEmitterSimulator>(travelLength);
		
		double initialDelayTimeCounter = delayInSecs;
		
		// Go through the full wave of simulations required for what has been specified
		// by the parameters and add a simulator for each emitter in the wave
		for (int i = 0; i < travelLength; i++) {
			assert(emitterIter.hasNext());
			FireEmitter currEmitter = emitterIter.next();
			assert(currEmitter != null);
			
			Deque<MultiLerp> intensityLerps = new ArrayDeque<MultiLerp>(width);
			for (int j = 0; j < width; j++) {
				intensityLerps.add((MultiLerp) intensityLerp.clone());
			}
	
			newSimWave.add(new FireEmitterSimulator(this, currEmitter, this.wavesOfOrderedFireSims.size(),
					i, initialDelayTimeCounter, intensityLerps));
			
			initialDelayTimeCounter += intensityLerp.getTotalTimeLength();
		}

		// Successfully generated a new wave of fire emitter simulators, add it to this action and exit with success!
		this.wavesOfOrderedFireSims.add(newSimWave);
		return true;
	}
	
	boolean addFireEmitterWave(FireEmitterIterator emitterIter, int width, List<MultiLerp> intensityLerps, double delayInSecs) {
		// Make sure the parameters are at least moderately correct
		if (emitterIter == null || intensityLerps.isEmpty() || width <= 0 || width > intensityLerps.size()) {
			assert(false);
			return false;
		}
		
		// Build the lerp deques for each of the emitter simulators ...
		List<Deque<MultiLerp>> multiLerpDeques = new ArrayList<Deque<MultiLerp>>(intensityLerps.size());
		for (int i = 0; i < intensityLerps.size(); i++) {
			
			Deque<MultiLerp> lerpDeque = new ArrayDeque<MultiLerp>(width);
			
			for (int j = 0; j < width; j++) {
				if (j + i >= intensityLerps.size()) {
					MultiLerp currMultiLerp = intensityLerps.get(i);
					lerpDeque.push((MultiLerp)currMultiLerp.clone());
				}
				else {
					MultiLerp currMultiLerp = intensityLerps.get(i+j);
					lerpDeque.addLast((MultiLerp)currMultiLerp.clone());
				}
			}
			
			multiLerpDeques.add(lerpDeque);
		}
		
		ArrayList<FireEmitterSimulator> newSimWave = new ArrayList<FireEmitterSimulator>(intensityLerps.size());
		double initialDelayTimeCounter = delayInSecs;
		int currentEmitterIndex = 0;
		
		assert(multiLerpDeques.size() == intensityLerps.size());
		for (int i = 0; i < multiLerpDeques.size(); i++) {
			
			Deque<MultiLerp> currIntensityLerpDeque = multiLerpDeques.get(i);
			assert(currIntensityLerpDeque != null);
			
			assert(emitterIter.hasNext());
			FireEmitter currEmitter = emitterIter.next();
			assert(currEmitter != null);
			
			newSimWave.add(new FireEmitterSimulator(this, currEmitter, this.wavesOfOrderedFireSims.size(),
					currentEmitterIndex, initialDelayTimeCounter, currIntensityLerpDeque));
			
			initialDelayTimeCounter += currIntensityLerpDeque.peek().getTotalTimeLength();
			currentEmitterIndex++;
		}
		
		// Successfully generated a new wave of fire emitter simulators, add it to this action and exit with success!
		this.wavesOfOrderedFireSims.add(newSimWave);
		return true;
	}
	
	FireEmitterModel getFireEmitterModel() {
		return this.fireEmitterModel;
	}	
	
	int getTotalNumFlames() {
		
		int total = 0;
		
		Iterator<ArrayList<FireEmitterSimulator>> arrayIter = this.wavesOfOrderedFireSims.iterator();
		while (arrayIter.hasNext()) {
			ArrayList<FireEmitterSimulator> currArray = arrayIter.next();
			total += currArray.size();
		}
		
		return total;
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
		
		if (!this.firstTickDone) {
			this.onFirstTick();
			this.firstTickDone = true;
		}
		
		// Go through every simulator, tick them, check for ones that are finished and clean them up
		Iterator<ArrayList<FireEmitterSimulator>> arrayIter = this.wavesOfOrderedFireSims.iterator();
		while (arrayIter.hasNext()) {
			
			ArrayList<FireEmitterSimulator> currArray = arrayIter.next();
			Iterator<FireEmitterSimulator> simIter    = currArray.iterator();
			
			while (simIter.hasNext()) {
				FireEmitterSimulator currSimulator = simIter.next();
				if (this.tickSimulator(dT, currSimulator)) {
					
					// Just to make sure it's dead we kill it -- this will ensure that all flames
					// associated with the simulator are killed as well
					currSimulator.kill();
					
					// Remove the simulator from this action
					simIter.remove();
				}
			}
			
			if (currArray.isEmpty()) {
				arrayIter.remove();
			}
			
		}
		
	}
	
	abstract boolean tickSimulator(double dT, FireEmitterSimulator simulator);
	abstract void onFirstTick();
	abstract GameModel.Entity getContributorEntity();
	abstract FireEmitter.FlameType getActionFlameType();
	
}
