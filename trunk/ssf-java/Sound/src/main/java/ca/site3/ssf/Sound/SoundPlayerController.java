package ca.site3.ssf.Sound;

import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.site3.ssf.gamemodel.IGameModelEvent;
import ca.site3.ssf.gamemodel.IGameModelListener;

/**
 * Listens for game events and plays sound effects and music as appropriate.
 * @author Mike
 */
public class SoundPlayerController implements IGameModelListener {
	
	private static final String DEFAULT_CONFIG_FILEPATH = "SoundProperties.properties";
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	private String resourcePath;
	private Properties configFile;
	private AudioSettings settings;
	
	public SoundPlayerController(AudioSettings settings) {
		assert(settings != null);
		this.settings = settings;
		this.setConfigFile(DEFAULT_CONFIG_FILEPATH);
	}
	
	public void setAudioSettings(AudioSettings settings) {
		synchronized(this.settings) {
			this.settings = settings;
		}
	}
	
    /**
	 * Called for any event that can be listened for in the gamemodel.
	 * @param event The object holding the event information.
	 */
	public void onGameModelEvent(IGameModelEvent gameModelEvent) {		
		
		SoundPlayer soundPlayer = SoundPlayer.build(this.resourcePath, this.configFile, gameModelEvent);
		if (soundPlayer != null) {
			synchronized(this.settings) {
				soundPlayer.playSounds(this.settings, gameModelEvent);
			}
		}
		
	}
	
	private void setConfigFile(String configPath) {
		this.configFile = new Properties();
		
		try {
			this.configFile.load(this.getClass().getResourceAsStream(configPath));
			this.resourcePath = configFile.getProperty("ResourcePath");
		}
		catch(IOException ex) {
			logger.warn("Setting config file failed.", ex);
		}
	}
}
