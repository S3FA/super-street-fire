package ca.site3.ssf.gamemodel;

import java.util.EnumSet;

/**
 * If you want to be notified of interesting things that happen during the game,
 * register an IGameModelListener with an {@link IGameModel}.
 *
 */
public interface IGameModelListener {

	/**
	 *	The 'types' of Game state changes
	 */
	public enum GameStateChangeFlag {
		
		PLAYER_HEALTH_CHANGE,
		
		ROUND_TIME_CHANGE,
		
		FIRE_EMITTER_CHANGE
	}
	
	
	
	void onGameStateChanged(GameState oldState, GameState newState, EnumSet<GameStateChangeFlag> changeFlags);

}
