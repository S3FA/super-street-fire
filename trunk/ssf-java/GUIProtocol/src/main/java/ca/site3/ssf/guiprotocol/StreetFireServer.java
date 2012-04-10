package ca.site3.ssf.guiprotocol;


import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.EnumSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.site3.ssf.gamemodel.AbstractGameModelCommand;
import ca.site3.ssf.gamemodel.Action;
import ca.site3.ssf.gamemodel.ActionFactory;
import ca.site3.ssf.gamemodel.ActionFactory.PlayerActionType;
import ca.site3.ssf.gamemodel.ExecuteGenericActionCommand;
import ca.site3.ssf.gamemodel.ExecutePlayerActionCommand;
import ca.site3.ssf.gamemodel.FireEmitter.Location;
import ca.site3.ssf.gamemodel.FireEmitterChangedEvent;
import ca.site3.ssf.gamemodel.GameStateChangedEvent;
import ca.site3.ssf.gamemodel.IGameModel.Entity;
import ca.site3.ssf.gamemodel.IGameModelEvent;
import ca.site3.ssf.gamemodel.IGameModelEvent.Type;
import ca.site3.ssf.gamemodel.InitiateNextStateCommand;
import ca.site3.ssf.gamemodel.KillGameCommand;
import ca.site3.ssf.gamemodel.MatchEndedEvent;
import ca.site3.ssf.gamemodel.MatchEndedEvent.MatchResult;
import ca.site3.ssf.gamemodel.PlayerAttackActionEvent;
import ca.site3.ssf.gamemodel.PlayerBlockActionEvent;
import ca.site3.ssf.gamemodel.PlayerHealthChangedEvent;
import ca.site3.ssf.gamemodel.RoundBeginTimerChangedEvent;
import ca.site3.ssf.gamemodel.RoundEndedEvent;
import ca.site3.ssf.gamemodel.RoundPlayTimerChangedEvent;
import ca.site3.ssf.gamemodel.TogglePauseGameCommand;
import ca.site3.ssf.gamemodel.TouchFireEmitterCommand;
import ca.site3.ssf.guiprotocol.Event.GameEvent;
import ca.site3.ssf.guiprotocol.Event.GameEvent.EventType;
import ca.site3.ssf.guiprotocol.Event.GameEvent.FireEmitter;
import ca.site3.ssf.guiprotocol.Event.GameEvent.Player;
import ca.site3.ssf.guiprotocol.GuiCommand.Command;

