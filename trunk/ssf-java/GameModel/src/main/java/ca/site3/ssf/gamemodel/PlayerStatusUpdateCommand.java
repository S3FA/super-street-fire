package ca.site3.ssf.gamemodel;

public final class PlayerStatusUpdateCommand extends AbstractGameModelCommand {

	private final int playerNum;
	private final boolean unlimitedMovesOn;
	
	public PlayerStatusUpdateCommand(int playerNum, boolean unlimitedMovesOn) {
		assert(playerNum == 1 || playerNum == 2);
		this.playerNum        = playerNum;
		this.unlimitedMovesOn = unlimitedMovesOn;
	}
	
	@Override
	void execute(GameModel gameModel) {
		assert(gameModel != null);
		Player player = gameModel.getPlayer(this.playerNum);
		if (player == null) {
			return;
		}
		player.setHasInfiniteMoves(this.unlimitedMovesOn);
	}

}
