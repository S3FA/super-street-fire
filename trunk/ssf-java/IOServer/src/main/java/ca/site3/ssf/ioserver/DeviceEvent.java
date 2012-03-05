package ca.site3.ssf.ioserver;



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
	
	/**
	 * Which player (or ringmaster) the device corresponds to.
	 */
	public enum Source {
		PLAYER_1,
		PLAYER_2,
		RINGMASTER
	}
	
	/**
	 * Enumerates the types of input devices a player or ringmaster may have
	 */
	public enum Device {
		LEFT_GLOVE,
		RIGHT_GLOVE,
		HEADSET
	}
	
	
	private Type type;
	private Source source;
	private Device device;
	
	private long timestamp;

	protected DeviceEvent(Type type, Source src, Device dvc, long timestamp) {
		this.type = type;
		this.source = src;
		this.device = dvc;
		
		this.timestamp = timestamp;
	}
	
	
	public Type getType() {
		return type;
	}
	
	public Source getSource() {
		return source;
	}

	public Device getDevice() {
		return device;
	}
	
	
	public long getTimestamp() {
		return timestamp;
	}
}
