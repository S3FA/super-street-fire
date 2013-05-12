package ca.site3.ssf.gamemodel;

import ca.site3.ssf.common.Algebra;

/**
 * Represents the model for tracking a player's timing for blocks in SSF. This
 * will be told whether a player is supposed to block and from there it will track
 * up to when the block will no longer be acceptable. This class is also responsible for
 * emitting any of the relevant events for block signaling.
 * 
 * @author Callum
 *
 */
class BlockTimingModel {

	// We need to set this based on the average time it takes for a blocking
	// event to travel from the GameModel to the hardware that tells the player to block
	private static final long EVENT_TO_HARDWARE_TRAVEL_TIME_IN_MS = 100; // TODO: Change this based on testing...
	
	// NOTE: The average human reaction time (from perception to basic motor reaction is about 225ms); however,
	// this is for simple mouse clicking and NOT blocking.
	private static final long AVG_HUMAN_BLOCK_REACTION_TIME_IN_MS = 700; // TODO: Change this based on testing...
	
	// The total block window is the complete amount of time in which a block is acceptable
	private static final long TOTAL_BLOCK_WINDOW_TIME_IN_MS = 
			EVENT_TO_HARDWARE_TRAVEL_TIME_IN_MS + AVG_HUMAN_BLOCK_REACTION_TIME_IN_MS;
	private static final double TOTAL_BLOCK_WINDOW_TIME_IN_SECS = (double)TOTAL_BLOCK_WINDOW_TIME_IN_MS / 1000.0;
	
	// The window of time during the total block window, where blocks are 100% effective
	// (i.e., they only do chip damage). NOTE: This must be <= TOTAL_BLOCK_WINDOW_TIME_IN_MS.
	private static final long FULLY_EFFECTIVE_BLOCK_WINDOW_TIME_IN_MS = EVENT_TO_HARDWARE_TRAVEL_TIME_IN_MS + AVG_HUMAN_BLOCK_REACTION_TIME_IN_MS / 2;
	
	private static double TOTAL_MINUS_FULLY_EFFECTIVE_TIME_IN_SECS = (double)(TOTAL_BLOCK_WINDOW_TIME_IN_MS - FULLY_EFFECTIVE_BLOCK_WINDOW_TIME_IN_MS) / 1000.0;
	
	
	// This is the fraction of the total block window that occurs before an attack lands its first damage
	private static final double FRACTION_OF_BLOCK_WINDOW_BEFORE_FIRST_ATK_HURT = 0.75;
	
	/**
	 * Gets the time in seconds before an attack delivers its first hurting blow, where the block window for
	 * that attack should be started.
	 * @return The time in seconds before the first damage of an attack, to start the attack's block window.
	 */
	public static double getBlockWindowTimeBeforeAtkFirstHurt() {
		return TOTAL_BLOCK_WINDOW_TIME_IN_SECS * FRACTION_OF_BLOCK_WINDOW_BEFORE_FIRST_ATK_HURT;
	}
	
	
	// The countdown time in seconds for when a block is allowed/accepted, when
	// this expires (after TOTAL_BLOCK_WINDOW_TIME_IN_MS milliseconds) it should be assigned a value of 0
	private double allowedBlockCountdownInSecs;
	
	private PlayerAttackAction associatedAtk;          // The attack that this block model will allow blocking of
	private GameModelActionSignaller actionSignaller;  // Signaller so that this can communicate block events
	
	BlockTimingModel(PlayerAttackAction associatedAtk, GameModelActionSignaller actionSignaller) {
		assert(associatedAtk != null);
		assert(actionSignaller != null);
		
		this.associatedAtk   = associatedAtk;
		this.actionSignaller = actionSignaller;
		
		this.stopBlockWindow();
	}
	
	/**
	 * A call to this indicates the beginning of a block window.
	 * This will begin a block window countdown where blocks are allowed
	 * for the relevant player.
	 */
	void startBlockWindow() {
		this.allowedBlockCountdownInSecs = (double)TOTAL_BLOCK_WINDOW_TIME_IN_MS / 1000.0;
		
		// Raise an event for the beginning of the block window
		//actionSignaller.fireOnBlockWindowStartAction(this.associatedAtk, this.allowedBlockCountdownInSecs);
	}
	
	/**
	 * Terminates any currently active block window, regardless of this object's
	 * current state.
	 */
	void stopBlockWindow() {
		//if (this.allowedBlockCountdownInSecs > 0.0) {
			//actionSignaller.fireOnBlockWindowStopAction(this.associatedAtk);
		//}
		
		this.allowedBlockCountdownInSecs = 0.0;
	}
	
	/**
	 * Query whether the block window is currently active.
	 * @return true if the block window is active, false if not.
	 */
	boolean isBlockWindowActive() {
		return this.allowedBlockCountdownInSecs > 0.0;
	}
	
	/**
	 * Calling this will indicate that a block occurred, this will change
	 * the state of this object to stop tracking the current block window and
	 * it will also indicate the effectiveness of the block.
	 * @return The effectiveness of the block as a percentage in the interval [0,1].
	 * Where 0 is not effective at all, and 1 as completely effective.
	 */
	float block() {
		double countdownTime = this.allowedBlockCountdownInSecs;
		this.allowedBlockCountdownInSecs = 0.0;
		
		float effectiveness = 0.0f;
		
		// If there is no block window active then the block is completely ineffective (0).
		if (countdownTime > 0) {
			
			// Check to see if the block occurs in the fully-effective time window
			if (countdownTime >= TOTAL_MINUS_FULLY_EFFECTIVE_TIME_IN_SECS) {
				effectiveness = 1.0f;
			}
			else {
				// Linearly interpolate the value between 1 and 0 based on how far towards the end of the block window we are
				effectiveness = Algebra.LerpF(TOTAL_MINUS_FULLY_EFFECTIVE_TIME_IN_SECS, 0.0, 1.0f, 0.0f, countdownTime);
			}
			assert(effectiveness > 0.0);
			
			// Raise an event to signify that the block was successful with some amount of effectiveness
			//actionSignaller.fireOnBlockSuccessAction(this.associatedAtk, effectiveness);
		}
		
		return effectiveness;
	}
	
	/**
	 * Simulates any currently active block window (this will countdown active block windows,
	 * which keeps track of whether blocks are allowed).
	 * @param dT The delta time in seconds between this and the previous frame of the gamemodel.
	 */
	void tick(double dT) {
		// Just exit if there's no block currently allowed
		if (this.allowedBlockCountdownInSecs <= 0.0) {
			return;
		}
		
		// Tick down the block window countdown time
		this.allowedBlockCountdownInSecs -= dT;
		
		// Check to see if the block window is over...
		if (this.allowedBlockCountdownInSecs < 0) {
			this.allowedBlockCountdownInSecs = 0.0;
		}
	}

}
