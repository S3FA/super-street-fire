package ca.site3.ssf.Sound;

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
			PlaybackHandler.playAudioFile(resourcePath + configFile.getProperty("GameStateType.IdleState"), false);
		}
		else if (event.getNewState() == GameState.GameStateType.MATCH_ENDED_STATE)
		{
			PlaybackHandler.playAudioFile(resourcePath + configFile.getProperty("GameStateType.MatchEndedState"), false);
		}
		else if (event.getNewState() == GameState.GameStateType.NO_STATE)
		{
			PlaybackHandler.playAudioFile(resourcePath + configFile.getProperty("GameStateType.NoState"), false);
		}
		else if (event.getNewState() == GameState.GameStateType.PAUSED_STATE)
		{
			PlaybackHandler.playAudioFile(resourcePath + configFile.getProperty("GameStateType.PausedState"), false);
		}
		else if (event.getNewState() == GameState.GameStateType.RINGMASTER_STATE)
		{
			PlaybackHandler.playAudioFile(resourcePath + configFile.getProperty("GameStateType.RingmasterState"), false);
		}
		else if (event.getNewState() == GameState.GameStateType.ROUND_BEGINNING_STATE)
		{
			PlaybackHandler.playAudioFile(resourcePath + configFile.getProperty("GameStateType.RoundBeginningState"), false);
		}
		else if (event.getNewState() == GameState.GameStateType.ROUND_ENDED_STATE)
		{
			PlaybackHandler.playAudioFile(resourcePath + configFile.getProperty("GameStateType.RoundEndedState"), false);
		}
		else if (event.getNewState() == GameState.GameStateType.ROUND_IN_PLAY_STATE)
		{
			PlaybackHandler.playAudioFile(resourcePath + configFile.getProperty("GameStateType.RoundInPlayState"), false);
		}
		else if (event.getNewState() == GameState.GameStateType.TEST_ROUND_STATE)
		{
			PlaybackHandler.playAudioFile(resourcePath + configFile.getProperty("GameStateType.TestRoundState"), false);
		}
		else if (event.getNewState() == GameState.GameStateType.TIE_BREAKER_ROUND_STATE)
		{
			PlaybackHandler.playAudioFile(resourcePath + configFile.getProperty("GameStateType.TieBreakerRoundState"), false);
		}
	}
}
