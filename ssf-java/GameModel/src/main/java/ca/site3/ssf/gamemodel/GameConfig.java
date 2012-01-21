package ca.site3.ssf.gamemodel;


/**
 * Stores values of configurable parameters for the game.
 * 
 * @author Callum
 * @author Greg
 *
 */
public class GameConfig {
	private boolean chipDamageOn = true;
	
	public GameConfig() {
	}
	
	public void setChipDamageOn(boolean chipDmgOn) {
		this.chipDamageOn = chipDmgOn;
	}
	public boolean getChipDamageOn() {
		return this.chipDamageOn;
	}
	
	
}
