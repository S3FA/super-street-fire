package ca.site3.ssf.gamemodel;

/**
 * The BlockWindowEvent is an event signaled by the GameModel when a block window becomes active
 * or expires. When becoming active, the event will contain information about how long the 
 * block window will be active for, otherwise the length of time is undefined.
 * 
 * In all cases, this event will provide a unique ID for the block window associated with the event
 * so that listeners can update their view according to pairs of BlockWindowEvents where the window
 * becomes active and where the same window expires.
 * 
 * @author Callum
 * 
 */
@SuppressWarnings("serial")
public class BlockWindowEvent implements IGameModelEvent {

	private final int blockWindowID;				// The block window's unique identifier
	private final boolean blockWindowHasExpired;	// Whether the block window has expired / been stopped or not
	private final double blockWindowTimeInSecs;		// The time left for the block window before it will automatically be stopped
	private final int blockingPlayerNumber;			// The number of the player (1 or 2) that the blocking window applies to
	
	public BlockWindowEvent(int windowID, boolean windowExpired, double windowTimeLengthInSecs, int blockingPlayerNum) {
		assert(blockingPlayerNum == 1 || blockingPlayerNum == 2);
		
		this.blockWindowID = windowID;
		this.blockWindowHasExpired = windowExpired;
		this.blockWindowTimeInSecs = windowTimeLengthInSecs;
		this.blockingPlayerNumber = blockingPlayerNum;
	}

	public int getBlockWindowID() {
		return this.blockWindowID;
	}
	public boolean getHasBlockWindowExpired() {
		return this.blockWindowHasExpired;
	}
	public double getBlockWindowTimeLengthInSeconds() {
		return this.blockWindowTimeInSecs;
	}
	public int getBlockingPlayerNumber() {
		return this.blockingPlayerNumber;
	}
	
	@Override
	public Type getType() {
		return IGameModelEvent.Type.BLOCK_WINDOW;
	}

}
