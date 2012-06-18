package ca.site3.ssf.Sound;

import java.util.Properties;

import ca.site3.ssf.gamemodel.GameStateChangedEvent;
import ca.site3.ssf.gamemodel.IGameModelEvent;

class GameStateChangedSoundPlayer extends SoundPlayer {
	
	GameStateChangedSoundPlayer(String resourcePath, Properties configFile) {
		super(resourcePath, configFile);
	}
	
	/**
	 * Handles the sounds based on game state.
	 */
	public void playSounds(AudioSettings settings, IGameModelEvent gameModelEvent) {
		if (gameModelEvent.getType() != IGameModelEvent.Type.GAME_STATE_CHANGED) {
			return;
		}
		
		GameStateChangedEvent event = (GameStateChangedEvent)gameModelEvent;
		String audioFilepath = "";
		
		switch (event.getNewState()) {
		case IDLE_STATE: {
			audioFilepath = this.resourcePath + this.configFile.getProperty("GameState.IdleState");
			break;
		}
		case MATCH_ENDED_STATE: {
			audioFilepath = this.resourcePath + this.configFile.getProperty("GameState.MatchEndedState");
			break;
		}
		case NO_STATE: {
			audioFilepath = this.resourcePath + this.configFile.getProperty("GameState.NoState");
			break;
		}
		case PAUSED_STATE: {
			audioFilepath = this.resourcePath + this.configFile.getProperty("GameState.PausedState");
			break;
		}
		case RINGMASTER_STATE: {
			audioFilepath = this.resourcePath + this.configFile.getProperty("GameState.RingmasterState");
			break;
		}
		case ROUND_BEGINNING_STATE: {
			audioFilepath = this.resourcePath + this.configFile.getProperty("GameState.RoundBeginningState");
			break;
		}
		case ROUND_ENDED_STATE: {
			audioFilepath = this.resourcePath + this.configFile.getProperty("GameState.RoundEndedState");
			break;
		}
		case ROUND_IN_PLAY_STATE: {
			audioFilepath = this.resourcePath + this.configFile.getProperty("GameState.RoundInPlayState");
			break;
		}
		case TEST_ROUND_STATE: {
			audioFilepath = this.resourcePath + this.configFile.getProperty("GameState.TestRoundState");
			break;
		}
		case TIE_BREAKER_ROUND_STATE: {
			audioFilepath = this.resourcePath + this.configFile.getProperty("GameState.TieBreakerRoundState");
			break;
		}
		default:
			break;
		}
		
		if (audioFilepath.isEmpty()) {
			return;
		}
		
		new Thread(new PlaybackHandler(audioFilepath, PlaybackHandler.INFINITE_NUM_LOOPS, settings.getVolume())).start();
	}

}