/**
 * Accepts connections from a {@link StreetFireGuiClient} and handles
 * subsequent GUI commands, which are placed on the provided
 * command queue.
 * 
 * Also sends along {@link IGameModelEvent} events of interest to any
 * connected GUIs.
 * 
 * This class is thread-safe.
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
	
	private BlockingQueue<GameEvent> eventQueue = new LinkedBlockingQueue<Event.GameEvent>();
	
	/** thread to monitor eventQueue and send messages to GUIs */
	private SendThread sendThread;
	
	
	private Set<GuiHandler> activeHandlers = new CopyOnWriteArraySet<StreetFireServer.GuiHandler>();
	
	
	
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
		
		sendThread = new SendThread();
		sendThread.start();
		
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
	
	
	/**
	 * Monitors eventQueue and sends commands along to the server
	 */
	private class SendThread extends Thread {
		@Override
		public void run() {
			while (true) {
				try {
					GameEvent event = eventQueue.take();
					
					for (GuiHandler guiHandler : activeHandlers) {
						try {
							guiHandler.sendGameEvent(event);
						} catch (IOException ex) {
							log.error("Exception sending GameEvent {} to GUI client",event);
						}
					}
				} catch (InterruptedException ex) {
					log.warn("Interrupted waiting for an event",ex);
				}
			}
		}
	}
	
	public void notifyGUI(IGameModelEvent e) {
		eventQueue.offer(eventToProtobuf(e));
	}
	

	private static GameEvent eventToProtobuf(IGameModelEvent evt) {
		GameEvent.Builder b = GameEvent.newBuilder();
		if (evt.getType() == Type.FireEmitterChanged) {
			FireEmitterChangedEvent e = (FireEmitterChangedEvent)evt;
			FireEmitter emitter = FireEmitter.newBuilder()
				.setEmitterIndex(e.getIndex())
				.setEmitterType(SerializationHelper.locationToEventProtobuf(e.getLocation()))
				.setIntensityPlayer1(e.getIntensity(Entity.PLAYER1_ENTITY))
				.setIntensityPlayer2(e.getIntensity(Entity.PLAYER2_ENTITY))
				.setIntensityRingmaster(e.getIntensity(Entity.RINGMASTER_ENTITY)).build();
			b.setType(EventType.FireEmitterChanged)
				.setEmitter(emitter);
		} else if (evt.getType() == Type.GameStateChanged) {
			GameStateChangedEvent e = (GameStateChangedEvent)evt;
			b.setType(EventType.GameStateChanged)
				.setOldGameState(SerializationHelper.gameStateToProtobuf(e.getOldState()))
				.setNewGameState(SerializationHelper.gameStateToProtobuf(e.getNewState()));
		} else if (evt.getType() == Type.MatchEnded) {
			MatchEndedEvent e = (MatchEndedEvent)evt;
			b.setType(EventType.MatchEnded)
				.setMatchWinner(e.getMatchResult() == MatchResult.PLAYER1_VICTORY ? Player.P1 : Player.P2);
		} else if (evt.getType() == Type.PlayerAttackAction) {
			PlayerAttackActionEvent e = (PlayerAttackActionEvent)evt;
			b.setType(EventType.PlayerAttackAction)
				.setPlayer(e.getPlayerNum() == 1 ? Player.P1 : Player.P2)
				.setAttackType(SerializationHelper.attackTypeToProtobuf(e.getAttackType()));
		} else if (evt.getType() == Type.PlayerBlockAction) {
			PlayerBlockActionEvent e = (PlayerBlockActionEvent)evt;
			b.setType(EventType.PlayerBlockAction)
				.setPlayer(e.getPlayerNum() == 1 ? Player.P1 : Player.P2);
		} else if (evt.getType() == Type.PlayerHealthChanged) {
			PlayerHealthChangedEvent e = (PlayerHealthChangedEvent)evt;
			b.setType(EventType.PlayerHealthChanged)
				.setPlayer(e.getPlayerNum() == 1 ? Player.P1 : Player.P2)
				.setOldHealth(e.getPrevLifePercentage())
				.setNewHealth(e.getNewLifePercentage());
		} else if (evt.getType() == Type.RingmasterAction) {
			//RingmasterActionEvent e = (RingmasterActionEvent)evt;
			b.setType(EventType.RingmasterAction);
		} else if (evt.getType() == Type.RoundBeginTimerChanged) {
			RoundBeginTimerChangedEvent e = (RoundBeginTimerChangedEvent)evt;
			b.setType(EventType.RoundBeginTimerChanged)
				.setRoundNumber(e.getRoundNumber())
				.setBeginType(SerializationHelper.beginTypeToProtobuf(e.getThreeTwoOneFightTime()));
		} else if (evt.getType() == Type.RoundEnded) {
			RoundEndedEvent e = (RoundEndedEvent)evt;
			b.setType(EventType.RoundEnded)
				.setRoundNumber(e.getRoundNumber())
				.setTimedOut(e.getRoundTimedOut())
				.setRoundWinner(SerializationHelper.roundWinnerProtobuf(e.getRoundResult()));
		} else if (evt.getType() == Type.RoundPlayTimerChanged) {
			RoundPlayTimerChangedEvent e = (RoundPlayTimerChangedEvent)evt;
			b.setType(EventType.RoundPlayTimerChanged)
				.setTimeInSecs(e.getTimeInSecs());
		} else {
			throw new IllegalArgumentException("Unknown game event type: "+evt.getType());
		}
		return b.build();
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
			
			activeHandlers.remove(this);
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
		
		
		
		private void sendGameEvent(GameEvent event) throws IOException {
			event.writeDelimitedTo(socket.getOutputStream());
		}
	}
}
