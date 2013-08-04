package ca.site3.ssf.ioserver;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import ca.site3.ssf.gamemodel.SystemInfoRefreshEvent.OutputDeviceStatus;


@SuppressWarnings("serial")
public class BoardQueryTestWindow extends JFrame implements ActionListener {
	private static final int NUM_BOARDS = 32;
	
	private IOServer ioserver;
	
	private List<JButton> boardButtons = new ArrayList<JButton>(NUM_BOARDS);
	private JButton queryAllBoardsBtn;
	private JTextArea resultTextArea;

	public BoardQueryTestWindow(IOServer server)  {
		super("Board Query Testing");
		setLocationRelativeTo(null);
		
		assert(server != null);
		this.ioserver = server;

		this.getContentPane().setLayout(new BorderLayout());
		
		JPanel buttonPanel = new JPanel();
		final int NUM_COLS = 4;
		buttonPanel.setLayout(new GridLayout(NUM_BOARDS / NUM_COLS + 1, NUM_COLS));
		
		for (int i = 1; i <= NUM_BOARDS; i++) {
			JButton btn = new JButton("Query board " + i);
			this.boardButtons.add(btn);
			btn.addActionListener(this);
			btn.putClientProperty("num", i);
			buttonPanel.add(btn);
		}
		
		this.queryAllBoardsBtn = new JButton("Query All");
		this.queryAllBoardsBtn .addActionListener(this);
		buttonPanel.add(this.queryAllBoardsBtn);
		
		this.getContentPane().add(buttonPanel, BorderLayout.CENTER);
		
		JPanel resultPanel = new JPanel();
		resultPanel.setLayout(new BorderLayout());
		resultPanel.setBorder(new EmptyBorder(5,5,5,5));
		
		this.resultTextArea = new JTextArea(10, 1);
		this.resultTextArea.setEditable(false);
		this.resultTextArea.setEnabled(true);
		
		JScrollPane areaScrollPane = new JScrollPane(this.resultTextArea);
		areaScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		areaScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		areaScrollPane.setPreferredSize(new Dimension(250, 250));
		
		resultPanel.add(new JLabel("Results:"), BorderLayout.NORTH);
		resultPanel.add(areaScrollPane, BorderLayout.CENTER);
		
		this.getContentPane().add(resultPanel, BorderLayout.SOUTH);
		
		this.pack();
	}
	
	public void actionPerformed(ActionEvent e) {
		JButton btn = (JButton)e.getSource();
		SerialCommunicator serialComm = this.ioserver.getSerialCommunicator();
		if (serialComm == null) {
			this.resultTextArea.setText("No result: serial communicator is not initialized!");
			return;
		}

		if (btn == this.queryAllBoardsBtn) {
			OutputDeviceStatus[] boardStatuses = serialComm.queryAllBoards();
			String resultStr = "";
			for (int i = 0; i < boardStatuses.length; i++) {
				resultStr += this.buildResultMessage(boardStatuses[i]) + "\n";
			}
			this.resultTextArea.setText(resultStr);
		}
		else {
			int boardNumber = (Integer)btn.getClientProperty("num");
			String resultStr = this.buildResultMessage(serialComm.queryBoard(boardNumber));
			this.resultTextArea.setText(resultStr);
		}
	}
	
	private String buildResultMessage(OutputDeviceStatus status) {
		if (status == null) {
			assert(false);
			return "ERROR: null result was received.";
		}
		
		return "Query result (board #" + status.deviceId + "): {responding: " + status.isResponding + 
				", armed: " + status.isArmed + ", flame: " + status.isFlame + "}";	
	}
	
	public static void main(String... args) {
		BoardQueryTestWindow w = new BoardQueryTestWindow(null);
		w.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		w.setLocationRelativeTo(null);
		w.pack();
		w.setVisible(true);
	}
}
