package ca.site3.ssf.ioserver;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.EnumSet;
import java.util.Enumeration;

import org.slf4j.LoggerFactory;

import ca.site3.ssf.gamemodel.FireEmitter;
import ca.site3.ssf.gamemodel.FireEmitter.Location;
import ca.site3.ssf.gamemodel.FireEmitterChangedEvent;
import ca.site3.ssf.gamemodel.IGameModel.Entity;
import ch.qos.logback.classic.Level;

public class TestSerialStuff {

	SerialPort serialPort;
	
//	@Test
	public void test() {
		
		initSerialStuff("/dev/master");
		
		assertNotNull(serialPort);
		
		BufferedOutputStream ostream = null;
		try {
			ostream = new BufferedOutputStream(serialPort.getOutputStream());
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		assertNotNull(ostream);
		try {
			for (int i=0; i<10000; i++) {
				ostream.write("Hadouken! Spinning Bird Kick!\n".getBytes());
				ostream.flush();
			}
			
		} catch (IOException ex) {
			ex.printStackTrace();
			fail(ex.getMessage());
		} finally {		
			closeSerialStuff();
		}
	}

	
	/**
	 * Initialize the serial comm port.
	 */
	private void initSerialStuff(String serialDevice) {
		
		
		CommPortIdentifier commPortId = null;
		
		try {
			commPortId = CommPortIdentifier.getPortIdentifier(serialDevice);
		} catch (NoSuchPortException ex) {
			ex.printStackTrace();
		} catch (UnsatisfiedLinkError ex) {
			System.out.println("Could not load rxtx serial comm native library.\n" + 
						"If you're on a Mac, copy IOServer/src/main/resources/librxtxSerial.jnilib to ~/Library/Java/Extensions/\n" +
						"Otherwise take a look here: http://rxtx.qbang.org/wiki/index.php/Main_Page");
		}
		
		Enumeration<CommPortIdentifier> ids = CommPortIdentifier.getPortIdentifiers();
		for (CommPortIdentifier id = ids.nextElement(); ids.hasMoreElements(); id = ids.nextElement()) {
			System.out.println(id.getName()+" portType: "+id.getPortType());
		}
		
		if (commPortId == null) {
			System.out.println("Could not get serial port ID for device '"+serialDevice+"'. No fire :-(");
			return;
		}
		
		try {
			serialPort = (SerialPort) commPortId.open("StreetFire IOServer", 5000);
		} catch (PortInUseException ex) {
			System.out.println("Serial port in use! This might be solved by 'sudo mkdir /var/lock; sudo chmod 777 /var/lock' on a Mac");
			ex.printStackTrace();
			return;
		} catch (NullPointerException ex) {
			System.out.println("Null pointer exception trying to open port from identifier '"+commPortId.getName()+"'");
			ex.printStackTrace();
		}
		
		System.out.println(serialPort);
		
		// 57600 8N1 for now.. may need to expose these in command line args
		int baudRate = 57600;
		int databits = SerialPort.DATABITS_8;
		int stopbits = SerialPort.STOPBITS_1;
		int parity = SerialPort.PARITY_NONE;
		
		try {
			serialPort.setSerialPortParams(baudRate, databits, stopbits, parity);
		} catch (UnsupportedCommOperationException ex) {
			System.out.println("Could not configure serial port");
			ex.printStackTrace();
		}
	}
	
	
	
	private void closeSerialStuff() {
		if (serialPort != null) {
			try {
				serialPort.getInputStream().close();
				serialPort.getOutputStream().close();
			} catch (IOException ex) {
				System.out.println("Error trying to close serial port stream");
				ex.printStackTrace();
			}
			serialPort.close();
			
			serialPort = null;
		}
	}
	
	public static void main(String[] args) {		
		configureLogging(1);
		
		TestSerialStuff tss = new TestSerialStuff();
//		tss.initSerialStuff("/dev/master");
//		tss.test();
		tss.testBoard32();
	}
	
	
	private void testBoard32() {
		initSerialStuff("/dev/tty.usbserial-A40081Z7");
		assertNotNull(serialPort);
		InputStream in = null;
		OutputStream out = null;
		try {
			in = serialPort.getInputStream();
			out = serialPort.getOutputStream();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		
		SerialCommunicator sc = new SerialCommunicator(in, out, null);
		
		Thread commThread = new Thread(sc);
		commThread.start();
		
		
		for (int i=0; i<10; i++) {
			final Entity entity = i % 2 == 0 ? Entity.PLAYER1_ENTITY : Entity.PLAYER2_ENTITY;
			final float intensity = i == 9 ? 0f : 1f;
			
			FireEmitter emitter = new FireEmitter(32,8,Location.OUTER_RING) {
				protected @Override float getContributorIntensity(Entity contributor) {
					if (contributor == entity) {
						return intensity;
					} else {
						return 0f;
					}
				}
				protected @Override EnumSet<Entity> getContributingEntities() {
					return EnumSet.of(entity);
				}
			};
			FireEmitterChangedEvent event = new FireEmitterChangedEvent(emitter);
			
			sc.notifyFireEmitters(event);
			try { Thread.sleep(1000); } catch (InterruptedException ex) { ex.printStackTrace(); }
		}
		sc.stop();
		closeSerialStuff();
	}
	
	

	private static void configureLogging(int level) {
		ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger)
				LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
		
		switch (level) {
			case 0:
				root.setLevel(Level.OFF); break;
			case 1:
				root.setLevel(Level.TRACE); break;
			case 2:
				root.setLevel(Level.DEBUG); break;
			case 3:
				root.setLevel(Level.INFO); break;
			case 4:
				root.setLevel(Level.WARN); break;
			case 5:
				root.setLevel(Level.ERROR); break;
			default:
				root.setLevel(Level.INFO);
		}
	}
}
