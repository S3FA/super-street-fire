package ca.site3.ssf.gamemodel;

import java.util.Collection;
import java.util.HashSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The GameModelActionSignaller provides a centralized location for all methods used
 * to fire off events for all of the registered event listeners for the gamemodel package.
 * This class should be used across the gamemodel whenever a relevant event takes place
 * in order to notify all listeners.
 * 
 * @author Callum
 *
 */
class GameModelActionSignaller {
	
	private Collection<IGameModelListener> listeners = null;
	private Logger logger = null;
	
	GameModelActionSignaller() {
		this.logger    = LoggerFactory.getLogger(getClass());
		this.listeners = new HashSet<IGameModelListener>();
	}
	
	void addGameModelListener(IGameModelListener l) {
		this.listeners.add(l);
	}
	
	void removeGameModelListener(IGameModelListener l) {
		this.listeners.remove(l);
	}	
	
	/**
	 * Triggers each of the listener's callbacks for a GameState change.
	 * @param oldState The previous/old state that was replaced.
	 * @param newState The current/new state that was just set.
	 */
	void fireOnGameStateChanged(GameState oldState, GameState newState) {
		for (IGameModelListener listener : this.listeners) {
			try {
				listener.onGameStateChanged(oldState.getStateType(), newState.getStateType());
			}
			catch (Exception ex) {
				this.logger.error("Exception occurred while firing game state change", ex);
			}
		}
	}	
	
	/**
	 * Triggers each of the listener's callbacks for a player health change.
	 * @param playerNum The player number of the player whose health changed.
	 * @param prevLifePercentage The player's previous health amount, before the change.
	 * @param newLifePercentage The player's new health amount, after the change. 
	 */
	void fireOnPlayerHealthChanged(int playerNum, float prevLifePercentage, float newLifePercentage) {
		for (IGameModelListener listener : this.listeners) {
			try {
				listener.onPlayerHealthChanged(playerNum, prevLifePercentage, newLifePercentage);
			}
			catch (Exception ex) {
				this.logger.error("Exception occurred while firing player health change", ex);
			}
		}
	}
	
	void fireOnRoundPlayTimerChanged(int newCountdownTimeInSecs) {
		for (IGameModelListener listener : this.listeners) {
			try {
				listener.onRoundPlayTimerChanged(newCountdownTimeInSecs);
			}
			catch (Exception ex) {
				this.logger.error("Exception occurred while firing round in-play timer changed", ex);
			}
		}
	}
	
	/**
	 * Triggers each of the listener's callbacks for a round begin fight timer change.
	 * @param threeTwoOneFightTime The latest/current value of the timer.
	 */
	void fireOnRoundBeginFightTimerChanged(IGameModelListener.RoundBeginCountdownType threeTwoOneFightTime) {
		for (IGameModelListener listener : this.listeners) {
			try {
				listener.onRoundBeginFightTimerChanged(threeTwoOneFightTime);
			}
			catch (Exception ex) {
				this.logger.error("Exception occurred while firing round begin fight timer changed", ex);
			}
		}
	}
	
	/**
	 * Triggers each of the listener's callbacks for a FireEmitter change.
	 * @param fireEmitter The emitter that changed.
	 */
	void fireOnFireEmitterChanged(FireEmitter fireEmitter) {
		for (IGameModelListener listener : this.listeners) {
			try {
				listener.onFireEmitterChanged(new ImmutableFireEmitter(fireEmitter));
			}
			catch (Exception ex) {
				this.logger.error("Exception occurred while firing fire emitter changed", ex);
			}
		}
	}
	
}
