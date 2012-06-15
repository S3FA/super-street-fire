package ca.site3.ssf.Sound;

import java.util.Properties;

import ca.site3.ssf.gamemodel.GameState;
import ca.site3.ssf.gamemodel.GameStateChangedEvent;
import ca.site3.ssf.gamemodel.IGameModelEvent;

public class GameStateChangedSoundPlayer extends SoundPlayerController implements ISoundPlayer {
	
	// Handle the sounds based on game state
	public void playSounds(IGameModelEvent gameModelEvent)
	{
		GameStateChangedEvent event = (GameStateChangedEvent)gameModelEvent;
		
		if (event.getNewState() == GameState.GameStateType.IDLE_STATE)
		{
			PlaybackHandler.playAudioFile(resourcePath + configFile.getProperty("GameState.IdleState"), true);
		}
		else if (event.getNewState() == GameState.GameStateType.MATCH_ENDED_STATE)
		{
			PlaybackHandler.playAudioFile(resourcePath + configFile.getProperty("GameState.MatchEndedState"), true);
		}
		else if (event.getNewState() == GameState.GameStateType.NO_STATE)
		{
			PlaybackHandler.playAudioFile(resourcePath + configFile.getProperty("GameState.NoState"), true);
		}
		else if (event.getNewState() == GameState.GameStateType.PAUSED_STATE)
		{
			PlaybackHandler.playAudioFile(resourcePath + configFile.getProperty("GameState.PausedState"), true);
		}
		else if (event.getNewState() == GameState.GameStateType.RINGMASTER_STATE)
		{
			PlaybackHandler.playAudioFile(resourcePath + configFile.getProperty("GameState.RingmasterState"), true);
		}
		else if (event.getNewState() == GameState.GameStateType.ROUND_BEGINNING_STATE)
		{
			PlaybackHandler.playAudioFile(resourcePath + configFile.getProperty("GameState.RoundBeginningState"), true);
		}
		else if (event.getNewState() == GameState.GameStateType.ROUND_ENDED_STATE)
		{
			PlaybackHandler.playAudioFile(resourcePath + configFile.getProperty("GameState.RoundEndedState"), true);
		}
		else if (event.getNewState() == GameState.GameStateType.ROUND_IN_PLAY_STATE)
		{
			PlaybackHandler.playAudioFile(resourcePath + configFile.getProperty("GameState.RoundInPlayState"), true);
		}
		else if (event.getNewState() == GameState.GameStateType.TEST_ROUND_STATE)
		{
			PlaybackHandler.playAudioFile(resourcePath + configFile.getProperty("GameState.TestRoundState"), true);
		}
		else if (event.getNewState() == GameState.GameStateType.TIE_BREAKER_ROUND_STATE)
		{
			PlaybackHandler.playAudioFile(resourcePath + configFile.getProperty("GameState.TieBreakerRoundState"), true);
		}
	}
}
