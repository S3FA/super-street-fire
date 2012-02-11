package ca.site3.ssf.gamemodel;

import java.util.ArrayList;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class FireEmitterModel {
	
	final private FireEmitterConfig config;
	
	private GameModelActionSignaller actionSignaller = null;
	private Logger logger = null;
	
	private ArrayList<FireEmitter> outerRingEmitters = null; // The outer ring of emitters, starting at the first emitter to the right
															 // of player one's back
	
	private ArrayList<FireEmitter> leftRailEmitters  = null; // Left rail is the rail of emitters on the left from player one's perspective,
														     // they are ordered starting at player one going towards player two
	
	private ArrayList<FireEmitter> rightRailEmitters = null; // Right rail is the rail of emitters on the right from player one's perspective,
															 // they are ordered starting at player one going towards player two
	
	FireEmitterModel(FireEmitterConfig config, GameModelActionSignaller actionSignaller) {
		this.logger = LoggerFactory.getLogger(getClass());
		
		this.config = config;
		assert(this.config != null);
	
		this.actionSignaller = actionSignaller;
		assert(this.actionSignaller != null);
		
		int globalEmitterIDCounter = 0;
		
		// Setup the outer ring emitters...
		if (this.config.isOuterRingEnabled()) {
			this.outerRingEmitters = new ArrayList<FireEmitter>(this.config.getNumOuterRingEmitters());
			for (int i = 0; i < this.config.getNumOuterRingEmitters(); i++) {
				this.outerRingEmitters.add(new FireEmitter(globalEmitterIDCounter++, i, FireEmitter.Location.OUTER_RING));
			}
		}
		
		// Setup the two inner rails of gameplay emitters on either side of the players...
		this.leftRailEmitters  = new ArrayList<FireEmitter>(this.config.getNumEmittersPerRail());
		this.rightRailEmitters = new ArrayList<FireEmitter>(this.config.getNumEmittersPerRail());
		for (int i = 0; i < this.config.getNumEmittersPerRail(); i++) {
			this.leftRailEmitters.add(new FireEmitter(globalEmitterIDCounter++, i, FireEmitter.Location.LEFT_RAIL));
		}
		for (int i = 0; i < this.config.getNumEmittersPerRail(); i++) {
			this.rightRailEmitters.add(new FireEmitter(globalEmitterIDCounter++, i, FireEmitter.Location.RIGHT_RAIL));
		}
	}
	
	FireEmitterConfig getConfig() {
		return this.config;
	}
	
	FireEmitter getOuterRingEmitter(int index, boolean wrapAround) {
		if (!this.config.isOuterRingEnabled()) {
			return null;
		}
		
		int actualIndex = index;
		if (wrapAround) {
			actualIndex %= this.outerRingEmitters.size();
		}
		
		if (actualIndex >= this.outerRingEmitters.size() || actualIndex < 0) {
			return null;
		}
		
		return this.outerRingEmitters.get(actualIndex);
	}
	
	FireEmitter getLeftRailEmitter(int index) {
		if (index >= this.leftRailEmitters.size() || index < 0) {
			return null;
		}
		return this.leftRailEmitters.get(index);
	}
	
	FireEmitter getRightRailEmitter(int index) {
		if (index >= this.rightRailEmitters.size() || index < 0) {
			return null;
		}
		return this.rightRailEmitters.get(index);
	}
	
	FireEmitter getEmitter(FireEmitter.Location location, int index) {
		switch (location) {
			case LEFT_RAIL:
				return this.getLeftRailEmitter(index);
			case RIGHT_RAIL:
				return this.getRightRailEmitter(index);
			case OUTER_RING:
				return this.getOuterRingEmitter(index, true);
			default:
				assert(false);
				break;
		}
		
		return null;
	}
	
	/**
	 * Get the emitters that once an attack flame has executed on them, will cause damage
	 * to the given player.
	 * @param playerNum The player who will be damaged by attacks on the returned emitters.
	 * @return The emitters that can damage the given player when an attack flame passes over them.
	 */
	Collection<FireEmitter> getDamageEmitters(int playerNum) {
		Collection<FireEmitter> result = new ArrayList<FireEmitter>(2);
		assert(playerNum == 1 || playerNum == 2);
		switch (playerNum) {
			case 1:
				result.add(this.leftRailEmitters.get(0));
				result.add(this.rightRailEmitters.get(0));
				break;
			case 2:
				result.add(this.leftRailEmitters.get(this.leftRailEmitters.size()-1));
				result.add(this.rightRailEmitters.get(this.rightRailEmitters.size()-1));
				break;
			default:
				assert(false);
				break;
		}
		
		return result;
	}
	
	
	FireEmitterIterator getPlayerLeftHandStartEmitterIter(int playerNum) {
		assert(playerNum == 1 || playerNum == 2);
		switch (playerNum) {
			case 1:
				return new FireEmitterIterator(this.leftRailEmitters, 0, false);
			case 2:
				return new FireEmitterIterator(this.rightRailEmitters, this.rightRailEmitters.size(), true);
			default:
				assert(false);
				return null;
		}
	}
	FireEmitterIterator getPlayerRightHandStartEmitterIter(int playerNum) {
		assert(playerNum == 1 || playerNum == 2);
		switch (playerNum) {
			case 1:
				return new FireEmitterIterator(this.rightRailEmitters, 0, false);
			case 2:
				return new FireEmitterIterator(this.leftRailEmitters, this.leftRailEmitters.size(), true);
			default:
				assert(false);
				return null;
		}
	}
	
	
	/**
	 * Resets all of the emitters in the game (both rails and the outer ring). This
	 * effectively reduces the intensity of every emitter to zero, instantly.
	 */
	void resetAllEmitters() {
		for (FireEmitter emitter : this.leftRailEmitters) {
			emitter.reset();
			this.actionSignaller.fireOnFireEmitterChanged(emitter);
		}
		for (FireEmitter emitter : this.rightRailEmitters) {
			emitter.reset();
			this.actionSignaller.fireOnFireEmitterChanged(emitter);
		}
		for (FireEmitter emitter : this.outerRingEmitters) {
			emitter.reset();
			this.actionSignaller.fireOnFireEmitterChanged(emitter);
		}
	}
	

}
