package ca.site3.ssf.ioserver;

import java.awt.Color;

import ca.site3.ssf.gamemodel.BlockWindowEvent;
import ca.site3.ssf.gamemodel.FireEmitterChangedEvent;
import ca.site3.ssf.gamemodel.GameState.GameStateType;
import ca.site3.ssf.gamemodel.GameInfoRefreshEvent;
import ca.site3.ssf.gamemodel.GameStateChangedEvent;
import ca.site3.ssf.gamemodel.IGameModel;
import ca.site3.ssf.gamemodel.IGameModelEvent;
import ca.site3.ssf.gamemodel.IGameModelEvent.Type;
import ca.site3.ssf.gamemodel.GameState;
import ca.site3.ssf.gamemodel.IGameModelListener;
import ca.site3.ssf.gamemodel.PlayerActionPointsChangedEvent;
import ca.site3.ssf.gamemodel.PlayerBlockActionEvent;
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
	
	private GameState.GameStateType currGameState;
	
	/**
	 * @param commQueue for events of interest to non-GUI game hardware
	 * @param serialOutputStream for 
	 */
	public GameEventRouter(StreetFireServer server, SerialCommunicator serialComm) {
		this.server = server;
		this.serialComm = serialComm;
		this.currGameState = GameState.GameStateType.NO_STATE;
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
			
			case BLOCK_WINDOW:
				serialComm.onBlockWindow((BlockWindowEvent)event);
				break;
				
			case ROUND_PLAY_TIMER_CHANGED:				
				serialComm.onTimerChanged((RoundPlayTimerChangedEvent) event, this.currGameState);
				break;
				
			case ROUND_BEGIN_TIMER_CHANGED: {
				RoundBeginTimerChangedEvent e = (RoundBeginTimerChangedEvent) event;
				switch (e.getThreeTwoOneFightTime()) {
				case THREE:
					serialComm.onTimerChanged(3, this.currGameState, Color.YELLOW);
					break;
				case TWO:
					serialComm.onTimerChanged(2, this.currGameState, new Color(255, 100, 0));
					break;
				case ONE:
					serialComm.onTimerChanged(1, this.currGameState, Color.RED);
					break;
				case FIGHT:
				default:
					break;
				}
				break;
			}
			
			case GAME_STATE_CHANGED: {
				
				GameStateChangedEvent e = (GameStateChangedEvent)event;
				this.currGameState = e.getNewState();
				
				if (e.getNewState() == GameStateType.IDLE_STATE ||
					e.getNewState() == GameStateType.NO_STATE) {
					
					serialComm.setGlowfliesOn(false);
					serialComm.onPlayerHealthChanged(new PlayerHealthChangedEvent(1, 0, 0));
					serialComm.onPlayerHealthChanged(new PlayerHealthChangedEvent(2, 0, 0));
					serialComm.onTimerChanged(99, this.currGameState, Color.BLACK);
				}
				else {
					serialComm.setGlowfliesOn(true);
				}
				
				break;
			}
			
			case GAME_INFO_REFRESH: {
				
				GameInfoRefreshEvent e = (GameInfoRefreshEvent)event;
				this.currGameState = e.getCurrentGameState();
				if (e.getCurrentGameState() == GameStateType.IDLE_STATE ||
					e.getCurrentGameState() == GameStateType.NO_STATE) {
					
					serialComm.setGlowfliesOn(false);
					serialComm.onPlayerHealthChanged(new PlayerHealthChangedEvent(1, 0, 0));
					serialComm.onPlayerHealthChanged(new PlayerHealthChangedEvent(2, 0, 0));
					serialComm.onTimerChanged(99, this.currGameState, Color.BLACK);
				}
				else {
					serialComm.setGlowfliesOn(true);
				}
				break;
			}
			
			default:
				break;
		}
	}
}
