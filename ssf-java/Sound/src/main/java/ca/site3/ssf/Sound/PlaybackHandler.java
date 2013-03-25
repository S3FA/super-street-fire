package ca.site3.ssf.Sound;

//import java.io.IOException;

//import javax.sound.sampled.LineEvent;
//import javax.sound.sampled.LineListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import paulscode.sound.SoundSystemConfig;

import java.io.File;
import java.net.URL;

/**
 * Plays a .ogg file by file name.
 * @author Mike, Callum
 */
class PlaybackHandler {// implements LineListener {
	
	private static Logger logger = LoggerFactory.getLogger(PlaybackHandler.class);

	private final SoundPlayerController controller;
	private final String sourceName;
	private String audioFilePath;
	private URL audioFileURL;
	private PlaybackSettings settings;

	static PlaybackHandler build(SoundPlayerController controller, String source, PlaybackSettings settings) {
		PlaybackHandler result = new PlaybackHandler(controller, source, settings);
		boolean isInit = result.init();
		result.setSettings(settings);
		
		if (!isInit) {
			return null;
		}
		
		return result;
	}
	
	private PlaybackHandler(SoundPlayerController controller, String source, PlaybackSettings settings) {
		assert(controller != null);
		assert(settings != null);
		assert(source != null);
		
		this.controller = controller;
		this.audioFilePath = controller.getResourcePath() + source;
		this.sourceName = source;
		
		try
		{
			this.audioFileURL = new File(audioFilePath).toURI().toURL();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	private boolean init() {
		try{
			this.controller.mySoundSystem.newStreamingSource(true, sourceName, this.audioFileURL, this.audioFilePath, true, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0 );
		}
		catch(Exception ex){
			logger.warn("Failed to read audio file " + audioFilePath);
			return false;
		}
		
		return true;
	}
	
	void play() {
		try 
		{
			// Pause the background music if necessary, and queue it up to restart immediately after playback
			if(this.settings.getIsQuietBackgground())
			{
				controller.mySoundSystem.pause(controller.getBackgroundSource());
				this.controller.mySoundSystem.play(sourceName);
				controller.mySoundSystem.queueSound(controller.getBackgroundSource(), controller.getBackgroundFileName());
			}
			else
			{
				this.controller.mySoundSystem.play(sourceName);
			}
		}catch (Exception e) {
			logger.warn("Exception while attempting to play the ogg in playbackHandler.", e);
			return;
		}
	}

	void stop() {
		this.controller.mySoundSystem.stop(sourceName);
	}
	
	void setSettings(PlaybackSettings settings) {
		assert(settings != null);
		this.settings = settings;
		this.updateSettings();
		
	}
	
	void setGlobalAudioSettings(AudioSettings globalSettings) {
		this.settings.setVolume(globalSettings.getVolume());
	}
	
	private void updateSettings() {
		// Adjust the volume on the audio clip
		this.controller.mySoundSystem.setVolume(sourceName, this.settings.getVolume());
	}
	
	public String getSourceName()
	{
		return this.sourceName;
	}
	
	public String getFilePath()
	{
		return this.audioFilePath;
	}
}
