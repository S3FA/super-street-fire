package ca.site3.ssf.Sound;

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Plays a .wav file by file name.
 * @author Mike, Callum
 */
class PlaybackHandler implements Runnable {
	
	static final int INFINITE_NUM_LOOPS = 0;
	
	private static final long MAX_EXTERNAL_BUFFER_SIZE = 128000;
	
	private static Logger logger = LoggerFactory.getLogger(PlaybackHandler.class);
	
	private AudioInputStream audioInputStream = null;
	private SourceDataLine srcDataLine = null;
	private byte[] externalAudioBuffer = null;
	
	private final String audioFilepath;
	private final int numLoops;
	private final float volume;
	
	private volatile boolean stop = false;
	
	PlaybackHandler(String audioFilepath, int numLoops, float volume) {
		this.audioFilepath = audioFilepath;
		this.numLoops = numLoops;
		this.volume = volume;
	}

	public void run() {
		
		// Open the audio file from disk (make sure it even exists)
		File audioFile = new File(audioFilepath);
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
		
		if (this.audioInputStream.markSupported()) {
			logger.warn("Audio resetting is not supported!");
		}
		
		// Create a source data line for the audio
		AudioFormat audioFormat = audioInputStream.getFormat();
		// Open a data line to play our type of sampled audio.
		// Use SourceDataLine for play and TargetDataLine for record.
		DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
		if (!AudioSystem.isLineSupported(info)) {
			logger.warn("Play.playAudioStream does not handle this type of audio on this system.");
			return;
		}
		
		try {
			// Create a SourceDataLine for play back (throws LineUnavailableException).
			this.srcDataLine = (SourceDataLine)AudioSystem.getLine(info);
			// The line needs to acquire system resources (throws LineAvailableException).
			this.srcDataLine.open(audioFormat);
		}
		catch (LineUnavailableException e) {
			logger.warn("Exception while attempting to get and open a source data line for audio stream.", e);
			return;
		}
		
		if (this.srcDataLine == null) {
			logger.warn("Failed to get source data line from the audio system.");
			return;
		}
		
		// Adjust the volume on the output line.
		if (this.srcDataLine.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
			FloatControl volume = (FloatControl)this.srcDataLine.getControl(FloatControl.Type.MASTER_GAIN);
			volume.setValue(Math.min(volume.getMaximum(), Math.max(volume.getMinimum(), this.volume)));
		}
		
		// Get the byte length of the audio stream
		long tempStreamLengthInBytes = audioInputStream.getFrameLength() * audioFormat.getFrameSize();
		if (tempStreamLengthInBytes > Integer.MAX_VALUE) {
			logger.warn("The length of the audio stream input exceeds Integer.MAX_VALUE, cannot properly reset stream!");
			return;
		}
		int streamLengthInBytes = (int)tempStreamLengthInBytes;
		
		// Setup the external audio buffer (for moving data from the audio input stream through to the source data line)
		int externalBufferSize = (int)Math.min(MAX_EXTERNAL_BUFFER_SIZE, Math.ceil(audioFormat.getSampleRate() * audioFormat.getFrameSize()));
		this.externalAudioBuffer = new byte[externalBufferSize];
		
		this.srcDataLine.start();
		
		int playCount = 0;
		int bytesRead = 0;

		while ((playCount < this.numLoops || this.numLoops == INFINITE_NUM_LOOPS) && !this.stop) {
			
			this.audioInputStream.mark(streamLengthInBytes);
			
			bytesRead = 0;
			while (bytesRead != -1) {
				try {
					bytesRead = this.audioInputStream.read(this.externalAudioBuffer, 0, this.externalAudioBuffer.length);
				}
				catch (IOException e) {
					logger.warn("Audio input stream read fail.", e);
					this.stop = true;
					break;
				}
				
				if (bytesRead >= 0) {
					int	bytesWritten = this.srcDataLine.write(this.externalAudioBuffer, 0, bytesRead);
				}
			}
			
			if (!this.stop) {
				playCount++;
				try {
					this.audioInputStream.reset();
				}
				catch (IOException e) {
					logger.warn("Failed to reset audio input stream.", e);
					break;
				}
			}
			
		}
		
		// If this thread has been told to stop then we don't bother draining the data line since
		// it's a blocking operation and we want to exit immediately
		if (!this.stop) {
			logger.debug("Draining audio source data line.");
			this.srcDataLine.drain();
		}
		
		logger.debug("Closing audio source data line.");
		this.srcDataLine.close();
	} 
	
