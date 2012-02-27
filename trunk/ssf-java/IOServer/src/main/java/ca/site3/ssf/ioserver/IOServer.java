package ca.site3.ssf.ioserver;

import java.util.AbstractQueue;
import java.util.Arrays;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.site3.ssf.gamemodel.GameConfig;
import ca.site3.ssf.gamemodel.GameModel;
import ca.site3.ssf.gamemodel.IGameModel;
import ca.site3.ssf.gamemodel.IGameModelListener;
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
	
	private CommunicationsManager commManager;
	
	
	/**
	 * Configure things and kick off the event loop 
	 */
	public void start(CommandLineArgs arguments) {
		
		log.info("Starting I/O server");
		this.args = arguments;
		
		this.frameLengthInMillis = (int)Math.round(1000.0 / args.tickFrequency);
		
		GameConfig gameConfig = new GameConfig(args.isChipDamage, args.minTimeBetweenPlayerActionsInSecs, 
												args.roundTimeInSecs, args.numRoundsPerMatch);
		game = new GameModel(gameConfig);
		
		commManager  = new CommunicationsManager();
		this.gameEventRouter = new GameEventRouter(commManager.getCommOutQueue(), commManager.getGuiOutQueue());
		game.addGameModelListener(gameEventRouter);
		
		isStopped = false;
		runLoop();
		log.info("I/O server terminating");
		
		
	}
	
	
	/**
	 * Maintains timing information and ticks the game engine
	 */
	private void runLoop() {
		
		startTime = System.currentTimeMillis();
		
		long deltaFrameTime = 0; // current frame's delta time (millis)
		long currentTime = System.currentTimeMillis(); // Temporary variable for the current absolute time
		long lastFrameTime = currentTime; // Holds the absolute time of the last frame
		long millisSinceStart = 0; // Milliseconds since the start of the simulation
		
		while (isStopped == false) {
			currentTime = System.currentTimeMillis();
			deltaFrameTime = currentTime - lastFrameTime;
			lastFrameTime = currentTime;
			millisSinceStart = currentTime - startTime;
			
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
					log.warn("IO/Server interrupted during runLoop",ex);
				}
			}
		}
	}
	
	
	public void stop() {
		isStopped = true;
	}
	
	
	public static void main(String[] argv) {
		
		CommandLineArgs args = new CommandLineArgs();
		// populates args from argv
		new JCommander(args, argv);
		
		configureLogging(args.verbosity);
		
		IOServer server = new IOServer();
		server.start(args);
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