package ca.site3.ssf.gamemodel;

/**
 * Interface representing the game state
 * 
 * @author callum
 * @author greg
 */
public interface IGameModel {

	public static final int PLAYER_1 = 1;
	public static final int PLAYER_2 = 2;
	
	
	
	/**
	 * @return configuration for the game
	 */
	GameConfig getGameConfig();
	
	
	/**
	 * Set the parameters the game should use 
	 * @param config
	 */
	void setGameConfig(GameConfig config);
	
	void tick(double dT);
	
	void initiateNextMatchRound();
	
	void executePlayerMove(int playerNum, Move moveType);
	
	boolean togglePauseGame();
	
	void killGame();
	
	
	void addListener(IGameModelListener l);
	
	void removeListener(IGameModelListener l);
}
