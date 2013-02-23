package ca.site3.ssf.gamemodel;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Immutable representation for all fire emitters, used when raising events
 * in the event handler interface for the GameModel.
 * @author Callum
 *
 */
@SuppressWarnings("serial")
public final class FireEmitterChangedEvent implements IGameModelEvent {
	
	final private int index;
	final private FireEmitter.Location location;
	final private EnumSet<GameModel.Entity> contributingEntities;
	final private Map<GameModel.Entity, Float> intensities;
	final private float maxIntensity;
	
	public FireEmitterChangedEvent(FireEmitter emitter) {
		super();
		
		this.index     = emitter.getIndex();
		this.location  = emitter.getLocation();
		this.contributingEntities = emitter.getContributingEntities();
		this.intensities = new HashMap<GameModel.Entity, Float>(3);
		
		float maxIntensity = 0;
		for (GameModel.Entity entity : this.contributingEntities) {
			float intensity = emitter.getContributorIntensity(entity);
			this.intensities.put(entity, intensity);
			maxIntensity = Math.max(intensity, this.maxIntensity);
		}
		this.maxIntensity = maxIntensity;
	}
	
	public int getIndex() {
		return this.index;
	}
	public final FireEmitter.Location getLocation() {
		return this.location;
	}
	public float getIntensity(GameModel.Entity entity) {
		if (intensities.containsKey(entity) == false)
			return 0f;
		return this.intensities.get(entity).floatValue();
	}
	public float getMaxIntensity() {
		return this.maxIntensity;
	}
	public final EnumSet<GameModel.Entity> getContributingEntities() {
		return this.contributingEntities;
	}

	public Type getType() {
		return Type.FIRE_EMITTER_CHANGED;
	}
	
}
