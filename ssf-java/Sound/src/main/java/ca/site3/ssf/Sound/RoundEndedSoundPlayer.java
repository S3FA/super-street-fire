package ca.site3.ssf.Sound;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import ca.site3.ssf.gamemodel.IGameModelEvent;
import ca.site3.ssf.gamemodel.RoundEndedEvent;
import ca.site3.ssf.gamemodel.RoundEndedEvent.RoundResult;

class RoundEndedSoundPlayer extends SoundPlayer {

	private Map<RoundResult, PlaybackHandler> actionAudioMap = new HashMap<RoundResult, PlaybackHandler>(RoundResult.values().length);
	private Properties configProperties;
	
	RoundEndedSoundPlayer(SoundPlayerController controller, RoundResult roundResult) {
		super(controller);
		
		configProperties = controller.getConfigProperties();
		PlaybackSettings playbackSettings = getDefaultPlaybackSettings();

		actionAudioMap.put(RoundResult.PLAYER1_VICTORY, PlaybackHandler.build(controller, configProperties.getProperty("RoundResult.PlayerOneVictory"), playbackSettings));
		actionAudioMap.put(RoundResult.PLAYER2_VICTORY, PlaybackHandler.build(controller, configProperties.getProperty("RoundResult.PlayerTwoVictory"), playbackSettings));
		actionAudioMap.put(RoundResult.TIE, PlaybackHandler.build(controller, configProperties.getProperty("RoundResult.Tie"), playbackSettings));
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
		
		this.controller.stopAllSounds();
		
		RoundEndedEvent event = (RoundEndedEvent)gameModelEvent;
		PlaybackHandler playbackHandler = this.actionAudioMap.get(event.getRoundResult());
		
		if (event.isPerfect())
		{
			playbackHandler.hasFollowupSound = true;
			playbackHandler.followupSoundSource = configProperties.getProperty("RoundResult.Perfect");
		}
		else if(event.isToasty())
		{
			playbackHandler.hasFollowupSound = true;
			playbackHandler.followupSoundSource = configProperties.getProperty("RoundResult.Toasty");
		}
		
		return playbackHandler;
	}
	
	public boolean isBackgroundSoundPlayer(IGameModelEvent gameModelEvent) {
		return false;
	}
}
