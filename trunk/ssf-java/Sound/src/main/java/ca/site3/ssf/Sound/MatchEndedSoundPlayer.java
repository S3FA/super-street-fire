package ca.site3.ssf.Sound;

import java.util.Properties;

import ca.site3.ssf.gamemodel.IGameModelEvent;
import ca.site3.ssf.gamemodel.MatchEndedEvent;

class MatchEndedSoundPlayer extends SoundPlayer {
	
	private PlaybackHandler p1VictoryPlayback;
	private PlaybackHandler p2VictoryPlayback;
		
	MatchEndedSoundPlayer(SoundPlayerController controller) {
		super(controller);
		
		Properties configProperties = controller.getConfigProperties();
		PlaybackSettings playbackSettings = getDefaultPlaybackSettings();

		this.p1VictoryPlayback = PlaybackHandler.build(controller, configProperties.getProperty("MatchResult.PlayerOneVictory"), playbackSettings);
		this.p2VictoryPlayback = PlaybackHandler.build(controller, configProperties.getProperty("MatchResult.PlayerTwoVictory"), playbackSettings);
	
		// Stop all other controller sounds...
		controller.stopAllSounds();
	}
	
	// Get the default playback settings for this sound player
	private PlaybackSettings getDefaultPlaybackSettings()
	{
		return new PlaybackSettings(controller.getAudioSettings().getVolume(), false, false);
	}
	
	public PlaybackHandler getAudioPlaybackHandler(IGameModelEvent gameModelEvent) {
		if (gameModelEvent == null || gameModelEvent.getType() != IGameModelEvent.Type.MATCH_ENDED) {
			return null;
		}
		
		MatchEndedEvent event = (MatchEndedEvent)gameModelEvent;
		
		switch (event.getMatchResult()) {
		case PLAYER1_VICTORY: {
			return this.p1VictoryPlayback;
		}
		case PLAYER2_VICTORY: {
			return this.p2VictoryPlayback;
		}
		default:
			assert(false);
			break;
		}
		
		return null;
	}
	
	public boolean isBackgroundSoundPlayer(IGameModelEvent gameModelEvent) {
		return false;
	}
}
