package org.Sound;

import ca.site3.ssf.gamemodel.IGameModelEvent;
import ca.site3.ssf.gamemodel.IGameModelListener;

/**
 * Listens for game events and plays sound effects and music as appropriate
 * @author Mike
 */
public class SoundController implements IGameModelListener
{
    public static void main( String[] args )
    {
        System.out.println("Created a new instance of SoundController.");
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
