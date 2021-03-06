package ca.site3.ssf.guiprotocol;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

import ca.site3.ssf.gamemodel.ActionFactory.ActionType;
import ca.site3.ssf.gamemodel.FireEmitter.Location;
import ca.site3.ssf.gamemodel.GameState.GameStateType;
import ca.site3.ssf.gamemodel.IGameModel.Entity;
import ca.site3.ssf.gamemodel.MatchEndedEvent;
import ca.site3.ssf.gamemodel.PlayerAttackAction.AttackType;
import ca.site3.ssf.gamemodel.PlayerAttackActionFailedEvent;
import ca.site3.ssf.gamemodel.RoundBeginTimerChangedEvent;
import ca.site3.ssf.gamemodel.RoundEndedEvent;
import ca.site3.ssf.gamemodel.SystemInfoRefreshEvent;
import ca.site3.ssf.gamemodel.SystemInfoRefreshEvent.OutputDeviceStatus;
import ca.site3.ssf.guiprotocol.Common.Player;
import ca.site3.ssf.guiprotocol.Event.GameEvent;
import ca.site3.ssf.guiprotocol.Event.GameEvent.BoardStatus;
import ca.site3.ssf.guiprotocol.Event.GameEvent.RingmasterActionType;
import ca.site3.ssf.guiprotocol.GuiCommand.Command.FireEmitterType;
import ca.site3.ssf.guiprotocol.GuiCommand.Command.PlayerAction;
import ca.site3.ssf.guiprotocol.GuiCommand.Command.RingmasterAction;

/**
 * Utility methods for converting between enums etc. in the
 * GameModel and in the protobuf definitions
 * 
 * @author greg, Callum
 */
class SerializationHelper {

	static Player entityToProtobuf(Entity player) {
		switch (player) {
		case PLAYER1_ENTITY:
			return Player.P1;
		case PLAYER2_ENTITY:
			return Player.P2;
		case RINGMASTER_ENTITY:
			return Player.RINGMASTER;
		default:
			throw new IllegalArgumentException("Unknown player type: "+player);
		}
	}
	
	static Entity playerToGame(Player player) {
		switch (player) {
		case P1:
			return Entity.PLAYER1_ENTITY;
		case P2:
			return Entity.PLAYER2_ENTITY;
		case RINGMASTER:
			return Entity.RINGMASTER_ENTITY;
		default:
			throw new IllegalArgumentException("Unknown player type: "+player);
		}
	}
	

	static EnumSet<Entity> playersToGame(Collection<Player> players) {
		EnumSet<Entity> entities = EnumSet.noneOf(Entity.class);
		for (Player p : players) {
			entities.add(playerToGame(p));
		}
		return entities;
	}
	
	
	static Collection<Player> entitiesToProtobuf(EnumSet<Entity> entities) {
		Collection<Player> players = new ArrayList<Common.Player>(entities.size());
		for (Entity e : entities) {
			players.add(entityToProtobuf(e));
		}
		return players;
	}
	
	
	static int playerToNum(Player player) {
		switch (player) {
		case P1:
			return 1;
		case P2:
			return 2;
		default:
			return -1;
		}
	}
	
	static Player playerFromNum(int num) {
		if (num == 1) {
			return Player.P1;
		} else if (num == 2) {
			return Player.P2;
		} else {
			throw new IllegalArgumentException("Invalid player number: "+num);
		}
	}
	
	static int entityToNum(Entity player) {
		if (player == Entity.PLAYER1_ENTITY)
			return 1;
		else if (player == Entity.PLAYER2_ENTITY)
			return 2;
		else
			return -1;
	}
	
	static Entity entityFromNum(int num) {
		if (num == 1) {
			return Entity.PLAYER1_ENTITY;
		} else if (num == 2) {
			return Entity.PLAYER2_ENTITY;
		}
		throw new IllegalArgumentException("Invalid player number: "+num);
	}
	
	
	
