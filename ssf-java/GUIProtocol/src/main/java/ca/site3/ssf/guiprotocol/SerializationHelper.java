package ca.site3.ssf.guiprotocol;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

import ca.site3.ssf.gamemodel.ActionFactory.PlayerActionType;
import ca.site3.ssf.gamemodel.FireEmitter.Location;
import ca.site3.ssf.gamemodel.GameState.GameStateType;
import ca.site3.ssf.gamemodel.IGameModel.Entity;
import ca.site3.ssf.gamemodel.PlayerAttackAction.AttackType;
import ca.site3.ssf.gamemodel.MatchEndedEvent;
import ca.site3.ssf.gamemodel.RoundBeginTimerChangedEvent;
import ca.site3.ssf.gamemodel.RoundBeginTimerChangedEvent.RoundBeginCountdownType;
import ca.site3.ssf.gamemodel.RoundEndedEvent;
import ca.site3.ssf.gamemodel.RoundEndedEvent.RoundResult;
import ca.site3.ssf.guiprotocol.Event.GameEvent;
import ca.site3.ssf.guiprotocol.Event.GameEvent.GameState;
import ca.site3.ssf.guiprotocol.Event.GameEvent.MatchResult;
import ca.site3.ssf.guiprotocol.GuiCommand.Command.FireEmitterType;
import ca.site3.ssf.guiprotocol.GuiCommand.Command.Player;
import ca.site3.ssf.guiprotocol.GuiCommand.Command.PlayerAction;

/**
 * Utility methods for converting between enums etc. in the
 * GameModel and in the protobuf definitions
 * 
 * @author greg
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
		Collection<Player> players = new ArrayList<GuiCommand.Command.Player>(entities.size());
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
	
	
	
	static PlayerAction actionToProtobuf(PlayerActionType type) {
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
		case SONIC_BOOM_ATTACK:
			return PlayerAction.SONIC_BOOM_ATTACK;
		default:
			return null;
		}
	}
	
	static PlayerActionType actionToGame(PlayerAction action) {
		switch (action) {
		case BLOCK:
			return PlayerActionType.BLOCK;
		case HADOUKEN_ATTACK:
			return PlayerActionType.HADOUKEN_ATTACK;
		case HOOK_ATTACK:
			return PlayerActionType.HOOK_ATTACK;
		case JAB_ATTACK:
			return PlayerActionType.JAB_ATTACK;
		case UPPERCUT_ATTACK:
			return PlayerActionType.UPPERCUT_ATTACK;
		case SONIC_BOOM_ATTACK:
			return PlayerActionType.SONIC_BOOM_ATTACK;
		default:
			throw new IllegalArgumentException("Unknown player action: "+action);
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
	
	static GameStateType protobufToGameState(GameState gameState) {
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
		case TIE_BREAKER_ROUND_STATE:
			return GameStateType.TIE_BREAKER_ROUND_STATE;
		default:
			assert(false);
			throw new IllegalArgumentException("Unknown GameState: "+gameState);
		}
	}
	
	static GameState gameStateToProtobuf(GameStateType gameState) {
		assert(gameState != null);
		
		switch (gameState) {
		case IDLE_STATE:
			return GameState.IDLE_STATE;
		case MATCH_ENDED_STATE:
			return GameState.MATCH_ENDED_STATE;
		case NO_STATE:
			return GameState.NO_STATE;
		case PAUSED_STATE:
			return GameState.PAUSED_STATE;
		case RINGMASTER_STATE:
			return GameState.RINGMASTER_STATE;
		case ROUND_BEGINNING_STATE:
			return GameState.ROUND_BEGINNING_STATE;
		case ROUND_ENDED_STATE:
			return GameState.ROUND_ENDED_STATE;
		case ROUND_IN_PLAY_STATE:
			return GameState.ROUND_IN_PLAY_STATE;
		case TIE_BREAKER_ROUND_STATE:
			return GameState.TIE_BREAKER_ROUND_STATE;
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
		case RIGHT_HOOK:
			return AttackType.RIGHT_HOOK_ATTACK;
		case RIGHT_JAB:
			return AttackType.RIGHT_JAB_ATTACK;
		case RIGHT_UPPERCUT:
			return AttackType.RIGHT_UPPERCUT_ATTACK;
		case SONIC_BOOM:
			return AttackType.SONIC_BOOM_ATTACK;
		default:
			throw new IllegalArgumentException("Unrecognized protobuf attack type: "+t);
		}
	}
	
	static ca.site3.ssf.guiprotocol.Event.GameEvent.AttackType attackTypeToProtobuf(AttackType t) {
		switch (t) {
		case CUSTOM_UNDEFINED_ATTACK:
			return ca.site3.ssf.guiprotocol.Event.GameEvent.AttackType.CUSTOM_UNDEFINED;
		case HADOUKEN_ATTACK:
			return ca.site3.ssf.guiprotocol.Event.GameEvent.AttackType.HADOUKEN;
		case LEFT_HOOK_ATTACK:
			return ca.site3.ssf.guiprotocol.Event.GameEvent.AttackType.LEFT_HOOK;
		case LEFT_JAB_ATTACK:
			return ca.site3.ssf.guiprotocol.Event.GameEvent.AttackType.LEFT_JAB;
		case LEFT_UPPERCUT_ATTACK:
			return ca.site3.ssf.guiprotocol.Event.GameEvent.AttackType.LEFT_UPPERCUT;
		case RIGHT_HOOK_ATTACK:
			return ca.site3.ssf.guiprotocol.Event.GameEvent.AttackType.RIGHT_HOOK;
		case RIGHT_JAB_ATTACK:
			return ca.site3.ssf.guiprotocol.Event.GameEvent.AttackType.RIGHT_JAB;
		case RIGHT_UPPERCUT_ATTACK:
			return ca.site3.ssf.guiprotocol.Event.GameEvent.AttackType.RIGHT_UPPERCUT;
		case SONIC_BOOM_ATTACK:
			return ca.site3.ssf.guiprotocol.Event.GameEvent.AttackType.SONIC_BOOM;
		default:
			throw new IllegalArgumentException("Unrecognized AttackType: "+t);
		}
	}
	
}
