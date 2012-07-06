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
	
	public final static int PLAYER_1_NUM = 1;
	public final static int PLAYER_2_NUM = 2;
	
	public enum Entity {
		PLAYER1_ENTITY("Player 1", true, PLAYER_1_NUM),
		PLAYER2_ENTITY("Player 2", true, PLAYER_2_NUM),
		RINGMASTER_ENTITY("Ringmaster", false, -1);
		
		private final String name;
		private final boolean isPlayer;
		private final int playerNum;
		
		Entity(String name, boolean isPlayer, int playerNum) {
			this.name = name;
			this.isPlayer = isPlayer;
			this.playerNum = playerNum;
		}
	
		public boolean getIsPlayer() {
			return this.isPlayer;
		}
		public int getPlayerNum() {
			return this.playerNum;
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
