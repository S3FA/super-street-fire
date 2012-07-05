package ca.site3.ssf.gamemodel;

import java.util.Arrays;
import java.util.List;

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
		
		NO_STATE               ("N/A",                       true,  false, null),
		ROUND_BEGINNING_STATE  ("Round Beginning",           true,  true,  null),
		ROUND_IN_PLAY_STATE    ("Round In-Play",             true,  true,  null),
		ROUND_ENDED_STATE      ("Round Ended",               true,  true,  null),
		TIE_BREAKER_ROUND_STATE("Tie Breaker Round In-Play", true,  true,  null),
		TEST_ROUND_STATE       ("Test Round",                true,  true,  null),
		PAUSED_STATE           ("Paused",                    true,  true,  null),
		RINGMASTER_STATE       ("Ringmaster Control",        true,  true,  Arrays.asList(ROUND_BEGINNING_STATE)),
		MATCH_ENDED_STATE      ("Match Ended",               true,  true,  Arrays.asList(RINGMASTER_STATE)),
		IDLE_STATE             ("Idle",                      false, false, Arrays.asList(RINGMASTER_STATE, TEST_ROUND_STATE));
		
		final private String name;
		final private boolean isKillable;
		final private boolean canPauseToggled;
		final private List<GameStateType> nextControllableGoToStates;
		
		GameStateType(String name, boolean isKillable,
				      boolean canPauseToggled, List<GameStateType> nextControllableGoToStates) {
			
			this.name = name;
			this.isKillable = isKillable;
			this.nextControllableGoToStates = nextControllableGoToStates;
			this.canPauseToggled = canPauseToggled;
		}
		
		public boolean isGoToNextStateControllable() {
			return this.nextControllableGoToStates != null;
		}
		public boolean isKillable() {
			return this.isKillable;
		}
		public boolean canBePausedOrUnpaused() {
			return this.canPauseToggled;
		}
		public List<GameStateType> nextControllableGoToStates() {
			return this.nextControllableGoToStates;
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
	abstract void initiateNextState(GameState.GameStateType nextState);
	abstract void executeAction(Action action);
	abstract void togglePause();
	abstract GameState.GameStateType getStateType();

}
