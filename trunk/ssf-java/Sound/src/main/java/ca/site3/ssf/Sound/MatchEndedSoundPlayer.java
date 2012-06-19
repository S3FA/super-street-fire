package ca.site3.ssf.Sound;

import java.util.Properties;

import ca.site3.ssf.gamemodel.IGameModelEvent;
import ca.site3.ssf.gamemodel.MatchEndedEvent;

class MatchEndedSoundPlayer extends SoundPlayer {
	
	MatchEndedSoundPlayer(String resourcePath, Properties configFile) {
		super(resourcePath, configFile);
	}
	
	public int getNumPlays(IGameModelEvent gameModelEvent) {
		return 1;
	}
	
	/**
	 * Handles the sounds based on match ending.
	 */
	public String getAudioResourcePath(IGameModelEvent gameModelEvent) {
		
		if (gameModelEvent == null || gameModelEvent.getType() != IGameModelEvent.Type.MATCH_ENDED) {
			return "";
		}
		
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
		
		return audioFilepath;
	}
}
