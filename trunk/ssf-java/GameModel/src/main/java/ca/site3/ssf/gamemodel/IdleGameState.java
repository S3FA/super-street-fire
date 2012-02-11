/**
 * 
 */
package ca.site3.ssf.gamemodel;

/**
 * The idle state does nothing - just waits for a signal to start the game.
 * @author Callum
 * 
 */
class IdleGameState extends GameState {

	final static private double TIME_BETWEEN_RESETS = 0.1;
	private double timeSinceLastReset;
	
	
	/**
	 * Constructor for IdleGameState.
	 * @param gameModel The game model that acts as the context for the states.
	 */
	IdleGameState(GameModel gameModel) {
		super(gameModel);
		
		// Make sure the game is completely reset
		this.gameModel.resetGame();
		this.timeSinceLastReset = 0.0;
	}
	
	@Override
	void tick(double dT) {
		
		// Keep a heart beat of constantly resetting/turning-off all of the fire emitters
		this.timeSinceLastReset += dT;
		if (this.timeSinceLastReset >= IdleGameState.TIME_BETWEEN_RESETS) {
			this.resetAllEmitters();
			assert(this.timeSinceLastReset == 0.0);
		}
		
	}
	
	@Override
	void killToIdle() {
		// Does nothing, we're already in the idle state.
	}
	
	@Override
	void initiateNextState() {
		// Starts a new match by changing the current state...
		this.gameModel.setNextGameState(new RingmasterGameState(this.gameModel));
	}
	
	@Override
	void executeAction(Action action) {
		// Do nothing with the action - we're in an idle state where no actions can be made.
	}
	
	@Override
	void togglePause() {
		// Does nothing - the game is not in any form of play state.
	}
	
	@Override
	GameState.GameStateType getStateType() {
		return GameState.GameStateType.IDLE_STATE;
	}

	/**
	 * Helper function for resetting all of the fire emitters in the game.
	 */
	private void resetAllEmitters() {
		// Ensure that all of the fire emitters in the game are constantly being kept killed
		FireEmitterModel fireEmitterModel = this.gameModel.getFireEmitterModel();
		assert(fireEmitterModel != null);
		fireEmitterModel.resetAllEmitters();
		this.timeSinceLastReset = 0.0;
	}
	
}
