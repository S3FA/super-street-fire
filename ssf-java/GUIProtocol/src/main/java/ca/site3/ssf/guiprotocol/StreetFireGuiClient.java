package ca.site3.ssf.guiprotocol;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.EnumSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.site3.ssf.gamemodel.ActionFactory;
import ca.site3.ssf.gamemodel.ExecutePlayerActionCommand;
import ca.site3.ssf.gamemodel.FireEmitter;
import ca.site3.ssf.gamemodel.FireEmitter.Location;
import ca.site3.ssf.gamemodel.FireEmitterChangedEvent;
import ca.site3.ssf.gamemodel.GameStateChangedEvent;
import ca.site3.ssf.gamemodel.IGameModel.Entity;
import ca.site3.ssf.gamemodel.IGameModelEvent;
import ca.site3.ssf.gamemodel.InitiateNextStateCommand;
import ca.site3.ssf.gamemodel.KillGameCommand;
import ca.site3.ssf.gamemodel.MatchEndedEvent;
import ca.site3.ssf.gamemodel.PlayerHealthChangedEvent;
import ca.site3.ssf.gamemodel.RingmasterActionEvent;
import ca.site3.ssf.gamemodel.RoundPlayTimerChangedEvent;
import ca.site3.ssf.gamemodel.MatchEndedEvent.MatchResult;
import ca.site3.ssf.gamemodel.PlayerAttackActionEvent;
import ca.site3.ssf.gamemodel.PlayerBlockActionEvent;
import ca.site3.ssf.gamemodel.RoundBeginTimerChangedEvent;
import ca.site3.ssf.gamemodel.RoundBeginTimerChangedEvent.RoundBeginCountdownType;
import ca.site3.ssf.gamemodel.RoundEndedEvent.RoundResult;
import ca.site3.ssf.gamemodel.RoundEndedEvent;
import ca.site3.ssf.gamemodel.TogglePauseGameCommand;
import ca.site3.ssf.gamemodel.TouchFireEmitterCommand;
import ca.site3.ssf.guiprotocol.Event.GameEvent;
import ca.site3.ssf.guiprotocol.Event.GameEvent.Player;
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
 * @author greg
 */
public class StreetFireGuiClient {

	private Logger log = LoggerFactory.getLogger(getClass());
	
	private int port;
	private InetAddress serverAddress;
	
	private Socket socket;
	
	/** contains messages to be sent to server */
	private BlockingQueue<Command> commandQueue = new LinkedBlockingQueue<Command>();
	/** thread to monitor commandQueue and send messages to server */
	private SendThread sendThread;
	
