package ca.site3.ssf.devgui;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Queue;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import ca.site3.ssf.gamemodel.AbstractGameModelCommand;
import ca.site3.ssf.gamemodel.Action;
import ca.site3.ssf.gamemodel.ActionFactory;
import ca.site3.ssf.gamemodel.ExecuteGenericActionCommand;
import ca.site3.ssf.gamemodel.InitiateNextStateCommand;
import ca.site3.ssf.gesturerecognizer.GestureType;

@SuppressWarnings("serial")
class ControlPanel extends JPanel implements ActionListener {
	
	private ActionFactory actionFactory = null;
	private Queue<AbstractGameModelCommand> commandQueue = null;
	
	private JButton nextStateButton = null;
	
	
	private JButton executeP1ActionButton 			= null;
	private JButton executeP2ActionButton 			= null;
	//private JButton executeRingmasterActionButton	= null;
	
	private JComboBox playerActionComboBox     = null;
	//private JComboBox ringmasterActionComboBox = null;
	
	
	
	
	ControlPanel(ActionFactory actionFactory, Queue<AbstractGameModelCommand> commandQueue) {
		super();
		
		this.actionFactory = actionFactory;
		this.commandQueue = commandQueue;
		assert(actionFactory != null && commandQueue != null);
		
		TitledBorder border = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), "Controls");
		border.setTitleColor(Color.black);
		this.setBorder(border);
		
		this.setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
		
		this.nextStateButton = new JButton("Initiate Next State");
		this.nextStateButton.addActionListener(this);
		this.add(this.nextStateButton);
	
		
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
		if (event.getSource() == this.nextStateButton) {
			this.commandQueue.add(new InitiateNextStateCommand());
		}
		else if (event.getSource() == this.executeP1ActionButton) {
			this.executePlayerAction(1);
		}
		else if (event.getSource() == this.executeP2ActionButton) {
			this.executePlayerAction(2);
		}
		
	}
	

	private void executePlayerAction(int playerNum) {
		try {
			GestureType gesture = GestureType.valueOf(GestureType.class,
				this.playerActionComboBox.getSelectedItem().toString());
			
			Action action = actionFactory.buildPlayerAction(
					playerNum, gesture.getActionFactoryType(), gesture.getUsesLeftHand(), gesture.getUsesRightHand());
			this.commandQueue.add(new ExecuteGenericActionCommand(action));
					
		}
		catch (IllegalArgumentException ex) {
			assert(false);
		}
	}
	
	
}
