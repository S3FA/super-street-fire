package ca.site3.ssf.gamemodel;

public final class GameStateChangedEvent implements IGameModelEvent {

	final private GameState.GameStateType oldState;
	final private GameState.GameStateType newState;
	
	public GameStateChangedEvent(GameState.GameStateType oldState, GameState.GameStateType newState) {
		super();
		this.oldState = oldState;
		this.newState = newState;
	}
	
	public GameState.GameStateType getOldState() {
		return this.oldState;
	}
	
	public GameState.GameStateType getNewState() {
		return this.newState;
	}
	
	public Type getType() {
		return Type.GAME_STATE_CHANGED;
	}

}
