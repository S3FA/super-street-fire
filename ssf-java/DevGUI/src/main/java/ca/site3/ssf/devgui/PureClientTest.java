package ca.site3.ssf.devgui;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.site3.ssf.common.LoggingUtil;
import ca.site3.ssf.gamemodel.GameInfoRefreshEvent;
import ca.site3.ssf.gamemodel.GameState;
import ca.site3.ssf.gamemodel.GameState.GameStateType;
import ca.site3.ssf.gamemodel.GameStateChangedEvent;
import ca.site3.ssf.gamemodel.IGameModelEvent;
import ca.site3.ssf.gamemodel.RoundBeginTimerChangedEvent;
import ca.site3.ssf.gamemodel.RoundPlayTimerChangedEvent;
import ca.site3.ssf.guiprotocol.StreetFireGuiClient;
import ca.site3.ssf.ioserver.CommandLineArgs;

import com.beust.jcommander.JCommander;

class PureClientTest extends JFrame implements ActionListener {
	private static final long serialVersionUID = 1L;
	
	private Logger log = LoggerFactory.getLogger(getClass());
	private final CommandLineArgs args;
	
	private StreetFireGuiClient client = null;
	private Thread gameEventThread = null;
	
	private JButton nextStateButton = null;
	private JLabel timerLabel       = null;
	
	private GameStateType nextState = null;
	
	PureClientTest(CommandLineArgs args) {
		this.args = args;
	}
	
	private void getThisPartyStarted() {
		
		InetAddress localhost = null;
		try {
			localhost = InetAddress.getLocalHost();
		} catch (UnknownHostException ex) {
			log.error("Could not find localhost",ex);
		}
		client = new StreetFireGuiClient(localhost, args.guiPort, true);
		
		try {
			client.connect();
		} catch (IOException ex) {
			log.error("DevGUI could not connect to IOServer",ex);
		}
		
		// Setup the frame's basic characteristics...
		this.setTitle("Super Street Fire (Pure Client Test)");
		this.setPreferredSize(new Dimension(500, 500));
		this.setMinimumSize(new Dimension(500, 500));
		this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		this.setLayout(new FlowLayout());
		
		this.timerLabel = new JLabel("");
		this.add(this.timerLabel);
		
		this.nextStateButton = new JButton("Next state");
		this.nextStateButton.addActionListener(this);
		this.add(this.nextStateButton);
		
		this.pack();
		this.setLocationRelativeTo(null);
		
		this.gameEventThread = new Thread("Pure client game event listener thread") {
			public @Override void run() {
				while (true) {
					try {
						IGameModelEvent event = client.getEventQueue().take();
						handleGameModelEvent(event);
					} catch (InterruptedException ex) {
						log.warn("Pure client interrupted while waiting for game model event",ex);
					}
				}
			}
		};
		gameEventThread.start();
	}
	
	private void handleGameModelEvent(final IGameModelEvent event) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				
				
				
				switch (event.getType()) {
				
				case GAME_INFO_REFRESH:
					log.info(event.getType().toString());
					onGameInfoRefresh((GameInfoRefreshEvent)event);
					break;
					
				case FIRE_EMITTER_CHANGED:
					//log.info(event.getType().toString());
					break;
					
				case GAME_STATE_CHANGED: {
					
					GameStateChangedEvent e = (GameStateChangedEvent)event;
					log.info(event.getType().toString() + " : " + e.getNewState());
					
					gameStateChanged(e.getNewState());
					break;
				}
				
				case MATCH_ENDED:
					log.info(event.getType().toString());
					break;
				case PLAYER_ATTACK_ACTION:
					log.info(event.getType().toString());
					break;
				case PLAYER_BLOCK_ACTION:
					log.info(event.getType().toString());
					break;
				case PLAYER_HEALTH_CHANGED:
					//log.info(event.getType().toString());
					break;
				case RINGMASTER_ACTION:
					log.info(event.getType().toString());
					break;
					
				case ROUND_BEGIN_TIMER_CHANGED: {
					log.info(event.getType().toString());
					RoundBeginTimerChangedEvent e = (RoundBeginTimerChangedEvent)event;
					timerLabel.setText(e.getThreeTwoOneFightTime().toString());
					break;
				}
				
				case ROUND_ENDED:
					log.info(event.getType().toString());
					break;
					
				case ROUND_PLAY_TIMER_CHANGED: {
					log.info(event.getType().toString());
					RoundPlayTimerChangedEvent e = (RoundPlayTimerChangedEvent)event;
					timerLabel.setText("" + e.getTimeInSecs());
					break;
				}
					
				case SYSTEM_INFO_REFRESH:
				default:
					assert(false);
					break;
				}
							
			}
		});
	}
	
	public static void main(String[] argv) {
		
		final CommandLineArgs args = new CommandLineArgs();
		new JCommander(args, argv);
		
		LoggingUtil.configureLogging(args.verbosity);
		
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch (Exception ex) {
			LoggerFactory.getLogger(PureClientTest.class).warn("Couldn't set system look and feel", ex);
		}
		
		PureClientTest window = new PureClientTest(args);
		window.getThisPartyStarted();
		window.setVisible(true);
	}

	private void gameStateChanged(GameState.GameStateType stateType) {
		if (stateType.isGoToNextStateControllable()) {
			this.nextState = stateType.nextControllableGoToStates().get(0);
			this.nextStateButton.setText(this.nextState.toString());
			this.nextStateButton.setEnabled(true);
		}
		else {
			this.nextStateButton.setEnabled(false);
		}
	}
	
	private void onGameInfoRefresh(GameInfoRefreshEvent event) {
		this.gameStateChanged(event.getCurrentGameState());
	}
	
	@Override
	public void actionPerformed(ActionEvent event) {
		if (event.getSource() == this.nextStateButton) {
			if (this.nextState != null) {
				try {
					this.client.initiateNextState(this.nextState);
				} catch (IOException e) {
					log.error("Failed to initiate next state.", e);
				}
			}
		}
		
	}
}
