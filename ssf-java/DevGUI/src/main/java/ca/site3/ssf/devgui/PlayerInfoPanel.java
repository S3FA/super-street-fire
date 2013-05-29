package ca.site3.ssf.devgui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.border.TitledBorder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.site3.ssf.gamemodel.PlayerAttackAction;
import ca.site3.ssf.gamemodel.PlayerAttackAction.AttackType;
import ca.site3.ssf.gamemodel.PlayerAttackActionFailedEvent;
import ca.site3.ssf.guiprotocol.StreetFireGuiClient;

class PlayerInfoPanel extends JPanel implements ItemListener {
	
	private static final long serialVersionUID = 1L;
	
	private int playerNum;
	
	private JProgressBar lifeBar;
	private JProgressBar actionPointBar;
	private JLabel lastActionLabel;
	private JLabel timeOfLastActionLabel;
	
	private JLabel blockWindowAvailableLabel;
	private JProgressBar blockWindowBar;
	
	private JCheckBox unlimitedMovesCheckBox;
	
	private StreetFireGuiClient client = null;
	
	private Logger log = LoggerFactory.getLogger(getClass());
	
	private Map<Integer, TimedBlockWindow> activeBlockSignals = new HashMap<Integer, TimedBlockWindow>();
	
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

		this.actionPointBar = new JProgressBar(JProgressBar.HORIZONTAL, 0, 100);
		this.actionPointBar.setBorderPainted(true);
		this.actionPointBar.setStringPainted(true);
		this.actionPointBar.setForeground(new Color(0, 255, 255));
		this.setActionPoints(0);
		
		JLabel actionPtsLabel = new JLabel("Action Points:");
		actionPtsLabel.setForeground(Color.black);
		
		formLayoutHelper.addLabel(actionPtsLabel, this);
		formLayoutHelper.addLastField(this.actionPointBar, this);
		
		this.blockWindowAvailableLabel = new JLabel("N/A");
		this.blockWindowAvailableLabel.setForeground(Color.black);
		this.blockWindowBar = new JProgressBar(JProgressBar.HORIZONTAL, 0, 100);
		this.blockWindowBar.setBorderPainted(true);
		this.blockWindowBar.setStringPainted(true);
		this.blockWindowBar.setForeground(new Color(218, 165, 32));
		
		Dimension blockWindowBarDim = new Dimension(80, 16);
		this.blockWindowBar.setPreferredSize(blockWindowBarDim);
		this.blockWindowBar.setMinimumSize(blockWindowBarDim);
		this.blockWindowBar.setMaximumSize(blockWindowBarDim);
		this.blockWindowBar.setValue(0);
		
		
		JLabel blockLabel = new JLabel("Block:");
		blockLabel.setForeground(Color.black);
		
		formLayoutHelper.addLabel(blockLabel, this);
		formLayoutHelper.addMiddleField(this.blockWindowAvailableLabel, this);
		formLayoutHelper.addLastField(this.blockWindowBar, this);
		
		
		this.lastActionLabel = new JLabel("N/A");
		this.lastActionLabel.setForeground(Color.black);
		
		JLabel lastActionLabel = new JLabel("Last Action:");
		lastActionLabel.setForeground(Color.black);
		formLayoutHelper.addLabel(lastActionLabel, this);
		formLayoutHelper.addLastField(this.lastActionLabel, this);
		
		this.timeOfLastActionLabel = new JLabel("N/A");
		this.timeOfLastActionLabel.setForeground(Color.black);		
		
		JLabel timeLabel = new JLabel("Time of Last Action:");
		timeLabel.setForeground(Color.black);
		formLayoutHelper.addLabel(timeLabel, this);
		formLayoutHelper.addLastField(this.timeOfLastActionLabel, this);
		
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
	
	void setActionPoints(float actionPointAmt) {
		this.actionPointBar.setValue(Math.max(0, Math.min(100, (int)actionPointAmt)));
	}

	void setLastActionAsAttack(PlayerAttackAction.AttackType actionType, double clockTime) {
		
		if (actionType == null) {
			this.lastActionLabel.setText("N/A");
			this.timeOfLastActionLabel.setText("N/A");
			return;
		}
		
		this.lastActionLabel.setText(actionType.toString());
		this.timeOfLastActionLabel.setText("" + clockTime);
	}
	void setLastActionAsBlock(double clockTime) {
		this.lastActionLabel.setText("BLOCK");
		this.timeOfLastActionLabel.setText("" + clockTime);
	}

	void setUnlimitedMovesCheckBoxEnabled(boolean enabled) {
		this.unlimitedMovesCheckBox.setVisible(enabled);
		this.unlimitedMovesCheckBox.setEnabled(enabled);
	}
	
	void attackFailed(AttackType attackType, PlayerAttackActionFailedEvent.Reason reason, double clockTime) {
		this.lastActionLabel.setText("<html>" + "FAILED!<br>" + attackType.toString() + "<br>(" + reason.getDescription() + ")</html>");
		this.timeOfLastActionLabel.setText("" + clockTime);
	}
	
	void setBlockWindowSignalActive(double percent) {
    	this.blockWindowBar.setValue((int)percent);
    	this.blockWindowAvailableLabel.setText("BLOCK NOW!");
	}
	void setBlockWindowSignalInactive() {
    	this.blockWindowBar.setValue(0);
    	this.blockWindowAvailableLabel.setText("N/A");
	}
	
	@SuppressWarnings("rawtypes")
	void tick(double dT) {

		double highestBlockWindowPercentTime = 0.0;
		
		Iterator it = this.activeBlockSignals.entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry pair = (Map.Entry)it.next();
	        
	        TimedBlockWindow blockWindow = (TimedBlockWindow)pair.getValue();
	        blockWindow.tick(dT);
	        if (blockWindow.isFinished()) {
	        	it.remove();
	        }
	        else {
	        	highestBlockWindowPercentTime = Math.max(highestBlockWindowPercentTime, 
	        			100 * blockWindow.getCountdownTime() / blockWindow.getBlockTimeLength());
	        }
	    }
	    
	    if (highestBlockWindowPercentTime > 0) {
	    	this.setBlockWindowSignalActive(highestBlockWindowPercentTime);
	    }
	    else {
	    	this.setBlockWindowSignalInactive();
	    }
	    
	}
	
	public void addBlockWindow(int blockWindowID, double timeLengthInSecs) {
		this.activeBlockSignals.put(new Integer(blockWindowID), new TimedBlockWindow(timeLengthInSecs));
		this.setBlockWindowSignalActive(100);
	}
	public void removeBlockWindow(int blockWindowID) {
		this.activeBlockSignals.remove(new Integer(blockWindowID));
		
		if (this.activeBlockSignals.isEmpty()) {
			this.setBlockWindowSignalInactive();
		}
		
	}
	public void removeAllBlockWindows() {
		this.activeBlockSignals.clear();
		this.setBlockWindowSignalInactive();
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