	static PlayerAction playerActionToProtobuf(ActionType type) {
		switch (type) {
		case BLOCK:
			return PlayerAction.BLOCK;
		case HADOUKEN_ATTACK:
			return PlayerAction.HADOUKEN_ATTACK;
		case HOOK_ATTACK:
			return PlayerAction.HOOK_ATTACK;
		case JAB_ATTACK:
			return PlayerAction.JAB_ATTACK;
		case UPPERCUT_ATTACK:
			return PlayerAction.UPPERCUT_ATTACK;
		case CHOP_ATTACK:
			return PlayerAction.CHOP_ATTACK;
		case SONIC_BOOM_ATTACK:
			return PlayerAction.SONIC_BOOM_ATTACK;
		case SHORYUKEN_ATTACK:
			return PlayerAction.SHORYUKEN_ATTACK;
		case DOUBLE_LARIAT_ATTACK:
			return PlayerAction.DOUBLE_LARIAT_ATTACK;
		//case QUADRUPLE_LARIAT_ATTACK:
		//	return PlayerAction.QUADRUPLE_LARIAT_ATTACK;
		case SUMO_HEADBUTT_ATTACK:
			return PlayerAction.SUMO_HEADBUTT_ATTACK;
		case ONE_HUNDRED_HAND_SLAP_ATTACK:
			return PlayerAction.ONE_HUNDRED_HAND_SLAP_ATTACK;
		case PSYCHO_CRUSHER_ATTACK:
			return PlayerAction.PSYCHO_CRUSHER_ATTACK;
		case YMCA_ATTACK:
			return PlayerAction.YMCA_ATTACK;
		case NYAN_CAT_ATTACK:
			return PlayerAction.NYAN_CAT_ATTACK;
		//case DISCO_STU_ATTACK:
		//	return PlayerAction.DISCO_STU_ATTACK;
		case ARM_WINDMILL_ATTACK:
			return PlayerAction.ARM_WINDMILL_ATTACK;
		case SUCK_IT_ATTACK:
			return PlayerAction.SUCK_IT_ATTACK;
		//case VAFANAPOLI_ATTACK:
		//	return PlayerAction.VAFANAPOLI_ATTACK;
		default:
			return null;
		}
	}
	
	static RingmasterAction ringmasterActionToProtobuf(ActionType action) {
		switch (action) {
		case RINGMASTER_CIRCLE_ACTION:
			return RingmasterAction.CIRCLE_ACTION;
		case RINGMASTER_DRUM_ACTION:
			return RingmasterAction.DRUM_ACTION;
		case RINGMASTER_ERUPTION_ACTION:
			return RingmasterAction.ERUPTION_ACTION;
		case RINGMASTER_HADOUKEN_ACTION:
			return RingmasterAction.HADOUKEN_ACTION;
		case RINGMASTER_HALF_RING_ACTION:
			return RingmasterAction.HALF_RING_ACTION;
		case RINGMASTER_JAB_ACTION:
			return RingmasterAction.JAB_ACTION;
		default:
			return null;
		}
	}
	
	static ActionType playerActionToGame(PlayerAction action) {
		switch (action) {
		case BLOCK:
			return ActionType.BLOCK;
		case HADOUKEN_ATTACK:
			return ActionType.HADOUKEN_ATTACK;
		case HOOK_ATTACK:
			return ActionType.HOOK_ATTACK;
		case JAB_ATTACK:
			return ActionType.JAB_ATTACK;
		case UPPERCUT_ATTACK:
			return ActionType.UPPERCUT_ATTACK;
		case CHOP_ATTACK:
			return ActionType.CHOP_ATTACK;
		case SONIC_BOOM_ATTACK:
			return ActionType.SONIC_BOOM_ATTACK;
		case SHORYUKEN_ATTACK:
			return ActionType.SHORYUKEN_ATTACK;
		case DOUBLE_LARIAT_ATTACK:
			return ActionType.DOUBLE_LARIAT_ATTACK;
		//case QUADRUPLE_LARIAT_ATTACK:
		//	return ActionType.QUADRUPLE_LARIAT_ATTACK;
		case SUMO_HEADBUTT_ATTACK:
			return ActionType.SUMO_HEADBUTT_ATTACK;
		case ONE_HUNDRED_HAND_SLAP_ATTACK:
			return ActionType.ONE_HUNDRED_HAND_SLAP_ATTACK;
		case PSYCHO_CRUSHER_ATTACK:
			return ActionType.PSYCHO_CRUSHER_ATTACK;
		case YMCA_ATTACK:
			return ActionType.YMCA_ATTACK;
		case NYAN_CAT_ATTACK:
			return ActionType.NYAN_CAT_ATTACK;
		//case DISCO_STU_ATTACK:
		//	return ActionType.DISCO_STU_ATTACK;
		case ARM_WINDMILL_ATTACK:
			return ActionType.ARM_WINDMILL_ATTACK;
		case SUCK_IT_ATTACK:
			return ActionType.SUCK_IT_ATTACK;
		//case VAFANAPOLI_ATTACK:
		//	return ActionType.VAFANAPOLI_ATTACK;
		default:
			throw new IllegalArgumentException("Unknown player action: " + action);
		}
	}
	
