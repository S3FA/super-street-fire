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
class PlaybackHandler {
	
	private static Logger logger = LoggerFactory.getLogger(PlaybackHandler.class);

	private final SoundPlayerController controller;
	private String sourceName;
	private String audioFilePath;
	private URL audioFileURL;
	private PlaybackSettings settings;
	
	public boolean hasFollowupSound = false;
	public String followupSoundSource = null;
	public boolean isBackgroundPlayer;
	public boolean hasQuietBackground;

	static PlaybackHandler build(SoundPlayerController controller, String source, PlaybackSettings settings) {
		try{
			// Check to see if the audio file is even readable...
			File audioFile = new File(controller.getResourcePath() + source);
			if (!audioFile.canRead()) {
				logger.warn("Could not load audio file: {}", audioFile.getAbsoluteFile());
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
			return null;
		}
	}
	
	private PlaybackHandler(SoundPlayerController controller, File audioFile, PlaybackSettings settings) {
		assert(controller != null);
		assert(settings != null);
		assert(audioFile != null);
		
		this.controller = controller;
		this.audioFilePath = audioFile.getAbsolutePath();
		this.sourceName = audioFile.getName();
		this.settings = settings;
		this.isBackgroundPlayer = settings.getIsLooping();
		this.hasQuietBackground = settings.getIsQuietBackground();
		
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
			this.controller.mySoundSystem.newStreamingSource(this.isBackgroundPlayer, sourceName, this.audioFileURL, this.audioFilePath, this.isBackgroundPlayer, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0 );
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
			if(this.hasQuietBackground)
			{
				controller.pauseBackgroundMusic();
				controller.mySoundSystem.play(this.sourceName);
				controller.setBackgroundOverrideSource(this.sourceName);
			}
			else
			{
				controller.mySoundSystem.play(this.sourceName);
				
				if (hasFollowupSound)
				{
					File followupFile = new File(controller.getResourcePath() + followupSoundSource);
					URL followupURL = followupFile.toURI().toURL();
					controller.mySoundSystem.queueSound(this.sourceName, followupURL, followupSoundSource);
				}
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
	}
	
	void setGlobalAudioSettings(AudioSettings globalSettings) {
		this.settings.setVolume(globalSettings.getVolume());
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
