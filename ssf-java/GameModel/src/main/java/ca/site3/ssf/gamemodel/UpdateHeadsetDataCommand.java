package ca.site3.ssf.gamemodel;

public final class UpdateHeadsetDataCommand extends AbstractGameModelCommand {

	private final int playerNum;
	private final HeadsetData data;
	
	public UpdateHeadsetDataCommand(int playerNum, HeadsetData data) {
		super();
		
		assert(playerNum == 1 || playerNum == 2);
		this.playerNum = playerNum;
		
		assert(data != null);
		this.data = data;
	}
	
	@Override
	void execute(GameModel gameModel) {
		assert(gameModel != null);
		gameModel.updatePlayerHeadsetData(this.playerNum, this.data);
	}

}
