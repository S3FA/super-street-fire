package ca.site3.ssf.gesturerecordergui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.site3.ssf.gesturerecognizer.GestureInstance;
import ca.site3.ssf.ioserver.DeviceEvent;
import ca.site3.ssf.ioserver.GloveEvent;
import ca.site3.ssf.ioserver.DeviceEvent.Type;
import ca.site3.ssf.ioserver.DeviceNetworkListener;

// The main window for the recorder GUI.
public class MainWindow extends JFrame {
	
	private Logger log = LoggerFactory.getLogger(getClass());
	
	private static final long serialVersionUID = 1L;
	private GestureData gesture = null;
	private RecorderPanel recorderPanel   = null;
	private ControlPanel controlPanel = null;
	private LoggerPanel loggerPanel = null;
	private boolean IsRecordMode = false;
	
	private volatile boolean isListeningForEvents = true;
	private BlockingQueue<DeviceEvent> eventQueue = new LinkedBlockingQueue<DeviceEvent>();
	private DeviceNetworkListener gloveListener = new DeviceNetworkListener(31337, eventQueue);
	private Thread consumerThread;
	
	
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
		// to stop this call gloveListener.stop()
		producerThread.start();
		
		// to stop this set isListeningForEvents false and call consumerThread.interrupt()
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
					if (null != e && e.getType() == Type.GloveEvent) {
						hardwareEventListener((GloveEvent)e);
					}
				}
			}
		});
		
		/* For debugging/testing purposes only */
		this.recorderPanel.fileInfoPanel.isNewFile = true;
		this.recorderPanel.fileInfoPanel.exportCsv.setState(true);
		this.recorderPanel.fileInfoPanel.exportRecognizer.setState(true);
		this.IsRecordMode = true;
		
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
	// Should accept an IOServer.GloveData object from IOServer
	public void hardwareEventListener(GloveEvent gloveEvent) {
		if (false)//event.getSource() == recordButtonPress from IOServer)
		{
			// Toggle record mode. Currently we're assuming one record mode/trigger for both gloves, we can split it if needed
			this.IsRecordMode = !this.IsRecordMode;
			this.recorderPanel.fileInfoPanel.isNewFile = true;
			this.controlPanel.recordingLabel.setVisible(this.IsRecordMode);
		}
		else if (true){//event.getSource() == someHardwareEvent from IOServer)
			
			// Build the gesture coordinates object
			BuildGestureObject();
			DisplayAndLogData();
			
			// If we're recording, do some additional actions with the data object
			if(this.IsRecordMode)
			{
				// Export to the gesture recognizer if selected
				if (this.recorderPanel.fileInfoPanel.exportRecognizer.getState()){
					SendDataToGestureRecognizer();
				}
				
				// Write the results to a CSV file if selected
				if (this.recorderPanel.fileInfoPanel.exportCsv.getState()){
					this.recorderPanel.fileInfoPanel.recordFileInformation(this.gesture.gyroLeft,
						this.gesture.magLeft,
						this.gesture.accLeft,
						this.gesture.gyroRight,
						this.gesture.magRight,
						this.gesture.accRight,
						this.gesture.GestureName,
						this.gesture.Time);
				}
				
				this.recorderPanel.fileInfoPanel.isNewFile = false;
			}
		}
	}
	
	
	
	// Constructs a gesture object from the data sent by the IOServer
	public void BuildGestureObject()
	{
		Calendar cal = Calendar.getInstance();
	    SimpleDateFormat sdf = new SimpleDateFormat("H:mm:ss:SSS");
	    
		this.gesture = new GestureData();
		this.gesture.gyroLeft = "0";
		this.gesture.magLeft = "0";
		this.gesture.accLeft = "0";
		this.gesture.gyroRight = "0";
		this.gesture.magRight = "0";
		this.gesture.accRight = "0";
		this.gesture.GestureName = this.recorderPanel.fileInfoPanel.gestureName.getText() == "" ? "Unspecified" : this.recorderPanel.fileInfoPanel.gestureName.getText();
		this.gesture.Time = sdf.format(cal.getTime());
	}
	
	// Displays the coordinates in the UI and logs anything recorded on screen
	public void DisplayAndLogData()
	{
		// Populate the displays on the GUI with the data we're sent
		this.recorderPanel.sensorDataPanelLeft.showCurrentData(this.gesture.gyroLeft, this.gesture.magLeft, this.gesture.accLeft);
		this.recorderPanel.sensorDataPanelRight.showCurrentData(this.gesture.gyroRight, this.gesture.magRight, this.gesture.accRight);
		
		// Log the data on the UI
		this.loggerPanel.logGestureData(this.gesture.gyroLeft,
			this.gesture.magLeft,
			this.gesture.accLeft,
			this.gesture.gyroRight,
			this.gesture.magRight,
			this.gesture.accRight,
			this.gesture.GestureName,
			this.gesture.Time);
	}
	
	// Get a string of instance data from the Gesture Recognizer
	public void GetDataFromGestureRecognizer(){
		GestureInstance gestureInstance = new GestureInstance();
		//String gestureInstanceData = gestureInstance.toDataString();
		
		// Convert gestureInstanceData to a GestureData object. 
		//TODO: What format does gesture instance need? 
	}
	
	// Send a data string to the Gesture Recognizer
	public void SendDataToGestureRecognizer(){
		GestureInstance gestureInstance = new GestureInstance();
		String formattedGestureData = "";
		
		// Convert the data to a string readable by the gesture recognizer
		//TODO: How is this data string formatted?
		
		//gestureInstance.fromDataString(formattedGestureData);
		
		// Log and/or record the data?
	}
}
