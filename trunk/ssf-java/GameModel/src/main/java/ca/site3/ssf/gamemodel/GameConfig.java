package ca.site3.ssf.gamemodel;


/**
 * Stores values of configurable parameters for the game.
 * 
 * @author Callum
 * @author Greg
 *
 */
final public class GameConfig {
	
	// Whether chip damage (damage when a player is blocking) is turned on or not
	final private boolean chipDamageOn;
	// The total percent of the attack damage that is done when it is blocked, this is only
	// considered if chipDamageOn is true
	final private float chipDamagePercentage; 
	
	// The minimum delay between player actions (i.e., players have to wait at least this long before
	// another performed action is executed).
	final private double  minTimeBetweenPlayerActionsInSecs;
	
	// Amount of time per-round in seconds
	final private int roundTimeInSecs;
	
	// Number of rounds per match, should always be an odd number greater than zero
	final private int numRoundsPerMatch;
	
	public GameConfig(boolean chipDamageOn, double minTimeBetweenPlayerActionsInSecs,
				      int roundTimeInSecs, int numRoundsPerMatch, float chipDamagePercentage) {
		
		this.chipDamageOn = chipDamageOn;
		this.minTimeBetweenPlayerActionsInSecs = minTimeBetweenPlayerActionsInSecs;
		this.roundTimeInSecs = roundTimeInSecs;
		this.numRoundsPerMatch = numRoundsPerMatch;
		this.chipDamagePercentage = chipDamagePercentage;
		
		assert(roundTimeInSecs > 0);
		assert(minTimeBetweenPlayerActionsInSecs >= 0);
		assert(numRoundsPerMatch > 0 && numRoundsPerMatch % 2 == 1);
		assert(chipDamagePercentage >= 0.0f && chipDamagePercentage <= 1.0f);
	}
	

	public boolean getChipDamageOn() {
		return this.chipDamageOn;
	}
	public float getChipDamagePercentage() {
		return this.chipDamagePercentage;
	}
	public double getMinTimeBetweenPlayerActionsInSecs() {
		return this.minTimeBetweenPlayerActionsInSecs;
	}
	public int getRoundTimeInSecs() {
		return this.roundTimeInSecs;
	}
	public int getNumRoundsPerMatch() {
		return this.numRoundsPerMatch;
	}
	public int getNumRequiredVictoryRoundsForMatchVictory() {
		return (int)(this.numRoundsPerMatch/2) + 1;
	}
	
}
