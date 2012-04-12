package ca.site3.ssf.ioserver;

import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import ca.site3.ssf.gamemodel.AbstractGameModelCommand;
import ca.site3.ssf.gamemodel.IGameModelEvent;
import ca.site3.ssf.gesturerecognizer.GestureInstance;
import ca.site3.ssf.gesturerecognizer.GestureRecognizer;

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
	 * Listened on by gesture recorder, though there should soon be an intermediate
	 * common layer in IOServer that aggregates {@link DeviceEvent}s into the 
	 * {@link GestureInstance} structures expected by the {@link GestureRecognizer}.
	 * @see whiteboard at site3
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
}
