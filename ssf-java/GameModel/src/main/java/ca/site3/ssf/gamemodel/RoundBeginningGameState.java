package ca.site3.ssf.gamemodel;

/**
 * Round Beginning State, think "3, 2, 1, FIGHT!" - Happens 
 * at the beginning of every new round of play.
 * @author Callum
 * @author Greg
 *
 */
class RoundBeginningGameState extends GameState {

	private int roundNum;
	
	/**
	 * Constructor for RoundBeginningGameState.
	 * @param gameModel The game model that acts as the context for the states.
	 * @param roundNum  The round number (starts at 1).
	 */
	RoundBeginningGameState(GameModel gameModel, int roundNum) {
		super(gameModel);
		this.roundNum = roundNum;
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
	void initiateNextMatchRound() {
		// TODO Auto-generated method stub

	}

	@Override
	void togglePause() {
		// TODO Auto-generated method stub

	}

	@Override
	GameState.GameStateType getStateType() {
		return GameState.GameStateType.ROUND_BEGINNING_STATE;
	}

}
