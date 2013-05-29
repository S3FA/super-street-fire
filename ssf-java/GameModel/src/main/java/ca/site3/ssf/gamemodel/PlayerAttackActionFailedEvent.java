package ca.site3.ssf.gamemodel;


@SuppressWarnings("serial")
public class PlayerAttackActionFailedEvent implements IGameModelEvent {

	public enum Reason { 
		NOT_ENOUGH_ACTION_POINTS("Not enough action points"); 
		
		private final String descriptionStr;
		
		Reason(String descriptionStr) {
			this.descriptionStr = descriptionStr;
		}
		
		public String getDescription() {
			return this.descriptionStr;
		}
	};
	
	final private int playerNum;                             // The number of the player whose attack failed
	final private PlayerAttackAction.AttackType attackType;  // The type of attack that failed
	final private Reason reason;						     // The reason for the failure
	
	public PlayerAttackActionFailedEvent(int playerNum, PlayerAttackAction.AttackType attackType, Reason reason) {
		super();
		this.playerNum  = playerNum;
		this.attackType = attackType;
		this.reason     = reason;
	}
	
	public int getPlayerNum() {
		return this.playerNum;
	}
	
	public PlayerAttackAction.AttackType getAttackType() {
		return this.attackType;
	}
	
	public Reason getReason() {
		return this.reason;
	}
	
	@Override
	public Type getType() {
		return IGameModelEvent.Type.PLAYER_ATTACK_ACTION_FAILED;
	}

}
