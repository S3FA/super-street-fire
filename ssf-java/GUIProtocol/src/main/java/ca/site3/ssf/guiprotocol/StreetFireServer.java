package ca.site3.ssf.guiprotocol;


import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.LinkedBlockingQueue;

import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocketFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.site3.ssf.gamemodel.AbstractGameModelCommand;
import ca.site3.ssf.gamemodel.Action;
import ca.site3.ssf.gamemodel.ActionFactory;
import ca.site3.ssf.gamemodel.ActionFactory.ActionType;
import ca.site3.ssf.gamemodel.ExecuteGenericActionCommand;
import ca.site3.ssf.gamemodel.ExecutePlayerActionCommand;
import ca.site3.ssf.gamemodel.ExecuteRingmasterActionCommand;
import ca.site3.ssf.gamemodel.FireEmitter.Location;
import ca.site3.ssf.gamemodel.FireEmitterChangedEvent;
import ca.site3.ssf.gamemodel.GameInfoRefreshEvent;
import ca.site3.ssf.gamemodel.GameStateChangedEvent;
import ca.site3.ssf.gamemodel.IGameModel.Entity;
import ca.site3.ssf.gamemodel.BlockWindowEvent;
import ca.site3.ssf.gamemodel.IGameModelEvent;
import ca.site3.ssf.gamemodel.InitiateNextStateCommand;
import ca.site3.ssf.gamemodel.KillGameCommand;
import ca.site3.ssf.gamemodel.MatchEndedEvent;
import ca.site3.ssf.gamemodel.PlayerActionPointsChangedEvent;
import ca.site3.ssf.gamemodel.PlayerAttackActionEvent;
import ca.site3.ssf.gamemodel.PlayerAttackActionFailedEvent;
import ca.site3.ssf.gamemodel.PlayerBlockActionEvent;
import ca.site3.ssf.gamemodel.PlayerHealthChangedEvent;
import ca.site3.ssf.gamemodel.PlayerStatusUpdateCommand;
import ca.site3.ssf.gamemodel.QueryGameInfoRefreshCommand;
import ca.site3.ssf.gamemodel.RingmasterActionEvent;
import ca.site3.ssf.gamemodel.RoundBeginTimerChangedEvent;
import ca.site3.ssf.gamemodel.RoundEndedEvent;
import ca.site3.ssf.gamemodel.RoundPlayTimerChangedEvent;
import ca.site3.ssf.gamemodel.SystemInfoRefreshEvent;
import ca.site3.ssf.gamemodel.TogglePauseGameCommand;
import ca.site3.ssf.gamemodel.TouchFireEmitterCommand;
import ca.site3.ssf.gamemodel.UnrecognizedGestureEvent;
import ca.site3.ssf.guiprotocol.Event.GameEvent;
import ca.site3.ssf.guiprotocol.Event.GameEvent.EventType;
import ca.site3.ssf.guiprotocol.Event.GameEvent.FireEmitter;
import ca.site3.ssf.guiprotocol.GuiCommand.Command;
import ca.site3.ssf.guiprotocol.GuiCommand.Command.CommandType;
import ca.site3.ssf.guiprotocol.SystemCommand.SystemCommandType;

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
 * @author greg, Callum
 */
public class StreetFireServer implements Runnable {

	private Logger log = LoggerFactory.getLogger(getClass());
	
	private int port;
	
	private volatile boolean stop = false;
	
	private ActionFactory actionFactory;
	
	private boolean useSSL;
	
	private ServerSocket serverSocket;
	
	private Queue<AbstractGameModelCommand> gameCommandQueue;
	
	private Queue<SystemCommand> systemCommandQueue;
	
	private BlockingQueue<GameEvent> eventQueue = new LinkedBlockingQueue<Event.GameEvent>();
	
	/** thread to monitor eventQueue and send messages to GUIs */
	private SendThread sendThread;
	
	
	private Set<GuiHandler> activeHandlers = new CopyOnWriteArraySet<StreetFireServer.GuiHandler>();
	
	
	
