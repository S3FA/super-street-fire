package ca.site3.ssf.ioserver;

import ca.site3.ssf.gamemodel.IGameModel.Entity;
import ca.site3.ssf.ioserver.DeviceConstants.DeviceType;



/**
 * An event that came from an input device.
 * 
 * @author greg
 */
public abstract class DeviceEvent {

	/**
	 * The type of event. For casting purposes.
	 */
	public enum Type {
		
		/** Corresponds to a {@link GloveEvent} */
		GloveEvent,
		
		/** Corresponds to a {@link HeadsetEvent} */
		HeadsetEvent
	};
	
	
	
	private Type type;
	private Entity source;
	private DeviceType device;
	
	private long timestamp;

	protected DeviceEvent(Type type, Entity src, DeviceType dvc, long timestamp) {
		this.type = type;
		this.source = src;
		this.device = dvc;
		
		this.timestamp = timestamp;
	}
	
	
	public Type getType() {
		return type;
	}
	
	public Entity getSource() {
		return source;
	}

	public DeviceType getDevice() {
		return device;
	}
	
	
	public long getTimestamp() {
		return timestamp;
	}
}
