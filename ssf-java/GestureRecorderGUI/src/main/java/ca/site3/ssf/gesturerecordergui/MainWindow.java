package ca.site3.ssf.gesturerecordergui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.site3.ssf.gesturerecognizer.GestureInstance;
import ca.site3.ssf.gesturerecognizer.GloveData;
import ca.site3.ssf.ioserver.DeviceEvent;
import ca.site3.ssf.ioserver.DeviceNetworkListener;
import ca.site3.ssf.ioserver.GloveEvent;

// The main window for the recorder GUI.
public class MainWindow extends JFrame {
	
	private Logger log = LoggerFactory.getLogger(getClass());
	
	private static final long serialVersionUID = 1L;
	
	private GestureInstance gestureInstance;
	private ArrayList<GloveData> leftGloveData;
	private ArrayList<GloveData> rightGloveData;
	private ArrayList<Double> timeData;
	private Long startTime;
	
	private RecorderPanel recorderPanel   = null;
	private ControlPanel controlPanel = null;
	private LoggerPanel loggerPanel = null;
	private boolean IsRecordMode = false;
	
	private volatile boolean isListeningForEvents = true;
	private BlockingQueue<DeviceEvent> eventQueue = new LinkedBlockingQueue<DeviceEvent>();
	private DeviceNetworkListener gloveListener = new DeviceNetworkListener(31337, eventQueue);
	private Thread consumerThread;
	private Runnable doUpdateInterface;
	
	public MainWindow() {
		super();
		
		// Setup the frame's basic characteristics...
		this.setTitle("Super Street Fire (Gesture Recorder GUI)");
		this.setPreferredSize(new Dimension(900, 500));
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setLayout(new BorderLayout());
		
		// Setup the frame's contents...
		Container contentPane = this.getContentPane();
		
		JPanel wrapperPanel = new JPanel();
		wrapperPanel.setLayout(new BorderLayout());
		
		this.recorderPanel = new RecorderPanel();
		this.controlPanel = new ControlPanel();
		this.loggerPanel = new LoggerPanel();
		
		wrapperPanel.add(this.recorderPanel, BorderLayout.NORTH);
		wrapperPanel.add(this.controlPanel, BorderLayout.CENTER);
		wrapperPanel.add(this.loggerPanel, BorderLayout.SOUTH);
		contentPane.add(wrapperPanel, BorderLayout.NORTH);
		
		this.pack();
		this.setLocationRelativeTo(null);
		
		// Kick off the hardware event listener in the IOServer 
		Thread producerThread = new Thread(gloveListener);
		
		// To stop this call gloveListener.stop()
		producerThread.start();
		
		// To stop this set isListeningForEvents false and call consumerThread.interrupt()
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

	// Triggered from IOServer. Will most Set the coordinates data. If we're in record mode, save that data too.
	public void hardwareEventListener(GloveEvent gloveEvent) {
		if (gloveEvent.isButtonPressed())
		{
			// If we just started recording, mark the start time. This will be a brand new gesture and file.
			if (!this.IsRecordMode)
			{
				this.recorderPanel.fileInfoPanel.isNewFile = true; 
				this.leftGloveData = new ArrayList<GloveData>();
				this.rightGloveData = new ArrayList<GloveData>();
				this.timeData = new ArrayList<Double>();
				this.startTime = System.nanoTime();
			}
			
			this.IsRecordMode = true;
		}
		else
		{
			// If we're ending a recording, create and export the gesture instance object
			if(this.IsRecordMode)
			{
				GestureInstance instance = createGestureInstance();
				
				// If export to CSV is selected, perform the export
				if(this.recorderPanel.fileInfoPanel.exportCsv.getState())
				{
					this.recorderPanel.fileInfoPanel.exportToCsv(instance);
				}
				
				// If export to the gesture recognizer is selected, export to the GestureRecognizer
				if(this.recorderPanel.fileInfoPanel.exportRecognizer.getState())
				{
					this.recorderPanel.fileInfoPanel.exportToRecognizer(instance);
				}
			}
			
			this.IsRecordMode = false;
		}
		
		// Set the recording indicator
		this.controlPanel.recordingLabel.setVisible(this.IsRecordMode);
		
		// Build the gesture coordinates object and final vars to use in the UI thread
		final double time = getElapsedTime();
		final GloveData gloveData = buildGloveData(gloveEvent);
		final String gestureName = this.recorderPanel.fileInfoPanel.gestureName.getSelectedItem().toString();
		
		//TODO: Find out which glove we're using and add to the appropriate list
		this.leftGloveData.add(gloveData);
		this.rightGloveData.add(gloveData);
		this.timeData.add(time);
		
		// Set up a UI updating thread
		doUpdateInterface = new Runnable() {       	
            public void run() {
            	displayAndLogData(gloveData, gestureName, time);
            }
        };
		SwingUtilities.invokeLater(doUpdateInterface);
	}
	
	// Displays the coordinates in the UI and logs anything recorded on screen
	public void displayAndLogData(GloveData data, String gestureName, double time)
	{
		// Populate the displays on the GUI with the data we're sent depending on which glove to display
		this.recorderPanel.sensorDataPanelLeft.showCurrentData(data);
		this.recorderPanel.sensorDataPanelRight.showCurrentData(data);
		
		// Log the data on the UI
		this.loggerPanel.logGestureData(data, gestureName, time);
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
	
	// Gets the time elapsed since recording started in milliseconds
	public double getElapsedTime()
	{
		return (double)(System.nanoTime() - this.startTime) / 1000000;
	}
}
