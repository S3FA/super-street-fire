package ca.site3.ssf.gamemodel;

@SuppressWarnings("serial")
public final class RingmasterActionEvent implements IGameModelEvent {

	private final RingmasterAction.ActionType ringmasterAction;
	
	public RingmasterActionEvent(RingmasterAction.ActionType ringmasterAction) {
		super();
		this.ringmasterAction = ringmasterAction;
	}
	
	public Type getType() {
		return Type.RINGMASTER_ACTION;
	}

	public RingmasterAction.ActionType getActionType() {
		return this.ringmasterAction;
	}
}
