package ca.site3.ssf.guiprotocol;


import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.site3.ssf.gamemodel.AbstractGameModelCommand;
import ca.site3.ssf.gamemodel.Action;
import ca.site3.ssf.gamemodel.ActionFactory;
import ca.site3.ssf.gamemodel.ActionFactory.PlayerActionType;
import ca.site3.ssf.gamemodel.ExecuteGenericActionCommand;
import ca.site3.ssf.gamemodel.ExecutePlayerActionCommand;
import ca.site3.ssf.gamemodel.FireEmitter.Location;
import ca.site3.ssf.gamemodel.IGameModel.Entity;
import ca.site3.ssf.gamemodel.InitiateNextStateCommand;
import ca.site3.ssf.gamemodel.KillGameCommand;
import ca.site3.ssf.gamemodel.TogglePauseGameCommand;
import ca.site3.ssf.gamemodel.TouchFireEmitterCommand;
import ca.site3.ssf.guiprotocol.GuiCommand.Command;

/**
 * Accepts connections from a {@link StreetFireGuiClient} and handles
 * subsequent GUI messages.
 * 
 * @author greg
 */
public class StreetFireServer implements Runnable {

	private Logger log = LoggerFactory.getLogger(getClass());
	
	private int port;
	
	private volatile boolean stop = false;
	
	private ActionFactory actionFactory;
	
	private ServerSocket socket;
	
	private Queue<AbstractGameModelCommand> commandQueue;
	
	private Set<GuiHandler> activeHandlers = new HashSet<StreetFireServer.GuiHandler>();
	
	
	public StreetFireServer(int port, ActionFactory actionFactory, Queue<AbstractGameModelCommand> commandQueue) {
		this.port = port;
		this.actionFactory = actionFactory;
		this.commandQueue = commandQueue;
	}
	

	
	public void run() {
		//SSLServerSocketFactory f = (SSLServerSocketFactory)SSLServerSocketFactory.getDefault();
		
		try {
			//socket = f.createServerSocket(port);
			socket = new ServerSocket(port);
		} catch (SocketException ex) {
			log.error("Exception setting timeout on server socket",ex);
		} catch (IOException ex) {
			log.error("Could not create server socket",ex);
			return;
		}
		
		while ( ! stop ) {
			try {
				Socket s = socket.accept();
				log.info("Accepted connection from "+s.getInetAddress());
				GuiHandler handler = new GuiHandler(s);
				activeHandlers.add(handler);
				Thread t = new Thread(handler, "GuiHandler - "+handler.hashCode());
				t.start();
			} catch (SocketTimeoutException ex) {
				log.info("Server socket timeout",ex);
			} catch (IOException ex) {
				log.warn("Exception listening on server socket",ex);
			}
		}
		
		for (GuiHandler h : activeHandlers) {
			h.stop();
		}
	}
	
	
	public void stop() {
		stop = true;
	}
	
	
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// GUI Communication Handler
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	
	/**
	 * Listens for commands from a {@link StreetFireGuiClient}, decodes them and schedules 
	 * them for execution.
	 * 
	 * Also passes game events to the GUI as appropriate.
	 * 
	 * 
	 * @author greg
	 */
	private class GuiHandler implements Runnable {
	
		private final Socket socket;
		
		private volatile boolean shouldListen = true;
		
		
		public GuiHandler(Socket s) {
			this.socket = s;
		}
		
		
		public void run() {
			while ( shouldListen ) {
				try {
					Command cmd = Command.parseDelimitedFrom(socket.getInputStream());
					if (cmd != null) {
						AbstractGameModelCommand gameCmd = parseCommand(cmd); 
						if (gameCmd != null) {
							commandQueue.add(gameCmd);
						}
					}
				} catch (IOException ex) {
					log.warn("IOException listening on GUI input stream",ex);
				} catch (Exception ex) {
					log.error("Exception listening on GUI input stream",ex);
				}
			}
			
			if (socket.isClosed() == false) {
				try {
					socket.close();
				} catch (IOException ex) {
					log.warn("Exception closing GuiHandler socket",ex);
				}
			}
		}
		
		
		public void stop() {
			this.shouldListen = false;
			
			if (socket.isClosed() == false) {
				try {
					socket.close();
				} catch (IOException ex) {
					log.error("Exception closing GUI handler socket",ex);
				}
			}
		}
		
		
		private AbstractGameModelCommand parseCommand(Command cmd) {
			switch (cmd.getType()) {
			case EXECUTE_GENERIC_ACTION:
				return createGenericActionCommand(cmd);
			case EXECUTE_PLAYER_ACTION:
				return createPlayerActionCommand(cmd);
			case KILL_GAME:
				return createKillCommand(cmd);
			case NEXT_STATE:
				return createNextStateCommand(cmd);
			case TOGGLE_PAUSE:
				return createTogglePauseCommand(cmd);
			case TOUCH_EMITTER:
				return createFireEmitterCommand(cmd);
			default:
				log.warn("Unhandled command type: "+cmd.getType());
				return null;
			}
		}
		
		
		private ExecuteGenericActionCommand createGenericActionCommand(Command cmd) {
			int playerNum = SerializationHelper.playerToNum(cmd.getPlayer());
			PlayerActionType actionType = SerializationHelper.actionToGame(cmd.getPlayerAction());
			Action a = actionFactory.buildPlayerAction(playerNum, actionType, cmd.getLeftHand(), cmd.getRightHand());
			return new ExecuteGenericActionCommand(a);
		}
		
		
		private ExecutePlayerActionCommand createPlayerActionCommand(Command cmd) {
			int playerNum = SerializationHelper.playerToNum(cmd.getPlayer());
			PlayerActionType type = SerializationHelper.actionToGame(cmd.getPlayerAction());
			return new ExecutePlayerActionCommand(playerNum, type, cmd.getLeftHand(), cmd.getRightHand());
		}
		
		
		private InitiateNextStateCommand createNextStateCommand(Command cmd) {
			return new InitiateNextStateCommand();
		}
		
		
		private KillGameCommand createKillCommand(Command cmd) {
			return new KillGameCommand();
		}
		
		
		private TogglePauseGameCommand createTogglePauseCommand(Command cmd) {
			return new TogglePauseGameCommand();
		}
		
		
		private TouchFireEmitterCommand createFireEmitterCommand(Command cmd) {
			Location location = SerializationHelper.emitterTypeToGame(cmd.getEmitterType());
			EnumSet<Entity> contributors = SerializationHelper.playersToGame(cmd.getEmitterEntitiesList());
			return new TouchFireEmitterCommand(location, cmd.getEmitterIndex(), cmd.getIntensity(), contributors);
		}
	}
}
