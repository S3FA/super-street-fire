package ca.site3.ssf.gamemodel;

import java.util.ArrayList;
import java.util.Collection;

public class RoundInPlayState extends GameState {

	private Collection<Action> activeActions = new ArrayList<Action>();
	
	
	public RoundInPlayState(GameModel gameModel) {
		super(gameModel);
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
	GameStateType getStateType() {
		// TODO Auto-generated method stub
		return null;
	}

}
