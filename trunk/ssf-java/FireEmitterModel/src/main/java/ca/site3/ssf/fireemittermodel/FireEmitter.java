package ca.site3.ssf.fireemittermodel;

public class FireEmitter {
	
	final public static float MAX_INTENSITY = 1.0f;
	final public static float MIN_INTENSITY = 0.0f;
	private float intensity = FireEmitter.MIN_INTENSITY; // Intensity of the flame [0,1], 0 is completely off, 1 is completely on.
	
	public FireEmitter() {
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
	
	public float getIntensity() {
		return this.intensity;
	}
	
}
