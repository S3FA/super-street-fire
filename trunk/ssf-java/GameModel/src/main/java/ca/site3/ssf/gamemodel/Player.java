package ca.site3.ssf.gamemodel;

/**
 * The Player class represents a player of the Super Street Fire game.
 * @author Callum
 *
 */
public class Player {
	
	//public enum PlayerNumber { PLAYER_ONE, PLAYER_TWO }
	
	public static final float KO_HEALTH   = 0.0f;
	public static final float FULL_HEALTH = 100.0f;
	
	private float health;
	private int numRoundWins;
	private boolean isInvincible;
	
	public Player() {
		this.reset();
		this.isInvincible = false;
	}
	
	public void reset() {
		this.resetHealth();
		this.numRoundWins = 0;
	}
	
	public void resetHealth() {
		this.health = Player.FULL_HEALTH;
	}
	
	public void doDamage(float damageAmt) {
		assert(damageAmt > 0);
		if (this.isInvincible) {
			return;
		}
		
		this.health -= damageAmt;
	}
	
	public void setInvincible(boolean invincibilityOn) {
		this.isInvincible = invincibilityOn;
	}
	
	public boolean isKOed() {
		return (this.health <= Player.KO_HEALTH);
	}
	
	public float getHealth() {
		return this.health;
	}
	
	public int getNumRoundWins() {
		return this.numRoundWins;
	}
	
	
	
	
}
