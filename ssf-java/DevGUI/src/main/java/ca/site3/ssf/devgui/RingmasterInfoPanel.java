package ca.site3.ssf.devgui;

import java.awt.Color;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

class RingmasterInfoPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	private JLabel lastAction;
	
	RingmasterInfoPanel() {
		super();
		
		TitledBorder border = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(ArenaDisplay.RINGMASTER_COLOUR), "Ringmaster");
		border.setTitleColor(Color.black);
		this.setBorder(border);
		
        GridBagLayout layout = new GridBagLayout();
		this.setLayout(layout);

		FormLayoutHelper formLayoutHelper = new FormLayoutHelper();
		
		this.lastAction = new JLabel("N/A");
		this.lastAction.setForeground(Color.black);
		
		JLabel lastActionLabel = new JLabel("Last Action:");
		lastActionLabel.setForeground(Color.black);
		formLayoutHelper.addLabel(lastActionLabel, this);
		formLayoutHelper.addLastField(this.lastAction, this);

	}
	
	void setLastAction(ca.site3.ssf.gamemodel.RingmasterAction.ActionType actionType, double clockTime) {
		this.lastAction.setText(actionType.toString());
	}
	
}
