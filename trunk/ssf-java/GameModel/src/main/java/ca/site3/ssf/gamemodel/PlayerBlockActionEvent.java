package ca.site3.ssf.gamemodel;

@SuppressWarnings("serial")
public final class PlayerBlockActionEvent implements IGameModelEvent {

	final private int playerNum; 				// The number of the player who is blocking
	final private boolean blockWasEffective;	// Whether the block was effective at actually blocking an incoming attack
	
	public PlayerBlockActionEvent(int playerNum, boolean blockWasEffective) {
		super();
		this.playerNum = playerNum;
		this.blockWasEffective = blockWasEffective;
	}
	
	public int getPlayerNum() {
		return this.playerNum;
	}
	
	public boolean getBlockWasEffective() {
		return this.blockWasEffective;
	}
	
	public Type getType() {
		return Type.PLAYER_BLOCK_ACTION;
	}

}
