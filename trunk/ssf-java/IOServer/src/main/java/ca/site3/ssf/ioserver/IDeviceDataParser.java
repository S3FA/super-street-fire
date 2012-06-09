package ca.site3.ssf.ioserver;

import java.net.InetAddress;
import java.util.List;

/**
 * Translates raw data from the network into a higher-level DeviceEvent
 * 
 * @author greg
 */
public interface IDeviceDataParser {

	/**
	 * Translates raw data from a device into a DeviceEvent
	 * 
	 * @param data raw data from a device
	 * @param src IP address the data came from
	 * @return a DeviceEvent representing the data from the peripheral
	 */
	List<? extends DeviceEvent> parseDeviceData(byte[] data, int dataLength, InetAddress src) throws Exception;
}
