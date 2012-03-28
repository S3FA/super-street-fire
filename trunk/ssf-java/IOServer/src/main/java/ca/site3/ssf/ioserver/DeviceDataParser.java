package ca.site3.ssf.ioserver;

import java.net.InetAddress;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.site3.ssf.ioserver.DeviceConstants.Device;
import ca.site3.ssf.ioserver.DeviceConstants.DeviceType;

/**
 * Parses data that comes in over UDP into a {@link DeviceEvent}.
 * 
 * 
 * @author greg
 */
public class DeviceDataParser implements IDeviceDataParser {

	private Logger log = LoggerFactory.getLogger(getClass());
	
	
	private DeviceStatus deviceStatus;
	
	private Pattern pattern = Pattern.compile("^AN0:([^,]+),AN1:([^,]+),AN2:([^,]+),AN3:([^,]+),AN4:([^,]+),AN5:([^,]+)\\|$");
	
	
	public DeviceDataParser(DeviceStatus deviceStatus) {
		this.deviceStatus = deviceStatus;
	}
	
	
	
	public DeviceEvent parseDeviceData(byte[] data, InetAddress srcIP) throws Exception {
		
		Device d = deviceStatus.getDeviceAtAddress(srcIP);
		if (d == null) {
			log.debug("No device at address: {}",srcIP);
			return null;
		}
		
		if (d.type == DeviceType.HEADSET) {
			return null;
		}
		
		// XXX: currently assuming one line per datagram
		String dataStr = new String(data);
		if (dataStr.startsWith("AN0") == false) {
			log.warn("Ignoring data: '{}'",dataStr);
			return null;
		}
		if (dataStr.endsWith("|") == false) {
			log.warn("Looks like an incomplete frame: '{}'", dataStr);
			return null;
		}
		
		Matcher m = pattern.matcher(dataStr);
		if (m.matches() == false) {
			log.warn("Input did not match regex: '{}'", dataStr);
			return null;
		}
		
		double[] gyro = new double[3];
		double[] accel = new double[3];
		double[] heading = new double[] { 0, 0, 0 };
		
		for (int i=0; i<3; i++) {
			try {
				gyro[i] = Double.parseDouble(m.group(i+1));
				accel[i] = Double.parseDouble(m.group(i+4));
			} catch (NumberFormatException ex) {
				log.error("Failed parsing glove data",ex);
				return null;
			}
		}
		
		
		boolean buttonDown = true;
		return new GloveEvent(d.entity, d.type, System.currentTimeMillis(), buttonDown, gyro, accel, heading);
	}

}
