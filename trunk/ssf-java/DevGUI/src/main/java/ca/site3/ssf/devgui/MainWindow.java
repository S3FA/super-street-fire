package ca.site3.ssf.devgui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import javax.swing.BorderFactory;
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

import ca.site3.ssf.gamemodel.FireEmitterChangedEvent;
import ca.site3.ssf.gamemodel.FireEmitterConfig;
import ca.site3.ssf.gamemodel.GameInfoRefreshEvent;
import ca.site3.ssf.gamemodel.GameStateChangedEvent;
import ca.site3.ssf.gamemodel.IGameModel;
import ca.site3.ssf.gamemodel.IGameModel.Entity;
import ca.site3.ssf.gamemodel.IGameModelEvent;
import ca.site3.ssf.gamemodel.MatchEndedEvent;
import ca.site3.ssf.gamemodel.PlayerAttackActionEvent;
import ca.site3.ssf.gamemodel.PlayerBlockActionEvent;
import ca.site3.ssf.gamemodel.PlayerHealthChangedEvent;
import ca.site3.ssf.gamemodel.RingmasterActionEvent;
import ca.site3.ssf.gamemodel.RoundBeginTimerChangedEvent;
import ca.site3.ssf.gamemodel.RoundEndedEvent;
import ca.site3.ssf.gamemodel.RoundPlayTimerChangedEvent;
import ca.site3.ssf.gamemodel.GameState;
import ca.site3.ssf.guiprotocol.StreetFireGuiClient;
import ca.site3.ssf.ioserver.CommandLineArgs;
import ca.site3.ssf.ioserver.DeviceConstants.Device;
import ca.site3.ssf.ioserver.DeviceStatus;
import ca.site3.ssf.ioserver.DeviceStatus.IDeviceStatusListener;
import ca.site3.ssf.ioserver.IOServer;
import ch.qos.logback.classic.Level;

import com.beust.jcommander.JCommander;


/**
 * Main frame for the developer GUI. Displays current state of the FireEmitterModel, 
 * player health, game status etc. 
 *  
 *  @author Callum
 */
public class MainWindow extends JFrame implements ActionListener, IDeviceStatusListener {
	
	private static final long serialVersionUID = 1L;
	private Logger log = LoggerFactory.getLogger(getClass());
	
	private JMenuBar menuBar = null;
	private JMenu windowMenu = null;
	private JMenuItem gloveInfoWindowMenuItem = null;
	
	private GloveDataInfoPanel p1LeftGloveInfoPanel  = null;
	private GloveDataInfoPanel p1RightGloveInfoPanel = null;
	private GloveDataInfoPanel p2LeftGloveInfoPanel  = null;
	private GloveDataInfoPanel p2RightGloveInfoPanel = null;
	private GloveDataInfoPanel ringmasterLeftGloveInfoPanel  = null;
	private GloveDataInfoPanel ringmasterRightGloveInfoPanel = null;
	
	private ArenaDisplay arenaDisplay = null;
	private GameInfoPanel infoPanel   = null;
	private ControlPanel controlPanel = null;
    private IGameModel gameModel      = null;	
    private IOServer ioserver         = null;
    
    private CommandLineArgs args       = null;
    private StreetFireGuiClient client = null;
    
