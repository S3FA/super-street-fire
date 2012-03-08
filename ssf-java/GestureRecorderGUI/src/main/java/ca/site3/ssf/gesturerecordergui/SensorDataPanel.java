package ca.site3.ssf.gesturerecordergui;

import java.awt.Color;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import ca.site3.ssf.gesturerecognizer.GloveData;

/**
 * A container to display the glove coordinates in real time
 * @author Mike
 *
 */
class SensorDataPanel extends JPanel {
	
	private static final long serialVersionUID = 1L;

	private JTextField gyroscopeData;
	private JTextField magnetometerData;
	private JTextField accelerometerData;
	
	SensorDataPanel(String title) {
		super();
		
		Color borderColour = Color.black;
		
		TitledBorder border = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(borderColour), "Sensor Data (" + title + ")");
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
	
	// Update the current data values on the UI
	public void showCurrentData(GloveData data)
	{
		this.gyroscopeData.setText(Double.toString(data.getGyroData().getX()) + ", " + Double.toString(data.getGyroData().getY()) + ", " + Double.toString(data.getGyroData().getZ())); 
		this.magnetometerData.setText(Double.toString(data.getMagnetoData().getX()) + ", " + Double.toString(data.getMagnetoData().getY()) + ", " + Double.toString(data.getMagnetoData().getZ()));
		this.accelerometerData.setText(Double.toString(data.getAccelData().getX()) + ", " + Double.toString(data.getAccelData().getY()) + ", " + Double.toString(data.getAccelData().getZ()));
	}
}
