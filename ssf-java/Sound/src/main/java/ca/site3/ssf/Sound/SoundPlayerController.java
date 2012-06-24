package ca.site3.ssf.Sound;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.site3.ssf.gamemodel.IGameModelEvent;
import ca.site3.ssf.gamemodel.IGameModelListener;

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
	
	private Set<PlaybackHandler> fgPlaybackHandlers = new HashSet<PlaybackHandler>(20);
	private Set<PlaybackHandler> bgPlaybackHandlers = new HashSet<PlaybackHandler>(2);
	
	private BlockingQueue<IGameModelEvent> incomingEvents = new LinkedBlockingQueue<IGameModelEvent>();
	
	private volatile boolean stop = false;
	
	public SoundPlayerController(AudioSettings settings) {
		assert(settings != null);
		this.settings = settings;
		this.setConfigFile(DEFAULT_CONFIG_FILEPATH);
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
	
	void addAndPlayForegroundHandler(PlaybackHandler handler) {
		this.fgPlaybackHandlers.add(handler);
		handler.play();
	}
	
	void addAndPlayBackgroundHandler(PlaybackHandler handler) {
		this.bgPlaybackHandlers.add(handler);
		handler.play();
	}
	
	void stopAllSounds() {
		Iterator<PlaybackHandler> iter = this.fgPlaybackHandlers.iterator();
		while (iter.hasNext()) {
			PlaybackHandler handler = iter.next();
			handler.stop();
			iter.remove();
		}
		assert(this.fgPlaybackHandlers.isEmpty());
		
		iter = this.bgPlaybackHandlers.iterator();
		while (iter.hasNext()) {
			PlaybackHandler handler = iter.next();
			handler.stop();
			iter.remove();
		}
		assert(this.bgPlaybackHandlers.isEmpty());
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

	void removePlaybackHandler(PlaybackHandler handler) {
		assert(handler != null);
		if (!this.fgPlaybackHandlers.remove(handler)) {
			this.bgPlaybackHandlers.remove(handler);
		}
	}

	@Override
	public void run() {
		while (!this.stop) {
			IGameModelEvent gameModelEvent;
			try {
				gameModelEvent = this.incomingEvents.take();
			} catch (InterruptedException e) {
				continue;
			}
			
			SoundPlayer soundPlayer = SoundPlayer.build(this, gameModelEvent);
			if (soundPlayer != null) {
				soundPlayer.execute(gameModelEvent);
			}
		}
	}

}
