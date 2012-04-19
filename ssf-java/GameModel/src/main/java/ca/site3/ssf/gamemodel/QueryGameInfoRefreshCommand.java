package ca.site3.ssf.gamemodel;

public final class QueryGameInfoRefreshCommand extends AbstractGameModelCommand {

	public QueryGameInfoRefreshCommand() {
		super();
	}
	
	@Override
	void execute(GameModel gameModel) {
		assert(gameModel != null);
		gameModel.queryGameInfoRefresh();
	}

}
