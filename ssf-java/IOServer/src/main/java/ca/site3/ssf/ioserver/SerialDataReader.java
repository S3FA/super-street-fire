package ca.site3.ssf.ioserver;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.site3.ssf.common.CommUtil;
import ca.site3.ssf.gamemodel.SystemInfoRefreshEvent.OutputDeviceStatus;

/**
 * Listens on an InputStream for status updates from the boards.
 * 
 * Flame effect nodes can return their status to the server. This can be used to determine 
 * whether the boards are active and working or to get more detailed information. This should 
 * not be used with the broadcast address, as all the boards will respond at the same time. 
 * These packets take the following format for the payload, using the same framing + length 
 * header and checksum trailer:
 * [dest node = 0] [source node] 'S' [armed status] 'F' [flame status]
 * The destination node is the server (0). 
 * The source node is the node ID.
 * 'S' [armed status] - sends '0' if solenoid power is not present, '1' if present.
 * 'F' [flame status] - sends '0' if no fire is present, '1' if present, '?' if unknown.
 * 
 * @author greg
 */
public class SerialDataReader implements Runnable {

	private Logger log = LoggerFactory.getLogger(getClass());
	
	
	private BufferedInputStream inStream;
	
	private LinkedBlockingQueue<OutputDeviceStatus> messageQueue = new LinkedBlockingQueue<OutputDeviceStatus>();
	
	private volatile boolean shouldRun = true;
	
	
	public SerialDataReader(InputStream is) {
		inStream = new BufferedInputStream(is);
	}
	
	public BlockingQueue<OutputDeviceStatus> getStatusUpdateQueue() {
		return messageQueue;
	}
	
	public void run() {
		byte[] buffer = new byte[16];
		while (shouldRun) {
			try {
				int len = inStream.read(buffer);
				if (len == -1) {
					log.warn("End of serial data stream");
					break;
				}
				log.debug("Read {} bytes of serial data: {}", len, CommUtil.bytesToHexString(buffer));
				
				int msgStart = -1;
				for (int i=0; i<len; i++) {
					if (buffer[i] == (byte)0xAA) {
						msgStart = i;
						break;
					}
				}
				if (msgStart == -1) {
					log.warn("No framing bytes found");
					continue;
				}
				if ( ! validateMessage(buffer, msgStart) ) {
					log.warn("Ignoring invalid message");
					continue;
				}
				
				if (buffer[msgStart+3] != 0) {
					log.warn("Discarding message not addressed to server");
					continue;
				}
				
				boolean isArmed = buffer[msgStart+6] == 49; // ASCII '1'
				boolean isFlame = buffer[msgStart+8] == 49; // currently ignores possible '?' status
				OutputDeviceStatus status = new OutputDeviceStatus(buffer[msgStart+4], true, isArmed, isFlame);
				messageQueue.offer(status);
			} catch (IOException ex) {
				log.warn("IOException reading serial data", ex);
			}
		}
		log.info("SerialDataReader stopped.");
	}

	
	private boolean validateMessage(byte[] message, int offset) {
		if (message[offset] != (byte)0xAA || message[offset+1] != (byte)0xAA) {
			log.warn("Invalid framing bytes");
			return false;
		}
		int payloadLength = message[offset+2];
		if (offset + payloadLength + 4 >= message.length) {
			log.warn("Payload length {} + header/checksum greater than message length ({})",payloadLength,message.length);
			return false;
		}
		byte[] payload = Arrays.copyOfRange(message, offset+3, offset+3+payloadLength);
		byte checksum = SerialCommunicator.getChecksum(payload);
		int checksumIndex = offset+3+payloadLength;
		if (checksum != (byte)message[checksumIndex]) {
			byte[] stuff = new byte[] { (byte)message[checksumIndex], (byte)checksumIndex, (byte)checksum };
			System.out.println("message[checksumIndex] checksumIndex checksum" + CommUtil.bytesToHexString(stuff));
			log.warn("Invalid message checksum. Received [checksum, index, calculated: {}", stuff); 
			return false;
		}
		return true;
	}
	
	/**
	 * Clear message queue
	 */
	void clear() {
		messageQueue.clear();
	}
	
	void stop() {
		shouldRun = false;
		try {
			inStream.close();
		} catch (Exception ex) {
			log.warn("Closing serial input stream");
		}
	}
}
