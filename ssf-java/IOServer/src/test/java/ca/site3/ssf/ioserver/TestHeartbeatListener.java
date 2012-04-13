package ca.site3.ssf.ioserver;

import static org.junit.Assert.*;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import org.junit.Test;

import ca.site3.ssf.ioserver.DeviceConstants.Device;

public class TestHeartbeatListener {

	@Test
	public void testSimpleHeartbeat() {
		BufferedInputStream is = new BufferedInputStream(getClass().getClassLoader().getResourceAsStream("heartbeat.data"));
		byte[] data = new byte[110];
		try {
			is.read(data);
		} catch (IOException ex) {
			fail(ex.getMessage());
		}
		
		DeviceStatus deviceStatus = new DeviceStatus();
		int port = 55555;
		HeartbeatListener hbl = new HeartbeatListener(port, deviceStatus);
		
		Thread listenerThread = new Thread(hbl,"Heartbeat unit test thread");
		listenerThread.start();
		
		try {
			InetAddress localhost = InetAddress.getByName("localhost");
			DatagramPacket p = new DatagramPacket(data, data.length, localhost, port);
			DatagramSocket socket = new DatagramSocket();
			socket.send(p);
			
			try {	
				Thread.sleep(500);
			} catch (InterruptedException ex) {}
			
			Device d = Device.P1_LEFT_GLOVE;
			assertEquals(deviceStatus.getDeviceAddress(d), localhost);
			assertEquals(deviceStatus.getDeviceAtAddress(localhost), d);
			assertEquals(deviceStatus.getDeviceRssi(d), 0.3622, 0.00001);
			
			float batteryMax = 5000f; // might need to change to 3700 or ??
			assertEquals(deviceStatus.getDeviceBattery(d), 3061 / batteryMax, 0.00001);
			
			socket.close();
			hbl.stop();
		} catch (UnknownHostException ex) {
			fail(ex.getMessage());
		} catch (SocketException ex) {
			fail(ex.getMessage());
		} catch (IOException ex) {
			fail(ex.getMessage());
		}
		
		
	}

}
