package ca.site3.ssf.Sound;

import ca.site3.ssf.gamemodel.IGameModelEvent;

/**
 * An interface for the sound players - a sound player is a class that is responsible for determining
 * the audio that should be playing for a particular IGameModelEvent.
 * @author Mike, Callum
 *
 */
interface ISoundPlayer {

	public boolean isBackgroundSoundPlayer(IGameModelEvent gameModelEvent);
	public PlaybackHandler getAudioPlaybackHandler(IGameModelEvent gameModelEvent);
	public void execute(IGameModelEvent gameModelEvent);
	
}