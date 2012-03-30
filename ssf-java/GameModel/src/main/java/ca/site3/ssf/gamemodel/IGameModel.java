package ca.site3.ssf.gamemodel;

import java.util.EnumSet;

/**
 * Interface representing the game model for Super Street Fire.
 * This is the only interface that the gamemodel package offers to other
 * packages in order to drive its activity.
 * 
 * @author Callum
 * @author Greg
 */
public interface IGameModel {
	
	public enum Entity {
		PLAYER1_ENTITY("Player 1"),
		PLAYER2_ENTITY("Player 2"),
		RINGMASTER_ENTITY("Ringmaster");
		
		private final String name;
		
		Entity(String name) {
			this.name = name;
		}
	
		public String toString() {
			return this.name;
		}
	};
	
	void tick(double dT);
	
	ActionFactory getActionFactory();
	void executeGenericAction(Action action);  // Or use the ExecuteGenericActionCommand class
	void killGame();						   // Or use the KillGameCommand class
	void initiateNextState();				   // Or use the InitiateNextStateCommand class
	void togglePauseGame();					   // Or use the TogglePauseGameCommand class

	void touchFireEmitter(FireEmitter.Location location, int index,
			float intensity, EnumSet<Entity> contributors);  // Or use the TouchFireEmitterCommand class
	
	void addGameModelListener(IGameModelListener l);
	void removeGameModelListener(IGameModelListener l);
	
	void executeCommand(AbstractGameModelCommand command);
	
	GameConfig getConfiguration();
}
