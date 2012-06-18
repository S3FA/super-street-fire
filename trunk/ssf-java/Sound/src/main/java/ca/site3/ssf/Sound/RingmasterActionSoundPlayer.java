package ca.site3.ssf.Sound;

import java.util.Properties;

import ca.site3.ssf.gamemodel.IGameModelEvent;
import ca.site3.ssf.gamemodel.RingmasterActionEvent;

class RingmasterActionSoundPlayer extends SoundPlayer {
	
	RingmasterActionSoundPlayer(String resourcePath, Properties configFile) {
		super(resourcePath, configFile);
	}
	
	/**
	 * Handles the sounds based on ringmaster actions.
	 */
	public void playSounds(AudioSettings settings, IGameModelEvent gameModelEvent) {
		if (gameModelEvent.getType() != IGameModelEvent.Type.RINGMASTER_ACTION) {
			return;
		}
		
		RingmasterActionEvent event = (RingmasterActionEvent)gameModelEvent;
		// TODO: Ringmaster action sounds...?
	}
}
