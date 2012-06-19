package ca.site3.ssf.Sound;

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.Control;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Plays a .wav file by file name.
 * @author Mike, Callum
 */
class PlaybackHandler implements LineListener {
	
	static final int INFINITE_NUM_LOOPS = 0;
	
	private static Logger logger = LoggerFactory.getLogger(PlaybackHandler.class);
	
	private AudioInputStream audioInputStream = null;
	private Clip clip = null;

	private final SoundPlayerController controller;
	private final String audioFilepath;
	private final int numPlays;
	private final float volume;

	PlaybackHandler(SoundPlayerController controller, String audioFilepath, int numPlays, float volume) {
		assert(controller != null);
		assert(audioFilepath != null);
		assert(numPlays == INFINITE_NUM_LOOPS || numPlays > 0);
		
		this.controller = controller;
		this.audioFilepath = audioFilepath;
		this.numPlays = numPlays;
		this.volume = volume;
		
		this.init();
	}

	private void init() {
		
		// Open the audio file from disk (make sure it even exists)
		File audioFile = new File(this.audioFilepath);
		if (!audioFile.canRead()) {
			logger.warn("Failed to read audio file " + audioFilepath);
			return;
		}
		
		// Create an audio input stream for the file
		try {
			this.audioInputStream = AudioSystem.getAudioInputStream(audioFile);
		} catch (UnsupportedAudioFileException e1) {
			logger.warn("Failed to get a supported audio input stream.", e1);
			return;
		} catch (IOException e1) {
			logger.warn("Failed to open audio input stream.", e1);
			return;
		}
		
		if (this.audioInputStream == null) {
			logger.warn("Failed to create audio input stream for file " + audioFilepath);
			return;
		}
		
		try {
			this.clip = AudioSystem.getClip();
		} catch (LineUnavailableException e) {
			logger.warn("Failed to get clip for audio playback.", e);
			return;
		}
		
		// Adjust the volume on the audio clip
		if (this.clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
			FloatControl volume = (FloatControl)this.clip.getControl(FloatControl.Type.MASTER_GAIN);
			volume.setValue(Math.min(volume.getMaximum(), Math.max(volume.getMinimum(), this.volume)));
		}
		
		this.clip.addLineListener(this);
	}
	
	public void play() {
		try {
			this.clip.open(this.audioInputStream);
			if (this.numPlays == INFINITE_NUM_LOOPS) {
				this.clip.loop(Clip.LOOP_CONTINUOUSLY);
			}
			else {
				this.clip.loop(this.numPlays - 1);
			}
			
		} catch (LineUnavailableException e) {
			logger.warn("Exception while attempting to get and open a clip for audio stream.", e);
			return;
		} catch (IOException e) {
			logger.warn("Audio input stream read fail.", e);
			return;
		}
	}

	public void stop() {
		if (this.clip != null) {
			this.clip.stop();
		}
	}
	
	public void update(LineEvent lineEvent) {
		if (lineEvent.getType() == LineEvent.Type.STOP) {
			lineEvent.getLine().close();
			logger.debug("Closing audio source data line.");
			this.controller.removePlaybackHandler(this);
		}
	} 
	
}
