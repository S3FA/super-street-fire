package ca.site3.ssf.Sound;

import java.util.Properties;

import ca.site3.ssf.gamemodel.IGameModelEvent;
import ca.site3.ssf.gamemodel.RoundEndedEvent;
import ca.site3.ssf.gamemodel.RoundEndedEvent.RoundResult;

class RoundEndedSoundPlayer extends SoundPlayer {
	
	private PlaybackHandler p1VictoryAudioHandler;
	private PlaybackHandler p2VictoryAudioHandler;
	private PlaybackHandler tieAudioHandler;
	
	RoundEndedSoundPlayer(SoundPlayerController controller, RoundResult roundResult) {
		super(controller);
		
		Properties configProperties = controller.getConfigProperties();
		PlaybackSettings playbackSettings = getDefaultPlaybackSettings();

		this.p1VictoryAudioHandler = PlaybackHandler.build(controller, configProperties.getProperty("RoundResult.PlayerOneVictory"), playbackSettings);
		this.p2VictoryAudioHandler = PlaybackHandler.build(controller, configProperties.getProperty("RoundResult.PlayerTwoVictory"), playbackSettings);
		this.tieAudioHandler = PlaybackHandler.build(controller, configProperties.getProperty("RoundResult.Tie"), playbackSettings);
		
		// Stop all other controller sounds...
		controller.stopAllSounds();
	}
	
	// Get the default playback settings for this sound player
	private PlaybackSettings getDefaultPlaybackSettings()
	{
		return new PlaybackSettings(controller.getAudioSettings().getVolume(), false, false);
	}
	
	public PlaybackSettings getPlaybackSettings(AudioSettings globalSettings, IGameModelEvent gameModelEvent) {
		assert(globalSettings != null);
		return new PlaybackSettings(globalSettings.getVolume(), false, false);
	}
	
	// Handle the sounds based on round ending
	public PlaybackHandler getAudioPlaybackHandler(IGameModelEvent gameModelEvent) {
		if (gameModelEvent == null || gameModelEvent.getType() != IGameModelEvent.Type.ROUND_ENDED) {
			return null;
		}
		
		RoundEndedEvent event = (RoundEndedEvent)gameModelEvent;
		switch (event.getRoundResult()) {
		
			case PLAYER1_VICTORY:
				return this.p1VictoryAudioHandler;
			case PLAYER2_VICTORY:
				return this.p2VictoryAudioHandler;
			case TIE:
				return this.tieAudioHandler;
			
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
