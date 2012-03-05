package ca.site3.ssf.ioserver;

import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import ca.site3.ssf.gamemodel.IGameModelEvent;

/**
 * Holds queues to be used for passing game events / info to and from the main event thread.
 * Also manages consumer threads for the queues.
 * 
 * @author greg
 */
public class CommunicationsManager {

	private BlockingQueue<IGameModelEvent> commOutQueue = new LinkedBlockingQueue<IGameModelEvent>();
	
	private BlockingQueue<DeviceEvent> commInQueue = new LinkedBlockingQueue<DeviceEvent>();
	
	private BlockingQueue<IGameModelEvent> guiOutQueue = new LinkedBlockingQueue<IGameModelEvent>();
	
	private BlockingQueue<GUIEvent> guiInQueue = new LinkedBlockingQueue<GUIEvent>();
	
	
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
		for (BlockingQueue<?> q : Arrays.asList(commOutQueue,commInQueue,guiOutQueue,guiInQueue)) {
			q.clear();
		}
		for (Thread t : Arrays.asList(commOutConsumer, commInConsumer, guiOutConsumer, guiInConsumer)) {
			t.interrupt();
		}
	}
	
	BlockingQueue<IGameModelEvent> getCommOutQueue() {
		return commOutQueue;
	}


	BlockingQueue<DeviceEvent> getCommInQueue() {
		return commInQueue;
	}


	BlockingQueue<IGameModelEvent> getGuiOutQueue() {
		return guiOutQueue;
	}


	BlockingQueue<GUIEvent> getGuiInQueue() {
		return guiInQueue;
	}
}
