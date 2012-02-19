package ca.site3.ssf.gamemodel;

/**
 * Abstract class for representing a general state of the Super Street Fire game.
 * @author Callum
 * @author Greg
 */
public abstract class GameState {

	/**
	 * The enumeration of the various game state types, useful for events and casting.
	 */
	public enum GameStateType {
		NO_STATE                 { public String toString() { return "N/A"; } },
		IDLE_STATE               { public String toString() { return "Idle"; } },
		RINGMASTER_STATE         { public String toString() { return "Ringmaster Control"; } },
		ROUND_BEGINNING_STATE    { public String toString() { return "Round Beginning"; } },
		ROUND_IN_PLAY_STATE      { public String toString() { return "Round In-Play"; } },
		ROUND_ENDED_STATE        { public String toString() { return "Round Ended"; } },
		TIE_BREAKER_ROUND_STATE  { public String toString() { return "Tie Breaker Round In-Play"; } },
		MATCH_ENDED_STATE        { public String toString() { return "Match Ended"; } },
		PAUSED_STATE             { public String toString() { return "Paused"; } }
	};
	
	protected GameModel gameModel = null;
	
	/**
	 * Constructor for GameState.
	 * @param gameModel The already created/established game model, used by the states.
	 */
	GameState(GameModel gameModel) {
		this.gameModel = gameModel;
		assert(this.gameModel != null);
	}

	// Event methods that must be implemented by child classes
	abstract void tick(double dT);
	abstract void killToIdle();
	abstract void initiateNextState();
	abstract void executeAction(Action action);
	abstract void togglePause();
	abstract GameState.GameStateType getStateType();

}