	static ActionType ringmasterActionToGame(RingmasterAction action) {
		
		switch (action) {
		case CIRCLE_ACTION:
			return ActionType.RINGMASTER_CIRCLE_ACTION;
		case DRUM_ACTION:
			return ActionType.RINGMASTER_DRUM_ACTION;
		case ERUPTION_ACTION:
			return ActionType.RINGMASTER_ERUPTION_ACTION;
		case HADOUKEN_ACTION:
			return ActionType.RINGMASTER_HADOUKEN_ACTION;
		case HALF_RING_ACTION:
			return ActionType.RINGMASTER_HALF_RING_ACTION;
		case JAB_ACTION:
			return ActionType.RINGMASTER_JAB_ACTION;
		default:
			throw new IllegalArgumentException("Unknown ringmaster action: " + action);
		}
	}
	
	static Location emitterTypeToGame(FireEmitterType t) {
		switch (t) {
		case LEFT_RAIL:
			return Location.LEFT_RAIL;
		case OUTER_RING:
			return Location.OUTER_RING;
		case RIGHT_RAIL:
			return Location.RIGHT_RAIL;
		default:
			throw new IllegalArgumentException("Unknown fire emitter location: "+t);
		}
	}
	
	// ugh duplication here is where java really bogs down
	static Location eventEmitterTypeToGame(ca.site3.ssf.guiprotocol.Event.GameEvent.FireEmitterType t) {
		switch (t) {
		case LEFT_RAIL:
			return Location.LEFT_RAIL;
		case OUTER_RING:
			return Location.OUTER_RING;
		case RIGHT_RAIL:
			return Location.RIGHT_RAIL;
		default:
			throw new IllegalArgumentException("Unknown fire emitter location from event: "+t);
		}
	}
	
	
	static FireEmitterType locationToProtobuf(Location l) {
		switch (l) {
		case LEFT_RAIL:
			return FireEmitterType.LEFT_RAIL;
		case OUTER_RING:
			return FireEmitterType.OUTER_RING;
		case RIGHT_RAIL:
			return FireEmitterType.RIGHT_RAIL;
		default:
			throw new IllegalArgumentException("Unknown fire emitter location: "+l);
		}
	}

	
	static GameEvent.FireEmitterType locationToEventProtobuf(Location l) {
		switch (l) {
		case LEFT_RAIL:
			return GameEvent.FireEmitterType.LEFT_RAIL;
		case OUTER_RING:
			return GameEvent.FireEmitterType.OUTER_RING;
		case RIGHT_RAIL:
			return GameEvent.FireEmitterType.RIGHT_RAIL;
		default:
			throw new IllegalArgumentException("Unknown fire emitter location: "+l);
		}
	}
	
	
	static List<RoundEndedEvent.RoundResult> protobufToRoundResults(List<GameEvent.RoundResult> results) {
		List<RoundEndedEvent.RoundResult> returnVal = new ArrayList<RoundEndedEvent.RoundResult>(results.size());
		for (GameEvent.RoundResult currRoundResult : results) {
			returnVal.add(SerializationHelper.protobufToRoundResult(currRoundResult));
		}
		
		return returnVal;
	}
	
