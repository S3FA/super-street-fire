package ca.site3.ssf.ioserver;

import java.util.concurrent.BlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.site3.ssf.gamemodel.IGameModel;
import ca.site3.ssf.gamemodel.IGameModelEvent;
import ca.site3.ssf.gamemodel.IGameModelListener;


/**
 * Handles notifications coming from the {@link IGameModel} by wrapping them in a IGameModelEvent
 * and shoving them onto the appropriate queue to be consumed by other thread(s). 
 * 
 * @author greg
 */
public class GameEventRouter implements IGameModelListener {

	private Logger log = LoggerFactory.getLogger(getClass());
	
	private BlockingQueue<IGameModelEvent> commQueue;
	private BlockingQueue<IGameModelEvent> guiQueue;
	
	
	/**
	 * @param commQueue for events of interest to non-GUI game hardware
	 * @param guiQueue for events that should be passed along to the GUI
	 */
	public GameEventRouter(BlockingQueue<IGameModelEvent> commQueue, BlockingQueue<IGameModelEvent> guiQueue) {
		this.commQueue = commQueue;
		this.guiQueue = guiQueue;
	}


	public void onGameModelEvent(IGameModelEvent event) {
		switch (event.getType()) {
		case FireEmitterChanged:
			break;
		case GameStateChanged:
			break;
		case MatchEnded:
			break;
		case PlayerAttackAction:
			break;
		case PlayerBlockAction:
			break;
		case PlayerHealthChanged:
			break;
		case RingmasterAction:
			break;
		case RoundBeginTimerChanged:
			break;
		case RoundEnded:
			break;
		case RoundPlayTimerChanged:
			break;
		default:
			log.warn("Unhandled GameModel event type: "+event.getType());
			break;
		}
	}
	
}
