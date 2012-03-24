package ca.site3.ssf.gamemodel;

public final class RoundPlayTimerChangedEvent implements IGameModelEvent {
	
	// The current count down time of the current game round. The
	// timer will start at a large time value and count down to zero over the course of a round.
	// NOTE: These events will stop if the round stops due to a player winning/losing or an abrupt
	// change in state.
	final private int timeInSecs;
	
	public RoundPlayTimerChangedEvent(int timeInSecs) {
		super();
		this.timeInSecs = timeInSecs;
	}
	
	public int getTimeInSecs() {
		return this.timeInSecs;
	}
	
	public Type getType() {
		return Type.RoundPlayTimerChanged;
	}

}
