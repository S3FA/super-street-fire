package ca.site3.ssf.gamemodel;

public final class KillGameCommand extends AbstractGameModelCommand {

	public KillGameCommand() {
		super();
	}
	
	@Override
	void execute(GameModel gameModel) {
		gameModel.killGame();
	}

}
