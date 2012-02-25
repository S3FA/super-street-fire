package ca.site3.ssf.gamemodel;

public final class ExecuteGenericActionCommand extends AbstractGameModelCommand {
	
	final private Action action;
	
	public ExecuteGenericActionCommand(Action action) {
		super();
		this.action = action;
		assert(action != null);
	}
	
	@Override
	void execute(GameModel gameModel) {
		gameModel.executeGenericAction(this.action);
	}

}
