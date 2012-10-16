package ca.site3.ssf.Sound;

public final class AudioSettings {
	private final float volume;
	private final float bgGainFraction;
	
	public AudioSettings(float volume, float bgGainFraction) {
		assert(bgGainFraction >= 0.0 && bgGainFraction <= 1.0);
		this.volume = volume;
		this.bgGainFraction = bgGainFraction;
	}
	
	public float getVolume() {
		return this.volume;
	}
	
	public float getBgGainFraction() {
		return this.bgGainFraction;
	}
}
