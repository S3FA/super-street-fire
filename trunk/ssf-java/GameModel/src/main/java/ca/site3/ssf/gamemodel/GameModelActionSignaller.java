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
	 * Triggers each of the listener's callbacks for a player action point change.
	 * @param playerNum The player number of the player whose action points changed.
	 * @param prevActionPointAmt The player's previous action point amount, before the change.
	 * @param newActionPointAmt The player's new action point amount after the change.
	 */
	void fireOnPlayerActionPointsChanged(int playerNum, float prevActionPointAmt, float newActionPointAmt) {
		PlayerActionPointsChangedEvent event = new PlayerActionPointsChangedEvent(playerNum, prevActionPointAmt, newActionPointAmt);
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
	void fireOnRoundEnded(int roundNumber, RoundResult roundResult, boolean roundTimedOut, double p1Health, double p2Health) {
		RoundEndedEvent event = new RoundEndedEvent(roundNumber, roundResult, roundTimedOut, p1Health, p2Health);
		this.fireGameModelEvent(event);
	}
	
	/**
	 * Triggers each of the listener's callbacks for the match ended event.
	 * @param matchResult The match result.
	 */
	void fireOnMatchEnded(MatchResult matchResult, double p1Health, double p2Health) {
		MatchEndedEvent event = new MatchEndedEvent(matchResult, p1Health, p2Health);
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
	 * Triggers each of the listener's callbacks for a player attack failure event.
	 * @param playerNum Attacker player number.
	 * @param attackType The type of attack.
	 * @param reason The reason for the failure.
	 */
	void fireOnAttackFailedAction(int playerNum, PlayerAttackAction.AttackType attackType, PlayerAttackActionFailedEvent.Reason reason) {
		PlayerAttackActionFailedEvent event = new PlayerAttackActionFailedEvent(playerNum, attackType, reason);
		this.fireGameModelEvent(event);
	}
	
	/**
	 * Triggers each of the listener's callbacks for a player block event.
	 * @param playerNum Blocker player number.
	 * @param blockWasEffective Whether the block was effective or not (i.e., it actually blocked something).
	 */
	void fireOnPlayerBlockAction(int playerNum, boolean blockWasEffective) {
		PlayerBlockActionEvent event = new PlayerBlockActionEvent(playerNum, blockWasEffective);
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

	/**
	 * Triggers each of the listener's callbacks for a blocking window event.
	 * @param blockWindowID The ID of the blocking window signalling the event.
	 * @param blockWindowExpired Whether the blocking window just expired or not.
	 * @param blockWindowTimeLengthInSecs The length of the block window when first started, only valid if the window hasn't yet expired.
	 * @param blockingPlayerNum The player who the block window applies to (i.e., the player who must block).
	 */
	void fireOnBlockWindowEvent(int blockWindowID, boolean blockWindowExpired, double blockWindowTimeLengthInSecs, int blockingPlayerNum) {
		BlockWindowEvent event = new BlockWindowEvent(blockWindowID, blockWindowExpired, blockWindowTimeLengthInSecs, blockingPlayerNum);
		this.fireGameModelEvent(event);
	}
}
