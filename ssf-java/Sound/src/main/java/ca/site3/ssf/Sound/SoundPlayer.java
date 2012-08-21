package ca.site3.ssf.Sound;

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

	protected SoundPlayerController controller;
	
	protected SoundPlayer(SoundPlayerController controller) {
		assert(controller != null);
		this.controller = controller;
	}
	
	/**
	 * Factory method for building the appropriate SoundPlayer for the given gameModelEvent.
	 * @param resourcePath The path to the sound resources.
	 * @param configFile The configuration/properties file for audio lookup.
	 * @param gameModelEvent The game model event to base the creation of the sound player off of.
	 * @return The resulting SoundPlayer, null on error.
	 */
	public static SoundPlayer build(SoundPlayerController controller, IGameModelEvent gameModelEvent) {
		if (controller == null || gameModelEvent == null) {
			return null;
		}
		
		SoundPlayer result = null;
		switch (gameModelEvent.getType()) {
		
		case GAME_STATE_CHANGED: {
			result = new GameStateChangedSoundPlayer(controller);
			break;
		}
		case PLAYER_ATTACK_ACTION: {
			result = new PlayerAttackActionSoundPlayer(controller);
			break;
		}
		case ROUND_ENDED: {
			result = new RoundEndedSoundPlayer(controller);
			break;
		}
		case MATCH_ENDED: {
			result = new MatchEndedSoundPlayer(controller);
			break;
		}
		case RINGMASTER_ACTION: {
			result = new RingmasterActionSoundPlayer(controller);
			break;
		}
		case ROUND_BEGIN_TIMER_CHANGED: {
			result = new RoundBeginTimerChangedSoundPlayer(controller);
			break;
		}
		case UNRECOGNIZED_GESTURE: {
			result = new UnrecognizedGestureSoundPlayer(controller);
			break;
		}
		default:
			break;
		}
		
		return result;
	}
	
	public PlaybackSettings getPlaybackSettings(AudioSettings globalSettings, IGameModelEvent gameModelEvent) {
		assert(globalSettings != null);
		return new PlaybackSettings(globalSettings.getVolume(), PlaybackSettings.BALANCED_PAN, 1);
	}
	
	public void execute(IGameModelEvent gameModelEvent) {
		
		PlaybackHandler handler = this.getAudioPlaybackHandler(gameModelEvent);
		if (handler == null) {
			return;
		}
		
		AudioSettings globalSettings  = controller.getAudioSettings();
		assert(globalSettings != null);
		handler.setSettings(this.getPlaybackSettings(globalSettings, gameModelEvent));

		if (this.isBackgroundSoundPlayer(gameModelEvent)) {
			controller.addAndPlayBackgroundHandler(handler);
		}
		else {
			controller.addAndPlayForegroundHandler(handler);
		}
	}
}
