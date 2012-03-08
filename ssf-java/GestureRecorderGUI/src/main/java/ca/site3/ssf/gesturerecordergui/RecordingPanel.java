package ca.site3.ssf.gesturerecordergui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagLayout;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import ca.site3.ssf.gesturerecognizer.GestureInstance;
import ca.site3.ssf.gesturerecognizer.GloveData;

/**
 * A container for displaying sensor data, creating gesture instances and saving them to a file
 * @author Mike
 *
 */
class RecordingPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	
	private SensorDataPanel sensorDataPanelLeft;
	private SensorDataPanel sensorDataPanelRight;
	private FileInfoPanel fileInfoPanel;
	private ControlPanel controlPanel;
	private LoggerPanel loggerPanel;
	private boolean isRecordMode;
	
	RecordingPanel() {
		super();
		
		TitledBorder border = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), "Gesture Recorder");
		border.setTitleColor(Color.black);
		this.setBorder(border);

		FormLayoutHelper formLayoutHelper = new FormLayoutHelper();
		
		GridBagLayout layout = new GridBagLayout();
		JPanel stateInfoPanel = new JPanel();
        stateInfoPanel.setLayout(layout);
        this.setLayout(layout);
        
		this.controlPanel = new ControlPanel();
		this.loggerPanel = new LoggerPanel("Log");

		this.sensorDataPanelLeft = new SensorDataPanel("Left");
		this.sensorDataPanelRight = new SensorDataPanel("Right");
		this.fileInfoPanel = new FileInfoPanel();
		stateInfoPanel.setLayout(new GridLayout(1,2));
		
		stateInfoPanel.add(this.sensorDataPanelLeft, BorderLayout.WEST);
		stateInfoPanel.add(this.sensorDataPanelRight, BorderLayout.EAST);
		stateInfoPanel.add(this.fileInfoPanel, BorderLayout.SOUTH);
		
		formLayoutHelper.addLastField(stateInfoPanel, this);
		formLayoutHelper.addLastField(this.controlPanel, this);
		formLayoutHelper.addLastField(this.loggerPanel, this);
		
		this.isRecordMode = false;
	}
	
	// Displays the coordinates in the UI and logs anything recorded on screen
	public void displayAndLogData(GloveData data, String gestureName, double time)
	{
		// Populate the displays on the GUI with the data we're sent depending on which glove to display
		this.sensorDataPanelLeft.showCurrentData(data);
		this.sensorDataPanelRight.showCurrentData(data);
		
		// Log the data on the UI
		this.loggerPanel.logGestureData(data, gestureName, time);
	}
	
	// Sets recording mode
	public void setRecordMode(boolean isRecordingMode)
	{
		this.isRecordMode = isRecordingMode;
		this.controlPanel.showRecordingLabel(isRecordingMode);
	}
	
	// Gets the recording mode
	public boolean getRecordMode()
	{
		return this.isRecordMode;
	}
	
	// Sets whether we're making a new file
	public void setNewFile(boolean isNewFile)
	{
		this.fileInfoPanel.setNewFile(isNewFile);
	}
	
	// Gets whether we're making a new file
	public boolean getNewFile()
	{
		return this.fileInfoPanel.getNewFile();
	}
	
	public boolean getCsvExportState()
	{
		return this.fileInfoPanel.getCsvExportState();
	}
	
	public void exportToCsv(GestureInstance instance)
	{
		this.fileInfoPanel.exportToCsv(instance);
	}
	
	public boolean getRecognizerExportState()
	{
		return this.fileInfoPanel.getRecognizerExportState();
	}
	
	public void exportToRecognizer(GestureInstance instance)
	{
		this.fileInfoPanel.exportToRecognizer(instance);
	}
	
	public String getGestureName()
	{
		return this.fileInfoPanel.getGestureName();
	}
}
