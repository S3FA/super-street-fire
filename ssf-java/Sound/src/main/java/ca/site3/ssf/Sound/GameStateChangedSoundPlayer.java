package ca.site3.ssf.Sound;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;

import ca.site3.ssf.gamemodel.GameState.GameStateType;
import ca.site3.ssf.gamemodel.GameStateChangedEvent;
import ca.site3.ssf.gamemodel.IGameModelEvent;

class GameStateChangedSoundPlayer extends SoundPlayer {
	
	private Map<GameStateType, PlaybackHandler> stateAudioMap =
			new HashMap<GameStateType, PlaybackHandler>(GameStateType.values().length);
	
	private static final List<String> THEME_SONG_RESOURCES = Arrays.asList(
		"Theme.Balrog",
		"Theme.Bison",
		"Theme.Blanka",
		"Theme.Chunli",
		"Theme.GuileSpecial",
		"Theme.Ken",
		"Theme.Ryu",
		"Theme.RyuSpecial",
		"Theme.ZangiefSpecial");

	
	GameStateChangedSoundPlayer(SoundPlayerController controller) {
		super(controller);
		
		Properties configProperties = controller.getConfigProperties();
		String resourcePath = controller.getResourcePath();
		AudioSettings globalSettings = controller.getAudioSettings();
		
		String tempPath = "";
		tempPath = resourcePath + configProperties.getProperty("GameState.IdleState");
		stateAudioMap.put(GameStateType.IDLE_STATE, PlaybackHandler.build(controller, tempPath,
				new PlaybackSettings(globalSettings.getVolume() * globalSettings.getBgGainFraction(),
					PlaybackSettings.BALANCED_PAN,
					PlaybackSettings.INFINITE_NUM_PLAYS)));
		
		tempPath = resourcePath + configProperties.getProperty("GameState.MatchEndedState");
		stateAudioMap.put(GameStateType.MATCH_ENDED_STATE, PlaybackHandler.build(controller, tempPath,
				new PlaybackSettings(globalSettings.getVolume() * globalSettings.getBgGainFraction(),
					PlaybackSettings.BALANCED_PAN,
					PlaybackSettings.INFINITE_NUM_PLAYS)));
		
		tempPath = resourcePath + configProperties.getProperty("GameState.NoState");
		stateAudioMap.put(GameStateType.NO_STATE, PlaybackHandler.build(controller, tempPath,
				new PlaybackSettings(globalSettings.getVolume() * globalSettings.getBgGainFraction(),
					PlaybackSettings.BALANCED_PAN,
					PlaybackSettings.INFINITE_NUM_PLAYS)));
		
		tempPath = resourcePath + configProperties.getProperty("GameState.PausedState");
		stateAudioMap.put(GameStateType.PAUSED_STATE, PlaybackHandler.build(controller, tempPath,
				new PlaybackSettings(globalSettings.getVolume() * globalSettings.getBgGainFraction(),
					PlaybackSettings.BALANCED_PAN,
					PlaybackSettings.INFINITE_NUM_PLAYS)));
		
		tempPath = resourcePath + configProperties.getProperty("GameState.RingmasterState");
		stateAudioMap.put(GameStateType.RINGMASTER_STATE, PlaybackHandler.build(controller, tempPath,
				new PlaybackSettings(globalSettings.getVolume() * globalSettings.getBgGainFraction(),
					PlaybackSettings.BALANCED_PAN,
					PlaybackSettings.INFINITE_NUM_PLAYS)));
		
		tempPath = resourcePath + configProperties.getProperty(pickARandomThemeSong());
		stateAudioMap.put(GameStateType.ROUND_BEGINNING_STATE, PlaybackHandler.build(controller, tempPath,
				new PlaybackSettings(globalSettings.getVolume() * globalSettings.getBgGainFraction(),
					PlaybackSettings.BALANCED_PAN,
					PlaybackSettings.INFINITE_NUM_PLAYS)));
		
		tempPath = resourcePath + configProperties.getProperty("GameState.RoundEndedState");
		stateAudioMap.put(GameStateType.ROUND_ENDED_STATE, PlaybackHandler.build(controller, tempPath,
				new PlaybackSettings(globalSettings.getVolume() * globalSettings.getBgGainFraction(),
					PlaybackSettings.BALANCED_PAN,
					PlaybackSettings.INFINITE_NUM_PLAYS)));
		
		tempPath = resourcePath + configProperties.getProperty("GameState.RoundInPlayState");
		stateAudioMap.put(GameStateType.ROUND_IN_PLAY_STATE, PlaybackHandler.build(controller, tempPath,
				new PlaybackSettings(globalSettings.getVolume() * globalSettings.getBgGainFraction(),
					PlaybackSettings.BALANCED_PAN,
					PlaybackSettings.INFINITE_NUM_PLAYS)));
		
		tempPath = resourcePath + configProperties.getProperty("GameState.TestRoundState");
		stateAudioMap.put(GameStateType.TEST_ROUND_STATE, PlaybackHandler.build(controller, tempPath,
				new PlaybackSettings(globalSettings.getVolume() * globalSettings.getBgGainFraction(),
					PlaybackSettings.BALANCED_PAN,
					PlaybackSettings.INFINITE_NUM_PLAYS)));
		
		tempPath = resourcePath + configProperties.getProperty("GameState.TieBreakerRoundState");
		stateAudioMap.put(GameStateType.TIE_BREAKER_ROUND_STATE, PlaybackHandler.build(controller, tempPath,
				new PlaybackSettings(globalSettings.getVolume() * globalSettings.getBgGainFraction(),
					PlaybackSettings.BALANCED_PAN,
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
	
	
	private String pickARandomThemeSong() {
		Random random = new Random(System.currentTimeMillis());
		int songIndex = random.nextInt(THEME_SONG_RESOURCES.size());
		return THEME_SONG_RESOURCES.get(songIndex);
	}
}
