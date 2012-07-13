package ca.site3.ssf.ioserver;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.site3.ssf.gamemodel.FireEmitterChangedEvent;
import ca.site3.ssf.gamemodel.IGameModel.Entity;
import ca.site3.ssf.gamemodel.SystemInfoRefreshEvent;
import ca.site3.ssf.gamemodel.SystemInfoRefreshEvent.OutputDeviceStatus;
import ca.site3.ssf.guiprotocol.StreetFireServer;

/**
 * Contains logic for converting game event data to the format expected by the
 * output hardware, and writes it out to the serial device.
 * 
 * Also stuff for sending query commands to the boards and awaiting their responses.
 * 
 * @author greg
 */
public class SerialCommunicator implements Runnable {

	private Logger log = LoggerFactory.getLogger(getClass());
	
	private static final byte[] STOP_SENTINEL = new byte[] { (byte)0 };
	private static final byte[] QUERY_SYSTEM_SENTINEL = new byte[] { (byte)'?' };
	
	
	private OutputDeviceStatus[] systemStatus = null;
	
	private BufferedOutputStream out;
	
	private BlockingQueue<byte[]> messageQueue = new LinkedBlockingQueue<byte[]>();
	
	private SerialDataReader reader;
	
	/** not ideal that this is here. used to fire system info refresh event to GUIs */
	private StreetFireServer server;
	
	
	public SerialCommunicator(InputStream serialIn, OutputStream serialOut, StreetFireServer guiOut) {
		this.reader = new SerialDataReader(serialIn);
		this.out = new BufferedOutputStream(serialOut);
		this.server = guiOut;
	}
	
	/**
	 * Puts a sentinel value onto the message queue that will stop
	 * this SerialCommunicator from listening for events to
	 * broadcast out.
	 */
	public void stop() {
		messageQueue.add(STOP_SENTINEL);
	}
	
	public void run() {
		Thread inThread = new Thread(reader, "Serial input reader");
		inThread.start();
		
		while ( true ) {
			try {
				byte[] message = messageQueue.take();
				if (message.equals(STOP_SENTINEL)) {
					log.info("SerialCommunicator stopping");
					break;
				}
				if (message.equals(QUERY_SYSTEM_SENTINEL)) {
					doSystemQuery();
				} else {
					this.out.write(message);
					this.out.flush();
//					 log.debug("wrote message: {}", bytesToHexString(message));
				}
			} catch (IOException ex) {
				log.error("Error writing to serial device",ex);
			} catch (InterruptedException ex) {
				log.warn("Interrupted trying to get message from queue",ex);
			}
		}
		reader.stop();
	}

	
	final char[] hexArray = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
	@SuppressWarnings("unused")
	private String bytesToHexString(byte[] bytes) {
	    char[] hexChars = new char[bytes.length * 2];
	    int v;
	    for ( int j = 0; j < bytes.length; j++ ) {
	        v = bytes[j] & 0xFF;
	        hexChars[j * 2] = hexArray[v >>> 4];
	        hexChars[j * 2 + 1] = hexArray[v & 0x0F];
	    }
	    return new String(hexChars);
	}
	

