package ca.site3.ssf.gamemodel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

class RoundInPlayState extends GameState {

	private Collection<Action> activeActions = new ArrayList<Action>();
	
	private double secsSinceLastP1Action = Double.MAX_VALUE;
	private double secsSinceLastP2Action = Double.MAX_VALUE;
	
	private double countdownTimeInSecs;
	private int lastRoundedCountdownValueInSecs;
	
	
	public RoundInPlayState(GameModel gameModel) {
		super(gameModel);
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
				this.roundWasWonLost(p2);
			}
			else {
				this.roundWasTied();
			}
			return;
		}
		else if (p2.isKOed()) {
			if (!p1.isKOed()) {
				// Player 1 has officially won this match!
				this.roundWasWonLost(p1);
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
				this.roundWasWonLost(p1);
			}
			else if (p2.getHealth() > p1.getHealth()) {
				this.roundWasWonLost(p2);
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
		
		// Update the count down timer...
		this.setCountdownTimer(this.countdownTimeInSecs - dT);
	}

	@Override
	void killToIdle() {
		// Place the game into the idle state within the next tick
		this.gameModel.setNextGameState(new IdleGameState(this.gameModel));
	}

	@Override
	void initiateNextState() {
		// This is ignored while the game is in play - you can't start the next round
		// until the current one is finished!
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
		
		this.activeActions.add(action);
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
	
	private void setCountdownTimer(double timeInSecs) {
		this.countdownTimeInSecs = timeInSecs;
		
		int prevLastRoundedTime = this.lastRoundedCountdownValueInSecs;
		this.lastRoundedCountdownValueInSecs = (int)this.countdownTimeInSecs;
		
		// Fire off an event if the count down value has changed
		if (prevLastRoundedTime != this.lastRoundedCountdownValueInSecs &&
			this.lastRoundedCountdownValueInSecs >= 0) {
			
			this.gameModel.getActionSignaller().fireOnRoundPlayTimerChanged(this.lastRoundedCountdownValueInSecs);
		}
	}
	
	/**
	 * Helper function, only called when the round has officially been won by one player and
	 * lost by the other.
	 */
	private void roundWasWonLost(Player victoryPlayer) {
		assert(victoryPlayer != null);
		
		victoryPlayer.incrementNumRoundWins();
		this.gameModel.incrementNumRoundsPlayed();
		
		// Check to see if the match is over...
		GameConfig gameConfig = this.gameModel.getConfig();
		assert(gameConfig != null);
		
		final int NUM_WINS_FOR_VICTORY = gameConfig.getNumRequiredVictoryRoundsForMatchVictory();
		assert(victoryPlayer.getNumRoundWins() <= NUM_WINS_FOR_VICTORY);
		if (victoryPlayer.getNumRoundWins() == NUM_WINS_FOR_VICTORY) {
			// The player who won this round just won the match as well
			this.gameModel.setNextGameState(new MatchOverGameState(this.gameModel, victoryPlayer));
			return;
		}
		
		// The match should still have rounds left to play
		assert(this.gameModel.getNumRoundsPlayed() < gameConfig.getNumRoundsPerMatch());

		// The round is over but the match isn't
		this.gameModel.setNextGameState(new RoundEndedGameState(this.gameModel, victoryPlayer));
	}
	
	/**
	 * Helper function, only called when the round has officially been tied by both players.
	 */
	private void roundWasTied() {
		this.gameModel.incrementNumRoundsPlayed();
		Player p1 = this.gameModel.getPlayer1();
		Player p2 = this.gameModel.getPlayer2();
		
		p1.incrementNumRoundWins();
		p2.incrementNumRoundWins();
		
		GameConfig gameConfig = this.gameModel.getConfig();
		assert(gameConfig != null);
		
		final int NUM_WINS_FOR_VICTORY = gameConfig.getNumRequiredVictoryRoundsForMatchVictory();
		assert(p1.getNumRoundWins() <= NUM_WINS_FOR_VICTORY);
		assert(p2.getNumRoundWins() <= NUM_WINS_FOR_VICTORY);
		
		// Check for a complete match tie (i.e., over the entire match there has
		// been a full tie between players) - this should almost never happen...
		if (p1.getNumRoundWins() == NUM_WINS_FOR_VICTORY && p2.getNumRoundWins() == NUM_WINS_FOR_VICTORY) {
			// Go to a tie breaker state...
			this.gameModel.setNextGameState(new TieBreakerGameState(this.gameModel));
			return;
		}
		
		// Check to see if either player won the game...
		if (this.checkForMatchOverVictory(p1) || this.checkForMatchOverVictory(p2)) {
			return;
		}
		
		// Round is over but the match isn't
		this.gameModel.setNextGameState(new RoundEndedGameState(this.gameModel, null));
	}
	
	private boolean checkForMatchOverVictory(Player playerToCheck) {
		// Check to see if the match is over...
		GameConfig gameConfig = this.gameModel.getConfig();
		assert(gameConfig != null);
		
		final int NUM_WINS_FOR_VICTORY = gameConfig.getNumRequiredVictoryRoundsForMatchVictory();
		assert(playerToCheck.getNumRoundWins() <= NUM_WINS_FOR_VICTORY);
		if (playerToCheck.getNumRoundWins() == NUM_WINS_FOR_VICTORY) {
			// The player who won this round just won the match as well
			this.gameModel.setNextGameState(new MatchOverGameState(this.gameModel, playerToCheck));
			return true;
		}
		
		return false;
	}
	

}