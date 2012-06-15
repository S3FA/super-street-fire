package ca.site3.ssf.Sound;

import java.io.IOException;
import java.util.Properties;

import ca.site3.ssf.gamemodel.IGameModelEvent;
import ca.site3.ssf.gamemodel.IGameModelListener;
import ca.site3.ssf.gamemodel.PlayerAttackAction;
import ca.site3.ssf.gamemodel.PlayerAttackActionEvent;

/**
 * Listens for game events and plays sound effects and music as appropriate
 * @author Mike
 */
public class SoundPlayerController implements IGameModelListener
{
	protected static String resourcePath;
	protected String configPath = "SoundProperties.properties";
	protected static Properties configFile;
	protected Thread loopingSoundThread;
	public static IGameModelEvent event;

	// Sets up the config file and determines whether to loop the track to create an appropriate thread
	public void playEventSounds(IGameModelEvent gameModelEvent)
	{
		this.setConfigFile();
		this.event = gameModelEvent;
		
		// Need some condition to determine whether there is a looping track event. Currently assuming GameStateChangedEvents always loop
		if (event.getType() == IGameModelEvent.Type.GAME_STATE_CHANGED)
		{
			// Interrupt the existing looping music thread if it exists
			this.stopLoopingMusic();
			
			this.loopingSoundThread = new Thread(new Runnable() {
				public void run() {
					System.out.println("New thread created for a looping track.");
					onGameModelEvent(event);
				}
			});
			
			this.loopingSoundThread.start();
		}
		else
		{
			// Create a thread for the sound effect
			Thread soundThread = new Thread(new Runnable() {
				public void run() {
					System.out.println("New thread created for a sound effect.");
					onGameModelEvent(event);
				}
			});
			
			soundThread.start();
		}
	}
	
    /**
	 * Called for any event that can be listened for in the gamemodel.
	 * @param event The object holding the event information.
	 */
	public void onGameModelEvent(IGameModelEvent gameModelEvent)
	{		
		// Call the appropriate SoundPlayer based on the IGameModelEvent.Type
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
	}
	
	// Interrupts the currently looping music thread if necessary
	public void stopLoopingMusic()
	{		
		if (this.loopingSoundThread != null)
		{
			System.out.println("Interrupting the looping sounds thread.");
			this.loopingSoundThread.stop();
		}
	}
	
	protected void setConfigFile()
	{
		this.configFile = new Properties();
		
		try
		{
			this.configFile.load(this.getClass().getResourceAsStream(configPath));
			this.resourcePath = configFile.getProperty("ResourcePath");
		}
		catch(IOException ex)
		{
			System.out.println("Setting config file failed.");
		}
	}
}
