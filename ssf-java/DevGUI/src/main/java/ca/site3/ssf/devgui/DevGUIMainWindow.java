package ca.site3.ssf.devgui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.TitledBorder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.site3.ssf.Sound.AudioSettings;
import ca.site3.ssf.Sound.SoundPlayerController;
import ca.site3.ssf.common.LoggingUtil;
import ca.site3.ssf.gamemodel.BlockWindowEvent;
import ca.site3.ssf.gamemodel.FireEmitterChangedEvent;
import ca.site3.ssf.gamemodel.FireEmitterConfig;
import ca.site3.ssf.gamemodel.GameInfoRefreshEvent;
import ca.site3.ssf.gamemodel.GameState;
import ca.site3.ssf.gamemodel.GameStateChangedEvent;
import ca.site3.ssf.gamemodel.IGameModel;
import ca.site3.ssf.gamemodel.IGameModel.Entity;
import ca.site3.ssf.gamemodel.IGameModelEvent;
import ca.site3.ssf.gamemodel.MatchEndedEvent;
import ca.site3.ssf.gamemodel.PlayerActionPointsChangedEvent;
import ca.site3.ssf.gamemodel.PlayerAttackActionEvent;
import ca.site3.ssf.gamemodel.PlayerAttackActionFailedEvent;
import ca.site3.ssf.gamemodel.PlayerBlockActionEvent;
import ca.site3.ssf.gamemodel.PlayerHealthChangedEvent;
import ca.site3.ssf.gamemodel.RingmasterActionEvent;
import ca.site3.ssf.gamemodel.RoundBeginTimerChangedEvent;
import ca.site3.ssf.gamemodel.RoundEndedEvent;
import ca.site3.ssf.gamemodel.RoundPlayTimerChangedEvent;
import ca.site3.ssf.gamemodel.SystemInfoRefreshEvent;
import ca.site3.ssf.guiprotocol.StreetFireGuiClient;
import ca.site3.ssf.ioserver.CommandLineArgs;
import ca.site3.ssf.ioserver.DeviceConstants.Device;
import ca.site3.ssf.ioserver.DeviceStatus;
import ca.site3.ssf.ioserver.DeviceStatus.IDeviceStatusListener;
import ca.site3.ssf.ioserver.GlowflyTestWindow;
import ca.site3.ssf.ioserver.IOServer;
import ca.site3.ssf.ioserver.BoardQueryTestWindow;

import com.apple.eawt.AppEvent.QuitEvent;
import com.apple.eawt.Application;
import com.apple.eawt.QuitHandler;
import com.apple.eawt.QuitResponse;
import com.beust.jcommander.JCommander;


/**
 * Main frame for the developer GUI. Displays current state of the FireEmitterModel, 
 * player health, game status etc. 
 *  
 *  @author Callum
 */
public class DevGUIMainWindow extends JFrame implements ActionListener, IDeviceStatusListener {
	
	private static final long serialVersionUID = 1L;
	private Logger log = LoggerFactory.getLogger(getClass());
	
	private JMenuBar menuBar = null;
	private JMenu windowMenu = null;
	private JMenuItem gloveInfoWindowMenuItem = null;
	private JMenuItem customActionMenuItem = null;
	private JMenuItem glowflyTestMenuItem = null;
	private JMenuItem boardQueryTestMenuItem = null;
	
	private JFrame gloveDataWindow = null;
	private GloveDataInfoPanel p1LeftGloveInfoPanel  = null;
	private GloveDataInfoPanel p1RightGloveInfoPanel = null;
	private GloveDataInfoPanel p2LeftGloveInfoPanel  = null;
	private GloveDataInfoPanel p2RightGloveInfoPanel = null;
	private GloveDataInfoPanel rmLeftGloveInfoPanel  = null;
	private GloveDataInfoPanel rmRightGloveInfoPanel = null;
	
	private JFrame customActionWindow = null;
	private CustomActionPanel customActionPanel  = null;
	private GlowflyTestWindow glowflyTestWindow  = null;
	private BoardQueryTestWindow boardTestWindow = null;
	
