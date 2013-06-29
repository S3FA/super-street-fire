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
		PlaybackSettings playbackSettings = getDefaultPlaybackSettings();

		stateAudioMap.put(GameStateType.IDLE_STATE, PlaybackHandler.build(controller, configProperties.getProperty("GameState.IdleState"), playbackSettings));
		stateAudioMap.put(GameStateType.MATCH_ENDED_STATE, PlaybackHandler.build(controller,configProperties.getProperty("GameState.MatchEndedState"), playbackSettings));
		stateAudioMap.put(GameStateType.NO_STATE, PlaybackHandler.build(controller, configProperties.getProperty("GameState.NoState"), playbackSettings));
		stateAudioMap.put(GameStateType.RINGMASTER_STATE, PlaybackHandler.build(controller, configProperties.getProperty("GameState.RingmasterState"), playbackSettings));
		stateAudioMap.put(GameStateType.ROUND_BEGINNING_STATE, PlaybackHandler.build(controller, configProperties.getProperty(pickARandomThemeSong()), playbackSettings));
		stateAudioMap.put(GameStateType.ROUND_ENDED_STATE, PlaybackHandler.build(controller, configProperties.getProperty("GameState.RoundEndedState"), playbackSettings));
		stateAudioMap.put(GameStateType.ROUND_IN_PLAY_STATE, PlaybackHandler.build(controller, configProperties.getProperty("GameState.RoundInPlayState"), playbackSettings));
		stateAudioMap.put(GameStateType.TEST_ROUND_STATE, PlaybackHandler.build(controller, configProperties.getProperty("GameState.TestRoundState"), playbackSettings));
		stateAudioMap.put(GameStateType.TIE_BREAKER_ROUND_STATE, PlaybackHandler.build(controller, configProperties.getProperty("GameState.TieBreakerRoundState"), playbackSettings));
	}
	
	// Get the default playback settings for this sound player
	private PlaybackSettings getDefaultPlaybackSettings() {
		return new PlaybackSettings(controller.getAudioSettings().getVolume(), true, false);
	}
	
	public PlaybackSettings getPlaybackSettings(AudioSettings globalSettings, IGameModelEvent gameModelEvent) {
		return new PlaybackSettings(globalSettings.getVolume(), true, false);
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
