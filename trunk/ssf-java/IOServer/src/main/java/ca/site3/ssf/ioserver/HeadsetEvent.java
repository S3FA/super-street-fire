package ca.site3.ssf.ioserver;

/**
 * Represents the state of a headset's sensors
 * 
 * @author greg
 */
public class HeadsetEvent extends DeviceEvent {

	private double attention;
	private double meditation;
	
	
	/**
	 * @param intensity between 0 and 1
	 */
	public HeadsetEvent(Source src, Device dvc, long timestamp, double attention, double meditation) {
		super(Type.HeadsetEvent, src, dvc, timestamp);
		
		if (attention < 0 || attention > 1) {
			throw new IllegalArgumentException("Headset attention value should be normalized between 0 and 1");
		} else if (meditation < 0 || meditation > 1) {
			throw new IllegalArgumentException("Headset meditation value should be normalized between 0 and 1");
		}
		
		this.attention = attention;
		this.meditation = meditation;
	}


	/**
	 * @return the attention value, between 0 and 1 inclusive
	 */
	public double getAttention() {
		return attention;
	}


	/**
	 * @return the meditation value, between 0 and 1 inclusive
	 */
	public double getMeditation() {
		return meditation;
	}
}
