package ca.site3.ssf.devgui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.util.Queue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import ca.site3.ssf.gamemodel.AbstractGameModelCommand;
import ca.site3.ssf.gamemodel.FireEmitterChangedEvent;
import ca.site3.ssf.gamemodel.FireEmitterConfig;
import ca.site3.ssf.gamemodel.GameState.GameStateType;
import ca.site3.ssf.gamemodel.GameStateChangedEvent;
import ca.site3.ssf.gamemodel.IGameModel;
import ca.site3.ssf.gamemodel.IGameModel.Entity;
import ca.site3.ssf.gamemodel.IGameModelEvent;
import ca.site3.ssf.gamemodel.IGameModelListener;
import ca.site3.ssf.gamemodel.MatchEndedEvent;
import ca.site3.ssf.gamemodel.PlayerAttackActionEvent;
import ca.site3.ssf.gamemodel.PlayerBlockActionEvent;
import ca.site3.ssf.gamemodel.PlayerHealthChangedEvent;
import ca.site3.ssf.gamemodel.RingmasterActionEvent;
import ca.site3.ssf.gamemodel.RoundBeginTimerChangedEvent;
import ca.site3.ssf.gamemodel.RoundEndedEvent;
import ca.site3.ssf.gamemodel.RoundPlayTimerChangedEvent;


/**
 * Main frame for the developer GUI. Displays current state of the FireEmitterModel, 
 * player health, game status etc. 
 * 
 *  TODO: get the devgui to be using the guiprotocol to communicate with the ioserver directly
 *  (might be nice to have an option to either directly listen on the game model or else
 *  use the GUIProtocol)
 *  
 *  @author Callum
 */
public class MainWindow extends JFrame implements IGameModelListener {
	
	private static final long serialVersionUID = 1L;
	
	private ArenaDisplay arenaDisplay = null;
	private GameInfoPanel infoPanel   = null;
	private ControlPanel controlPanel = null;
    private IGameModel gameModel      = null;	
	
    private Queue<AbstractGameModelCommand> commandQueue;
    
    
	public MainWindow(IGameModel gameModel, Queue<AbstractGameModelCommand> commandQueue) {
		this.gameModel = gameModel;
		this.commandQueue = commandQueue;
		
		// Setup the frame's basic characteristics...
		this.setTitle("Super Street Fire (Developer GUI)");
		this.setPreferredSize(new Dimension(1000, 750));
		this.setMinimumSize(new Dimension(1000, 750));
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setLayout(new BorderLayout());
		
		// Setup the frame's contents...
		this.arenaDisplay = new ArenaDisplay(gameModel, new FireEmitterConfig(true, 16, 8));
		Container contentPane = this.getContentPane();
		contentPane.add(this.arenaDisplay, BorderLayout.CENTER);
		
		JPanel infoAndControlPanel = new JPanel();
		infoAndControlPanel.setLayout(new BorderLayout());
		
		this.infoPanel = new GameInfoPanel();
		infoAndControlPanel.add(this.infoPanel, BorderLayout.NORTH);
		
		this.controlPanel = new ControlPanel(gameModel.getActionFactory(), commandQueue);
		infoAndControlPanel.add(this.controlPanel, BorderLayout.CENTER);
		
		contentPane.add(infoAndControlPanel, BorderLayout.SOUTH);
		
		this.pack();
		this.setLocationRelativeTo(null);
		
		this.gameModel.addGameModelListener(this);
	}

	// GameModel 
	public void onGameModelEvent(final IGameModelEvent event) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				switch (event.getType()) {
				case FireEmitterChanged:
					MainWindow.this.onFireEmitterChanged((FireEmitterChangedEvent)event);
					break;
				case GameStateChanged:
					MainWindow.this.onGameStateChanged((GameStateChangedEvent)event);
					break;
				case MatchEnded:
					MainWindow.this.onMatchEnded((MatchEndedEvent)event);
					break;
				case PlayerAttackAction:
					MainWindow.this.onPlayerAttackAction((PlayerAttackActionEvent)event);
					break;
				case PlayerBlockAction:
					MainWindow.this.onPlayerBlockAction((PlayerBlockActionEvent)event);
					break;
				case PlayerHealthChanged:
					MainWindow.this.onPlayerHealthChanged((PlayerHealthChangedEvent)event);
					break;
				case RingmasterAction:
					MainWindow.this.onRingmasterAction((RingmasterActionEvent)event);
					break;
				case RoundBeginTimerChanged:
					MainWindow.this.onRoundBeginFightTimerChanged((RoundBeginTimerChangedEvent)event);
					break;
				case RoundEnded:
					MainWindow.this.onRoundEnded((RoundEndedEvent)event);
					break;
				case RoundPlayTimerChanged:
					MainWindow.this.onRoundPlayTimerChanged((RoundPlayTimerChangedEvent)event);
					break;
				default:
					assert(false);
					break;
				}				
			}
		});
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
		String infoText = "Round " + event.getRoundNumber() + ":\n";
		if (event.getRoundTimedOut()) {
			infoText += "Time Out\n";
		}
		switch (event.getRoundResult()) {
		case PLAYER1_VICTORY:
			infoText += "Player 1 Wins!";
			break;
		case PLAYER2_VICTORY:
			infoText += "Player 2 Wins!";
			break;
		case TIE:
			infoText += "Tie!";
			break;
		default:
			assert(false);
			break;
		}

		this.arenaDisplay.setInfoText(infoText);
	}

	private void onMatchEnded(MatchEndedEvent event) {
		String infoText = "Match Over\n";
		switch (event.getMatchResult()) {
			case PLAYER1_VICTORY:
				infoText += "Player 1 Wins!";
				break;
			case PLAYER2_VICTORY:
				infoText += "Player 2 Wins!";
				break;
			default:
				assert(false);
				break;
		}
		this.arenaDisplay.setInfoText(infoText);
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

}
