package ca.site3.ssf.gamemodel;

public final class UnrecognizedGestureEvent implements IGameModelEvent {

	private final IGameModel.Entity entity;
	
	UnrecognizedGestureEvent(IGameModel.Entity entity) {
		assert(entity != null);
		this.entity = entity;
	}
	
	public Type getType() {
		return IGameModelEvent.Type.UNRECOGNIZED_GESTURE;
	}

	public IGameModel.Entity getEntity() {
		return this.entity;
	}
	
}
