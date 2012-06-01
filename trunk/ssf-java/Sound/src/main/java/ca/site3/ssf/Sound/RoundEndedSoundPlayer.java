package ca.site3.ssf.Sound;

import ca.site3.ssf.gamemodel.IGameModelEvent;
import ca.site3.ssf.gamemodel.RoundEndedEvent;

public class RoundEndedSoundPlayer extends SoundPlayerController implements ISoundPlayer {
	
	// Handle the sounds based on round ending
	public void playSounds(IGameModelEvent gameModelEvent)
	{
		RoundEndedEvent event = (RoundEndedEvent)gameModelEvent;
		
		if (event.getRoundResult() == RoundEndedEvent.RoundResult.PLAYER1_VICTORY)
		{
			PlaybackHandler.playAudioFile(resourcePath + configFile.getProperty("RoundResult.PlayerOneVictory"));
		}
		else if (event.getRoundResult() == RoundEndedEvent.RoundResult.PLAYER2_VICTORY)
		{
			PlaybackHandler.playAudioFile(resourcePath + configFile.getProperty("RoundResult.PlayerTwoVictory"));
		}
		else if (event.getRoundResult() == RoundEndedEvent.RoundResult.TIE)
		{
			PlaybackHandler.playAudioFile(resourcePath + configFile.getProperty("RoundResult.Tie"));
		}
	}
}
