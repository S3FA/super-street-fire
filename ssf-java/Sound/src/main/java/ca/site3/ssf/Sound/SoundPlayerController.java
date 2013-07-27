package ca.site3.ssf.Sound;

import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.site3.ssf.gamemodel.GameState.GameStateType;
import ca.site3.ssf.gamemodel.GameStateChangedEvent;
import ca.site3.ssf.gamemodel.IGameModelEvent;
import ca.site3.ssf.gamemodel.IGameModelListener;

import paulscode.sound.IStreamListener;
import paulscode.sound.SoundSystem;
import paulscode.sound.SoundSystemConfig;
import paulscode.sound.SoundSystemException;
import paulscode.sound.codecs.CodecJOrbis;
import paulscode.sound.libraries.LibraryLWJGLOpenAL;

/**
 * Listens for game events and plays sound effects and music as appropriate.
 * Uses SoundEventMapper to determine what sounds to play
 * Uses PlaybackHandlers for each sound
 * Audio and Playback settings determine specific sound settings
 * @author Mike, Callum
 */
public class SoundPlayerController implements IGameModelListener, IStreamListener {
	
	private static final String DEFAULT_CONFIG_FILEPATH = "SoundProperties.properties";
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	private String resourcePath;
	private Properties configProperties;
	private AudioSettings settings;
	private String backgroundSource;
	private String backgroundFileName;
	private String backgroundOverrideSource;
	
	private GameStateType currentGameState; 
	
	SoundSystem mySoundSystem;
	SoundEventMapper soundEventMapper;
	
	public SoundPlayerController(AudioSettings settings) {
		assert(settings != null);
		
		this.backgroundFileName = "";
		this.backgroundSource   = "";
		
		this.settings = settings;
		this.setConfigFile(DEFAULT_CONFIG_FILEPATH);
		
		init();
	}
	
	// Initialize the sound player
	void init() {
		// Add the libraries and codecs
		try {
			SoundSystemConfig.addLibrary( LibraryLWJGLOpenAL.class );
            SoundSystemConfig.setCodec( "ogg", CodecJOrbis.class );
            SoundSystemConfig.addStreamListener(this);
		}
		catch( SoundSystemException e ) {
            System.out.println("Error linking with the plugins" );
        }
		
		// Create the sound system
		try {
            mySoundSystem = new SoundSystem( LibraryLWJGLOpenAL.class );
        }
        catch( SoundSystemException e ) {
        	System.out.println("LWJGL OpenAL library is not compatible on this computer");
            e.printStackTrace();
            return;
        }
		
		// Make sure there are audio settings available
		assert(getAudioSettings() != null);
		
		// Build the sound event mappings so we can map game events to sounds
		this.soundEventMapper = new SoundEventMapper(this);
	}
	
    /**
	 * Called for any event that can be listened for in the gamemodel.
	 * @param event The object holding the event information.
	 */
	public void onGameModelEvent(IGameModelEvent gameModelEvent) {		
		
		switch (gameModelEvent.getType()) {
		
			case GAME_STATE_CHANGED: {
				// Decide whether to stop/pause/play sounds
				GameStateChangedEvent ce = (GameStateChangedEvent) gameModelEvent;
				GameStateType oldGameState = ce.getOldState();
	
				this.currentGameState = ce.getNewState();
	
				switch (ce.getNewState()) {
					case IDLE_STATE:
						this.stopAllSounds();
					case PAUSED_STATE:
						this.pauseBackgroundMusic();
						break;
					default:
						if (oldGameState == GameStateType.PAUSED_STATE) {
							this.unpauseBackgroundMusic();
						}
						break;
				}
				break;
			}
			
			default:
				break;
		}
		
		// Play the sound
		this.playSound(soundEventMapper.getPlaybackHandler(gameModelEvent));
	}
	
	// Tells the playback handler to play the sound
	public void playSound(PlaybackHandler handler) {
		if (handler == null) {
			return;
		}
		
		// Update the current background music source
		if(handler.isBackgroundPlayer)
		{
			this.setBackgroundFileName(handler.getFilePath());
			this.setBackgroundSource(handler.getSourceName());
		}
		
		// Tell the playback handler to play the sound
		handler.play();
	}
	
	// Stops the background music and removes all references to it
	void stopAllSounds() {
		try {
			mySoundSystem.stop(this.getBackgroundSource());
			mySoundSystem.removeSource(this.getBackgroundSource());
			mySoundSystem.dequeueSound(this.getBackgroundSource(), this.getBackgroundFileName());
			mySoundSystem.stop(this.getBackgroundOverrideSource());
			mySoundSystem.removeSource(this.getBackgroundOverrideSource());
		}
		catch(Exception ex) {
			// An error occurred while trying to clean up the background music sources
			ex.printStackTrace();
		}
		
		this.setBackgroundSource("");
		this.setBackgroundFileName("");
	}
	
	// Pauses the background music
	void pauseBackgroundMusic() {
		try {
			if (this.backgroundSource != null) {
				mySoundSystem.pause(this.backgroundSource);
			}
		}
		catch(Exception ex) {
			// There was an exception trying to pause the background music
			ex.printStackTrace();
		}
	}
	
	// Unpauses the background music
	void unpauseBackgroundMusic() {
		try {
			if (this.backgroundSource != null) {
				mySoundSystem.play(this.backgroundSource);
			}
		}
		catch(Exception ex) {
			// There was an exception trying to unpause the background music.
			ex.printStackTrace();
		}
	}
	
	// Stops a specific sound source from playing
	void stopSound(String source) {
		mySoundSystem.stop(source);
	}
	
	// Detect when a stream has finished playing - if it's a bg music overriding sound, unpause the background music
	public void endOfStream(String sourcename, int queueSize) {
	    if (this.backgroundOverrideSource != null && this.backgroundOverrideSource.equals(sourcename)) {
	    	if (this.currentGameState != GameStateType.PAUSED_STATE) {
	    		this.unpauseBackgroundMusic();
	    	}
	        
	        this.setBackgroundOverrideSource("");
	    }
	}
	
	// Sets the config file referenced throughout the library
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
	
	// Gets the resource path
	String getResourcePath() {
		return this.resourcePath;
	}
	
	// Gets the config properties
	Properties getConfigProperties() {
		return this.configProperties;
	}
	
	// Sets the audio settings
	public void setAudioSettings(AudioSettings settings) {
		synchronized(this.settings) {
			this.settings = settings;
		}
	}
	
	// Gets the audio settings
	public AudioSettings getAudioSettings() {
		AudioSettings result = null;
		synchronized(this.settings) {
			result = this.settings;
		}
		return result;
	}

	// Gets the current source of background overriding sounds
	public String getBackgroundOverrideSource() {
		return backgroundOverrideSource;
	}
	
	// Sets the current source of background overriding sounds
	public void setBackgroundOverrideSource(String source) {
		this.backgroundOverrideSource = source;
	}
	
	// Gets the current source of background music
	public String getBackgroundSource() {
		return backgroundSource;
	}
	
	// Gets the current source of background music's file name
	public String getBackgroundFileName() {
		return backgroundFileName;
	}
	
	// Sets the background source
	public void setBackgroundSource(String source) {
		backgroundSource = source;
	}
	
	// Sets the background source's file name
	public void setBackgroundFileName(String fileName) {
		backgroundFileName = fileName;
	}
}
