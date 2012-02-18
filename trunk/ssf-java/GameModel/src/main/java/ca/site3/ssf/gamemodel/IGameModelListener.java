package ca.site3.ssf.gamemodel;

/**
 * If you want to be notified of interesting things that happen during the game,
 * register an IGameModelListener with an {@link IGameModel}.
 * 
 * @author Callum
 * @author Greg
 */
public interface IGameModelListener {
	
	public enum RoundBeginCountdownType { THREE, TWO, ONE, FIGHT };
	public enum GameResult { PLAYER1_VICTORY, PLAYER2_VICTORY, TIE };
	
	/**
	 * Event method, called whenever the game state changes.
	 * @param oldState The previous game state that was set before the change.
	 * @param newState The newly set game state after the change.
	 */
	void onGameStateChanged(GameState.GameStateType oldState, GameState.GameStateType newState);
	
	/**
	 * Event method, called whenever a player's health amount changes.
	 * @param playerNum The player whose health amount changed.
	 * @param prevLifePercentage The previous health amount of the player, before the change.
	 * @param newLifePercentage The new health amount of the player, after the change.
	 */
	void onPlayerHealthChanged(int playerNum, float prevLifePercentage, float newLifePercentage);
	
	/**
	 * Event method, called each change in tick of the round in-play game state.
	 * @param newCountdownTimeInSecs The current count down time of the current game round. The
	 * timer will start at a large time value and count down to zero over the course of a round.
	 * NOTE: These events will stop if the round stops due to a player winning/losing or an abrupt
	 * change in state.
	 */
	void onRoundPlayTimerChanged(int newCountdownTimeInSecs);
	
	/**
	 * Event method, called when the round begin timer is counting down (i.e., "3, 2, 1, FIGHT!").
	 * For each state in the count down this method will be called. This method
	 * should be expected to be called four times at the beginning of each round.
	 * @param threeTwoOneFightTime The current count down value.
	 */
	void onRoundBeginFightTimerChanged(RoundBeginCountdownType threeTwoOneFightTime);
	
	/**
	 * Event method, called when a game round ends - this is for all rounds including tie breaker rounds.
	 * @param roundResult The result of the round.
	 * @param roundTimedOut Whether the round timed out or not, in the case of a tie breaker round, this
	 * will always be true.
	 */
	void onRoundEnded(GameResult roundResult, boolean roundTimedOut);
	
	/**
	 * Event method, called when the game match ends.
	 * @param matchResult The result of the match.
	 */
	void onMatchEnded(GameResult matchResult);
	
	/**
	 * Event method, called when a player attack action is executed in the game.
	 * @param playerNum The number of the player who is attacking.
	 * @param attackType The type of attack that was executed.
	 */
	void onPlayerAttackAction(int playerNum, PlayerAttackAction.AttackType attackType);
	
	/**
	 * Event method, called when a player block action is executed in the game.
	 * @param playerNum The number of the player who is blocking.
	 */
	void onPlayerBlockAction(int playerNum);
	
	/**
	 * Event method, called when the ringmaster executes an action.
	 */
	void onRingmasterAction();
	
	/**
	 * Event method, called whenever a fire emitter changes.
	 * @param fireEmitter Data describing the fire emitter that changed.
	 */
	void onFireEmitterChanged(ImmutableFireEmitter fireEmitter);
	
	
}
