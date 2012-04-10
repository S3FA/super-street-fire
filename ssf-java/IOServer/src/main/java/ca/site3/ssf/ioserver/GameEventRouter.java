package ca.site3.ssf.ioserver;

import java.util.concurrent.BlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.site3.ssf.gamemodel.IGameModel;
import ca.site3.ssf.gamemodel.IGameModelEvent;
import ca.site3.ssf.gamemodel.IGameModelListener;
import ca.site3.ssf.guiprotocol.StreetFireServer;


/**
 * Handles notifications coming from the {@link IGameModel} 
 * 
 * @author greg
 */
public class GameEventRouter implements IGameModelListener {

	private Logger log = LoggerFactory.getLogger(getClass());
	
	private BlockingQueue<IGameModelEvent> commQueue;
	
	private StreetFireServer server;
	
	
	/**
	 * @param commQueue for events of interest to non-GUI game hardware
	 * @param guiQueue for events that should be passed along to the GUI
	 */
	public GameEventRouter(StreetFireServer server, BlockingQueue<IGameModelEvent> commQueue) {
		this.commQueue = commQueue;
		this.server = server;
	}


	public void onGameModelEvent(IGameModelEvent event) {
		// just blast all events out to GUI for now
		server.notifyGUI(event);
	}
	
}
