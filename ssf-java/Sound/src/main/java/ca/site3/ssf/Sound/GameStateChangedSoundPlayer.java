package ca.site3.ssf.Sound;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import ca.site3.ssf.gamemodel.GameState.GameStateType;
import ca.site3.ssf.gamemodel.GameStateChangedEvent;
import ca.site3.ssf.gamemodel.IGameModelEvent;

class GameStateChangedSoundPlayer extends SoundPlayer {
	
	private Map<GameStateType, PlaybackHandler> stateAudioMap =
			new HashMap<GameStateType, PlaybackHandler>(GameStateType.values().length);
	
	GameStateChangedSoundPlayer(SoundPlayerController controller) {
		super(controller);
		
		Properties configProperties = controller.getConfigProperties();
		String resourcePath = controller.getResourcePath();
		AudioSettings globalSettings = controller.getAudioSettings();
		
		String tempPath = "";
		tempPath = resourcePath + configProperties.getProperty("GameState.IdleState");
		stateAudioMap.put(GameStateType.IDLE_STATE, new PlaybackHandler(controller, tempPath,
				new PlaybackSettings(globalSettings.getVolume(), PlaybackSettings.BALANCED_PAN,
					PlaybackSettings.INFINITE_NUM_PLAYS)));
		
		tempPath = resourcePath + configProperties.getProperty("GameState.MatchEndedState");
		stateAudioMap.put(GameStateType.MATCH_ENDED_STATE, new PlaybackHandler(controller, tempPath,
				new PlaybackSettings(globalSettings.getVolume(), PlaybackSettings.BALANCED_PAN,
					PlaybackSettings.INFINITE_NUM_PLAYS)));
		
		tempPath = resourcePath + configProperties.getProperty("GameState.NoState");
		stateAudioMap.put(GameStateType.NO_STATE, new PlaybackHandler(controller, tempPath,
				new PlaybackSettings(globalSettings.getVolume(), PlaybackSettings.BALANCED_PAN,
					PlaybackSettings.INFINITE_NUM_PLAYS)));
		
		tempPath = resourcePath + configProperties.getProperty("GameState.PausedState");
		stateAudioMap.put(GameStateType.PAUSED_STATE, new PlaybackHandler(controller, tempPath,
				new PlaybackSettings(globalSettings.getVolume(), PlaybackSettings.BALANCED_PAN,
					PlaybackSettings.INFINITE_NUM_PLAYS)));
		
		tempPath = resourcePath + configProperties.getProperty("GameState.RingmasterState");
		stateAudioMap.put(GameStateType.RINGMASTER_STATE, new PlaybackHandler(controller, tempPath,
				new PlaybackSettings(globalSettings.getVolume(), PlaybackSettings.BALANCED_PAN,
					PlaybackSettings.INFINITE_NUM_PLAYS)));
		
		tempPath = resourcePath + configProperties.getProperty("GameState.RoundBeginningState");
		stateAudioMap.put(GameStateType.ROUND_BEGINNING_STATE, new PlaybackHandler(controller, tempPath,
				new PlaybackSettings(globalSettings.getVolume(), PlaybackSettings.BALANCED_PAN,
					PlaybackSettings.INFINITE_NUM_PLAYS)));
		
		tempPath = resourcePath + configProperties.getProperty("GameState.RoundEndedState");
		stateAudioMap.put(GameStateType.ROUND_ENDED_STATE, new PlaybackHandler(controller, tempPath,
				new PlaybackSettings(globalSettings.getVolume(), PlaybackSettings.BALANCED_PAN,
					PlaybackSettings.INFINITE_NUM_PLAYS)));
		
		tempPath = resourcePath + configProperties.getProperty("GameState.RoundInPlayState");
		stateAudioMap.put(GameStateType.ROUND_IN_PLAY_STATE, new PlaybackHandler(controller, tempPath,
				new PlaybackSettings(globalSettings.getVolume(), PlaybackSettings.BALANCED_PAN,
					PlaybackSettings.INFINITE_NUM_PLAYS)));
		
		tempPath = resourcePath + configProperties.getProperty("GameState.TestRoundState");
		stateAudioMap.put(GameStateType.TEST_ROUND_STATE, new PlaybackHandler(controller, tempPath,
				new PlaybackSettings(globalSettings.getVolume(), PlaybackSettings.BALANCED_PAN,
					PlaybackSettings.INFINITE_NUM_PLAYS)));
		
		tempPath = resourcePath + configProperties.getProperty("GameState.TieBreakerRoundState");
		stateAudioMap.put(GameStateType.TIE_BREAKER_ROUND_STATE, new PlaybackHandler(controller, tempPath,
				new PlaybackSettings(globalSettings.getVolume(), PlaybackSettings.BALANCED_PAN,
					PlaybackSettings.INFINITE_NUM_PLAYS)));
	}
	
	public PlaybackSettings getPlaybackSettings(AudioSettings globalSettings, IGameModelEvent gameModelEvent) {
		return new PlaybackSettings(globalSettings.getVolume(), PlaybackSettings.BALANCED_PAN,
				PlaybackSettings.INFINITE_NUM_PLAYS);
	}

	public PlaybackHandler getAudioPlaybackHandler(IGameModelEvent gameModelEvent) {
		if (gameModelEvent == null || gameModelEvent.getType() != IGameModelEvent.Type.GAME_STATE_CHANGED) {
			return null;
		}
		
		GameStateChangedEvent event = (GameStateChangedEvent)gameModelEvent;
		return this.stateAudioMap.get(event.getNewState());
	}

	public boolean isBackgroundSoundPlayer(IGameModelEvent gameModelEvent) {
		return true;
	}
}
