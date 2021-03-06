package ca.site3.ssf.guiprotocol;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.EnumSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.site3.ssf.gamemodel.ActionFactory;
import ca.site3.ssf.gamemodel.ExecutePlayerActionCommand;
import ca.site3.ssf.gamemodel.FireEmitter;
import ca.site3.ssf.gamemodel.FireEmitter.Location;
import ca.site3.ssf.gamemodel.FireEmitterChangedEvent;
import ca.site3.ssf.gamemodel.GameInfoRefreshEvent;
import ca.site3.ssf.gamemodel.GameState.GameStateType;
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
import ca.site3.ssf.gamemodel.RingmasterActionEvent;
import ca.site3.ssf.gamemodel.RoundBeginTimerChangedEvent;
import ca.site3.ssf.gamemodel.RoundEndedEvent;
import ca.site3.ssf.gamemodel.RoundPlayTimerChangedEvent;
import ca.site3.ssf.gamemodel.TogglePauseGameCommand;
import ca.site3.ssf.gamemodel.TouchFireEmitterCommand;
import ca.site3.ssf.gamemodel.UnrecognizedGestureEvent;
import ca.site3.ssf.guiprotocol.Event.GameEvent;
import ca.site3.ssf.guiprotocol.GuiCommand.Command;
import ca.site3.ssf.guiprotocol.GuiCommand.Command.Builder;
import ca.site3.ssf.guiprotocol.GuiCommand.Command.CommandType;

/**
 * Class for connecting to and communicating with a {@link StreetFireServer} 
 * over the network. This class is thread-safe.
 * 
 * The class also kicks off two threads once connected; one for sending
 * commands over the network and one dedicated to listening for events
 * from the server.
 * 
 * Calls to "command" methods ({@link #killGame()}, {@link #initiateNextState()}
 * etc.) will use the dedicated thread to send the commands.
 * 
 * Events from the server are placed in the {@link BlockingQueue}
 * returned by {@link #getEventQueue()}.
 * 
 * 
 * @author greg, Callum
 */
public class StreetFireGuiClient {

	private Logger log = LoggerFactory.getLogger(getClass());
	
	private int port;
	private InetAddress serverAddress;
	
	private boolean useSSL;
	
	private Socket socket;
	
	/** contains messages to be sent to server */
	private BlockingQueue<Command> commandQueue = new LinkedBlockingQueue<Command>();
	/** thread to monitor commandQueue and send messages to server */
	private SendThread sendThread;
	
	/** contains messages received from the server */
	private BlockingQueue<IGameModelEvent> eventQueue = new LinkedBlockingQueue<IGameModelEvent>();
	/** monitors socket for incoming events from server and places them on eventQueue */
	private ReceiveThread receiveThread;
	
	
	public StreetFireGuiClient(InetAddress ioserver, int port, boolean useSSL) {
		this.serverAddress = ioserver;
		this.port = port;
		this.useSSL = useSSL;
	}
	
	/**
	 * Connect to the IO Server. This must be called before any 
	 * attempts are made at communication. 
	 * 
	 * @return true if the connection was successful, false otherwise
	 * @throws IOException
	 */
	public boolean connect() throws IOException {
		//System.setProperty("javax.net.ssl.trustStore", "cacerts.jks");
    	//System.setProperty("javax.net.ssl.trustStorePassword", "changeit");
    	
		SocketFactory socketFactory = null;
		if (useSSL) {
			socketFactory = SSLSocketFactory.getDefault();
		} else {
			socketFactory = SocketFactory.getDefault();
		}
        socket = socketFactory.createSocket(serverAddress, port);
		
		if (socket.isConnected()) {
			sendThread = new SendThread();
			sendThread.start();
			receiveThread = new ReceiveThread();
			receiveThread.start();
			
			// Cause an immediate refresh of the game state for this client
			this.queryGameInfoRefresh();
		}
		
		return socket.isConnected();
	}
	
	public boolean isConnected() {
		if (socket == null) {
			return false;
		}
		return socket.isConnected();
	}
	
	
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// SENDING / (COMMANDS)
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	
	
	/**
	 * @return a queue that will be populated with events that come from the server
	 */
	public BlockingQueue<IGameModelEvent> getEventQueue() {
		return eventQueue;
	}
	
