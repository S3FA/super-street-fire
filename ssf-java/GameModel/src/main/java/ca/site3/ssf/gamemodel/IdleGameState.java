/**
 * 
 */
package ca.site3.ssf.gamemodel;

/**
 * The idle state does nothing - just waits for a signal to start the game.
 * @author Callum
 * 
 */
public class IdleGameState extends GameState {

	/**
	 * Constructor for IdleGameState.
	 * @param gameModel The game model that acts as the context for the states.
	 */
	public IdleGameState(GameModel gameModel) {
		super(gameModel);
	}

	public void tick(double dT) {
		// TODO Auto-generated method stub

	}

	public void killToIdle() {
		// Does nothing, we're already in the idle state.
	}

	public void initiateNextMatchRound() {
		// Starts a new match by changing the current state...
		this.gameModel.setNextGameState(new RoundBeginningGameState(this.gameModel, 1));
	}

	public void togglePause() {
		// Does nothing.
	}

	public GameState.GameStateType getStateType() {
		return GameState.GameStateType.IDLE_STATE;
	}

}
