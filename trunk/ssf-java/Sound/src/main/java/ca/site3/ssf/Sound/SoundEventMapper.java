package ca.site3.ssf.Sound;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;

import ca.site3.ssf.gamemodel.GameStateChangedEvent;
import ca.site3.ssf.gamemodel.IGameModelEvent;
import ca.site3.ssf.gamemodel.MatchEndedEvent;
import ca.site3.ssf.gamemodel.PlayerAttackActionEvent;
import ca.site3.ssf.gamemodel.PlayerBlockActionEvent;
import ca.site3.ssf.gamemodel.RingmasterActionEvent;
import ca.site3.ssf.gamemodel.RoundBeginTimerChangedEvent;
import ca.site3.ssf.gamemodel.RoundEndedEvent;
import ca.site3.ssf.gamemodel.UnrecognizedGestureEvent;
import ca.site3.ssf.gamemodel.GameState.GameStateType;
import ca.site3.ssf.gamemodel.MatchEndedEvent.MatchResult;
import ca.site3.ssf.gamemodel.PlayerAttackAction.AttackType;
import ca.site3.ssf.gamemodel.RingmasterAction.ActionType;
import ca.site3.ssf.gamemodel.RoundBeginTimerChangedEvent.RoundBeginCountdownType;
import ca.site3.ssf.gamemodel.RoundEndedEvent.RoundResult;

public class SoundEventMapper {
	
	private Map<GameStateType, PlaybackHandler> gameStateAudioMap =	new HashMap<GameStateType, PlaybackHandler>(GameStateType.values().length);
	private Map<AttackType, PlaybackHandler> attackTypeAudioMap = new HashMap<AttackType, PlaybackHandler>(AttackType.values().length);
	private Map<ActionType, PlaybackHandler> actionTypeAudioMap = new HashMap<ActionType, PlaybackHandler>(ActionType.values().length);
	private Map<RoundBeginCountdownType, PlaybackHandler> roundBeginCountdownTypeAudioMap = new HashMap<RoundBeginCountdownType, PlaybackHandler>(RoundBeginCountdownType.values().length);
	
	// Special playback handlers that either change each time or have special trigger conditions
	private PlaybackHandler playerAttackActionHandler_nyanWaits = null;
	private PlaybackHandler playerBlockActionHandler_P1BlockSuccess = null;
	private PlaybackHandler playerBlockActionHandler_P2BlockSuccess = null;
	private PlaybackHandler unrecognizedGestureHandler = null;
	
	private AudioSettings globalSettings = null;
	private PlaybackSettings playbackSettings = null;
	private Properties configProperties;
	
	protected SoundPlayerController controller;
	
	private static final List<String> THEME_SONG_RESOURCES = Arrays.asList(
		"Theme.Balrog",
		"Theme.Bison",
		"Theme.Blanka",
		"Theme.Chunli",
		"Theme.GuileSpecial",
		"Theme.Ken",
		"Theme.Ryu",
		"Theme.RyuSpecial",
		"Theme.ZangiefSpecial");
	
	protected SoundEventMapper (SoundPlayerController controller){
		assert(controller != null);
		this.controller = controller;
		this.configProperties = controller.getConfigProperties();
		
		this.buildSoundMaps();
	}
	
