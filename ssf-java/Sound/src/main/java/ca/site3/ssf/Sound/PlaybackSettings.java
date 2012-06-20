package ca.site3.ssf.Sound;

final class PlaybackSettings {

	static final int INFINITE_NUM_PLAYS = 0;
	
	static final float BALANCED_PAN = 0.0f;
	static final float PLAYER_1_PAN = -1.0f;
	static final float PLAYER_2_PAN = 1.0f;
	
	private final float volume;
	private final float pan;
	private final int numPlays;
	
	PlaybackSettings(float vol, float pan, int numPlays) {
		this.volume = vol;
		this.pan    = pan;
		this.numPlays = numPlays;
	}
	
	float getVolume() {
		return this.volume;
	}
	float getPan() {
		return this.pan;
	}
	int getNumPlays() {
		return this.numPlays;
	}

	static float getPlayerPan(int playerNum) {
		if (playerNum == 1) {
			return PLAYER_1_PAN;
		}
		return PLAYER_2_PAN;
	}
}
