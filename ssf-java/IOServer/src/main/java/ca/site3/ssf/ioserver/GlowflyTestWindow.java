package ca.site3.ssf.ioserver;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.WindowConstants;


@SuppressWarnings("serial")
public class GlowflyTestWindow extends JFrame implements ActionListener {
	private static final int NUM_BOARDS = 32;
	
	private IOServer ioserver;
	
	private List<JButton> boardButtons = new ArrayList<JButton>(32);
	
	private JButton broadcastOn;
	private JButton broadcastOff;
	
	
	public GlowflyTestWindow(IOServer server)  {
		super("Glowfly Testing");
		setLocationRelativeTo(null);
		
		ioserver = server;

		this.getContentPane().setLayout(new GridLayout(NUM_BOARDS / 2 + 1, 2));
		
		for (int i = 1; i <= NUM_BOARDS; i++) {
			JButton btn = new JButton("Turn board " + i + " on");
			boardButtons.add(btn);
			btn.addActionListener(this);
			btn.putClientProperty("fire", false);
			btn.putClientProperty("id", i);
			this.getContentPane().add(btn);
		}
		
		broadcastOn = new JButton("Broadcast glowflies on");
		broadcastOn.addActionListener(this);
		this.getContentPane().add(broadcastOn);
		
		broadcastOff = new JButton("Broadcast glowflies off");
		broadcastOff.addActionListener(this);
		this.getContentPane().add(broadcastOff);
		
		this.pack();
	}
	
	
	public void actionPerformed(ActionEvent e) {
		
		JButton btn = (JButton)e.getSource();
		SerialCommunicator serialComm = ioserver.getSerialCommunicator();
		assert(serialComm != null);
		
		if (btn == broadcastOn) {
			serialComm.setGlowfliesOn(true, true);
		}
		else if (btn == broadcastOff) {
			serialComm.setGlowfliesOn(false, true);
		}
		else {
			int boardId = (Integer)btn.getClientProperty("id");
			boolean isOn = (Boolean)btn.getClientProperty("fire");
			btn.setText("Turn board " + boardId + " " + (isOn? "off": "on"));
			btn.putClientProperty("fire", ! isOn);
			serialComm.toggleGlowfly(!isOn, boardId);
		}
	}
	
	
	public static void main(String... args) {
		GlowflyTestWindow w = new GlowflyTestWindow(null);
		w.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		w.setLocationRelativeTo(null);
		w.pack();
		w.setVisible(true);
	}
}
