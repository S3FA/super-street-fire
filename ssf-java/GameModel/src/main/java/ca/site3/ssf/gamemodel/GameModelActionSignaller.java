package ca.site3.ssf.gamemodel;

import java.util.Collection;
import java.util.HashSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.site3.ssf.gamemodel.MatchEndedEvent.MatchResult;
import ca.site3.ssf.gamemodel.RoundEndedEvent.RoundResult;

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
		
		GameStateChangedEvent event = new GameStateChangedEvent(oldStateType, newStateType);
		for (IGameModelListener listener : this.listeners) {
			try {
				listener.onGameModelEvent(event);
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
		
		PlayerHealthChangedEvent event = new PlayerHealthChangedEvent(playerNum, prevLifePercentage, newLifePercentage);
		for (IGameModelListener listener : this.listeners) {
			try {
				listener.onGameModelEvent(event);
			}
			catch (Exception ex) {
				this.logger.error("Exception occurred while firing player health change event", ex);
			}
		}
	}
	
	void fireOnRoundPlayTimerChanged(int newCountdownTimeInSecs) {
		
		RoundPlayTimerChangedEvent event = new RoundPlayTimerChangedEvent(newCountdownTimeInSecs);
		for (IGameModelListener listener : this.listeners) {
			try {
				listener.onGameModelEvent(event);
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
	void fireOnRoundBeginFightTimerChanged(RoundBeginTimerChangedEvent.RoundBeginCountdownType threeTwoOneFightTime) {
		RoundBeginTimerChangedEvent event = new RoundBeginTimerChangedEvent(threeTwoOneFightTime);
		for (IGameModelListener listener : this.listeners) {
			try {
				listener.onGameModelEvent(event);
			}
			catch (Exception ex) {
				this.logger.error("Exception occurred while firing round begin fight timer changed event", ex);
			}
		}
	}
	
	/**
	 * Triggers each of the listener's callbacks for a round ended event.
	 * @param roundNumber The number of the round that ended.
	 * @param roundResult The round result.
	 * @param roundTimedOut Whether the round timed out or not.
	 */
	void fireOnRoundEnded(int roundNumber, RoundResult roundResult, boolean roundTimedOut) {
		RoundEndedEvent event = new RoundEndedEvent(roundNumber, roundResult, roundTimedOut);
		for (IGameModelListener listener : this.listeners) {
			try {
				listener.onGameModelEvent(event);
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
	void fireOnMatchEnded(MatchResult matchResult) {
		MatchEndedEvent event = new MatchEndedEvent(matchResult);
		for (IGameModelListener listener : this.listeners) {
			try {
				listener.onGameModelEvent(event);
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
		PlayerAttackActionEvent event = new PlayerAttackActionEvent(playerNum, attackType);
		for (IGameModelListener listener : this.listeners) {
			try {
				listener.onGameModelEvent(event);
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
		PlayerBlockActionEvent event = new PlayerBlockActionEvent(playerNum);
		for (IGameModelListener listener : this.listeners) {
			try {
				listener.onGameModelEvent(event);
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
		RingmasterActionEvent event = new RingmasterActionEvent();
		for (IGameModelListener listener : this.listeners) {
			try {
				listener.onGameModelEvent(event);
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
				listener.onGameModelEvent(new FireEmitterChangedEvent(fireEmitter));
			}
			catch (Exception ex) {
				this.logger.error("Exception occurred while firing fire emitter changed event", ex);
			}
		}
	}
	
}
