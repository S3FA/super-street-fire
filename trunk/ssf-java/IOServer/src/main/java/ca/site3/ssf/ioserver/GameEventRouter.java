package ca.site3.ssf.ioserver;

import ca.site3.ssf.gamemodel.FireEmitterChangedEvent;
import ca.site3.ssf.gamemodel.GameState.GameStateType;
import ca.site3.ssf.gamemodel.GameInfoRefreshEvent;
import ca.site3.ssf.gamemodel.GameStateChangedEvent;
import ca.site3.ssf.gamemodel.IGameModel;
import ca.site3.ssf.gamemodel.IGameModelEvent;
import ca.site3.ssf.gamemodel.IGameModelEvent.Type;
import ca.site3.ssf.gamemodel.IGameModelListener;
import ca.site3.ssf.gamemodel.PlayerActionPointsChangedEvent;
import ca.site3.ssf.gamemodel.PlayerHealthChangedEvent;
import ca.site3.ssf.gamemodel.RoundBeginTimerChangedEvent;
import ca.site3.ssf.gamemodel.RoundPlayTimerChangedEvent;
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
		
		switch (event.getType()) {
		
			case FIRE_EMITTER_CHANGED:
				serialComm.notifyFireEmitters((FireEmitterChangedEvent) event);
				break;
				
			case PLAYER_HEALTH_CHANGED:
				serialComm.onPlayerHealthChanged((PlayerHealthChangedEvent) event);
				break;
				
			case PLAYER_ACTION_POINTS_CHANGED:
				serialComm.onPlayerActionPointsChanged((PlayerActionPointsChangedEvent) event);
				break;
				
			case ROUND_PLAY_TIMER_CHANGED:
				serialComm.onTimerChanged((RoundPlayTimerChangedEvent) event);
				break;
				
			case ROUND_BEGIN_TIMER_CHANGED: {
				RoundBeginTimerChangedEvent e = (RoundBeginTimerChangedEvent) event;
				switch (e.getThreeTwoOneFightTime()) {
				case THREE:
					serialComm.onTimerChanged(3);
					break;
				case TWO:
					serialComm.onTimerChanged(2);
					break;
				case ONE:
					serialComm.onTimerChanged(1);
					break;
				case FIGHT:
					serialComm.onTimerChanged(0);
					break;
				default:
					break;
				}
				break;
			}
			
			case GAME_STATE_CHANGED: {
				GameStateChangedEvent e = (GameStateChangedEvent)event;
				if (e.getNewState() == GameStateType.IDLE_STATE) {
					serialComm.setGlowfliesOn(false);
					serialComm.onPlayerHealthChanged(new PlayerHealthChangedEvent(1, 0, 0));
					serialComm.onPlayerHealthChanged(new PlayerHealthChangedEvent(2, 0, 0));
					serialComm.onTimerChanged(new RoundPlayTimerChangedEvent(99));
				} else {
					serialComm.setGlowfliesOn(true);
				}
				break;
			}
			case GAME_INFO_REFRESH: {
				GameInfoRefreshEvent e = (GameInfoRefreshEvent)event;
				if (e.getCurrentGameState() == GameStateType.IDLE_STATE) {
					serialComm.setGlowfliesOn(false);
				} else {
					serialComm.setGlowfliesOn(true);
				}
				break;
			}
			
			default:
				break;
		}
	}
}
