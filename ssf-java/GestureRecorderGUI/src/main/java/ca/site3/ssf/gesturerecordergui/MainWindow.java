package ca.site3.ssf.gesturerecordergui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.site3.ssf.gesturerecognizer.GestureInstance;
import ca.site3.ssf.gesturerecognizer.GestureRecognizer;
import ca.site3.ssf.gesturerecognizer.GloveData;
import ca.site3.ssf.ioserver.CommandLineArgs;
import ca.site3.ssf.ioserver.DeviceDataParser;
import ca.site3.ssf.ioserver.DeviceEvent;
import ca.site3.ssf.ioserver.DeviceNetworkListener;
import ca.site3.ssf.ioserver.DeviceStatus;
import ca.site3.ssf.ioserver.GloveEvent;
import ca.site3.ssf.ioserver.HeartbeatListener;

/**
 * The main GUI class and driver for the gesture recorder
 * @author Mike
 *
 */
public class MainWindow extends JFrame {
	
	private Logger log = LoggerFactory.getLogger(getClass());
	private static final long serialVersionUID = 1L;
	
	private ArrayList<GloveData> leftGloveData;
	private ArrayList<GloveData> rightGloveData;
	private ArrayList<Double> timeData;
	private Long startTime;
	
	private RecordingPanel recordingPanel  = null;
	private TrainingPanel trainingPanel = null;
	private TestingPanel testingPanel = null;
	
	private volatile boolean isListeningForEvents = true;
	private BlockingQueue<DeviceEvent> eventQueue = new LinkedBlockingQueue<DeviceEvent>();
	
	private DeviceStatus deviceStatus = new DeviceStatus();
	private HeartbeatListener heartbeatListener = new HeartbeatListener(55555, deviceStatus);
	private DeviceNetworkListener gloveListener = new DeviceNetworkListener(new CommandLineArgs().devicePort, new DeviceDataParser(deviceStatus), eventQueue);
	
	private Thread consumerThread;
	private Runnable doUpdateInterface;

	private JTabbedPane tabbedPane = null;
	
	public MainWindow() {
		super();
		
		// Setup the frame's basic characteristics...
		this.setTitle("Super Street Fire (Gesture Recorder GUI)");
		this.setPreferredSize(new Dimension(900, 600));
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setLayout(new BorderLayout());

		// Create the tabbed pane
		this.tabbedPane = new JTabbedPane(); 
		this.tabbedPane.addTab("Recording", null, createRecordingTab());
		this.tabbedPane.addTab("Training", null, createTrainingTab());
		this.tabbedPane.addTab("Testing", null, createTestingTab());
		
		// Setup the frame's contents...
		Container contentPane = this.getContentPane();
		contentPane.add(this.tabbedPane, BorderLayout.NORTH);
		
		this.pack();
		this.setLocationRelativeTo(null);
		
		// Kick off the hardware event listener in the IOServer. To stop this call gloveListener.stop()
		Thread heartbeatThread = new Thread(heartbeatListener, "Heartbeat Thread");
		heartbeatThread.start();
		Thread producerThread = new Thread(gloveListener, "Glove listener Thread");
		producerThread.start();
		
		// To stop this set isListeningForEvents false and call consumerThread.interrupt()\
		//TODO: Start or stop this based on which tab we're in. 
		consumerThread = new Thread(new Runnable() {
			public void run() {
				DeviceEvent e;
				long timeout = 0;
				long startTime = 0;
				boolean recording = false;
				
				while (isListeningForEvents) {
					//try {
						e = eventQueue.poll();//.take();
					//} catch (InterruptedException ex) {
					//	log.info("Glove event consumer interrupted",ex);
					//	e = null;
					//}
					
					timeout = System.currentTimeMillis() - startTime;
					
					// If we find an event then fire off the event listener and reset the timeout
					if (null != e && e.getType() == DeviceEvent.Type.GloveEvent) {
						
						GloveEvent gloveEvent = (GloveEvent)e;
						
						switch (gloveEvent.getEventType()) {
						
						case BUTTON_DOWN_EVENT:
							
							if (!recording) {
								recording = true;
								startTime = System.currentTimeMillis();
							}
							timeout = 0;
							
							break;
							
						case BUTTON_UP_EVENT:
							if (recording) {
								recording = false;
								exportGatheredData();
							}
							break;
							
						case DATA_EVENT:
							if (recording) {
								if (timeout > (GestureRecognizer.MAXIMUM_GESTURE_RECOGNITION_TIME_IN_SECS * 1000)) {
									recording = false;
									exportGatheredData();
								}
								else {
									timeout = 0;
									hardwareEventListener((GloveEvent)e);
								}
							}
							break;
							
						default:
							assert(false);
							break;
						}
						
					}
				}
			}
		});
		
		// Start listening for and consuming the data from the gloves
		consumerThread.start();
	}

