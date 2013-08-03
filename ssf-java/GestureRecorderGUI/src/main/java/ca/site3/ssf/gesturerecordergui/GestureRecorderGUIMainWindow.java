package ca.site3.ssf.gesturerecordergui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Level;

import com.beust.jcommander.JCommander;

import ca.site3.ssf.common.LoggingUtil;
import ca.site3.ssf.gamemodel.IGameModel.Entity;
import ca.site3.ssf.gesturerecognizer.GestureInstance;
import ca.site3.ssf.gesturerecognizer.GestureRecognizer;
import ca.site3.ssf.gesturerecognizer.GestureType;
import ca.site3.ssf.gesturerecognizer.GloveData;
import ca.site3.ssf.ioserver.CommandLineArgs;
import ca.site3.ssf.ioserver.DeviceDataParser;
import ca.site3.ssf.ioserver.DeviceEvent;
import ca.site3.ssf.ioserver.DeviceNetworkListener;
import ca.site3.ssf.ioserver.DeviceStatus;
import ca.site3.ssf.ioserver.GloveEvent;
import ca.site3.ssf.ioserver.GloveEventCoalescer;
import ca.site3.ssf.ioserver.HeartbeatListener;
import ca.site3.ssf.ioserver.EntityGestureInstance;

/**
 * The main GUI class and driver for the gesture recorder
 * @author Mike, Callum
 *
 */
public class GestureRecorderGUIMainWindow extends JFrame {
	
	private static final String DEFAULT_GLOVE_NETWORK_INTERFACE_IP = "192.168.100.2";
	
	private Logger log = LoggerFactory.getLogger(getClass());
	private static final long serialVersionUID = 1L;
	
	private RecordingPanel recordingPanel  = null;
	private TrainingPanel trainingPanel = null;
	private TestingPanel testingPanel = null;
	
	private volatile boolean isListeningForEvents = true;
	
	// Pieces of the IOServer that are required to capture glove data and aggregate that data into gesture instances
	private BlockingQueue<EntityGestureInstance> gestureQueue = new LinkedBlockingQueue<EntityGestureInstance>();
	private BlockingQueue<DeviceEvent> eventQueue = new LinkedBlockingQueue<DeviceEvent>();
	private DeviceStatus deviceStatus = new DeviceStatus();
	private HeartbeatListener heartbeatListener  = new HeartbeatListener(DEFAULT_GLOVE_NETWORK_INTERFACE_IP, 55555, deviceStatus);
	private DeviceNetworkListener gloveListener  = new DeviceNetworkListener(DEFAULT_GLOVE_NETWORK_INTERFACE_IP,
			new CommandLineArgs().devicePort, new DeviceDataParser(deviceStatus), eventQueue);
	private GloveEventCoalescer eventAggregator  = null;
	
	private Thread consumerThread;

	private JTabbedPane tabbedPane = null;
	
	public GestureRecorderGUIMainWindow() {
		super();
		
		// Setup the frame's basic characteristics...
		this.setTitle("Super Street Fire (Gesture Recorder GUI)");
		this.setPreferredSize(new Dimension(1200, 600));
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setLayout(new BorderLayout());

		// Create the tabbed pane
		this.tabbedPane = new JTabbedPane(); 
		this.tabbedPane.addTab("Recording", null, createRecordingTab());
		this.tabbedPane.addTab("Training", null, createTrainingTab());
		this.tabbedPane.addTab("Testing", null, createTestingTab());
		
		// Setup the frame's contents...
		Container contentPane = this.getContentPane();
		contentPane.setLayout(new BorderLayout());
		contentPane.add(this.tabbedPane, BorderLayout.CENTER);
		
		this.pack();
		this.setLocationRelativeTo(null);
		
		// Kick off the hardware event listener in the IOServer. To stop this call gloveListener.stop()
		Thread heartbeatThread = new Thread(heartbeatListener, "Heartbeat Thread");
		heartbeatThread.start();
		Thread producerThread = new Thread(gloveListener, "Glove listener Thread");
		producerThread.start();
		
		this.eventAggregator = new GloveEventCoalescer(System.currentTimeMillis(), eventQueue, gestureQueue);
		Thread eventAggregatorThread = new Thread(this.eventAggregator, "Event aggregator thread");
		eventAggregatorThread.start();
		
		this.consumerThread = new Thread(new Runnable() {
			public void run() {

				EntityGestureInstance currGestureInst = null;
				
				while (isListeningForEvents) {
					
					// Happily enough, all of the logic for determining the length of gesture, aggregating data for two-handed gestures
					// and generally dealing with what constitutes the data for a gesture is dealt with by the GloveEventCoalescer, we
					// just have to wait for one to appear on the queue, yay!
					
					try {
						currGestureInst = gestureQueue.take();
					}
					catch (InterruptedException e) {
						log.warn("Gesture queue blocking was interrupted.", e);
						currGestureInst = null;
						continue;
					}
					
					assert(currGestureInst != null);
					exportGatheredData(currGestureInst);
				}
			}
		});
		
		// Start listening for and consuming the data from the gloves
		this.consumerThread.start();
	}

