package ca.site3.ssf.devgui;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.site3.ssf.gamemodel.ActionFactory;
import ca.site3.ssf.gamemodel.GameState;
import ca.site3.ssf.gamemodel.GameState.GameStateType;
import ca.site3.ssf.gesturerecognizer.GestureType;
import ca.site3.ssf.guiprotocol.StreetFireGuiClient;

@SuppressWarnings("serial")
class ControlPanel extends JPanel implements ActionListener {
	
	private Logger log = LoggerFactory.getLogger(getClass());
	
	private ActionFactory actionFactory = null;
	private StreetFireGuiClient client = null;
	
	private JButton killButton       = null;
	private JButton nextStateButton1 = null;
	private JButton nextStateButton2 = null;
	private JButton pauseButton      = null;
	private JButton testButton       = null;
	
	private JButton executeP1ActionButton 			= null;
	private JButton executeP2ActionButton 			= null;
	private JButton executeRingmasterActionButton	= null;
	
	private JComboBox player1ActionComboBox    = null;
	private JComboBox player2ActionComboBox    = null;
	private JComboBox ringmasterActionComboBox = null;
	
	private List<GameStateType> nextStates = new ArrayList<GameStateType>(2);
	
	private DevGUIMainWindow mainWindow = null;
	
	ControlPanel(DevGUIMainWindow mainWindow, ActionFactory actionFactory, StreetFireGuiClient client) {
		
		assert(mainWindow != null);
		this.mainWindow = mainWindow;
		
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		
		this.actionFactory = actionFactory;
		this.client = client;
		assert(this.actionFactory != null && this.client != null);
		
		this.setBorder(new EmptyBorder(0, 0, 0, 0));
		this.setLayout(new FlowLayout());
		
		JPanel generalButtonPanel = new JPanel();
		generalButtonPanel.setBorder(new EmptyBorder(0, 0, 0, 0));
		
		this.nextStateButton1 = new JButton("Enter Ringmaster State");
		this.nextStateButton1.addActionListener(this);
		generalButtonPanel.add(this.nextStateButton1);
		
		this.nextStateButton2 = new JButton("Test Round");
		this.nextStateButton2.addActionListener(this);
		generalButtonPanel.add(this.nextStateButton2);

		this.killButton = new JButton("Kill Game");
		this.killButton.addActionListener(this);
		generalButtonPanel.add(this.killButton);
		
		this.pauseButton = new JButton("Pause");
		this.pauseButton.addActionListener(this);
		generalButtonPanel.add(this.pauseButton);
		
		this.testButton = new JButton("Test System");
		this.testButton.addActionListener(this);
		generalButtonPanel.add(this.testButton);
		
		this.add(generalButtonPanel);
		
		Collection<String> playerActionStrs = new ArrayList<String>(GestureType.values().length);
		Collection<String> ringmasterActionStrs = new ArrayList<String>(GestureType.values().length);

		for (GestureType gestureType : GestureType.values()) {
			if (gestureType.getIsRingmasterGesture()) {
				ringmasterActionStrs.add(gestureType.toString());
			}
			else {
				playerActionStrs.add(gestureType.toString());
			}
		}
		
		this.player1ActionComboBox    = new JComboBox(playerActionStrs.toArray());
		this.player2ActionComboBox    = new JComboBox(playerActionStrs.toArray());
		this.ringmasterActionComboBox = new JComboBox(ringmasterActionStrs.toArray());
		this.executeP1ActionButton = new JButton("Execute P1");
		this.executeP1ActionButton.addActionListener(this);
		this.executeP2ActionButton = new JButton("Execute P2");
		this.executeP2ActionButton.addActionListener(this);
		this.executeRingmasterActionButton = new JButton("Execute for Ringmaster");
		this.executeRingmasterActionButton.addActionListener(this);
		
		player1ActionComboBox.setMaximumSize(new Dimension(160, Integer.MAX_VALUE));
		player2ActionComboBox.setMaximumSize(new Dimension(160, Integer.MAX_VALUE));
		
		JPanel actionPanel = new JPanel();
		actionPanel.setLayout(new BoxLayout(actionPanel, BoxLayout.X_AXIS));
		actionPanel.add(this.player1ActionComboBox);
		actionPanel.add(this.executeP1ActionButton);
		actionPanel.add(this.player2ActionComboBox);
		actionPanel.add(this.executeP2ActionButton);
		actionPanel.add(this.ringmasterActionComboBox);
		actionPanel.add(this.executeRingmasterActionButton);
		
		this.add(actionPanel);
	}

