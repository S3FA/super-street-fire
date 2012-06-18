package ca.site3.ssf.Sound;

import java.util.Properties;

import ca.site3.ssf.gamemodel.IGameModelEvent;

abstract class SoundPlayer implements ISoundPlayer {

	protected final String resourcePath;
	protected final Properties configFile;
	
	protected SoundPlayer(String resourcePath, Properties configFile) {
		assert(resourcePath != null);
		assert(configFile != null);
		this.resourcePath = resourcePath;
		this.configFile   = configFile;
	}
	
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
	
}
