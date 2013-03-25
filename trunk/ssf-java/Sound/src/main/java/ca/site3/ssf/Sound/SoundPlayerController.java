package ca.site3.ssf.Sound;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.site3.ssf.gamemodel.GameStateChangedEvent;
import ca.site3.ssf.gamemodel.IGameModelEvent;
import ca.site3.ssf.gamemodel.IGameModelEvent.Type;
import ca.site3.ssf.gamemodel.IGameModelListener;

import paulscode.sound.SoundSystem;
import paulscode.sound.SoundSystemConfig;
import paulscode.sound.SoundSystemException;
import paulscode.sound.codecs.CodecJOrbis;
import paulscode.sound.libraries.LibraryLWJGLOpenAL;

/**
 * Listens for game events and plays sound effects and music as appropriate.
 * @author Mike, Callum
 */
public class SoundPlayerController implements IGameModelListener, Runnable {
	
	private static final String DEFAULT_CONFIG_FILEPATH = "SoundProperties.properties";
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	private String resourcePath;
	private Properties configProperties;
	private AudioSettings settings;
	private String backgroundSource;
	private String backgroundFileName;
	private String backgroundFilePath;
	
	private BlockingQueue<IGameModelEvent> incomingEvents = new LinkedBlockingQueue<IGameModelEvent>();
	
	SoundSystem mySoundSystem;
	
	private volatile boolean stop = false;
	
	public SoundPlayerController(AudioSettings settings) {
		assert(settings != null);
		this.settings = settings;
		this.setConfigFile(DEFAULT_CONFIG_FILEPATH);
		
		init();
	}
	
	// Initialize the sound player
	void init()
	{
		try
		{
			SoundSystemConfig.addLibrary( LibraryLWJGLOpenAL.class );
            SoundSystemConfig.setCodec( "ogg", CodecJOrbis.class );
		}
		catch( SoundSystemException e )
        {
            System.out.println("error linking with the plugins" );
        }
		
		 try
        {
            mySoundSystem = new SoundSystem( LibraryLWJGLOpenAL.class );
        }
        catch( SoundSystemException e )
        {
        	System.out.println( "LWJGL OpenAL library is not compatible on this computer" );
            e.printStackTrace();
            return;
        }
	}
	
	String getResourcePath() {
		return this.resourcePath;
	}
	
	Properties getConfigProperties() {
		return this.configProperties;
	}
	
	public void setAudioSettings(AudioSettings settings) {
		synchronized(this.settings) {
			this.settings = settings;
		}
	}
	
	public AudioSettings getAudioSettings() {
		AudioSettings result = null;
		synchronized(this.settings) {
			result = this.settings;
		}
		return result;
	}

	public String getBackgroundSource()
	{
		return backgroundSource;
	}
	
	public String getBackgroundFileName()
	{
		return backgroundFileName;
	}
	
	public String getBackgroundFilePath()
	{
		return backgroundFilePath;
	}
	
	public void setBackgroundSource(String source)
	{
		backgroundSource = source;
	}
	
	public void setBackgroundFileName(String fileName)
	{
		backgroundFileName = fileName;
	}
	
	public void setBackgroundFilePath(String filePath)
	{
		backgroundFilePath = filePath;
	}
	
	public void stop() {
		this.stop = true;
	}
	
    /**
	 * Called for any event that can be listened for in the gamemodel.
	 * @param event The object holding the event information.
	 */
	public void onGameModelEvent(IGameModelEvent gameModelEvent) {		
		this.incomingEvents.add(gameModelEvent);
	}
	
	void stopAllSounds() 
	{
		mySoundSystem.cleanup();
	}
	
	private void setConfigFile(String configPath) {
		this.configProperties = new Properties();
		
		try {
			this.configProperties.load(this.getClass().getResourceAsStream(configPath));
			this.resourcePath = configProperties.getProperty("ResourcePath");
		}
		catch (IOException ex) {
			logger.warn("Setting config file failed.", ex);
			this.configProperties = null;
			this.resourcePath = "";
		}
	}

	void stopSound(String source) {
		mySoundSystem.stop(source);
	}

	public void run() {
		while (!this.stop) {
			IGameModelEvent gameModelEvent;
			try {
				gameModelEvent = this.incomingEvents.take();
			} catch (InterruptedException e) {
				continue;
			}
			
			if (gameModelEvent.getType() == Type.GAME_STATE_CHANGED) {
				GameStateChangedEvent ce = (GameStateChangedEvent) gameModelEvent;
				switch (ce.getNewState()) {
				case IDLE_STATE:
				case PAUSED_STATE:
					stopAllSounds();
					break;
				default:
					break;
				}
			}
			
			SoundPlayer soundPlayer = SoundPlayer.build(this, gameModelEvent);
			if (soundPlayer != null) {
				soundPlayer.execute(gameModelEvent);
			}
		}
	}

}
