package ca.site3.ssf.gamemodel;

public final class PlayerBlockActionEvent implements IGameModelEvent {

	final private int playerNum; // The number of the player who is blocking
	
	public PlayerBlockActionEvent(int playerNum) {
		super();
		this.playerNum = playerNum;
	}
	
	public int getPlayerNum() {
		return this.playerNum;
	}
	
	public Type getType() {
		return Type.PlayerBlockAction;
	}

}
