package ca.site3.ssf.Sound;

import java.util.Properties;

import ca.site3.ssf.gamemodel.IGameModelEvent;
import ca.site3.ssf.gamemodel.RoundBeginTimerChangedEvent;

class RoundBeginTimerChangedSoundPlayer extends SoundPlayer {
	
	private PlaybackHandler threeAudioHandler;
	private PlaybackHandler twoAudioHandler; 
	private PlaybackHandler oneAudioHandler;
	private PlaybackHandler fightAudioHandler;
	
	RoundBeginTimerChangedSoundPlayer(SoundPlayerController controller) {
		super(controller);
		
		Properties configProperties = controller.getConfigProperties();
		PlaybackSettings playbackSettings = getDefaultPlaybackSettings();
		
		this.threeAudioHandler = PlaybackHandler.build(controller, configProperties.getProperty("RoundBeginCountdown.Three"), playbackSettings);
		this.twoAudioHandler = PlaybackHandler.build(controller, configProperties.getProperty("RoundBeginCountdown.Two"), playbackSettings);
		this.oneAudioHandler = PlaybackHandler.build(controller, configProperties.getProperty("RoundBeginCountdown.One"), playbackSettings);
		this.fightAudioHandler = PlaybackHandler.build(controller, configProperties.getProperty("RoundBeginCountdown.Fight"), playbackSettings);
	}
	
	// Get the default playback settings for this sound player
	private PlaybackSettings getDefaultPlaybackSettings()
	{
		return new PlaybackSettings(controller.getAudioSettings().getVolume(), false, false);
	}
	
	// Handle the sounds based on round begin timer changing
	public PlaybackHandler getAudioPlaybackHandler(IGameModelEvent gameModelEvent) {
		if (gameModelEvent == null || gameModelEvent.getType() != IGameModelEvent.Type.ROUND_BEGIN_TIMER_CHANGED) {
			return null;
		}
		
		RoundBeginTimerChangedEvent event = (RoundBeginTimerChangedEvent)gameModelEvent;
		switch (event.getThreeTwoOneFightTime()) {
		
		case THREE:
			return this.threeAudioHandler;
		case TWO: 
			return this.twoAudioHandler;
		case ONE:
			return this.oneAudioHandler;
		case FIGHT: 
			return this.fightAudioHandler;
		
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
