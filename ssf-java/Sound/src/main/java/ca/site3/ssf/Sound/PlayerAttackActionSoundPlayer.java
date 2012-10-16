package ca.site3.ssf.Sound;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import ca.site3.ssf.gamemodel.IGameModelEvent;
import ca.site3.ssf.gamemodel.PlayerAttackAction.AttackType;
import ca.site3.ssf.gamemodel.PlayerAttackActionEvent;

class PlayerAttackActionSoundPlayer extends SoundPlayer {
	
	private Map<AttackType, PlaybackHandler> attackAudioMap =
			new HashMap<AttackType, PlaybackHandler>(AttackType.values().length);
	
	PlayerAttackActionSoundPlayer(SoundPlayerController controller) {
		super(controller);
		
		Properties configProperties = controller.getConfigProperties();
		String resourcePath = controller.getResourcePath();
		AudioSettings globalSettings = controller.getAudioSettings();
		
		String tempPath = "";
		tempPath = resourcePath + configProperties.getProperty("AttackType.CustomUndefinedAttack");
		attackAudioMap.put(AttackType.CUSTOM_UNDEFINED_ATTACK, PlaybackHandler.build(controller, tempPath,
			new PlaybackSettings(globalSettings.getVolume(), PlaybackSettings.BALANCED_PAN, 1)));

		tempPath = resourcePath + configProperties.getProperty("AttackType.LeftHookAttack");
		attackAudioMap.put(AttackType.LEFT_HOOK_ATTACK, PlaybackHandler.build(controller, tempPath,
				new PlaybackSettings(globalSettings.getVolume(), PlaybackSettings.BALANCED_PAN, 1)));
		
		tempPath = resourcePath + configProperties.getProperty("AttackType.LeftJabAttack");
		attackAudioMap.put(AttackType.LEFT_JAB_ATTACK, PlaybackHandler.build(controller, tempPath,
				new PlaybackSettings(globalSettings.getVolume(), PlaybackSettings.BALANCED_PAN, 1)));
		
		tempPath = resourcePath + configProperties.getProperty("AttackType.LeftUppercutAttack");
		attackAudioMap.put(AttackType.LEFT_UPPERCUT_ATTACK, PlaybackHandler.build(controller, tempPath,
				new PlaybackSettings(globalSettings.getVolume(), PlaybackSettings.BALANCED_PAN, 1)));
		
		tempPath = resourcePath + configProperties.getProperty("AttackType.LeftChopAttack");
		attackAudioMap.put(AttackType.LEFT_CHOP_ATTACK, PlaybackHandler.build(controller, tempPath,
				new PlaybackSettings(globalSettings.getVolume(), PlaybackSettings.BALANCED_PAN, 1)));
		
		tempPath = resourcePath + configProperties.getProperty("AttackType.RightHookAttack");
		attackAudioMap.put(AttackType.RIGHT_HOOK_ATTACK, PlaybackHandler.build(controller, tempPath,
				new PlaybackSettings(globalSettings.getVolume(), PlaybackSettings.BALANCED_PAN, 1)));
		
		tempPath = resourcePath + configProperties.getProperty("AttackType.RightJabAttack");
		attackAudioMap.put(AttackType.RIGHT_JAB_ATTACK, PlaybackHandler.build(controller, tempPath,
				new PlaybackSettings(globalSettings.getVolume(), PlaybackSettings.BALANCED_PAN, 1)));
		
		tempPath = resourcePath + configProperties.getProperty("AttackType.RightUppercutAttack");
		attackAudioMap.put(AttackType.RIGHT_UPPERCUT_ATTACK, PlaybackHandler.build(controller, tempPath,
				new PlaybackSettings(globalSettings.getVolume(), PlaybackSettings.BALANCED_PAN, 1)));
		
		tempPath = resourcePath + configProperties.getProperty("AttackType.RightChopAttack");
		attackAudioMap.put(AttackType.RIGHT_CHOP_ATTACK, PlaybackHandler.build(controller, tempPath,
				new PlaybackSettings(globalSettings.getVolume(), PlaybackSettings.BALANCED_PAN, 1)));
		
		tempPath = resourcePath + configProperties.getProperty("AttackType.SonicBoomAttack");
		attackAudioMap.put(AttackType.SONIC_BOOM_ATTACK, PlaybackHandler.build(controller, tempPath,
				new PlaybackSettings(globalSettings.getVolume(), PlaybackSettings.BALANCED_PAN, 1)));
		
		tempPath = resourcePath + configProperties.getProperty("AttackType.ShoryukenAttack");
		PlaybackHandler shoryukenPlayback = PlaybackHandler.build(controller, tempPath,
				new PlaybackSettings(globalSettings.getVolume(), PlaybackSettings.BALANCED_PAN, 1));
		attackAudioMap.put(AttackType.LEFT_SHORYUKEN_ATTACK, shoryukenPlayback);
		attackAudioMap.put(AttackType.RIGHT_SHORYUKEN_ATTACK, shoryukenPlayback);
		
		tempPath = resourcePath + configProperties.getProperty("AttackType.HadoukenAttack");
		attackAudioMap.put(AttackType.HADOUKEN_ATTACK, PlaybackHandler.build(controller, tempPath,
				new PlaybackSettings(globalSettings.getVolume(), PlaybackSettings.BALANCED_PAN, 1)));
		
		tempPath = resourcePath + configProperties.getProperty("AttackType.DoubleLariatAttack");
		attackAudioMap.put(AttackType.DOUBLE_LARIAT_ATTACK, PlaybackHandler.build(controller, tempPath,
				new PlaybackSettings(globalSettings.getVolume(), PlaybackSettings.BALANCED_PAN, 1)));
		
		tempPath = resourcePath + configProperties.getProperty("AttackType.QuadrupleLariatAttack");
		attackAudioMap.put(AttackType.QUADRUPLE_LARIAT_ATTACK, PlaybackHandler.build(controller, tempPath,
				new PlaybackSettings(globalSettings.getVolume(), PlaybackSettings.BALANCED_PAN, 1)));
		
		tempPath = resourcePath + configProperties.getProperty("AttackType.SumoHeadbuttAttack");
		attackAudioMap.put(AttackType.SUMO_HEADBUTT_ATTACK, PlaybackHandler.build(controller, tempPath,
				new PlaybackSettings(globalSettings.getVolume(), PlaybackSettings.BALANCED_PAN, 1)));
		
		tempPath = resourcePath + configProperties.getProperty("AttackType.TwoHandedOneHundredHandSlapAttack");
		attackAudioMap.put(AttackType.TWO_HANDED_ONE_HUNDRED_HAND_SLAP_ATTACK, PlaybackHandler.build(controller, tempPath,
				new PlaybackSettings(globalSettings.getVolume(), PlaybackSettings.BALANCED_PAN, 1)));
		
		tempPath = resourcePath + configProperties.getProperty("AttackType.OneHandedOneHundredHandSlapAttack");
		PlaybackHandler oneHandedOneHundredHandSlap = PlaybackHandler.build(controller, tempPath,
				new PlaybackSettings(globalSettings.getVolume(), PlaybackSettings.BALANCED_PAN, 1));
		attackAudioMap.put(AttackType.LEFT_ONE_HUNDRED_HAND_SLAP_ATTACK, oneHandedOneHundredHandSlap);
		attackAudioMap.put(AttackType.RIGHT_ONE_HUNDRED_HAND_SLAP_ATTACK, oneHandedOneHundredHandSlap);
		
		tempPath = resourcePath + configProperties.getProperty("AttackType.PsychoCrusherAttack");
		attackAudioMap.put(AttackType.PSYCHO_CRUSHER_ATTACK, PlaybackHandler.build(controller, tempPath,
				new PlaybackSettings(globalSettings.getVolume(), PlaybackSettings.BALANCED_PAN, 1)));
		
		tempPath = resourcePath + configProperties.getProperty("AttackType.YmcaAttack");
		attackAudioMap.put(AttackType.YMCA_ATTACK, PlaybackHandler.build(controller, tempPath,
				new PlaybackSettings(globalSettings.getVolume(), PlaybackSettings.BALANCED_PAN, 1)));
		
		tempPath = resourcePath + configProperties.getProperty("AttackType.NyanCatAttack");
		attackAudioMap.put(AttackType.NYAN_CAT_ATTACK, PlaybackHandler.build(controller, tempPath,
				new PlaybackSettings(globalSettings.getVolume(), PlaybackSettings.BALANCED_PAN, 1)));
		
		tempPath = resourcePath + configProperties.getProperty("AttackType.ArmWindmillAttack");
		attackAudioMap.put(AttackType.ARM_WINDMILL_ATTACK, PlaybackHandler.build(controller, tempPath,
				new PlaybackSettings(globalSettings.getVolume(), PlaybackSettings.BALANCED_PAN, 1)));
		
		tempPath = resourcePath + configProperties.getProperty("AttackType.DiscoStuAttack");
		attackAudioMap.put(AttackType.DISCO_STU_ATTACK, PlaybackHandler.build(controller, tempPath,
				new PlaybackSettings(globalSettings.getVolume(), PlaybackSettings.BALANCED_PAN, 1)));
		
		tempPath = resourcePath + configProperties.getProperty("AttackType.SuckItAttack");
		attackAudioMap.put(AttackType.SUCK_IT_ATTACK, PlaybackHandler.build(controller, tempPath,
				new PlaybackSettings(globalSettings.getVolume(), PlaybackSettings.BALANCED_PAN, 1)));
		
		tempPath = resourcePath + configProperties.getProperty("AttackType.VafanapoliAttack");
		PlaybackHandler vafanapoliHandler = PlaybackHandler.build(controller, tempPath,
				new PlaybackSettings(globalSettings.getVolume(), PlaybackSettings.BALANCED_PAN, 1));
		attackAudioMap.put(AttackType.LEFT_VAFANAPOLI_ATTACK, vafanapoliHandler);
		attackAudioMap.put(AttackType.RIGHT_VAFANAPOLI_ATTACK, vafanapoliHandler);
	}
	
	public PlaybackHandler getAudioPlaybackHandler(IGameModelEvent gameModelEvent) {
		if (gameModelEvent == null || gameModelEvent.getType() != IGameModelEvent.Type.PLAYER_ATTACK_ACTION) {
			return null;
		}
		
		PlayerAttackActionEvent event = (PlayerAttackActionEvent)gameModelEvent;
		return this.attackAudioMap.get(event.getAttackType());
	}
	
	public boolean isBackgroundSoundPlayer(IGameModelEvent gameModelEvent) {
		return false;
	}
	
	public PlaybackSettings getPlaybackSettings(AudioSettings globalSettings, IGameModelEvent gameModelEvent) {
		assert(globalSettings != null);
		if (gameModelEvent == null || gameModelEvent.getType() != IGameModelEvent.Type.PLAYER_ATTACK_ACTION) {
			return null;
		}
		PlayerAttackActionEvent event = (PlayerAttackActionEvent)gameModelEvent;
		return new PlaybackSettings(globalSettings.getVolume(), PlaybackSettings.getPlayerPan(event.getPlayerNum()), 1);
	}
}