	static List<GameEvent.RoundResult> roundResultsToProtobuf(List<RoundEndedEvent.RoundResult> results) {
		List<GameEvent.RoundResult> returnVal = new ArrayList<GameEvent.RoundResult>(results.size());
		for (RoundEndedEvent.RoundResult currRoundResult : results) {
			returnVal.add(SerializationHelper.roundResultToProtobuf(currRoundResult));
		}
		
		return returnVal;
	}
	
	static RoundEndedEvent.RoundResult protobufToRoundResult(GameEvent.RoundResult result) {
		assert(result != null);
		
		switch (result) {
		case PLAYER_1_ROUND_WIN:
			return RoundEndedEvent.RoundResult.PLAYER1_VICTORY;
		case PLAYER_2_ROUND_WIN:
			return RoundEndedEvent.RoundResult.PLAYER2_VICTORY;
		case ROUND_TIE:
			return RoundEndedEvent.RoundResult.TIE;
		default:
			assert(false);
			throw new IllegalArgumentException("Unknown round result: " + result);
		}
	}
	
	static GameEvent.RoundResult roundResultToProtobuf(RoundEndedEvent.RoundResult result) {
		assert(result != null);
		
		switch (result) {
		case PLAYER1_VICTORY:
			return GameEvent.RoundResult.PLAYER_1_ROUND_WIN;
		case PLAYER2_VICTORY:
			return GameEvent.RoundResult.PLAYER_2_ROUND_WIN;
		case TIE:
			return GameEvent.RoundResult.ROUND_TIE;
		default:
			assert(false);
			throw new IllegalArgumentException("Unknown round result: " + result);
		}
	}
	
	static MatchEndedEvent.MatchResult protobufToMatchResult(GameEvent.MatchResult result) {
		assert(result != null);
		
		switch (result) {
		case PLAYER_1_MATCH_WIN:
			return MatchEndedEvent.MatchResult.PLAYER1_VICTORY;
		case PLAYER_2_MATCH_WIN:
			return MatchEndedEvent.MatchResult.PLAYER2_VICTORY;
		default:
			assert(false);
			throw new IllegalArgumentException("Unknown match result: " + result);
		}
	}
	
	static GameEvent.MatchResult matchResultToProtobuf(MatchEndedEvent.MatchResult result) {
		assert(result != null);
		
		switch (result) {
		case PLAYER1_VICTORY:
			return GameEvent.MatchResult.PLAYER_1_MATCH_WIN;
		case PLAYER2_VICTORY:
			return GameEvent.MatchResult.PLAYER_2_MATCH_WIN;
		default:
			assert(false);
			break;
		}
		
		return null;
	}
	
	static RoundBeginTimerChangedEvent.RoundBeginCountdownType protobufToRoundBeginCountdownTimer(GameEvent.RoundBeginCountdownTime countdownTime) {
		assert(countdownTime != null);
		
		switch (countdownTime) {
		case THREE:
			return RoundBeginTimerChangedEvent.RoundBeginCountdownType.THREE;
		case TWO:
			return RoundBeginTimerChangedEvent.RoundBeginCountdownType.TWO;
		case ONE:
			return RoundBeginTimerChangedEvent.RoundBeginCountdownType.ONE;
		case FIGHT:
			return RoundBeginTimerChangedEvent.RoundBeginCountdownType.FIGHT;
		default:
			assert(false);
			throw new IllegalArgumentException("Unknown RoundBeginCountdownType: " + countdownTime);
		}
	}
	
	static GameEvent.RoundBeginCountdownTime roundBeginCountdownTimerToProtobuf(RoundBeginTimerChangedEvent.RoundBeginCountdownType countdownTime) {
		if (countdownTime == null) {
			return null;
		}
		
		switch (countdownTime) {
		case FIGHT:
			return GameEvent.RoundBeginCountdownTime.FIGHT;
		case ONE:
			return GameEvent.RoundBeginCountdownTime.ONE;
		case TWO:
			return GameEvent.RoundBeginCountdownTime.TWO;
		case THREE:
			return GameEvent.RoundBeginCountdownTime.THREE;
		default:
			assert(false);
			throw new IllegalArgumentException("Unknown RoundBeginCountdownType: " + countdownTime);
		}
	}
	
