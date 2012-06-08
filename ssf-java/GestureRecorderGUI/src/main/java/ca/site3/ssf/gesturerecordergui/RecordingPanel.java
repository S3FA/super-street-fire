package ca.site3.ssf.gesturerecordergui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import ca.site3.ssf.gesturerecognizer.GestureInstance;
import ca.site3.ssf.gesturerecognizer.GestureType;
import ca.site3.ssf.gesturerecognizer.GloveData;

/**
 * A container for displaying sensor data, creating gesture instances and saving them to a file
 * @author Mike
 *
 */
class RecordingPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	
	private FileInfoPanel fileInfoPanel;
	private LoggerPanel loggerPanel;
	private boolean isRecordMode;
	
	RecordingPanel() {
		super();
		
		TitledBorder border = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), "Gesture Recorder");
		border.setTitleColor(Color.black);
		this.setBorder(border);

		this.loggerPanel = new LoggerPanel("Log");
		this.loggerPanel.setTextAreaSize(20, 100);

		this.fileInfoPanel = new FileInfoPanel();
		
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		topPanel.add(this.fileInfoPanel);
		
		this.setLayout(new BorderLayout());
		this.add(topPanel, BorderLayout.NORTH);
		this.add(this.loggerPanel, BorderLayout.CENTER);
		
		this.isRecordMode = false;
	}
	
	public GestureType getSelectedGesture() {
		return this.fileInfoPanel.getSelectedGesture();
	}
	
	// Displays the coordinates in the UI and logs anything recorded on screen
	public void displayAndLogData(GloveData data, String gestureName, double time)
	{
		// Log the data on the UI
		this.loggerPanel.logGestureData(data, gestureName, time);
	}
	public void setLogString(String str) {
		this.loggerPanel.setLogText(str);
	}
	
	// Sets recording mode
	public void setRecordMode(boolean isRecordingMode) {
		this.isRecordMode = isRecordingMode;
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
