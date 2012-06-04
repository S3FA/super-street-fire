package ca.site3.ssf.Sound;

import ca.site3.ssf.gamemodel.IGameModelEvent;
import ca.site3.ssf.gamemodel.MatchEndedEvent;

public class MatchEndedSoundPlayer extends SoundPlayerController implements ISoundPlayer {
	
	// Handle the sounds based on match ending
	public void playSounds(IGameModelEvent gameModelEvent)
	{
		MatchEndedEvent event = (MatchEndedEvent)gameModelEvent;
		
		if (event.getMatchResult() == MatchEndedEvent.MatchResult.PLAYER1_VICTORY)
		{
			PlaybackHandler.playAudioFile(resourcePath + configFile.getProperty("MatchResult.PlayerOneVictory"), false);
		}
		else if (event.getMatchResult() == MatchEndedEvent.MatchResult.PLAYER2_VICTORY)
		{
			PlaybackHandler.playAudioFile(resourcePath + configFile.getProperty("MatchResult.PlayerTwoVictory"), false);
		}
	}
}
