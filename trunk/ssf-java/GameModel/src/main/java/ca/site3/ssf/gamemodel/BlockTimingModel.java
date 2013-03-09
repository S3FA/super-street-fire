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
	private static final int NO_ALLOWED_BLOCK_ACTIVE = -1;
	
	// The total block window is the complete amount of time that a block is acceptable for
	// starting from the allowedBlockStartTime
	private static final long TOTAL_BLOCK_WINDOW_TIME_IN_MS = 2000; // TODO: Change this based on testing...
	
	// The window of time during the total block window, where blocks are 100% effective
	// (i.e., they only do chip damage). NOTE: This must be <= TOTAL_BLOCK_WINDOW_TIME_IN_MS.
	private static final long FULLY_EFFECTIVE_BLOCK_WINDOW_TIME_IN_MS = 1000;
	
	// The countdown time in milliseconds for when a block is allowed/accepted, when
	// this expires (after TOTAL_BLOCK_WINDOW_TIME_IN_MS milliseconds) it should be assigned
	// a value of NO_ALLOWED_BLOCK_ACTIVE.
	private long allowedBlockCountdownInMs;
	 
	BlockTimingModel() {
		this.stopBlockWindow();
	}
	
	/**
	 * A call to this indicates the beginning of a block window.
	 * This will begin a block window countdown where blocks are allowed
	 * for the relevant player.
	 */
	void startBlockWindow() {
		this.allowedBlockCountdownInMs = TOTAL_BLOCK_WINDOW_TIME_IN_MS;
	}
	
	/**
	 * Terminates any currently active block window, regardless of this object's
	 * current state.
	 */
	void stopBlockWindow() {
		this.allowedBlockCountdownInMs = NO_ALLOWED_BLOCK_ACTIVE;
	}
	
	/**
	 * Calling this will indicate that a block occurred, this will change
	 * the state of this object to stop tracking the current block window and
	 * it will also indicate the effectiveness of the block.
	 * @return The effectiveness of the block as a percentage in the interval [0,1].
	 * Where 0 is not effective at all, and 1 as completely effective.
	 */
	float block() {
		long countdownTime = this.allowedBlockCountdownInMs;
		this.allowedBlockCountdownInMs = NO_ALLOWED_BLOCK_ACTIVE;
		
		// If there is no block window active then the block is completely ineffective (0).
		if (countdownTime <= 0) {
			return 0.0f;
		}
		
		// Check to see if the block occurs in the fully-effective time window
		if (countdownTime >= TOTAL_BLOCK_WINDOW_TIME_IN_MS - FULLY_EFFECTIVE_BLOCK_WINDOW_TIME_IN_MS) {
			return 1.0f;
		}
			
		// Linearly interpolate the value between 1 and 0 based on how far towards the end of the
		// block window we are
		return Algebra.LerpF(
				(double)(TOTAL_BLOCK_WINDOW_TIME_IN_MS - FULLY_EFFECTIVE_BLOCK_WINDOW_TIME_IN_MS), 
				0.0, 1.0f, 0.0f, countdownTime);
	}
	
	/**
	 * Simulates any currently active block window (this will countdown active block windows,
	 * which keeps track of whether blocks are allowed).
	 * @param dT The delta time in seconds between this and the previous frame of the gamemodel.
	 */
	void tick(double dT) {
		// Just exit if there's no block currently allowed
		if (this.allowedBlockCountdownInMs == NO_ALLOWED_BLOCK_ACTIVE) {
			return;
		}
		
		// Tick down the block window countdown time
		double dtInMilliseconds = dT * 1000.0;
		this.allowedBlockCountdownInMs -= dtInMilliseconds;
		
		// Check to see if the block window is over...
		if (this.allowedBlockCountdownInMs <= 0) {
			this.allowedBlockCountdownInMs = NO_ALLOWED_BLOCK_ACTIVE;
		}
	}

}
