package ca.site3.ssf.gamemodel;

public final class PlayerHealthChangedEvent implements IGameModelEvent {

	final private int playerNum;			// The player whose health amount changed
	final private float prevLifePercentage; // The previous health amount of the player, before the change
	final private float newLifePercentage;  // The new health amount of the player, after the change

	public PlayerHealthChangedEvent(int playerNum, float prevLifePercentage, float newLifePercentage) {
		super();
		this.playerNum = playerNum;
		this.prevLifePercentage = prevLifePercentage;
		this.newLifePercentage = newLifePercentage;
	}
	
	public int getPlayerNum() {
		return this.playerNum;
	}
	
	public float getPrevLifePercentage() {
		return this.prevLifePercentage;
	}
	
	public float getNewLifePercentage() {
		return this.newLifePercentage;
	}
	
	public Type getType() {
		return Type.PLAYER_HEALTH_CHANGED;
	}

}
