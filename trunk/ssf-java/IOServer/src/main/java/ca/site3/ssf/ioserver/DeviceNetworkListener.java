package ca.site3.ssf.ioserver;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The DeviceNetworkListener listens on a UDP socket for events from the game
 * peripherals (gloves, headsets). It passes the data to an {@link IDeviceDataParser}
 * which unpacks the data into {@link DeviceEvent}s. The events are then placed onto
 * a queue to be consumed.
 * 
 * @author greg
 */
public class DeviceNetworkListener implements Runnable {

	private Logger log = LoggerFactory.getLogger(getClass());
	
	private IDeviceDataParser dataParser;
	
	private final String ipAddress;
	private final int port;
	private DatagramSocket socket;
	
	private Queue<DeviceEvent> eventQueue;
	
	private volatile boolean stop = false;
	
	
	/**
	 * @param port the port to listen on
	 * @param dataParser an object that can translate raw data into higher-level {@link DeviceEvent}s
	 * @param q queue the {@link DeviceEvent}s will be placed on 
	 */
	public DeviceNetworkListener(String ipAddress, int port, IDeviceDataParser dataParser, Queue<DeviceEvent> q) {
		this.ipAddress = ipAddress;
		this.port = port;
		this.dataParser = dataParser;
		this.eventQueue = q;
	}

	
	public void run() {
		stop = false;
		
		InetAddress localInterface = null;
		try {
			localInterface = InetAddress.getByName(this.ipAddress);
		}
		catch (UnknownHostException ex) {
			log.error("Could not find local network interface for device network listener", ex);
			
			try {
				localInterface = InetAddress.getByName("0.0.0.0");
			}
			catch (UnknownHostException e) {
				log.error("This should never ever happen.", e);
				return;
			}
		}
		
		try {
			socket = new DatagramSocket(port, localInterface);
			log.info("Listening for devices on Network Interface {} (IP) port {} (UDP)", localInterface, port);
		}
		catch (SocketException ex) {
			log.error("Unable to open UDP socket for listening on port "+port, ex);
			return;
		}
		
		final int DATAGRAM_BUFFER_SIZE = 8192;
		byte receivedData[] = new byte[DATAGRAM_BUFFER_SIZE];
		DatagramPacket receivedPacket = new DatagramPacket(receivedData, receivedData.length);
		
		while (!stop) {
			// reset buffer size
			receivedPacket.setLength(receivedData.length);
			try {
				socket.receive(receivedPacket);
			}
			catch (SocketTimeoutException ex) {
				log.debug("Device listener timed out");
			}
			catch (SocketException ex) {
				log.info("Device listener socket closed",ex);
				break;
			}
			catch (IOException ex) {
				log.warn("Exception receiving packet",ex);
			}

			try {
				InetAddress address = receivedPacket.getAddress();
				List<? extends DeviceEvent> events = dataParser.parseDeviceData(receivedPacket.getData(), receivedPacket.getLength(), address);
				if (events != null) {
					for (DeviceEvent e : events) {
						log.debug("Created DeviceEvent: {}", e.toString());
						if (e != null) {
							eventQueue.add(e);
						}
					}
				}
				else {
					try {
						log.warn("Could not parse data: " + new String(receivedPacket.getData(), 0, receivedPacket.getLength(), "ASCII").trim());
					}
					catch (UnsupportedEncodingException e) {
						log.error("Error while encoding string.", e);
					}
				}
				
			}
			catch (Exception ex) {
				try {
					log.warn("Could not parse packet data: " + new String(receivedPacket.getData(), 0, receivedPacket.getLength(), "ASCII").trim(), ex);
				} catch (UnsupportedEncodingException e) {
					log.error("Error while encoding string.", e);
				}
			}
		}
		
		if ( ! socket.isClosed()) {
			socket.close();
		}
		
		log.info("device network listener exiting");
	}
	
	public void stop() {
		log.info("Stopping device network listener");
		this.stop = true;
		this.socket.close();
	}
}
