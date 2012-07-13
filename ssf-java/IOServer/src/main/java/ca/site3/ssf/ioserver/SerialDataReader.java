package ca.site3.ssf.ioserver;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import ca.site3.ssf.gamemodel.SystemInfoRefreshEvent.OutputDeviceStatus;

/**
 * Listens on an InputStream for status updates from the boards.
 * 
 * @author greg
 *
 */
public class SerialDataReader implements Runnable {

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
		byte[] buffer = new byte[6]; // used to read in responses
		int len = -1;
		
		while (shouldRun) {
			
		}
	}

	/**
	 * Clear message queue
	 */
	void clear() {
		messageQueue.clear();
	}
	
	void stop() {
		shouldRun = false;
	}
}
