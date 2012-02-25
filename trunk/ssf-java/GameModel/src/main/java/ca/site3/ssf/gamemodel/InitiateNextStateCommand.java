package ca.site3.ssf.gamemodel;

public final class InitiateNextStateCommand extends AbstractGameModelCommand {

	public InitiateNextStateCommand() {
		super();
	}
	
	@Override
	void execute(GameModel gameModel) {
		gameModel.initiateNextState();
	}

}