	static GameStateType protobufToGameState(Common.GameState gameState) {
		assert(gameState != null);
		
		switch (gameState) {
		case IDLE_STATE:
			return GameStateType.IDLE_STATE;
		case MATCH_ENDED_STATE:
			return GameStateType.MATCH_ENDED_STATE;
		case NO_STATE:
			return GameStateType.NO_STATE;
		case PAUSED_STATE:
			return GameStateType.PAUSED_STATE;
		case RINGMASTER_STATE:
			return GameStateType.RINGMASTER_STATE;
		case ROUND_BEGINNING_STATE:
			return GameStateType.ROUND_BEGINNING_STATE;
		case ROUND_ENDED_STATE:
			return GameStateType.ROUND_ENDED_STATE;
		case ROUND_IN_PLAY_STATE:
			return GameStateType.ROUND_IN_PLAY_STATE;
		case TEST_ROUND_STATE:
			return GameStateType.TEST_ROUND_STATE;
		case TIE_BREAKER_ROUND_STATE:
			return GameStateType.TIE_BREAKER_ROUND_STATE;
		default:
			assert(false);
			throw new IllegalArgumentException("Unknown GameState: "+gameState);
		}
	}
	
	static Common.GameState gameStateToProtobuf(GameStateType gameState) {
		assert(gameState != null);
		
		switch (gameState) {
		case IDLE_STATE:
			return Common.GameState.IDLE_STATE;
		case MATCH_ENDED_STATE:
			return Common.GameState.MATCH_ENDED_STATE;
		case NO_STATE:
			return Common.GameState.NO_STATE;
		case PAUSED_STATE:
			return Common.GameState.PAUSED_STATE;
		case RINGMASTER_STATE:
			return Common.GameState.RINGMASTER_STATE;
		case ROUND_BEGINNING_STATE:
			return Common.GameState.ROUND_BEGINNING_STATE;
		case ROUND_ENDED_STATE:
			return Common.GameState.ROUND_ENDED_STATE;
		case ROUND_IN_PLAY_STATE:
			return Common.GameState.ROUND_IN_PLAY_STATE;
		case TEST_ROUND_STATE:
			return Common.GameState.TEST_ROUND_STATE;
		case TIE_BREAKER_ROUND_STATE:
			return Common.GameState.TIE_BREAKER_ROUND_STATE;
		default:
			assert(false);
			throw new IllegalArgumentException("Unknown GameStateType: "+gameState);
		}
	}
	
	static AttackType protobufToAttackType(ca.site3.ssf.guiprotocol.Event.GameEvent.AttackType t) {
		switch (t) {
		case CUSTOM_UNDEFINED:
			return AttackType.CUSTOM_UNDEFINED_ATTACK;
		case HADOUKEN:
			return AttackType.HADOUKEN_ATTACK;
		case LEFT_HOOK:
			return AttackType.LEFT_HOOK_ATTACK;
		case LEFT_JAB:
			return AttackType.LEFT_JAB_ATTACK;
		case LEFT_UPPERCUT:
			return AttackType.LEFT_UPPERCUT_ATTACK;
		case LEFT_CHOP:
			return AttackType.LEFT_CHOP_ATTACK;
		case RIGHT_HOOK:
			return AttackType.RIGHT_HOOK_ATTACK;
		case RIGHT_JAB:
			return AttackType.RIGHT_JAB_ATTACK;
		case RIGHT_UPPERCUT:
			return AttackType.RIGHT_UPPERCUT_ATTACK;
		case RIGHT_CHOP:
			return AttackType.RIGHT_CHOP_ATTACK;
		case SONIC_BOOM:
			return AttackType.SONIC_BOOM_ATTACK;
		case LEFT_SHORYUKEN_ATTACK:
			return AttackType.LEFT_SHORYUKEN_ATTACK;
		case RIGHT_SHORYUKEN_ATTACK:
			return AttackType.RIGHT_SHORYUKEN_ATTACK;
		case DOUBLE_LARIAT_ATTACK:
			return AttackType.DOUBLE_LARIAT_ATTACK;
		//case QUADRUPLE_LARIAT_ATTACK:
		//	return AttackType.QUADRUPLE_LARIAT_ATTACK;
		case SUMO_HEADBUTT_ATTACK:
			return AttackType.SUMO_HEADBUTT_ATTACK;
		case LEFT_ONE_HUNDRED_HAND_SLAP_ATTACK:
			return AttackType.LEFT_ONE_HUNDRED_HAND_SLAP_ATTACK;
		case RIGHT_ONE_HUNDRED_HAND_SLAP_ATTACK:
			return AttackType.RIGHT_ONE_HUNDRED_HAND_SLAP_ATTACK;
		case TWO_HANDED_ONE_HUNDRED_HAND_SLAP_ATTACK:
			return AttackType.TWO_HANDED_ONE_HUNDRED_HAND_SLAP_ATTACK;
		case PSYCHO_CRUSHER_ATTACK:
			return AttackType.PSYCHO_CRUSHER_ATTACK;
		case YMCA_ATTACK:
			return AttackType.YMCA_ATTACK;
		case NYAN_CAT_ATTACK:
			return AttackType.NYAN_CAT_ATTACK;
		//case DISCO_STU_ATTACK:
		//	return AttackType.DISCO_STU_ATTACK;
		case ARM_WINDMILL_ATTACK:
			return AttackType.ARM_WINDMILL_ATTACK;
		case SUCK_IT_ATTACK:
			return AttackType.SUCK_IT_ATTACK;
		//case LEFT_VAFANAPOLI_ATTACK:
		//	return AttackType.LEFT_VAFANAPOLI_ATTACK;
		//case RIGHT_VAFANAPOLI_ATTACK:
		//	return AttackType.RIGHT_VAFANAPOLI_ATTACK;
		default:
			throw new IllegalArgumentException("Unrecognized protobuf attack type: "+t);
		}
	}
	
