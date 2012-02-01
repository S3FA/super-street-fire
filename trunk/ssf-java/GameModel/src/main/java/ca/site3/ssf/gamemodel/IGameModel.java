package ca.site3.ssf.gamemodel;

/**
 * Interface representing the game model for Super Street Fire.
 * This is the only interface that the gamemodel package offers to other
 * packages in order to drive its activity.
 * 
 * @author Callum
 * @author Greg
 */
public interface IGameModel {

	void tick(double dT);
	void killGame();
	void initiateNextMatchRound();
	void togglePauseGame();
	
	//void executePlayerAction(Player.PlayerNumber playerNum, PlayerAction action);
	//void executeRingmasterAction(RingmasterAction action);
	
	void addGameModelListener(IGameModelListener l);
	void removeGameModelListener(IGameModelListener l);
}
