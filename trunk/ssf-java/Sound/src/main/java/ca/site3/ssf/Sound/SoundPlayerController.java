package ca.site3.ssf.Sound;

import java.io.IOException;
import java.util.Properties;

import ca.site3.ssf.gamemodel.GameState;
import ca.site3.ssf.gamemodel.GameStateChangedEvent;
import ca.site3.ssf.gamemodel.IGameModelEvent;
import ca.site3.ssf.gamemodel.IGameModelListener;
import ca.site3.ssf.gamemodel.MatchEndedEvent;
import ca.site3.ssf.gamemodel.PlayerAttackAction;
import ca.site3.ssf.gamemodel.PlayerAttackActionEvent;
import ca.site3.ssf.gamemodel.PlayerBlockActionEvent;
import ca.site3.ssf.gamemodel.RingmasterActionEvent;
import ca.site3.ssf.gamemodel.RoundBeginTimerChangedEvent;
import ca.site3.ssf.gamemodel.RoundEndedEvent;

/**
 * Listens for game events and plays sound effects and music as appropriate
 * @author Mike
 */
public class SoundPlayerController implements IGameModelListener
{
	protected String resourcePath;
	protected Properties configFile;
	
	/** Plays audio from given file names. */
	public static void main( String [] args ) {
		System.out.println("Started the SoundPlayerController.");
	} 

    /**
	 * Called for any event that can be listened for in the gamemodel.
	 * @param event The object holding the event information.
	 */
	public void onGameModelEvent(IGameModelEvent event)
	{	
		setConfigFile();
		
		if (event.getType() == IGameModelEvent.Type.GAME_STATE_CHANGED)
		{
			GameStateChangedSoundPlayer player = new GameStateChangedSoundPlayer();
			player.playSounds(event);
		}
		else if (event.getType() == IGameModelEvent.Type.PLAYER_ATTACK_ACTION)
		{
			PlayerAttackActionSoundPlayer player = new PlayerAttackActionSoundPlayer();
			player.playSounds(event);
		}
		else if (event.getType() == IGameModelEvent.Type.ROUND_ENDED)
		{
			RoundEndedSoundPlayer player = new RoundEndedSoundPlayer();
			player.playSounds(event);
		}
		else if (event.getType() == IGameModelEvent.Type.MATCH_ENDED)
		{
			MatchEndedSoundPlayer player = new MatchEndedSoundPlayer();
			player.playSounds(event);
		}
		else if (event.getType() == IGameModelEvent.Type.RINGMASTER_ACTION)
		{
			RingmasterActionSoundPlayer player = new RingmasterActionSoundPlayer();
			player.playSounds(event);
		}
		else if (event.getType() == IGameModelEvent.Type.ROUND_BEGIN_TIMER_CHANGED)
		{
			RoundBeginTimerChangedSoundPlayer player = new RoundBeginTimerChangedSoundPlayer();
			player.playSounds(event);
		}
		
		System.exit( 0 );
	}
	
	public void setConfigFile()
	{
		this.configFile = new Properties();
		
		try
		{
			this.configFile.load(this.getClass().getClassLoader().getResourceAsStream("/SoundProperties.properties"));
			this.resourcePath = configFile.getProperty("ResourcePath");
		}
		catch(IOException ex)
		{
			System.out.println("Setting config file failed.");
		}
	}
}
