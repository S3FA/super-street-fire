package ca.site3.ssf.gamemodel;

/**
 * Interface representing the game model for Super Street Fire.
 * 
 * @author Callum
 * @author Greg
 */
public interface IGameModel {

	void tick(double dT);
	void kill();
	void initiateNextMatchRound();
	void togglePause();
	
	void addListener(IGameModelListener l);
	void removeListener(IGameModelListener l);
}
