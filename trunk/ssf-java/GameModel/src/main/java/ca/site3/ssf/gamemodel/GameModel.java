package ca.site3.ssf.gamemodel;

import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.site3.ssf.gamemodel.IGameModelListener.GameStateChangeFlag;

/**
 * Default implementation of GameModel
 * 
 * @author Callum
 * @author Greg
 *
 */
public class GameModel implements IGameModel {

	private GameState currState = null;
	private GameState nextState = null;
	
	private Collection<IGameModelListener> listeners = new HashSet<IGameModelListener>();
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	private GameConfig config;
	
	public GameModel(GameConfig config) {
		this.config = config;
		assert(this.config != null);
	}
	
	public void tick(double dT) {
		
		// Check to see whether a new state has been set during the previous tick...
		if (this.nextState != null) {
			
			GameState oldState = this.currState;
			this.currState = this.nextState;
			
			// The state has officially changed, fire an event...
			this.fireGameStateChanged(oldState, this.nextState);
			
			// Clear the next state, we've now officially switched states
			this.nextState = null;
		}
	
		this.currState.tick(dT);
	}


	public void setNextGameState(GameState nextState) {
		assert(nextState != null);
		this.nextState = nextState;
	}
	
	public void kill() {
		this.currState.killToIdle();
		this.nextState = null;
	}

	public void initiateNextMatchRound() {
		this.currState.initiateNextMatchRound();
	}

	public void togglePause() {
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
