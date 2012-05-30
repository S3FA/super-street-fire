package ca.site3.ssf.Sound;

import ca.site3.ssf.gamemodel.IGameModelEvent;
import ca.site3.ssf.gamemodel.IGameModelListener;

/**
 * Listens for game events and plays sound effects and music as appropriate
 * @author Mike
 */
public class SoundController implements IGameModelListener
{
	/** Plays audio from given file names. */
	public static void main( String [] args ) {
		
		// Test the playback functionality
		PlaybackHandler.playAudioFile("Resources/Hadoken.wav");

		// Must exit explicitly since audio creates non-daemon threads.
		System.exit( 0 );
	} 

    /**
	 * Called for any event that can be listened for in the gamemodel.
	 * @param event The object holding the event information.
	 */
	public void onGameModelEvent(IGameModelEvent event)
	{
		if (event.getType() == IGameModelEvent.Type.GAME_STATE_CHANGED)
		{
			playGameStateSounds();
		}
		else if (event.getType() == IGameModelEvent.Type.PLAYER_ATTACK_ACTION)
		{
			playAttackSounds();
		}
		else if (event.getType() == IGameModelEvent.Type.PLAYER_BLOCK_ACTION)
		{
			playBlockSounds();
		}
		else if (event.getType() == IGameModelEvent.Type.ROUND_ENDED)
		{
			playRoundEndedSounds();
		}
		else if (event.getType() == IGameModelEvent.Type.MATCH_ENDED)
		{
			playMatchEndedSounds();
		}
		else if (event.getType() == IGameModelEvent.Type.RINGMASTER_ACTION)
		{
			playRingMasterSounds();
		}
	}
	
	// Handle different types of sound events
	private void playGameStateSounds()
	{
		
	}
	
	private void playAttackSounds()
	{
		
	}
	
	private void playBlockSounds()
	{
		
	}
	
	private void playRoundEndedSounds()
	{
		
	}
	
	private void playMatchEndedSounds()
	{
		
	}
	
	private void playRingMasterSounds()
	{
		
	}
}
