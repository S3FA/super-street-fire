package ca.site3.ssf.gamemodel;

public final class RingmasterActionEvent implements IGameModelEvent {

	public RingmasterActionEvent() {
		super();
	}
	
	public Type getType() {
		return Type.RINGMASTER_ACTION;
	}

}
