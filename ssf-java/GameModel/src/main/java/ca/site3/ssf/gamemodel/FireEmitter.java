package ca.site3.ssf.gamemodel;

import java.util.AbstractMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

class FireEmitter {
	
	public enum Location  { LEFT_RAIL, RIGHT_RAIL, OUTER_RING };
	public enum FlameType { ATTACK_FLAME, BLOCK_FLAME, NON_GAME_FLAME };
	
	final public static float MAX_INTENSITY = 1.0f;
	final public static float MIN_INTENSITY = 0.0f;

	final private int index;		    // Unique index within the location of the emitter
	final private Location location;    // The location of the emitter within the game arena (see FireEmitter.Location)
	final private int globalEmitterID;  // Unique identifier among all other fire emitters in the simulation
	
	// Mapping of contributors to this flame emitter
	private AbstractMap<GameModel.Entity, FireEmitterContributor> contributors =
			new HashMap<GameModel.Entity, FireEmitterContributor>(GameModel.Entity.values().length);
	
	FireEmitter(int globalEmitterID, int index, Location location) {
		this.globalEmitterID = globalEmitterID;
		this.index = index;
		this.location = location;
		
		assert(this.index >= 0);
		assert(location != null);
		
		for (GameModel.Entity gameEntity : GameModel.Entity.values()) {
			this.contributors.put(gameEntity, new FireEmitterContributor(gameEntity));
		}
	}
	
	/**
	 * Reset this FireEmitter to be completely off i.e., all intensities of all
	 * contributors are reduced to zero.
	 */
	void reset() {
		for (FireEmitterContributor contributor : this.contributors.values()) {
			contributor.reset();
		}
	}
	
	/**
	 * Sets the intensity for a particular contributor for a particular flame type on this FireEmitter.
	 * @param contributor The player or ringmaster entity contributing to the flame.
	 * @param flameType The type of flame being contributed.
	 * @param intensity The intensity of the flame being contributed.
	 */
	void setIntensity(GameModel.Entity contributor, FireEmitter.FlameType flameType, float intensity) {
		FireEmitterContributor fireEmitterContrib = this.contributors.get(contributor);
		assert(fireEmitterContrib != null);
		fireEmitterContrib.setIntensity(flameType, intensity);
	}
	
	int getIndex() {
		return this.index;
	}
	
	FireEmitter.Location getLocation() {
		return this.location;
	}
	
	
}
