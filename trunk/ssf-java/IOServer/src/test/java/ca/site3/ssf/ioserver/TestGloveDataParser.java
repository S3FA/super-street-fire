package ca.site3.ssf.ioserver;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.LinkedList;
import java.util.Queue;

import org.junit.Test;

import ca.site3.ssf.ioserver.DeviceConstants.Device;

public class TestGloveDataParser {

	@Test
	public void testGloverParser() {
		
		Queue<DeviceEvent> q = new LinkedList<DeviceEvent>();
		
		try {
			InetAddress localhost = InetAddress.getLocalHost();
			int port = 31337;
			
			DeviceStatus deviceStatus = new DeviceStatus();
			deviceStatus.setDeviceInfo(Device.P1_LEFT_GLOVE, localhost, (byte)1, 1);
			DeviceNetworkListener listener = new DeviceNetworkListener(port, new DeviceDataParser(deviceStatus), q);
			Thread listenerThread = new Thread(listener, "DeviceNetworkListener Thread");
			listenerThread.start();
			
			DatagramSocket socket = new DatagramSocket();
			
			FileReader fr = new FileReader(new File(getClass().getClassLoader().getResource("gloves.data").toURI()));
			BufferedReader reader = new BufferedReader(fr);
			for (String line = reader.readLine(); line != null; line = reader.readLine()) {
				DatagramPacket p = new DatagramPacket(line.getBytes("ASCII"), line.length(), localhost, port);
				socket.send(p);
			}
			
			// some time to catch up with stuff
			try {
				Thread.sleep(1000);
			} catch (InterruptedException ex) {
				ex.printStackTrace();
			}
			
			socket.close();
			listener.stop();
		} catch (Exception ex) {
			fail(ex.getMessage());
		}

		assertEquals("Frame count / event count mismatch", 7, q.size());
		assertEquals(GloveEvent.class, q.peek().getClass());
		GloveEvent e = (GloveEvent) q.peek();
		
		double expectedGyro[] = new double[] { 109.71, 72.51,-97.64 };
		double expectedAccel[] = new double[] {26.41, -70.77, 4528.14}; 
		double epsilon = 0.000001;
		for (int i=0; i<3; i++) {
			assertEquals("Gyro mismatch",expectedGyro[i], e.getGyro()[i], epsilon);
			assertEquals("Accelerometer mismatch",expectedAccel[i], e.getAcceleration()[i], epsilon);
		}
	}

}