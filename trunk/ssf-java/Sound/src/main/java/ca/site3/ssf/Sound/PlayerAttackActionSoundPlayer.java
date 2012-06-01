package ca.site3.ssf.Sound;

import ca.site3.ssf.gamemodel.IGameModelEvent;
import ca.site3.ssf.gamemodel.PlayerAttackAction;
import ca.site3.ssf.gamemodel.PlayerAttackActionEvent;

public class PlayerAttackActionSoundPlayer extends SoundPlayerController implements ISoundPlayer {
	
	// Handle the sounds based on player attacks
	public void playSounds(IGameModelEvent gameModelEvent)
	{
		PlayerAttackActionEvent event = (PlayerAttackActionEvent)gameModelEvent;
		if (event.getAttackType() == PlayerAttackAction.AttackType.CUSTOM_UNDEFINED_ATTACK)
		{
			PlaybackHandler.playAudioFile(resourcePath + configFile.getProperty("AttackType.CustomUndefinedAttack"));
		}
		else if (event.getAttackType() == PlayerAttackAction.AttackType.HADOUKEN_ATTACK)
		{
			PlaybackHandler.playAudioFile(resourcePath + configFile.getProperty("AttackType.HadokenAttack"));
		}
		else if (event.getAttackType() == PlayerAttackAction.AttackType.LEFT_HOOK_ATTACK)
		{
			PlaybackHandler.playAudioFile(resourcePath + configFile.getProperty("AttackType.LeftHookAttack"));
		}
		else if (event.getAttackType() == PlayerAttackAction.AttackType.LEFT_JAB_ATTACK)
		{
			PlaybackHandler.playAudioFile(resourcePath + configFile.getProperty("AttackType.LeftJabAttack"));
		}
		else if (event.getAttackType() == PlayerAttackAction.AttackType.LEFT_UPPERCUT_ATTACK)
		{
			PlaybackHandler.playAudioFile(resourcePath + configFile.getProperty("AttackType.LeftUppercutAttack"));
		}
		else if (event.getAttackType() == PlayerAttackAction.AttackType.RIGHT_HOOK_ATTACK)
		{
			PlaybackHandler.playAudioFile(resourcePath + configFile.getProperty("AttackType.RightHookAttack"));
		}
		else if (event.getAttackType() == PlayerAttackAction.AttackType.RIGHT_JAB_ATTACK)
		{
			PlaybackHandler.playAudioFile(resourcePath + configFile.getProperty("AttackType.RightJabAttack"));
		}
		else if (event.getAttackType() == PlayerAttackAction.AttackType.RIGHT_UPPERCUT_ATTACK)
		{
			PlaybackHandler.playAudioFile(resourcePath + configFile.getProperty("AttackType.RightUppercutAttack"));
		}
		else if (event.getAttackType() == PlayerAttackAction.AttackType.SONIC_BOOM_ATTACK)
		{
			PlaybackHandler.playAudioFile(resourcePath + configFile.getProperty("AttackType.SonicBoomAttack"));
		}
	}
}
