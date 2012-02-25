package ca.site3.ssf.gesturerecordergui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagLayout;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

// A container panel for the sensor data and the file info panels
class RecorderPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	
	public SensorDataPanel sensorDataPanel;
	public FileInfoPanel fileInfoPanel;
	
	RecorderPanel() {
		super();
		
		TitledBorder border = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), "Gesture Recorder");
		border.setTitleColor(Color.black);
		this.setBorder(border);

		JPanel stateInfoPanel = new JPanel();
		
        GridBagLayout layout = new GridBagLayout();
        stateInfoPanel.setLayout(layout);

		this.sensorDataPanel = new SensorDataPanel();
		this.fileInfoPanel = new FileInfoPanel();
		this.setLayout(new GridLayout(0,2));
		
		this.add(this.sensorDataPanel, BorderLayout.WEST);
		this.add(this.fileInfoPanel, BorderLayout.EAST);
	}
}