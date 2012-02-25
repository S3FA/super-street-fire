package ca.site3.ssf.gamemodel;

public final class RoundPlayTimerChangedEvent implements IGameModelEvent {
	
	// The current count down time of the current game round. The
	// timer will start at a large time value and count down to zero over the course of a round.
	// NOTE: These events will stop if the round stops due to a player winning/losing or an abrupt
	// change in state.
	final private int countdownTimeInSecs;
	
	public RoundPlayTimerChangedEvent(int countdownTimeInSecs) {
		super();
		this.countdownTimeInSecs = countdownTimeInSecs;
	}
	
	public int getCountdownTimeInSecs() {
		return this.countdownTimeInSecs;
	}
	
	public Type getType() {
		return Type.RoundPlayTimerChanged;
	}

}
