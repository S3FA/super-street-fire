package ca.site3.ssf.gamemodel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of GameModel.
 * 
 * @author Callum
 * @author Greg
 *
 */
public class GameModel implements IGameModel {

	private GameState currState = null;
	private GameState nextState = null;
	
	private GameConfig config = null;
	
	private Player player1 = null;
	private Player player2 = null;
	
	private FireEmitterModel fireEmitterModel = null;
	
	private GameModelActionSignaller actionSignaller = null;
	
	private Logger logger = null;
	
	private int numRoundsPlayed; // The number of rounds that are played
	
	public GameModel(GameConfig config) {
		this.logger = LoggerFactory.getLogger(getClass());
		
		this.config = config;
		assert(this.config != null);
		
		this.numRoundsPlayed = 0;
		this.actionSignaller = new GameModelActionSignaller();
		
		this.player1 = new Player(1, this.actionSignaller);
		this.player2 = new Player(2, this.actionSignaller);
		
		this.fireEmitterModel = new FireEmitterModel(new FireEmitterConfig(true, 16, 8), this.actionSignaller);
		
		// Make sure the rest of the model is setup before the state
		this.nextState = new IdleGameState(this);
	}
	
	// Begin IGameModel Interface function implementations *******************************************
	
	public void tick(double dT) {
		
		// Check to see whether a new state has been set during the previous tick...
		if (this.nextState != null) {
			
			// There is a new/next state that we need to switch to, change to it
			GameState oldState = this.currState;
			this.currState = this.nextState;
			
			// The state has officially changed, fire an event...
			this.actionSignaller.fireOnGameStateChanged(oldState, this.nextState);
			
			// Clear the next state, we've now officially switched states
			this.nextState = null;
		}
	
		// Tick the current state to simulate the game...
		this.currState.tick(dT);
	}
	
	public void killGame() {
		this.logger.info("Request to kill the game was received, killing game to idle state.");
		
		this.nextState = null;
		this.currState.killToIdle();
	}

	public void initiateNextState() {
		this.currState.initiateNextState();
	}

	public void togglePauseGame() {
		this.currState.togglePause();
	}
	
	public ActionFactory getActionFactory() {
		return new ActionFactory(this);
	}
	
	public void executeGenericAction(Action action) {
		this.currState.executeAction(action);
	}
	
	public void addGameModelListener(IGameModelListener l) {
		this.actionSignaller.addGameModelListener(l);
	}
	
	public void removeGameModelListener(IGameModelListener l) {
		this.actionSignaller.removeGameModelListener(l);
	}	
	
	public void executeCommand(AbstractGameModelCommand command) {
		command.execute(this);
	}
	
	// End IGameModel Interface function implementations *******************************************
	
	/**
	 * Sets the next game state to the given state, the state will officially be
	 * updated on the next Tick of the GameModel.
	 * @param nextState The next state that the game will be changed to.
	 */
	void setNextGameState(GameState nextState) {
		assert(nextState != null);
		
		// Ignore the state change if we're just going to change to the same state
		if (nextState.getStateType() == this.currState.getStateType()) {
			return;
		}

		this.logger.info("Changing game state on next tick to " + nextState.getStateType().toString());
		this.nextState = nextState;
	}	
	
	/**
	 * Completely resets the game data and turns all emitters off.
	 */
	void resetGame() {
		// Make sure the game is completely reset:
		// - All emitters must be turned off
		// - All players must have full health restored and all record of wins/losses wiped
		this.getFireEmitterModel().resetAllEmitters();
		this.getPlayer1().reset();
		this.getPlayer2().reset();
		assert(this.numRoundsPlayed <= this.getConfig().getNumRoundsPerMatch());
		this.numRoundsPlayed = 0;
	}
	
	void incrementNumRoundsPlayed() {
		this.numRoundsPlayed++;
	}
	int getNumRoundsPlayed() {
		return this.numRoundsPlayed;
	}
	
	
	/**
	 * Get the player with the given player number.
	 * @param playerNum The number of the player must be either 1 or 2.
	 * @return The player object corresponding to the given player number, null on bad value.
	 */
	Player getPlayer(int playerNum) {
		
		switch (playerNum) {
			case 1:
				return this.getPlayer1();
			case 2:
				return this.getPlayer2();
			default:
				assert(false);
				break;
		}
		return null;
	}
	
	Player getPlayer(GameModel.Entity entity) {
		switch (entity) {
			case PLAYER1_ENTITY:
				return this.getPlayer1();
			case PLAYER2_ENTITY:
				return this.getPlayer2();
			default:
				assert(false);
				break;
		}
		return null;
	}
	
	Player getPlayer1() {
		return this.player1;
	}
	Player getPlayer2() {
		return this.player2;
	}
	
	GameConfig getConfig() {
		return this.config;
	}
	
	GameModelActionSignaller getActionSignaller() {
		return this.actionSignaller;
	}
	FireEmitterModel getFireEmitterModel() {
		return this.fireEmitterModel;
	}
	
}
