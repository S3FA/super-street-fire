package ca.site3.ssf.gamemodel;

import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.site3.ssf.gamemodel.IGameModelListener.GameStateChangeFlag;

/**
 * Some of the monkey work for implementing an IGameModel
 * 
 * @author greg
 */
public abstract class AbstractGameModel implements IGameModel {

	protected Logger log = LoggerFactory.getLogger(getClass());
	
	
	private GameConfig config;
	
	
	private Collection<IGameModelListener> listeners = new HashSet<IGameModelListener>();
	
	
	
	private GameState currentGameState;
	
	
	
	public GameConfig getGameConfig() {
		return config;
	}

	
	public void setGameConfig(GameConfig config) {
		this.config = config;
	}

	
	public synchronized GameState getGameState() {
		return currentGameState;
	}
	
	
	/**
	 * Update the game state.
	 * @param fireEvent true if listeners should be notified
	 */
	final synchronized void updateGameState(GameState newState, boolean fireEvent) {
		GameState oldState = this.currentGameState;
		this.currentGameState = newState;
		if (fireEvent) {
			fireGameStateChanged(oldState, newState);
		}
	}
	
	
	private synchronized void fireGameStateChanged(GameState oldState, GameState newState) {
		
		EnumSet<GameStateChangeFlag> changes = determineChanges(oldState, newState);
		
		// not syncing on listeners here because our list probably won't change during the game
		for (IGameModelListener l : listeners) {
			try {
				l.onGameStateChanged(oldState, newState, changes);
			} catch (Exception ex) {
				log.warn("Exception firing state change", ex);
			}
		}
	}

	
	private EnumSet<GameStateChangeFlag> determineChanges(GameState oldState, GameState newState) {
		return EnumSet.allOf(GameStateChangeFlag.class);
	}
	
	public void addListener(IGameModelListener l) {
		synchronized(listeners) {
			listeners.add(l);
		}
	}
	
	public void removeListener(IGameModelListener l) {
		synchronized (listeners) {
			listeners.remove(l);
		}
	}
}
