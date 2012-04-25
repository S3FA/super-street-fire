package ca.site3.ssf.ioserver;

import java.util.Arrays;

import ca.site3.ssf.gamemodel.IGameModel.Entity;
import ca.site3.ssf.ioserver.DeviceConstants.DeviceType;

/**
 * An event that came from a glove peripheral. This comprises
 * the device information (player/device type) plus the
 * current state of the glove's various sensors.
 * 
 * @author greg
 */
public final class GloveEvent extends DeviceEvent {

	public enum EventType { BUTTON_DOWN_EVENT, BUTTON_UP_EVENT, DATA_EVENT };
	
	private final double[] gyro;
	private final double[] acceleration;
	private final double[] magnetometer;
	private final EventType eventType;
	
	public GloveEvent(Entity src, DeviceType dvc,
			long timestamp, EventType eventType, 
			double[] gyro, double[] acceleration, double[] magnetometer) {
		
		super(Type.GloveEvent, src, dvc, timestamp);
		
		this.gyro = gyro;
		this.acceleration = acceleration;
		this.magnetometer = magnetometer;
		this.eventType = eventType;
	}

	/**
	 * @return the rotation (an array of length 3)
	 */
	public final double[] getGyro() {
		return this.gyro;
	}

	/**
	 * @return the acceleration (an array of length 3)
	 */
	public final double[] getAcceleration() {
		return this.acceleration;
	}

	/**
	 * @return the heading (an array of length 3)
	 */
	public final double[] getMagnetometer() {
		return this.magnetometer;
	}

	/**
	 * @return The type of glove event.
	 */
	public final EventType getEventType() {
		return this.eventType;
	}
	
	
	@Override
	public String toString() {
		return "GloveEvent (" + this.eventType.toString() + ") : gyro: " + Arrays.toString(this.gyro) + " acceleration: " + Arrays.toString(this.acceleration);
	}
}
