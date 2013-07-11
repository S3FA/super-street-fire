package ca.site3.ssf.Sound;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import ca.site3.ssf.gamemodel.IGameModelEvent;
import ca.site3.ssf.gamemodel.RoundBeginTimerChangedEvent;
import ca.site3.ssf.gamemodel.RoundBeginTimerChangedEvent.RoundBeginCountdownType;

class RoundBeginTimerChangedSoundPlayer extends SoundPlayer {
	
	private Map<RoundBeginCountdownType, PlaybackHandler> actionAudioMap = new HashMap<RoundBeginCountdownType, PlaybackHandler>(RoundBeginCountdownType.values().length);
	
	RoundBeginTimerChangedSoundPlayer(SoundPlayerController controller) {
		super(controller);
		
		Properties configProperties = controller.getConfigProperties();
		PlaybackSettings playbackSettings = getDefaultPlaybackSettings();
		
		actionAudioMap.put(RoundBeginCountdownType.THREE, PlaybackHandler.build(controller, configProperties.getProperty("RoundBeginCountdown.Three"), playbackSettings));
		actionAudioMap.put(RoundBeginCountdownType.TWO, PlaybackHandler.build(controller, configProperties.getProperty("RoundBeginCountdown.Two"), playbackSettings));
		actionAudioMap.put(RoundBeginCountdownType.ONE, PlaybackHandler.build(controller, configProperties.getProperty("RoundBeginCountdown.One"), playbackSettings));
		actionAudioMap.put(RoundBeginCountdownType.FIGHT, PlaybackHandler.build(controller, configProperties.getProperty("RoundBeginCountdown.Fight"), playbackSettings));
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
		return this.actionAudioMap.get(event.getThreeTwoOneFightTime());
	}
	
	public boolean isBackgroundSoundPlayer(IGameModelEvent gameModelEvent) {
		return false;
	}
}
