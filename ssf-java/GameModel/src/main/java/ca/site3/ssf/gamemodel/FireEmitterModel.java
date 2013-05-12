package ca.site3.ssf.gamemodel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class FireEmitterModel {
	
	final static int RINGMASTER_6OCLOCK_OUTER_RING_LEFT_EMITTER   = 11;
	final static int RINGMASTER_6OCLOCK_OUTER_RING_RIGHT_EMITTER  = 12;
	final static int RINGMASTER_12OCLOCK_OUTER_RING_LEFT_EMITTER  = 4;
	final static int RINGMASTER_12OCLOCK_OUTER_RING_RIGHT_EMITTER = 3;
	final static int RINGMASTER_3OCLOCK_OUTER_RING_CLOSE_EMITTER  = 15;
	final static int RINGMASTER_3OCLOCK_OUTER_RING_FAR_EMITTER    = 0;
	final static int RINGMASTER_9OCLOCK_OUTER_RING_CLOSE_EMITTER  = 8;
	final static int RINGMASTER_9OCLOCK_OUTER_RING_FAR_EMITTER    = 7;
	
	final private FireEmitterConfig config;
	
	private GameModelActionSignaller actionSignaller = null;
	private Logger logger = null;
	
	private ArrayList<FireEmitter> outerRingEmitters = null; // The outer ring of emitters, starting at the first emitter to the right
															 // of player one's back and moving in a counter-clockwise direction
	
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
	GameModelActionSignaller getActionSignaller() {
		return this.actionSignaller;
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
	
	boolean isDamageEmitter(int damagedPlayerNum, FireEmitter emitter) {
		Collection<FireEmitter> dmgEmitters = this.getDamageEmitters(damagedPlayerNum);
		return dmgEmitters.contains(emitter);
	}
	
	ArrayList<FireEmitter> getPlayerLeftEmitters(int playerNum) {
		switch (playerNum) {
		case 1:
			return this.leftRailEmitters;
		case 2:
			return this.rightRailEmitters;
		default:
			assert(false);
			return null;
		}
	}
	ArrayList<FireEmitter> getPlayerRightEmitters(int playerNum) {
		switch (playerNum) {
		case 1:
			return this.rightRailEmitters;
		case 2:
			return this.leftRailEmitters;
		default:
			assert(false);
			return null;
		}
	}
	
	FireEmitterIterator getPlayerLeftHandStartEmitterIter(int playerNum) {
		switch (playerNum) {
			case 1:
				return new FireEmitterIterator(this.leftRailEmitters, 0, false, false);
			case 2:
				return new FireEmitterIterator(this.rightRailEmitters, this.rightRailEmitters.size(), true, false);
			default:
				assert(false);
				return null;
		}
	}
	FireEmitterIterator getPlayerRightHandStartEmitterIter(int playerNum) {
		switch (playerNum) {
			case 1:
				return new FireEmitterIterator(this.rightRailEmitters, 0, false, false);
			case 2:
				return new FireEmitterIterator(this.leftRailEmitters, this.leftRailEmitters.size(), true, false);
			default:
				assert(false);
				return null;
		}
	}

	FireEmitterIterator getOuterRingStartEmitterIter(int startEmitterIdx, boolean clockwise) {
		return new FireEmitterIterator(this.outerRingEmitters, startEmitterIdx, clockwise, true);
	}
	FireEmitterIterator getLeftRailStartEmitterIter(int startEmitterIdx) {
		return new FireEmitterIterator(this.leftRailEmitters, startEmitterIdx, false, false);
	}
	FireEmitterIterator getRightRailStartEmitterIter(int startEmitterIdx) {
		return new FireEmitterIterator(this.rightRailEmitters, startEmitterIdx, false, false);
	}
	
	/**
	 * Gets the index of the outer ring emitter that's located behind the given player and in a location
	 * relative to that player as described by 'behindAndToTheLeft'. 
	 * @param playerNum The player nearest to the emitter.
	 * @param behindAndToTheLeft The location of the emitter relative to the player (if this is false it is
	 * assumed to be "behind and to the right" of the player.
	 * @return The index of the appropriate outer ring emitter.
	 */
	int getSemanticOuterRingEmitterIndex(int playerNum, boolean behindAndToTheLeft, int distanceAway) {
		int idx = 0;
		if (playerNum == 1) {
			if (behindAndToTheLeft) {
				idx = this.outerRingEmitters.size() - distanceAway;
			}
			else {
				idx = distanceAway;
			}
		}
		else {
			if (behindAndToTheLeft) {
				idx = this.outerRingEmitters.size() / 2 - 1 - distanceAway;
			}
			else {
				idx = (this.outerRingEmitters.size() / 2) + distanceAway;
			}
		}
		
		if (idx > this.outerRingEmitters.size()) {
			return idx % this.outerRingEmitters.size();
		}
		while (idx < 0) {
			idx += this.outerRingEmitters.size();
		}
		return idx;
	}
	
	int getRandomOneSidedOuterRingEmitterIndex(int playerNum, boolean leftSide) {
		Random randomNumGen = new Random();
		int randomHalfNum = 1 + randomNumGen.nextInt(this.outerRingEmitters.size()/2 - 1);
		
		if (playerNum == 1) {
			if (leftSide) {
				return this.outerRingEmitters.size() - randomHalfNum;
			}
			else {
				return 0 + randomHalfNum;
			}
		}
		else {
			if (leftSide) {
				return (this.outerRingEmitters.size() / 2) - randomHalfNum;
			}
			else {
				return ((this.outerRingEmitters.size() / 2) + 1) + randomHalfNum;
			}
		}
	}
	
	
	/**
	 * Resets all of the emitters in the game (both rails and the outer ring). This
	 * effectively reduces the intensity of every emitter to zero, instantly.
	 */
	void resetAllEmitters() {
		this.logger.info("Resetting all fire emitters.");
		
		for (FireEmitter emitter : this.outerRingEmitters) {
			emitter.reset();
		}
		for (FireEmitter emitter : this.leftRailEmitters) {
			emitter.reset();
		}
		for (FireEmitter emitter : this.rightRailEmitters) {
			emitter.reset();
		}
		this.fireAllEmitterChangedEvent();
	}
	
	void fireAllEmitterChangedEvent() {
		this.fireAllOuterRingChangedEvent();
		this.fireAllLeftRailChangedEvent();
		this.fireAllRightRailChangedEvent();
	}
	
	void fireAllOuterRingChangedEvent() {
		for (FireEmitter emitter : this.outerRingEmitters) {
			emitter.fireOnFireEmitterChanged(this.actionSignaller);
		}
	}
	void fireAllRightRailChangedEvent() {
		for (FireEmitter emitter : this.rightRailEmitters) {
			emitter.fireOnFireEmitterChanged(this.actionSignaller);
		}
	}
	void fireAllLeftRailChangedEvent() {
		for (FireEmitter emitter : this.leftRailEmitters) {
			emitter.fireOnFireEmitterChanged(this.actionSignaller);
		}
	}

}
