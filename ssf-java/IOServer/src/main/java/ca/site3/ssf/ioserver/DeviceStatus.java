package ca.site3.ssf.ioserver;

import java.net.InetAddress;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.site3.ssf.ioserver.DeviceConstants.Device;

/**
 * Model for storing information about all connected devices.
 * Currently no listener interface; clients must poll for changes.
 * 
 * @author greg
 */
public class DeviceStatus {
	
	private Logger log = LoggerFactory.getLogger(getClass());
	
	
	private Map<Device, InetAddress> deviceToAddress = 
			new EnumMap<Device, InetAddress>(Device.class);
	private Map<InetAddress, Device> addressToDevice = 
			new ConcurrentHashMap<InetAddress, DeviceConstants.Device>(EnumSet.allOf(Device.class).size());
	private Map<Device, Float> deviceToRssi = 
			new EnumMap<Device, Float>(Device.class);
	private Map<Device, Integer> deviceToBattery = 
			new EnumMap<Device, Integer>(Device.class);
	
	private Map<Device, Long> latestHeartbeats = new EnumMap<Device, Long>(Device.class);
	
	
	/**
	 * @param d device identifier. Must not be null. 
	 * @param address IP address or null if no longer connected
	 * @param rssi radio strength signal indicator
	 * @param battery voltage in mV
	 */
	public void setDeviceInfo(Device d, InetAddress address, byte rssi, int battery) {
		
		if (address != null) {
			addressToDevice.put(address, d);
			deviceToAddress.put(d, address);
		} else {
			if (deviceToAddress.get(d) != null) {
				addressToDevice.put(deviceToAddress.get(d), null);
			}
		}
		
		deviceToRssi.put(d, rssi / (float)Byte.MAX_VALUE);		
		deviceToBattery.put(d,battery);
		
		latestHeartbeats.put(d, System.currentTimeMillis());
		log.debug("Device {} at address {} (RSSI={})", new Object[]{d,address,rssi});
	}
	
	
	
	/**
	 * @param address
	 * @return the device corresponding to the given address
	 */
	public Device getDeviceAtAddress(InetAddress address) {
		return addressToDevice.get(address);
	}
	
	/**
	 * @param d
	 * @return The IP address of the device
	 */
	public InetAddress getDeviceAddress(Device d) {
		return deviceToAddress.get(d);
	}
	
	/**
	 * @param d
	 * @return the most recent RSSI value for d
	 */
	public float getDeviceRssi(Device d) {
		return deviceToRssi.get(d);
	}
	
	/**
	 * @param d
	 * @return the most recent battery level for d in mV
	 */
	public int getDeviceBattery(Device d) {
		return deviceToBattery.get(d);
	}
}