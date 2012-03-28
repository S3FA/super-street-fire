package ca.site3.ssf.ioserver;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.Queue;

import org.junit.Test;

public class TestLegacyGloveParser {

	@Test
	public void testGloverParser() {
		BufferedInputStream is = new BufferedInputStream(getClass().getClassLoader().getResourceAsStream("gloves_legacy.data"));
		byte[] bytes = new byte[12098];
		try {
			is.read(bytes);
		} catch (IOException ex) {
			fail(ex.getMessage());
		}
		String buf = new String(bytes);
		String[] frames = buf.split("\\|");
		
		Queue<DeviceEvent> q = new LinkedList<DeviceEvent>();
		int port = 31337;
		DeviceNetworkListener listener = new DeviceNetworkListener(port, new LegacyGloveDataParser(), q);
		Thread listenerThread = new Thread(listener, "DeviceNetworkListener Thread");
		listenerThread.start();
		
		
		try {
			InetAddress localhost = InetAddress.getByName("localhost");
			DatagramSocket socket = new DatagramSocket();
			
			for (String frame : frames) {
				frame = frame + "|";
				DatagramPacket p = new DatagramPacket(frame.getBytes("ASCII"), frame.length(), localhost, port);
				socket.send(p);
			}
			
			// some time to catch up with stuff
			try {
				Thread.sleep(2000);
			} catch (InterruptedException ex) {
				ex.printStackTrace();
			}
			
			socket.close();
			listener.stop();
			
			assertEquals("Frame count / event count mismatch",frames.length, q.size());
			
			// head, accel, gyros
			// 1L:0.52,0.29,148.84_12,50,8_-2,3,246|
			assertEquals(GloveEvent.class, q.peek().getClass());
			GloveEvent e = (GloveEvent) q.peek();
			
			float[] expectedMagnetometer = new float[] { 0.52f, 0.29f, 148.84f };
			float[] expectedAccel = new float[] { 12, 50, 8 };
			float[] expectedGyro = new float[] { -2f, 3f, 246f };
			
			float eps = 0.000001f;
			for (int i=0; i<3; i++) {
				assertEquals("Magnetometer mismatch",expectedMagnetometer[i], e.getMagnetometer()[i], eps);
				assertEquals("Accelerometer mismatch",expectedAccel[i], e.getAcceleration()[i], eps);
				assertEquals("Gyro data mismatch",expectedGyro[i], e.getGyro()[i], eps);
			}
			
			
		} catch (UnknownHostException ex) {
			fail(ex.getMessage());
		} catch (SocketException ex) {
			fail(ex.getMessage());
		} catch (IOException ex) {
			fail(ex.getMessage());
		}
	}

}
