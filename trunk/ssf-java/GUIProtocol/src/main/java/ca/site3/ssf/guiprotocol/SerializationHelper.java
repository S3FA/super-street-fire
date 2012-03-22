package ca.site3.ssf.guiprotocol;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;

import ca.site3.ssf.gamemodel.ActionFactory.PlayerActionType;
import ca.site3.ssf.gamemodel.FireEmitter.Location;
import ca.site3.ssf.gamemodel.IGameModel.Entity;
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
	
}
