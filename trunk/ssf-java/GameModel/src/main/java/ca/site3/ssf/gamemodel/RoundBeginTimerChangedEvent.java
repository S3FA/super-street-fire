package ca.site3.ssf.gamemodel;

public final class RoundBeginTimerChangedEvent implements IGameModelEvent {

	public enum RoundBeginCountdownType {
		THREE { public String toString() { return "3"; } },
		TWO   { public String toString() { return "2"; } },
		ONE   { public String toString() { return "1"; } },
		FIGHT { public String toString() { return "FIGHT!"; } }
	};
	
	final private int roundNumber;
	final private RoundBeginCountdownType threeTwoOneFightTime;
	
	public RoundBeginTimerChangedEvent(RoundBeginCountdownType threeTwoOneFightTime, int roundNumber) {
		super();
		this.threeTwoOneFightTime = threeTwoOneFightTime;
		this.roundNumber = roundNumber;
	}
	
	public RoundBeginCountdownType getThreeTwoOneFightTime() {
		return this.threeTwoOneFightTime;
	}
	
	public int getRoundNumber() {
		return this.roundNumber;
	}
	
	public Type getType() {
		return Type.RoundBeginTimerChanged;
	}

}
