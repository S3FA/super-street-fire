package ca.site3.ssf.devgui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;

import ca.site3.ssf.gamemodel.IGameModel;

class ControlPanel extends JPanel implements ActionListener {
	
	private IGameModel gameModel    = null;
	private JButton nextStateButton = null;
	
	
	ControlPanel(IGameModel gameModel) {
		super();
		
		this.gameModel = gameModel;
		assert(gameModel != null);
		
		this.nextStateButton = new JButton("Initiate Next State");
		this.nextStateButton.addActionListener(this);
		this.add(this.nextStateButton);
		
	}

	public void actionPerformed(ActionEvent event) {
		if (event.getSource() == this.nextStateButton) {
			this.gameModel.initiateNextState();
		}
		
	}
	

	
	
	
}
