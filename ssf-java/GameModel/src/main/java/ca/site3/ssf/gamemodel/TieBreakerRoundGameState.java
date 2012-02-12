package ca.site3.ssf.gamemodel;

class TieBreakerGameState extends GameState {

	public TieBreakerGameState(GameModel gameModel) {
		super(gameModel);
		// TODO Auto-generated constructor stub
	}

	@Override
	void tick(double dT) {
		// TODO Auto-generated method stub

		// TODO: this.gameModel.getActionSignaller().fireOnRoundEnded(result, true);
		
	}

	@Override
	void killToIdle() {
		this.gameModel.setNextGameState(new IdleGameState(this.gameModel));
	}

	@Override
	void initiateNextState() {
		// The tie breaker must play out before going to the next state
	}

	@Override
	void executeAction(Action action) {
		// TODO Auto-generated method stub

	}

	@Override
	void togglePause() {
		this.gameModel.setNextGameState(new PausedGameState(this.gameModel, this));
	}

	@Override
	GameStateType getStateType() {
		return GameState.GameStateType.TIE_BREAKER_ROUND_STATE;
	}

}
