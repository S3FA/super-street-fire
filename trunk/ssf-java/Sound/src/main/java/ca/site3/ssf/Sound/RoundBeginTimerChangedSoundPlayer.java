package ca.site3.ssf.Sound;

import ca.site3.ssf.gamemodel.IGameModelEvent;
import ca.site3.ssf.gamemodel.RoundBeginTimerChangedEvent;

public class RoundBeginTimerChangedSoundPlayer extends SoundPlayerController implements ISoundPlayer {
	
	// Handle the sounds based on round begin timer changing
	public void playSounds(IGameModelEvent gameModelEvent)
	{
		RoundBeginTimerChangedEvent event = (RoundBeginTimerChangedEvent)gameModelEvent;
		
		if (event.getThreeTwoOneFightTime() == RoundBeginTimerChangedEvent.RoundBeginCountdownType.THREE)
		{
			PlaybackHandler.playAudioFile(resourcePath + configFile.getProperty("RoundBeginCountdown.Three"));
		}
		else if (event.getThreeTwoOneFightTime() == RoundBeginTimerChangedEvent.RoundBeginCountdownType.TWO)
		{
			PlaybackHandler.playAudioFile(resourcePath + configFile.getProperty("RoundBeginCountdown.Two"));
		}
		else if (event.getThreeTwoOneFightTime() == RoundBeginTimerChangedEvent.RoundBeginCountdownType.ONE)
		{
			PlaybackHandler.playAudioFile(resourcePath + configFile.getProperty("RoundBeginCountdown.One"));
		}
		else if (event.getThreeTwoOneFightTime() == RoundBeginTimerChangedEvent.RoundBeginCountdownType.FIGHT)
		{
			PlaybackHandler.playAudioFile(resourcePath + configFile.getProperty("RoundBeginCountdown.Fight"));
		}
	}
}
