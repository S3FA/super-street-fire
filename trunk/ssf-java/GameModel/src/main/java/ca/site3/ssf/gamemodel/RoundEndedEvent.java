package ca.site3.ssf.gamemodel;

@SuppressWarnings("serial")
public final class RoundEndedEvent implements IGameModelEvent {

	public enum RoundResult { 
		PLAYER1_VICTORY { public String toString() { return "Player 1 Wins!"; } }, 
		PLAYER2_VICTORY { public String toString() { return "Player 2 Wins!"; } }, 
		TIE             { public String toString() { return "Tie!"; } }
	};
	
	final private int roundNumber;			// The number of the round that just ended (starting at 1)
	final private RoundResult roundResult;   // The result of the round
	final private boolean roundTimedOut;	// Whether the round timed out or not, in the case of a tie breaker round, this will always be true
	
	public RoundEndedEvent(int roundNumber, RoundResult roundResult, boolean roundTimedOut) {
		super();
		this.roundNumber   = roundNumber;
		this.roundResult   = roundResult;
		this.roundTimedOut = roundTimedOut;
	}
	
	public int getRoundNumber() {
		return this.roundNumber;
	}
	
	public RoundResult getRoundResult() {
		return this.roundResult;
	}
	
	public boolean getRoundTimedOut() {
		return this.roundTimedOut;
	}
	
	public Type getType() {
		return Type.ROUND_ENDED;
	}

}
