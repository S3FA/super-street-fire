package ca.site3.ssf.gamemodel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.site3.ssf.gamemodel.FireEmitter.Location;


/**
 * This shouldn't really be part of the GameModel but 
 * trying to get it in for test build.
 * 
 * Doesn't currently support timer / life bars or air cannons.
 * 
 * @author greg
 */
@SuppressWarnings("serial")
public class SystemInfoRefreshEvent implements IGameModelEvent {

	private Logger log = LoggerFactory.getLogger(getClass());
	
	public static class OutputDeviceStatus {
		/** deviceId from <em>hardware</em>, doesn't correspond to GameModel index/ID */
		public final int deviceId;
		public final boolean isResponding;
		public final boolean isArmed;
		public final boolean isFlame;
		public OutputDeviceStatus(int deviceId, boolean isResponding, boolean isArmed, boolean isFlame) {
			this.deviceId = deviceId;
			this.isResponding = isResponding;
			this.isArmed = isArmed;
			this.isFlame = isFlame;
		}
	}
	
	private OutputDeviceStatus[] leftRail;
	private OutputDeviceStatus[] rightRail;
	private OutputDeviceStatus[] outerRing;
	
	private final long timestamp;
	
	public SystemInfoRefreshEvent(OutputDeviceStatus[] leftRail, OutputDeviceStatus[] rightRail, OutputDeviceStatus[] outerRing) {
		this.leftRail = leftRail;
		this.rightRail = rightRail;
		this.outerRing = outerRing;
		
		this.timestamp = System.currentTimeMillis();
	}
	
	public Type getType() {
		return Type.SYSTEM_INFO_REFRESH;
	}

	
	public OutputDeviceStatus getDeviceStatus(Location location, int index) {
		try {
			switch (location) {
			case LEFT_RAIL:
				return leftRail[index];
			case RIGHT_RAIL:
				return rightRail[index];
			case OUTER_RING:
				return outerRing[index];
			}
		} catch (ArrayIndexOutOfBoundsException ex) {
			log.error("Invalid device index",ex);
		}
		throw new IllegalArgumentException("Invalid device location/index: "+ location + "/" + index); 
	}
	
	
	public long getTimestamp() {
		return timestamp;
	}
}
