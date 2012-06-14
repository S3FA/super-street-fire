package ca.site3.ssf.gamemodel;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.site3.ssf.gamemodel.MatchEndedEvent.MatchResult;
import ca.site3.ssf.gamemodel.RoundBeginTimerChangedEvent.RoundBeginCountdownType;
import ca.site3.ssf.gamemodel.RoundEndedEvent.RoundResult;

/**
 * Default implementation of GameModel.
 * 
 * @author Callum
 * @author Greg
 *
 */
public class GameModel implements IGameModel {

	private GameConfig config;
	
	private GameState currState = null;
	private GameState nextState = null;
	
	private Player player1 = null;
	private Player player2 = null;
	
	private FireEmitterModel fireEmitterModel = null;
	
	private GameModelActionSignaller actionSignaller = null;
	
	private Logger logger = null;
	
	private List<RoundResult> roundResults = new ArrayList<RoundResult>(4);
	
	public GameModel(GameConfig config) {
		this.logger = LoggerFactory.getLogger(getClass());
		
		this.config = config;
		assert(this.config != null);
		
		this.actionSignaller = new GameModelActionSignaller();
		
		this.player1 = new Player(1, this.actionSignaller, this.config);
		this.player2 = new Player(2, this.actionSignaller, this.config);
		
		this.fireEmitterModel = new FireEmitterModel(new FireEmitterConfig(true, 16, 8), this.actionSignaller);
		
		// Make sure the rest of the model is setup before the state
		this.nextState = new IdleGameState(this);
	}
	
	// Begin IGameModel Interface function implementations *******************************************
	
	public void tick(double dT) {
		
		// Check to see whether a new state has been set during the previous tick...
		if (this.nextState != null) {
			
			// There is a new/next state that we need to switch to, change to it
			GameState oldState = this.currState;
			this.currState = this.nextState;
			
			// The state has officially changed, fire an event...
			this.actionSignaller.fireOnGameStateChanged(oldState, this.nextState);
			
			// Clear the next state, we've now officially switched states
			this.nextState = null;
		}
	
		// Tick the current state to simulate the game...
		this.currState.tick(dT);
	}
	
	public void killGame() {
		this.logger.info("Request to kill the game was received, killing game to idle state.");
		
		this.nextState = null;
		this.currState.killToIdle();
	}

	public void initiateNextState(GameState.GameStateType nextState) {
		this.currState.initiateNextState(nextState);
	}

	public void togglePauseGame() {
		this.currState.togglePause();
	}
	
	public void updatePlayerHeadsetData(int playerNum, HeadsetData data) {
		// TODO: What do we do with the headset data? ... moves document needs to specify this.
		//this.currState.updatePlayerHeadsetData(playerNum, data);
	}
	
	public void touchFireEmitter(FireEmitter.Location location, int index,
								 float intensity, EnumSet<Entity> contributors) {
		
		// Make sure the game is in the ringmaster control state, otherwise
		// this ability should not be allowed!
		if (this.currState.getStateType() != GameState.GameStateType.RINGMASTER_STATE) {
			return;
		}
		
		final double TOTAL_EMITTER_ON_LENGTH_IN_SECS = 0.5;
		
		ActionFactory actionFactory = this.getActionFactory();
		for (Entity contributor : contributors) {
			Action newAction = actionFactory.buildCrowdPleaserTouchAction(
					contributor, location, index, TOTAL_EMITTER_ON_LENGTH_IN_SECS, 1);
			this.currState.executeAction(newAction);
		}	
	}
	
	public ActionFactory getActionFactory() {
		return new ActionFactory(this);
	}
	
	public void queryGameInfoRefresh() {
		if (this.currState == null) {
			return;
		}
		
		int roundInPlayTimer = -1;
		RoundBeginCountdownType roundBeginCountdown = RoundBeginCountdownType.THREE;
		boolean roundTimedOut   = false;
		MatchResult matchResult = MatchResult.PLAYER1_VICTORY;
		
		switch (this.currState.getStateType()) {
		
		case ROUND_BEGINNING_STATE:
			roundBeginCountdown = ((RoundBeginningGameState)this.currState).getCountState();
			break;
		case ROUND_IN_PLAY_STATE:
			roundInPlayTimer = ((RoundInPlayState)this.currState).getLastCountdownValueInSecs();
			break;
		case ROUND_ENDED_STATE:
			roundTimedOut = ((RoundEndedGameState)this.currState).getRoundTimedOut();
			break;
		case MATCH_ENDED_STATE:
			matchResult = ((MatchEndedGameState)this.currState).getMatchResult();
			break;
		default:
			break;
			
		}
		
		// Fire off the info refresh event based on the current game model's information...
		GameInfoRefreshEvent event = new GameInfoRefreshEvent(
				this.currState.getStateType(), this.roundResults, matchResult, 
				this.player1.getHealth(), this.player2.getHealth(),
				roundBeginCountdown, roundInPlayTimer, roundTimedOut);
		this.actionSignaller.fireOnQueryGameInfoRefresh(event);
		
		// Inform all listeners of the state of all the fire emitters
		this.fireEmitterModel.fireAllEmitterChangedEvent();
	}
	
