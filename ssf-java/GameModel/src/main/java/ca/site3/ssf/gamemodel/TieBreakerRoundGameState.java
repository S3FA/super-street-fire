package ca.site3.ssf.gamemodel;

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
class TieBreakerGameState extends PlayerFightingGameState {

	private double roundTime = 0.0;
	
	public TieBreakerGameState(GameModel gameModel) {
		super(gameModel, true);
		
		this.gameModel.getActionSignaller().fireOnRoundPlayTimerChanged(0);
	}

	@Override
	void tick(double dT) {
		super.tick(dT);
		
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
				this.removeAction(currAction);
				continue;
			}
			currAction.tick(dT);
		}
		
		// Send event to update all the fire emitters...
		this.gameModel.getFireEmitterModel().fireAllEmitterChangedEvent();
		
		// Update time since last attack counters
		this.secsSinceLastP1LeftAction  += dT;
		this.secsSinceLastP1RightAction += dT;
		this.secsSinceLastP2LeftAction  += dT;
		this.secsSinceLastP2RightAction += dT;
		
		this.roundTime += dT;
		this.gameModel.getActionSignaller().fireOnRoundPlayTimerChanged((int)Math.ceil(this.roundTime));
	}

	@Override
	GameStateType getStateType() {
		return GameState.GameStateType.TIE_BREAKER_ROUND_STATE;
	}

	private void onPlayerVictory(Player victoryPlayer) {
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
		this.gameModel.getActionSignaller().fireOnRoundEnded(this.gameModel.getNumRoundsPlayed(), result, false, gameModel.getPlayer1().getHealth(), gameModel.getPlayer2().getHealth());
		this.gameModel.setNextGameState(new MatchEndedGameState(this.gameModel, victoryPlayer));
	}
	
}
