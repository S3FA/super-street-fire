package ca.site3.ssf.gesturerecordergui;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

// A container panel for the start/stop buttons and their handlers
class ControlPanel extends JPanel {
	
	public JLabel recordingLabel = null;
	
	ControlPanel() {
		super();
		
		this.recordingLabel = new JLabel();
		this.recordingLabel.setForeground(Color.RED);
		this.recordingLabel.setText("NOW RECORDING");
		this.recordingLabel.setVisible(false);
		
		this.add(this.recordingLabel);
	}
}
