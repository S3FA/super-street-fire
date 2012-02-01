package ca.site3.ssf.gamemodel;

import java.util.Collection;
import java.util.HashSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of GameModel
 * 
 * @author Callum
 * @author Greg
 *
 */
class GameModel implements IGameModel {

	private GameState currState = null;
	private GameState nextState = null;
	
	private Player player1 = null;
	private Player player2 = null;
	
	private FireEmitterModel fireEmitterModel = null;
	
	private Collection<IGameModelListener> listeners = new HashSet<IGameModelListener>();
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	private GameConfig config = null;
	
	public GameModel(GameConfig config) {
		this.config = config;
		assert(this.config != null);
		
		this.player1 = new Player(1);
		this.player2 = new Player(2);
		
		this.fireEmitterModel = new FireEmitterModel(new FireEmitterConfig(true, 16, 8), this.listeners);
		
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
			this.fireOnGameStateChanged(oldState, this.nextState);
			
			// Clear the next state, we've now officially switched states
			this.nextState = null;
		}
	
		// Tick the current state to simulate the game...
		this.currState.tick(dT);
	}


	public void setNextGameState(GameState nextState) {
		assert(nextState != null);
		
		// Ignore the state change if we're just going to change to the same state
		if (this.nextState.getStateType() == this.currState.getStateType()) {
			return;
		}
		
		this.nextState = nextState;
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
	
	public void addGameModelListener(IGameModelListener l) {
		this.listeners.add(l);
	}
	
	public void removeGameModelListener(IGameModelListener l) {
		this.listeners.remove(l);
	}	
	
	// End IGameModel Interface function implementations *******************************************
	
	/**
	 * Get the player with the given player number.
	 * @param playerNum The number of the player must be either 1 or 2.
	 * @return The player object corresponding to the given player number, null on bad value.
	 */
	public Player getPlayer(int playerNum) {
		
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
	
	public Player getPlayer1() {
		return this.player1;
	}
	public Player getPlayer2() {
		return this.player2;
	}
	
	
	/**
	 * Helper method for triggering each of the listeners callbacks for a GameState change.
	 * @param oldState The previous/old state that was replaced.
	 * @param newState The current/new state that was just set.
	 */
	private void fireOnGameStateChanged(GameState oldState, GameState newState) {
		for (IGameModelListener listener : this.listeners) {
			try {
				listener.onGameStateChanged(oldState.getStateType(), newState.getStateType());
			}
			catch (Exception ex) {
				this.logger.error("Exception occurred while firing game state change", ex);
			}
		}
	}


	
	
	
}