	public void queryGameInfoRefresh() throws IOException {
		Builder b = Command.newBuilder().setType(CommandType.QUERY_GAME_INFO_REFRESH);
		submitCommand(b.build());
	}
	
	/**
	 * @see KillGameCommand
	 * @throws IOException
	 */
	public void killGame() throws IOException {
		Builder b = Command.newBuilder().setType(CommandType.KILL_GAME);
		submitCommand(b.build());
	}
	
	/**
	 * @see InitiateNextStateCommand
	 * @throws IOException
	 */
	public void initiateNextState(GameStateType state) throws IOException {
		Builder b = Command.newBuilder().setType(CommandType.NEXT_STATE)
				.setNextState(SerializationHelper.gameStateToProtobuf(state));
		submitCommand(b.build());
	}
	
	/**
	 * @see TogglePauseGameCommand
	 * @throws IOException
	 */
	public void togglePauseGame() throws IOException {
		Builder b = Command.newBuilder().setType(CommandType.TOGGLE_PAUSE);
		submitCommand(b.build());
	}

	public void executeGenericAction(int playerNum, boolean usesLeftHand, boolean usesRightHand,
			                         float damagePerFlame, int flameWidth, double durationInSecs,
			                         double acceleration) throws IOException {
		Builder b = Command.newBuilder().setType(CommandType.EXECUTE_GENERIC_ACTION)
				.setPlayer(SerializationHelper.playerFromNum(playerNum))
				.setLeftHand(usesLeftHand)
				.setRightHand(usesRightHand)
				.setDmgPerFlame(damagePerFlame)
				.setFlameWidth(flameWidth)
				.setDurationInSeconds(durationInSecs)
				.setAcceleration(acceleration);
		
		submitCommand(b.build());
	}
	
	/**
	 * @see ExecutePlayerActionCommand
	 * @throws IOException
	 */
	public void executePlayerAction(int playerNum, 
									ActionFactory.ActionType playerActionType, 
									boolean usesLeftHand,
									boolean usesRightHand) throws IOException {
		
		if (!playerActionType.getIsPlayerAction()) {
			assert(false);
			return;
		}
		
		Builder b = Command.newBuilder().setType(CommandType.EXECUTE_PLAYER_ACTION)
					.setPlayer(SerializationHelper.playerFromNum(playerNum))
					.setPlayerAction(SerializationHelper.playerActionToProtobuf(playerActionType))
					.setLeftHand(usesLeftHand)
					.setRightHand(usesRightHand);
		submitCommand(b.build());
	}
	
	
	public void executeRingmasterAction(ActionFactory.ActionType ringmasterActionType,
										boolean usesLeftHand, boolean usesRightHand) throws IOException {
		
		if (ringmasterActionType.getIsPlayerAction()) {
			assert(false);
			return;
		}
		
		Builder b = Command.newBuilder().setType(CommandType.EXECUTE_RINGMASTER_ACTION)
				.setRingmasterAction(SerializationHelper.ringmasterActionToProtobuf(ringmasterActionType))
				.setLeftHand(usesLeftHand)
				.setRightHand(usesRightHand);
		submitCommand(b.build());
	}
	
	public void updatePlayerStatus(int playerNum, boolean unlimitedMovesOn) throws IOException {
		Builder b = Command.newBuilder().setType(CommandType.UPDATE_PLAYER_STATUS)
				.setPlayer(SerializationHelper.playerFromNum(playerNum))
				.setUnlimitedMovesOn(unlimitedMovesOn);
		submitCommand(b.build());
	}
	
	/**
	 * @see TouchFireEmitterCommand
	 * @throws IOException
	 */
	public void activateEmitter(Location location, 
								int index, 
								float intensity, 
								EnumSet<Entity> contributors) throws IOException {
		
		Builder b = Command.newBuilder().setType(CommandType.TOUCH_EMITTER)
				.setEmitterType(SerializationHelper.locationToProtobuf(location))
				.setEmitterIndex(index)
				.setIntensity(intensity)
				.addAllEmitterEntities(SerializationHelper.entitiesToProtobuf(contributors));
		submitCommand(b.build());
	}
	
	
	public void testSystem() throws IOException {
		Builder b = Command.newBuilder().setType(CommandType.QUERY_SYSTEM_INFO);
		submitCommand(b.build());
	}
	