	private void buildSoundMaps()
	{
		this.globalSettings = controller.getAudioSettings();
		this.playbackSettings = getDefaultPlaybackSettings();
		
		// Game State events
		gameStateAudioMap.put(GameStateType.IDLE_STATE, PlaybackHandler.build(controller, configProperties.getProperty("GameState.IdleState"), playbackSettings));
		gameStateAudioMap.put(GameStateType.MATCH_ENDED_STATE, PlaybackHandler.build(controller,configProperties.getProperty("GameState.MatchEndedState"), playbackSettings));
		gameStateAudioMap.put(GameStateType.NO_STATE, PlaybackHandler.build(controller, configProperties.getProperty("GameState.NoState"), playbackSettings));
		gameStateAudioMap.put(GameStateType.RINGMASTER_STATE, PlaybackHandler.build(controller, configProperties.getProperty("GameState.RingmasterState"), playbackSettings));
		gameStateAudioMap.put(GameStateType.ROUND_BEGINNING_STATE, PlaybackHandler.build(controller, configProperties.getProperty(pickARandomThemeSong()), new PlaybackSettings(globalSettings.getVolume(), true, false)));
		gameStateAudioMap.put(GameStateType.ROUND_ENDED_STATE, PlaybackHandler.build(controller, configProperties.getProperty("GameState.RoundEndedState"), playbackSettings));
		gameStateAudioMap.put(GameStateType.ROUND_IN_PLAY_STATE, PlaybackHandler.build(controller, configProperties.getProperty("GameState.RoundInPlayState"), playbackSettings));
		gameStateAudioMap.put(GameStateType.TEST_ROUND_STATE, PlaybackHandler.build(controller, configProperties.getProperty("GameState.TestRoundState"), playbackSettings));
		gameStateAudioMap.put(GameStateType.TIE_BREAKER_ROUND_STATE, PlaybackHandler.build(controller, configProperties.getProperty("GameState.TieBreakerRoundState"), playbackSettings));
		
		// Player attack events
		attackTypeAudioMap.put(AttackType.CUSTOM_UNDEFINED_ATTACK, PlaybackHandler.build(controller, configProperties.getProperty("AttackType.CustomUndefinedAttack"), playbackSettings));
		attackTypeAudioMap.put(AttackType.LEFT_HOOK_ATTACK, PlaybackHandler.build(controller, configProperties.getProperty("AttackType.LeftHookAttack"), playbackSettings));
		attackTypeAudioMap.put(AttackType.LEFT_JAB_ATTACK, PlaybackHandler.build(controller, configProperties.getProperty("AttackType.LeftJabAttack"), playbackSettings));
		attackTypeAudioMap.put(AttackType.LEFT_UPPERCUT_ATTACK, PlaybackHandler.build(controller, configProperties.getProperty("AttackType.LeftUppercutAttack"), playbackSettings));
		attackTypeAudioMap.put(AttackType.LEFT_CHOP_ATTACK, PlaybackHandler.build(controller, configProperties.getProperty("AttackType.LeftChopAttack"), playbackSettings));
		attackTypeAudioMap.put(AttackType.RIGHT_HOOK_ATTACK, PlaybackHandler.build(controller, configProperties.getProperty("AttackType.RightHookAttack"), playbackSettings));
		attackTypeAudioMap.put(AttackType.RIGHT_JAB_ATTACK, PlaybackHandler.build(controller, configProperties.getProperty("AttackType.RightJabAttack"), playbackSettings));
		attackTypeAudioMap.put(AttackType.RIGHT_UPPERCUT_ATTACK, PlaybackHandler.build(controller, configProperties.getProperty("AttackType.RightUppercutAttack"), playbackSettings));
		attackTypeAudioMap.put(AttackType.RIGHT_CHOP_ATTACK, PlaybackHandler.build(controller, configProperties.getProperty("AttackType.RightChopAttack"), playbackSettings));
		attackTypeAudioMap.put(AttackType.SONIC_BOOM_ATTACK, PlaybackHandler.build(controller, configProperties.getProperty("AttackType.SonicBoomAttack"), playbackSettings));
		attackTypeAudioMap.put(AttackType.HADOUKEN_ATTACK, PlaybackHandler.build(controller, configProperties.getProperty("AttackType.HadoukenAttack"),	playbackSettings));
		attackTypeAudioMap.put(AttackType.DOUBLE_LARIAT_ATTACK, PlaybackHandler.build(controller, configProperties.getProperty("AttackType.DoubleLariatAttack"), playbackSettings));
		attackTypeAudioMap.put(AttackType.SUMO_HEADBUTT_ATTACK, PlaybackHandler.build(controller, configProperties.getProperty("AttackType.SumoHeadbuttAttack"), playbackSettings));
		attackTypeAudioMap.put(AttackType.TWO_HANDED_ONE_HUNDRED_HAND_SLAP_ATTACK, PlaybackHandler.build(controller, configProperties.getProperty("AttackType.TwoHandedOneHundredHandSlapAttack"), playbackSettings));
		attackTypeAudioMap.put(AttackType.ARM_WINDMILL_ATTACK, PlaybackHandler.build(controller, configProperties.getProperty("AttackType.ArmWindmillAttack"), playbackSettings));
		attackTypeAudioMap.put(AttackType.SUCK_IT_ATTACK, PlaybackHandler.build(controller, configProperties.getProperty("AttackType.SuckItAttack"), playbackSettings));
		attackTypeAudioMap.put(AttackType.PSYCHO_CRUSHER_ATTACK, PlaybackHandler.build(controller, configProperties.getProperty("AttackType.PsychoCrusherAttack"), playbackSettings));

		PlaybackHandler shoryukenPlayback = PlaybackHandler.build(controller, configProperties.getProperty("AttackType.ShoryukenAttack"), playbackSettings);
		attackTypeAudioMap.put(AttackType.LEFT_SHORYUKEN_ATTACK, shoryukenPlayback);
		attackTypeAudioMap.put(AttackType.RIGHT_SHORYUKEN_ATTACK, shoryukenPlayback);
		
		PlaybackHandler oneHandedOneHundredHandSlap = PlaybackHandler.build(controller, configProperties.getProperty("AttackType.OneHandedOneHundredHandSlapAttack"), playbackSettings);
		attackTypeAudioMap.put(AttackType.LEFT_ONE_HUNDRED_HAND_SLAP_ATTACK, oneHandedOneHundredHandSlap);
		attackTypeAudioMap.put(AttackType.RIGHT_ONE_HUNDRED_HAND_SLAP_ATTACK, oneHandedOneHundredHandSlap);

		attackTypeAudioMap.put(AttackType.YMCA_ATTACK, PlaybackHandler.build(controller, configProperties.getProperty("AttackType.YmcaAttack"), 
				new PlaybackSettings(globalSettings.getVolume(), false, true)));
		
		attackTypeAudioMap.put(AttackType.NYAN_CAT_ATTACK, PlaybackHandler.build(controller, configProperties.getProperty("AttackType.NyanCatAttack"), 
				new PlaybackSettings(globalSettings.getVolume(), false, true)));
		
		playerAttackActionHandler_nyanWaits = PlaybackHandler.build(controller, configProperties.getProperty("AttackType.NyanWaitsAttack"), new PlaybackSettings(globalSettings.getVolume(), false, true));
		
		// Ringmaster action events
		actionTypeAudioMap.put(ActionType.RINGMASTER_DRUM_ACTION, PlaybackHandler.build(controller, configProperties.getProperty("RingmasterAttack.Drum"), playbackSettings));
		actionTypeAudioMap.put(ActionType.RINGMASTER_ERUPTION_ACTION, PlaybackHandler.build(controller, configProperties.getProperty("RingmasterAttack.Eruption"), playbackSettings));
		actionTypeAudioMap.put(ActionType.RINGMASTER_HADOUKEN_ACTION, PlaybackHandler.build(controller, configProperties.getProperty("RingmasterAttack.Hadouken"), playbackSettings));
		actionTypeAudioMap.put(ActionType.RINGMASTER_LEFT_CIRCLE_ACTION, PlaybackHandler.build(controller, configProperties.getProperty("RingmasterAttack.LeftCircle"), playbackSettings));
		actionTypeAudioMap.put(ActionType.RINGMASTER_LEFT_HALF_RING_ACTION, PlaybackHandler.build(controller, configProperties.getProperty("RingmasterAttack.LeftHalf"), playbackSettings));
		actionTypeAudioMap.put(ActionType.RINGMASTER_RIGHT_CIRCLE_ACTION, PlaybackHandler.build(controller, configProperties.getProperty("RingmasterAttack.RightCircle"), playbackSettings));
		actionTypeAudioMap.put(ActionType.RINGMASTER_RIGHT_HALF_RING_ACTION, PlaybackHandler.build(controller, configProperties.getProperty("RingmasterAttack.RightHalf"), playbackSettings));
		
		PlaybackHandler tempHandler = PlaybackHandler.build(controller, configProperties.getProperty("RingmasterAttack.Jab"), playbackSettings);
		actionTypeAudioMap.put(ActionType.RINGMASTER_LEFT_JAB_ACTION, tempHandler);
		actionTypeAudioMap.put(ActionType.RINGMASTER_RIGHT_JAB_ACTION, tempHandler);
		
		// Round countdown events
		roundBeginCountdownTypeAudioMap.put(RoundBeginCountdownType.THREE, PlaybackHandler.build(controller, configProperties.getProperty("RoundBeginCountdown.Three"), playbackSettings));
		roundBeginCountdownTypeAudioMap.put(RoundBeginCountdownType.TWO, PlaybackHandler.build(controller, configProperties.getProperty("RoundBeginCountdown.Two"), playbackSettings));
		roundBeginCountdownTypeAudioMap.put(RoundBeginCountdownType.ONE, PlaybackHandler.build(controller, configProperties.getProperty("RoundBeginCountdown.One"), playbackSettings));
		roundBeginCountdownTypeAudioMap.put(RoundBeginCountdownType.FIGHT, PlaybackHandler.build(controller, configProperties.getProperty("RoundBeginCountdown.Fight"), playbackSettings));

		// Player block events
		playerBlockActionHandler_P1BlockSuccess = PlaybackHandler.build(controller, configProperties.getProperty("BlockType.P1Block"), new PlaybackSettings(globalSettings.getVolume(), false, true));
		playerBlockActionHandler_P2BlockSuccess = PlaybackHandler.build(controller, configProperties.getProperty("BlockType.P2Block"), new PlaybackSettings(globalSettings.getVolume(), false, true));
		
		// Unrecognized gesture
		unrecognizedGestureHandler = PlaybackHandler.build(controller, configProperties.getProperty("Action.UnrecognizedGesture"), playbackSettings);
	}
	
