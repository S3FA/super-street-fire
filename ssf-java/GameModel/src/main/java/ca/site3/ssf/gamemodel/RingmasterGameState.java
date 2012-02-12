package ca.site3.ssf.gamemodel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * The RingmasterGameState is a game state that occurs before, between and after
 * rounds and matches of the Super Street Fire game.
 * In this state, the Ringmaster has control over her fire emitters and can perform
 * actions to wow the crowd. In this state the ringmaster can control when the next
 * round/match begins and perform however many and whatever actions they want.
 * 
 * @author Callum
 *
 */
class RingmasterGameState extends GameState {

	private Collection<Action> activeRingmasterActions = new ArrayList<Action>(15);
	
	RingmasterGameState(GameModel gameModel) {
		super(gameModel);
	}
	
	@Override
	void tick(double dT) {
		
		// Execute any queued ringmaster actions...
		Iterator<Action> iter = this.activeRingmasterActions.iterator();
		while (iter.hasNext()) {
			Action currAction = iter.next();
			assert(currAction != null);
			
			if (currAction.isFinished()) {
				iter.remove();
				continue;
			}
			currAction.tick(dT);
		}
		
		// Send event to update all the fire emitters...
		this.gameModel.getFireEmitterModel().fireAllEmitterChangedEvent();
	}

	@Override
	void killToIdle() {
		// Go to the idle game state
		this.gameModel.setNextGameState(new IdleGameState(this.gameModel));
	}

	@Override
	void initiateNextState() {
		// This will move the game into a round beginning state and will
		// incorporate all previous information about whether other rounds have been
		// played of the current match or not
		this.gameModel.setNextGameState(new RoundBeginningGameState(this.gameModel));
	}

	@Override
	void executeAction(Action action) {
		
		// We only accept ringmaster actions in this state...
		switch (action.getContributorEntity()) {
			case RINGMASTER_ENTITY:
				break;
			
			default:
				// Ignore and discard all other entity actions
				return;
		}

		this.activeRingmasterActions.add(action);
	}

	@Override
	void togglePause() {
		// Pause this state, useful if you want the game state to be maintained
		// but technical difficulties are hampering further game play...
		this.gameModel.setNextGameState(new PausedGameState(this.gameModel, this));
	}

	@Override
	GameStateType getStateType() {
		return GameState.GameStateType.RINGMASTER_STATE;
	}

}
