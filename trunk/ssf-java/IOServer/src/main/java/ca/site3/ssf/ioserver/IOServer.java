package ca.site3.ssf.ioserver;

import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.site3.ssf.gamemodel.AbstractGameModelCommand;
import ca.site3.ssf.gamemodel.Action;
import ca.site3.ssf.gamemodel.GameConfig;
import ca.site3.ssf.gamemodel.GameModel;
import ca.site3.ssf.gamemodel.HeadsetData;
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
	
	private GestureRecognizer gestureRecognizer = new GestureRecognizer();
	
	private IGameModelListener gameEventRouter;
	
	/** Comm port for sending commands to flamethrowers, timer, etc. Null until {@link #initSerialDevice()} is called.  */
	private SerialPort serialPort;
	
	/** Time the event loop started (millis since epoch) */
	private long startTime;
	
	/** Amount of time a tick should take (used as a limiting factor) */
	private int frameLengthInMillis;
	
	/** Flag to indicate whether the server should be stopped */
	private volatile boolean isStopped = false;
	
	private CommunicationsManager commManager = new CommunicationsManager();
	
	private DeviceStatus deviceStatus = new DeviceStatus();
	
	private DeviceNetworkListener deviceListener;
	
	private GloveEventCoalescer eventAggregator;
	
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
		
		startTime = System.currentTimeMillis();
		frameLengthInMillis = (int)Math.round(1000.0 / args.tickFrequency);
		
		heartbeatListener = new HeartbeatListener(args.gloveInterfaceIP, args.heartbeatPort, deviceStatus);
		Thread heartbeatListenerThread = new Thread(heartbeatListener, "Glove heartbeat listener thread");
		heartbeatListenerThread.start();
		
		StreetFireServer guiServer = new StreetFireServer(args.guiPort, game.getActionFactory(), commManager.getCommandQueue());
		Thread guiServerThread = new Thread(guiServer, "GUI Server Thread");
		guiServerThread.start();
		
		
		initSerialDevice();
		
		InputStream serialIn = null;
		OutputStream serialOut = null;
		if (serialPort != null) {
			try {
				serialIn = serialPort.getInputStream();
				serialOut = serialPort.getOutputStream();
			} catch (IOException ex) {
				log.error("Exception accessing serial stream",ex);
			}
		}
		if (serialOut == null) {
			serialOut = new OutputStream() { public @Override void write(int b) throws IOException { } }; // /dev/null
		}
		if (serialIn == null) {
			serialIn = new InputStream() { public @Override int read() throws IOException { return 0; } }; // /dev/null
		}
		
		SerialCommunicator serialComm = new SerialCommunicator(serialIn, serialOut);
		Thread serialCommThread = new Thread(serialComm, "Serial communications thread");
		serialCommThread.start();
		
		gameEventRouter = new GameEventRouter(guiServer, serialComm);
		game.addGameModelListener(gameEventRouter);
		
		eventAggregator = new GloveEventCoalescer(startTime, 0.5, commManager.getCommInQueue(), commManager.getGestureQueue());

		Thread eventAggregatorThread = new Thread(eventAggregator, "Event aggregator thread");
		eventAggregatorThread.start();
		
		deviceListener = new DeviceNetworkListener(args.gloveInterfaceIP, args.devicePort, new DeviceDataParser(deviceStatus), commManager.getCommInQueue());
		Thread deviceListenerThread = new Thread(deviceListener, "DeviceListener Thread");
		deviceListenerThread.start();
		
		
		// Attempt to setup the gesture recognizer
		try {
			boolean success = this.gestureRecognizer.loadRecognizerEngine(new FileReader(args.gestureEngineFilepath));
			if (!success) {
				log.warn("Failed to read gesture recognition engine from " + args.gestureEngineFilepath);
			}
		}
		catch (FileNotFoundException e) {
			log.warn("Could not load file gesture recognition engine from " + args.gestureEngineFilepath, e);
		}
		
		isStopped = false;
		runLoop();
		log.info("I/O server terminating");
		
		closeSerialDevice();
		
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
			
			// Go through our various queues of data that has been aggregated and concentrated from
			// the various clients of the IOServer, execute that data on the GameModel
			
			// Commands to the GameModel
			while (!commManager.getCommandQueue().isEmpty() ) {
				game.executeCommand(commManager.getCommandQueue().remove());
			}
			
			// Recognize gestures and execute any recognized gestures on the GameModel
			while (!commManager.getGestureQueue().isEmpty()) {
				EntityGestureInstance gesture = commManager.getGestureQueue().remove();
				
				Action recognizedAction = null;
				if (gesture.getEntity().getIsPlayer()) {
					// TODO: Forwarding of headset data with the Action to the GameModel as well?
					recognizedAction = gestureRecognizer.recognizePlayerGesture(getGameModel().getActionFactory(),
							gesture.getEntity().getPlayerNum(), gesture);
				}
				else {
					recognizedAction = gestureRecognizer.recognizeRingmasterGesture(getGameModel().getActionFactory(), gesture);
				}
				
				if (recognizedAction != null) {
					getGameModel().executeGenericAction(recognizedAction);
				}
				else {
					// Gesture was unrecognized, inform the gamemodel that there was a 'bad' gesture for
					// a particular player, this will raise an event that allows feedback to be had
					getGameModel().raiseUnrecognizedGestureEvent(gesture.getEntity());
				}
			}
			
			// Forward the general stream of headset events to the game model - this provides information for affecting
			// the game in-general as opposed to when the data is submitted via an Action, where it will only affect that action
			while (!eventAggregator.getP1HeadsetEventQueue().isEmpty()) {
				HeadsetEvent p1HeadsetEvent = eventAggregator.getP1HeadsetEventQueue().remove();
				getGameModel().updatePlayerHeadsetData(1, new HeadsetData(p1HeadsetEvent.getAttention(), p1HeadsetEvent.getMeditation())); 
			}
			while (!eventAggregator.getP2HeadsetEventQueue().isEmpty()) {
				HeadsetEvent p2HeadsetEvent = eventAggregator.getP2HeadsetEventQueue().remove();
				getGameModel().updatePlayerHeadsetData(2, new HeadsetData(p2HeadsetEvent.getAttention(), p2HeadsetEvent.getMeditation())); 
			}
			
			try {
				game.tick(deltaFrameTime / 1000.0);
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
		
		/*
		 *  give main thread time to clean up (this typically gets called on
		 *  the AWT thread when the dev gui window is closing, which also
		 *  triggers the application to exit). 
		 */
		try {
			Thread.sleep(500);
		} catch (InterruptedException ex) { ex.printStackTrace(); }
	}
	
	
	
	public static void main(String... argv) {
		final CommandLineArgs args = new CommandLineArgs();
		// populates args from argv
		new JCommander(args, argv);
		
		configureLogging(args.verbosity);
		
		final IOServer ioserver = new IOServer(args);
		ioserver.start();
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
	
	
	/**
	 * Initialize the serial comm port.
	 */
	private void initSerialDevice() {
		
		CommPortIdentifier commPortId = null;
		try {
			commPortId = CommPortIdentifier.getPortIdentifier(args.serialDevice);
		} catch (NoSuchPortException ex) {
			log.error("Could not open serial port '"+ args.serialDevice+"'",ex);
		} catch (UnsatisfiedLinkError ex) {
			log.error("Could not load rxtx serial comm native library.\n" + 
						"If you're on a Mac, copy IOServer/src/main/resources/librxtxSerial.jnilib to ~/Library/Java/Extensions/\n" +
						"Otherwise take a look here: http://rxtx.qbang.org/wiki/index.php/Main_Page");
		}
		
		if (commPortId == null) {
			log.error("Could not get serial port ID for device '"+args.serialDevice+"'. No fire :-(");
			return;
		}
		
		try {
			serialPort = (SerialPort) commPortId.open("StreetFire IOServer", 5000);
			serialPort.getInputStream();
			serialPort.getOutputStream();
		} catch (PortInUseException ex) {
			log.error("Serial port in use! This might be solved by 'sudo mkdir /var/lock; sudo chmod 777 /var/lock' on a Mac",ex);
			return;
		} catch (Exception ex) {
			log.error("Exception opening serial port",ex);
			if (serialPort != null) {
				serialPort.close();
			}
			return;
		}
		
		// 57600 8N1 for now.. may need to expose these in command line args
		int baudRate = 57600;
		int databits = SerialPort.DATABITS_8;
		int stopbits = SerialPort.STOPBITS_1;
		int parity = SerialPort.PARITY_NONE;
		
		try {
			serialPort.setSerialPortParams(baudRate, databits, stopbits, parity);
			serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
		} catch (UnsupportedCommOperationException ex) {
			log.error("Could not configure serial port", ex);
		}
	}
	
	private void closeSerialDevice() {
		if (serialPort != null) {
			log.info("Closing serial port");
			try {
				serialPort.getOutputStream().write("buh-bye\n".getBytes());
				serialPort.getOutputStream().flush();
				serialPort.getInputStream().close();
				serialPort.getOutputStream().close();
			} catch (IOException ex) {
				log.warn("Error trying to close serial port stream",ex);
			}
			serialPort.close();
			
			serialPort = null;
		}
	}
}