	/**
	 * Sends the given command to the server.
	 * 
	 * @param cmd
	 * @throws IOException
	 */
	private void submitCommand(Command cmd) throws IOException {
		commandQueue.offer(cmd);
	}
	

	/**
	 * Monitors commandQueue and sends commands along to the server
	 */
	private class SendThread extends Thread {
		public SendThread() {
			super("GUI Client command sender");
		}
		
		@Override
		public void run() {
			while (true) {
				try {
					Command cmd = commandQueue.take();
					log.debug("SendThread " + System.identityHashCode(this) + " sending command: " + cmd.getType());
					cmd.writeDelimitedTo(socket.getOutputStream());
				}
				catch (InterruptedException ex) {
					log.warn("Interrupted waiting for a command",ex);
				}
				catch (IOException ex) {
					log.error("Exception sending data to server, marking as not connected",ex);
					if (socket != null) {
						try {
							socket.close();
						} catch (IOException ex2) {
							log.error("Exception trying to close socket!", ex);
						}
						socket = null;
					}
				}
				
				if (!isConnected()) {
					log.info("No longer connected; Stopped listening for commands");
					if (socket != null) {
						try {
							socket.close();
						}
						catch (IOException e) { }
						socket = null;
					}
					
					break;
				}
			}
		}
	}
	

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// RECEIVING / (EVENTS)
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	
	/**
	 * Listens for events coming from the server and puts then on the event queue.
	 */
	private class ReceiveThread extends Thread {
		public ReceiveThread() {
			super("GUI Client event receiver");
		}
		
