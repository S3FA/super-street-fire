package ca.site3.ssf.Sound;

class PlaybackSettings {

	private float volume;	
	private boolean isLooping;
	private boolean isQuietBackground;
	
	PlaybackSettings(float vol, boolean loop, boolean quietBackgroundMusic){
		this.volume = vol;
		this.isLooping = loop;
		this.isQuietBackground = quietBackgroundMusic;
	}
	
	void setVolume(float vol) {
		this.volume = vol;
	}

	float getVolume() {
		return this.volume;
	}	
	
	void setIsLooping(boolean loop) {
		this.isLooping = loop;
	}
	
	boolean getIsLooping() {
		return this.isLooping;
	}
	
	void setIsQuietBackground(boolean quiet) {
		this.isQuietBackground = quiet;
	}
	
	boolean getIsQuietBackgground() {
		return this.isQuietBackground;
	}
}
