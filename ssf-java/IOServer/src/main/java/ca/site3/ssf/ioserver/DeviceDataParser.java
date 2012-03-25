package ca.site3.ssf.ioserver;

import java.net.InetAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.site3.ssf.ioserver.DeviceConstants.Device;
import ca.site3.ssf.ioserver.DeviceConstants.DeviceType;

/**
 * Parses data that comes in over UDP into a {@link DeviceEvent}.
 * 
 * Expected data format is:
 * <pre>
 * TBD!
 * </pre>
 * 
 * @author greg
 */
public class DeviceDataParser implements IDeviceDataParser {

	private Logger log = LoggerFactory.getLogger(getClass());
	
	
	private DeviceStatus deviceStatus;
	
	
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
		
		long timestamp = -1;
		boolean buttonDown = false;
		
		double[] rot = new double[3];
		double[] acc = new double[3];
		double[] heading = new double[3];
		
		return new GloveEvent(d.entity, d.type, timestamp, buttonDown, rot, acc, heading);
	}

}
