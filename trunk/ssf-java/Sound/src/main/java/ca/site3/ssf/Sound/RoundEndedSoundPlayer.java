package ca.site3.ssf.Sound;

import java.util.Properties;

import ca.site3.ssf.gamemodel.IGameModelEvent;
import ca.site3.ssf.gamemodel.RoundEndedEvent;

class RoundEndedSoundPlayer extends SoundPlayer {
	
	private PlaybackHandler p1VictoryAudioHandler;
	private PlaybackHandler p2VictoryAudioHandler;
	private PlaybackHandler tieAudioHandler;
	
	RoundEndedSoundPlayer(SoundPlayerController controller) {
		super(controller);
		
		Properties configProperties = controller.getConfigProperties();
		String resourcePath = controller.getResourcePath();
		AudioSettings globalSettings = controller.getAudioSettings();
		
		String tempPath = "";
		tempPath = resourcePath + configProperties.getProperty("RoundResult.PlayerOneVictory");
		this.p1VictoryAudioHandler = PlaybackHandler.build(controller, tempPath,
			new PlaybackSettings(globalSettings.getVolume(), PlaybackSettings.BALANCED_PAN, 1));
		
		tempPath = resourcePath + configProperties.getProperty("RoundResult.PlayerTwoVictory");
		this.p2VictoryAudioHandler = PlaybackHandler.build(controller, tempPath,
			new PlaybackSettings(globalSettings.getVolume(), PlaybackSettings.BALANCED_PAN, 1));
		
		tempPath = resourcePath + configProperties.getProperty("RoundResult.Tie");
		this.tieAudioHandler = PlaybackHandler.build(controller, tempPath,
			new PlaybackSettings(globalSettings.getVolume(), PlaybackSettings.BALANCED_PAN, 1));
		
		// Stop all other controller sounds...
		controller.stopAllSounds();
	}
	
	public PlaybackSettings getPlaybackSettings(AudioSettings globalSettings, IGameModelEvent gameModelEvent) {
		assert(globalSettings != null);
		return new PlaybackSettings(globalSettings.getVolume(), PlaybackSettings.BALANCED_PAN, 1);
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
