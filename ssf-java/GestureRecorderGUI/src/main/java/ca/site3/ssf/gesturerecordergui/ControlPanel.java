package ca.site3.ssf.gesturerecordergui;

import java.awt.Color;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * A container for the recording start/stop buttons and related indicators
 * @author Mike
 *
 */
class ControlPanel extends JPanel {
	
	private static final long serialVersionUID = 1L;
	private JLabel recordingLabel = null;
	
	ControlPanel() {
		super();
		
		this.recordingLabel = new JLabel();
		this.recordingLabel.setForeground(Color.RED);
		this.recordingLabel.setText("NOW RECORDING");
		this.recordingLabel.setVisible(false);
		
		this.add(this.recordingLabel);
	}
	
	// Hide or show the recording label
	public void showRecordingLabel(boolean isRecordingMode)
	{
		this.recordingLabel.setVisible(isRecordingMode);
	}
}
