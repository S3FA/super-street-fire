package ca.site3.ssf.gamemodel;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Container for representing a flame-effect/fire emitter in the Super Street Fire game.
 * Holds information about its location within the game and information about the
 * entities and actions contributing towards its overall intensity.
 * 
 * @author Callum
 *
 */
class FireEmitter {
	
	public enum Location  { LEFT_RAIL, RIGHT_RAIL, OUTER_RING };
	public enum FlameType { ATTACK_FLAME, BLOCK_FLAME, NON_GAME_FLAME };
	
	final public static float MAX_INTENSITY = 1.0f;
	final public static float MIN_INTENSITY = 0.0f;

	final private int index;		    // Unique index within the location of the emitter
	final private Location location;    // The location of the emitter within the game arena (see FireEmitter.Location)
	final private int globalEmitterID;  // Unique identifier among all other fire emitters in the simulation
	
	// Mapping of contributors to this flame emitter
	private Map<GameModel.Entity, FireEmitterContributor> contributors =
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
	 * Get the index of this emitter within its location (e.g., if its on the right rail, how
	 * far along the rail is it?).
	 * @return The zero-based index.
	 */
	int getIndex() {
		return this.index;
	}
	
	/**
	 * The location of this emitter within the game arena.
	 * @return The emitter location.
	 */
	FireEmitter.Location getLocation() {
		return this.location;
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
	 * Sets the intensity for a particular contributor for a particular action on this FireEmitter.
	 * @param action The action contributing to the flame.
	 * @param intensity The intensity of the flame being contributed.
	 */
	void setIntensity(Action action, float intensity) {
		FireEmitterContributor fireEmitterContrib = this.contributors.get(action.getContributorEntity());
		assert(fireEmitterContrib != null);
		fireEmitterContrib.setIntensity(action, intensity);
	}
	
	/**
	 * Gets the total, final intensity of this emitter after consideration of all contributors
	 * to its flame.
	 * @return The intensity of this fire emitter.
	 */
	float getIntensity() {
		float totalIntensity = FireEmitter.MIN_INTENSITY;
		for (FireEmitterContributor contributor : this.contributors.values()) {
			totalIntensity = Math.max(totalIntensity, contributor.getResolvedIntensity());
		}
		return totalIntensity;
	}
	
	/**
	 * Determine if this emitter has a simultaneous attack from one player and block from another
	 * player currently being executed on it.
	 * @return true if there is a simultaneous block/attack on this emitter from conflicting players, false if not.
	 */
	boolean hasAttackBlockConflict() {
		EnumSet<FireEmitter.FlameType> player1FlameTypes = this.contributors.get(GameModel.Entity.PLAYER1_ENTITY).getFlameTypes();
		EnumSet<FireEmitter.FlameType> player2FlameTypes = this.contributors.get(GameModel.Entity.PLAYER2_ENTITY).getFlameTypes();
		
		return (player1FlameTypes.contains(FireEmitter.FlameType.ATTACK_FLAME) &&
				player2FlameTypes.contains(FireEmitter.FlameType.BLOCK_FLAME)) ||
		       (player1FlameTypes.contains(FireEmitter.FlameType.BLOCK_FLAME)  &&
		    	player2FlameTypes.contains(FireEmitter.FlameType.ATTACK_FLAME));
	}
	
}
