package ca.site3.ssf.gamemodel;

import java.util.Map;
import java.util.EnumSet;
import java.util.HashMap;

/**
 * A FlameEmitter has zero or more owners/contributors, up to the total number of game entities, which
 * is currently 3 (player1, player2, and ringmaster). 
 * Each owner contributes different actions, which in turn contribute different intensities. Ultimately
 * an emitter will always have the maximum of all the intensities contributing to it.
 * If a given action's contribution intensity is greater than the minimum (i.e., zero), it is
 * considered to be contributing towards the overall emitter and that owner's colour
 * (if it has a colour) will be added to the flame as well.
 * 
 * @author Callum
 *
 */
class FireEmitterContributor {
	
	private GameModel.Entity owner;
	
	// Mapping of an Action to the intensity of the flame driven by that action
	// NOTE: Once the intensity contribution for a given action is set to FireEmitter.MIN_INTENSITY,
	// it will be removed from this map entirely
	private Map<Action, Float> actionIntensities = new HashMap<Action, Float>(3);
	
	FireEmitterContributor(GameModel.Entity owner) {
		this.owner = owner;
		this.reset();
	}
	
	/**
	 * Reset the intensity of this contributor's flames to the zero.
	 */
	void reset() {
		this.actionIntensities.clear();
	}
	
	GameModel.Entity getContributor() {
		return this.owner;
	}
	
	/**
	 * Set the intensity for an action of this contributor to the given value.
	 * @param action The action whose associated intensity is being changed.
	 * @param intensity The intensity to apply, if FireEmitter.MIN_INTENSITY, the 
	 * action will no longer be contributing.
	 */
	void setIntensity(Action action, float intensity) {
		assert(action != null);
		assert(action.getContributorEntity() == this.owner);
		
		if (intensity > FireEmitter.MAX_INTENSITY || intensity < FireEmitter.MIN_INTENSITY) {
			assert(false);
			return;
		}
		
		// If the set intensity is zero then just remove the action from contributing
		if (intensity == FireEmitter.MIN_INTENSITY) {
			this.actionIntensities.remove(action);
			return;
		}
		
		this.actionIntensities.put(action, intensity);
	}
	
	/**
	 * Get the intensity of the flame associated with a given action.
	 * @param action The action with the associated flame intensity.
	 * @return The intensity for the given flame type.
	 */
	float getIntensity(Action action) {
		assert(action != null);
		if (!this.actionIntensities.containsKey(action)) {
			return FireEmitter.MIN_INTENSITY;
		}
		
		return this.actionIntensities.get(action);
	}
	
	/**
	 * Get the fully resolved 'final' intensity for this emitter (just the maximum of all
	 * the intensities for all actions for this contributor).
	 * @return The final intensity of the emitter for this contributor.
	 */
	float getResolvedIntensity() {
		float maxIntensity = FireEmitter.MIN_INTENSITY;
		for (Float currIntensity : this.actionIntensities.values()) {
			maxIntensity = Math.max(maxIntensity, currIntensity);
		}
		return maxIntensity;
	}
	
	/**
	 * Get the set of contributing flame types from all actions in this contributor.
	 * @return The set of active flame types from this contributor.
	 */
	EnumSet<FireEmitter.FlameType> getFlameTypes() {
		EnumSet<FireEmitter.FlameType> result = EnumSet.noneOf(FireEmitter.FlameType.class);
		for (Action action : this.actionIntensities.keySet()) {
			result.add(action.getActionFlameType());
		}
		return result;
	}
	
}