	// Build the GUI
	static void createAndShowGUI() {
		GestureRecorderGUIMainWindow mainWindow = new GestureRecorderGUIMainWindow();
		mainWindow.setVisible(true);
	}
	
	/**
	 * The main driver method for the Developer GUI.
	 * @param args
	 */
	public static void main(String[] argv) {
		
		final CommandLineArgs args = new CommandLineArgs();
		// populates args from argv
		new JCommander(args, argv);
		
		LoggingUtil.configureLogging(args.verbosity);
		
        Runnable doCreateAndShowGUI = new Runnable() {
        	
            public void run() {
            	GestureRecorderGUIMainWindow.createAndShowGUI();
            }
        };
        SwingUtilities.invokeLater(doCreateAndShowGUI);
	}

	public void exportGatheredData(EntityGestureInstance recordedGestureInstance) {
		
		int selectedTab = this.tabbedPane.getSelectedIndex();
		GestureInstance instance = recordedGestureInstance;
		
		final int RECORDING_TAB_IDX = 0;
		//final int TRAINING_TAB_IDX  = 1;
		final int TESTING_TAB_IDX   = 2;
		
		// selectedTab: 0 is recording, 1 is Training, 2 is Testing
		if (selectedTab == RECORDING_TAB_IDX) {
			
			// Make sure the gesture matches the handedness of the selected gesture...
			GestureType selectedGesture = this.recordingPanel.getSelectedGesture();
			
			// Check to see whether the gesture is acceptable in the face of up-front analysis from the gesture recognizer...
			if (!GestureRecognizer.isAcceptableGestureForGivenType(instance, selectedGesture)) {
				this.recordingPanel.setLogString("Unacceptable gesture, please try again!");
			}
			else {
				// Acceptable gesture...
				List<String> filenames = new ArrayList<String>(2);
				
				// If export to CSV is selected, perform the export
				if (this.recordingPanel.getCsvExportState()) {
					String filename = this.recordingPanel.exportToCsv(instance);
					filenames.add(filename);
				}
				
				// If export to the gesture recognizer is selected, export to the GestureRecognizer
				if (this.recordingPanel.getRecognizerExportState()) {
					String filename = this.recordingPanel.exportToRecognizer(instance);
					if (filename != null) {
						filenames.add(filename);
					}
				}
				
				// Show all of the recorded data in the GUI Log...
				if (filenames.isEmpty()) {
					this.recordingPanel.setLogString("FAILED TO WRITE GESTURE!! PERHAPS A BAD DIRECTORY IS SELECTED...");
				}
				else {
					String logStr = "Gesture exported to file(s):\n";
					for (String filename : filenames) {
						logStr += filename + "\n";
					}
	
					logStr += "\n" + instance.toDataString();
					this.recordingPanel.setLogString(logStr);
				}
			}

		}
		else if (selectedTab == TESTING_TAB_IDX && this.testingPanel.isEngineLoaded()) {
			// If we're on the testing
			this.testingPanel.testGestureInstance(instance, recordedGestureInstance.getEntity() == Entity.RINGMASTER_ENTITY);
		}
		
		this.recordingPanel.setRecordMode(false);
	}
	
	// Call the recorder's display and log method
	public void displayAndLogData(GloveData gloveData, String gestureName, double time){
		this.recordingPanel.displayAndLogData(gloveData, gestureName, time);
	}
	
	// Construct a glove data object using the gesture recognizer
	public GloveData buildGloveData(GloveEvent event) {
		double[] gyro = event.getGyro();
		double[] mag  = event.getMagnetometer();
		double[] acc  = event.getAcceleration();
		
		return new GloveData(gyro[0], gyro[1], gyro[2], acc[0], acc[1], acc[2], mag[0], mag[1], mag[2]);
	}
	
	// Creates and instantiates the recorder panel 
	public JPanel createRecordingTab()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		
		this.recordingPanel = new RecordingPanel();
		panel.add(this.recordingPanel, BorderLayout.CENTER);
		
		return panel;
	}
	
	// Creates and instantiates the training panel 
	public JPanel createTrainingTab()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		
		this.trainingPanel = new TrainingPanel();
		panel.add(this.trainingPanel, BorderLayout.CENTER);
				
		return panel;
	}
	
	// Creates and instantiates the testing panel 
	public JPanel createTestingTab()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		
		this.testingPanel = new TestingPanel();
		panel.add(this.testingPanel, BorderLayout.CENTER);
				
		return panel;
	}

}
