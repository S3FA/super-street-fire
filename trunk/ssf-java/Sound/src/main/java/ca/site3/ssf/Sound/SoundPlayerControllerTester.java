package ca.site3.ssf.Sound;

import ca.site3.ssf.gamemodel.GameState.GameStateType;
import ca.site3.ssf.gamemodel.GameStateChangedEvent;
import ca.site3.ssf.gamemodel.PlayerAttackAction;
import ca.site3.ssf.gamemodel.PlayerAttackActionEvent;

public class SoundPlayerControllerTester {

	static SoundPlayerController soundPlayerController;
	
	/** Plays audio from given file names. */
	// A test class that tests a hadouken attack and a game state changed event which causes a looping track (which is then explicitly stopped)
	public static void main(String[] args) {
		
		System.out.println("Started the SoundPlayerControllerTester.");
		PlayerAttackActionEvent player1AttackActionEvent = new PlayerAttackActionEvent(1, PlayerAttackAction.AttackType.HADOUKEN_ATTACK);
		PlayerAttackActionEvent player2AttackActionEvent = new PlayerAttackActionEvent(2, PlayerAttackAction.AttackType.HADOUKEN_ATTACK);
		//GameStateChangedEvent gameStateChangedEvent = new GameStateChangedEvent(GameStateType.IDLE_STATE, GameStateType.ROUND_BEGINNING_STATE);
		
		soundPlayerController = new SoundPlayerController(new AudioSettings(5.0f));
		
		try {
			
			soundPlayerController.onGameModelEvent(player1AttackActionEvent);
			Thread.sleep(2000);
			//soundPlayerController.onGameModelEvent(gameStateChangedEvent);
			//Thread.sleep(2000);
			soundPlayerController.onGameModelEvent(player2AttackActionEvent);
			Thread.sleep(2000);
			
			soundPlayerController.stopAllSounds();

		} 
		catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	} 

}
