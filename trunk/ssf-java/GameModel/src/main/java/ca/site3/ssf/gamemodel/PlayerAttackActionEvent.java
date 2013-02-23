package ca.site3.ssf.gamemodel;

@SuppressWarnings("serial")
public final class PlayerAttackActionEvent implements IGameModelEvent {

	final private int playerNum;							// The number of the player who is attacking
	final private PlayerAttackAction.AttackType attackType; // The type of attack that was executed
	
	public PlayerAttackActionEvent(int playerNum, PlayerAttackAction.AttackType attackType) {
		super();
		this.playerNum = playerNum;
		this.attackType = attackType;
	}
	
	public int getPlayerNum() {
		return this.playerNum;
	}
	
	public PlayerAttackAction.AttackType getAttackType() {
		return this.attackType;
	}
	
	public Type getType() {
		return Type.PLAYER_ATTACK_ACTION;
	}

}
