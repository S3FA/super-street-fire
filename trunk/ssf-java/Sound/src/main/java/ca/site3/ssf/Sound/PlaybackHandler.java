package ca.site3.ssf.Sound;

import java.io.IOException;

import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Plays a .ogg file by file name.
 * @author Mike, Callum
 */
class PlaybackHandler implements LineListener {
	
	private static Logger logger = LoggerFactory.getLogger(PlaybackHandler.class);
	
	private OggClip ogg = null;
	
	private final SoundPlayerController controller;
	private final String audioFilepath;
	private PlaybackSettings settings;

	static PlaybackHandler build(SoundPlayerController controller, String audioFilepath, PlaybackSettings settings) {
		PlaybackHandler result = new PlaybackHandler(controller, audioFilepath, settings);
		boolean isInit = result.init();
		result.setSettings(settings);
		
		if (!isInit) {
			return null;
		}
		
		return result;
	}
	
	private PlaybackHandler(SoundPlayerController controller, String audioFilepath, PlaybackSettings settings) {
		assert(controller != null);
		assert(audioFilepath != null);
		assert(settings != null);
		
		this.controller = controller;
		this.audioFilepath = audioFilepath;
	}
	
	private boolean init() {
		try{
			this.ogg = new OggClip(this.audioFilepath);
		}
		catch(IOException ex){
			logger.warn("Failed to read audio file " + audioFilepath);
			return false;
		}
		// Open the audio file from disk (make sure it even exists)
		//File audioFile = new File(this.audioFilepath);
		//if (!audioFile.canRead()) {
		//	logger.warn("Failed to read audio file " + audioFilepath);
		//	return;
		//}
		
		// Create an audio input stream for the file
		//try {
			/*
			// This doesn't work properly on a mac...
			AudioFileFormat fileFormat = AudioSystem.getAudioFileFormat(audioFile);
			AudioFormat format = fileFormat.getFormat();
			
			// We need to alter the format of the audio input stream to ensure that it's in stereo (i.e., it must have 2 channels)
			this.audioInputStream = AudioSystem.getAudioInputStream(
					new AudioFormat(format.getSampleRate(), format.getSampleSizeInBits(), 2, false, format.isBigEndian()),
					AudioSystem.getAudioInputStream(audioFile));
			*/
		//	this.audioInputStream = AudioSystem.getAudioInputStream(audioFile);
		//} catch (UnsupportedAudioFileException e1) {
		//	logger.warn("Failed to get a supported audio input stream.", e1);
		//	return;
		//} catch (IOException e1) {
		//	logger.warn("Failed to open audio input stream.", e1);
		//	return;
		//}
		
		//if (this.audioInputStream == null) {
		//	logger.warn("Failed to create audio input stream for file " + audioFilepath);
		//	return;
		//}

		//try {
		//	this.clip = AudioSystem.getClip();
		//} catch (LineUnavailableException e) {
		//	logger.warn("Failed to get clip for audio playback.", e);
		//	return;
		//}
				
		//this.clip.addLineListener(this);
		//this.ogg.addLineListener(this);
		
		return true;
	}
	
	void play() {
		if (this.ogg == null) {
			this.controller.removePlaybackHandler(this);
			return;
		}
		
		try {
			//this.clip.open(this.audioInputStream);
			this.updateSettings();
			
			if (this.settings.getNumPlays() == PlaybackSettings.INFINITE_NUM_PLAYS) {
				//this.clip.loop(Clip.LOOP_CONTINUOUSLY);
				this.ogg.loop();
			}
			else {
				//this.clip.loop(this.settings.getNumPlays() - 1);
				this.ogg.loop(this.settings.getNumPlays() - 1);
			}
			
		//} catch (LineUnavailableException e) {
		//	logger.warn("Exception while attempting to get and open a clip for audio stream.", e);
		//	return;
		//} catch (IOException e) {
		//	logger.warn("Audio input stream read fail.", e);
		//	return;
		//}
		}catch (Exception e) {
			logger.warn("Exception while attempting to play the ogg in playbackHandler.", e);
			return;
		}
	}

	void stop() {
		if (this.ogg != null) {
			this.ogg.stop();
		}
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
		if (this.ogg == null) {
			return;
		}
		
		// Adjust the volume on the audio clip
		
		ogg.setGain(Math.min(1.0f, Math.max(0.0f, this.settings.getVolume())));
		//if (this.clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
		//	FloatControl volume = (FloatControl)this.clip.getControl(FloatControl.Type.MASTER_GAIN);
		//	volume.setValue(Math.min(volume.getMaximum(), Math.max(volume.getMinimum(), this.settings.getVolume())));
		//}
		// Adjust the pan of the audio clip
		ogg.setBalance(Math.min(1.0f, Math.max(-1.0f, this.settings.getVolume())));
		//if (this.clip.isControlSupported(FloatControl.Type.PAN)) {
		//	FloatControl pan = (FloatControl)this.clip.getControl(FloatControl.Type.PAN);
		//	pan.setValue(Math.min(pan.getMaximum(), Math.max(pan.getMinimum(), this.settings.getPan())));
		//}
		//else if (this.clip.isControlSupported(FloatControl.Type.BALANCE)) {
		//	FloatControl balance = (FloatControl)this.clip.getControl(FloatControl.Type.BALANCE);
		//	balance.setValue(Math.min(balance.getMaximum(), Math.max(balance.getMinimum(), this.settings.getPan())));
		//}
		
	}
	
	public void update(LineEvent lineEvent) {
		if (lineEvent.getType() == LineEvent.Type.STOP) {
			lineEvent.getLine().close();
			logger.debug("Closing audio source data line.");
			this.controller.removePlaybackHandler(this);
		}
	} 
	
}