	/**
	 * Play the given audio file.
	 * @param fileName Full file path of the audio file.
	 * @param isLooping Whether or not to loop the audio.
	 */
	/*
	public static void playAudioFile(String fileName, int numLoops) {
		File soundFile = new File(fileName);
		
		try 
		{
			// Continue playing the sound as long as we're looping
			do
			{
				// Create a stream from the given file
				AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(soundFile);
				playAudioStream(audioInputStream);
			}
			while (isLooping);
		} 
		catch (Exception e) {
			logger.warn("Problem with file " + fileName + ":", e);
		}
		
		// When the sound finished playing, return to close the thread
		return;
	} 
	*/
	
	/**
	 * Plays audio from the given audio input stream. This is a blocking function that will play
	 * the complete audio from a given input stream.
	 * @param audioInputStream The audio input stream to play.
	 */
	/*
	private static void playAudioStream(AudioInputStream audioInputStream) {
		// Audio format provides information like sample rate, size, channels.
		AudioFormat audioFormat = audioInputStream.getFormat();
		logger.debug("Play input audio format=" + audioFormat);
		
		// Open a data line to play our type of sampled audio.
		// Use SourceDataLine for play and TargetDataLine for record.
		DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
		
		if (!AudioSystem.isLineSupported(info)) {
			logger.warn("Play.playAudioStream does not handle this type of audio on this system.");
			return;
		}
				
		try {
			// Create a SourceDataLine for play back (throws LineUnavailableException).
			SourceDataLine dataLine = (SourceDataLine)AudioSystem.getLine(info);
			
			// System.out.println( "SourceDataLine class=" + dataLine.getClass() );
			// The line acquires system resources (throws LineAvailableException).
			dataLine.open(audioFormat);
			
			// Adjust the volume on the output line.
			if(dataLine.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
				FloatControl volume = (FloatControl)dataLine.getControl(FloatControl.Type.MASTER_GAIN);
				volume.setValue(6);
			}
			
			// Allows the line to move data in and out to a port.
			dataLine.start();
			
			// Create a buffer for moving data from the audio stream to the line.
			int bufferSize = (int) audioFormat.getSampleRate() * audioFormat.getFrameSize();
			byte [] buffer = new byte[ bufferSize ];
			
			// Move the data until done or there is an error.
			try {
				int bytesRead = 0;
				while (bytesRead >= 0) {
					bytesRead = audioInputStream.read(buffer, 0, buffer.length);
					if (bytesRead >= 0) {
						// logger.debug("Play.playAudioStream bytes read=" + bytesRead +
						// ", frame size=" + audioFormat.getFrameSize() + ", frames read=" + bytesRead / audioFormat.getFrameSize());
						
						// Odd sized sounds throw an exception if we don't write the same amount.
						int framesWritten = dataLine.write(buffer, 0, bytesRead);
					}
				} 
			} 
			catch (IOException e) {
				logger.warn("Exception while reading/writing audio stream.", e);
				e.printStackTrace();
			}
			
			logger.debug("Play.playAudioStream draining line.");
			
			// Blocking call that continues data line I/O until its buffer is drained
			dataLine.drain();
			logger.debug("Play.playAudioStream closing line.");
			
			// Closes the data line, freeing any resources such as the audio device.
			dataLine.close();
		} 
		catch (LineUnavailableException e) {
			logger.warn("Exception while playing audio stream.", e);
		}
	}
	*/
	
}
