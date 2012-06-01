package ca.site3.ssf.Sound;

import ca.site3.ssf.gamemodel.IGameModelEvent;
import ca.site3.ssf.gamemodel.RingmasterActionEvent;

public class RingmasterActionSoundPlayer extends SoundPlayerController implements ISoundPlayer {
	
	// Handle the sounds based on ringmaster actions
	public void playSounds(IGameModelEvent gameModelEvent)
	{
		RingmasterActionEvent event = (RingmasterActionEvent)gameModelEvent;
	}
}
