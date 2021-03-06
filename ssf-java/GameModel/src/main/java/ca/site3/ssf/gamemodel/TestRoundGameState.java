package ca.site3.ssf.gamemodel;

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
class TestRoundGameState extends PlayerFightingGameState {
	
	private double roundTime = 0.0;
	
	public TestRoundGameState(GameModel gameModel) {
		super(gameModel, false);
		
		// Set both players to be invincible
		Player p1 = this.gameModel.getPlayer1();
		Player p2 = this.gameModel.getPlayer2();
		assert(p1 != null && p2 != null);
		
		p1.setInvincible(true);
		p2.setInvincible(true);
		
		this.gameModel.getActionSignaller().fireOnRoundPlayTimerChanged(0);
	}

	@Override
	void tick(double dT) {
		super.tick(dT);
		
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
		
		// Update the round time, send an event if the time changed by a whole integer
		int lastRoundTimeCeiled = (int)Math.ceil(this.roundTime);
		this.roundTime += dT;
		int currRoundTimeCeiled = (int)Math.ceil(this.roundTime);
		if (currRoundTimeCeiled != lastRoundTimeCeiled) {
			this.gameModel.getActionSignaller().fireOnRoundPlayTimerChanged(currRoundTimeCeiled);
		}
	}

	@Override
	GameStateType getStateType() {
		return GameState.GameStateType.TEST_ROUND_STATE;
	}

}
