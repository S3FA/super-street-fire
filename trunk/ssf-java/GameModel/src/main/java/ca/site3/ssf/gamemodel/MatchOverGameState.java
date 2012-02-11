package ca.site3.ssf.gamemodel;

class MatchOverGameState extends GameState {

	final private Player victoryPlayer;
	
	public MatchOverGameState(GameModel gameModel, Player victoryPlayer) {
		super(gameModel);
		this.victoryPlayer = victoryPlayer;
		assert(victoryPlayer != null);
		assert(victoryPlayer == this.gameModel.getPlayer1() || victoryPlayer == this.gameModel.getPlayer2());
	}

	@Override
	void tick(double dT) {
		// TODO Auto-generated method stub

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
