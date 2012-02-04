package ca.site3.ssf.gamemodel;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;

/**
 * A FlameEmitter has zero or more owners/contributors, up to the total number of game entities, which
 * is currently 3 (player1, player2, and ringmaster). 
 * Each owner contributes different flame types with different intensities. Ultimately
 * an emitter will always have the maximum of all the intensities contributing to it.
 * If a given owner's intensity is greater than the minimum (i.e., zero), this it is
 * considered to be contributing towards the overall emitter and that owner's colour
 * (if it has a colour) will be added to the flame as well.
 * 
 * @author Callum
 *
 */
class FireEmitterContributor {
	
	private GameModel.Entity owner;
	
	// TODO: Change FireEmitter.FlameType map key to be an Action - way more flexible and can allow multiple attacks/blocks/etc.
	// per emitter from the same player.
	
	// Mapping of flame type (nature of the flame) to the intensity of the flame [0,1], 0 is completely off, 1 is completely on.
	private AbstractMap<FireEmitter.FlameType, Float> flameTypeIntensities =
			new HashMap<FireEmitter.FlameType, Float>(FireEmitter.FlameType.values().length);
	
	FireEmitterContributor(GameModel.Entity owner) {
		this.owner = owner;
		this.reset();
	}
	
	/**
	 * Reset the intensity of this contributor's flames to the zero.
	 */
	void reset() {
		for (FireEmitter.FlameType flameType : FireEmitter.FlameType.values()) {
			this.flameTypeIntensities.put(flameType, FireEmitter.MIN_INTENSITY);
		}
	}
	
	void setIntensity(FireEmitter.FlameType flameType, float intensity) {
		if (intensity > FireEmitter.MAX_INTENSITY || intensity < FireEmitter.MIN_INTENSITY) {
			assert(false);
			return;
		}
		
		// Make sure that the ringmaster is not participating in the game flame types and that
		// players are not participating in non-game flame types!
		assert(this.owner != GameModel.Entity.RINGMASTER_ENTITY || flameType == FireEmitter.FlameType.NON_GAME_FLAME);
		assert(this.owner == GameModel.Entity.RINGMASTER_ENTITY || flameType != FireEmitter.FlameType.NON_GAME_FLAME);
		
		this.flameTypeIntensities.put(flameType, intensity);
	}
	
	/**
	 * Get the intensity of the flame associated with a particular type of flame for this contributor.
	 * @param flameType The type of the flame.
	 * @return The intensity for the given flame type.
	 */
	float getIntensity(FireEmitter.FlameType flameType) {
		return this.flameTypeIntensities.get(flameType);
	}
	
	/**
	 * Get the fully resolved 'final' intensity for this emitter (just the maximum of all
	 * the intensities for all flame types for this contributor).
	 * @return The final intensity of the emitter for this contributor.
	 */
	float getResolvedIntensity() {
		float maxIntensity = FireEmitter.MIN_INTENSITY;
		for (Map.Entry<FireEmitter.FlameType, Float> entry : this.flameTypeIntensities.entrySet()) {
			maxIntensity = Math.max(maxIntensity, entry.getValue());
		}
		return maxIntensity;
	}
}