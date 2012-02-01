package ca.site3.ssf.gamemodel;

import java.util.EnumSet;

import ca.site3.ssf.gamemodel.GamePlayFireEmitter.EmitterColour;

public class FireEmitter {
	
	final public static float MAX_INTENSITY       = 1.0f;
	final public static float MIN_INTENSITY       = 0.0f;
	final public static int INVALID_EMITTER_ID    = -1;
	final public static int INVALID_EMITTER_INDEX = -1;
	
	public enum Location { LEFT_RAIL, RIGHT_RAIL, OUTER_RING };
	
	
	private int globalEmitterID = FireEmitter.INVALID_EMITTER_ID;  // Unique identifier among all other fire emitters in the simulation
	private int index           = FireEmitter.INVALID_EMITTER_INDEX;
	private float intensity     = FireEmitter.MIN_INTENSITY;       // Intensity of the flame [0,1], 0 is completely off, 1 is completely on.
	private Location location   = null;
	
	public FireEmitter(int globalEmitterID, int index, Location location) {
		this.globalEmitterID = globalEmitterID;
		this.index = index;
		this.location = location;
		
		assert(this.globalEmitterID != FireEmitter.INVALID_EMITTER_ID);
		assert(this.index >= 0);
		assert(this.location != null);
	}
	
	public void reset() {
		this.intensity = FireEmitter.MIN_INTENSITY;
	}
	
	public void setIntensity(float intensity) {
		if (intensity > FireEmitter.MAX_INTENSITY || intensity < FireEmitter.MIN_INTENSITY) {
			assert(false);
			return;
		}
		
		this.intensity = intensity;
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
	
	public EnumSet<EmitterColour> getOnColours() {
		return EnumSet.noneOf(EmitterColour.class);
	}
	
}
