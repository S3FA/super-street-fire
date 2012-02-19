package ca.site3.ssf.ioserver;

import java.util.AbstractQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.site3.ssf.gamemodel.GameState.GameStateType;
import ca.site3.ssf.gamemodel.IGameModel;
import ca.site3.ssf.gamemodel.IGameModelListener;
import ca.site3.ssf.gamemodel.ImmutableFireEmitter;
import ca.site3.ssf.gamemodel.PlayerAttackAction.AttackType;


/**
 * Handles notifications coming from the {@link IGameModel} by wrapping them in a GameEvent
 * and shoving them onto the appropriate queue to be consumed by other thread(s). 
 * 
 * @author greg
 */
public class GameEventRouter implements IGameModelListener {

	private Logger log = LoggerFactory.getLogger(getClass());
	
	private AbstractQueue<GameEvent> commQueue;
	private AbstractQueue<GameEvent> guiQueue;
	
	
	/**
	 * @param commQueue for events of interest to non-GUI game hardware
	 * @param guiQueue for events that should be passed along to the GUI
	 */
	public GameEventRouter(AbstractQueue<GameEvent> commQueue, AbstractQueue<GameEvent> guiQueue) {
		this.commQueue = commQueue;
		this.guiQueue = guiQueue;
	}
	
	
	public void onGameStateChanged(GameStateType oldState, GameStateType newState) {
		
	}

	public void onPlayerHealthChanged(int playerNum, float prevLifePercentage, float newLifePercentage) {
		
	}

	public void onRoundPlayTimerChanged(int newCountdownTimeInSecs) {
		
	}

	public void onRoundBeginFightTimerChanged(RoundBeginCountdownType threeTwoOneFightTime) {
		
	}

	public void onRoundEnded(int roundNumber, GameResult roundResult, boolean roundTimedOut) {
		
	}

	public void onMatchEnded(GameResult matchResult) {
		
	}

	public void onPlayerAttackAction(int playerNum, AttackType attackType) {
		
	}

	public void onPlayerBlockAction(int playerNum) {
		
	}

	public void onRingmasterAction() {
		
	}

	public void onFireEmitterChanged(ImmutableFireEmitter fireEmitter) {
		
	}
}
