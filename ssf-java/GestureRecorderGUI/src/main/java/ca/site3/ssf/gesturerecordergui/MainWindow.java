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
import ca.site3.ssf.gesturerecognizer.GloveData;
import ca.site3.ssf.ioserver.DeviceEvent;
import ca.site3.ssf.ioserver.DeviceNetworkListener;
import ca.site3.ssf.ioserver.GloveEvent;

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
	private DeviceNetworkListener gloveListener = new DeviceNetworkListener(31337, eventQueue);
	private Thread consumerThread;
	private Runnable doUpdateInterface;
	
	private JTabbedPane tabbedPane = null;
	
	public MainWindow() {
		super();
		
		// Setup the frame's basic characteristics...
		this.setTitle("Super Street Fire (Gesture Recorder GUI)");
		this.setPreferredSize(new Dimension(900, 500));
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setLayout(new BorderLayout());

		// Create and instantiate the recording, training and testing tab
		JPanel recordingTab = createRecordingTab();
		JPanel trainingTab = createTrainingTab();
		JPanel testingTab = createTestingTab();
		
		// Create the tabbed pane
		this.tabbedPane = new JTabbedPane(); 
		this.tabbedPane.addTab("Recording", null, recordingTab);
		this.tabbedPane.addTab("Training", null, trainingTab);
		this.tabbedPane.addTab("Testing", null, testingTab);
		
		// Setup the frame's contents...
		Container contentPane = this.getContentPane();
		contentPane.add(this.tabbedPane, BorderLayout.NORTH);
		
		this.pack();
		this.setLocationRelativeTo(null);
		
		// Kick off the hardware event listener in the IOServer. To stop this call gloveListener.stop()
		Thread producerThread = new Thread(gloveListener);
		producerThread.start();
		
		// To stop this set isListeningForEvents false and call consumerThread.interrupt()\
		//TODO: Start or stop this based on which tab we're in. 
		consumerThread = new Thread(new Runnable() {
			public void run() {
				DeviceEvent e;
				while (isListeningForEvents) {					
					try {
						e = eventQueue.take();
					} catch (InterruptedException ex) {
						log.info("Glove event consumer interrupted",ex);
						e = null;
					}
					if (null != e && e.getType() == DeviceEvent.Type.GloveEvent) {
						hardwareEventListener((GloveEvent)e);
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

	//TODO: move recorder specific logic to the recorder panel
	// Triggered from IOServer. Will most Set the coordinates data. If we're in record mode, save that data too.
	public void hardwareEventListener(GloveEvent gloveEvent) {
		//TODO: Use this.tabbedPane.getSelectedIndex(); to determine what to do with hardware input
		if (gloveEvent.isButtonPressed())
		{
			// If we just started recording, mark the start time. This will be a brand new gesture and file.
			if (!this.recordingPanel.getRecordMode())
			{
				this.recordingPanel.setNewFile(true); 
				this.leftGloveData = new ArrayList<GloveData>();
				this.rightGloveData = new ArrayList<GloveData>();
				this.timeData = new ArrayList<Double>();
				this.startTime = System.nanoTime();
			}
			
			this.recordingPanel.setRecordMode(true);
		}
		else
		{
			// If we're ending a recording, create and export the gesture instance object
			if(this.recordingPanel.getRecordMode())
			{
				GestureInstance instance = createGestureInstance();
				
				//TODO: Check which tab is currently selected, if we're on the trainer and are in training mode then do something else
				// If export to CSV is selected, perform the export
				if(this.recordingPanel.getCsvExportState())
				{
					this.recordingPanel.exportToCsv(instance);
				}
				
				// If export to the gesture recognizer is selected, export to the GestureRecognizer
				if(this.recordingPanel.getRecognizerExportState())
				{
					this.recordingPanel.exportToRecognizer(instance);
				}
				
				if(this.testingPanel.isEngineLoaded())
				{
					this.testingPanel.testGestureInstance(instance);
				}
			}
			
			this.recordingPanel.setRecordMode(false);
		}
		
		// Set the recording indicator
		//this.recordingPanel(this.recordingPanel.getRecordMode());
		
		// Build the gesture coordinates object and final vars to use in the UI thread
		final double time = getElapsedTime();
		final GloveData gloveData = buildGloveData(gloveEvent);
		final String gestureName = this.recordingPanel.getGestureName();
		
		//TODO: Find out which glove we're using and add to the appropriate list
		this.leftGloveData.add(gloveData);
		this.rightGloveData.add(gloveData);
		this.timeData.add(time);
		
		// Update the UI with the glove data
		this.doUpdateInterface = new Runnable() {       	
            public void run() {
            	displayAndLogData(gloveData, gestureName, time);
            }
        };
		SwingUtilities.invokeLater(this.doUpdateInterface);
	}
	
	// Call the recorder's display and log method
	public void displayAndLogData(GloveData gloveData, String gestureName, double time){
		this.recordingPanel.displayAndLogData(gloveData, gestureName, time);
	}
	
	// Construct a glove data object using the gesture recognizer
	public GloveData buildGloveData(GloveEvent event)
	{
		double[] gyro = event.getGyro();
		double[] mag = event.getMagnetometer();
		double[] acc = event.getAcceleration();
		
		GloveData gloveData = new GloveData();
		gloveData.fromDataString(Double.toString(gyro[0]) + "," + Double.toString(gyro[1]) + "," + Double.toString(gyro[2]) + "," + 
								 Double.toString(acc[0]) + "," + Double.toString(acc[1]) + "," + Double.toString(acc[2]) + "," + 
								 Double.toString(mag[0]) + "," + Double.toString(mag[1]) + "," + Double.toString(mag[2]));
		
		return gloveData;
	}
	
	// Create a gesture instance from a list of left glove, right glove and time objects
	public GestureInstance createGestureInstance()
	{
		GloveData[] leftGloveData = new GloveData[this.leftGloveData.size()];
		GloveData[] rightGloveData = new GloveData[this.rightGloveData.size()];
		double[] timeData = new double[this.timeData.size()];
		
		int count = 0;
		for (GloveData data : this.leftGloveData)
		{
			data.toDataString();
			leftGloveData[count] = data;
			count++;
		}
		
		count = 0;
		for (GloveData data : this.rightGloveData)
		{
			rightGloveData[count] = data;
		}
		
		return new GestureInstance(leftGloveData, rightGloveData, timeData);
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
	
	// Gets the time elapsed since recording started in milliseconds
	public double getElapsedTime()
	{
		return (double)(System.nanoTime() - this.startTime) / 1000000;
	}
}
