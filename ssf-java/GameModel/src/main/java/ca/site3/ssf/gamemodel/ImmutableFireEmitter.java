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
final public class ImmutableFireEmitter {
	
	final private int index;
	final private FireEmitter.Location location;
	final private EnumSet<GameModel.Entity> contributingEntities;
	final private Map<GameModel.Entity, Float> intensities;
	
	public ImmutableFireEmitter(FireEmitter emitter) {
		this.index     = emitter.getIndex();
		this.location  = emitter.getLocation();
		this.contributingEntities = emitter.getContributingEntities();
		this.intensities = new HashMap<GameModel.Entity, Float>(3);
		for (GameModel.Entity entity : this.contributingEntities) {
			this.intensities.put(entity, emitter.getContributorIntensity(entity));
		}
	}
	
	public int getIndex() {
		return this.index;
	}
	public FireEmitter.Location getLocation() {
		return this.location;
	}
	public float getIntensity(GameModel.Entity entity) {
		return this.intensities.get(entity).floatValue();
	}
	public EnumSet<GameModel.Entity> getContributingEntities() {
		return this.contributingEntities;
	}
	
}
