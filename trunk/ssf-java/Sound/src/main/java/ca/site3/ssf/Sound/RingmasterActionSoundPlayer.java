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
		String resourcePath = controller.getResourcePath();
		AudioSettings globalSettings = controller.getAudioSettings();
		
		String tempPath = "";
		tempPath = resourcePath + configProperties.getProperty("RingmasterAttack.Drum");
		actionAudioMap.put(ActionType.RINGMASTER_DRUM_ACTION, PlaybackHandler.build(controller, tempPath,
			new PlaybackSettings(globalSettings.getVolume(), PlaybackSettings.BALANCED_PAN, 1)));

		tempPath = resourcePath + configProperties.getProperty("RingmasterAttack.Eruption");
		actionAudioMap.put(ActionType.RINGMASTER_ERUPTION_ACTION, PlaybackHandler.build(controller, tempPath,
				new PlaybackSettings(globalSettings.getVolume(), PlaybackSettings.BALANCED_PAN, 1)));
		
		tempPath = resourcePath + configProperties.getProperty("RingmasterAttack.Hadouken");
		actionAudioMap.put(ActionType.RINGMASTER_HADOUKEN_ACTION, PlaybackHandler.build(controller, tempPath,
				new PlaybackSettings(globalSettings.getVolume(), PlaybackSettings.BALANCED_PAN, 1)));
		
		tempPath = resourcePath + configProperties.getProperty("RingmasterAttack.LeftCircle");
		actionAudioMap.put(ActionType.RINGMASTER_LEFT_CIRCLE_ACTION, PlaybackHandler.build(controller, tempPath,
				new PlaybackSettings(globalSettings.getVolume(), PlaybackSettings.BALANCED_PAN, 1)));
		
		tempPath = resourcePath + configProperties.getProperty("RingmasterAttack.LeftHalf");
		actionAudioMap.put(ActionType.RINGMASTER_LEFT_HALF_RING_ACTION, PlaybackHandler.build(controller, tempPath,
				new PlaybackSettings(globalSettings.getVolume(), PlaybackSettings.BALANCED_PAN, 1)));
		
		tempPath = resourcePath + configProperties.getProperty("RingmasterAttack.Jab");
		PlaybackHandler tempHandler = PlaybackHandler.build(controller, tempPath,
				new PlaybackSettings(globalSettings.getVolume(), PlaybackSettings.BALANCED_PAN, 1));
		actionAudioMap.put(ActionType.RINGMASTER_LEFT_JAB_ACTION, tempHandler);
		actionAudioMap.put(ActionType.RINGMASTER_RIGHT_JAB_ACTION, tempHandler);
		
		tempPath = resourcePath + configProperties.getProperty("RingmasterAttack.RightCircle");
		actionAudioMap.put(ActionType.RINGMASTER_RIGHT_CIRCLE_ACTION, PlaybackHandler.build(controller, tempPath,
				new PlaybackSettings(globalSettings.getVolume(), PlaybackSettings.BALANCED_PAN, 1)));
		
		tempPath = resourcePath + configProperties.getProperty("RingmasterAttack.RightHalf");
		actionAudioMap.put(ActionType.RINGMASTER_RIGHT_HALF_RING_ACTION, PlaybackHandler.build(controller, tempPath,
				new PlaybackSettings(globalSettings.getVolume(), PlaybackSettings.BALANCED_PAN, 1)));
		

	}
	
	public PlaybackSettings getPlaybackSettings(AudioSettings globalSettings, IGameModelEvent gameModelEvent) {
		assert(globalSettings != null);
		return new PlaybackSettings(globalSettings.getVolume(), PlaybackSettings.BALANCED_PAN, 1);
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
