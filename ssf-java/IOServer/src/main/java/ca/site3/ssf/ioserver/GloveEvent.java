package ca.site3.ssf.ioserver;

/**
 * An event that came from a glove peripheral. This comprises
 * the device information (player/device type) plus the
 * current state of the glove's various sensors.
 * 
 * @author greg
 */
public class GloveEvent extends DeviceEvent {

	private final double[] gyro;
	
	private final double[] acceleration;
	
	private final double[] magnetometer;
	
	private final boolean buttonPressed;
	
	
	
	public GloveEvent(Source src, Device dvc,
			long timestamp, boolean buttonDown,
			double[] gyro, double[] acceleration, double[] magnetometer) {
		super(Type.GloveEvent, src, dvc, timestamp);
		
		this.gyro = gyro;
		this.acceleration = acceleration;
		this.magnetometer = magnetometer;
		this.buttonPressed = buttonDown;
	}



	/**
	 * @return the rotation (an array of length 3)
	 */
	public double[] getGyro() {
		return gyro;
	}

	/**
	 * @return the acceleration (an array of length 3)
	 */
	public double[] getAcceleration() {
		return acceleration;
	}

	/**
	 * @return the heading (an array of length 3)
	 */
	public double[] getMagnetometer() {
		return magnetometer;
	}

	/**
	 * @return true if the button is being pressed, false otherwise
	 */
	public boolean isButtonPressed() {
		return buttonPressed;
	}
}
