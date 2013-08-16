package ca.site3.ssf.ioserver;

import java.net.InetAddress;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.site3.ssf.ioserver.DeviceConstants.Device;

/**
 * Model for storing information about all connected devices.
 * This means things like gloves and headsets (wireless).
 * 
 * For info about wifire board status, see {@link PeripheralStatus}.
 * 
 * @author greg
 */
public class DeviceStatus {
	
	public interface IDeviceStatusListener {
		void deviceStatusChanged(DeviceStatus status);
	}
	
	
	private Logger log = LoggerFactory.getLogger(getClass());
	
	
	private Map<Device, InetAddress> deviceToAddress = 
			new ConcurrentHashMap<Device, InetAddress>();
	private Map<InetAddress, Device> addressToDevice = 
			new ConcurrentHashMap<InetAddress, DeviceConstants.Device>(EnumSet.allOf(Device.class).size());
	private Map<Device, Float> deviceToRssi = 
			new ConcurrentHashMap<Device, Float>();
	private Map<Device, Float> deviceToBattery = 
			new ConcurrentHashMap<Device, Float>();
	
	private Map<Device, Long> latestHeartbeats = new EnumMap<Device, Long>(Device.class);
	
	private Set<IDeviceStatusListener> listeners = new HashSet<IDeviceStatusListener>();
	
	
	
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
		
		deviceToRssi.put(d, (float)rssi / 100.0f);		
		deviceToBattery.put(d, (float)battery / 3700.0f); // NOTE: If this is constantly too low, then change 5000 to 3700
		
		latestHeartbeats.put(d, System.currentTimeMillis());
		log.debug("Device {} at address {} (RSSI={})", new Object[]{d,address,rssi});
		
		// blast out a notice regardless of whether the data changed
		notifyListeners();
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
	 * @return the most recent RSSI value as a percentage [0,1].
	 */
	public float getDeviceRssi(Device d) {
		Float rssi = deviceToRssi.get(d);
		if (rssi == null)
			return 0f;
		return rssi;
	}
	
	/**
	 * @param d
	 * @return the most recent battery level for d as a percentage [0,1]
	 */
	public float getDeviceBattery(Device d) {
		Float battery = deviceToBattery.get(d);
		if (battery == null)
			return 0f;
		return battery;
	}
	
	public long getLastUpdateTime(Device d) {
		Long t = latestHeartbeats.get(d);
		if (t == null) {
			return -1;
		} else {
			return t;
		}
	}
	
	public void addListener(IDeviceStatusListener l) {
		listeners.add(l);
	}
	
	public void removeListener(IDeviceStatusListener l) {
		listeners.remove(l);
	}
	
	
	private void notifyListeners() {
		for (IDeviceStatusListener l : listeners) {
			l.deviceStatusChanged(this);
		}
	}
}
