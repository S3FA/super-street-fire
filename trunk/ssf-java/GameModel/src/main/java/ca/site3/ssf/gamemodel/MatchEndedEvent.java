package ca.site3.ssf.gamemodel;

public final class MatchEndedEvent implements IGameModelEvent {

	public enum MatchResult { 
		PLAYER1_VICTORY { public String toString() { return "Player 1 Wins!"; } }, 
		PLAYER2_VICTORY { public String toString() { return "Player 2 Wins!"; } }
	};
	
	final private MatchResult matchResult;
	
	public MatchEndedEvent(MatchResult matchResult) {
		super();
		this.matchResult = matchResult;
	}
	
	public MatchResult getMatchResult() {
		return this.matchResult;
	}
	
	public Type getType() {
		return Type.MATCH_ENDED;
	}

}