		@Override
		public void run() {
			while (true) {
				try {
					GameEvent event = GameEvent.parseDelimitedFrom(socket.getInputStream());
					if (event != null) {
						IGameModelEvent gameEvent = parseEvent(event);
						if (gameEvent != null) {
							eventQueue.add(gameEvent);
						}
					}
				}
				catch (IOException ex) {
					log.warn("IOException listening on GUI input stream, marking as not connected", ex);
					if (socket != null) {
						try {
							socket.close();
						}
						catch (IOException ex2) {
							log.error("IOException trying to close socket");
						}
						socket = null;
					}
				} 
				catch (Exception ex) {
					log.error("Exception listening on GUI input stream",ex);
				}
				
				if (!isConnected()) {
					log.info("No longer connected; Stopped receiving events");
					if (socket != null) {
						try {
							socket.close();
						}
						catch (IOException e) { }
					}
					socket = null;
					break;
				}
			}
		}
	}
	
	
	private IGameModelEvent parseEvent(GameEvent e) {
		switch (e.getType()) {
		
		case GAME_INFO_REFRESH:
			return new GameInfoRefreshEvent(
				SerializationHelper.protobufToGameState(e.getGameState()),
				SerializationHelper.protobufToRoundResults(e.getRoundResultsList()),
				SerializationHelper.protobufToMatchResult(e.getMatchResult()),
				e.getPlayer1Health(), e.getPlayer2Health(),
				e.getPlayer1ActionPoints(), e.getPlayer2ActionPoints(),
				e.getPlayer1UnlimitedMovesOn(), e.getPlayer2UnlimitedMovesOn(),
				SerializationHelper.protobufToRoundBeginCountdownTimer(e.getRoundBeginTimer()),
				e.getRoundInPlayTimer(), e.getTimedOut());
		
		case SYSTEM_INFO_REFRESH:
			return SerializationHelper.protobufToBoardInfo(e.getBoardStatusList());
			
		case FIRE_EMITTER_CHANGED:
			return new FireEmitterChangedEvent(createFireEmitter(e));
			
		case GAME_STATE_CHANGED:
			return new GameStateChangedEvent(
				SerializationHelper.protobufToGameState(e.getOldGameState()), 
				SerializationHelper.protobufToGameState(e.getNewGameState()));
			
		case MATCH_ENDED:
			return new MatchEndedEvent(SerializationHelper.protobufToMatchResult(e.getMatchResult()), e.getPlayer1Health(), e.getPlayer2Health());

		case PLAYER_ATTACK_ACTION:
			return new PlayerAttackActionEvent(playerNumFromPlayer(e.getPlayer()), 
					SerializationHelper.protobufToAttackType(e.getAttackType()));
			
		case PLAYER_BLOCK_ACTION:
			return new PlayerBlockActionEvent(playerNumFromPlayer(e.getPlayer()), e.getBlockWasEffective());
			
		case PLAYER_HEALTH_CHANGED:
			return new PlayerHealthChangedEvent(playerNumFromPlayer(e.getPlayer()), 
					e.getOldHealth(), e.getNewHealth());
			
		case RINGMASTER_ACTION:
			return new RingmasterActionEvent(SerializationHelper.protobufToRingmasterAction(e.getRingmasterActionType()));
			
		case ROUND_BEGIN_TIMER_CHANGED:
			return new RoundBeginTimerChangedEvent(SerializationHelper.protobufToRoundBeginCountdownTimer(e.getRoundBeginTimer()), e.getRoundNumber());
			
		case ROUND_ENDED:
			return new RoundEndedEvent(e.getRoundNumber(), SerializationHelper.protobufToRoundResult(e.getRoundResult()), e.getTimedOut(), e.getPlayer1Health(), e.getPlayer2Health());
			
		case ROUND_PLAY_TIMER_CHANGED:
			return new RoundPlayTimerChangedEvent(e.getTimeInSecs());
		
		case UNRECOGNIZED_GESTURE:
			return new UnrecognizedGestureEvent(SerializationHelper.playerToGame(e.getPlayer()));
		
		case BLOCK_WINDOW:
			return new BlockWindowEvent(e.getBlockWindowID(), e.getBlockWindowHasExpired(),
					e.getBlockWindowTimeInSecs(), e.getBlockingPlayerNumber());
		
		case PLAYER_ACTION_POINTS_CHANGED:
			return new PlayerActionPointsChangedEvent(playerNumFromPlayer(e.getPlayer()), 
					e.getOldActionPoints(), e.getNewActionPoints());
		
		case PLAYER_ATTACK_ACTION_FAILED:
			return new PlayerAttackActionFailedEvent(playerNumFromPlayer(e.getPlayer()), 
					SerializationHelper.protobufToAttackType(e.getAttackType()), 
					SerializationHelper.protobufToAttackFailureReason(e.getAttackFailureReason()));
			
		default:
			log.error("Unknown GameEvent type: " + e.getType());
			return null;
		}
	}
	
	private int playerNumFromPlayer(Common.Player p) {
		if (p == Common.Player.RINGMASTER) {
			throw new IllegalArgumentException("Ringmaster is not a valid player");
		}
		return p == Common.Player.P1 ? 1 : 2;
	}
	
	private FireEmitter createFireEmitter(GameEvent e) {
		final ca.site3.ssf.guiprotocol.Event.GameEvent.FireEmitter fe = e.getEmitter();
		return new FireEmitter(0, fe.getEmitterIndex(), SerializationHelper.eventEmitterTypeToGame(fe.getEmitterType()) ) {
			@Override
			protected float getContributorIntensity(Entity contributor) {
				switch (contributor) {
				case PLAYER1_ENTITY:
					return fe.getIntensityPlayer1();
				case PLAYER2_ENTITY:
					return fe.getIntensityPlayer2();
				case RINGMASTER_ENTITY:
					return fe.getIntensityRingmaster();
				default:
					throw new IllegalArgumentException("Unknown entity: " + contributor);
				}
			}

			@Override
			protected EnumSet<Entity> getContributingEntities() {
				EnumSet<Entity> entities = EnumSet.noneOf(Entity.class);
				if (fe.getIntensityPlayer1() > MIN_INTENSITY) {
					entities.add(Entity.PLAYER1_ENTITY);
				}
				if (fe.getIntensityPlayer2() > MIN_INTENSITY) {
					entities.add(Entity.PLAYER2_ENTITY);
				}
				if (fe.getIntensityRingmaster() > MIN_INTENSITY) {
					entities.add(Entity.RINGMASTER_ENTITY);
				}
				return entities;
			}
		};
	}
	
}
