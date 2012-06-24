package ca.site3.ssf.ioserver;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.site3.ssf.gamemodel.FireEmitterChangedEvent;
import ca.site3.ssf.gamemodel.IGameModel.Entity;

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
	
	private BufferedOutputStream serialOutStream;
	private BufferedInputStream serialInStream;
	
	private BlockingQueue<byte[]> messageQueue = new LinkedBlockingQueue<byte[]>();
	
	
	public SerialCommunicator(InputStream serialIn, OutputStream serialOut) {
		this.serialInStream = new BufferedInputStream(serialIn);
		this.serialOutStream = new BufferedOutputStream(serialOut);
	}
	
		
	
	public void run() {
		while (true) {
			try {
//System.out.println("take...");
				byte[] message = messageQueue.take();
//System.out.println("...took");
				serialOutStream.write(message);
				serialOutStream.flush();
//System.out.println("wrote and flushed: "+message);
			} catch (IOException ex) {
				log.error("Error writing to serial device",ex);
			} catch (InterruptedException ex) {
				log.warn("Interrupted trying to get message from queue",ex);
			}
		}
	}

	

	/**
	 * Send messages to the serial port to trigger the flame effect.
	 * See https://code.google.com/p/super-street-fire/wiki/FlameEffectControllerProtocol
	 * 
	 * Each flame effect has 5 outputs: '1','2','3','4' for effect outputs (values from 0-100)
	 * 									'A' for AC output of hot surface igniter (values 0 or 1)
	 * 
	 * These outputs are controlled by sending commands. One command per output means that we
	 * need to send 4 commands per event to ensure each effect output is in the correct state.
	 * 
	 * We get one of these events for each emitter every tick, even if there's no actual change.
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
		 * effect output 1 -> player 1 colour
		 * effect output 2 -> player 2 colour
		 * effect output 3 -> both players colour
		 * effect output 4 -> ringmaster colour
		 */
		
		// corresponds to effect outputs '1' to '4' (i _think_ this is meant to be ASCII)
		byte[] commands = new byte[] { (byte)49,(byte)50,(byte)51,(byte)52 }; 
		byte[] values = new byte[4]; // the values for the commands
		
		if (event.getContributingEntities().size() > 1) {
			// multiple contributing entities must be P1 and P2 (i think)  so turn off other effect heads 
			values[0] = 0; // (byte)(100 * event.getIntensity(Entity.PLAYER1_ENTITY);
			values[1] = 0; // (byte)(100 * event.getIntensity(Entity.PLAYER2_ENTITY);
			values[2] = (byte)(100 * event.getMaxIntensity()); // other option to sum P1 and P2 intensities
			values[3] = 0; // ringmaster
		} else if (event.getContributingEntities().contains(Entity.PLAYER1_ENTITY)) {
			values[0] = (byte) (100 * event.getIntensity(Entity.PLAYER1_ENTITY));
			values[1] = 0;
			values[2] = 0;
			values[3] = 0;
		} else if (event.getContributingEntities().contains(Entity.PLAYER2_ENTITY)) {
			values[0] = 0;
			values[1] = (byte) (100 * event.getIntensity(Entity.PLAYER2_ENTITY));
			values[2] = 0;
			values[3] = 0;
		} else if (event.getContributingEntities().contains(Entity.RINGMASTER_ENTITY)) {
			values[0] = 0;
			values[1] = 0;
			values[2] = 0;
			values[3] = (byte) (100 * event.getIntensity(Entity.RINGMASTER_ENTITY));
		} else {
			values[0] = values[1] = values[2] = values[3] = 0;
		}
		
		byte destNode = getHardwareIdFromEvent(event);
		
		for (int effectOutputIndex=0; effectOutputIndex < 4; effectOutputIndex++) {
			byte[] payload = new byte[] { destNode, commands[effectOutputIndex], values[effectOutputIndex] };
			byte checksum = getChecksum(payload);
			byte[] message = new byte[] {	
					(byte)0xAA, (byte)0xAA,	// framing bytes
					(byte)3,				// payload length: dest node + command + value
					payload[0],payload[1],payload[2],
					checksum
			};
			enqueueMessage(message);
		}
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
	private byte getHardwareIdFromEvent(FireEmitterChangedEvent e) {
		
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
		 * Note: bytes are signed in Java and represented as 2's complement. 
		 * I have my doubts whether this algorithm will be equivalent to whatever 
		 * the boards are doing.
		 */
		byte sum = 0;
		for (byte b : payload) {
			sum += b; // seems likely that this will roll over
		}
		return (byte) ~sum;
	}
	
	
	/**
	 * Places a message on the queue to be sent over the serial interface.
	 * @param message
	 */
	private void enqueueMessage(byte[] message) {
//System.out.println("enqueuing message...");
		if (messageQueue.offer(message) == false) {
			log.warn("No room on queue for serial message");
		}
//System.out.println("\t...done");
	}
}