	public PlaybackHandler getPlaybackHandler(IGameModelEvent gameModelEvent) {
		if (controller == null || gameModelEvent == null) {
			return null;
		}

		switch (gameModelEvent.getType()) {
			// based on this, get the specific action audio map entry with the appropriate casted game event type. everything can be mapped together
			case GAME_STATE_CHANGED: {
				GameStateChangedEvent gameStateChangedEvent = (GameStateChangedEvent)gameModelEvent;
				
				// If a new round is beginning, pick a random theme song
				if(gameStateChangedEvent.getNewState() == GameStateType.ROUND_BEGINNING_STATE){
					return PlaybackHandler.build(controller, configProperties.getProperty(pickARandomThemeSong()), new PlaybackSettings(globalSettings.getVolume(), true, false));
				}
				
				return this.gameStateAudioMap.get(gameStateChangedEvent.getNewState());
			}
			case PLAYER_ATTACK_ACTION: {
				PlayerAttackActionEvent playerAttackActionEvent = (PlayerAttackActionEvent)gameModelEvent;
				double chanceOfNyanWaits = Math.random();
				
				// 10% of the time nyan cat is triggered, play Nyan Waits instead
				if(playerAttackActionEvent.getAttackType() == AttackType.NYAN_CAT_ATTACK && chanceOfNyanWaits > 0.9)
				{
					return playerAttackActionHandler_nyanWaits;
				}
				
				return this.attackTypeAudioMap.get(playerAttackActionEvent.getAttackType());
			}
			case PLAYER_BLOCK_ACTION: {
				PlayerBlockActionEvent playerBlockActionEvent = (PlayerBlockActionEvent)gameModelEvent;

				// 10% of the time nyan cat is triggered, play Nyan Waits instead
				if(playerBlockActionEvent.getBlockWasEffective() && playerBlockActionEvent.getPlayerNum() == 1)
				{
					return playerBlockActionHandler_P1BlockSuccess;
				}
				else if(playerBlockActionEvent.getBlockWasEffective() && playerBlockActionEvent.getPlayerNum() == 2)
				{
					return playerBlockActionHandler_P2BlockSuccess;
				}
				
				return null;
			}
			case ROUND_ENDED: {
				this.controller.stopAllSounds();

				RoundEndedEvent roundEndedEvent = (RoundEndedEvent)gameModelEvent;
				PlaybackHandler playbackHandler = null;
				
				// Grab the appropriate playback handler. Needs to be rebuilt each round for custom round conditions like toasty/perfect
				if (roundEndedEvent.getRoundResult() == RoundResult.PLAYER1_VICTORY){
					playbackHandler = PlaybackHandler.build(controller, configProperties.getProperty("RoundResult.PlayerOneVictory"), playbackSettings);
				}
				else if (roundEndedEvent.getRoundResult() == RoundResult.PLAYER2_VICTORY){
					playbackHandler = PlaybackHandler.build(controller, configProperties.getProperty("RoundResult.PlayerTwoVictory"), playbackSettings);
				}
				else {
					playbackHandler = PlaybackHandler.build(controller, configProperties.getProperty("RoundResult.Tie"), playbackSettings);
				}

				// Assign any special victory conditions to the playback handler
				if (roundEndedEvent.isPerfect())
				{
					playbackHandler.hasFollowupSound = true;
					playbackHandler.followupSoundSource = this.configProperties.getProperty("RoundResult.Perfect");
				}
				else if(roundEndedEvent.isToasty())
				{
					playbackHandler.hasFollowupSound = true;
					playbackHandler.followupSoundSource = this.configProperties.getProperty("RoundResult.Toasty");
				}
				
				return playbackHandler;
			}
			case MATCH_ENDED: {
				this.controller.stopAllSounds();
				return null;
			}
			case RINGMASTER_ACTION: {
				RingmasterActionEvent ringmasterActionEvent = (RingmasterActionEvent)gameModelEvent;
				return this.actionTypeAudioMap.get(ringmasterActionEvent.getActionType());
			}
			case ROUND_BEGIN_TIMER_CHANGED: {
				RoundBeginTimerChangedEvent roundBeginTimerChangedEvent = (RoundBeginTimerChangedEvent)gameModelEvent;
				return this.roundBeginCountdownTypeAudioMap.get(roundBeginTimerChangedEvent.getThreeTwoOneFightTime());
			}
			case UNRECOGNIZED_GESTURE: {
				UnrecognizedGestureEvent unrecognizedGestureEvent = (UnrecognizedGestureEvent)gameModelEvent;
				
				if (!unrecognizedGestureEvent.getEntity().getIsPlayer()) {
					break;
				}
				
				return unrecognizedGestureHandler;
			}
			 	
			default:
				break;
		}
		
		return null;
	}
	
	private PlaybackSettings getDefaultPlaybackSettings()
	{
		return new PlaybackSettings(controller.getAudioSettings().getVolume(), false, false);
	}
	
	private String pickARandomThemeSong() {
		Random random = new Random(System.currentTimeMillis());
		int songIndex = random.nextInt(THEME_SONG_RESOURCES.size());
		return THEME_SONG_RESOURCES.get(songIndex);
	}
}
