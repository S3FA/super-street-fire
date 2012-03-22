package ca.site3.ssf.gamemodel;

public final class ExecutePlayerActionCommand extends AbstractGameModelCommand {
	
	final private int playerNum;
	final private ActionFactory.PlayerActionType playerActionType;
	final private boolean usesLeftHand;
	final private boolean usesRightHand;
	
	public ExecutePlayerActionCommand(int playerNum, ActionFactory.PlayerActionType playerActionType, 
									  boolean usesLeftHand, boolean usesRightHand) {
		super();
		this.playerNum = playerNum;
		this.playerActionType = playerActionType;
		this.usesLeftHand     = usesLeftHand;
		this.usesRightHand    = usesRightHand;
	}
	
	@Override
	void execute(GameModel gameModel) {
		Action playerAction = gameModel.getActionFactory().buildPlayerAction(
				this.playerNum, this.playerActionType, this.usesLeftHand, this.usesRightHand);
		assert(playerAction != null);
		gameModel.executeGenericAction(playerAction);
	}

}