	public StreetFireServer(int port, boolean useSSL, ActionFactory actionFactory, Queue<AbstractGameModelCommand> gameQueue, Queue<SystemCommand> systemQueue) {
		this.port = port;
		this.useSSL = useSSL;
		this.actionFactory = actionFactory;
		this.gameCommandQueue = gameQueue;
		this.systemCommandQueue = systemQueue;
	}
	
	public void run() {
		ServerSocketFactory ssFactory = null;
		if (useSSL) {
			System.setProperty("javax.net.ssl.keyStore", "../GUIProtocol/Certificates/keystore.jks");
	 	    System.setProperty("javax.net.ssl.keyStorePassword", "changeit");
	 	    System.setProperty("javax.net.ssl.trustStore", "../GUIProtocol/Certificates/cacerts.jks");
	 	    System.setProperty("javax.net.ssl.trustStorePassword", "changeit");
	
	 	    ssFactory = SSLServerSocketFactory.getDefault();
		} else {
			ssFactory = ServerSocketFactory.getDefault();
		}
		
		try {
			serverSocket = ssFactory.createServerSocket(port);
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
				// Accept incoming connections from GUI clients
				Socket s = serverSocket.accept();
				log.info("Accepted connection from " + s.getInetAddress());
				
				// A connection has been accepted, build a handler for the new connection and start it up
				GuiHandler handler = new GuiHandler(s);
				activeHandlers.add(handler);
				Thread t = new Thread(handler, "GuiHandler - " + handler.hashCode());
				t.start();
				// Don't touch the socket ('s') again, leave it to the new thread.
				
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
	 * Monitors eventQueue and sends commands along to the clients.
	 */
	private class SendThread extends Thread {
		@Override
		
		public void run() {
			boolean isDone = false;
			while (!isDone) {
				
				if (!serverSocket.isBound() || serverSocket.isClosed()) {
					isDone = true;
					continue;
				}
				Set<GuiHandler> droppedConnections = new HashSet<StreetFireServer.GuiHandler>();
				try {
					// Get the next event that occurred in the gamemodel on the server
					GameEvent event = eventQueue.take();
					
					// Go through and send the event to each client GUI
					Iterator<GuiHandler> iter = activeHandlers.iterator();
					while (iter.hasNext()) {
						GuiHandler guiHandler = iter.next();
						try {
							guiHandler.sendGameEvent(event);
						}
						catch (IOException ex) {
							log.warn("Exception sending GameEvent to GUI client. Removing handler", ex);
							droppedConnections.add(guiHandler);
						}
						
						if (!serverSocket.isBound() || serverSocket.isClosed()) {
							isDone = true;
							break;
						}
					}
					
				} catch (InterruptedException ex) {
					log.warn("Interrupted waiting for an event", ex);
				}
				
				activeHandlers.removeAll(droppedConnections);
				
				// Kill all the dropped connections
				Iterator<GuiHandler> droppedIter = droppedConnections.iterator();
				while (droppedIter.hasNext()) {
					GuiHandler droppedHandler = droppedIter.next();
					droppedHandler.stop();
				}
				
			}
			
			// Make sure we tell the server that it's no longer viable, stop its socket accepting thread.
			stop = true;
		}
	}
	
	/**
	 * Places the game model event in a queue to be sent to the GUI via
	 * the Server thread.
	 * @param e
	 */
	public void notifyGUI(IGameModelEvent e) {
		eventQueue.offer(eventToProtobuf(e));
	}
	
	private static GameEvent eventToProtobuf(IGameModelEvent evt) {
		GameEvent.Builder b = GameEvent.newBuilder();
		
		switch (evt.getType()) {
		
		case GAME_INFO_REFRESH: {
			GameInfoRefreshEvent e = (GameInfoRefreshEvent)evt;
			List<RoundEndedEvent.RoundResult> roundResults = e.getCurrentRoundResults();
			b.setType(EventType.GAME_INFO_REFRESH)
				.setGameState(SerializationHelper.gameStateToProtobuf(e.getCurrentGameState()))
				.addAllRoundResults(SerializationHelper.roundResultsToProtobuf(roundResults))
				.setMatchResult(SerializationHelper.matchResultToProtobuf(e.getMatchResult()))
				.setPlayer1Health(e.getPlayer1Health())
				.setPlayer2Health(e.getPlayer2Health())
				.setPlayer1ActionPoints(e.getPlayer1ActionPoints())
				.setPlayer2ActionPoints(e.getPlayer2ActionPoints())
				.setRoundBeginTimer(SerializationHelper.roundBeginCountdownTimerToProtobuf(e.getRoundBeginCountdown()))
				.setRoundInPlayTimer(e.getRoundInPlayTimer())
				.setTimedOut(e.getRoundTimedOut());
			break;
		}
		
		case FIRE_EMITTER_CHANGED: {
			FireEmitterChangedEvent e = (FireEmitterChangedEvent)evt;
			FireEmitter emitter = FireEmitter.newBuilder()
				.setEmitterIndex(e.getIndex())
				.setEmitterType(SerializationHelper.locationToEventProtobuf(e.getLocation()))
				.setIntensityPlayer1(e.getIntensity(Entity.PLAYER1_ENTITY))
				.setIntensityPlayer2(e.getIntensity(Entity.PLAYER2_ENTITY))
				.setIntensityRingmaster(e.getIntensity(Entity.RINGMASTER_ENTITY)).build();
			b.setType(EventType.FIRE_EMITTER_CHANGED)
				.setEmitter(emitter);
			break;
		}
		
		case GAME_STATE_CHANGED: {
			GameStateChangedEvent e = (GameStateChangedEvent)evt;
			b.setType(EventType.GAME_STATE_CHANGED)
				.setOldGameState(SerializationHelper.gameStateToProtobuf(e.getOldState()))
				.setNewGameState(SerializationHelper.gameStateToProtobuf(e.getNewState()));
			break;
		}
		
		case MATCH_ENDED: {
			MatchEndedEvent e = (MatchEndedEvent)evt;
			b.setType(EventType.MATCH_ENDED)
				.setMatchResult(SerializationHelper.matchResultToProtobuf(e.getMatchResult()));
			break;
		}
		
		case PLAYER_ATTACK_ACTION: {
			PlayerAttackActionEvent e = (PlayerAttackActionEvent)evt;
			b.setType(EventType.PLAYER_ATTACK_ACTION)
				.setPlayer(e.getPlayerNum() == 1 ? Common.Player.P1 : Common.Player.P2)
				.setAttackType(SerializationHelper.attackTypeToProtobuf(e.getAttackType()));
			break;
		}
		
		case PLAYER_BLOCK_ACTION: {
			PlayerBlockActionEvent e = (PlayerBlockActionEvent)evt;
			b.setType(EventType.PLAYER_BLOCK_ACTION)
				.setPlayer(e.getPlayerNum() == 1 ? Common.Player.P1 : Common.Player.P2)
				.setBlockWasEffective(e.getBlockWasEffective());
			break;
		}
		
		case PLAYER_HEALTH_CHANGED: {
			PlayerHealthChangedEvent e = (PlayerHealthChangedEvent)evt;
			b.setType(EventType.PLAYER_HEALTH_CHANGED)
				.setPlayer(e.getPlayerNum() == 1 ? Common.Player.P1 : Common.Player.P2)
				.setOldHealth(e.getPrevLifePercentage())
				.setNewHealth(e.getNewLifePercentage());
			break;
		}
		
		case RINGMASTER_ACTION: {
			RingmasterActionEvent e = (RingmasterActionEvent)evt;
			b.setType(EventType.RINGMASTER_ACTION);
			b.setRingmasterActionType(SerializationHelper.ringmasterActionTypeToProtobuf(e.getActionType()));
			break;
		}
		
		case ROUND_BEGIN_TIMER_CHANGED: {
			RoundBeginTimerChangedEvent e = (RoundBeginTimerChangedEvent)evt;
			b.setType(EventType.ROUND_BEGIN_TIMER_CHANGED)
				.setRoundNumber(e.getRoundNumber())
				.setRoundBeginTimer(SerializationHelper.roundBeginCountdownTimerToProtobuf(e.getThreeTwoOneFightTime()));
			break;
		}
		
		case ROUND_ENDED: {
			RoundEndedEvent e = (RoundEndedEvent)evt;
			b.setType(EventType.ROUND_ENDED)
				.setRoundNumber(e.getRoundNumber())
				.setTimedOut(e.getRoundTimedOut())
				.setRoundResult(SerializationHelper.roundResultToProtobuf(e.getRoundResult()));
			break;
		}
		
		case ROUND_PLAY_TIMER_CHANGED: {
			RoundPlayTimerChangedEvent e = (RoundPlayTimerChangedEvent)evt;
			b.setType(EventType.ROUND_PLAY_TIMER_CHANGED)
				.setTimeInSecs(e.getTimeInSecs());
			break;
		}
		
		case UNRECOGNIZED_GESTURE: {
			UnrecognizedGestureEvent e = (UnrecognizedGestureEvent)evt;
			b.setType(EventType.UNRECOGNIZED_GESTURE)
				.setPlayer(SerializationHelper.entityToProtobuf(e.getEntity()));
			break;
		}
		
		case BLOCK_WINDOW: {
			BlockWindowEvent e = (BlockWindowEvent)evt;
			b.setType(EventType.BLOCK_WINDOW)
				.setBlockWindowID(e.getBlockWindowID())
				.setBlockWindowHasExpired(e.getHasBlockWindowExpired())
				.setBlockWindowTimeInSecs(e.getBlockWindowTimeLengthInSeconds())
				.setBlockingPlayerNumber(e.getBlockingPlayerNumber());
			break;
		}
		
		case PLAYER_ACTION_POINTS_CHANGED: {
			PlayerActionPointsChangedEvent e = (PlayerActionPointsChangedEvent)evt;
			b.setType(EventType.PLAYER_ACTION_POINTS_CHANGED)
				.setPlayer(e.getPlayerNum() == 1 ? Common.Player.P1 : Common.Player.P2)
				.setOldActionPoints(e.getPrevActionPointAmt())
				.setNewActionPoints(e.getNewActionPointAmt());
			break;
		}
		
		case PLAYER_ATTACK_ACTION_FAILED: {
			PlayerAttackActionFailedEvent e = (PlayerAttackActionFailedEvent)evt;
			b.setType(EventType.PLAYER_ATTACK_ACTION_FAILED)
				.setPlayer(e.getPlayerNum() == 1 ? Common.Player.P1 : Common.Player.P2)
				.setAttackType(SerializationHelper.attackTypeToProtobuf(e.getAttackType()))
				.setAttackFailureReason(SerializationHelper.attackFailureReasonToProtobuf(e.getReason()));
			break;
		}
		
		case SYSTEM_INFO_REFRESH: {
			SystemInfoRefreshEvent e = (SystemInfoRefreshEvent)evt;
			b.setType(EventType.SYSTEM_INFO_REFRESH)
				.addAllBoardStatus(SerializationHelper.boardInfoToProtobuf(e));
			break;
		}
		default:
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
				
				if (!this.socket.isBound() || this.socket.isClosed()) {
					shouldListen = false;
					continue;
				}
				
				try {
					Command cmd = Command.parseDelimitedFrom(socket.getInputStream());

					if (cmd != null) {
						if (cmd.getType() == CommandType.QUERY_SYSTEM_INFO) {
							systemCommandQueue.add(new SystemCommand(SystemCommandType.QUERY_SYSTEM_INFO));
						}
						else {
						
							AbstractGameModelCommand gameCmd = parseCommand(cmd); 
							if (gameCmd != null) {
								gameCommandQueue.add(gameCmd);
							}
						}
					}
				}
				catch (SocketException ex) {
					log.warn("SocketException, closing connection with GUI", ex);
					shouldListen = false;
					break;
				}
				catch (IOException ex) {
					log.warn("IOException listening on GUI input stream", ex);	
				} 
				catch (Exception ex) {
					log.error("Exception listening on GUI input stream", ex);
				}
			}
			
			if (socket.isClosed() == false) {
				try {
					socket.close();
				} catch (IOException ex) {
					log.warn("Exception closing GuiHandler socket", ex);
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
			case EXECUTE_RINGMASTER_ACTION:
				return createRingmasterActionCommand(cmd);
			case KILL_GAME:
				return createKillCommand(cmd);
			case NEXT_STATE:
				return createNextStateCommand(cmd);
			case TOGGLE_PAUSE:
				return createTogglePauseCommand(cmd);
			case TOUCH_EMITTER:
				return createFireEmitterCommand(cmd);
			case QUERY_GAME_INFO_REFRESH:
				return createQueryGameInfoRefreshCommand(cmd);
			case UPDATE_PLAYER_STATUS:
				return createPlayerStatusUpdateCommand(cmd);
			default:
				log.warn("Unhandled command type: "+cmd.getType());
				return null;
			}
		}
		
		private ExecuteGenericActionCommand createGenericActionCommand(Command cmd) {
			int playerNum         = SerializationHelper.playerToNum(cmd.getPlayer());
			int flameWidth        = cmd.getFlameWidth();
			float dmgPerFlame     = cmd.getDmgPerFlame();
			double durationInSecs = cmd.getDurationInSeconds();
			double acceleration   = cmd.getAcceleration();
			
			Action a = actionFactory.buildCustomPlayerAttackAction(playerNum, 
					flameWidth, dmgPerFlame, acceleration, cmd.getLeftHand(), cmd.getRightHand(),
					durationInSecs, ActionFactory.DEFAULT_FULL_ON_FRACTION, ActionFactory.DEFAULT_FULL_OFF_FRACTION);
			return new ExecuteGenericActionCommand(a);
		}
		
		
		private ExecutePlayerActionCommand createPlayerActionCommand(Command cmd) {
			int playerNum = SerializationHelper.playerToNum(cmd.getPlayer());
			ActionType type = SerializationHelper.playerActionToGame(cmd.getPlayerAction());
			return new ExecutePlayerActionCommand(playerNum, type, cmd.getLeftHand(), cmd.getRightHand());
		}
		
		private ExecuteRingmasterActionCommand createRingmasterActionCommand(Command cmd) {
			ActionType type = SerializationHelper.ringmasterActionToGame(cmd.getRingmasterAction());
			return new ExecuteRingmasterActionCommand(type, cmd.getLeftHand(), cmd.getRightHand());
		}
		
		private InitiateNextStateCommand createNextStateCommand(Command cmd) {
			return new InitiateNextStateCommand(SerializationHelper.protobufToGameState(cmd.getNextState()));
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
		
		private QueryGameInfoRefreshCommand createQueryGameInfoRefreshCommand(Command cmd) {
			return new QueryGameInfoRefreshCommand();
		}

		private PlayerStatusUpdateCommand createPlayerStatusUpdateCommand(Command cmd) {
			int playerNum = SerializationHelper.playerToNum(cmd.getPlayer());
			return new PlayerStatusUpdateCommand(playerNum, cmd.getUnlimitedMovesOn());
		}
		
		private void sendGameEvent(GameEvent event) throws IOException {
			event.writeDelimitedTo(socket.getOutputStream());
		}
	}
}
