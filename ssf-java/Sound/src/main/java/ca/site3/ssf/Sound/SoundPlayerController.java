package ca.site3.ssf.Sound;

import java.io.IOException;
import java.util.Properties;

import ca.site3.ssf.gamemodel.IGameModelEvent;
import ca.site3.ssf.gamemodel.IGameModelListener;

/**
 * Listens for game events and plays sound effects and music as appropriate
 * @author Mike
 */
public class SoundPlayerController implements IGameModelListener
{
	protected String resourcePath;
	protected Properties configFile;
	protected Thread loopingSoundThread;
	protected static IGameModelEvent event;
	
	/** Plays audio from given file names. */
	//TODO: Replace main with a constructor. This only exists for testing purposes
	public static void main( String [] args ) {
		System.out.println("Started the SoundPlayerController.");
	
		// Test run of the audio file player. Test a non-looping sound
		Thread soundThread = new Thread(new Runnable() {
			public void run() {
				PlaybackHandler.playAudioFile("Resources/Hadoken.wav", false);
			}
		});
		
		// Test a looping sound
		Thread soundThread2 = new Thread(new Runnable() {
			public void run() {
				PlaybackHandler.playAudioFile("Resources/Sonic-boom.wav", true);
			}
		});
		
		// Start listening for and consuming the data from the gloves
		soundThread.start();
		soundThread2.start();
	} 

    /**
	 * Called for any event that can be listened for in the gamemodel.
	 * @param event The object holding the event information.
	 */
	public void onGameModelEvent(IGameModelEvent gameModelEvent)
	{	
		this.setConfigFile();
		this.event = gameModelEvent;
		
		// Currently assuming that GameStateChanged events are always changes to looping music. This logic will probably need to change.
		if (event.getType() == IGameModelEvent.Type.GAME_STATE_CHANGED)
		{
			// Interrupt the existing looping music thread if it exists
			if (this.loopingSoundThread != null)
			{
				this.loopingSoundThread.interrupt();
			}
			
			this.loopingSoundThread = new Thread(new Runnable() {
				public void run() {
					System.out.println("New thread created for a looping track.");
					GameStateChangedSoundPlayer player = new GameStateChangedSoundPlayer();
					player.playSounds(event);
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
					if (event.getType() == IGameModelEvent.Type.PLAYER_ATTACK_ACTION)
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
			});
			
			soundThread.start();
		}
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
