package ca.site3.ssf.gamemodel;

import java.util.Collection;
import java.util.HashSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.site3.ssf.gamemodel.ActionFactory.ActionType;
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
	 * Helper function for executing the given event for all listeners and dealing with exceptions.
	 * @param event The event to execute/fire-off for all listeners.
	 */
	private void fireGameModelEvent(IGameModelEvent event) {
		for (IGameModelListener listener : this.listeners) {
			try {
				listener.onGameModelEvent(event);
			}
			catch (Exception ex) {
				this.logger.error("Exception occurred while firing event " + event.getClass().getName(), ex);
			}
		}
	}
	
	void fireOnQueryGameInfoRefresh(GameInfoRefreshEvent event) {
		this.fireGameModelEvent(event);
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
		this.fireGameModelEvent(event);
	}	
	
	/**
	 * Triggers each of the listener's callbacks for a player health change.
	 * @param playerNum The player number of the player whose health changed.
	 * @param prevLifePercentage The player's previous health amount, before the change.
	 * @param newLifePercentage The player's new health amount, after the change. 
	 */
	void fireOnPlayerHealthChanged(int playerNum, float prevLifePercentage, float newLifePercentage) {
		PlayerHealthChangedEvent event = new PlayerHealthChangedEvent(playerNum, prevLifePercentage, newLifePercentage);
		this.fireGameModelEvent(event);
	}
	
	/**
	 * Triggers each of the listener's callbacks for when the round-in-play timer changes.
	 * @param newTimeInSecs The latest time on the round-in-play timer.
	 */
	void fireOnRoundPlayTimerChanged(int newTimeInSecs) {
		RoundPlayTimerChangedEvent event = new RoundPlayTimerChangedEvent(newTimeInSecs);
		this.fireGameModelEvent(event);
	}
	
	/**
	 * Triggers each of the listener's callbacks for a round begin fight timer change.
	 * @param threeTwoOneFightTime The latest/current value of the timer.
	 */
	void fireOnRoundBeginFightTimerChanged(RoundBeginTimerChangedEvent.RoundBeginCountdownType threeTwoOneFightTime, int roundNumber) {
		RoundBeginTimerChangedEvent event = new RoundBeginTimerChangedEvent(threeTwoOneFightTime, roundNumber);
		this.fireGameModelEvent(event);
	}
	
	/**
	 * Triggers each of the listener's callbacks for a round ended event.
	 * @param roundNumber The number of the round that ended.
	 * @param roundResult The round result.
	 * @param roundTimedOut Whether the round timed out or not.
	 */
	void fireOnRoundEnded(int roundNumber, RoundResult roundResult, boolean roundTimedOut) {
		RoundEndedEvent event = new RoundEndedEvent(roundNumber, roundResult, roundTimedOut);
		this.fireGameModelEvent(event);
	}
	
	/**
	 * Triggers each of the listener's callbacks for the match ended event.
	 * @param matchResult The match result.
	 */
	void fireOnMatchEnded(MatchResult matchResult) {
		MatchEndedEvent event = new MatchEndedEvent(matchResult);
		this.fireGameModelEvent(event);
	}
	
	/**
	 * Triggers each of the listener's callbacks for a player attack event.
	 * @param playerNum Attacker player number.
	 * @param attackType The type of attack.
	 */
	void fireOnPlayerAttackAction(int playerNum, PlayerAttackAction.AttackType attackType) {
		PlayerAttackActionEvent event = new PlayerAttackActionEvent(playerNum, attackType);
		this.fireGameModelEvent(event);
	}
	
	/**
	 * Triggers each of the listener's callbacks for a player block event.
	 * @param playerNum Blocker player number.
	 */
	void fireOnPlayerBlockAction(int playerNum) {
		PlayerBlockActionEvent event = new PlayerBlockActionEvent(playerNum);
		this.fireGameModelEvent(event);
	}
	
	/**
	 * Triggers each of the listener's callbacks for a ringmaster action event.
	 * @param action The ringmaster action being fired.
	 */
	void fireOnRingmasterAction(RingmasterAction.ActionType action) {
		RingmasterActionEvent event = new RingmasterActionEvent(action);
		this.fireGameModelEvent(event);
	}
	
	/**
	 * Triggers each of the listener's callbacks for a FireEmitter change.
	 * @param fireEmitter The emitter that changed.
	 */
	void fireOnFireEmitterChanged(FireEmitter fireEmitter) {
		FireEmitterChangedEvent event = new FireEmitterChangedEvent(fireEmitter);
		this.fireGameModelEvent(event);
	}
	
	/**
	 * Triggers each of the listener's callbacks for a unrecognized gesture event.
	 * @param entity The entity whose gesture was unrecognized.
	 */
	void fireOnUnrecognizedGestureEvent(IGameModel.Entity entity) {
		UnrecognizedGestureEvent event = new UnrecognizedGestureEvent(entity);
		this.fireGameModelEvent(event);
	}
}
