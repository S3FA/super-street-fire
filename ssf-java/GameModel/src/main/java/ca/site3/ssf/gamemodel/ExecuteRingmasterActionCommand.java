package ca.site3.ssf.gamemodel;

public final class ExecuteRingmasterActionCommand extends AbstractGameModelCommand {

	final private ActionFactory.ActionType ringmasterActionType;
	final private boolean usesLeftHand;
	final private boolean usesRightHand;
	
	public ExecuteRingmasterActionCommand(ActionFactory.ActionType ringmasterActionType, 
									      boolean usesLeftHand, boolean usesRightHand) {
		
		super();
		this.ringmasterActionType = ringmasterActionType;
		this.usesLeftHand = usesLeftHand;
		this.usesRightHand = usesRightHand;
		
		assert(ringmasterActionType != null);
		assert(this.usesLeftHand || this.usesRightHand);
		assert(!ringmasterActionType.getIsPlayerAction());
	}
	
	
	@Override
	void execute(GameModel gameModel) {
		assert(gameModel != null);
		
		if (this.ringmasterActionType.getIsPlayerAction()) {
			assert(false);
			return;
		}
		
		Action ringmasterAction = gameModel.getActionFactory().buildRingmasterAction(
				this.ringmasterActionType, this.usesLeftHand, this.usesRightHand);
		assert(ringmasterAction != null);
		gameModel.executeGenericAction(ringmasterAction);
	}

}
