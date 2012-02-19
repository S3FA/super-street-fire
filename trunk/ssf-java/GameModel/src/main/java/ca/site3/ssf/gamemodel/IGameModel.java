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
	
	public enum Entity { PLAYER1_ENTITY, PLAYER2_ENTITY, RINGMASTER_ENTITY };
	
	/**
	 * Tick the game model.
	 * @param dT time since last tick (in seconds)
	 */
	void tick(double dT);
	
	
	void killGame();
	void initiateNextState();
	void togglePauseGame();

	ActionFactory getActionFactory();
	void executeGenericAction(Action action);

	void addGameModelListener(IGameModelListener l);
	void removeGameModelListener(IGameModelListener l);
}