	public void actionPerformed(ActionEvent event) {
		try {
			if (event.getSource() == this.nextStateButton1) {
				if (this.nextStates.size() == 0) {
					assert(false);
					return;
				}
				client.initiateNextState(this.nextStates.get(0));
			}
			else if (event.getSource() == this.nextStateButton2) {
				if (this.nextStates.size() <= 1) {
					assert(false);
					return;
				}
				client.initiateNextState(this.nextStates.get(1));
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
			else if (event.getSource() == this.executeRingmasterActionButton) {
				this.executeRingmasterAction();
			}
			else if (event.getSource() == this.testButton) {
				client.testSystem();
			}
		}
		catch (IOException ex) {
			log.warn("Exception communicating with IOServer",ex);
			this.mainWindow.displayNotConnectedDialog(true);
			while (!client.isConnected()) {
				try {
					client.connect();
				}
				catch (IOException e) {
				}
			}
			this.mainWindow.displayNotConnectedDialog(false);
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
			List<GameStateType> nextGoToStates = stateType.nextControllableGoToStates();
			assert(nextGoToStates != null);
			assert(nextGoToStates.size() <= 2);
			
			this.nextStates = new ArrayList<GameStateType>(nextGoToStates);	
			Collections.copy(this.nextStates, nextGoToStates);
			
			switch (nextGoToStates.get(0)) {
			
				case RINGMASTER_STATE:
					this.nextStateButton1.setText("Enter Ringmaster State");
					break;
					
				case ROUND_BEGINNING_STATE:
					this.nextStateButton1.setText("Begin Round");
					break;
				
				default:
					assert(false);
					return;
			}
			this.nextStateButton1.setEnabled(true);
			
			if (nextGoToStates.size() == 2) {
				switch (nextGoToStates.get(1)) {
				case TEST_ROUND_STATE:
					this.nextStateButton2.setText("Test Round");
					break;
				default:
					assert(false);
					break;
				}
				this.nextStateButton2.setVisible(true);
				this.nextStateButton2.setEnabled(true);
			}
			else {
				this.nextStateButton2.setVisible(false);
				this.nextStateButton2.setEnabled(false);
			}
		}
		else {
			this.nextStateButton1.setEnabled(false);
			this.nextStateButton2.setVisible(false);
			this.nextStateButton2.setEnabled(false);
		}
		
		switch (stateType) {
		
			case ROUND_IN_PLAY_STATE:
			case TIE_BREAKER_ROUND_STATE:
			case TEST_ROUND_STATE:
				this.setEnableActionControls(true, false);
				break;
				
			case RINGMASTER_STATE:
				this.setEnableActionControls(false, true);
				break;
				
			default:
				this.setEnableActionControls(false, false);
				break;
		}
		
		this.invalidate();
	}

	private void executePlayerAction(int playerNum) {
		try {
			GestureType gesture = null;
			
			if (playerNum == 1) {
				gesture = GestureType.valueOf(GestureType.class, this.player1ActionComboBox.getSelectedItem().toString());
			}
			else {
				gesture = GestureType.valueOf(GestureType.class, this.player2ActionComboBox.getSelectedItem().toString());
			}
			
			assert(gesture != null);
			client.executePlayerAction(playerNum, gesture.getActionFactoryType(), gesture.getUsesLeftHand(), gesture.getUsesRightHand());
		}
		catch (IllegalArgumentException ex) {
			assert(false);
		}
		catch (IOException ex) {
			log.warn("Could not execute player action",ex);
		}
	}
	
	private void executeRingmasterAction() {
		try {
			GestureType gesture = GestureType.valueOf(GestureType.class,
				this.ringmasterActionComboBox.getSelectedItem().toString());
			client.executeRingmasterAction(gesture.getActionFactoryType(), gesture.getUsesLeftHand(), gesture.getUsesRightHand());
		}
		catch (IllegalArgumentException ex) {
			assert(false);
		}
		catch (IOException ex) {
			log.warn("Could not execute player action",ex);
		}
	}
	
	private void setEnableActionControls(boolean enabledPlayerControls, boolean enabledRingmasterControls) {
		this.player1ActionComboBox.setVisible(enabledPlayerControls);
		this.player2ActionComboBox.setVisible(enabledPlayerControls);
		this.executeP1ActionButton.setVisible(enabledPlayerControls);
		this.executeP2ActionButton.setVisible(enabledPlayerControls);
		this.ringmasterActionComboBox.setVisible(enabledRingmasterControls);
		this.executeRingmasterActionButton.setVisible(enabledRingmasterControls);
	}
	
}
