package ca.site3.ssf.ioserver;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.RXTXCommDriver;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.Enumeration;

import org.junit.Test;

public class TestSerialStuff {

	SerialPort serialPort;
	
	@Test
	public void test() {
		
		initSerialStuff();
		
		assertNotNull(serialPort);
		
		BufferedOutputStream ostream = null;
		try {
			ostream = new BufferedOutputStream(serialPort.getOutputStream());
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		assertNotNull(ostream);
		try {
			ostream.write("Hadouken! Spinning Bird Kick!\n".getBytes());
			ostream.flush();
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
	private void initSerialStuff() {
		
		String serialDevice = "/dev/master";
		
		CommPortIdentifier commPortId = null;
		try {
			commPortId = CommPortIdentifier.getPortIdentifier(serialDevice);
		} catch (NoSuchPortException ex) {
			ex.printStackTrace();
			CommPortIdentifier.addPortName(serialDevice, CommPortIdentifier.PORT_SERIAL, new RXTXCommDriver());
			try {
				commPortId = CommPortIdentifier.getPortIdentifier(serialDevice);
			} catch (NoSuchPortException ex2) {
				System.out.println("Could not open or add serial port '"+ serialDevice+"'");
				ex.printStackTrace();
				fail();
			}
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
}
