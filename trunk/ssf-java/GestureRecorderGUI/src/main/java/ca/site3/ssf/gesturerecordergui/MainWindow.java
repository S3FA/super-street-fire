package ca.site3.ssf.gesturerecordergui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import ca.site3.ssf.ioserver.HardwareEvent;

// The main window for the recorder GUI.
public class MainWindow extends JFrame implements HardwareEvent, ActionListener {
	
	private static final long serialVersionUID = 1L;

	private RecorderPanel recorderPanel   = null;
	private ControlPanel controlPanel = null;
	
	public MainWindow() {
		super();
		
		// Setup the frame's basic characteristics...
		this.setTitle("Super Street Fire (Gesture Recorder GUI)");
		this.setPreferredSize(new Dimension(600, 200));
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setLayout(new BorderLayout());
		
		// Setup the frame's contents...
		Container contentPane = this.getContentPane();
		
		JPanel wrapperPanel = new JPanel();
		wrapperPanel.setLayout(new BorderLayout());
		
		this.recorderPanel = new RecorderPanel();
		this.controlPanel = new ControlPanel();
		
		wrapperPanel.add(this.recorderPanel, BorderLayout.NORTH);
		wrapperPanel.add(this.controlPanel, BorderLayout.SOUTH);
		contentPane.add(wrapperPanel, BorderLayout.NORTH);
		
		this.pack();
		this.setLocationRelativeTo(null);
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

	// Triggered from IOServer. Set the coordinates data. If we're in record mode, save that data too.
	public void actionPerformed(ActionEvent event) {
		//if (event.getSource() == someHardwareEvent from IOServer)
			this.recorderPanel.sensorDataPanel.gyroscopeData.setText(""); // hardware data
			this.recorderPanel.sensorDataPanel.magnetometerData.setText(""); // hardware data
			this.recorderPanel.sensorDataPanel.accelerometerData.setText(""); // hardware data
			
			if (this.controlPanel.IsRecordMode){
				this.recorderPanel.fileInfoPanel.recordFileInformation(this.recorderPanel.sensorDataPanel.gyroscopeData.getText(),
																		this.recorderPanel.sensorDataPanel.magnetometerData.getText(),
																		this.recorderPanel.sensorDataPanel.accelerometerData.getText(),
																		this.recorderPanel.fileInfoPanel.gestureName.getText(),
																		this.recorderPanel.fileInfoPanel.fileName.getText() == "" ? "default" : this.recorderPanel.fileInfoPanel.fileName.getText());
			}
		//}
	}
}
