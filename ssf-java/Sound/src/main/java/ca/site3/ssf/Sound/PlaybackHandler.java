package ca.site3.ssf.Sound;

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
	
	public boolean isBackgroundPlayer;

	static PlaybackHandler build(SoundPlayerController controller, String source, PlaybackSettings settings) {
		try{
			// Check to see if the audio file is even readable...
			File audioFile = new File(controller.getResourcePath() + source);
			if (!audioFile.canRead()) {
				return null;
			}
			
			PlaybackHandler result = new PlaybackHandler(controller, audioFile, settings);
			boolean isInit = result.init();
			result.setSettings(settings);
			
			if (!isInit) {
				return null;
			}
			
			return result;
		}
		catch(Exception ex)
		{
			logger.warn("Exception while attempting to set up the playbackHandler.", ex);
			throw ex;
			//return null;
		}
	}
	
	private PlaybackHandler(SoundPlayerController controller, File audioFile, PlaybackSettings settings) {
		assert(controller != null);
		assert(settings != null);
		assert(audioFile != null);
		assert(settings != null);
		
		this.controller = controller;
		this.audioFilePath = audioFile.getAbsolutePath();
		this.sourceName = audioFile.getName();
		this.settings = settings;
		
		try
		{
			this.audioFileURL = audioFile.toURI().toURL();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	private boolean init() {
		try{
			this.controller.mySoundSystem.newStreamingSource(this.isBackgroundPlayer, sourceName, this.audioFileURL, this.audioFilePath, this.settings.getIsLooping(), 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0 );
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
			if(this.settings.getIsQuietBackground())
			{
				String bgSource = controller.getBackgroundSource();
				String bgFile = controller.getBackgroundFileName();
				URL queuedFile = new File(bgFile).toURI().toURL();
				
				controller.mySoundSystem.stop(bgSource);
				controller.mySoundSystem.removeSource(bgSource);
				controller.mySoundSystem.dequeueSound(bgSource, bgFile);
				
				// Note that when you queue up a sound, it actually plays in the source you are queuing it up to play AFTER.
				controller.mySoundSystem.play(sourceName);
				controller.mySoundSystem.queueSound(sourceName, queuedFile, bgSource);
				
				// See above - that's why we need to set the background source to be the source we want to queue bg music to play after
				controller.setBackgroundFileName(this.audioFilePath);
				controller.setBackgroundSource(sourceName);
			}
			else
			{
				controller.mySoundSystem.play(this.sourceName);
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
		try{
			// Adjust the volume on the audio clip
			//this.controller.mySoundSystem.setVolume(sourceName, this.settings.getVolume());
		}
		catch (Exception ex){
			logger.warn("Exception while attempting to set the volume.", ex);
			throw ex;
		}
	}
	
	public void setIsBackground(boolean background)
	{
		this.isBackgroundPlayer = background;
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
