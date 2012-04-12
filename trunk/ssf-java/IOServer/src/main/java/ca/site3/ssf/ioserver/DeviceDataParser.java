package ca.site3.ssf.ioserver;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
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
	
	// gX:69.68,gY:-77.45,gZ:-20.77,aX:-265.64,aY:239.84,aZ:4228.79,RLL:0.69,PCH:-2.46|
	private Pattern pattern = Pattern.compile("^gX:([^,]+),gY:([^,]+),gZ:([^,]+),aX:([^,]+),aY:([^,]+),aZ:([^,]+),RLL:([^,]+),PCH:([^|]+)\\|$");
	
	public DeviceDataParser(DeviceStatus deviceStatus) {
		this.deviceStatus = deviceStatus;
	}
	
	
	
	public List<DeviceEvent> parseDeviceData(byte[] data, InetAddress srcIP) throws Exception {
		
		Device d = deviceStatus.getDeviceAtAddress(srcIP);
		if (d == null) {
			log.debug("No device at address: {}",srcIP);
			return null;
		}
		
		if (d.type == DeviceType.HEADSET) {
			return null;
		}
		
		/*
		 *  XXX: currently assuming datagrams break cleanly at | boundaries
		 *  (though could be multiple lines per datagram)
		 */
		String rawString = new String(data).trim();
		String[] strings = rawString.split("\n");
		List<DeviceEvent> events = new ArrayList<DeviceEvent>(strings.length);
		for (String dataStr : strings) {
			GloveEvent gloveEvent = parseSingleLine(d, dataStr);
			events.add(gloveEvent);
		}
		
		return events;
	}

	
	private GloveEvent parseSingleLine(Device d, String dataStr) {
		if (dataStr.startsWith("gX") == false) {
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
		
//		double roll = Double.parseDouble(m.group(7));
//		double pitch = Double.parseDouble(m.group(8));
		
		boolean buttonDown = true;
		return new GloveEvent(d.entity, d.type, System.currentTimeMillis(), buttonDown, gyro, accel, heading);
	}
}
