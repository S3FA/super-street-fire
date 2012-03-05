package ca.site3.ssf.ioserver;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The DeviceNetworkListener listens on a UDP socket for events from the game
 * peripherals (gloves, headsets). It passes the data to an {@link IDeviceDataParser}
 * which unpacks the data into a {@link DeviceEvent}. This event is then placed onto
 * a queue to be consumed.
 * 
 * @author greg
 */
public class DeviceNetworkListener implements Runnable {

	private Logger log = LoggerFactory.getLogger(getClass());
	
	private IDeviceDataParser dataParser;
	
	private int port;
	private DatagramSocket socket;
	
	private Queue<DeviceEvent> eventQueue;
	
	private volatile boolean stop = false;
	
	
	/**
	 * @param port the port to listen on
	 * @param q queue the {@link DeviceEvent}s will be placed on 
	 */
	public DeviceNetworkListener(int port, Queue<DeviceEvent> q) {
		this(port, new DeviceDataParser(), q);
	}
	
	/**
	 * @param port the port to listen on
	 * @param dataParser an object that can translate raw data into higher-level {@link DeviceEvent}s
	 * @param q queue the {@link DeviceEvent}s will be placed on 
	 */
	public DeviceNetworkListener(int port, IDeviceDataParser dataParser, Queue<DeviceEvent> q) {
		this.port = port;
		this.dataParser = dataParser;
		this.eventQueue = q;
	}

	
	public void run() {
		stop = false;
		try {
			socket = new DatagramSocket(port);
			socket.setSoTimeout(10 * 1000);
		} catch (SocketException ex) {
			log.error("Unable to open UDP socket for listening on port "+port, ex);
			return;
		}
		
		int bufSize = 1024;
		byte receivedData[] = new byte[bufSize];
		
		while ( ! stop ) {
			DatagramPacket receivedPacket = new DatagramPacket(receivedData, bufSize);
			try {
				socket.receive(receivedPacket);
			} catch (SocketTimeoutException ex) {
				log.info("Device listener timed out");
			} catch (SocketException ex) {
				log.info("Device listener socket closed",ex);
				break;
			} catch (IOException ex) {
				log.warn("Exception receiving packet",ex);
			}
			
			try {
				byte[] data = Arrays.copyOfRange(receivedPacket.getData(), receivedPacket.getOffset(), 
						receivedPacket.getOffset()+receivedPacket.getLength());
				InetAddress address = receivedPacket.getAddress();
				DeviceEvent event = dataParser.parseDeviceData(data, address);
				eventQueue.add(event);
			} catch (Exception ex) {
				log.warn("Could not parse packet data", ex);
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
