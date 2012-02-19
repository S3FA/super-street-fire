package ca.site3.ssf.gamemodel;

import java.util.Collection;
import java.util.HashSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.site3.ssf.gamemodel.IGameModelListener.GameResult;

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
		boolean success = this.listeners.add(l);
		assert(success);
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
		GameState.GameStateType oldStateType = GameState.GameStateType.NO_STATE;
		GameState.GameStateType newStateType = GameState.GameStateType.NO_STATE;
		if (oldState != null) {
			oldStateType = oldState.getStateType();
		}
		if (newState != null) {
			newStateType = newState.getStateType();
		}
		
		for (IGameModelListener listener : this.listeners) {
			try {
				listener.onGameStateChanged(oldStateType, newStateType);
			}
			catch (Exception ex) {
				this.logger.error("Exception occurred while firing game state change event", ex);
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
				this.logger.error("Exception occurred while firing player health change event", ex);
			}
		}
	}
	
	void fireOnRoundPlayTimerChanged(int newCountdownTimeInSecs) {
		for (IGameModelListener listener : this.listeners) {
			try {
				listener.onRoundPlayTimerChanged(newCountdownTimeInSecs);
			}
			catch (Exception ex) {
				this.logger.error("Exception occurred while firing round in-play timer changed event", ex);
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
				this.logger.error("Exception occurred while firing round begin fight timer changed event", ex);
			}
		}
	}
	
	/**
	 * Triggers each of the listener's callbacks for a round ended event.
	 * @param roundResult The round result.
	 * @param roundTimedOut Whether the round timed out or not.
	 */
	void fireOnRoundEnded(IGameModelListener.GameResult roundResult, boolean roundTimedOut) {
		for (IGameModelListener listener : this.listeners) {
			try {
				listener.onRoundEnded(roundResult, roundTimedOut);
			}
			catch (Exception ex) {
				this.logger.error("Exception occurred while firing round ended event", ex);
			}
		}
	}
	
	/**
	 * Triggers each of the listener's callbacks for the match ended event.
	 * @param matchResult The match result.
	 */
	void fireOnMatchEnded(GameResult matchResult) {
		for (IGameModelListener listener : this.listeners) {
			try {
				listener.onMatchEnded(matchResult);
			}
			catch (Exception ex) {
				this.logger.error("Exception occurred while firing match ended event", ex);
			}
		}
		
	}
	
	/**
	 * Triggers each of the listener's callbacks for a player attack event.
	 * @param playerNum Attacker player number.
	 * @param attackType The type of attack.
	 */
	void fireOnPlayerAttackAction(int playerNum, PlayerAttackAction.AttackType attackType) {
		for (IGameModelListener listener : this.listeners) {
			try {
				listener.onPlayerAttackAction(playerNum, attackType);
			}
			catch (Exception ex) {
				this.logger.error("Exception occurred while firing player attack action event", ex);
			}
		}
	}
	
	/**
	 * Triggers each of the listener's callbacks for a player block event.
	 * @param playerNum Blocker player number.
	 */
	void fireOnPlayerBlockAction(int playerNum) {
		for (IGameModelListener listener : this.listeners) {
			try {
				listener.onPlayerBlockAction(playerNum);
			}
			catch (Exception ex) {
				this.logger.error("Exception occurred while firing player block action event", ex);
			}
		}
	}
	
	/**
	 * Triggers each of the listener's callbacks for a ringmaster action event.
	 */
	void fireOnRingmasterAction() {
		for (IGameModelListener listener : this.listeners) {
			try {
				listener.onRingmasterAction();
			}
			catch (Exception ex) {
				this.logger.error("Exception occurred while firing ringmaster action event", ex);
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
				this.logger.error("Exception occurred while firing fire emitter changed event", ex);
			}
		}
	}
	
}
