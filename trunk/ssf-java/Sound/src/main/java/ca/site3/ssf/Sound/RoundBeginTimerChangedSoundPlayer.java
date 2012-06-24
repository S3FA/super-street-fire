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
		String resourcePath = controller.getResourcePath();
		AudioSettings globalSettings = controller.getAudioSettings();
		
		String tempPath = "";
		tempPath = resourcePath + configProperties.getProperty("RoundBeginCountdown.Three");
		this.threeAudioHandler = new PlaybackHandler(controller, tempPath,
			new PlaybackSettings(globalSettings.getVolume(), PlaybackSettings.BALANCED_PAN, 1));
		
		tempPath = resourcePath + configProperties.getProperty("RoundBeginCountdown.Two");
		this.twoAudioHandler = new PlaybackHandler(controller, tempPath,
				new PlaybackSettings(globalSettings.getVolume(), PlaybackSettings.BALANCED_PAN, 1));
		
		tempPath = resourcePath + configProperties.getProperty("RoundBeginCountdown.One");
		this.oneAudioHandler = new PlaybackHandler(controller, tempPath,
				new PlaybackSettings(globalSettings.getVolume(), PlaybackSettings.BALANCED_PAN, 1));
		
		tempPath = resourcePath + configProperties.getProperty("RoundBeginCountdown.Fight");
		this.fightAudioHandler = new PlaybackHandler(controller, tempPath,
				new PlaybackSettings(globalSettings.getVolume(), PlaybackSettings.BALANCED_PAN, 1));
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
