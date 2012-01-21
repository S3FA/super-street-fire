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

	void onGameStateChanged(GameState.GameStateType oldState, GameState.GameStateType newState);
	

}