	private ArenaDisplay arenaDisplay = null;
	private GameInfoPanel infoPanel   = null;
	private ControlPanel controlPanel = null;
    private IGameModel gameModel      = null;	
    private IOServer ioserver         = null;
    
    private SoundPlayerController soundPlayerController;
    
    private JDialog connectingDlg = new JDialog(this, "Not connected...", false);
    
    private final CommandLineArgs args;
    private StreetFireGuiClient client = null;

    static final int PREF_WINDOW_WIDTH  = 1360;
    static final int PREF_WINDOW_HEIGHT = 760;
    
    // Thread that monitors the queue for game model events
    private Thread gameEventThread;
    // Thread that allows for animation / ticking of GUI elements
    private Thread tickThread;
    
	public DevGUIMainWindow(IOServer ioserver, CommandLineArgs args) {
		
		this.args = args;
		this.ioserver = ioserver;
		this.gameModel = ioserver.getGameModel();
		
		this.glowflyTestWindow = new GlowflyTestWindow(ioserver);
		this.boardTestWindow   = new BoardQueryTestWindow(ioserver);
		
		if (System.getProperty("os.name").startsWith("Mac OS X")) {
			initMacStuff();
		}
	}
	
	private void getThisPartyStarted() {
		
		InetAddress localhost = null;
		try {
			localhost = InetAddress.getLocalHost();
		} catch (UnknownHostException ex) {
			log.error("Could not find localhost",ex);
		}
		client = new StreetFireGuiClient(localhost, args.guiPort, args.useSSL);
		
		try {
			client.connect();
		}
		catch (IOException ex) {
			log.error("DevGUI could not connect to IOServer",ex);
		}
		
		// NOTE: If we setup the sound player to be a direct listener of the game model then the 
		// the sound player will be touched by the ioserver's thread. This is why we don't keep a
		// member of it, so that we don't make the mistake of touching it with any of the GUI threads.
		// GW: how about we just be careful. Need a reference to this to cleanly stop sound on quit.
		soundPlayerController = new SoundPlayerController(new AudioSettings(5.0f, 0.33f));
		this.ioserver.getGameModel().addGameModelListener(soundPlayerController);
		
		// Setup the frame's basic characteristics...
		this.setTitle("Super Street Fire (Developer GUI) - " + args.serialDevice);
		this.setPreferredSize(new Dimension(PREF_WINDOW_WIDTH, PREF_WINDOW_HEIGHT));
		this.setMinimumSize(new Dimension(PREF_WINDOW_WIDTH, 720));
		this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		this.setLayout(new BorderLayout());
		
		this.menuBar = new JMenuBar();
		this.windowMenu = new JMenu("Window");
		this.gloveInfoWindowMenuItem = new JMenuItem("Glove Information");
		this.gloveInfoWindowMenuItem.addActionListener(this);
		this.customActionMenuItem = new JMenuItem("Action Prototyping");
		this.customActionMenuItem.addActionListener(this);
		this.glowflyTestMenuItem = new JMenuItem("Glowfly Test UI");
		this.glowflyTestMenuItem.addActionListener(this);
		this.boardQueryTestMenuItem = new JMenuItem("Board Query Test UI");
		this.boardQueryTestMenuItem.addActionListener(this);
		
		this.windowMenu.add(this.gloveInfoWindowMenuItem);
		this.windowMenu.add(this.customActionMenuItem);
		this.windowMenu.add(this.glowflyTestMenuItem);
		this.windowMenu.add(this.boardQueryTestMenuItem);
		
		this.menuBar.add(this.windowMenu);
		
		this.setJMenuBar(this.menuBar);
		
		this.p1LeftGloveInfoPanel  = new GloveDataInfoPanel(GloveDataInfoPanel.GloveType.LEFT_GLOVE);
		this.p1RightGloveInfoPanel = new GloveDataInfoPanel(GloveDataInfoPanel.GloveType.RIGHT_GLOVE);
		this.p2LeftGloveInfoPanel  = new GloveDataInfoPanel(GloveDataInfoPanel.GloveType.LEFT_GLOVE);
		this.p2RightGloveInfoPanel = new GloveDataInfoPanel(GloveDataInfoPanel.GloveType.RIGHT_GLOVE);		
		this.rmLeftGloveInfoPanel  = new GloveDataInfoPanel(GloveDataInfoPanel.GloveType.LEFT_GLOVE);
		this.rmRightGloveInfoPanel = new GloveDataInfoPanel(GloveDataInfoPanel.GloveType.RIGHT_GLOVE);	
		this.setupGloveDataFrame();
		
		this.customActionPanel = new CustomActionPanel(client);
		this.setupCustomActionFrame();
		
		// Setup the frame's contents...
		this.arenaDisplay = new ArenaDisplay(gameModel.getConfiguration().getNumRoundsPerMatch(),
				new FireEmitterConfig(true, 16, 8), client);
		Container contentPane = this.getContentPane();
		contentPane.add(this.arenaDisplay, BorderLayout.CENTER);
		
		JPanel infoAndControlPanel = new JPanel();
		infoAndControlPanel.setLayout(new BorderLayout());
		
		this.infoPanel = new GameInfoPanel(client);
		infoAndControlPanel.add(this.infoPanel, BorderLayout.NORTH);
		
		this.controlPanel = new ControlPanel(this, gameModel.getActionFactory(), client);
		infoAndControlPanel.add(this.controlPanel, BorderLayout.CENTER);
		
		contentPane.add(infoAndControlPanel, BorderLayout.SOUTH);

		this.pack();
		this.setLocationRelativeTo(null);
		
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				DevGUIMainWindow.this.ioserver.stop();
			}
		});
		
		this.gameEventThread = new Thread("DevGUI game event listener thread") {
			public @Override void run() {
				while (true) {
					
					try {
						IGameModelEvent event = client.getEventQueue().take();
						handleGameModelEvent(event);
					}
					catch (InterruptedException ex) {
						log.warn("DevGUI interrupted while waiting for game model event",ex);
					}
				}
			}
		};
		this.gameEventThread.start();
		
		this.tickThread = new Thread("DevGUI tick thread") {
			public @Override void run() {
				
				final int MAX_FRAME_RATE_HZ  = 60;
				final long MAX_FRAME_TIME_MS = (long)(1000 * (1.0 / (double)MAX_FRAME_RATE_HZ));
				
				long prevTimeInMs = System.currentTimeMillis();
				long currTimeInMs = prevTimeInMs;
				
				while (true) {	
					currTimeInMs = System.currentTimeMillis();
					
					long deltaTimeInMs = currTimeInMs - prevTimeInMs;
					prevTimeInMs = currTimeInMs;
					
					double deltaTimeInSecs = (double)deltaTimeInMs / 1000.0;
					assert(deltaTimeInSecs >= 0);
					
					tickGUI(deltaTimeInSecs);
					
					try {
						Thread.sleep(Math.max(0, MAX_FRAME_TIME_MS - deltaTimeInMs));
					} 
					catch (InterruptedException e) {
					}					
				}
			}
		};
		this.tickThread.start();
		this.ioserver.getDeviceStatus().addListener(this);
	}

	private void handleGameModelEvent(final IGameModelEvent event) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				switch (event.getType()) {
				case GAME_INFO_REFRESH:
					DevGUIMainWindow.this.onGameInfoRefresh((GameInfoRefreshEvent)event);
					break;
				case FIRE_EMITTER_CHANGED:
					DevGUIMainWindow.this.onFireEmitterChanged((FireEmitterChangedEvent)event);
					break;
				case GAME_STATE_CHANGED:
					DevGUIMainWindow.this.onGameStateChanged((GameStateChangedEvent)event);
					break;
				case MATCH_ENDED:
					DevGUIMainWindow.this.onMatchEnded((MatchEndedEvent)event);
					break;
				case PLAYER_ATTACK_ACTION:
					DevGUIMainWindow.this.onPlayerAttackAction((PlayerAttackActionEvent)event);
					break;
				case PLAYER_BLOCK_ACTION:
					DevGUIMainWindow.this.onPlayerBlockAction((PlayerBlockActionEvent)event);
					break;
				case PLAYER_HEALTH_CHANGED:
					DevGUIMainWindow.this.onPlayerHealthChanged((PlayerHealthChangedEvent)event);
					break;
				case RINGMASTER_ACTION:
					DevGUIMainWindow.this.onRingmasterAction((RingmasterActionEvent)event);
					break;
				case ROUND_BEGIN_TIMER_CHANGED:
					DevGUIMainWindow.this.onRoundBeginFightTimerChanged((RoundBeginTimerChangedEvent)event);
					break;
				case ROUND_ENDED:
					DevGUIMainWindow.this.onRoundEnded((RoundEndedEvent)event);
					break;
				case ROUND_PLAY_TIMER_CHANGED:
					DevGUIMainWindow.this.onRoundPlayTimerChanged((RoundPlayTimerChangedEvent)event);
					break;
				case UNRECOGNIZED_GESTURE:
					break;
				case BLOCK_WINDOW:
					DevGUIMainWindow.this.onBlockWindowEvent((BlockWindowEvent)event);
					break;
				case PLAYER_ACTION_POINTS_CHANGED:
					DevGUIMainWindow.this.onPlayerActionPointsChanged((PlayerActionPointsChangedEvent)event);
					break;
				case PLAYER_ATTACK_ACTION_FAILED:
					DevGUIMainWindow.this.onPlayerAttackFailed((PlayerAttackActionFailedEvent)event);
					break;
				case SYSTEM_INFO_REFRESH:
					DevGUIMainWindow.this.onSystemInfoRefresh((SystemInfoRefreshEvent)event);
					break;
				default:
					assert(false);
					break;
				}				
			}
		});
	}
	
	private void tickGUI(final double dT) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				DevGUIMainWindow.this.infoPanel.tick(dT);
				
				p1LeftGloveInfoPanel.tick();
				p1RightGloveInfoPanel.tick();
				p2LeftGloveInfoPanel.tick();
				p2RightGloveInfoPanel.tick();
				rmLeftGloveInfoPanel.tick();
				rmRightGloveInfoPanel.tick();
			}
		});
	}
	
	private void onSystemInfoRefresh(SystemInfoRefreshEvent event) {
		this.arenaDisplay.setSystemStatus(event);
	}
	
	private void onGameInfoRefresh(GameInfoRefreshEvent event) {
		this.performOnCurrStateChanges(event.getCurrentGameState());
		this.infoPanel.getPlayer1Panel().setLife(event.getPlayer1Health());
		this.infoPanel.getPlayer2Panel().setLife(event.getPlayer2Health());
		
		// Update the round results...
		this.arenaDisplay.setInfoText("");
		this.arenaDisplay.clearRoundResults();
		for (int i = 0; i < event.getCurrentRoundResults().size(); i++) {
			this.arenaDisplay.setRoundResult(i+1, event.getCurrentRoundResults().get(i));
		}
		
		switch (event.getCurrentGameState()) {
		
		case ROUND_BEGINNING_STATE:
			this.onRoundBeginFightTimerChanged(new RoundBeginTimerChangedEvent(event.getRoundBeginCountdown(), event.getRoundNumber()));
			break;
		
		case ROUND_IN_PLAY_STATE:
		case TIE_BREAKER_ROUND_STATE:
			this.onRoundPlayTimerChanged(new RoundPlayTimerChangedEvent(event.getRoundInPlayTimer()));
			break;
		
		case TEST_ROUND_STATE:
			this.onTestRound();
			break;
			
		case ROUND_ENDED_STATE: {
			List<RoundEndedEvent.RoundResult> roundResults = event.getCurrentRoundResults();
			assert(!roundResults.isEmpty());
			
			int roundNumber = roundResults.size();
			int roundIndex  = roundNumber - 1;
			
			this.onRoundEnded(new RoundEndedEvent(roundNumber, roundResults.get(roundIndex), event.getRoundTimedOut(), event.getPlayer1Health(), event.getPlayer2Health()));
			break;
		}
		
		case MATCH_ENDED_STATE:
			this.onMatchEnded(new MatchEndedEvent(event.getMatchResult(), event.getPlayer1Health(), event.getPlayer2Health()));
			break;
		
		default:
			break;
		}
		
	}
	
	private void onGameStateChanged(GameStateChangedEvent event) {
		this.infoPanel.setPreviousGameState(event.getOldState());
		
		if (event.getOldState() == GameState.GameStateType.TEST_ROUND_STATE) {
			this.infoPanel.getPlayer1Panel().setUnlimitedMovesCheckBoxEnabled(true);
			this.infoPanel.getPlayer2Panel().setUnlimitedMovesCheckBoxEnabled(true);
		}
		
		this.performOnCurrStateChanges(event.getNewState());
	}
	
	private void performOnCurrStateChanges(GameState.GameStateType currentState) {
		this.infoPanel.setCurrentGameState(currentState);
		
		switch (currentState) {
		case IDLE_STATE:
			this.arenaDisplay.setInfoText("");
			this.arenaDisplay.clearRoundResults();
			this.infoPanel.setRoundTimer(-1);
			break;
			
		case TEST_ROUND_STATE:
			this.onTestRound();
			break;
			
		default:
			break;
		}
		
		// If the game is not in-play then we remove all the block windows/signals in the GUI
		if (currentState != GameState.GameStateType.ROUND_IN_PLAY_STATE && 
			currentState != GameState.GameStateType.TEST_ROUND_STATE) {
			this.infoPanel.removeAllBlockWindows();
		}
		
		this.controlPanel.gameStateChanged(currentState);
	}

	private void onPlayerHealthChanged(PlayerHealthChangedEvent event) {
		PlayerInfoPanel playerPanel = this.infoPanel.getPlayerPanel(event.getPlayerNum());
		playerPanel.setLife(event.getNewLifePercentage());
	}

	private void onRoundPlayTimerChanged(RoundPlayTimerChangedEvent event) {
		this.infoPanel.setRoundTimer(event.getTimeInSecs());
	}
	
	private void onBlockWindowEvent(BlockWindowEvent event) {
		if (event.getHasBlockWindowExpired()) {
			this.infoPanel.removeBlockWindow(event.getBlockWindowID(), event.getBlockingPlayerNumber());
		}
		else {
			this.infoPanel.addBlockWindow(event.getBlockWindowID(), event.getBlockingPlayerNumber(), event.getBlockWindowTimeLengthInSeconds());
		}
	}
	
	private void onPlayerActionPointsChanged(PlayerActionPointsChangedEvent event) {
		PlayerInfoPanel playerPanel = this.infoPanel.getPlayerPanel(event.getPlayerNum());
		playerPanel.setActionPoints(event.getNewActionPointAmt());
	}

	private void onPlayerAttackFailed(PlayerAttackActionFailedEvent event) {
		PlayerInfoPanel playerPanel = this.infoPanel.getPlayerPanel(event.getPlayerNum());
		playerPanel.attackFailed(event.getAttackType(), event.getReason(), this.infoPanel.getRoundTime());
	}
	
	private void onRoundBeginFightTimerChanged(RoundBeginTimerChangedEvent event) {
		// Clear the old arena display results on the beginning of a new match...
		if (event.getRoundNumber() == 1) {
			this.arenaDisplay.clearRoundResults();
		}
		
		this.arenaDisplay.setInfoText(event.getThreeTwoOneFightTime().toString());
	}

	private void onRoundEnded(RoundEndedEvent event) {

		this.arenaDisplay.setRoundResult(event.getRoundNumber(), event.getRoundResult());
		String infoText = "Round " + event.getRoundNumber() + ":\n";
		if (event.getRoundTimedOut()) {
			infoText += "Time Out\n";
		}
		infoText += event.getRoundResult().toString();
		
		this.arenaDisplay.setInfoText(infoText);
		this.infoPanel.removeAllBlockWindows();
	}

	private void onMatchEnded(MatchEndedEvent event) {
		String infoText = "Match Over\n";
		infoText += event.getMatchResult().toString();
		this.arenaDisplay.setInfoText(infoText);
		this.infoPanel.removeAllBlockWindows();
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
		RingmasterInfoPanel ringmasterPanel = this.infoPanel.getRingmasterPanel();
		ringmasterPanel.setLastAction(event.getActionType(), this.infoPanel.getRoundTime());
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
				// The ringmaster's contribution is the BASE colour of the flame
				// (i.e., the flame with no colours added), so we don't want to mix the colour
				// with other colours...
				if (colours.length > 1) {
					colours[i] = Color.black;
				}
				else {
					colours[i] = ArenaDisplay.RINGMASTER_COLOUR;
				}
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

	private void onTestRound() {
		this.arenaDisplay.clearRoundResults();
		this.arenaDisplay.setInfoText("Test Round");
		this.infoPanel.getPlayer1Panel().setUnlimitedMovesCheckBoxEnabled(false);
		this.infoPanel.getPlayer2Panel().setUnlimitedMovesCheckBoxEnabled(false);
	}
	
	void displayNotConnectedDialog(boolean showDialog) {
		this.connectingDlg.setVisible(showDialog);
	}
	
	@Override
	public void actionPerformed(ActionEvent event) {
		if (event.getSource() == this.gloveInfoWindowMenuItem) {
			this.gloveDataWindow.setVisible(true);
		}
		else if (event.getSource() == this.customActionMenuItem) {
			this.customActionWindow.setVisible(true);
		}
		else if (event.getSource() == this.glowflyTestMenuItem) {
			this.glowflyTestWindow.setVisible(true);
		}
		else if (event.getSource() == this.boardQueryTestMenuItem) {
			this.boardTestWindow.setVisible(true);
		}
	}
	
	private void setupCustomActionFrame() {
		assert(this.customActionWindow == null);
		
		this.customActionWindow = new JFrame();
		this.customActionWindow.setTitle("Action Prototyping");
		this.customActionWindow.setResizable(false);
		assert(this.customActionPanel != null);
		this.customActionWindow.add(this.customActionPanel);
		this.customActionWindow.setMinimumSize(new Dimension(500, 1));
		this.customActionWindow.pack();
		
		this.customActionWindow.setLocationRelativeTo(null);
	}
	
	private void setupGloveDataFrame() {
		assert(this.gloveDataWindow == null);
		this.gloveDataWindow = new JFrame();
		
		JPanel basePanel = new JPanel();
		basePanel.setLayout(new GridLayout(3, 1));
		
		JPanel p1GloveDataPanel = new JPanel();
		TitledBorder border = BorderFactory.createTitledBorder(
				BorderFactory.createLineBorder(Color.black), "Player 1 Glove Information");
		border.setTitleColor(Color.black);
		p1GloveDataPanel.setBorder(border);
		p1GloveDataPanel.setLayout(new GridLayout(0, 2));
		assert(this.p1LeftGloveInfoPanel != null);
		assert(this.p1RightGloveInfoPanel != null);
		p1GloveDataPanel.add(this.p1LeftGloveInfoPanel);
		p1GloveDataPanel.add(this.p1RightGloveInfoPanel);
		basePanel.add(p1GloveDataPanel);
		
		JPanel p2GloveDataPanel = new JPanel();
		border = BorderFactory.createTitledBorder(
				BorderFactory.createLineBorder(Color.black), "Player 2 Glove Information");
		border.setTitleColor(Color.black);
		p2GloveDataPanel.setBorder(border);
		p2GloveDataPanel.setLayout(new GridLayout(0, 2));
		assert(this.p2LeftGloveInfoPanel != null);
		assert(this.p2RightGloveInfoPanel != null);
		p2GloveDataPanel.add(this.p2LeftGloveInfoPanel);
		p2GloveDataPanel.add(this.p2RightGloveInfoPanel);
		basePanel.add(p2GloveDataPanel);
		
		JPanel ringmasterGloveDataPanel = new JPanel();
		border = BorderFactory.createTitledBorder(
				BorderFactory.createLineBorder(Color.black), "Ringmaster Glove Information");
		border.setTitleColor(Color.black);
		ringmasterGloveDataPanel.setBorder(border);
		ringmasterGloveDataPanel.setLayout(new GridLayout(0, 2));
		assert(this.rmLeftGloveInfoPanel  != null);
		assert(this.rmRightGloveInfoPanel != null);
		ringmasterGloveDataPanel.add(this.rmLeftGloveInfoPanel);
		ringmasterGloveDataPanel.add(this.rmRightGloveInfoPanel);
		basePanel.add(ringmasterGloveDataPanel);
		
		this.gloveDataWindow.setTitle("Glove Information");
		this.gloveDataWindow.setResizable(false);
		
		this.gloveDataWindow.add(basePanel);
		this.gloveDataWindow.pack();
		this.gloveDataWindow.setLocationRelativeTo(null);
	}
	
	private void initMacStuff() {
		Application app = Application.getApplication();
		try {
			Image ssfImage = ImageIO.read(getClass().getResource("ssfsmall.jpg"));
			app.setDockIconImage(ssfImage);
			app.setQuitHandler(new QuitHandler() {
				public @Override void handleQuitRequestWith(QuitEvent e, QuitResponse response) {
					DevGUIMainWindow.this.ioserver.stop();
					response.performQuit();
				}
			});
		} catch (IOException ex) {
			log.warn("Couldn't set dock image on Mac");
		}
		
	}
	
	public static void main(String[] argv) {
		
		final CommandLineArgs args = new CommandLineArgs();
		// populates args from argv
		new JCommander(args, argv);
		
		LoggingUtil.configureLogging(args.verbosity);
		
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception ex) {
			LoggerFactory.getLogger(DevGUIMainWindow.class).warn("Couldn't set system look and feel", ex);
		}
		
		final IOServer ioserver = new IOServer(args);
		Thread serverThread = new Thread(new Runnable() {
			public @Override void run() {
				ioserver.start();
			}
		}, "IOServer main thread");
		serverThread.start();
		
		DevGUIMainWindow window = new DevGUIMainWindow(ioserver, args);
		window.getThisPartyStarted();
		window.setVisible(true);
	}

	@Override
	public void deviceStatusChanged(DeviceStatus status) {
		{
			//InetAddress p1HeadsetAddr    = status.getDeviceAddress(Device.P1_HEADSET);
			InetAddress p1LeftGloveAddr  = status.getDeviceAddress(Device.P1_LEFT_GLOVE);
			InetAddress p1RightGloveAddr = status.getDeviceAddress(Device.P1_RIGHT_GLOVE);
			
			//String p1HeadsetAddrStr    = (p1HeadsetAddr == null)    ? "" : p1HeadsetAddr.getHostAddress();
			String p1LeftGloveAddrStr  = (p1LeftGloveAddr == null)  ? "" : p1LeftGloveAddr.getHostAddress();
			String p1RightGloveAddrStr = (p1RightGloveAddr == null) ? "" : p1RightGloveAddr.getHostAddress();
			
			//this.p1HeadsetInfoPanel.setIPAddress(p1HeadsetAddrStr);
			this.p1LeftGloveInfoPanel.setIPAddress(p1LeftGloveAddrStr);
			this.p1RightGloveInfoPanel.setIPAddress(p1RightGloveAddrStr);
			
			float p1LeftGloveSignalPercent = status.getDeviceRssi(Device.P1_LEFT_GLOVE);
			float p1RightGloveSignalPercent = status.getDeviceRssi(Device.P1_RIGHT_GLOVE);
			
			this.p1LeftGloveInfoPanel.setSignalPercent(100.0f * p1LeftGloveSignalPercent);
			this.p1RightGloveInfoPanel.setSignalPercent(100.0f * p1RightGloveSignalPercent);
			
			float p1LeftGloveBatteryPercent  = status.getDeviceBattery(Device.P1_LEFT_GLOVE);
			float p1RightGloveBatteryPercent = status.getDeviceBattery(Device.P1_RIGHT_GLOVE);
					
			this.p1LeftGloveInfoPanel.setBatteryPercent(100.0f * p1LeftGloveBatteryPercent);
			this.p1RightGloveInfoPanel.setBatteryPercent(100.0f * p1RightGloveBatteryPercent);
			
			this.p1LeftGloveInfoPanel.setLastUpdateTime(status.getLastUpdateTime(Device.P1_LEFT_GLOVE));
			this.p1RightGloveInfoPanel.setLastUpdateTime(status.getLastUpdateTime(Device.P1_RIGHT_GLOVE));
		}
		
		{
			//InetAddress p2HeadsetAddr    = status.getDeviceAddress(Device.P2_HEADSET);
			InetAddress p2LeftGloveAddr  = status.getDeviceAddress(Device.P2_LEFT_GLOVE);
			InetAddress p2RightGloveAddr = status.getDeviceAddress(Device.P2_RIGHT_GLOVE);
			
			//String p2HeadsetAddrStr    = (p2HeadsetAddr == null)    ? "" : p2HeadsetAddr.getHostAddress();
			String p2LeftGloveAddrStr  = (p2LeftGloveAddr == null)  ? "" : p2LeftGloveAddr.getHostAddress();
			String p2RightGloveAddrStr = (p2RightGloveAddr == null) ? "" : p2RightGloveAddr.getHostAddress();
			
			//this.p2HeadsetInfoPanel.setIPAddress(p2HeadsetAddrStr);
			this.p2LeftGloveInfoPanel.setIPAddress(p2LeftGloveAddrStr);
			this.p2RightGloveInfoPanel.setIPAddress(p2RightGloveAddrStr);
			
			float p2LeftGloveSignalPercent = status.getDeviceRssi(Device.P2_LEFT_GLOVE);
			float p2RightGloveSignalPercent = status.getDeviceRssi(Device.P2_RIGHT_GLOVE);
			
			this.p2LeftGloveInfoPanel.setSignalPercent(100.0f * p2LeftGloveSignalPercent);
			this.p2RightGloveInfoPanel.setSignalPercent(100.0f * p2RightGloveSignalPercent);
			
			float p2LeftGloveBatteryPercent  = status.getDeviceBattery(Device.P2_LEFT_GLOVE);
			float p2RightGloveBatteryPercent = status.getDeviceBattery(Device.P2_RIGHT_GLOVE);
					
			this.p2LeftGloveInfoPanel.setBatteryPercent(100.0f * p2LeftGloveBatteryPercent);
			this.p2RightGloveInfoPanel.setBatteryPercent(100.0f * p2RightGloveBatteryPercent);
			
			this.p2LeftGloveInfoPanel.setLastUpdateTime(status.getLastUpdateTime(Device.P2_LEFT_GLOVE));
			this.p2RightGloveInfoPanel.setLastUpdateTime(status.getLastUpdateTime(Device.P2_RIGHT_GLOVE));
		}
		
		{
			InetAddress rmLeftGloveAddr  = status.getDeviceAddress(Device.RM_LEFT_GLOVE);
			InetAddress rmRightGloveAddr = status.getDeviceAddress(Device.RM_RIGHT_GLOVE);
			
			String rmLeftGloveAddrStr  = (rmLeftGloveAddr == null)  ? "" : rmLeftGloveAddr.getHostAddress();
			String rmRightGloveAddrStr = (rmRightGloveAddr == null) ? "" : rmRightGloveAddr.getHostAddress();
			
			//this.p2HeadsetInfoPanel.setIPAddress(p2HeadsetAddrStr);
			this.rmLeftGloveInfoPanel.setIPAddress(rmLeftGloveAddrStr);
			this.rmRightGloveInfoPanel.setIPAddress(rmRightGloveAddrStr);
			
			float rmLeftGloveSignalPercent = status.getDeviceRssi(Device.RM_LEFT_GLOVE);
			float rmRightGloveSignalPercent = status.getDeviceRssi(Device.RM_RIGHT_GLOVE);
			
			this.rmLeftGloveInfoPanel.setSignalPercent(100.0f * rmLeftGloveSignalPercent);
			this.rmRightGloveInfoPanel.setSignalPercent(100.0f * rmRightGloveSignalPercent);
			
			float rmLeftGloveBatteryPercent  = status.getDeviceBattery(Device.RM_LEFT_GLOVE);
			float rmRightGloveBatteryPercent = status.getDeviceBattery(Device.RM_RIGHT_GLOVE);
					
			this.rmLeftGloveInfoPanel.setBatteryPercent(100.0f * rmLeftGloveBatteryPercent);
			this.rmRightGloveInfoPanel.setBatteryPercent(100.0f * rmRightGloveBatteryPercent);
			
			this.rmLeftGloveInfoPanel.setLastUpdateTime(status.getLastUpdateTime(Device.RM_LEFT_GLOVE));
			this.rmRightGloveInfoPanel.setLastUpdateTime(status.getLastUpdateTime(Device.RM_RIGHT_GLOVE));
		}
	}

}
