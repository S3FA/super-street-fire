package ca.site3.ssf.Sound;

import java.util.Properties;

import ca.site3.ssf.gamemodel.IGameModelEvent;
import ca.site3.ssf.gamemodel.RingmasterActionEvent;

class RingmasterActionSoundPlayer extends SoundPlayer {
	
	RingmasterActionSoundPlayer(SoundPlayerController controller) {
		super(controller);
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
		
		//RingmasterActionEvent event = (RingmasterActionEvent)gameModelEvent;
		// TODO: Ringmaster action sounds...?
		
		return null;
	}
	
	public boolean isBackgroundSoundPlayer(IGameModelEvent gameModelEvent) {
		return false;
	}
}
