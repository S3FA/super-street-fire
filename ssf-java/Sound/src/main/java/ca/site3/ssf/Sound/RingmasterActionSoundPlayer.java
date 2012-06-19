package ca.site3.ssf.Sound;

import java.util.Properties;

import ca.site3.ssf.gamemodel.IGameModelEvent;
import ca.site3.ssf.gamemodel.RingmasterActionEvent;

class RingmasterActionSoundPlayer extends SoundPlayer {
	
	RingmasterActionSoundPlayer(String resourcePath, Properties configFile) {
		super(resourcePath, configFile);
	}
	
	public int getNumPlays(IGameModelEvent gameModelEvent) {
		return 1;
	}
	
	/**
	 * Handles the sounds based on ringmaster actions.
	 */
	public String getAudioResourcePath(IGameModelEvent gameModelEvent) {
		if (gameModelEvent == null || gameModelEvent.getType() != IGameModelEvent.Type.RINGMASTER_ACTION) {
			return "";
		}
		
		RingmasterActionEvent event = (RingmasterActionEvent)gameModelEvent;
		// TODO: Ringmaster action sounds...?
		
		return "";
	}
}
