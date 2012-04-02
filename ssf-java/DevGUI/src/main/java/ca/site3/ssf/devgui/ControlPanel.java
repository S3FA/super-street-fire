package ca.site3.ssf.devgui;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.site3.ssf.gamemodel.ActionFactory;
import ca.site3.ssf.gamemodel.GameState;
import ca.site3.ssf.gesturerecognizer.GestureType;
import ca.site3.ssf.guiprotocol.StreetFireGuiClient;

@SuppressWarnings("serial")
class ControlPanel extends JPanel implements ActionListener {
	
	private Logger log = LoggerFactory.getLogger(getClass());
	
	private ActionFactory actionFactory = null;
	private StreetFireGuiClient client = null;
	
	private JButton killButton      = null;
	private JButton nextStateButton = null;
	private JButton pauseButton     = null;
	
	private JButton executeP1ActionButton 			= null;
	private JButton executeP2ActionButton 			= null;
	//private JButton executeRingmasterActionButton	= null;
	
	private JComboBox playerActionComboBox     = null;
	//private JComboBox ringmasterActionComboBox = null;
	
	
	

	ControlPanel(ActionFactory actionFactory, StreetFireGuiClient client) {
		super();
		
		this.actionFactory = actionFactory;
		this.client = client;
		assert(this.actionFactory != null && this.client != null);
		
		TitledBorder border = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), "Controls");
		border.setTitleColor(Color.black);
		this.setBorder(border);
		
		this.setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
		
		JPanel generalButtonPanel = new JPanel();
		generalButtonPanel.setLayout(new FlowLayout());
		
		
		this.nextStateButton = new JButton("Next State");
		this.nextStateButton.addActionListener(this);
		generalButtonPanel.add(this.nextStateButton);
	
		this.killButton = new JButton("Kill Game");
		this.killButton.addActionListener(this);
		generalButtonPanel.add(this.killButton);
		
		this.pauseButton = new JButton("Pause");
		this.pauseButton.addActionListener(this);
		generalButtonPanel.add(this.pauseButton);
		
		this.add(generalButtonPanel);
		
		String[] playerActionStrs = new String[GestureType.values().length];
		int count = 0;
		for (GestureType gestureType : GestureType.values()) {
			playerActionStrs[count++] = gestureType.toString();
		}
		
		this.playerActionComboBox  = new JComboBox(playerActionStrs);
		this.executeP1ActionButton = new JButton("Execute for Player 1");
		this.executeP1ActionButton.addActionListener(this);
		this.executeP2ActionButton = new JButton("Execute for Player 2");
		this.executeP2ActionButton.addActionListener(this);
		
		JPanel playerActionPanel = new JPanel();
		playerActionPanel.setLayout(new FlowLayout());
		playerActionPanel.add(this.playerActionComboBox);
		playerActionPanel.add(this.executeP1ActionButton);
		playerActionPanel.add(this.executeP2ActionButton);
		
		this.add(playerActionPanel);
	}

	public void actionPerformed(ActionEvent event) {
		try {
			if (event.getSource() == this.nextStateButton) {
				client.initiateNextState();
			}
			else if (event.getSource() == this.killButton) {
				client.killGame();
			}
			else if (event.getSource() == this.pauseButton) {
				client.togglePauseGame();
			}
			else if (event.getSource() == this.executeP1ActionButton) {
				this.executePlayerAction(1);
			}
			else if (event.getSource() == this.executeP2ActionButton) {
				this.executePlayerAction(2);
			}
		} catch (IOException ex) {
			log.warn("Exception communicating with IOServer",ex);
		}
	}
	
	void gameStateChanged(GameState.GameStateType stateType) {
		if (stateType.canBePausedOrUnpaused()) {
			
			if (stateType == GameState.GameStateType.PAUSED_STATE) {
				this.pauseButton.setText("Unpause");
			}
			else {
				this.pauseButton.setText("Pause");
			}
			this.pauseButton.setEnabled(true);
			
		}
		else {
			this.pauseButton.setEnabled(false);
		}
		
		this.killButton.setEnabled(stateType.isKillable());

		if (stateType.isGoToNextStateControllable()) {
			assert(stateType.nextControllableGoToState() != null);
			
			switch (stateType.nextControllableGoToState()) {
				case RINGMASTER_STATE:
					this.nextStateButton.setText("Enter Ringmaster State");	
					break;
				
				case ROUND_BEGINNING_STATE:
					this.nextStateButton.setText("Begin Round");
					break;
					
				default:
					assert(false);
					return;
			}
			
			this.nextStateButton.setEnabled(true);
		}
		else {
			this.nextStateButton.setEnabled(false);
		}
		
		switch (stateType) {
			case ROUND_IN_PLAY_STATE:
			case TIE_BREAKER_ROUND_STATE:
				this.setEnablePlayerActionControls(true);
				break;
			default:
				this.setEnablePlayerActionControls(false);
				break;
		}
		
	}
	

	private void executePlayerAction(int playerNum) {
		try {
			GestureType gesture = GestureType.valueOf(GestureType.class,
				this.playerActionComboBox.getSelectedItem().toString());
			client.executePlayerAction(playerNum, gesture.getActionFactoryType(), gesture.getUsesLeftHand(), gesture.getUsesRightHand());
		} catch (IllegalArgumentException ex) {
			assert(false);
		} catch (IOException ex) {
			log.warn("Could not execute player action",ex);
		}
	}
	
	
	private void setEnablePlayerActionControls(boolean enabled) {
		this.playerActionComboBox.setEnabled(enabled);
		this.executeP1ActionButton.setEnabled(enabled);
		this.executeP2ActionButton.setEnabled(enabled);
	}
	
}