	/**
	 * Send messages to the serial port to trigger the flame effect.
	 * See https://code.google.com/p/super-street-fire/wiki/FlameEffectControllerProtocol
	 * 
	 * Each flame effect has 5 outputs: '1','2','3','4' for effect outputs (values from 0-100)
	 * 									'A' for AC output of hot surface igniter (values 0 or 1)
	 * 
	 * These outputs are controlled by sending one command per output (multiple commands
	 * can be in a message payload).
	 * 
	 * Note: we get one of these events for each emitter every tick, even if there's no actual change.
	 * 
	 * 
	 * @param event
	 */
	void notifyFireEmitters(FireEmitterChangedEvent event) {
		
		/*
		 * Message format: 0xAA 0xAA [payload_length] [ ... payload ... ] [payload_checksum]
		 * Payload for purposes of fire emitters is:
		 * [dest node] [command] [value]
		 * 
		 * dest node is easily obtainable, see getHardwareIdFromEvent
		 * command possibilities are n = ascii '1' - '4' corresponding to flame effect output n to [value] %
		 * 
		 * i'm not sure my sure this mapping is correct but the way this is currently implemented is:
		 * 
		 * effect output 1 -> fire!
		 * effect output 2 -> player 1 colour
		 * effect output 3 -> player 2 colour
		 * effect output 4 -> currently unused
		 */
		
		// corresponds to effect outputs '1' to '4' (i _think_ this is meant to be ASCII)
		byte[] commands = new byte[] { (byte)49,(byte)50,(byte)51,(byte)52 }; 
		byte[] values = new byte[] { 0, 0, 0, 0 }; // the values for the commands
		
		if (event.getContributingEntities().size() > 0) {
			values[0] = (byte)(100 * event.getMaxIntensity()); // other option is to sum intensities
			
			if (event.getContributingEntities().contains(Entity.PLAYER1_ENTITY)) {
				values[1] = (byte) (100 * event.getIntensity(Entity.PLAYER1_ENTITY));
			}
			if (event.getContributingEntities().contains(Entity.PLAYER2_ENTITY)) {
				values[2] = (byte) (100 * event.getIntensity(Entity.PLAYER2_ENTITY));
			}
		}
		
		
		byte[] payload = new byte[9]; // 1 byte for dest node + 4*2 bytes command/values 
		payload[0] = getHardwareIdFromEvent(event);
		
		for (int effectOutputIndex=0; effectOutputIndex < 4; effectOutputIndex++) {
			payload[effectOutputIndex*2 + 1] = commands[effectOutputIndex];
			payload[effectOutputIndex*2 + 2] = values[effectOutputIndex];
		}

		byte[] message = new byte[payload.length + 4];
		message[0] = message[1] = (byte) 0xAA; // framing bytes
		message[2] = (byte)payload.length;
		for (int i=0; i < payload.length; i++) {
			message[i+3] = payload[i];
		}
		message[12] = getChecksum(payload);
		enqueueMessage(message);
	}
	
	
	/**
	 * Sends out a query command to the devices one at a time.
	 * Note! this will hold up the serial comm thread so it should not be
	 * done while a game is actually going on.
	 */
	void querySystemStatus() {
		enqueueMessage( QUERY_SYSTEM_SENTINEL ); // sentinel value to trigger system query
	}
	
	
	
