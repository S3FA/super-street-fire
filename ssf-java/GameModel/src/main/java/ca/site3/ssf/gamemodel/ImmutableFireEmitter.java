package ca.site3.ssf.gamemodel;

import java.util.EnumSet;

/**
 * Immutable representation for all fire emitters, used when raising events
 * in the event handler interface for the GameModel.
 * @author Callum
 *
 */
final public class ImmutableFireEmitter {
	
	final private int index;
	final private FireEmitter.Location location;
	final private float intensity;
	final private EnumSet<GamePlayFireEmitter.EmitterColour> onColours;
	
	public ImmutableFireEmitter(FireEmitter emitter) {
		this.index     = emitter.getIndex();
		this.location  = emitter.getLocation();
		this.intensity = emitter.getIntensity();
		this.onColours = emitter.getOnColours();
	}
	
	public int getIndex() {
		return this.index;
	}
	public FireEmitter.Location getLocation() {
		return this.location;
	}
	public float getIntensity() {
		return this.intensity;
	}
	public EnumSet<GamePlayFireEmitter.EmitterColour> getOnColours() {
		return this.onColours;
	}
	
}
