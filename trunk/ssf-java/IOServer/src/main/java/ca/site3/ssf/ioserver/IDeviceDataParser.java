package ca.site3.ssf.ioserver;

import java.net.InetAddress;

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
	DeviceEvent parseDeviceData(byte[] data, InetAddress src) throws Exception;
}