	/**
	 * Queries each emitter sequentially for its status. Waits a bit
	 * for a response then moves to the next if nothing after 100 ms.
	 * 
	 * This method gets called on the comm thread, so it
	 * directly writes to the serial out stream.
	 */
	private void doSystemQuery() {
		
		log.info("Querying boards...");
		
		systemStatus = new OutputDeviceStatus[32]; // indexes are off-by-one compared to device IDs
		for (int i=0; i<32; i++) {
			systemStatus[i] = new OutputDeviceStatus(i, false, false, false);
		}
		
		// sent out to each emitter in turn
		byte[] messageTemplate = new byte[] { 
			(byte) 0xAA, (byte) 0xAA, // framing bytes
			(byte) 2, // payload length (node ID and command)
			0, // destination node to be replaced within the loop
			(byte)63, // '?' command in ASCII
			0 // checksum to be replaced within the loop
		};
		
		
		reader.clear();
		for (int i=1; i<=32; i++) {
			messageTemplate[3] = (byte)i;
			messageTemplate[5] = (byte) ~(i + 63);
			try {
				this.out.write(messageTemplate);
				this.out.flush();
				try {
					OutputDeviceStatus status = reader.getStatusUpdateQueue().poll(100, TimeUnit.MILLISECONDS);
					if (status != null) {
						systemStatus[status.deviceId - 1] = status;
					}
				} catch (InterruptedException ex) {
					log.warn("Interrupted while waiting for device status",ex);
				}
				
			} catch (IOException ex) {
				log.error("Error reading or writing status query for node", ex);
			}
		}
		
		
		/*
		 * Convert from device ID to GameModel. Inverse of getHardwareIdFromEvent
		 */
		OutputDeviceStatus[] leftRailStatus = new OutputDeviceStatus[8];
		for (int i=0; i<8; i++) {
			leftRailStatus[i] = systemStatus[8+i];
		}
		OutputDeviceStatus[] rightRailStatus = new OutputDeviceStatus[8];
		for (int i=0; i<8; i++) {
			rightRailStatus[i] = systemStatus[i];
		}
		OutputDeviceStatus[] outerRingStatus = new OutputDeviceStatus[16];
		for (int i=0; i<8; i++) {
			outerRingStatus[i] = systemStatus[16+i];
		}
		for (int i=8; i<16; i++) {
			outerRingStatus[i] = systemStatus[31-(i-8)];
		}
		
		/*
		 * The organization of this code is not ideal. For convenience,
		 * SystemInfoRefreshEvent implements IGameModelEvent even though it
		 * doesn't originate from the GameModel. I'm leaving it that way for
		 * now because I can't bear the thought of adding yet another queue
		 * somewhere. Only the GUIs get notified of this, not any other
		 * IGameModelListeners
		 */
		SystemInfoRefreshEvent refreshEvent = new SystemInfoRefreshEvent(leftRailStatus, rightRailStatus, outerRingStatus);
		log.debug("Notifying GUI of system status");
		server.notifyGUI(refreshEvent);
	}
	
	
	
	
	/**
	 * The hardware device ID layout is as follows:
	 * https://code.google.com/p/super-street-fire/wiki/FlameEffectControllerProtocol
	 * <pre>
	 *                             QP1
	 *                 17                          25
	 *             18             33 35               26
	 *     ---------                                 ------------
	 *                       1             9
	 *
	 *         19            2             10             27
	 *         
	 *                       3             11
	 *    | 20                                                28  |
	 *    |                  4             12                     |
	 * QF |                                                       | QBack
	 *    |                  5             13                     |
	 *    | 21                                                29  |
	 *                       6             14
	 *         
	 *         22            7             15             30
	 *             
	 *                       8             16
	 *     ----------                                 ------------
	 *             23                                 31
	 *                            34 36            
	 *                 24                        32
	 *                             QP2
	 * </pre>
	 * 
	 * <p>33 -> P1 air cannon / platform lights<br />
	 *    34 -> P2 air cannon / platform lights<br />
	 *    35 -> P1 life bars / timer<br />
	 *    36 -> P1 life bars / timer
	 * </p>
	 * 
	 * FireEmitterModel (package-private class) is where the GameModel IDs are defined:<br>
	 * 
	 * The outer ring of emitters start at the first emitter to the right
	 *   of player one's back and moving in a counter-clockwise direction.<br>
	 *   --> This starts at 17 and goes up to 24 (0-7 in FireEmitterModel terms) , 
	 *       then goes backwards from 32 to 25 for FireEmitterModel indexes 8-15
	 * <br />
	 * Left rail is the rail of emitters on the left from player one's perspective,
	 *	 they are ordered starting at player one going towards player two.<br>
	 * <br />
	 * Right rail is the rail of emitters on the right from player one's perspective,
	 *   they are ordered starting at player one going towards player two<br>
	 * 
	 * Note GameModel indexes are 0-based and per-rail or per-outer ring
	 * 
	 * @param e
	 * @return
	 */
	private static byte getHardwareIdFromEvent(FireEmitterChangedEvent e) {
		
		switch (e.getLocation()) {
		case RIGHT_RAIL:
			return (byte) (1 + e.getIndex());
		case LEFT_RAIL:
			return (byte) (9 + e.getIndex());
		case OUTER_RING:
			if (e.getIndex() <=7) {
				return (byte) (17 + e.getIndex());
			} else {
				// e.getIndex will be from 8 to 15 corresponding to 32 to 25
				// 8->32, 9-31, 10->30, 11->29, 12->28, 13->27, 14->26, 15->25
				return (byte) (32 + (8 - e.getIndex()));
			}
		}
		throw new IllegalArgumentException("Can't deal with FireEmitterChangedEvent: "+e);
	}
	
	
	
	
	/**
	 * The checksum is one byte and computed by summing each of the payload bytes, 
	 * then taking the one's complement of the sum.
	 * 
	 * @param payload
	 * @return
	 */
	private byte getChecksum(byte[] payload) {
		/*
		 *  Note: bytes are signed in Java and represented as 2's complement.
		 *  We'll sum with a byte and let it roll over (could also use an int and 
		 *  use the least significant byte).
		 */
		byte sum = 0;
		for (byte b : payload) {
			sum += b; // seems likely that this will roll over
		}
		return (byte) ~sum; // tilde operator flips each bit (aka 1's complement) 
	}
	
	
	/**
	 * Places a message on the queue to be sent over the serial interface.
	 * @param message
	 */
	private void enqueueMessage(byte[] message) {
		if (messageQueue.offer(message) == false) {
			log.warn("No room on queue for serial message");
		}
	}
}