	static GameEvent.AttackType attackTypeToProtobuf(AttackType t) {
		switch (t) {
		case CUSTOM_UNDEFINED_ATTACK:
			return GameEvent.AttackType.CUSTOM_UNDEFINED;
		case HADOUKEN_ATTACK:
			return GameEvent.AttackType.HADOUKEN;
		case LEFT_HOOK_ATTACK:
			return GameEvent.AttackType.LEFT_HOOK;
		case LEFT_JAB_ATTACK:
			return GameEvent.AttackType.LEFT_JAB;
		case LEFT_UPPERCUT_ATTACK:
			return GameEvent.AttackType.LEFT_UPPERCUT;
		case LEFT_CHOP_ATTACK:
			return GameEvent.AttackType.LEFT_CHOP;
		case RIGHT_HOOK_ATTACK:
			return GameEvent.AttackType.RIGHT_HOOK;
		case RIGHT_JAB_ATTACK:
			return GameEvent.AttackType.RIGHT_JAB;
		case RIGHT_UPPERCUT_ATTACK:
			return GameEvent.AttackType.RIGHT_UPPERCUT;
		case RIGHT_CHOP_ATTACK:
			return GameEvent.AttackType.RIGHT_CHOP;
		case SONIC_BOOM_ATTACK:
			return GameEvent.AttackType.SONIC_BOOM;
		case LEFT_SHORYUKEN_ATTACK:
			return GameEvent.AttackType.LEFT_SHORYUKEN_ATTACK;
		case RIGHT_SHORYUKEN_ATTACK:
			return GameEvent.AttackType.RIGHT_SHORYUKEN_ATTACK;
		case DOUBLE_LARIAT_ATTACK:
			return GameEvent.AttackType.DOUBLE_LARIAT_ATTACK;
		//case QUADRUPLE_LARIAT_ATTACK:
		//	return GameEvent.AttackType.QUADRUPLE_LARIAT_ATTACK;
		case SUMO_HEADBUTT_ATTACK:
			return GameEvent.AttackType.SUMO_HEADBUTT_ATTACK;
		case LEFT_ONE_HUNDRED_HAND_SLAP_ATTACK:
			return GameEvent.AttackType.LEFT_ONE_HUNDRED_HAND_SLAP_ATTACK;
		case RIGHT_ONE_HUNDRED_HAND_SLAP_ATTACK:
			return GameEvent.AttackType.RIGHT_ONE_HUNDRED_HAND_SLAP_ATTACK;
		case TWO_HANDED_ONE_HUNDRED_HAND_SLAP_ATTACK:
			return GameEvent.AttackType.TWO_HANDED_ONE_HUNDRED_HAND_SLAP_ATTACK;
		case PSYCHO_CRUSHER_ATTACK:
			return GameEvent.AttackType.PSYCHO_CRUSHER_ATTACK;
		case YMCA_ATTACK:
			return GameEvent.AttackType.YMCA_ATTACK;
		case NYAN_CAT_ATTACK:
			return GameEvent.AttackType.NYAN_CAT_ATTACK;
		//case DISCO_STU_ATTACK:
		//	return GameEvent.AttackType.DISCO_STU_ATTACK;
		case ARM_WINDMILL_ATTACK:
			return GameEvent.AttackType.ARM_WINDMILL_ATTACK;
		case SUCK_IT_ATTACK:
			return GameEvent.AttackType.SUCK_IT_ATTACK;
		//case LEFT_VAFANAPOLI_ATTACK:
		//	return GameEvent.AttackType.LEFT_VAFANAPOLI_ATTACK;
		//case RIGHT_VAFANAPOLI_ATTACK:
		//	return GameEvent.AttackType.RIGHT_VAFANAPOLI_ATTACK;
		default:
			throw new IllegalArgumentException("Unrecognized AttackType: " + t);
		}
	}
	
