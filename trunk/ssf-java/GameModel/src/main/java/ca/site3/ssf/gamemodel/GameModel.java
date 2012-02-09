package ca.site3.ssf.gamemodel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of GameModel
 * 
 * @author Callum
 * @author Greg
 *
 */
public class GameModel implements IGameModel {

	public enum Entity { PLAYER1_ENTITY, PLAYER2_ENTITY, RINGMASTER_ENTITY };
	
	private GameState currState = null;
	private GameState nextState = null;
	
	private GameConfig config = null;
	
	private Player player1 = null;
	private Player player2 = null;
	
	private FireEmitterModel fireEmitterModel = null;
	
	private GameModelActionSignaller actionSignaller = null;
	
	private Logger logger = null;
	
	public GameModel(GameConfig config) {
		this.logger = LoggerFactory.getLogger(getClass());
		
		this.config = config;
		assert(this.config != null);
		
		this.actionSignaller = new GameModelActionSignaller();
		
		this.player1 = new Player(1, this.actionSignaller);
		this.player2 = new Player(2, this.actionSignaller);
		
		this.fireEmitterModel = new FireEmitterModel(new FireEmitterConfig(true, 16, 8), this.actionSignaller);
		
		// Make sure the rest of the model is setup before the state
		this.currState = new IdleGameState(this);
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
		this.currState.killToIdle();
		this.nextState = null;
	}

	public void initiateNextMatchRound() {
		this.currState.initiateNextMatchRound();
	}

	public void togglePauseGame() {
		this.currState.togglePause();
	}
	
	public ActionFactory getActionFactory() {
		return new ActionFactory(this);
	}
	
	public void addGameModelListener(IGameModelListener l) {
		this.actionSignaller.addGameModelListener(l);
	}
	
	public void removeGameModelListener(IGameModelListener l) {
		this.actionSignaller.removeGameModelListener(l);
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
		if (this.nextState.getStateType() == this.currState.getStateType()) {
			return;
		}
		
		this.nextState = nextState;
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
	
	Player getPlayer1() {
		return this.player1;
	}
	Player getPlayer2() {
		return this.player2;
	}
	
	GameModelActionSignaller GetActionSignaller() {
		return this.actionSignaller;
	}
	FireEmitterModel getFireEmitterModel() {
		return this.fireEmitterModel;
	}
	
}
