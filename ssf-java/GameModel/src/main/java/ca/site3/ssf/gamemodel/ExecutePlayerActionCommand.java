package ca.site3.ssf.gamemodel;

public final class ExecutePlayerActionCommand extends AbstractGameModelCommand {
	
	final private int playerNum;
	final private ActionFactory.ActionType playerActionType;
	final private boolean usesLeftHand;
	final private boolean usesRightHand;
	
	public ExecutePlayerActionCommand(int playerNum, ActionFactory.ActionType playerActionType, 
									  boolean usesLeftHand, boolean usesRightHand) {
		super();
		this.playerNum = playerNum;
		this.playerActionType = playerActionType;
		this.usesLeftHand     = usesLeftHand;
		this.usesRightHand    = usesRightHand;
		
		assert(playerActionType != null);
		assert(this.usesLeftHand || this.usesRightHand);
		assert(playerActionType.getIsPlayerAction());
	}
	
	@Override
	void execute(GameModel gameModel) {
		assert(gameModel != null);
		
		if (!playerActionType.getIsPlayerAction()) {
			assert(false);
			return;
		}
		
		Action playerAction = gameModel.getActionFactory().buildPlayerAction(
				this.playerNum, this.playerActionType, this.usesLeftHand, this.usesRightHand);
		assert(playerAction != null);
		gameModel.executeGenericAction(playerAction);
	}

}
