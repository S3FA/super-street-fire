package ca.site3.ssf.gamemodel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import ca.site3.ssf.gamemodel.RoundEndedEvent.RoundResult;

/**
 * State for settling ties in the Super Street Fire game, also aptly referred to
 * as a "sudden death state". 
 * Players will attack each other until one depletes the life of the other first - there
 * is ALWAYS a victor in this state - if both players knock each other out simultaneously
 * then the victory will go 
 * @author Callum
 *
 */
class TieBreakerGameState extends GameState {

	private Collection<Action> activeActions = new ArrayList<Action>();
	
	private double secsSinceLastP1Action = Double.MAX_VALUE;
	private double secsSinceLastP2Action = Double.MAX_VALUE;
	
	private double roundTime = 0.0;
	
	public TieBreakerGameState(GameModel gameModel) {
		super(gameModel);
		
		// Clear the complete fire emitter state, just for good measure
		this.gameModel.getFireEmitterModel().resetAllEmitters();
		
		// Make sure both players health is at full
		Player p1 = this.gameModel.getPlayer1();
		Player p2 = this.gameModel.getPlayer2();
		assert(p1 != null && p2 != null);
		p1.resetHealth();
		p2.resetHealth();
		
		this.gameModel.getActionSignaller().fireOnRoundPlayTimerChanged(0);
	}

	@Override
	void tick(double dT) {
		// Check to see if a player has won/lost the match...
		Player p1 = this.gameModel.getPlayer1();
		Player p2 = this.gameModel.getPlayer2();
		
		if (p1.isKOed() && p2.isKOed()) {
			Player victoryPlayer = null;
			
			// Check to see which player took the most damage...
			if (p1.getNonTruncatedHealth() < p2.getNonTruncatedHealth()) {
				victoryPlayer = p2;
			}
			else if (p2.getNonTruncatedHealth() < p1.getNonTruncatedHealth()) {
				victoryPlayer = p1;
			}
			else {
				// Check to see which player took the most damage last...
				if (p1.getLastDamageAmount() > p2.getLastDamageAmount()) {
					victoryPlayer = p2;
				}
				else if (p2.getLastDamageAmount() > p1.getLastDamageAmount()) {
					victoryPlayer = p1;
				}
				else {
					// Desperation... Randomly choose a winner in the most absurd of dire circumstances...
					int result = ((int)(Math.random() * Integer.MAX_VALUE)) % 2;
					if (result == 0) {
						victoryPlayer = p1;
					}
					else {
						victoryPlayer = p2;
					}
				}
			}
			
			this.onPlayerVictory(victoryPlayer);
			return;
		}
		
		// Check for one player winning or the other...
		if (p1.isKOed()) {
			this.onPlayerVictory(p2);
		}
		else if (p2.isKOed()) {
			this.onPlayerVictory(p1);
		}
		
		// Simulate all of the queued actions...
		Iterator<Action> iter = this.activeActions.iterator();
		while (iter.hasNext()) {
			
			Action currAction = iter.next();
			if (currAction.isFinished()) {
				iter.remove();
				continue;
			}
			currAction.tick(dT);
		}
		
		// Send event to update all the fire emitters...
		this.gameModel.getFireEmitterModel().fireAllEmitterChangedEvent();
		
		// Update time since last attack counters
		this.secsSinceLastP1Action += dT;
		this.secsSinceLastP2Action += dT;
		
		this.roundTime += dT;
		this.gameModel.getActionSignaller().fireOnRoundPlayTimerChanged((int)Math.ceil(this.roundTime));
	}

	@Override
	void killToIdle() {
		this.clearAndResetAllEmitters();
		this.gameModel.setNextGameState(new IdleGameState(this.gameModel));
	}

	@Override
	void initiateNextState() {
		// The tie breaker must play out before going to the next state
	}

	@Override
	void executeAction(Action action) {
		switch (action.getContributorEntity()) {
			case PLAYER1_ENTITY:
				if (this.secsSinceLastP1Action < this.gameModel.getConfig().getMinTimeBetweenPlayerActionsInSecs()) {
					// Player 1 has already made an action recently, exit without counting the current action
					return;
				}
				
				this.secsSinceLastP1Action = 0.0;
				break;
				
			case PLAYER2_ENTITY:
				if (this.secsSinceLastP2Action < this.gameModel.getConfig().getMinTimeBetweenPlayerActionsInSecs()) {
					// Player 2 has already made an action recently, exit without counting the current action
					return;
				}
				
				this.secsSinceLastP2Action = 0.0;
				break;
				
			// We only interpret player actions when the game is in play
			case RINGMASTER_ENTITY:
			default:
				return;
		}
		
		Action.mergeAction(this.activeActions, action);
	}

	@Override
	void togglePause() {
		this.gameModel.setNextGameState(new PausedGameState(this.gameModel, this));
	}

	@Override
	GameStateType getStateType() {
		return GameState.GameStateType.TIE_BREAKER_ROUND_STATE;
	}

	/**
	 * Helper function to clear all active actions in this state and reset all fire emitters.
	 */
	private void clearAndResetAllEmitters() {
		this.activeActions.clear();
		this.gameModel.getFireEmitterModel().resetAllEmitters();
	}
	
	
	private void onPlayerVictory(Player victoryPlayer) {
		assert(victoryPlayer != null);
		
		// Stop all emitters immediately
		this.clearAndResetAllEmitters();
		
		victoryPlayer.incrementNumRoundWins();
		this.gameModel.incrementNumRoundsPlayed();
		
		// Signal an event for the round ending in victory for a player...
		RoundResult result = RoundResult.PLAYER1_VICTORY;
		if (victoryPlayer.getPlayerNumber() == 2) {
			result = RoundResult.PLAYER2_VICTORY;
		}
		
		this.gameModel.getActionSignaller().fireOnRoundEnded(this.gameModel.getNumRoundsPlayed(), result, false);
		this.gameModel.setNextGameState(new MatchEndedGameState(this.gameModel, victoryPlayer));
	}
	
}
