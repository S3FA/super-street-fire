package ca.site3.ssf.devgui;

import java.awt.Color;
import java.awt.GridBagLayout;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.ToolTipManager;
import javax.swing.border.TitledBorder;

@SuppressWarnings("serial")
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
	
	private static final DateFormat DATE_FORMAT = new SimpleDateFormat("HH:mm:ss");
	
	private JLabel ipAddress;
	private JProgressBar rssiMeter;
	private JProgressBar batteryMeter;
	private long lastUpdate = -1;
	private JLabel lastUpdateLabel;
	
	GloveDataInfoPanel(GloveType gloveType) {
		
		TitledBorder border = BorderFactory.createTitledBorder(
				BorderFactory.createLineBorder(Color.black), gloveType.toString());
		border.setTitleColor(Color.black);
		this.setBorder(border);
		
		this.ipAddress = new JLabel();
		this.setIPAddress(NOT_CONNECTED_STR);
		
		this.rssiMeter = new JProgressBar(JProgressBar.HORIZONTAL, 0, 100);
		this.rssiMeter.setBorderPainted(true);
		this.rssiMeter.setStringPainted(true);
		this.setSignalPercent(0.0f);
		
		this.batteryMeter = new JProgressBar(JProgressBar.HORIZONTAL, 0, 100);
		this.batteryMeter.setBorderPainted(true);
		this.batteryMeter.setStringPainted(true);
		this.setBatteryPercent(0.0f);
		
		this.lastUpdateLabel = new JLabel();
		this.setLastUpdateTime(-1);
		
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
		
		JLabel updatedLabel = new JLabel("Last Updated:");
		formLayoutHelper.addLabel(updatedLabel, this);
		formLayoutHelper.addLastField(this.lastUpdateLabel, this);
		
		ToolTipManager.sharedInstance().registerComponent(this);
	}
	
	void setIPAddress(String ipAddressStr) {
		if (ipAddressStr == null || ipAddressStr.isEmpty()) {
			this.ipAddress.setText(NOT_CONNECTED_STR);
		}
		else {
			this.ipAddress.setText(ipAddressStr);
		}
	}
	
	void setBatteryPercent(float percent) {
		ProgressBarColourLerp.setPercentageAndRedToGreenColour(this.batteryMeter, percent);
	}
	
	void setSignalPercent(float percent) {
		ProgressBarColourLerp.setPercentageAndRedToGreenColour(this.rssiMeter, percent);
	}
	
	void setLastUpdateTime(long time) {
		this.lastUpdate = time;
		if (time < 0) {
			this.lastUpdateLabel.setText("[never]");
		} else {
			this.lastUpdateLabel.setText(DATE_FORMAT.format(new Date(time)));
		}
	}
	
	@Override
	public String getToolTipText() {
		if (lastUpdate == -1) {
			lastUpdate = 0;
			return null; // weirdness due to setToolTipText. See http://stackoverflow.com/questions/6441242/java-swing-how-to-be-notified-if-tool-tip-is-about-to-become-visible
		} else if (lastUpdate == 0) {
			return "No updates";
		} else {
			long secondsAgo = Math.round((System.currentTimeMillis() - lastUpdate) / 1000);
			return "Last heartbeat received " + secondsAgo + " seconds ago";
		}
	}
}
