package ca.site3.ssf.gamemodel;

class RoundEndedGameState extends GameState {

	final private Player roundVictor;
	
	/**
	 * Constructor for RoundEndedGameState.
	 * @param gameModel The game model.
	 * @param roundVictor The victor of the round, may be null on a tie.
	 */
	public RoundEndedGameState(GameModel gameModel, Player roundVictor) {
		super(gameModel);
		this.roundVictor = roundVictor;
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
		return GameState.GameStateType.ROUND_ENDED_STATE;
	}

}
