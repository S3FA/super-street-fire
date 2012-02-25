package ca.site3.ssf.gesturerecordergui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;

// A container panel for the start/stop buttons and their handlers
class ControlPanel extends JPanel implements ActionListener {
	
	private JButton startRecord = null;
	private JButton stopRecord = null;
	
	public boolean IsRecordMode = false;
	
	ControlPanel() {
		super();
		
		this.startRecord = new JButton("Record");
		this.startRecord.addActionListener(this);
		this.add(this.startRecord);
		
		this.stopRecord = new JButton("Stop");
		this.stopRecord.addActionListener(this);
		this.add(this.stopRecord);
	}

	// Set the recording state. Can be modified to look for hardware signals or a flag from Main instead of buttons
	public void actionPerformed(ActionEvent event) {
		if (event.getSource() == this.startRecord) {
			this.IsRecordMode = true;
		}
		else if (event.getSource() == this.stopRecord) {
			this.IsRecordMode = false;
		}
		
	}
}