	static GameEvent.AttackFailureReason attackFailureReasonToProtobuf(PlayerAttackActionFailedEvent.Reason reason) {
		switch (reason) {
			case NOT_ENOUGH_ACTION_POINTS:
				return GameEvent.AttackFailureReason.NOT_ENOUGH_ACTION_POINTS;
			default:
				throw new IllegalArgumentException("Unrecognized Player Attack Failure Reason: " + reason);
		}
	}
	
	static ca.site3.ssf.gamemodel.PlayerAttackActionFailedEvent.Reason protobufToAttackFailureReason(GameEvent.AttackFailureReason reason) {
		switch (reason) {
			case NOT_ENOUGH_ACTION_POINTS:
				return ca.site3.ssf.gamemodel.PlayerAttackActionFailedEvent.Reason.NOT_ENOUGH_ACTION_POINTS;
			default:
				throw new IllegalArgumentException("Unrecognized protobuf Player Attack Failure Reason: " + reason);
		}
	}
	
	static ca.site3.ssf.gamemodel.RingmasterAction.ActionType protobufToRingmasterAction(RingmasterActionType action) {
		switch (action) {
		case LEFT_CIRCLE_ACTION:
			return ca.site3.ssf.gamemodel.RingmasterAction.ActionType.RINGMASTER_LEFT_CIRCLE_ACTION;
		case RIGHT_CIRCLE_ACTION:
			return ca.site3.ssf.gamemodel.RingmasterAction.ActionType.RINGMASTER_RIGHT_CIRCLE_ACTION;
		case DRUM_ACTION:
			return ca.site3.ssf.gamemodel.RingmasterAction.ActionType.RINGMASTER_DRUM_ACTION;
		case ERUPTION_ACTION:
			return ca.site3.ssf.gamemodel.RingmasterAction.ActionType.RINGMASTER_ERUPTION_ACTION;
		case HADOUKEN_ACTION:
			return ca.site3.ssf.gamemodel.RingmasterAction.ActionType.RINGMASTER_HADOUKEN_ACTION;
		case LEFT_HALF_RING_ACTION:
			return ca.site3.ssf.gamemodel.RingmasterAction.ActionType.RINGMASTER_LEFT_HALF_RING_ACTION;
		case RIGHT_HALF_RING_ACTION:
			return ca.site3.ssf.gamemodel.RingmasterAction.ActionType.RINGMASTER_RIGHT_HALF_RING_ACTION;
		case LEFT_JAB_ACTION:
			return ca.site3.ssf.gamemodel.RingmasterAction.ActionType.RINGMASTER_LEFT_JAB_ACTION;
		case RIGHT_JAB_ACTION:
			return ca.site3.ssf.gamemodel.RingmasterAction.ActionType.RINGMASTER_RIGHT_JAB_ACTION;
		default:
			throw new IllegalArgumentException("Unrecognized RingmasterActionType: " + action);
		}
	}
	
