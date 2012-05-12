package ca.site3.ssf.gamemodel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * A state where the two players can carry out any actions they want in order to
 * get a feel for the game (also for general purpose testing of player actions).
 * Players are invincible in this state - they will not take damage and there is
 * no count down timer - the round must be exited by killing the game to the idle state.
 * 
 * @author Callum
 *
 */
class TestRoundGameState extends GameState {
	
	private Collection<Action> activeActions = new ArrayList<Action>();
	
	private double secsSinceLastP1Action = Double.MAX_VALUE;
	private double secsSinceLastP2Action = Double.MAX_VALUE;
	
	private double roundTime = 0.0;
	
	public TestRoundGameState(GameModel gameModel) {
		super(gameModel);
		
		// Set both players to be invincible
		Player p1 = this.gameModel.getPlayer1();
		Player p2 = this.gameModel.getPlayer2();
		assert(p1 != null && p2 != null);
		
		p1.setInvincible(true);
		p2.setInvincible(true);
	}

	@Override
	void tick(double dT) {
		
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
		// Turn off invincibility for both players
		Player p1 = this.gameModel.getPlayer1();
		Player p2 = this.gameModel.getPlayer2();
		assert(p1 != null && p2 != null);
		
		p1.setInvincible(false);
		p2.setInvincible(false);
		
		// Place the game into the idle state within the next tick
		this.clearAndResetAllEmitters();
		this.gameModel.setNextGameState(new IdleGameState(this.gameModel));
	}

	@Override
	void initiateNextState(GameState.GameStateType nextState) {
		// This is ignored - killToIdle in order go get out of this state
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
		// Pause the game...
		this.gameModel.setNextGameState(new PausedGameState(this.gameModel, this));
	}

	@Override
	GameStateType getStateType() {
		return GameState.GameStateType.TEST_ROUND_STATE;
	}

	/**
	 * Helper function to clear all active actions in this state and reset all fire emitters.
	 */
	private void clearAndResetAllEmitters() {
		this.activeActions.clear();
		this.gameModel.getFireEmitterModel().resetAllEmitters();
	}
}
