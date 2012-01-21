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
	
	private Collection<IGameModelListener> listeners = new HashSet<IGameModelListener>();
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	private GameConfig config;
	
	public GameModel(GameConfig config) {
		this.config = config;
		assert(this.config != null);
		
		this.player1 = new Player();
		this.player2 = new Player();
		
		this.currState = new IdleGameState(this);
	}
	
	public void tick(double dT) {
		
		// Check to see whether a new state has been set during the previous tick...
		if (this.nextState != null) {
			
			// There is a new/next state that we need to switch to, change to it
			GameState oldState = this.currState;
			this.currState = this.nextState;
			
			// The state has officially changed, fire an event...
			this.fireGameStateChanged(oldState, this.nextState);
			
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
	
	public void addListener(IGameModelListener l) {
		listeners.add(l);
	}
	
	public void removeListener(IGameModelListener l) {
		listeners.remove(l);
	}	
	
	
	/**
	 * Helper method for triggering each of the listeners callbacks for a GameState change.
	 * @param oldState The previous/old state that was replaced.
	 * @param newState The current/new state that was just set.
	 */
	private void fireGameStateChanged(GameState oldState, GameState newState) {
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
