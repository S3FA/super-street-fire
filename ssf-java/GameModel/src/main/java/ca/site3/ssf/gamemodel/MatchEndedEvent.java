package ca.site3.ssf.gamemodel;

@SuppressWarnings("serial")
public final class MatchEndedEvent implements IGameModelEvent {

	public enum MatchResult { 
		PLAYER1_VICTORY { public String toString() { return "Player 1 Wins!"; } }, 
		PLAYER2_VICTORY { public String toString() { return "Player 2 Wins!"; } }
	};
	
	final private MatchResult matchResult;
	
	final private double p1Health;
	final private double p2Health;
	
	static final public double TOASTY_THRESHOLD = 0.5;
	
	public MatchEndedEvent(MatchResult matchResult, double p1Health, double p2Health) {
		super();
		this.matchResult = matchResult;
		this.p1Health = p1Health;
		this.p2Health = p2Health;
	}
	
	public MatchResult getMatchResult() {
		return this.matchResult;
	}
	
	public Type getType() {
		return Type.MATCH_ENDED;
	}

	public boolean isToasty() {
		return getWinnerHealth() >= Player.FULL_HEALTH * TOASTY_THRESHOLD;
	}
	
	public boolean isPerfect() {
		return getWinnerHealth() == Player.FULL_HEALTH;	
	}
	
	private double getWinnerHealth() {
		switch (matchResult) {
		case PLAYER1_VICTORY:
			return p1Health;
		case PLAYER2_VICTORY:
			return p2Health;
		default:
			return 0;
		}
	}
}