	public void executeGenericAction(Action action) {
		if (action == null) {
			return;
		}
		this.currState.executeAction(action);
	}
	
	public void addGameModelListener(IGameModelListener l) {
		this.actionSignaller.addGameModelListener(l);
	}
	
	public void removeGameModelListener(IGameModelListener l) {
		this.actionSignaller.removeGameModelListener(l);
	}	
	
	public void executeCommand(AbstractGameModelCommand command) {
		assert(command != null);
		command.execute(this);
	}
	
	
	public GameConfig getConfiguration() {
		return config;
	}
	
	
	// End IGameModel Interface function implementations *******************************************
	
	/**
	 * Sets the next game state to the given state, the state will officially be
	 * updated on the next Tick of the GameModel.
	 * @param nextState The next state that the game will be changed to.
	 */
	void setNextGameState(GameState nextState) {
		assert(nextState != null);
		
		// Ignore the state change if we're just going to change to the same state
		if (nextState.getStateType() == this.currState.getStateType()) {
			return;
		}

		this.logger.info("Changing game state on next tick to " + nextState.getStateType().toString());
		this.nextState = nextState;
	}	
	
	/**
	 * Completely resets the game data and turns all emitters off.
	 */
	void resetGame() {
		// Make sure the game is completely reset:
		// - All emitters must be turned off
		// - All players must have full health restored and all record of wins/losses wiped
		this.getFireEmitterModel().resetAllEmitters();
		Player p1 = this.getPlayer1();
		Player p2 = this.getPlayer2();
		
		p1.matchReset();
		p2.matchReset();
		
		p1.clearHealth();
		p2.clearHealth();
		
		assert(this.roundResults.size() <= this.getConfig().getMaxNumRoundsPerMatch());
		this.roundResults.clear();
	}
	
	/**
	 * Add the result of a round to the list of round results held in this model.
	 * @param result The round result to add.
	 */
	void addRoundResult(RoundResult result) {
		this.roundResults.add(result);
		// It should never be the case that we add more results than there are maximum number of rounds
		assert(this.roundResults.size() <= this.getConfig().getMaxNumRoundsPerMatch());
	}
	
	/**
	 * Get the total number of rounds that have been played in the current match.
	 * @return The number of rounds played.
	 */
	int getNumRoundsPlayed() {
		return this.roundResults.size();
	}
	
	/**
	 * Get the round result for the given round number.
	 * @param roundNum The round number in [1, this.getConfig().getMaxNumRoundsPerMatch()].
	 * @return The result of the round corresponding to the given round number, null if invalid round number.
	 */
	RoundResult getRoundResult(int roundNum) {
		if (roundNum <= 0) {
			assert(false);
			return null;
		}
		
		int roundIndex = roundNum - 1;
		if (roundIndex >= this.roundResults.size()) {
			assert(false);
			return null;
		}
		
		return this.roundResults.get(roundIndex);
	}
	
	/**
	 * Get the player with the given player number.
	 * @param playerNum The number of the player must be either 1 or 2.
	 * @return The player object corresponding to the given player number, null on bad value.
	 */
	Player getPlayer(int playerNum) {
		
		switch (playerNum) {
			case 1:
				return this.getPlayer1();
			case 2:
				return this.getPlayer2();
			default:
				assert(false);
				break;
		}
		return null;
	}
	
	Player getPlayer(GameModel.Entity entity) {
		switch (entity) {
			case PLAYER1_ENTITY:
				return this.getPlayer1();
			case PLAYER2_ENTITY:
				return this.getPlayer2();
			default:
				assert(false);
				break;
		}
		return null;
	}
	
	Player getPlayer1() {
		return this.player1;
	}
	Player getPlayer2() {
		return this.player2;
	}
	
	GameConfig getConfig() {
		return this.config;
	}
	
	GameModelActionSignaller getActionSignaller() {
		return this.actionSignaller;
	}
	FireEmitterModel getFireEmitterModel() {
		return this.fireEmitterModel;
	}
	
}