	// Build the GUI
	static void createAndShowGUI() {
		MainWindow mainWindow = new MainWindow();
		mainWindow.setVisible(true);
	}
	
	/**
	 * The main driver method for the Developer GUI.
	 * @param args
	 */
	public static void main(String[] args) {
		
        Runnable doCreateAndShowGUI = new Runnable() {
        	
            public void run() {
            	MainWindow.createAndShowGUI();
            }
        };
        SwingUtilities.invokeLater(doCreateAndShowGUI);
	}

	public void exportGatheredData() {
		int selectedTab = this.tabbedPane.getSelectedIndex();
		GestureInstance instance = new GestureInstance(this.leftGloveData, this.rightGloveData, this.timeData);
		
		// selectedTab == 0 is recording, == 1 is Training, == 2 is Testing
		if (selectedTab == 0) {
			
			// Check to see whether the gesture is even long enough to warrent recording...
			if (GestureRecognizer.isAcceptableGesture(instance)) {
				
				// If export to CSV is selected, perform the export
				if(this.recordingPanel.getCsvExportState()) {
					this.recordingPanel.exportToCsv(instance);
				}
				
				// If export to the gesture recognizer is selected, export to the GestureRecognizer
				if (this.recordingPanel.getRecognizerExportState()) {
					this.recordingPanel.exportToRecognizer(instance);
				}
				
				// Show all of the recorded data in the GUI Log...
				this.recordingPanel.setLogString(instance.toDataString());
			}
			else {
				this.recordingPanel.setLogString("Unacceptable gesture, please try again!");
			}
		}
		else if (selectedTab == 2 && this.testingPanel.isEngineLoaded()) {
			// If we're on the testing
			this.testingPanel.testGestureInstance(instance);
		}
		
		this.recordingPanel.setRecordMode(false);
	}
	
	// Triggered from IOServer. Will most Set the coordinates data. If we're in record mode, save that data too.
	public void hardwareEventListener(GloveEvent gloveEvent) {
		
		if (!this.recordingPanel.getRecordMode()) {
			
			this.recordingPanel.setRecordMode(true);
			
			// If we just started recording, mark the start time. This will be a brand new gesture and file.
			this.recordingPanel.setNewFile(true); 
			this.leftGloveData = new ArrayList<GloveData>();
			this.rightGloveData = new ArrayList<GloveData>();
			this.timeData = new ArrayList<Double>();
			this.startTime = System.nanoTime();
		}
		else {
			this.recordingPanel.setNewFile(false);
		}
		
		// Build the gesture coordinates object and final vars to use in the UI thread
		final double time = getElapsedTime();
		final GloveData gloveData = buildGloveData(gloveEvent);
		final String gestureName = this.recordingPanel.getGestureName();
		
		switch (gloveEvent.getDevice()) {
		case LEFT_GLOVE:
			this.leftGloveData.add(gloveData);
			break;
		case RIGHT_GLOVE:
			this.rightGloveData.add(gloveData);
			break;
		default:
			assert(false);
			return;
		}
		this.timeData.add(time);
		
		// Update the UI with the glove data
		/*
		this.doUpdateInterface = new Runnable() {       	
            public void run() {
            	displayAndLogData(gloveData, gestureName, time);
            }
        };
		SwingUtilities.invokeLater(this.doUpdateInterface);
		*/
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
		panel.add(this.recordingPanel, BorderLayout.NORTH);
		
		return panel;
	}
	
	// Creates and instantiates the training panel 
	public JPanel createTrainingTab()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		
		this.trainingPanel = new TrainingPanel();
		panel.add(this.trainingPanel, BorderLayout.NORTH);
				
		return panel;
	}
	
	// Creates and instantiates the testing panel 
	public JPanel createTestingTab()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		
		this.testingPanel = new TestingPanel();
		panel.add(this.testingPanel, BorderLayout.NORTH);
				
		return panel;
	}
	
	// Gets the time elapsed since recording started in seconds
	public double getElapsedTime() {
		double diff = (System.nanoTime() - this.startTime) / (double)(10E9);
		return diff;
	}
}
