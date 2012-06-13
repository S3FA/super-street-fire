package ca.site3.ssf.gamemodel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import ca.site3.ssf.gamemodel.RoundEndedEvent.RoundResult;

class RoundInPlayState extends GameState {

	private Collection<Action> activeActions = new ArrayList<Action>();
	
	private double secsSinceLastP1Action = Double.MAX_VALUE;
	private double secsSinceLastP2Action = Double.MAX_VALUE;
	
	private double countdownTimeInSecs;
	private int lastRoundedCountdownValueInSecs;
	
	
	public RoundInPlayState(GameModel gameModel) {
		super(gameModel);
		
		// Clear the complete fire emitter state, just for good measure
		this.gameModel.getFireEmitterModel().resetAllEmitters();
		
		// Make sure both players health is at full
		Player p1 = this.gameModel.getPlayer1();
		Player p2 = this.gameModel.getPlayer2();
		assert(p1 != null && p2 != null);
		p1.resetHealth();
		p2.resetHealth();
		
		// Initialize the count down timer
		this.setCountdownTimer(this.gameModel.getConfig().getRoundTimeInSecs());
	}

	@Override
	void tick(double dT) {
		// Check to see if a player has won/lost the match...
		Player p1 = this.gameModel.getPlayer1();
		Player p2 = this.gameModel.getPlayer2();
		
		if (p1.isKOed()) {
			if (!p2.isKOed()) {
				// Player 2 has officially won this match!
				this.roundWasWon(p2);
			}
			else {
				this.roundWasTied();
			}
			return;
		}
		else if (p2.isKOed()) {
			if (!p1.isKOed()) {
				// Player 1 has officially won this match!
				this.roundWasWon(p1);
			}
			else {
				this.roundWasTied();
			}
			return;
		}
		
		// Check to see if the round timer has expired...
		if (this.countdownTimeInSecs <= 0.0) {
			// Match timer just ran out, check which player has the most life left, they win by default
			// if both players have the same amount of life left then it's a tie
			if (p1.getHealth() > p2.getHealth()) {
				this.roundWasWon(p1);
			}
			else if (p2.getHealth() > p1.getHealth()) {
				this.roundWasWon(p2);
			}
			else {
				// Tie...
				this.roundWasTied();
			}
			return;
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
		
		// Update the count down timer...
		this.setCountdownTimer(this.countdownTimeInSecs - dT);
		
		// Update time since last attack counters
		this.secsSinceLastP1Action += dT;
		this.secsSinceLastP2Action += dT;
	}

	@Override
	void killToIdle() {
		// Place the game into the idle state within the next tick
		this.clearAndResetAllEmitters();
		this.gameModel.setNextGameState(new IdleGameState(this.gameModel));
	}

	@Override
	void initiateNextState(GameState.GameStateType nextState) {
		// This is ignored while the game is in play - you can't start the next round
		// until the current one is finished!
	}
	
	@Override
	void executeAction(Action action) {
		
		switch (action.getContributorEntity()) {
			
			case PLAYER1_ENTITY:
				if (this.secsSinceLastP1Action < this.gameModel.getConfig().getMinTimeBetweenPlayerActionsInSecs() &&
					action.getActionFlameType() != FireEmitter.FlameType.BLOCK_FLAME) {
					
					// Player 1 has already made an action recently, exit without counting the current action
					return;
				}
				
				this.secsSinceLastP1Action = 0.0;
				break;
				
			case PLAYER2_ENTITY:
				if (this.secsSinceLastP2Action < this.gameModel.getConfig().getMinTimeBetweenPlayerActionsInSecs() &&
					action.getActionFlameType() != FireEmitter.FlameType.BLOCK_FLAME) {
					
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
		// Pause the game...
		this.gameModel.setNextGameState(new PausedGameState(this.gameModel, this));
	}

	@Override
	GameStateType getStateType() {
		return GameState.GameStateType.ROUND_IN_PLAY_STATE;
	}
	
	int getLastCountdownValueInSecs() {
		return this.lastRoundedCountdownValueInSecs;
	}
	
	/**
	 * Helper function to set the count down timer to the given time. This will not only
	 * set the count down timer value but also perform other functions required whenever
	 * the timer is updated.
	 * 
	 * @param timeInSecs The time to set the count down timer to.
	 */
	private void setCountdownTimer(double timeInSecs) {
		this.countdownTimeInSecs = timeInSecs;
		
		int prevLastRoundedTime = this.lastRoundedCountdownValueInSecs;
		this.lastRoundedCountdownValueInSecs = (int)Math.ceil(this.countdownTimeInSecs);
		
		// Fire off an event if the count down value has changed
		if (prevLastRoundedTime != this.lastRoundedCountdownValueInSecs &&
			this.lastRoundedCountdownValueInSecs >= 0) {
			
			this.gameModel.getActionSignaller().fireOnRoundPlayTimerChanged(this.lastRoundedCountdownValueInSecs);
		}
	}
	
	/**
	 * Helper function to clear all active actions in this state and reset all fire emitters.
	 */
	private void clearAndResetAllEmitters() {
		this.activeActions.clear();
		this.gameModel.getFireEmitterModel().resetAllEmitters();
	}
	
	/**
	 * Helper function, only called when the round has officially been won by one player and
	 * lost by the other.
	 */
	private void roundWasWon(Player victoryPlayer) {
		assert(victoryPlayer != null);
		
		// Stop all emitters immediately
		this.clearAndResetAllEmitters();
		
		// Signal an event for the round ending in victory for a player...
		RoundResult result = RoundResult.PLAYER1_VICTORY;
		if (victoryPlayer.getPlayerNumber() == 2) {
			result = RoundResult.PLAYER2_VICTORY;
		}
		
		victoryPlayer.incrementNumRoundWins();
		this.gameModel.addRoundResult(result);
		
		boolean roundTimedOut = this.roundHasTimedOut();
		this.gameModel.getActionSignaller().fireOnRoundEnded(this.gameModel.getNumRoundsPlayed(), result, roundTimedOut);
		
		GameConfig gameConfig = this.gameModel.getConfig();
		assert(gameConfig != null);
		
		final int NUM_WINS_FOR_VICTORY = gameConfig.getNumRequiredVictoryRoundsForMatchVictory();
		assert(victoryPlayer.getNumRoundWins() <= NUM_WINS_FOR_VICTORY);
		
		Player nonVictoryPlayer = this.gameModel.getPlayer(Player.getOpposingPlayerNum(victoryPlayer.getPlayerNumber()));
		assert(nonVictoryPlayer != null && nonVictoryPlayer != victoryPlayer);
		
		// Check special case of a player match victory on win...
		// The match will be over if the player who won this round has enough wins to beat a match or
		// if they have more wins than the other player and the total number of rounds played is equal
		// to the number of rounds in a match
		if (victoryPlayer.getNumRoundWins() == NUM_WINS_FOR_VICTORY ||
			(victoryPlayer.getNumRoundWins() > nonVictoryPlayer.getNumRoundWins() &&
			 this.gameModel.getNumRoundsPlayed() == gameConfig.getNumRoundsPerMatch())) {
			
			// The player who won this round just won the match as well
			this.gameModel.setNextGameState(new MatchEndedGameState(this.gameModel, victoryPlayer));
			return;
		}

		// The round is over but the match isn't
		this.gameModel.setNextGameState(new RoundEndedGameState(this.gameModel, victoryPlayer, roundTimedOut));
	}
	
	/**
	 * Helper function, only called when the round has officially been tied by both players.
	 */
	private void roundWasTied() {
		// Stop all emitters immediately
		this.clearAndResetAllEmitters();
		
		this.gameModel.addRoundResult(RoundResult.TIE);
		Player p1 = this.gameModel.getPlayer1();
		Player p2 = this.gameModel.getPlayer2();
		
		// We don't increment the number of wins for either player on a tie... 
		boolean roundTimedOut = this.roundHasTimedOut();
		this.gameModel.getActionSignaller().fireOnRoundEnded(this.gameModel.getNumRoundsPlayed(), RoundResult.TIE, roundTimedOut);
		
		GameConfig gameConfig = this.gameModel.getConfig();
		assert(gameConfig != null);
		
		// Check for the special case of a player match victory on a tie:
		// If one player has won more rounds than the other and the number of rounds in a match has been played then
		// the player with the most round victories has won the match...
		if (p1.getNumRoundWins() != p2.getNumRoundWins() &&
			this.gameModel.getNumRoundsPlayed() == gameConfig.getNumRoundsPerMatch()) {
			
			// There was a tie but the match is nevertheless over because one of the players had more wins than the other...
			Player matchWinner = null;
			if (p1.getNumRoundWins() > p2.getNumRoundWins()) {
				matchWinner = p1;
			}
			else {
				assert(p1.getNumRoundWins() < p2.getNumRoundWins());
				matchWinner = p2;
			}
			
			this.gameModel.setNextGameState(new MatchEndedGameState(this.gameModel, matchWinner));
			return;
		}
		
		// Round is over but the match isn't
		this.gameModel.setNextGameState(new RoundEndedGameState(this.gameModel, null, roundTimedOut));
	}

	boolean roundHasTimedOut() {
		return this.countdownTimeInSecs <= 0.0;
	}
	
}
