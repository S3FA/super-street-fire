package ca.site3.ssf.devgui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import ca.site3.ssf.gamemodel.*;
import ca.site3.ssf.gamemodel.GameState.GameStateType;
import ca.site3.ssf.gamemodel.IGameModel.Entity;
import ca.site3.ssf.gamemodel.PlayerAttackAction.AttackType;


public class MainWindow extends JFrame implements IGameModelListener, ActionListener {
	
	private static final long serialVersionUID = 1L;
	
	private ArenaDisplay arenaDisplay = null;
	private GameInfoPanel infoPanel   = null;
	private ControlPanel controlPanel = null;
	
	private Timer gameSimTimer;
    // TODO: For now we simulate a game model here, in the future we should get the
    // devgui to be using the guiprotocol to communicate with the ioserver directly!
    private GameConfig gameConfig = new GameConfig(true, 0.1, 5, 3);
    private IGameModel gameModel  = new GameModel(this.gameConfig);	
	
	
	public MainWindow() {
		super();
		
		// Setup the frame's basic characteristics...
		this.setTitle("Super Street Fire (Developer GUI)");
		this.setPreferredSize(new Dimension(1000, 900));
		this.setMinimumSize(new Dimension(800, 800));
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setLayout(new BorderLayout());
		
		// Setup the frame's contents...
		this.arenaDisplay = new ArenaDisplay(this.gameConfig, new FireEmitterConfig(true, 16, 8));
		Container contentPane = this.getContentPane();
		contentPane.add(this.arenaDisplay, BorderLayout.CENTER);
		
		JPanel infoAndControlPanel = new JPanel();
		infoAndControlPanel.setLayout(new BorderLayout());
		
		this.infoPanel = new GameInfoPanel();
		infoAndControlPanel.add(this.infoPanel, BorderLayout.NORTH);
		
		this.controlPanel = new ControlPanel(this.gameModel);
		infoAndControlPanel.add(this.controlPanel, BorderLayout.CENTER);
		
		contentPane.add(infoAndControlPanel, BorderLayout.SOUTH);
		
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
	public void onGameModelEvent(IGameModelEvent event) {
		switch (event.getType()) {
		case FireEmitterChanged:
			this.onFireEmitterChanged((FireEmitterChangedEvent)event);
			break;
		case GameStateChanged:
			this.onGameStateChanged((GameStateChangedEvent)event);
			break;
		case MatchEnded:
			this.onMatchEnded((MatchEndedEvent)event);
			break;
		case PlayerAttackAction:
			this.onPlayerAttackAction((PlayerAttackActionEvent)event);
			break;
		case PlayerBlockAction:
			this.onPlayerBlockAction((PlayerBlockActionEvent)event);
			break;
		case PlayerHealthChanged:
			this.onPlayerHealthChanged((PlayerHealthChangedEvent)event);
			break;
		case RingmasterAction:
			this.onRingmasterAction((RingmasterActionEvent)event);
			break;
		case RoundBeginTimerChanged:
			this.onRoundBeginFightTimerChanged((RoundBeginTimerChangedEvent)event);
			break;
		case RoundEnded:
			this.onRoundEnded((RoundEndedEvent)event);
			break;
		case RoundPlayTimerChanged:
			this.onRoundPlayTimerChanged((RoundPlayTimerChangedEvent)event);
			break;
		default:
			assert(false);
			break;
		}
	}
	
	private void onGameStateChanged(GameStateChangedEvent event) {
		this.infoPanel.setPreviousGameState(event.getOldState());
		this.infoPanel.setCurrentGameState(event.getNewState());
		if (event.getNewState() != GameStateType.ROUND_IN_PLAY_STATE) {
			this.infoPanel.setRoundTimer(-1);
		}
	}

	private void onPlayerHealthChanged(PlayerHealthChangedEvent event) {
		PlayerInfoPanel playerPanel = this.infoPanel.getPlayerPanel(event.getPlayerNum());
		playerPanel.setLife(event.getNewLifePercentage());
	}

	private void onRoundPlayTimerChanged(RoundPlayTimerChangedEvent event) {
		this.infoPanel.setRoundTimer(event.getCountdownTimeInSecs());
	}

	private void onRoundBeginFightTimerChanged(RoundBeginTimerChangedEvent event) {
		this.arenaDisplay.setInfoText(event.getThreeTwoOneFightTime().toString());
	}

	private void onRoundEnded(RoundEndedEvent event) {
		this.arenaDisplay.setRoundResult(event.getRoundNumber(), event.getRoundResult());
		this.arenaDisplay.setInfoText("Time Out");
	}

	private void onMatchEnded(MatchEndedEvent event) {
		this.arenaDisplay.setInfoText("Match Over");
	}

	private void onPlayerAttackAction(PlayerAttackActionEvent event) {
		PlayerInfoPanel playerPanel = this.infoPanel.getPlayerPanel(event.getPlayerNum());
		playerPanel.setLastActionAsAttack(event.getAttackType(), this.infoPanel.getRoundTime());
	}

	private void onPlayerBlockAction(PlayerBlockActionEvent event) {
		PlayerInfoPanel playerPanel = this.infoPanel.getPlayerPanel(event.getPlayerNum());
		playerPanel.setLastActionAsBlock(this.infoPanel.getRoundTime());
	}

	private void onRingmasterAction(RingmasterActionEvent event) {
		// TODO Auto-generated method stub
	}

	private void onFireEmitterChanged(FireEmitterChangedEvent event) {
		Color[] colours     = new Color[event.getContributingEntities().size()];
		float[] intensities = new float[event.getContributingEntities().size()];
		
		int i = 0;
		for (Entity entity : event.getContributingEntities()) {
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
			
			intensities[i] = event.getIntensity(entity);
			i++;
		}
		
		switch (event.getLocation()) {
		case LEFT_RAIL:
			this.arenaDisplay.setLeftRailEmitter(event.getIndex(), new EmitterData(intensities, colours));
			break;
			
		case RIGHT_RAIL:
			this.arenaDisplay.setRightRailEmitter(event.getIndex(), new EmitterData(intensities, colours));
			break;
			
		case OUTER_RING:
			this.arenaDisplay.setOuterRingEmitter(event.getIndex(), new EmitterData(intensities, colours));
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
