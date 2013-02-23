package ca.site3.ssf.gamemodel;

import java.util.ArrayList;
import java.util.List;

import ca.site3.ssf.gamemodel.MatchEndedEvent.MatchResult;
import ca.site3.ssf.gamemodel.RoundBeginTimerChangedEvent.RoundBeginCountdownType;
import ca.site3.ssf.gamemodel.RoundEndedEvent.RoundResult;

@SuppressWarnings("serial")
public final class GameInfoRefreshEvent implements IGameModelEvent {

	private final GameState.GameStateType currGameState;
	private final List<RoundResult> currRoundResults;
	private final MatchResult matchResult;
	private final float player1Health;
	private final float player2Health;
	private final boolean player1UnlimitedMovesOn;
	private final boolean player2UnlimitedMovesOn;
	private final RoundBeginCountdownType roundBeginCountdown;
	private final int roundInPlayTimerSecs;
	private final boolean roundTimedOut;
	
	public GameInfoRefreshEvent(GameState.GameStateType currGameState, List<RoundResult> currRoundResults, MatchResult matchResult,
			                    float player1Health, float player2Health,
			                	boolean player1UnlimitedMovesOn,
			                	boolean player2UnlimitedMovesOn,
			                	RoundBeginCountdownType roundBeginCountdown,
			                    int roundInPlayTimerSecs, boolean roundTimedOut) {
		
		assert(currGameState != null);
		assert(currRoundResults != null);
		assert(matchResult != null);
		assert(roundBeginCountdown != null);
		
		this.currGameState    = currGameState;
		this.currRoundResults = new ArrayList<RoundResult>(currRoundResults);
		this.matchResult      = matchResult;
		this.player1Health    = player1Health;
		this.player2Health    = player2Health;
		this.player1UnlimitedMovesOn = player1UnlimitedMovesOn;
		this.player2UnlimitedMovesOn = player2UnlimitedMovesOn;
		this.roundBeginCountdown  = roundBeginCountdown;
		this.roundInPlayTimerSecs = roundInPlayTimerSecs;
		this.roundTimedOut = roundTimedOut;
	}
	
	public Type getType() {
		return IGameModelEvent.Type.GAME_INFO_REFRESH;
	}
	public final GameState.GameStateType getCurrentGameState() {
		return this.currGameState;
	}
	public final List<RoundResult> getCurrentRoundResults() {
		return this.currRoundResults;
	}
	public final MatchResult getMatchResult() {
		// This will only make sense for a match ended state
		return this.matchResult;
	}
	public float getPlayer1Health() {
		return this.player1Health;
	}
	public float getPlayer2Health() {
		return this.player2Health;
	}
	public boolean getPlayer1UnlimitedMoves() {
		return this.player1UnlimitedMovesOn;
	}
	public boolean getPlayer2UnlimitedMoves() {
		return this.player2UnlimitedMovesOn;
	}
	public final RoundBeginCountdownType getRoundBeginCountdown() {
		return this.roundBeginCountdown;
	}
	public int getRoundInPlayTimer() {
		return this.roundInPlayTimerSecs;
	}
	public int getRoundNumber() {
		return this.currRoundResults.size() + 1;
	}
	public boolean getRoundTimedOut() {
		return this.roundTimedOut;
	}
}
