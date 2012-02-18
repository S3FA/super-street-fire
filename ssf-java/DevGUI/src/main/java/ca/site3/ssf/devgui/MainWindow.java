package ca.site3.ssf.devgui;

import java.awt.Dimension;

import javax.swing.JFrame;

import ca.site3.ssf.gamemodel.FireEmitterConfig;


public class MainWindow extends JFrame {
	
	private ArenaDisplay arenaDisplay;
	
	public MainWindow() {
		super();
		
		// Setup the frame's basic characteristics...
		this.setTitle("Super Street Fire (Developer GUI)");
		this.setPreferredSize(new Dimension(1000, 800));
		this.setMinimumSize(new Dimension(800, 600));
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		
		// Setup the frame's contents...
		this.arenaDisplay = new ArenaDisplay(new FireEmitterConfig(true, 16, 8));
		this.getContentPane().add(this.arenaDisplay);
		
		
		this.pack();
		this.setLocationRelativeTo(null);
	}

	/**
	 * The main driver method for the Developer GUI.
	 * @param args
	 */
	public static void main(String[] args) {
		MainWindow mainWindow = new MainWindow();
		mainWindow.setVisible(true);
	}

}
