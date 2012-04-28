package ca.site3.ssf.ioserver;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.site3.ssf.ioserver.DeviceConstants.Device;

/**
 * Listens for heartbeat messages from gloves and headsets.
 * Updates DeviceStatus accordingly.
 * 
 * Datagram format here: http://code.google.com/p/super-street-fire/wiki/GloveDataProtocol
 * 
 * <pre>
 * byte    size    description
 *    0       6    MAC address of AP that we are Associated with (for location)
 *    6       1    Channel we are on.
 *    7       1    RSSI
 *    8       2    local TCP port# (for connecting into the Wifly device )
 *   10       4    RTC value (MSB first to LSB last)
 *   14       2    Battery Voltage on Pin 20 in millivolts (2755 for example)
 *   16       2    value of the GPIO pins
 *   18      13    ASCII time
 *   32      26    Version string with date code
 *   60      32    Programmable Device ID string (set option deviceid <string>)
 *   92       2    Boot time in milliseconds.
 *   94      16    Voltage readings of Sensors 0 thru 7 (enabled with Òset opt format <mask>Ó )
 * </pre>
 * 
 * @author greg
 */
public class HeartbeatListener implements Runnable {

	private Logger log = LoggerFactory.getLogger(getClass());
	
	private int port;
	private DatagramSocket socket;
	
	private volatile boolean shouldListen = true;
	
	private DeviceStatus deviceStatus;
	
	
	
	public HeartbeatListener(int port, DeviceStatus deviceStatus) {
		this.port = port;
		this.deviceStatus = deviceStatus;
	}
	
	
	public void run() {
		
		try {
			socket = new DatagramSocket(port);
			log.info("Listening for heartbeats on port {} (UDP)",port);
		} catch (SocketException ex) {
			log.error("Could not start heartbeat listener",ex);
			return;
		}
		
		int bufSize = 128;
		byte receivedData[] = new byte[bufSize];
		DatagramPacket receivedPacket = new DatagramPacket(receivedData, bufSize);
		
		// some reusable arrays to avoid continually allocating heap memory
		byte[] batteryBuf = new byte[2];
		byte[] deviceidBuf = new byte[6]; // see note about cheating below
		
		while (shouldListen) {
			
			// reset in case of changing buffer size
			receivedPacket.setLength(bufSize);
			
			try {
				socket.receive(receivedPacket);
			} catch (SocketTimeoutException ex) {
				log.debug("Heartbeat listener timed out");
			} catch (SocketException ex) {
				log.info("Heartbeat socket closed",ex);
				break;
			} catch (IOException ex) {
				log.warn("Exception processing heartbeat",ex);
			}
			
			try {
				byte data[] = receivedPacket.getData();
				int offset = receivedPacket.getOffset();
				
				byte rssi = data[offset+7];
				
				System.arraycopy(data, offset+14, batteryBuf, 0, 2);
				// assuming big endian
				int battery = 0;
				for (int i = 0; i < batteryBuf.length; i++) {
				   battery = (battery << 8) + (batteryBuf[i] & 0xff);
				}
				
				// NOTE! according to protocol deviceid could be up to 32 bytes
				// but we're cheating and assuming format of SSFP?? (or SSFRM?)
				System.arraycopy(data, offset+60, deviceidBuf, 0, 6);
				String idString = new String(deviceidBuf);
				
				deviceStatus.setDeviceInfo(Device.fromId(idString), 
					receivedPacket.getAddress(), rssi, battery);
				
			} catch (Exception ex) {
				log.warn("Could not parse packet data", ex);
			}
		}
		
		log.info("Stopped listening for device heartbeats");
	}
	
	
	public void stop() {
		shouldListen = false;
	}
}
