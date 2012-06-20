package ca.site3.ssf.Sound;

import java.util.Properties;

import ca.site3.ssf.gamemodel.IGameModelEvent;
import ca.site3.ssf.gamemodel.PlayerAttackActionEvent;

class PlayerAttackActionSoundPlayer extends SoundPlayer {
	
	PlayerAttackActionSoundPlayer(String resourcePath, Properties configFile) {
		super(resourcePath, configFile);
	}
	
	public PlaybackSettings getPlaybackSettings(AudioSettings globalSettings, IGameModelEvent gameModelEvent) {
		assert(globalSettings != null);
		if (gameModelEvent == null || gameModelEvent.getType() != IGameModelEvent.Type.PLAYER_ATTACK_ACTION) {
			return null;
		}
		PlayerAttackActionEvent event = (PlayerAttackActionEvent)gameModelEvent;
		return new PlaybackSettings(globalSettings.getVolume(), PlaybackSettings.getPlayerPan(event.getPlayerNum()), 1);
	}
	
	/**
	 *  Handles the sounds for the various player attacks.
	 */
	public String getAudioResourcePath(IGameModelEvent gameModelEvent) {
		if (gameModelEvent == null || gameModelEvent.getType() != IGameModelEvent.Type.PLAYER_ATTACK_ACTION) {
			return "";
		}
		
		PlayerAttackActionEvent event = (PlayerAttackActionEvent)gameModelEvent;
		String audioFilepath = "";
		
		switch (event.getAttackType()) {
		
		case CUSTOM_UNDEFINED_ATTACK: {
			audioFilepath = this.resourcePath + this.configFile.getProperty("AttackType.CustomUndefinedAttack");
			break;
		}
		case LEFT_HOOK_ATTACK: {
			audioFilepath = this.resourcePath + this.configFile.getProperty("AttackType.LeftHookAttack");
			break;
		}
		case LEFT_JAB_ATTACK: {
			audioFilepath = this.resourcePath + this.configFile.getProperty("AttackType.LeftJabAttack");
			break;
		}
		case LEFT_UPPERCUT_ATTACK: {
			audioFilepath = this.resourcePath + this.configFile.getProperty("AttackType.LeftUppercutAttack");
			break;
		}
		case LEFT_CHOP_ATTACK: {
			audioFilepath = this.resourcePath + this.configFile.getProperty("AttackType.LeftChopAttack");
			break;
		}
		case RIGHT_HOOK_ATTACK: {
			audioFilepath = this.resourcePath + this.configFile.getProperty("AttackType.RightHookAttack");
			break;
		}
		case RIGHT_JAB_ATTACK: {
			audioFilepath = this.resourcePath + this.configFile.getProperty("AttackType.RightJabAttack");
			break;
		}
		case RIGHT_UPPERCUT_ATTACK: {
			audioFilepath = this.resourcePath + this.configFile.getProperty("AttackType.RightUppercutAttack");
			break;
		}
		case RIGHT_CHOP_ATTACK: {
			audioFilepath = this.resourcePath + this.configFile.getProperty("AttackType.RightChopAttack");
			break;
		}
		case SONIC_BOOM_ATTACK: {
			audioFilepath = this.resourcePath + this.configFile.getProperty("AttackType.SonicBoomAttack");
			break;
		}
		case LEFT_SHORYUKEN_ATTACK:
		case RIGHT_SHORYUKEN_ATTACK: {
			audioFilepath = this.resourcePath + this.configFile.getProperty("AttackType.ShoryukenAttack");
			break;
		}
		case HADOUKEN_ATTACK: {
			audioFilepath = this.resourcePath + this.configFile.getProperty("AttackType.HadoukenAttack");
			break;
		}
		case DOUBLE_LARIAT_ATTACK: {
			audioFilepath = this.resourcePath + this.configFile.getProperty("AttackType.DoubleLariatAttack");
			break;
		}
		case QUADRUPLE_LARIAT_ATTACK: {
			audioFilepath = this.resourcePath + this.configFile.getProperty("AttackType.QuadrupleLariatAttack");
			break;
		}
		case SUMO_HEADBUTT_ATTACK: {
			audioFilepath = this.resourcePath + this.configFile.getProperty("AttackType.SumoHeadbuttAttack");
			break;
		}
		case LEFT_ONE_HUNDRED_HAND_SLAP_ATTACK:
		case RIGHT_ONE_HUNDRED_HAND_SLAP_ATTACK:
		case TWO_HANDED_ONE_HUNDRED_HAND_SLAP_ATTACK: {
			audioFilepath = this.resourcePath + this.configFile.getProperty("AttackType.OneHundredHandSlapAttack");
			break;
		}
		case PSYCHO_CRUSHER_ATTACK: {
			audioFilepath = this.resourcePath + this.configFile.getProperty("AttackType.PsychoCrusherAttack");
			break;
		}
		case YMCA_ATTACK: {
			audioFilepath = this.resourcePath + this.configFile.getProperty("AttackType.YmcaAttack");
			break;
		}
		case NYAN_CAT_ATTACK: {
			audioFilepath = this.resourcePath + this.configFile.getProperty("AttackType.NyanCatAttack");
			break;
		}
		case ARM_WINDMILL_ATTACK: {
			audioFilepath = this.resourcePath + this.configFile.getProperty("AttackType.ArmWindmillAttack");
			break;
		}
		case DISCO_STU_ATTACK: {
			audioFilepath = this.resourcePath + this.configFile.getProperty("AttackType.DiscoStuAttack");
			break;
		}
		case SUCK_IT_ATTACK: {
			audioFilepath = this.resourcePath + this.configFile.getProperty("AttackType.SuckItAttack");
			break;
		}
		case LEFT_VAFANAPOLI_ATTACK:
		case RIGHT_VAFANAPOLI_ATTACK: {
			audioFilepath = this.resourcePath + this.configFile.getProperty("AttackType.VafanapoliAttack");
			break;
		}
		
		default:
			assert(false);
			break;
		}
		
		return audioFilepath;
	}
	
	public boolean isBackgroundSoundPlayer(IGameModelEvent gameModelEvent) {
		return false;
	}
}
