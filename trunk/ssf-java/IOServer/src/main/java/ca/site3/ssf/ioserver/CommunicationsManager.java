package ca.site3.ssf.ioserver;

import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import ca.site3.ssf.gamemodel.AbstractGameModelCommand;
import ca.site3.ssf.gamemodel.IGameModelEvent;
import ca.site3.ssf.gesturerecognizer.GestureInstance;

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
	
	private BlockingQueue<AbstractGameModelCommand> commandQueue = new LinkedBlockingQueue<AbstractGameModelCommand>();
	
	private BlockingQueue<PlayerGestureInstance> gestureQueue = new LinkedBlockingQueue<PlayerGestureInstance>();
	
	
	@SuppressWarnings("unchecked")
	public void shutdown() {
		for (BlockingQueue<?> q : Arrays.asList(commOutQueue,commInQueue,guiOutQueue,commandQueue)) {
			q.clear();
		}
	}
	
	BlockingQueue<IGameModelEvent> getCommOutQueue() {
		return commOutQueue;
	}


	/**
	 * Queue for events received from peripherals.
	 * 
	 * Populated by the {@link DeviceNetworkListener} / {@link IDeviceDataParser}.
	 * Consumed by {@link GloveEventCoalescer} which creates {@link GestureInstance}s
	 * and places them onto the queue returned by {@link #getGestureQueue()}.
	 */
	BlockingQueue<DeviceEvent> getCommInQueue() {
		return commInQueue;
	}


	BlockingQueue<IGameModelEvent> getGuiOutQueue() {
		return guiOutQueue;
	}


	BlockingQueue<AbstractGameModelCommand> getCommandQueue() {
		return commandQueue;
	}
	
	
	/**
	 * 
	 * @return
	 */
	BlockingQueue<PlayerGestureInstance> getGestureQueue() {
		return gestureQueue;
	}
}
