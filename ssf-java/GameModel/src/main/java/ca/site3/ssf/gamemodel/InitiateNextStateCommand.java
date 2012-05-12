package ca.site3.ssf.gamemodel;

public final class InitiateNextStateCommand extends AbstractGameModelCommand {
	private final GameState.GameStateType nextState;
	
	public InitiateNextStateCommand(GameState.GameStateType nextState) {
		super();
		this.nextState = nextState;
	}
	
	@Override
	void execute(GameModel gameModel) {
		assert(gameModel != null);
		gameModel.initiateNextState(this.nextState);
	}

}
