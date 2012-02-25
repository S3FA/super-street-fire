package ca.site3.ssf.gamemodel;

public final class TogglePauseGameCommand extends AbstractGameModelCommand {

	public TogglePauseGameCommand() {
		super();
	}
	
	@Override
	void execute(GameModel gameModel) {
		gameModel.togglePauseGame();
	}

}
