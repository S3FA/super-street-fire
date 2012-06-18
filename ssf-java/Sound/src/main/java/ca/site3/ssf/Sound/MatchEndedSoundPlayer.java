package ca.site3.ssf.Sound;

import java.util.Properties;

import ca.site3.ssf.gamemodel.IGameModelEvent;
import ca.site3.ssf.gamemodel.MatchEndedEvent;

class MatchEndedSoundPlayer extends SoundPlayer {
	
	MatchEndedSoundPlayer(String resourcePath, Properties configFile) {
		super(resourcePath, configFile);
	}
	
	/**
	 * Handles the sounds based on match ending.
	 */
	public void playSounds(AudioSettings settings, IGameModelEvent gameModelEvent) {
		
		MatchEndedEvent event = (MatchEndedEvent)gameModelEvent;
		String audioFilepath = "";
		
		switch (event.getMatchResult()) {
		case PLAYER1_VICTORY: {
			audioFilepath = this.resourcePath + this.configFile.getProperty("MatchResult.PlayerOneVictory");
			break;
		}
		case PLAYER2_VICTORY: {
			audioFilepath = this.resourcePath + this.configFile.getProperty("MatchResult.PlayerTwoVictory");
			break;
		}
		default:
			assert(false);
			break;
		}
		
		if (audioFilepath.isEmpty()) {
			return;
		}
		
		new Thread(new PlaybackHandler(audioFilepath, 1, settings.getVolume())).start();
	}
}
