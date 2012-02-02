package ca.site3.ssf.gamemodel;

import java.util.EnumSet;

import ca.site3.ssf.gamemodel.GamePlayFireEmitter.EmitterColour;

class FireEmitter {
	
	final public static float MAX_INTENSITY = 1.0f;
	final public static float MIN_INTENSITY = 0.0f;

	public enum Location { LEFT_RAIL, RIGHT_RAIL, OUTER_RING };
	
	final protected int index;		      // Unique index within the location of the emitter
	final protected Location location;    // The location of the emitter within the game arena (see FireEmitter.Location)
	final protected int globalEmitterID;  // Unique identifier among all other fire emitters in the simulation
	
	private float intensity = FireEmitter.MIN_INTENSITY;  // Intensity of the flame [0,1], 0 is completely off, 1 is completely on.
	
	FireEmitter(int globalEmitterID, int index, Location location) {
		this.globalEmitterID = globalEmitterID;
		this.index = index;
		this.location = location;
		
		assert(this.index >= 0);
		assert(location != null);
	}
	
	void reset() {
		this.intensity = FireEmitter.MIN_INTENSITY;
	}
	
	void setIntensity(float intensity) {
		if (intensity > FireEmitter.MAX_INTENSITY || intensity < FireEmitter.MIN_INTENSITY) {
			assert(false);
			return;
		}
		
		this.intensity = intensity;
	}
	
	int getIndex() {
		return this.index;
	}
	
	FireEmitter.Location getLocation() {
		return this.location;
	}
	
	float getIntensity() {
		return this.intensity;
	}
	
	EnumSet<EmitterColour> getOnColours() {
		return EnumSet.noneOf(EmitterColour.class);
	}
	
}
