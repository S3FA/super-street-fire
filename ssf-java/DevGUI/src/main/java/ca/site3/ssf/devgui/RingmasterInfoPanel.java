package ca.site3.ssf.devgui;

import java.awt.Color;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.border.TitledBorder;

class RingmasterInfoPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	private JLabel timeOfLastAction;
	
	RingmasterInfoPanel() {
		super();
		
		TitledBorder border = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(ArenaDisplay.RINGMASTER_COLOUR), "Ringmaster");
		border.setTitleColor(Color.black);
		this.setBorder(border);
		
        GridBagLayout layout = new GridBagLayout();
		this.setLayout(layout);

		FormLayoutHelper formLayoutHelper = new FormLayoutHelper();
		
		this.timeOfLastAction = new JLabel("N/A");
		this.timeOfLastAction.setForeground(Color.black);		
		
		JLabel timeLabel = new JLabel("Time of Last Action:");
		timeLabel.setForeground(Color.black);
		formLayoutHelper.addLabel(timeLabel, this);
		formLayoutHelper.addLastField(this.timeOfLastAction, this);
	}
	
	void setLastAction(double clockTime) {
		this.timeOfLastAction.setText("T-" + clockTime);
	}
	
}
