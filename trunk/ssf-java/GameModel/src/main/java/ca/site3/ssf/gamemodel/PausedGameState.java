package ca.site3.ssf.gamemodel;

/**
 * The PausedGameState is the state that the game will go to when paused,
 * this state keeps track of the state that was paused and will go back to
 * that state when unpaused.
 * @author Callum
 * @author Greg
 */
public class PausedGameState extends GameState {

	private GameState pausedState = null;
	
	public PausedGameState(GameModel gameModel, GameState pausedState) {
		super(gameModel);
		this.pausedState = pausedState;
		assert(this.pausedState != null);
	}

	@Override
	public void tick(double dT) {
		// The game is paused...
	}

	@Override
	public void killToIdle() {
		// Apply this to the paused state - since that state might want to do
		// some clean-up / upkeep before being killed and transitioned to Idle
		this.pausedState.killToIdle();
	}

	@Override
	public void initiateNextMatchRound() {
		// Ignore this, the game is paused...
	}

	@Override
	public void togglePause() {
		// Un-pause the game (go back to the previously paused state)
		this.gameModel.setNextGameState(this.pausedState);
	}

	@Override
	public GameState.GameStateType getStateType() {
		return GameState.GameStateType.PAUSED_STATE;
	}

}
