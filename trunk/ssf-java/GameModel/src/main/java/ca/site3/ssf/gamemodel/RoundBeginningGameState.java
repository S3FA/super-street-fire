package ca.site3.ssf.gamemodel;

/**
 * Round Beginning State, think "3, 2, 1, FIGHT!" - Happens 
 * at the beginning of every new round of play. This state has its own mini, internal
 * statemachine that ensures that the countdown triggers the proper
 * countdown events for all gamemodel listeners.
 * 
 * This state will automatically move to the in-play state once the countdown has
 * completed via its 'tick' method.
 * 
 * @author Callum
 * @author Greg
 *
 */
class RoundBeginningGameState extends GameState {
	
	final static private double FIGHT_COUNT_TOTAL = 3.0; // 3, 2, 1, FIGHT!
	private double fightCounter;
	
	private enum CountState { BEFORE_THREE, THREE, TWO, ONE, FIGHT };
	private CountState currState;
	
	/**
	 * Constructor for RoundBeginningGameState.
	 * @param gameModel The game model that acts as the context for the states.
	 */
	RoundBeginningGameState(GameModel gameModel) {
		super(gameModel);
		
		// Turn off all the fire emitters
		this.gameModel.getFireEmitterModel().resetAllEmitters();
		
		this.fightCounter = RoundBeginningGameState.FIGHT_COUNT_TOTAL;
		this.currState    = RoundBeginningGameState.CountState.BEFORE_THREE;
	}

	@Override
	void tick(double dT) {
		this.updateCountState();
		
		// NOTE: We use a number slightly less than zero because there's the 'FIGHT' portion
		// of the count down.
		if (this.fightCounter <= -0.1) {
			// Change to the next state...
			this.gameModel.setNextGameState(new RoundInPlayState(this.gameModel));
			return;
		}
		
		// TODO: Have some actions here for fire emitters...?
		//this.gameModel.getFireEmitterModel().fireAllEmitterChangedEvent();
		
		this.fightCounter -= dT;
	}

	@Override
	void killToIdle() {
		this.gameModel.setNextGameState(new IdleGameState(this.gameModel));
	}

	@Override
	void initiateNextState() {
		// Ignore this - this state will automatically move to the RoundInPlayGameState
		// when it's ready to.
	}

	@Override
	void executeAction(Action action) {
		// No actions are allowed here - this is the uninterrupted
		// count down to the beginning of the next round
	}
	
	@Override
	void togglePause() {
		// Pause the game...
		this.gameModel.setNextGameState(new PausedGameState(this.gameModel, this));
	}

	@Override
	GameState.GameStateType getStateType() {
		return GameState.GameStateType.ROUND_BEGINNING_STATE;
	}

	/**
	 * Helper for updating the internal count down state machine within
	 * this. This method ensures that the proper events for the round begin count down
	 * are executed on all gamemodel listeners.
	 */
	private void updateCountState() {
		switch (this.currState) {
		
			case BEFORE_THREE:
				this.gameModel.getActionSignaller().fireOnRoundBeginFightTimerChanged(IGameModelListener.RoundBeginCountdownType.THREE);
				this.currState = RoundBeginningGameState.CountState.THREE;
				break;
				
			case THREE:
				if (this.fightCounter <= 2.0) {
					this.gameModel.getActionSignaller().fireOnRoundBeginFightTimerChanged(IGameModelListener.RoundBeginCountdownType.TWO);
					this.currState = RoundBeginningGameState.CountState.TWO;
				}
				break;
				
			case TWO:
				if (this.fightCounter <= 1.0) {
					this.gameModel.getActionSignaller().fireOnRoundBeginFightTimerChanged(IGameModelListener.RoundBeginCountdownType.ONE);
					this.currState = RoundBeginningGameState.CountState.ONE;
				}
				break;
				
			case ONE:
				if (this.fightCounter <= 1.0) {
					this.gameModel.getActionSignaller().fireOnRoundBeginFightTimerChanged(IGameModelListener.RoundBeginCountdownType.FIGHT);
					this.currState = RoundBeginningGameState.CountState.FIGHT;
				}
				break;
				
			case FIGHT:
				break;
				
			default:
				assert(false);
				return;
		}
	}
	
}
