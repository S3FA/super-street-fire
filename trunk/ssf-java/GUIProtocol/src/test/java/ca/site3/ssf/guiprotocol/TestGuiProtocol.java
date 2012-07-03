package ca.site3.ssf.guiprotocol;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.Queue;

import org.junit.Test;

import ca.site3.ssf.gamemodel.AbstractGameModelCommand;
import ca.site3.ssf.gamemodel.ActionFactory.ActionType;
import ca.site3.ssf.gamemodel.FireEmitter.Location;
import ca.site3.ssf.gamemodel.GameState.GameStateType;
import ca.site3.ssf.gamemodel.GameConfig;
import ca.site3.ssf.gamemodel.GameModel;
import ca.site3.ssf.gamemodel.IGameModel;
import ca.site3.ssf.gamemodel.IGameModel.Entity;

public class TestGuiProtocol {

	
	@Test
	public void testCommands() {
		
		int port = 31337;
		InetAddress localhost = null;
		try {
			localhost = InetAddress.getLocalHost();
		} catch (UnknownHostException ex) {
			fail(ex.getMessage());
		}
		
		
		IGameModel game = new GameModel(new GameConfig(true,3,60,3,0.1f));
		Queue<AbstractGameModelCommand> commandQueue = new LinkedList<AbstractGameModelCommand>();
		
		StreetFireServer server = new StreetFireServer(port, game.getActionFactory(), commandQueue);
		Thread serverThread = new Thread(server);
		serverThread.start();
		try { Thread.sleep(500); } catch (InterruptedException ex) { ex.printStackTrace(); }
		
		StreetFireGuiClient client = new StreetFireGuiClient(localhost, port);
		try {
			client.connect();
			
			// these are very weak tests right now
			client.activateEmitter(Location.LEFT_RAIL, 3, 0.5f, EnumSet.of(Entity.RINGMASTER_ENTITY));
			client.initiateNextState(GameStateType.RINGMASTER_STATE);
			client.executePlayerAction(1, ActionType.HADOUKEN_ATTACK, true, true);
			client.killGame();
			
		} catch (IOException ex) {
			fail(ex.getMessage());
		}
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException ex) {
			ex.printStackTrace();
		}
		
		server.stop();
		
		// add 1 to expected number because client automatically sends refresh command
		assertEquals("Not all commands made it to queue", 4 + 1, commandQueue.size());
	}

	
	@Test
	public void testEvents() {
		
	}
}
