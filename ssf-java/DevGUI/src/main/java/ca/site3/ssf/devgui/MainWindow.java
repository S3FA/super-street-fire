package ca.site3.ssf.devgui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import ca.site3.ssf.gamemodel.*;
import ca.site3.ssf.gamemodel.GameState.GameStateType;
import ca.site3.ssf.gamemodel.IGameModel.Entity;
import ca.site3.ssf.gamemodel.PlayerAttackAction.AttackType;


public class MainWindow extends JFrame implements IGameModelListener, ActionListener {
	
	private static final long serialVersionUID = 1L;
	
	private ArenaDisplay arenaDisplay;
	private GameInfoPanel infoPanel;
	
	private Timer gameSimTimer;
    // TODO: For now we simulate a game model here, in the future we should get the
    // devgui to be using the guiprotocol to communicate with the ioserver directly!
    private GameConfig gameConfig = new GameConfig(true, 0.1, 60, 3);
    private IGameModel gameModel  = new GameModel(this.gameConfig);	
	
	
	public MainWindow() {
		super();
		
		// Setup the frame's basic characteristics...
		this.setTitle("Super Street Fire (Developer GUI)");
		this.setPreferredSize(new Dimension(1000, 800));
		this.setMinimumSize(new Dimension(800, 800));
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setLayout(new BorderLayout());
		
		// Setup the frame's contents...
		this.arenaDisplay = new ArenaDisplay(new FireEmitterConfig(true, 16, 8));
		Container contentPane = this.getContentPane();
		contentPane.add(this.arenaDisplay, BorderLayout.CENTER);
		
		this.infoPanel = new GameInfoPanel();
		contentPane.add(this.infoPanel, BorderLayout.SOUTH);
		
		this.pack();
		this.setLocationRelativeTo(null);
		
		this.gameModel.addGameModelListener(this);
		this.gameSimTimer = new Timer(16, this);
		this.gameSimTimer.start();
	}

	
	static void createAndShowGUI() {
		MainWindow mainWindow = new MainWindow();
		mainWindow.setVisible(true);
	}
	
	/**
	 * The main driver method for the Developer GUI.
	 * @param args
	 */
	public static void main(String[] args) {
		
        Runnable doCreateAndShowGUI = new Runnable() {
        	
            public void run() {
            	MainWindow.createAndShowGUI();
            }
        };
        SwingUtilities.invokeLater(doCreateAndShowGUI);
	}

	// GameModel 
	
	public void onGameStateChanged(GameStateType oldState, GameStateType newState) {
		this.infoPanel.setGameState(newState);
		if (newState != GameStateType.ROUND_IN_PLAY_STATE) {
			this.infoPanel.setRoundTimer(-1);
		}
		if (newState != GameStateType.ROUND_BEGINNING_STATE) {
			this.arenaDisplay.setInfoText("");
		}
	}

	public void onPlayerHealthChanged(int playerNum, float prevLifePercentage, float newLifePercentage) {
		PlayerInfoPanel playerPanel = this.infoPanel.getPlayerPanel(playerNum);
		playerPanel.setLife(newLifePercentage);
	}

	public void onRoundPlayTimerChanged(int newCountdownTimeInSecs) {
		this.infoPanel.setRoundTimer(newCountdownTimeInSecs);
	}

	public void onRoundBeginFightTimerChanged(RoundBeginCountdownType threeTwoOneFightTime) {
		this.arenaDisplay.setInfoText(threeTwoOneFightTime.toString());
	}

	public void onRoundEnded(GameResult roundResult, boolean roundTimedOut) {
		// TODO Auto-generated method stub
		
	}

	public void onMatchEnded(GameResult matchResult) {
		// TODO Auto-generated method stub
		
	}

	public void onPlayerAttackAction(int playerNum, AttackType attackType) {
		PlayerInfoPanel playerPanel = this.infoPanel.getPlayerPanel(playerNum);
		playerPanel.setLastActionAsAttack(attackType, this.infoPanel.getRoundTime());
	}

	public void onPlayerBlockAction(int playerNum) {
		PlayerInfoPanel playerPanel = this.infoPanel.getPlayerPanel(playerNum);
		playerPanel.setLastActionAsBlock(this.infoPanel.getRoundTime());
	}

	public void onRingmasterAction() {
		// TODO Auto-generated method stub
		
	}

	public void onFireEmitterChanged(ImmutableFireEmitter fireEmitter) {
		Color[] colours     = new Color[fireEmitter.getContributingEntities().size()];
		float[] intensities = new float[fireEmitter.getContributingEntities().size()];
		
		int i = 0;
		for (Entity entity : fireEmitter.getContributingEntities()) {
			switch (entity) {
			case PLAYER1_ENTITY:
				colours[i] = ArenaDisplay.PLAYER_1_COLOUR;
				break;
			case PLAYER2_ENTITY:
				colours[i] = ArenaDisplay.PLAYER_2_COLOUR;
				break;
			case RINGMASTER_ENTITY:
				colours[i] = ArenaDisplay.RINGMASTER_COLOUR;
				break;
			default:
				assert(false);
				break;
			}
			
			intensities[i] = fireEmitter.getIntensity(entity);
			i++;
		}
		
		switch (fireEmitter.getLocation()) {
		case LEFT_RAIL:
			this.arenaDisplay.setLeftRailEmitter(fireEmitter.getIndex(), new EmitterData(intensities, colours));
			break;
			
		case RIGHT_RAIL:
			this.arenaDisplay.setRightRailEmitter(fireEmitter.getIndex(), new EmitterData(intensities, colours));
			break;
			
		case OUTER_RING:
			this.arenaDisplay.setOuterRingEmitter(fireEmitter.getIndex(), new EmitterData(intensities, colours));
			break;
			
		default:
			assert(false);
			break;
		}
		
	}

	public void actionPerformed(ActionEvent event) {
		if (event.getSource() == this.gameSimTimer) {
			gameModel.tick(0.016666666);
		}
	}

}
