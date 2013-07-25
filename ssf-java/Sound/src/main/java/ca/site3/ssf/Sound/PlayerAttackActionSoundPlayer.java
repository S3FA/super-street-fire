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
	
	// A special playback handler that occurs one in ten times for nyan cat
	private PlaybackHandler nyanWaits = null;
	
	PlayerAttackActionSoundPlayer(SoundPlayerController controller) {
		super(controller);
		
		Properties configProperties = controller.getConfigProperties();
		AudioSettings globalSettings = controller.getAudioSettings();
		PlaybackSettings playbackSettings = getDefaultPlaybackSettings();
		
		attackAudioMap.put(AttackType.CUSTOM_UNDEFINED_ATTACK, PlaybackHandler.build(controller, configProperties.getProperty("AttackType.CustomUndefinedAttack"), playbackSettings));
		attackAudioMap.put(AttackType.LEFT_HOOK_ATTACK, PlaybackHandler.build(controller, configProperties.getProperty("AttackType.LeftHookAttack"), playbackSettings));
		attackAudioMap.put(AttackType.LEFT_JAB_ATTACK, PlaybackHandler.build(controller, configProperties.getProperty("AttackType.LeftJabAttack"), playbackSettings));
		attackAudioMap.put(AttackType.LEFT_UPPERCUT_ATTACK, PlaybackHandler.build(controller, configProperties.getProperty("AttackType.LeftUppercutAttack"), playbackSettings));
		attackAudioMap.put(AttackType.LEFT_CHOP_ATTACK, PlaybackHandler.build(controller, configProperties.getProperty("AttackType.LeftChopAttack"), playbackSettings));
		attackAudioMap.put(AttackType.RIGHT_HOOK_ATTACK, PlaybackHandler.build(controller, configProperties.getProperty("AttackType.RightHookAttack"), playbackSettings));
		attackAudioMap.put(AttackType.RIGHT_JAB_ATTACK, PlaybackHandler.build(controller, configProperties.getProperty("AttackType.RightJabAttack"), playbackSettings));
		attackAudioMap.put(AttackType.RIGHT_UPPERCUT_ATTACK, PlaybackHandler.build(controller, configProperties.getProperty("AttackType.RightUppercutAttack"), playbackSettings));
		attackAudioMap.put(AttackType.RIGHT_CHOP_ATTACK, PlaybackHandler.build(controller, configProperties.getProperty("AttackType.RightChopAttack"), playbackSettings));
		attackAudioMap.put(AttackType.SONIC_BOOM_ATTACK, PlaybackHandler.build(controller, configProperties.getProperty("AttackType.SonicBoomAttack"), playbackSettings));
		attackAudioMap.put(AttackType.HADOUKEN_ATTACK, PlaybackHandler.build(controller, configProperties.getProperty("AttackType.HadoukenAttack"),	playbackSettings));
		attackAudioMap.put(AttackType.DOUBLE_LARIAT_ATTACK, PlaybackHandler.build(controller, configProperties.getProperty("AttackType.DoubleLariatAttack"), playbackSettings));
		attackAudioMap.put(AttackType.SUMO_HEADBUTT_ATTACK, PlaybackHandler.build(controller, configProperties.getProperty("AttackType.SumoHeadbuttAttack"), playbackSettings));
		attackAudioMap.put(AttackType.TWO_HANDED_ONE_HUNDRED_HAND_SLAP_ATTACK, PlaybackHandler.build(controller, configProperties.getProperty("AttackType.TwoHandedOneHundredHandSlapAttack"), playbackSettings));
		attackAudioMap.put(AttackType.ARM_WINDMILL_ATTACK, PlaybackHandler.build(controller, configProperties.getProperty("AttackType.ArmWindmillAttack"), playbackSettings));
		attackAudioMap.put(AttackType.SUCK_IT_ATTACK, PlaybackHandler.build(controller, configProperties.getProperty("AttackType.SuckItAttack"), playbackSettings));
		attackAudioMap.put(AttackType.PSYCHO_CRUSHER_ATTACK, PlaybackHandler.build(controller, configProperties.getProperty("AttackType.PsychoCrusherAttack"), playbackSettings));

		PlaybackHandler shoryukenPlayback = PlaybackHandler.build(controller, configProperties.getProperty("AttackType.ShoryukenAttack"), playbackSettings);
		attackAudioMap.put(AttackType.LEFT_SHORYUKEN_ATTACK, shoryukenPlayback);
		attackAudioMap.put(AttackType.RIGHT_SHORYUKEN_ATTACK, shoryukenPlayback);
		
		PlaybackHandler oneHandedOneHundredHandSlap = PlaybackHandler.build(controller, configProperties.getProperty("AttackType.OneHandedOneHundredHandSlapAttack"), playbackSettings);
		attackAudioMap.put(AttackType.LEFT_ONE_HUNDRED_HAND_SLAP_ATTACK, oneHandedOneHundredHandSlap);
		attackAudioMap.put(AttackType.RIGHT_ONE_HUNDRED_HAND_SLAP_ATTACK, oneHandedOneHundredHandSlap);

		attackAudioMap.put(AttackType.YMCA_ATTACK, PlaybackHandler.build(controller, configProperties.getProperty("AttackType.YmcaAttack"), 
				new PlaybackSettings(globalSettings.getVolume(), false, true)));
		
		attackAudioMap.put(AttackType.NYAN_CAT_ATTACK, PlaybackHandler.build(controller, configProperties.getProperty("AttackType.NyanCatAttack"), 
				new PlaybackSettings(globalSettings.getVolume(), false, true)));
		
		nyanWaits = PlaybackHandler.build(controller, configProperties.getProperty("AttackType.NyanWaitsAttack"), new PlaybackSettings(globalSettings.getVolume(), false, true));
	}
	
	// Get the default playback settings for this sound player
	public PlaybackHandler getAudioPlaybackHandler(IGameModelEvent gameModelEvent) {
		
		if (gameModelEvent == null || gameModelEvent.getType() != IGameModelEvent.Type.PLAYER_ATTACK_ACTION) {
			return null;
		}
		
		PlayerAttackActionEvent event = (PlayerAttackActionEvent)gameModelEvent;
		double chanceOfNyanWaits = Math.random();
		
		// 10% of the time nyan cat is triggered, play Nyan Waits instead
		if(event.getAttackType() == AttackType.NYAN_CAT_ATTACK && chanceOfNyanWaits > 0.9)
		{
			return nyanWaits;
		}
		
		return this.attackAudioMap.get(event.getAttackType());
	}
	
	public boolean isBackgroundSoundPlayer(IGameModelEvent gameModelEvent) {
		return false;
	}
	
	private PlaybackSettings getDefaultPlaybackSettings()
	{
		return new PlaybackSettings(controller.getAudioSettings().getVolume(), false, false);
	}
	
	public PlaybackSettings getPlaybackSettings(AudioSettings globalSettings, IGameModelEvent gameModelEvent) {
		assert(globalSettings != null);
		if (gameModelEvent == null || gameModelEvent.getType() != IGameModelEvent.Type.PLAYER_ATTACK_ACTION) {
			return null;
		}

		return new PlaybackSettings(globalSettings.getVolume(), false, false);
	}
}
