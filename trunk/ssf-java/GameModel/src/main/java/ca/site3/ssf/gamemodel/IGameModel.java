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
		PLAYER1_ENTITY("Player 1", true),
		PLAYER2_ENTITY("Player 2", true),
		RINGMASTER_ENTITY("Ringmaster", false);
		
		private final String name;
		private final boolean isPlayer;
		
		Entity(String name, boolean isPlayer) {
			this.name = name;
			this.isPlayer = isPlayer;
		}
	
		public boolean getIsPlayer() {
			return this.isPlayer;
		}
		
		public String toString() {
			return this.name;
		}
	};
	
	void tick(double dT);
	
	ActionFactory getActionFactory();
	void queryGameInfoRefresh();			                       // Or use the QueryGameInfoRefreshCommand class
	void executeGenericAction(Action action);                      // Or use the ExecuteGenericActionCommand class
	void killGame();						                       // Or use the KillGameCommand class
	void initiateNextState(GameState.GameStateType nextState);	   // Or use the InitiateNextStateCommand class
	void togglePauseGame();					                       // Or use the TogglePauseGameCommand class
	void updatePlayerHeadsetData(int playerNum, HeadsetData data); // Or use the UpdateHeadsetDataCommand class
	void raiseUnrecognizedGestureEvent(Entity entity);             // Or use the RaiseUnrecognizedGestureEventCommand class
	
	void touchFireEmitter(FireEmitter.Location location, int index,
			float intensity, EnumSet<Entity> contributors);     // Or use the TouchFireEmitterCommand class
	
	void addGameModelListener(IGameModelListener l);
	void removeGameModelListener(IGameModelListener l);
	
	void executeCommand(AbstractGameModelCommand command);
	
	GameConfig getConfiguration();
}
