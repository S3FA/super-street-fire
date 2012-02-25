package ca.site3.ssf.gesturerecordergui;

import java.awt.Color;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

// A container panel for the hardware data displays
class SensorDataPanel extends JPanel {
	
	private static final long serialVersionUID = 1L;

	public JTextField gyroscopeData;
	public JTextField magnetometerData;
	public JTextField accelerometerData;
	
	SensorDataPanel() {
		super();
		
		Color borderColour = Color.black;
		
		TitledBorder border = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(borderColour), "Sensor Data");
		border.setTitleColor(Color.black);
		this.setBorder(border);
		
        GridBagLayout layout = new GridBagLayout();
		this.setLayout(layout);

		FormLayoutHelper formLayoutHelper = new FormLayoutHelper();
		
		this.gyroscopeData = new JTextField(15);
		this.gyroscopeData.setEditable(false);
		this.magnetometerData = new JTextField(15);
		this.magnetometerData.setEditable(false);
		this.accelerometerData = new JTextField(15);
		this.accelerometerData.setEditable(false);
		
		JLabel gyroscopeLabel = new JLabel("Gyroscope:");
		gyroscopeLabel.setForeground(Color.black);
		formLayoutHelper.addLabel(gyroscopeLabel, this);
		formLayoutHelper.addLastField(this.gyroscopeData, this);
		
		JLabel magnetometerLabel = new JLabel("Magnetometer:");
		magnetometerLabel.setForeground(Color.black);
		formLayoutHelper.addLabel(magnetometerLabel, this);
		formLayoutHelper.addLastField(this.magnetometerData, this);
		
		JLabel accelerometerLabel = new JLabel("Accelerometer:");
		accelerometerLabel.setForeground(Color.black);
		formLayoutHelper.addLabel(accelerometerLabel, this);
		formLayoutHelper.addLastField(this.accelerometerData, this);
	}
}
