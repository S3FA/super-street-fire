package ca.site3.ssf.Sound;

import ca.site3.ssf.gamemodel.IGameModelEvent;
import ca.site3.ssf.gamemodel.UnrecognizedGestureEvent;

class UnrecognizedGestureSoundPlayer extends SoundPlayer {

	private PlaybackHandler unrecognizedGestureSound;
	
	UnrecognizedGestureSoundPlayer(SoundPlayerController controller) {
		super(controller);
		
		String tempPath = "";
		AudioSettings globalSettings = controller.getAudioSettings();
		
		tempPath = controller.getResourcePath() +
				controller.getConfigProperties().getProperty("Action.UnrecognizedGesture");
		this.unrecognizedGestureSound = PlaybackHandler.build(controller, tempPath,
				new PlaybackSettings(globalSettings.getVolume(), PlaybackSettings.BALANCED_PAN, 1));
	}
	
	
	public boolean isBackgroundSoundPlayer(IGameModelEvent gameModelEvent) {
		return false;
	}

	public PlaybackHandler getAudioPlaybackHandler(IGameModelEvent gameModelEvent) {
		if (gameModelEvent == null || gameModelEvent.getType() != IGameModelEvent.Type.UNRECOGNIZED_GESTURE) {
			return null;
		}
		
		UnrecognizedGestureEvent event = (UnrecognizedGestureEvent)gameModelEvent;
		if (event.getEntity().getIsPlayer()) {
			return this.unrecognizedGestureSound;
		}
		
		return null;
	}

}
