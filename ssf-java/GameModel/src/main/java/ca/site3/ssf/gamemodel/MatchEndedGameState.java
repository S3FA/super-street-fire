package ca.site3.ssf.gamemodel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import ca.site3.ssf.gamemodel.MatchEndedEvent.MatchResult;

class MatchEndedGameState extends GameState {

	final private Player victoryPlayer;
	private Collection<Action> matchEndActions = new ArrayList<Action>(3);
	private boolean firstTick = true;
	
	public MatchEndedGameState(GameModel gameModel, Player victoryPlayer) {
		super(gameModel);
		
		//this.victoryPlayer = victoryPlayer;
		assert(victoryPlayer != null);
		assert(victoryPlayer == this.gameModel.getPlayer1() || victoryPlayer == this.gameModel.getPlayer2());
		this.victoryPlayer = victoryPlayer;
		
		// Signal the event for the end of the match...
		GameModelActionSignaller actionSignaller = this.gameModel.getActionSignaller();
		assert(actionSignaller != null);
		if (victoryPlayer.getPlayerNumber() == 1) {
			actionSignaller.fireOnMatchEnded(MatchResult.PLAYER1_VICTORY);
		}
		else {
			actionSignaller.fireOnMatchEnded(MatchResult.PLAYER2_VICTORY);
		}
		
		// Add match end actions to show the victory player's flames in all their glory...
		ActionFactory actionFactory = this.gameModel.getActionFactory();
		assert(actionFactory != null);
		
		Action tempAction = null;
		
		tempAction = actionFactory.buildPlayerWinAction(victoryPlayer.getPlayerNumber(), 2.75, 1, 0.0);
		assert(tempAction != null);
		this.matchEndActions.add(tempAction);
		
		FireEmitterConfig fireConfig = gameModel.getFireEmitterModel().getFireEmitterConfig();
		int victoryRailFlameWidth = fireConfig.getNumEmittersPerRail()/2;
		
		tempAction  = actionFactory.buildCrowdPleaserBurstAction(
				victoryPlayer.getEntity(), FireEmitter.Location.LEFT_RAIL, 2.75, 4, 0.0, victoryRailFlameWidth);
		assert(tempAction != null);
		this.matchEndActions.add(tempAction);
		
		tempAction = actionFactory.buildCrowdPleaserBurstAction(
				victoryPlayer.getEntity(), FireEmitter.Location.RIGHT_RAIL, 2.75, 4, 0.0, victoryRailFlameWidth);
		assert(tempAction != null);
		this.matchEndActions.add(tempAction);
		
		// Clear the complete fire emitter state, just for good measure
		this.gameModel.getFireEmitterModel().resetAllEmitters();
	}

	@Override
	void tick(double dT) {
		
		// Make absolutely sure that before any further flame emitters are turned on that
		// all of the emitters are initially reset
		if (this.firstTick) {
			this.firstTick = false;
			this.gameModel.getFireEmitterModel().resetAllEmitters();
		}
		
		// Once all the flashy actions are done we move on to the next state...
		if (this.matchEndActions.isEmpty()) {
			return;
		}
		
		// Tick the crowd wow-ing actions for the end of the match...
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
		this.gameModel.setNextGameState(new IdleGameState(this.gameModel));
	}

	@Override
	void initiateNextState(GameState.GameStateType nextState) {
		// We make sure that all match end actions have finished first (the player deserves their victory fire!)
		if (!this.matchEndActions.isEmpty()) {
			return;
		}
		
		// Reset the game (the ringmaster wants to exit this state and go back to showing off)
		this.gameModel.resetGame(false);
		this.gameModel.setNextGameState(new RingmasterGameState(this.gameModel));
	}

	@Override
	void executeAction(Action action) {
		// All executed actions are ignored in this state.
	}

	@Override
	void togglePause() {
		this.gameModel.setNextGameState(new PausedGameState(this.gameModel, this));
	}

	@Override
	GameStateType getStateType() {
		return GameState.GameStateType.MATCH_ENDED_STATE;
	}

	MatchResult getMatchResult() {
		if (this.victoryPlayer.getPlayerNumber() == 1) {
			return MatchResult.PLAYER1_VICTORY;
		}
		else {
			return MatchResult.PLAYER2_VICTORY;
		}
	}
	
}
