package ca.site3.ssf.Sound;

import java.util.Properties;

import ca.site3.ssf.gamemodel.IGameModelEvent;
import ca.site3.ssf.gamemodel.PlayerAttackActionEvent;

/**
 * Super class for all sound players - implements the ISoundPlayer. Classes
 * that inherit from this are responsible for determining the audio that should play
 * for particular gamemodel events.
 * 
 * @author Callum
 *
 */
abstract class SoundPlayer implements ISoundPlayer {

	protected SoundPlayerController controller;
	
	protected SoundPlayer(SoundPlayerController controller) {
		assert(controller != null);
		this.controller = controller;
	}
	
	PlaybackSettings getPlaybackSettings(AudioSettings globalSettings, IGameModelEvent gameModelEvent) {
		assert(globalSettings != null);
		return new PlaybackSettings(globalSettings.getVolume(), PlaybackSettings.BALANCED_PAN, 1);
	}
	
	public void execute(IGameModelEvent gameModelEvent) {
		
		PlaybackHandler handler = this.getAudioPlaybackHandler(gameModelEvent);
		if (handler == null) {
			return;
		}
		
		AudioSettings globalSettings  = controller.getAudioSettings();
		assert(globalSettings != null);
		handler.setSettings(this.getPlaybackSettings(globalSettings, gameModelEvent));

		if (this.isBackgroundSoundPlayer(gameModelEvent)) {
			controller.addAndPlayBackgroundHandler(handler);
		}
		else {
			controller.addAndPlayForegroundHandler(handler);
		}
	}
}
