package ca.site3.ssf.Sound;

import java.util.Properties;

import ca.site3.ssf.gamemodel.IGameModelEvent;

/**
 * Super class for all sound players - implements the ISoundPlayer. Classes
 * that inherit from this are responsible for determining the audio that should play
 * for particular gamemodel events.
 * 
 * @author Callum
 *
 */
abstract class SoundPlayer implements ISoundPlayer {

	protected final String resourcePath;
	protected final Properties configFile;
	
	protected SoundPlayer(String resourcePath, Properties configFile) {
		assert(resourcePath != null);
		assert(configFile != null);
		this.resourcePath = resourcePath;
		this.configFile   = configFile;
	}
	
	/**
	 * Factory method for building the appropriate SoundPlayer for the given gameModelEvent.
	 * @param resourcePath The path to the sound resources.
	 * @param configFile The configuration/properties file for audio lookup.
	 * @param gameModelEvent The game model event to base the creation of the sound player off of.
	 * @return The resulting SoundPlayer, null on error.
	 */
	public static SoundPlayer build(String resourcePath, Properties configFile, IGameModelEvent gameModelEvent) {
		if (resourcePath == null || resourcePath.isEmpty() || configFile == null || gameModelEvent == null) {
			return null;
		}
		
		SoundPlayer result = null;
		switch (gameModelEvent.getType()) {
		
		case GAME_STATE_CHANGED: {
			result = new GameStateChangedSoundPlayer(resourcePath, configFile);
			break;
		}
		case PLAYER_ATTACK_ACTION: {
			result = new PlayerAttackActionSoundPlayer(resourcePath, configFile);
			break;
		}
		case ROUND_ENDED: {
			result = new RoundEndedSoundPlayer(resourcePath, configFile);
			break;
		}
		case MATCH_ENDED: {
			result = new MatchEndedSoundPlayer(resourcePath, configFile);
			break;
		}
		case RINGMASTER_ACTION: {
			result = new RingmasterActionSoundPlayer(resourcePath, configFile);
			break;
		}
		case ROUND_BEGIN_TIMER_CHANGED: {
			result = new RoundBeginTimerChangedSoundPlayer(resourcePath, configFile);
			break;
		}
		default:
			break;
		}
		
		return result;
	}
	
	public void execute(SoundPlayerController controller, IGameModelEvent gameModelEvent) {
		String resourceFileStr = this.getAudioResourcePath(gameModelEvent);

		PlaybackHandler handler = null;
		
		AudioSettings globalSettings  = controller.getAudioSettings();
		assert(globalSettings != null);
		PlaybackSettings playbackSettings = this.getPlaybackSettings(globalSettings, gameModelEvent);
		if (playbackSettings == null) {
			return;
		}
		
		handler = new PlaybackHandler(controller, resourceFileStr, playbackSettings);
		
		if (this.isBackgroundSoundPlayer(gameModelEvent)) {
			controller.addAndPlayBackgroundHandler(handler);
		}
		else {
			controller.addAndPlayForegroundHandler(handler);
		}
	}
}
