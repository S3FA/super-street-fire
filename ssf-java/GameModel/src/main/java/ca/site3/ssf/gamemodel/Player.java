package ca.site3.ssf.gamemodel;

/**
 * A class that represents a player of the Super Street Fire game.
 * @author Callum
 *
 */
class Player {
	
	public static final float KO_HEALTH   = 0.0f;
	public static final float FULL_HEALTH = 100.0f;
	
	private int playerNum;
	private float health;
	private int numRoundWins;
	private boolean isInvincible;
	
	private GameModelActionSignaller actionSignaller = null;
	
	Player(int playerNum, GameModelActionSignaller actionSignaller) {
		assert(playerNum == 1 || playerNum == 2);
		
		// Set the signaller before doing anything else!
		this.actionSignaller = actionSignaller;
		assert(this.actionSignaller != null);
		
		this.reset();
		this.isInvincible = false;
		this.playerNum = playerNum;
	}
	
	void reset() {
		this.resetHealth();
		this.numRoundWins = 0;
	}
	
	void resetHealth() {
		this.setHealth(Player.FULL_HEALTH);
	}
	
	void doDamage(float damageAmt) {
		assert(damageAmt > 0);
		if (this.isInvincible) {
			return;
		}
		this.setHealth(this.health - damageAmt);
	}
	
	void setHealth(float health) {
		float healthBefore = this.health;
		this.health = Math.min(Player.FULL_HEALTH, Math.max(health, Player.KO_HEALTH));
		
		// If the health actually changed then trigger an event to indicate the change to all gamemodel listeners
		if (this.health != healthBefore) {
			this.actionSignaller.fireOnPlayerHealthChanged(this.playerNum, healthBefore, this.health);
		}
	}
	
	void setInvincible(boolean invincibilityOn) {
		this.isInvincible = invincibilityOn;
	}
	
	boolean isKOed() {
		return (this.health <= Player.KO_HEALTH);
	}
	
	int getPlayerNumber() {
		return this.playerNum;
	}
	
	float getHealth() {
		return this.health;
	}
	
	int getNumRoundWins() {
		return this.numRoundWins;
	}
	void incrementNumRoundWins() {
		this.numRoundWins++;
	}
	
	GameModel.Entity getEntity() {
		switch (this.playerNum) {
		case 1: return GameModel.Entity.PLAYER1_ENTITY;
		case 2: return GameModel.Entity.PLAYER2_ENTITY;
		default:
			assert(false);
			return GameModel.Entity.PLAYER1_ENTITY;
		}
	}
	
	static int getOpposingPlayerNum(int playerNum) {
		switch (playerNum) {
		case 1: return 2;
		case 2: return 1;
		default:
			assert(false);
			return 1;
		}
	}
	
}