	static RingmasterActionType ringmasterActionTypeToProtobuf(ca.site3.ssf.gamemodel.RingmasterAction.ActionType action) {
		switch (action) {
		case RINGMASTER_DRUM_ACTION:
			return RingmasterActionType.DRUM_ACTION;
		case RINGMASTER_ERUPTION_ACTION:
			return RingmasterActionType.ERUPTION_ACTION;
		case RINGMASTER_HADOUKEN_ACTION:
			return RingmasterActionType.HADOUKEN_ACTION;
		case RINGMASTER_LEFT_CIRCLE_ACTION:
			return RingmasterActionType.LEFT_CIRCLE_ACTION;
		case RINGMASTER_LEFT_HALF_RING_ACTION:
			return RingmasterActionType.LEFT_HALF_RING_ACTION;
		case RINGMASTER_LEFT_JAB_ACTION:
			return RingmasterActionType.LEFT_JAB_ACTION;
		case RINGMASTER_RIGHT_CIRCLE_ACTION:
			return RingmasterActionType.RIGHT_CIRCLE_ACTION;
		case RINGMASTER_RIGHT_HALF_RING_ACTION:
			return RingmasterActionType.RIGHT_HALF_RING_ACTION;
		case RINGMASTER_RIGHT_JAB_ACTION:
			return RingmasterActionType.RIGHT_JAB_ACTION;
		default:
			throw new IllegalArgumentException("Unrecognized RingmasterAction.ActionType: " + action);
		}
	}
	
	
	static Iterable<BoardStatus> boardInfoToProtobuf(SystemInfoRefreshEvent e) {
		List<BoardStatus> statuses = new ArrayList<Event.GameEvent.BoardStatus>(32);
		
		// left rail
		int i;
		for (i=0; i<8; i++) {
			OutputDeviceStatus s = e.getDeviceStatus(Location.LEFT_RAIL, i);
			BoardStatus.Builder b = BoardStatus.newBuilder();
			b.setDeviceId(i)
				.setResponding(s.isResponding)	
				.setArmed(s.isArmed)
				.setFlame(s.isFlame);
			statuses.add(b.build());
		}
		// right rail
		for (i=8; i<16; i++) {
			OutputDeviceStatus s = e.getDeviceStatus(Location.RIGHT_RAIL, i-8);
			BoardStatus.Builder b = BoardStatus.newBuilder();
			b.setDeviceId(i)
				.setResponding(s.isResponding)	
				.setArmed(s.isArmed)
				.setFlame(s.isFlame);
			statuses.add(b.build());
		}
		// outer ring
		for (i=16; i<32; i++) {
			OutputDeviceStatus s = e.getDeviceStatus(Location.OUTER_RING, i-16);
			BoardStatus.Builder b = BoardStatus.newBuilder();
			b.setDeviceId(i)
				.setResponding(s.isResponding)	
				.setArmed(s.isArmed)
				.setFlame(s.isFlame);
			statuses.add(b.build());
		}
		
		return statuses;
	}
	
	
	static SystemInfoRefreshEvent protobufToBoardInfo(Iterable<BoardStatus> statuses) {
		
		OutputDeviceStatus[] leftRail = new OutputDeviceStatus[8];
		OutputDeviceStatus[] rightRail = new OutputDeviceStatus[8];
		OutputDeviceStatus[] outerRing = new OutputDeviceStatus[16];
		
		int i=0;
		for (BoardStatus s : statuses) {
			if (i < 8) {
				leftRail[i] = new OutputDeviceStatus(i, s.getResponding(), s.getArmed(), s.getFlame());
			} else if (i < 16) {
				rightRail[i-8] = new OutputDeviceStatus(i, s.getResponding(), s.getArmed(), s.getFlame());
			} else {
				outerRing[i-16] = new OutputDeviceStatus(i, s.getResponding(), s.getArmed(), s.getFlame());
			}
			i++;
		}
		
		return new SystemInfoRefreshEvent(leftRail, rightRail, outerRing);
	}
}
