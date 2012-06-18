package ca.site3.ssf.Sound;

import java.util.Properties;

import ca.site3.ssf.gamemodel.IGameModelEvent;
import ca.site3.ssf.gamemodel.RoundBeginTimerChangedEvent;

class RoundBeginTimerChangedSoundPlayer extends SoundPlayer {
	
	RoundBeginTimerChangedSoundPlayer(String resourcePath, Properties configFile) {
		super(resourcePath, configFile);
	}
	
	// Handle the sounds based on round begin timer changing
	public void playSounds(AudioSettings settings, IGameModelEvent gameModelEvent) {
		if (gameModelEvent.getType() != IGameModelEvent.Type.ROUND_BEGIN_TIMER_CHANGED) {
			return;
		}
		
		RoundBeginTimerChangedEvent event = (RoundBeginTimerChangedEvent)gameModelEvent;
		String audioFilepath = "";
		switch (event.getThreeTwoOneFightTime()) {
		
		case THREE: {
			audioFilepath = resourcePath + configFile.getProperty("RoundBeginCountdown.Three");
			break;
		}
		case TWO: {
			audioFilepath = resourcePath + configFile.getProperty("RoundBeginCountdown.Two");
			break;
		}
		case ONE: {
			audioFilepath = resourcePath + configFile.getProperty("RoundBeginCountdown.One");
			break;
		}
		case FIGHT: {
			audioFilepath = resourcePath + configFile.getProperty("RoundBeginCountdown.Fight");
			break;
		}
		
		default:
			assert(false);
			break;
			
		}
		
		if (audioFilepath.isEmpty()) {
			return;
		}
		
		new Thread(new PlaybackHandler(audioFilepath, 1, settings.getVolume())).start();
	}
}
