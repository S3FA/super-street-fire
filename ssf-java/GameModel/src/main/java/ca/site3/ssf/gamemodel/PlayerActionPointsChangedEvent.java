package ca.site3.ssf.gamemodel;

@SuppressWarnings("serial")
public class PlayerActionPointsChangedEvent implements IGameModelEvent {

	private final int playerNum;            // The player whose action points changed
	private final float prevActionPointAmt; // The action points that the player had before the change
	private final float newActionPointAmt;  // The action points that the player has after the change
	
	public PlayerActionPointsChangedEvent(int playerNum, float prevActionPointAmt, float newActionPointAmt) {
		super();
		this.playerNum = playerNum;
		this.prevActionPointAmt = prevActionPointAmt;
		this.newActionPointAmt  = newActionPointAmt;
	}
	
	public int getPlayerNum() {
		return this.playerNum;
	}
	
	public float getPrevActionPointAmt() {
		return this.prevActionPointAmt;
	}
	
	public float getNewActionPointAmt() {
		return this.newActionPointAmt;
	}
	
	@Override
	public Type getType() {
		return IGameModelEvent.Type.PLAYER_ACTION_POINTS_CHANGED;
	}

}
