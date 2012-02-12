package ca.site3.ssf.gamemodel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

class MatchOverGameState extends GameState {

	final private Player victoryPlayer;
	private Collection<Action> matchEndActions = new ArrayList<Action>(3);
	
	public MatchOverGameState(GameModel gameModel, Player victoryPlayer) {
		super(gameModel);
		
		this.victoryPlayer = victoryPlayer;
		assert(victoryPlayer != null);
		assert(victoryPlayer == this.gameModel.getPlayer1() || victoryPlayer == this.gameModel.getPlayer2());
		
		// Add match end actions to show the victory player's flames in all their glory...
		ActionFactory actionFactory = this.gameModel.getActionFactory();
		assert(actionFactory != null);
		
		Action tempAction = null;
		
		tempAction = actionFactory.buildBurstAction(GameModel.Entity.RINGMASTER_ENTITY, FireEmitter.Location.OUTER_RING, 3.0, 1);
		assert(tempAction != null);
		this.matchEndActions.add(tempAction);
		
		tempAction  = actionFactory.buildBurstAction(victoryPlayer.getEntity(), FireEmitter.Location.LEFT_RAIL, 3.0, 6);
		assert(tempAction != null);
		this.matchEndActions.add(tempAction);
		
		tempAction = actionFactory.buildBurstAction(victoryPlayer.getEntity(), FireEmitter.Location.RIGHT_RAIL, 3.0, 6);
		assert(tempAction != null);
		this.matchEndActions.add(tempAction);
	}

	@Override
	void tick(double dT) {
		
		// Once all the flashy actions are done we move on to the next state...
		if (this.matchEndActions.isEmpty()) {
			
			// Turn off all the fire emitters and set the player health to zero...
			this.gameModel.getFireEmitterModel().resetAllEmitters();
			this.gameModel.getPlayer1().setHealth(Player.KO_HEALTH);
			this.gameModel.getPlayer2().setHealth(Player.KO_HEALTH);
			this.gameModel.setNextGameState(new RingmasterGameState(this.gameModel));
			return;
		}
		
		// Tick the crowd wow-ing actions for the end of the round...
		Iterator<Action> iter = this.matchEndActions.iterator();
		while (iter.hasNext()) {
			Action currAction = iter.next();
			if (currAction.isFinished()) {
				iter.remove();
				continue;
			}
			currAction.tick(dT);
		}
		
		// Send fire emitter value changed event for all emitters...
		this.gameModel.getFireEmitterModel().fireAllEmitterChangedEvent();
	}

	@Override
	void killToIdle() {
		// TODO Auto-generated method stub

	}

	@Override
	void initiateNextState() {
		// TODO Auto-generated method stub

	}

	@Override
	void executeAction(Action action) {
		// TODO Auto-generated method stub

	}

	@Override
	void togglePause() {
		// TODO Auto-generated method stub

	}

	@Override
	GameStateType getStateType() {
		return GameState.GameStateType.MATCH_OVER_STATE;
	}

}
