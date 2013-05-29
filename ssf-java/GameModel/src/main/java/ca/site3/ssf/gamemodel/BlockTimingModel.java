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

	// Keeps track of block window ids for events
	private static int BLOCK_WINDOW_ID_COUNTER = 0;
	
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
	
	// The unique id for this block timing model instance (for event purposes)
	private final int id;
	
	// The number of the player who this BlockTimingModel applies to
	private final int blockingPlayerNum;
	
	// The countdown time in seconds for when a block is allowed/accepted, when
	// this expires (after TOTAL_BLOCK_WINDOW_TIME_IN_MS milliseconds) it should be assigned a value of 0
	private double allowedBlockCountdownInSecs;
	
	private GameModelActionSignaller actionSignaller;  // Signal object so that this can communicate block window events
	private boolean isFinished; // Whether the window has happened and is complete
	
	BlockTimingModel(int blockingPlayerNum, GameModelActionSignaller actionSignaller) {
		assert(actionSignaller != null);
		
		this.id = BLOCK_WINDOW_ID_COUNTER;
		BLOCK_WINDOW_ID_COUNTER++;

		this.blockingPlayerNum = blockingPlayerNum;
		this.actionSignaller = actionSignaller;
		this.allowedBlockCountdownInSecs = 0.0;
		this.isFinished = false;
	}
	
	/**
	 * A call to this indicates the beginning of a block window.
	 * This will begin a block window countdown where blocks are allowed
	 * for the relevant player.
	 */
	void startBlockWindow() {
		if (this.isFinished) {
			return;
		}
		
		this.allowedBlockCountdownInSecs = (double)TOTAL_BLOCK_WINDOW_TIME_IN_MS / 1000.0;
		
		// Signal an event for the beginning of the block window
		actionSignaller.fireOnBlockWindowEvent(this.id, false, this.allowedBlockCountdownInSecs, this.blockingPlayerNum);
	}
	
	/**
	 * Terminates any currently active block window, regardless of this object's
	 * current state.
	 */
	void stopBlockWindow() {
		if (this.allowedBlockCountdownInSecs > 0.0 && !this.isFinished) {
			// Signal an event for the expiration of this block window
			actionSignaller.fireOnBlockWindowEvent(this.id, true, this.allowedBlockCountdownInSecs, this.blockingPlayerNum);
			this.isFinished = true;
		}
		
		this.allowedBlockCountdownInSecs = 0.0;
	}
	
	/**
	 * Query whether the block window is currently active.
	 * @return true if the block window is active, false if not.
	 */
	boolean isBlockWindowActive() {
		return this.allowedBlockCountdownInSecs > 0.0 && !this.isFinished;
	}
	
	/**
	 * Calling this will indicate that a block occurred, this will change
	 * the state of this object to stop tracking the current block window and
	 * it will also indicate the effectiveness of the block.
	 * @return The effectiveness of the block as a fractional percentage in the interval [0, 1].
	 * Where 0 is not effective at all, and 1 is completely effective.
	 */
	float block() {
		if (this.isFinished) {
			return 0.0f;
		}
		
		double countdownTime = this.allowedBlockCountdownInSecs;
		this.stopBlockWindow();
		
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
		}
		
		/*
		if (effectiveness > 0.0) {
			// Raise an event to signify that the block was successful with some amount of effectiveness
			//actionSignaller.fireOnBlockAction(this.associatedAtk, effectiveness);
		}
		else {
			// Raise an event to signify that the block was a failure
			//actionSignaller.fireOnBlockAction(null, 0.0);
		}
		*/
		
		return effectiveness;
	}
	
	/**
	 * Simulates any currently active block window (this will countdown active block windows,
	 * which keeps track of whether blocks are allowed).
	 * @param dT The delta time in seconds between this and the previous frame of the gamemodel.
	 */
	void tick(double dT) {
		// Just exit if there's no block currently allowed
		if (this.allowedBlockCountdownInSecs <= 0.0 || this.isFinished) {
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
