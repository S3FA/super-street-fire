package ca.site3.ssf.Sound;

import java.util.Properties;

import ca.site3.ssf.gamemodel.IGameModelEvent;
import ca.site3.ssf.gamemodel.RoundEndedEvent;

class RoundEndedSoundPlayer extends SoundPlayer {
	
	RoundEndedSoundPlayer(String resourcePath, Properties configFile) {
		super(resourcePath, configFile);
	}
	
	// Handle the sounds based on round ending
	public void playSounds(AudioSettings settings, IGameModelEvent gameModelEvent) {
		if (gameModelEvent.getType() != IGameModelEvent.Type.ROUND_ENDED) {
			return;
		}
		
		RoundEndedEvent event = (RoundEndedEvent)gameModelEvent;
		String audioFilepath = "";
		
		switch (event.getRoundResult()) {
		case PLAYER1_VICTORY: {
			audioFilepath = resourcePath + configFile.getProperty("RoundResult.PlayerOneVictory");
			break;
		}
		case PLAYER2_VICTORY: {
			audioFilepath = resourcePath + configFile.getProperty("RoundResult.PlayerTwoVictory");
			break;
		}
		case TIE: {
			audioFilepath = resourcePath + configFile.getProperty("RoundResult.Tie");
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
