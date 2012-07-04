package ca.site3.ssf.devgui;

import java.awt.Color;
import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.border.TitledBorder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.site3.ssf.gamemodel.PlayerAttackAction;
import ca.site3.ssf.guiprotocol.StreetFireGuiClient;

class PlayerInfoPanel extends JPanel implements ItemListener {
	
	private static final long serialVersionUID = 1L;
	
	private int playerNum;
	
	private JProgressBar lifeBar;
	private JLabel lastAction;
	private JLabel timeOfLastAction;
	
	private JCheckBox unlimitedMovesCheckBox;
	
	private StreetFireGuiClient client = null;
	
	private Logger log = LoggerFactory.getLogger(getClass());
	
	PlayerInfoPanel(StreetFireGuiClient client, int playerNum) {
		super();
		
		assert(client != null);
		this.client = client;
		
		Color borderColour = null;
		if (playerNum == 1) {
			borderColour = ArenaDisplay.PLAYER_1_COLOUR;
		}
		else {
			borderColour = ArenaDisplay.PLAYER_2_COLOUR;
		}
		
		this.playerNum = playerNum;
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
		
		this.unlimitedMovesCheckBox = new JCheckBox("Unlimited Moves");
		this.unlimitedMovesCheckBox.addItemListener(this);
		this.unlimitedMovesCheckBox.setVisible(true);
		this.unlimitedMovesCheckBox.setEnabled(true);
		this.unlimitedMovesCheckBox.setSelected(false);
		//JLabel unlimitedMovesLabel = new JLabel("Unlimited Moves:");
		//unlimitedMovesLabel.setForeground(Color.black);
		//formLayoutHelper.addLabel(unlimitedMovesLabel, this);
		formLayoutHelper.addLastField(this.unlimitedMovesCheckBox, this);
		
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

	void setUnlimitedMovesCheckBoxEnabled(boolean enabled) {
		this.unlimitedMovesCheckBox.setVisible(enabled);
		this.unlimitedMovesCheckBox.setEnabled(enabled);
	}
	
	@Override
	public void itemStateChanged(ItemEvent event) {
		if (event.getSource() == this.unlimitedMovesCheckBox) {
			try {
				this.client.updatePlayerStatus(this.playerNum, event.getStateChange() == ItemEvent.SELECTED);
			}
			catch (IOException e) {
				log.warn("Failed to update unlimited move attribute of player status.", e);
			}
		}
	}	
}
