package ca.site3.ssf.gamemodel;

/**
 * Interface for the various events that are raised by the GameModel.
 * @author Callum
 */
public interface IGameModelEvent {
	public enum Type {
		GAME_INFO_REFRESH,
		FIRE_EMITTER_CHANGED,
		GAME_STATE_CHANGED,
		PLAYER_HEALTH_CHANGED,
		ROUND_PLAY_TIMER_CHANGED,
		ROUND_BEGIN_TIMER_CHANGED,
		ROUND_ENDED,
		MATCH_ENDED,
		PLAYER_ATTACK_ACTION,
		PLAYER_BLOCK_ACTION,
		RINGMASTER_ACTION
	};
		
	public Type getType();
}
