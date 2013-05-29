package ca.site3.ssf.gamemodel;

import java.io.Serializable;

/**
 * Interface for the various events that are raised by the GameModel.
 * @author Callum
 */
public interface IGameModelEvent extends Serializable {
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
		RINGMASTER_ACTION,
		UNRECOGNIZED_GESTURE,
		BLOCK_WINDOW,
		PLAYER_ACTION_POINTS_CHANGED,
		PLAYER_ATTACK_ACTION_FAILED,
		
		SYSTEM_INFO_REFRESH // TODO: this doesn't strictly belong here... 
							// Callum: I don't think we need this in the game model, 
		                    // couldn't we just keep this stuff in the IOServer?
	};
		
	public Type getType();
}
