package ca.site3.ssf.gamemodel;

public final class RoundBeginTimerChangedEvent implements IGameModelEvent {

	public enum RoundBeginCountdownType {
		THREE { public String toString() { return "3"; } },
		TWO   { public String toString() { return "2"; } },
		ONE   { public String toString() { return "1"; } },
		FIGHT { public String toString() { return "FIGHT!"; } }
	};
	
	final private RoundBeginCountdownType threeTwoOneFightTime;
	
	public RoundBeginTimerChangedEvent(RoundBeginCountdownType threeTwoOneFightTime) {
		super();
		this.threeTwoOneFightTime = threeTwoOneFightTime;
	}
	
	public RoundBeginCountdownType getThreeTwoOneFightTime() {
		return this.threeTwoOneFightTime;
	}
	
	public Type getType() {
		return Type.RoundBeginTimerChanged;
	}

}
