package ca.site3.ssf.Sound;

import ca.site3.ssf.gamemodel.PlayerAttackAction;
import ca.site3.ssf.gamemodel.PlayerAttackActionEvent;
import ca.site3.ssf.gamemodel.GameStateChangedEvent;
import ca.site3.ssf.gamemodel.GameState.GameStateType;

import paulscode.sound.SoundSystem;

public class SoundPlayerControllerTester{

	static SoundPlayerController soundPlayerController;

    SoundSystem mySoundSystem;
	
	/** Plays audio from given file names. */
	// Tests the sound system by initiating a round start action, a hadouken attack and a nyan-cat attack (which pauses and resumes the background theme)
	public static void main(String[] args) 
	{
		new SoundPlayerControllerTester();
	} 
	
	public SoundPlayerControllerTester()
	{
		System.out.println("Started the SoundPlayerControllerTester.");
		
		init();
	}
	
	public void init()
	{
		soundPlayerController = new SoundPlayerController(new AudioSettings(5.0f, 0.33f));
		
    	// Create some sample actions
		GameStateChangedEvent gameStateChangedEvent = new GameStateChangedEvent(GameStateType.IDLE_STATE, GameStateType.ROUND_BEGINNING_STATE);
		PlayerAttackActionEvent player1AttackActionEvent = new PlayerAttackActionEvent(1, PlayerAttackAction.AttackType.HADOUKEN_ATTACK);
		PlayerAttackActionEvent player2AttackActionEvent = new PlayerAttackActionEvent(2, PlayerAttackAction.AttackType.NYAN_CAT_ATTACK);
		
		soundPlayerController.onGameModelEvent(gameStateChangedEvent);
		soundPlayerController.onGameModelEvent(player1AttackActionEvent);
		soundPlayerController.onGameModelEvent(player2AttackActionEvent);
		
		//new Thread(soundPlayerController, "Sound Player Controller").start();
	}
}
