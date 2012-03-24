package ca.site3.ssf.gamemodel;

/**
 * Abstract class for representing a general state of the Super Street Fire game.
 * @author Callum
 * @author Greg
 */
public abstract class GameState {

	/**
	 * The enumeration of the various game state types, useful for events and casting.
	 * Also provides a lot of information useful to various GUIs/views for knowing what
	 * next states are possible and not.
	 */
	public enum GameStateType {
		
		NO_STATE               ("N/A",                       false, true,  false, null),
		ROUND_BEGINNING_STATE  ("Round Beginning",           false, true,  true,  null),
		ROUND_IN_PLAY_STATE    ("Round In-Play",             false, true,  true,  null),
		ROUND_ENDED_STATE      ("Round Ended",               false, true,  true,  null),
		TIE_BREAKER_ROUND_STATE("Tie Breaker Round In-Play", false, true,  true,  null),
		MATCH_ENDED_STATE      ("Match Ended",               false, true,  true,  null),
		PAUSED_STATE           ("Paused",                    false, true,  true,  null),
		RINGMASTER_STATE       ("Ringmaster Control",        true,  true,  true,  ROUND_BEGINNING_STATE),
		IDLE_STATE             ("Idle",                      true,  false, false, RINGMASTER_STATE);
		
		final private String name;
		final private boolean isGoToNextStateControllable;
		final private boolean isKillable;
		final private boolean canPauseToggled;
		final private GameStateType nextControllableGoToState;
		
		GameStateType(String name, boolean isGoToNextStateControllable, boolean isKillable,
				      boolean canPauseToggled, GameStateType nextControllableGoToState) {
			
			this.name = name;
			this.isGoToNextStateControllable = isGoToNextStateControllable;
			this.isKillable = isKillable;
			this.nextControllableGoToState = nextControllableGoToState;
			this.canPauseToggled = canPauseToggled;
		}
		
		public boolean isGoToNextStateControllable() {
			return this.isGoToNextStateControllable;
		}
		public boolean isKillable() {
			return this.isKillable;
		}
		public boolean canBePausedOrUnpaused() {
			return this.canPauseToggled;
		}
		public GameStateType nextControllableGoToState() {
			return this.nextControllableGoToState;
		}
		
		public String toString() {
			return this.name;
		}
		
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