    /** thread that monitors the queue for game model events */
    private Thread gameEventThread;
    
    
	public MainWindow(IOServer ioserver, CommandLineArgs args) {
		this.args = args;
		this.ioserver = ioserver;
		this.gameModel = ioserver.getGameModel();
	}
	
	
	private void getThisPartyStarted() {
		
		InetAddress localhost = null;
		try {
			localhost = InetAddress.getLocalHost();
		} catch (UnknownHostException ex) {
			log.error("Could not find localhost",ex);
		}
		client = new StreetFireGuiClient(localhost, args.guiPort);
		
		try {
			client.connect();
		} catch (IOException ex) {
			log.error("DevGUI could not connect to IOServer",ex);
		}
		
		// Setup the frame's basic characteristics...
		this.setTitle("Super Street Fire (Developer GUI)");
		this.setPreferredSize(new Dimension(1000, 750));
		this.setMinimumSize(new Dimension(1000, 750));
		this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		this.setLayout(new BorderLayout());
		
		this.menuBar = new JMenuBar();
		this.windowMenu = new JMenu("Window");
		this.gloveInfoWindowMenuItem = new JMenuItem("Glove Information");
		this.gloveInfoWindowMenuItem.addActionListener(this);
		this.windowMenu.add(this.gloveInfoWindowMenuItem);
		this.menuBar.add(this.windowMenu);
		
		this.setJMenuBar(this.menuBar);
		
		this.p1LeftGloveInfoPanel  = new GloveDataInfoPanel(GloveDataInfoPanel.GloveType.LEFT_GLOVE);
		this.p1RightGloveInfoPanel = new GloveDataInfoPanel(GloveDataInfoPanel.GloveType.RIGHT_GLOVE);
		this.p2LeftGloveInfoPanel  = new GloveDataInfoPanel(GloveDataInfoPanel.GloveType.LEFT_GLOVE);
		this.p2RightGloveInfoPanel = new GloveDataInfoPanel(GloveDataInfoPanel.GloveType.RIGHT_GLOVE);		
		this.ringmasterLeftGloveInfoPanel  = new GloveDataInfoPanel(GloveDataInfoPanel.GloveType.LEFT_GLOVE);
		this.ringmasterRightGloveInfoPanel = new GloveDataInfoPanel(GloveDataInfoPanel.GloveType.RIGHT_GLOVE);	
		
		// Setup the frame's contents...
		this.arenaDisplay = new ArenaDisplay(gameModel.getConfiguration().getNumRoundsPerMatch(), new FireEmitterConfig(true, 16, 8), client);
		Container contentPane = this.getContentPane();
		contentPane.add(this.arenaDisplay, BorderLayout.CENTER);
		
		JPanel infoAndControlPanel = new JPanel();
		infoAndControlPanel.setLayout(new BorderLayout());
		
		this.infoPanel = new GameInfoPanel();
		infoAndControlPanel.add(this.infoPanel, BorderLayout.NORTH);
		
		this.controlPanel = new ControlPanel(gameModel.getActionFactory(), client);
		infoAndControlPanel.add(this.controlPanel, BorderLayout.CENTER);
		
		contentPane.add(infoAndControlPanel, BorderLayout.SOUTH);

		this.pack();
		this.setLocationRelativeTo(null);
		
		gameEventThread = new Thread("DevGUI game event listener thread") {
			public @Override void run() {
				while (true) {
					try {
						IGameModelEvent event = client.getEventQueue().take();
						handleGameModelEvent(event);
					} catch (InterruptedException ex) {
						log.warn("DevGUI interrupted while waiting for game model event",ex);
					}
				}
			}
		};
		gameEventThread.start();
		
		this.ioserver.getDeviceStatus().addListener(this);
	}

	 
	private void handleGameModelEvent(final IGameModelEvent event) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				switch (event.getType()) {
				case GAME_INFO_REFRESH:
					MainWindow.this.onGameInfoRefresh((GameInfoRefreshEvent)event);
					break;
				case FIRE_EMITTER_CHANGED:
					MainWindow.this.onFireEmitterChanged((FireEmitterChangedEvent)event);
					break;
				case GAME_STATE_CHANGED:
					MainWindow.this.onGameStateChanged((GameStateChangedEvent)event);
					break;
				case MATCH_ENDED:
					MainWindow.this.onMatchEnded((MatchEndedEvent)event);
					break;
				case PLAYER_ATTACK_ACTION:
					MainWindow.this.onPlayerAttackAction((PlayerAttackActionEvent)event);
					break;
				case PLAYER_BLOCK_ACTION:
					MainWindow.this.onPlayerBlockAction((PlayerBlockActionEvent)event);
					break;
				case PLAYER_HEALTH_CHANGED:
					MainWindow.this.onPlayerHealthChanged((PlayerHealthChangedEvent)event);
					break;
				case RINGMASTER_ACTION:
					MainWindow.this.onRingmasterAction((RingmasterActionEvent)event);
					break;
				case ROUND_BEGIN_TIMER_CHANGED:
					MainWindow.this.onRoundBeginFightTimerChanged((RoundBeginTimerChangedEvent)event);
					break;
				case ROUND_ENDED:
					MainWindow.this.onRoundEnded((RoundEndedEvent)event);
					break;
				case ROUND_PLAY_TIMER_CHANGED:
					MainWindow.this.onRoundPlayTimerChanged((RoundPlayTimerChangedEvent)event);
					break;
				default:
					assert(false);
					break;
				}				
			}
		});
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
			
		case ROUND_ENDED_STATE: {
			List<RoundEndedEvent.RoundResult> roundResults = event.getCurrentRoundResults();
			assert(!roundResults.isEmpty());
			
			int roundNumber = roundResults.size();
			int roundIndex  = roundNumber - 1;
			
			this.onRoundEnded(new RoundEndedEvent(roundNumber, roundResults.get(roundIndex), event.getRoundTimedOut()));
			break;
		}
		
		case MATCH_ENDED_STATE:
			this.onMatchEnded(new MatchEndedEvent(event.getMatchResult()));
			break;
		
		default:
			break;
		}
		
	}
	
	private void onGameStateChanged(GameStateChangedEvent event) {
		this.infoPanel.setPreviousGameState(event.getOldState());
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
		default:
			break;
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
	}

	private void onMatchEnded(MatchEndedEvent event) {
		String infoText = "Match Over\n";
		infoText += event.getMatchResult().toString();
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

	@Override
	public void actionPerformed(ActionEvent event) {
		if (event.getSource() == this.gloveInfoWindowMenuItem) {
			JFrame gloveDataWindow = new JFrame();
			
			JPanel basePanel = new JPanel();
			basePanel.setLayout(new GridLayout(3, 1));
			
			JPanel p1GloveDataPanel = new JPanel();
			TitledBorder border = BorderFactory.createTitledBorder(
					BorderFactory.createLineBorder(Color.black), "Player 1 Glove Information");
			border.setTitleColor(Color.black);
			p1GloveDataPanel.setBorder(border);
			p1GloveDataPanel.setLayout(new GridLayout(0, 2));
			p1GloveDataPanel.add(this.p1LeftGloveInfoPanel);
			p1GloveDataPanel.add(this.p1RightGloveInfoPanel);
			basePanel.add(p1GloveDataPanel);
			
			JPanel p2GloveDataPanel = new JPanel();
			border = BorderFactory.createTitledBorder(
					BorderFactory.createLineBorder(Color.black), "Player 2 Glove Information");
			border.setTitleColor(Color.black);
			p2GloveDataPanel.setBorder(border);
			p2GloveDataPanel.setLayout(new GridLayout(0, 2));
			p2GloveDataPanel.add(this.p2LeftGloveInfoPanel);
			p2GloveDataPanel.add(this.p2RightGloveInfoPanel);
			basePanel.add(p2GloveDataPanel);
			
			JPanel ringmasterGloveDataPanel = new JPanel();
			border = BorderFactory.createTitledBorder(
					BorderFactory.createLineBorder(Color.black), "Ringmaster Glove Information");
			border.setTitleColor(Color.black);
			ringmasterGloveDataPanel.setBorder(border);
			ringmasterGloveDataPanel.setLayout(new GridLayout(0, 2));
			ringmasterGloveDataPanel.add(this.ringmasterLeftGloveInfoPanel);
			ringmasterGloveDataPanel.add(this.ringmasterRightGloveInfoPanel);
			basePanel.add(ringmasterGloveDataPanel);
			
			gloveDataWindow.setTitle("Glove Information");
			gloveDataWindow.setResizable(false);
			
			gloveDataWindow.add(basePanel);
			gloveDataWindow.pack();
			gloveDataWindow.setLocationRelativeTo(null);
			gloveDataWindow.setVisible(true);
		}
	}
	
	public static void main(String[] argv) {
		
		final CommandLineArgs args = new CommandLineArgs();
		// populates args from argv
		new JCommander(args, argv);
		
		configureLogging(args.verbosity);
		
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception ex) {
			LoggerFactory.getLogger(MainWindow.class).warn("Couldn't set system look and feel", ex);
		}
		
		final IOServer ioserver = new IOServer(args);
		Thread serverThread = new Thread(new Runnable() {
			public @Override void run() {
				ioserver.start();
			}
		}, "IOServer main thread");
		serverThread.start();
		
		MainWindow window = new MainWindow(ioserver, args);
		window.getThisPartyStarted();
		window.setVisible(true);
	}

	private static void configureLogging(int level) {
		ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger)
				LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
		
		switch (level) {
			case 0:
				root.setLevel(Level.OFF); break;
			case 1:
				root.setLevel(Level.TRACE); break;
			case 2:
				root.setLevel(Level.DEBUG); break;
			case 3:
				root.setLevel(Level.INFO); break;
			case 4:
				root.setLevel(Level.WARN); break;
			case 5:
				root.setLevel(Level.ERROR); break;
			default:
				root.setLevel(Level.INFO);
		}
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
			
			this.p1LeftGloveInfoPanel.setSignalPercent(p1LeftGloveSignalPercent);
			this.p1RightGloveInfoPanel.setSignalPercent(p1RightGloveSignalPercent);
			
			float p1LeftGloveBatteryPercent  = status.getDeviceBattery(Device.P1_LEFT_GLOVE);
			float p1RightGloveBatteryPercent = status.getDeviceBattery(Device.P1_RIGHT_GLOVE);
					
			this.p1LeftGloveInfoPanel.setBatteryPercent(p1LeftGloveBatteryPercent);
			this.p1RightGloveInfoPanel.setBatteryPercent(p1RightGloveBatteryPercent);
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
			
			this.p2LeftGloveInfoPanel.setSignalPercent(p2LeftGloveSignalPercent);
			this.p2RightGloveInfoPanel.setSignalPercent(p2RightGloveSignalPercent);
			
			float p2LeftGloveBatteryPercent  = status.getDeviceBattery(Device.P2_LEFT_GLOVE);
			float p2RightGloveBatteryPercent = status.getDeviceBattery(Device.P2_RIGHT_GLOVE);
					
			this.p2LeftGloveInfoPanel.setBatteryPercent(p2LeftGloveBatteryPercent);
			this.p2RightGloveInfoPanel.setBatteryPercent(p2RightGloveBatteryPercent);			
		}
	}

}
