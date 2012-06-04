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
			PlaybackHandler.playAudioFile(resourcePath + configFile.getProperty("AttackType.CustomUndefinedAttack"), false);
		}
		else if (event.getAttackType() == PlayerAttackAction.AttackType.LEFT_HOOK_ATTACK)
		{
			PlaybackHandler.playAudioFile(resourcePath + configFile.getProperty("AttackType.LeftHookAttack"), false);
		}
		else if (event.getAttackType() == PlayerAttackAction.AttackType.LEFT_JAB_ATTACK)
		{
			PlaybackHandler.playAudioFile(resourcePath + configFile.getProperty("AttackType.LeftJabAttack"), false);
		}
		else if (event.getAttackType() == PlayerAttackAction.AttackType.LEFT_UPPERCUT_ATTACK)
		{
			PlaybackHandler.playAudioFile(resourcePath + configFile.getProperty("AttackType.LeftUppercutAttack"), false);
		}
		else if (event.getAttackType() == PlayerAttackAction.AttackType.LEFT_CHOP_ATTACK)
		{
			PlaybackHandler.playAudioFile(resourcePath + configFile.getProperty("AttackType.LeftChopAttack"), false);
		}
		else if (event.getAttackType() == PlayerAttackAction.AttackType.RIGHT_HOOK_ATTACK)
		{
			PlaybackHandler.playAudioFile(resourcePath + configFile.getProperty("AttackType.RightHookAttack"), false);
		}
		else if (event.getAttackType() == PlayerAttackAction.AttackType.RIGHT_JAB_ATTACK)
		{
			PlaybackHandler.playAudioFile(resourcePath + configFile.getProperty("AttackType.RightJabAttack"), false);
		}
		else if (event.getAttackType() == PlayerAttackAction.AttackType.RIGHT_UPPERCUT_ATTACK)
		{
			PlaybackHandler.playAudioFile(resourcePath + configFile.getProperty("AttackType.RightUppercutAttack"), false);
		}
		else if (event.getAttackType() == PlayerAttackAction.AttackType.RIGHT_CHOP_ATTACK)
		{
			PlaybackHandler.playAudioFile(resourcePath + configFile.getProperty("AttackType.RightChopAttack"), false);
		}
		else if (event.getAttackType() == PlayerAttackAction.AttackType.SONIC_BOOM_ATTACK)
		{
			PlaybackHandler.playAudioFile(resourcePath + configFile.getProperty("AttackType.SonicBoomAttack"), false);
		}
		else if (event.getAttackType() == PlayerAttackAction.AttackType.SHORYUKEN_ATTACK)
		{
			PlaybackHandler.playAudioFile(resourcePath + configFile.getProperty("AttackType.ShoryukenAttack"), false);
		}
		else if (event.getAttackType() == PlayerAttackAction.AttackType.HADOUKEN_ATTACK)
		{
			PlaybackHandler.playAudioFile(resourcePath + configFile.getProperty("AttackType.HadokenAttack"), false);
		}
		else if (event.getAttackType() == PlayerAttackAction.AttackType.DOUBLE_LARIAT_ATTACK)
		{
			PlaybackHandler.playAudioFile(resourcePath + configFile.getProperty("AttackType.DoubleLariatAttack"), false);
		}
		else if (event.getAttackType() == PlayerAttackAction.AttackType.SUMO_HEADBUTT_ATTACK)
		{
			PlaybackHandler.playAudioFile(resourcePath + configFile.getProperty("AttackType.SumoHeadbuttAttack"), false);
		}
		else if (event.getAttackType() == PlayerAttackAction.AttackType.ONE_HUNDRED_HAND_SLAP_ATTACK)
		{
			PlaybackHandler.playAudioFile(resourcePath + configFile.getProperty("AttackType.OneHundredHandSlapAttack"), false);
		}
		else if (event.getAttackType() == PlayerAttackAction.AttackType.PSYCHO_CRUSHER_ATTACK)
		{
			PlaybackHandler.playAudioFile(resourcePath + configFile.getProperty("AttackType.PsychoCrusherAttack"), false);
		}
		else if (event.getAttackType() == PlayerAttackAction.AttackType.YMCA_ATTACK)
		{
			PlaybackHandler.playAudioFile(resourcePath + configFile.getProperty("AttackType.YmcaAttack"), false);
		}
		else if (event.getAttackType() == PlayerAttackAction.AttackType.NYAN_CAT_ATTACK)
		{
			PlaybackHandler.playAudioFile(resourcePath + configFile.getProperty("AttackType.NyanCatAttack"), false);
		}
	}
}
