package ca.site3.ssf.ioserver;

import ca.site3.ssf.gamemodel.FireEmitterChangedEvent;
import ca.site3.ssf.gamemodel.IGameModel;
import ca.site3.ssf.gamemodel.IGameModelEvent;
import ca.site3.ssf.gamemodel.IGameModelEvent.Type;
import ca.site3.ssf.gamemodel.IGameModelListener;
import ca.site3.ssf.guiprotocol.StreetFireServer;


/**
 * Listens for events coming from the {@link IGameModel}. 
 * 
 * Notifies connected GUIs of relevant changes via {@link StreetFireServer} 
 * and output hardware via {@link SerialCommunicator}. 
 * 
 * This class is pretty much an unnecessary middleman, but keeps the
 * I/O server down to having a single, centralized {@link IGameModelListener}.
 * 
 * @author greg
 */
public class GameEventRouter implements IGameModelListener {

	StreetFireServer server;
	SerialCommunicator serialComm;
	
	
	/**
	 * @param commQueue for events of interest to non-GUI game hardware
	 * @param serialOutputStream for 
	 */
	public GameEventRouter(StreetFireServer server, SerialCommunicator serialComm) {
		this.server = server;
		this.serialComm = serialComm;
	}


	public void onGameModelEvent(IGameModelEvent event) {
		// just blast all events out to GUI for now.
		server.notifyGUI(event);
		
		if (event.getType() == Type.FIRE_EMITTER_CHANGED) {
			serialComm.notifyFireEmitters((FireEmitterChangedEvent) event);
		} else if (event.getType() == Type.PLAYER_HEALTH_CHANGED) {
			
		} else if (event.getType() == Type.ROUND_PLAY_TIMER_CHANGED) {
			
		}
	}
}