	/** contains messages received from the server */
	private BlockingQueue<IGameModelEvent> eventQueue = new LinkedBlockingQueue<IGameModelEvent>();
	/** monitors socket for incoming events from server and places them on eventQueue */
	private ReceiveThread receiveThread;
	
	
	public StreetFireGuiClient(InetAddress ioserver, int port) {
		serverAddress = ioserver;
		this.port = port;
	}
	
	
	/**
	 * Connect to the IO Server. This must be called before any 
	 * attempts are made at communication. 
	 * 
	 * @return true if the connection was successful, false otherwise
	 * @throws IOException
	 */
	public boolean connect() throws IOException {
		socket = new Socket(serverAddress, port);
		
		if (socket.isConnected()) {
			sendThread = new SendThread();
			sendThread.start();
			receiveThread = new ReceiveThread();
			receiveThread.start();
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
	public void initiateNextState() throws IOException {
		Builder b = Command.newBuilder().setType(CommandType.NEXT_STATE);
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

	/**
	 * @see ExecutePlayerActionCommand
	 * @throws IOException
	 */
	public void executePlayerAction(int playerNum, 
									ActionFactory.PlayerActionType playerActionType, 
									boolean usesLeftHand,
									boolean usesRightHand) throws IOException {
		
		Builder b = Command.newBuilder().setType(CommandType.EXECUTE_PLAYER_ACTION)
					.setPlayer(SerializationHelper.playerFromNum(playerNum))
					.setPlayerAction(SerializationHelper.actionToProtobuf(playerActionType))
					.setLeftHand(usesLeftHand)
					.setRightHand(usesRightHand);
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
	
	
	
	/**
	 * Sends the given command to the server.
	 * 
	 * @param cmd
	 * @throws IOException
	 */
	private void submitCommand(Command cmd) throws IOException {
		if (isConnected() == false) {
			throw new IllegalStateException("Not connected to server");
		}
		
		commandQueue.offer(cmd);
	}
	

	/**
	 * Monitors commandQueue and sends commands along to the server
	 */
	private class SendThread extends Thread {
		@Override
		public void run() {
			while (true) {
				try {
					Command cmd = commandQueue.take();
					cmd.writeDelimitedTo(socket.getOutputStream());
				} catch (InterruptedException ex) {
					log.warn("Interrupted waiting for a command",ex);
				} catch (IOException ex) {
					/* probably want some kind of callback to client here.
					   hacky but could put a special 'error message' 
					   IGameModelEvent on the event queue. 
					   Otherwise dedicated error handler callback */
					log.error("Exception sending data to server",ex);
				}
				
				if ( ! isConnected() ) {
					log.info("No longer connected; Stopped listening for commands");
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
				} catch (IOException ex) {
					log.warn("IOException listening on GUI input stream",ex);
				} catch (Exception ex) {
					log.error("Exception listening on GUI input stream",ex);
				}
			}
		}
	}
	
	
	private IGameModelEvent parseEvent(GameEvent e) {
		switch (e.getType()) {
		case FireEmitterChanged:
			return new FireEmitterChangedEvent(createFireEmitter(e));
		case GameStateChanged:
			return new GameStateChangedEvent(
						SerializationHelper.protobufToGameState(e.getOldGameState()), 
						SerializationHelper.protobufToGameState(e.getNewGameState()));
		case MatchEnded:
			switch (e.getMatchWinner()) {
			case P1:
				return new MatchEndedEvent(MatchResult.PLAYER1_VICTORY);
			case P2:
				return new MatchEndedEvent(MatchResult.PLAYER2_VICTORY);
			case RINGMASTER:
				// pass through
			default:
				throw new IllegalArgumentException("Only Player 1 or 2 can win a match");
			}
		case PlayerAttackAction:
			return new PlayerAttackActionEvent(playerNumFromPlayer(e.getPlayer()), 
					SerializationHelper.protobufToAttackType(e.getAttackType()));
		case PlayerBlockAction:
			return new PlayerBlockActionEvent(playerNumFromPlayer(e.getPlayer()));
		case PlayerHealthChanged:
			return new PlayerHealthChangedEvent(playerNumFromPlayer(e.getPlayer()), 
					e.getOldHealth(), e.getNewHealth());
		case RingmasterAction:
			return new RingmasterActionEvent();
		case RoundBeginTimerChanged:
			if (e.getBeginType() == 0) {
				return new RoundBeginTimerChangedEvent(RoundBeginCountdownType.FIGHT, e.getRoundNumber());
			} else if (e.getBeginType() == 1) {
				return new RoundBeginTimerChangedEvent(RoundBeginCountdownType.ONE, e.getRoundNumber());
			} else if (e.getBeginType() == 2) {
				return new RoundBeginTimerChangedEvent(RoundBeginCountdownType.TWO, e.getRoundNumber());
			} else if (e.getBeginType() == 3) {
				return new RoundBeginTimerChangedEvent(RoundBeginCountdownType.THREE, e.getRoundNumber());
			} else {
				throw new IllegalArgumentException("Bad RoundBeginTimerChanged type"+e.getBeginType());
			}
		case RoundEnded:
			RoundResult result = RoundResult.TIE;
			if (e.getRoundWinner() == Player.P1) {
				result = RoundResult.PLAYER1_VICTORY;
			} else if (e.getRoundWinner() == Player.P2) {
				result = RoundResult.PLAYER2_VICTORY;
			}
			return new RoundEndedEvent(e.getRoundNumber(), result, e.getTimedOut());
		case RoundPlayTimerChanged:
			return new RoundPlayTimerChangedEvent(e.getTimeInSecs());
		default:
			log.error("Unknown GameEvent type: "+e.getType());
			return null;
		}
	}
	
	private int playerNumFromPlayer(Player p) {
		if (p == Player.RINGMASTER) {
			throw new IllegalArgumentException("Ringmaster is not a valid player");
		}
		return p == Player.P1 ? 1 : 2;
	}
	
	private FireEmitter createFireEmitter(GameEvent e) {
		final ca.site3.ssf.guiprotocol.Event.GameEvent.FireEmitter fe = e.getEmitter();
		return new FireEmitter(fe.getEmitterIndex(), 0, SerializationHelper.eventEmitterTypeToGame(fe.getEmitterType()) ) {
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
					throw new IllegalArgumentException("Unknown entity: "+contributor);
				}
			}
		};
	}
	
}
