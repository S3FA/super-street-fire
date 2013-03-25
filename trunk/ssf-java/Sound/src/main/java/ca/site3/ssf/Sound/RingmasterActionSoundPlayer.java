package ca.site3.ssf.Sound;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import ca.site3.ssf.gamemodel.IGameModelEvent;
import ca.site3.ssf.gamemodel.RingmasterAction.ActionType;
import ca.site3.ssf.gamemodel.RingmasterActionEvent;

class RingmasterActionSoundPlayer extends SoundPlayer {
	
	private Map<ActionType, PlaybackHandler> actionAudioMap = new HashMap<ActionType, PlaybackHandler>(ActionType.values().length);
	
	RingmasterActionSoundPlayer(SoundPlayerController controller) {
		super(controller);
		
		Properties configProperties = controller.getConfigProperties();
		PlaybackSettings playbackSettings = getDefaultPlaybackSettings();
		
		actionAudioMap.put(ActionType.RINGMASTER_DRUM_ACTION, PlaybackHandler.build(controller, configProperties.getProperty("RingmasterAttack.Drum"), playbackSettings));
		actionAudioMap.put(ActionType.RINGMASTER_ERUPTION_ACTION, PlaybackHandler.build(controller, configProperties.getProperty("RingmasterAttack.Eruption"), playbackSettings));
		actionAudioMap.put(ActionType.RINGMASTER_HADOUKEN_ACTION, PlaybackHandler.build(controller, configProperties.getProperty("RingmasterAttack.Hadouken"), playbackSettings));
		actionAudioMap.put(ActionType.RINGMASTER_LEFT_CIRCLE_ACTION, PlaybackHandler.build(controller, configProperties.getProperty("RingmasterAttack.LeftCircle"), playbackSettings));
		actionAudioMap.put(ActionType.RINGMASTER_LEFT_HALF_RING_ACTION, PlaybackHandler.build(controller, configProperties.getProperty("RingmasterAttack.LeftHalf"), playbackSettings));
		actionAudioMap.put(ActionType.RINGMASTER_RIGHT_CIRCLE_ACTION, PlaybackHandler.build(controller, configProperties.getProperty("RingmasterAttack.RightCircle"), playbackSettings));
		actionAudioMap.put(ActionType.RINGMASTER_RIGHT_HALF_RING_ACTION, PlaybackHandler.build(controller, configProperties.getProperty("RingmasterAttack.RightHalf"), playbackSettings));
		
		PlaybackHandler tempHandler = PlaybackHandler.build(controller, configProperties.getProperty("RingmasterAttack.Jab"), playbackSettings);
		actionAudioMap.put(ActionType.RINGMASTER_LEFT_JAB_ACTION, tempHandler);
		actionAudioMap.put(ActionType.RINGMASTER_RIGHT_JAB_ACTION, tempHandler);
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
	
	/**
	 * Handles the sounds based on ringmaster actions.
	 */
	public PlaybackHandler getAudioPlaybackHandler(IGameModelEvent gameModelEvent) {
		if (gameModelEvent == null || gameModelEvent.getType() != IGameModelEvent.Type.RINGMASTER_ACTION) {
			return null;
		}
		
		RingmasterActionEvent event = (RingmasterActionEvent)gameModelEvent;
		return this.actionAudioMap.get(event.getActionType());
	}
	
	public boolean isBackgroundSoundPlayer(IGameModelEvent gameModelEvent) {
		return false;
	}
}
