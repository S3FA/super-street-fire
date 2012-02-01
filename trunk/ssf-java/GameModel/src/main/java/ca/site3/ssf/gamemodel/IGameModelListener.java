package ca.site3.ssf.gamemodel;

import java.util.EnumSet;

/**
 * If you want to be notified of interesting things that happen during the game,
 * register an IGameModelListener with an {@link IGameModel}.
 * 
 * @author Callum
 * @author Greg
 */
public interface IGameModelListener {

	/**
	 * Event method, called whenever the game state changes.
	 * @param oldState The previous game state that was set before the change.
	 * @param newState The newly set game state after the change.
	 */
	void onGameStateChanged(GameState.GameStateType oldState, GameState.GameStateType newState);
	
	/**
	 * Event method, called whenever a fire emitter changes.
	 * @param fireEmitter
	 */
	void onFireEmitterChanged(ImmutableFireEmitter fireEmitter);
	
	
}
