package ca.site3.ssf.devgui;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import ca.site3.ssf.gamemodel.GameState;
import ca.site3.ssf.guiprotocol.StreetFireGuiClient;

class GameInfoPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	private static final String EMPTY_STATE_TEXT = "N/A";
	
	private JLabel prevStateLabel;
	private JLabel currStateLabel;
	private JLabel currRoundTimeLabel;
	
	private PlayerInfoPanel player1Panel;
	private PlayerInfoPanel player2Panel;
	private RingmasterInfoPanel ringmasterPanel;
	
	GameInfoPanel(StreetFireGuiClient client) {
		super();
		
		TitledBorder border = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), "Game Information");
		border.setTitleColor(Color.black);
		this.setBorder(border);
		
		
		// GAME STATE INFO PANEL *********************************************
		JPanel stateInfoPanel = new JPanel();
		
        GridBagLayout layout = new GridBagLayout();
        stateInfoPanel.setLayout(layout);

		FormLayoutHelper formLayoutHelper = new FormLayoutHelper();		
		
		JLabel prevStateLabel = new JLabel("Previous State:");
		prevStateLabel.setForeground(Color.black);
		formLayoutHelper.addLabel(prevStateLabel, stateInfoPanel);
		this.prevStateLabel = new JLabel(EMPTY_STATE_TEXT);
		this.prevStateLabel.setForeground(Color.black);
		formLayoutHelper.addLastField(this.prevStateLabel, stateInfoPanel);
		
		JLabel currStateLabel = new JLabel("Current State:");
		currStateLabel.setForeground(Color.black);
		formLayoutHelper.addLabel(currStateLabel, stateInfoPanel);
		this.currStateLabel = new JLabel(EMPTY_STATE_TEXT);
		this.currStateLabel.setForeground(Color.black);
		formLayoutHelper.addLastField(this.currStateLabel, stateInfoPanel);
		
		JLabel roundTimeLabel = new JLabel("Round Time:");
		roundTimeLabel.setForeground(Color.black);
		formLayoutHelper.addLabel(roundTimeLabel, stateInfoPanel);
		
		this.currRoundTimeLabel = new JLabel(EMPTY_STATE_TEXT);
		this.currRoundTimeLabel.setForeground(Color.black);
		Font tempFont = this.currRoundTimeLabel.getFont();
		this.currRoundTimeLabel.setFont(new Font(tempFont.getFontName(), tempFont.getStyle(), 32));
		formLayoutHelper.addLastField(this.currRoundTimeLabel, stateInfoPanel);
		
		// ********************************************************************
		
		this.player1Panel = new PlayerInfoPanel(client, 1);
		this.player2Panel = new PlayerInfoPanel(client, 2);
		this.ringmasterPanel = new RingmasterInfoPanel();

		this.setLayout(new GridLayout(0,4));
		
		this.add(stateInfoPanel);
		this.add(this.player1Panel);
		this.add(this.player2Panel);
		this.add(this.ringmasterPanel);
	}

	void setRoundTimer(double time) {
		if (time < 0.0) {
			this.currRoundTimeLabel.setText(EMPTY_STATE_TEXT);
		}
		else {
			this.currRoundTimeLabel.setText("" + (int)time);
		}
	}
	double getRoundTime() {
		if (this.currRoundTimeLabel.getText() == EMPTY_STATE_TEXT) {
			return -1;
		}
		return Double.parseDouble(this.currRoundTimeLabel.getText());
	}
	
	void setPreviousGameState(GameState.GameStateType state) {
		if (state == null) {
			this.prevStateLabel.setText(EMPTY_STATE_TEXT);
		}
		else {
			this.prevStateLabel.setText(state.toString());
		}
	}
	void setCurrentGameState(GameState.GameStateType state) {
		if (state == null) {
			this.currStateLabel.setText(EMPTY_STATE_TEXT);
		}
		else {
			this.currStateLabel.setText(state.toString());
		}
	}

	PlayerInfoPanel getPlayer1Panel() {
		return this.player1Panel;
	}
	PlayerInfoPanel getPlayer2Panel() {
		return this.player2Panel;
	}
	RingmasterInfoPanel getRingmasterPanel() {
		return this.ringmasterPanel;
	}
	
	PlayerInfoPanel getPlayerPanel(int playerNum) {
		if (playerNum == 1) {
			return this.getPlayer1Panel();
		}
		else {
			return this.getPlayer2Panel();
		}
	}
}
