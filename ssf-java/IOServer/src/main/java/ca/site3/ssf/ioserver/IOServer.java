package ca.site3.ssf.ioserver;

import java.util.Queue;
import java.util.concurrent.BlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.site3.ssf.gamemodel.AbstractGameModelCommand;
import ca.site3.ssf.gamemodel.GameConfig;
import ca.site3.ssf.gamemodel.GameModel;
import ca.site3.ssf.gamemodel.IGameModel;
import ca.site3.ssf.gamemodel.IGameModelListener;
import ca.site3.ssf.gesturerecognizer.GestureRecognizer;
import ca.site3.ssf.guiprotocol.StreetFireServer;
import ch.qos.logback.classic.Level;

import com.beust.jcommander.JCommander;


/**
 * Entry point for the Super Street Fire I/O server. Handles initialization, event loop etc.
 * 
 * @author greg
 */
public class IOServer {
	
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	private CommandLineArgs args;
	
	
	private IGameModel game;
	
	private IGameModelListener gameEventRouter;
	
	/** Time the event loop started (millis since epoch) */
	private long startTime;
	
	/** Amount of time a tick should take (used as a limiting factor) */
	private int frameLengthInMillis;
	
	/** Flag to indicate whether the server should be stopped */
	private volatile boolean isStopped = false;
	
	private CommunicationsManager commManager = new CommunicationsManager();
	
	private DeviceStatus deviceStatus = new DeviceStatus();
	
	private DeviceNetworkListener deviceListener;
	
	private HeartbeatListener heartbeatListener;
	
	
	public IOServer(CommandLineArgs args) {
		this.args = args;

		GameConfig gameConfig = new GameConfig(args.isChipDamage, args.minTimeBetweenPlayerActionsInSecs, 
												args.roundTimeInSecs, args.numRoundsPerMatch, args.chipDamagePercentage);
		game = new GameModel(gameConfig);
	}
	
	/**
	 * Configure things and kick off the event loop 
	 */
	public void start() {
		
		log.info("\n~~~~~~~~~~~~~~~~~~~\n" +
				 "Starting I/O server\n" +
				  args.toString() +
				 "\n~~~~~~~~~~~~~~~~~~~");
		
		frameLengthInMillis = (int)Math.round(1000.0 / args.tickFrequency);
		
		
		heartbeatListener = new HeartbeatListener(args.heartbeatPort, deviceStatus);
		Thread heartbeatListenerThread = new Thread(heartbeatListener);
		heartbeatListenerThread.start();
		
		
		StreetFireServer guiServer = new StreetFireServer(args.guiPort, game.getActionFactory(), commManager.getCommandQueue());
		Thread guiServerThread = new Thread(guiServer, "GUI Server Thread");
		guiServerThread.start();
		
		gameEventRouter = new GameEventRouter(guiServer, commManager.getGuiOutQueue());
		game.addGameModelListener(gameEventRouter);
		
		deviceListener = new DeviceNetworkListener(args.devicePort, new DeviceDataParser(deviceStatus), commManager.getCommInQueue());
		Thread deviceListenerThread = new Thread(deviceListener, "DeviceListener Thread");
		deviceListenerThread.start();
		
		isStopped = false;
		runLoop();
		log.info("I/O server terminating");
		deviceListener.stop();
		
	}
	
	
	public IGameModel getGameModel() {
		return game;
	}
	
	public DeviceStatus getDeviceStatus() {
		return deviceStatus;
	}
	
	public Queue<AbstractGameModelCommand> getCommandQueue() {
		return commManager.getCommandQueue();
	}
	
	
	public BlockingQueue<DeviceEvent> getDeviceEventQueue() {
		return commManager.getCommInQueue();
	}
	
	/**
	 * Maintains timing information and ticks the game engine
	 */
	private void runLoop() {
		
		startTime = System.currentTimeMillis();
		
		long deltaFrameTime = 0; // current frame's delta time (millis)
		long currentTime = System.currentTimeMillis(); // Temporary variable for the current absolute time
		long lastFrameTime = currentTime; // Holds the absolute time of the last frame
		//long millisSinceStart = 0; // Milliseconds since the start of the simulation
		
		while (isStopped == false) {
			currentTime      = System.currentTimeMillis();
			deltaFrameTime   = currentTime - lastFrameTime;
			lastFrameTime    = currentTime;
			//millisSinceStart = currentTime - startTime;
			
			// ********************************************************************************************
			// TODO: Check for device events and then use the gesture recognizer to place actions into the command queue
			// NOTE: This could probably be moved into its own class and probably even its own thread...
			
			while (!commManager.getCommInQueue().isEmpty()) {
				DeviceEvent deviceEvent = commManager.getCommInQueue().remove();
				
				// TODO: Figure out how to properly combine left and right glove data when both buttons
				// are pressed (on each of the gloves) ... maybe do this at the DeviceNetworkListener / DeviceDataParser level?
				
				switch (deviceEvent.getDevice()) {
				
				case HEADSET:
					HeadsetEvent headsetEvent = (HeadsetEvent)deviceEvent;
					assert(headsetEvent != null);
					// TODO
					break;
					
				case LEFT_GLOVE: {
					GloveEvent leftGloveEvent = (GloveEvent)deviceEvent;
					assert(leftGloveEvent != null);
					// TODO
					break;
				}
					
				case RIGHT_GLOVE:
					GloveEvent rightGloveEvent = (GloveEvent)deviceEvent;
					assert(rightGloveEvent != null);
					// TODO
					break;
					
				default:
					assert(false);
					break;
				}
				
			}
			
			// ********************************************************************************************
			
			
			while (!commManager.getCommandQueue().isEmpty() ) {
				game.executeCommand(commManager.getCommandQueue().remove());
			}
			
			try {
				game.tick(deltaFrameTime/1000.0);
			} catch (Exception ex) {
				log.error("Exception while ticking game", ex);
			}
			
			
			if (deltaFrameTime < frameLengthInMillis) {
				try {
					Thread.sleep(frameLengthInMillis - deltaFrameTime);
				} catch (InterruptedException ex) {
					// not much to be done about this
					log.warn("IOServer interrupted during runLoop",ex);
				}
			}
		}
	}
	
	
	public void stop() {
		isStopped = true;
	}
	
	
	
	public static void main(String... argv) {
		final CommandLineArgs args = new CommandLineArgs();
		// populates args from argv
		new JCommander(args, argv);
		
		configureLogging(args.verbosity);
		
		final IOServer ioserver = new IOServer(args);
		ioserver.runLoop();
	}
	

	private static void configureLogging(int level) {
		ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger)
				LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
		
		switch (level) {
			case 0:
				root.setLevel(Level.OFF); break;
			case 1:
				root.setLevel(Level.TRACE); break;
			case 2:
				root.setLevel(Level.DEBUG); break;
			case 3:
				root.setLevel(Level.INFO); break;
			case 4:
				root.setLevel(Level.WARN); break;
			case 5:
				root.setLevel(Level.ERROR); break;
			default:
				root.setLevel(Level.INFO);
		}
	}
}
