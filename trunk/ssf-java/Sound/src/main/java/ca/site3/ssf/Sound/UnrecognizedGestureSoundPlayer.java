package ca.site3.ssf.Sound;

import java.util.Properties;

import ca.site3.ssf.gamemodel.IGameModelEvent;
import ca.site3.ssf.gamemodel.UnrecognizedGestureEvent;

class UnrecognizedGestureSoundPlayer extends SoundPlayer {

	private PlaybackHandler unrecognizedGestureSound;
	
	UnrecognizedGestureSoundPlayer(SoundPlayerController controller) {
		super(controller);

		Properties configProperties = controller.getConfigProperties();
		PlaybackSettings playbackSettings = getDefaultPlaybackSettings();
		
		this.unrecognizedGestureSound = PlaybackHandler.build(controller, configProperties.getProperty("Action.UnrecognizedGesture"), playbackSettings);
	}
	
	// Get the default playback settings for this sound player
	private PlaybackSettings getDefaultPlaybackSettings()
	{
		return new PlaybackSettings(controller.getAudioSettings().getVolume(), false, false);
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
