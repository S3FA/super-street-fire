package ca.site3.ssf.ioserver;

import java.util.AbstractQueue;
import java.util.Arrays;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Holds queues to be used for passing game events / info to and from the main event thread.
 * Also manages consumer threads for the queues.
 * 
 * @author greg
 */
public class CommunicationsManager {

	private AbstractQueue<GameEvent> commOutQueue = new ConcurrentLinkedQueue<GameEvent>();
	
	private AbstractQueue<HardwareEvent> commInQueue = new ConcurrentLinkedQueue<HardwareEvent>();
	
	private AbstractQueue<GameEvent> guiOutQueue = new ConcurrentLinkedQueue<GameEvent>();
	
	private AbstractQueue<GUIEvent> guiInQueue = new ConcurrentLinkedQueue<GUIEvent>();
	
	
	private Thread commOutConsumer;
	private Thread commInConsumer;
	private Thread guiOutConsumer;
	private Thread guiInConsumer;
	
	
	/**
	 * Start the threads that will be the consumers for the queues
	 */
	public void startConsumers() {
		// TODO
	}

	
	@SuppressWarnings("unchecked")
	public void shutdown() {
		for (AbstractQueue<?> q : Arrays.asList(commOutQueue,commInQueue,guiOutQueue,guiInQueue)) {
			q.clear();
		}
		for (Thread t : Arrays.asList(commOutConsumer, commInConsumer, guiOutConsumer, guiInConsumer)) {
			t.interrupt();
		}
	}
	
	AbstractQueue<GameEvent> getCommOutQueue() {
		return commOutQueue;
	}


	AbstractQueue<HardwareEvent> getCommInQueue() {
		return commInQueue;
	}


	AbstractQueue<GameEvent> getGuiOutQueue() {
		return guiOutQueue;
	}


	AbstractQueue<GUIEvent> getGuiInQueue() {
		return guiInQueue;
	}
}
