package ca.site3.ssf.gamemodel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

class RoundEndedGameState extends GameState {

	final static private double ROUND_ENDED_LENGTH_IN_SECS = 3.0;
	
	//final private Player roundVictor;
	
	private Collection<Action> roundEndActions = null;
	
	/**
	 * Constructor for RoundEndedGameState.
	 * @param gameModel The game model.
	 * @param roundVictor The victor of the round, may be null on a tie.
	 */
	RoundEndedGameState(GameModel gameModel, Player roundVictor) {
		super(gameModel);
		//this.roundVictor = roundVictor;
		
		if (roundVictor != null) {
			this.roundEndActions = new ArrayList<Action>(3);
		}
		else {
			this.roundEndActions = new ArrayList<Action>(5);
		}
		
		// Start by turning off all of the emitters
		this.gameModel.getFireEmitterModel().resetAllEmitters();
		
		// Execute some cool actions for the end of the round based on the victor (if there was one)...
		ActionFactory actionFactory = this.gameModel.getActionFactory();
		assert(actionFactory != null);
		
		Action tempAction = null;
		
		// Action for the various of fire emitter areas in the game arena...
		tempAction = actionFactory.buildCrowdPleaserBurstAction(GameModel.Entity.RINGMASTER_ENTITY,
				FireEmitter.Location.OUTER_RING, RoundEndedGameState.ROUND_ENDED_LENGTH_IN_SECS, 1);
		assert(tempAction != null);
		this.roundEndActions.add(tempAction);
		
		if (roundVictor != null) {
			tempAction  = actionFactory.buildCrowdPleaserBurstAction(roundVictor.getEntity(),
					FireEmitter.Location.LEFT_RAIL, RoundEndedGameState.ROUND_ENDED_LENGTH_IN_SECS, 3);
			assert(tempAction != null);
			this.roundEndActions.add(tempAction);
			tempAction = actionFactory.buildCrowdPleaserBurstAction(roundVictor.getEntity(),
					FireEmitter.Location.RIGHT_RAIL, RoundEndedGameState.ROUND_ENDED_LENGTH_IN_SECS, 3);
			assert(tempAction != null);
			this.roundEndActions.add(tempAction);
		}
		else {
			tempAction  = actionFactory.buildCrowdPleaserBurstAction(GameModel.Entity.PLAYER1_ENTITY,
					FireEmitter.Location.LEFT_RAIL, RoundEndedGameState.ROUND_ENDED_LENGTH_IN_SECS, 3);
			assert(tempAction != null);
			this.roundEndActions.add(tempAction);
			tempAction = actionFactory.buildCrowdPleaserBurstAction(GameModel.Entity.PLAYER1_ENTITY,
					FireEmitter.Location.RIGHT_RAIL, RoundEndedGameState.ROUND_ENDED_LENGTH_IN_SECS, 3);
			assert(tempAction != null);
			this.roundEndActions.add(tempAction);
			
			tempAction  = actionFactory.buildCrowdPleaserBurstAction(GameModel.Entity.PLAYER2_ENTITY,
					FireEmitter.Location.LEFT_RAIL, RoundEndedGameState.ROUND_ENDED_LENGTH_IN_SECS, 3);
			assert(tempAction != null);
			this.roundEndActions.add(tempAction);
			tempAction = actionFactory.buildCrowdPleaserBurstAction(GameModel.Entity.PLAYER2_ENTITY,
					FireEmitter.Location.RIGHT_RAIL, RoundEndedGameState.ROUND_ENDED_LENGTH_IN_SECS, 3);
			assert(tempAction != null);
			this.roundEndActions.add(tempAction);
		}
	}

	@Override
	void tick(double dT) {
		
		// Once all the flashy actions are done we move on to the next state...
		if (this.roundEndActions.isEmpty()) {
			
			// Turn off all the fire emitters and set the player health to zero...
			this.gameModel.getFireEmitterModel().resetAllEmitters();
			this.gameModel.getPlayer1().setHealth(Player.KO_HEALTH);
			this.gameModel.getPlayer2().setHealth(Player.KO_HEALTH);
			this.gameModel.setNextGameState(new RingmasterGameState(this.gameModel));
			return;
		}
		
		// Tick the crowd wow-ing actions for the end of the round...
		Iterator<Action> iter = this.roundEndActions.iterator();
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
		this.gameModel.setNextGameState(new IdleGameState(this.gameModel));
	}

	@Override
	void initiateNextState() {
		// Do nothing, we go to the next state after we've spent a certain amount of time
		// in this state.
	}

	@Override
	void executeAction(Action action) {
		// No actions are executed in this state
	}

	@Override
	void togglePause() {
		this.gameModel.setNextGameState(new PausedGameState(this.gameModel, this));
	}

	@Override
	GameStateType getStateType() {
		return GameState.GameStateType.ROUND_ENDED_STATE;
	}

}
