package ca.site3.ssf.devgui;

import java.awt.Color;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.border.TitledBorder;

import ca.site3.ssf.gamemodel.GameModel;
import ca.site3.ssf.gamemodel.IGameModel;

class GloveDataInfoPanel extends JPanel {
	
	enum GloveType {
		LEFT_GLOVE("Left Glove"),
		RIGHT_GLOVE("Right Glove");
		
		private final String name;
		
		GloveType(String name) {
			this.name = name;
		}
		public String toString() {
			return this.name;
		}
	};
	
	private static final String NOT_CONNECTED_STR = "Not Connected";
	
	private JLabel ipAddress;
	private JProgressBar rssiMeter;
	private JProgressBar batteryMeter;
	
	GloveDataInfoPanel(GloveType gloveType) {
		
		TitledBorder border = BorderFactory.createTitledBorder(
				BorderFactory.createLineBorder(Color.black), gloveType.toString());
		border.setTitleColor(Color.black);
		this.setBorder(border);
		
		this.ipAddress = new JLabel(NOT_CONNECTED_STR);
		
		this.rssiMeter = new JProgressBar(JProgressBar.HORIZONTAL, 0, 100);
		this.rssiMeter.setBorderPainted(true);
		this.rssiMeter.setStringPainted(true);
		this.batteryMeter = new JProgressBar(JProgressBar.HORIZONTAL, 0, 100);
		this.batteryMeter.setBorderPainted(true);
		this.batteryMeter.setStringPainted(true);
		
        GridBagLayout layout = new GridBagLayout();
		this.setLayout(layout);
		FormLayoutHelper formLayoutHelper = new FormLayoutHelper();
		
		JLabel ipAddrLabel = new JLabel("IP Address:");
		formLayoutHelper.addLabel(ipAddrLabel, this);
		formLayoutHelper.addLastField(this.ipAddress, this);
		
		JLabel rssiLabel = new JLabel("Signal:");
		formLayoutHelper.addLabel(rssiLabel, this);
		formLayoutHelper.addLastField(this.rssiMeter, this);
		
		JLabel batteryLabel = new JLabel("Battery:");
		formLayoutHelper.addLabel(batteryLabel, this);
		formLayoutHelper.addLastField(this.batteryMeter, this);
	}
	
	void setIPAddress(String ipAddressStr) {
		this.ipAddress.setText(ipAddressStr);
	}
	
	void setBatteryPercent(float percent) {
		ProgressBarColourLerp.setPercentageAndRedToGreenColour(this.batteryMeter, percent);
	}
	
	void setSignalPercent(float percent) {
		ProgressBarColourLerp.setPercentageAndRedToGreenColour(this.rssiMeter, percent);
	}
	
	
	
}
