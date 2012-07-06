package ca.site3.ssf.Sound;

import ca.site3.ssf.gamemodel.IGameModelEvent;

class SoundPlayerManager {
	
	private final GameStateChangedSoundPlayer gameStateChangedSP;
	private final PlayerAttackActionSoundPlayer playerAttackActionSP;
	private final RoundEndedSoundPlayer roundEndedSP;
	private final MatchEndedSoundPlayer matchEndedSP;
	private final RingmasterActionSoundPlayer ringmasterActionSP;
	private final RoundBeginTimerChangedSoundPlayer roundBeginTimerChangedSP;
	private final UnrecognizedGestureSoundPlayer unrecognizedGestureSP;
	
	SoundPlayerManager(SoundPlayerController controller) {
		assert(controller != null);
		
		this.gameStateChangedSP = new GameStateChangedSoundPlayer(controller);
		this.playerAttackActionSP = new PlayerAttackActionSoundPlayer(controller);
		this.roundEndedSP = new RoundEndedSoundPlayer(controller);
		this.matchEndedSP = new MatchEndedSoundPlayer(controller);
		this.ringmasterActionSP = new RingmasterActionSoundPlayer(controller);
		this.roundBeginTimerChangedSP = new RoundBeginTimerChangedSoundPlayer(controller);
		this.unrecognizedGestureSP = new UnrecognizedGestureSoundPlayer(controller);
	}
	
	/**
	 * Factory method for building the appropriate SoundPlayer for the given gameModelEvent.
	 * @param resourcePath The path to the sound resources.
	 * @param configFile The configuration/properties file for audio lookup.
	 * @param gameModelEvent The game model event to base the creation of the sound player off of.
	 * @return The resulting SoundPlayer, null on error.
	 */
	SoundPlayer build(IGameModelEvent gameModelEvent) {
		if (gameModelEvent == null) {
			return null;
		}
		
		SoundPlayer result = null;
		switch (gameModelEvent.getType()) {
		
		case GAME_STATE_CHANGED: {
			result = this.gameStateChangedSP;
			break;
		}
		case PLAYER_ATTACK_ACTION: {
			result = this.playerAttackActionSP;
			break;
		}
		case ROUND_ENDED: {
			result = this.roundEndedSP;
			break;
		}
		case MATCH_ENDED: {
			result = this.matchEndedSP;
			break;
		}
		case RINGMASTER_ACTION: {
			result = this.ringmasterActionSP;
			break;
		}
		case ROUND_BEGIN_TIMER_CHANGED: {
			result = this.roundBeginTimerChangedSP;
			break;
		}
		case UNRECOGNIZED_GESTURE: {
			result = this.unrecognizedGestureSP;
			break;
		}
		
		default:
			break;
		}
		
		return result;
	}
	
	
}
