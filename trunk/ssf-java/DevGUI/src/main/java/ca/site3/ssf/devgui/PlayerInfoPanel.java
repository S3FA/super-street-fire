package ca.site3.ssf.devgui;

import java.awt.Color;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.border.TitledBorder;

import ca.site3.ssf.gamemodel.PlayerAttackAction;

class PlayerInfoPanel extends JPanel {
	
	private static final long serialVersionUID = 1L;
	
	private JProgressBar lifeBar;
	private JLabel lastAction;
	private JLabel timeOfLastAction;
	
	PlayerInfoPanel(int playerNum) {
		super();
		
		Color borderColour = null;
		if (playerNum == 1) {
			borderColour = ArenaDisplay.PLAYER_1_COLOUR;
		}
		else {
			borderColour = ArenaDisplay.PLAYER_2_COLOUR;
		}
		
		TitledBorder border = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(borderColour), "Player " + playerNum);
		border.setTitleColor(Color.black);
		this.setBorder(border);
		
        GridBagLayout layout = new GridBagLayout();
		this.setLayout(layout);

		FormLayoutHelper formLayoutHelper = new FormLayoutHelper();
		
		this.lifeBar = new JProgressBar(JProgressBar.HORIZONTAL, 0, 100);
		this.lifeBar.setBorderPainted(true);
		this.lifeBar.setStringPainted(true);
		this.setLife(0);
		
		JLabel lifeLabel = new JLabel("Life:");
		lifeLabel.setForeground(Color.black);
		
		formLayoutHelper.addLabel(lifeLabel, this);
		formLayoutHelper.addLastField(this.lifeBar, this);

		this.lastAction = new JLabel("N/A");
		this.lastAction.setForeground(Color.black);
		
		JLabel lastActionLabel = new JLabel("Last Action:");
		lastActionLabel.setForeground(Color.black);
		formLayoutHelper.addLabel(lastActionLabel, this);
		formLayoutHelper.addLastField(this.lastAction, this);
		
		this.timeOfLastAction = new JLabel("N/A");
		this.timeOfLastAction.setForeground(Color.black);		
		
		JLabel timeLabel = new JLabel("Time of Last Action:");
		timeLabel.setForeground(Color.black);
		formLayoutHelper.addLabel(timeLabel, this);
		formLayoutHelper.addLastField(this.timeOfLastAction, this);
		
	}
	
	void setLife(float lifePercent) {
		ProgressBarColourLerp.setPercentageAndRedToGreenColour(this.lifeBar, lifePercent);
	}
	
	void setLastActionAsAttack(PlayerAttackAction.AttackType actionType, double clockTime) {
		
		if (actionType == null) {
			this.lastAction.setText("N/A");
			this.timeOfLastAction.setText("N/A");
			return;
		}
		
		this.lastAction.setText(actionType.toString());
		this.timeOfLastAction.setText("" + clockTime);
	}
	void setLastActionAsBlock(double clockTime) {
		this.lastAction.setText("BLOCK");
		this.timeOfLastAction.setText("" + clockTime);
	}	
}
