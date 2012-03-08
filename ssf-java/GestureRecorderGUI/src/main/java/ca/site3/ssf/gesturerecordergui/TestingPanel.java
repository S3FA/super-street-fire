package ca.site3.ssf.gesturerecordergui;

import java.awt.Color;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import ca.site3.ssf.gesturerecognizer.GestureInstance;
import ca.site3.ssf.gesturerecognizer.GestureRecognitionResult;
import ca.site3.ssf.gesturerecognizer.GestureRecognizer;

/**
 * A container for testing incoming gesture data against a gesture recognition engine
 * @author Mike
 *
 */
class TestingPanel extends JPanel implements ActionListener {

	private static final long serialVersionUID = 1L;
	
	private JTextField fileName;
	private JButton selectFileButton;
	private JButton testButton;
	
	private LoggerPanel loggerPanel;
	private ControlPanel controlPanel;
	private GestureRecognizer gestureRecognizer;
	private boolean isEngineLoaded;
	private JFileChooser fileChooser;
	
	TestingPanel() {
		super();
		
		Color borderColour = Color.black;
		
		TitledBorder border = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(borderColour), "Gesture Trainer");
		border.setTitleColor(borderColour);
		this.setBorder(border);
		
		TitledBorder fileBorder = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(borderColour), "Info");
		border.setTitleColor(borderColour);		
		
        GridBagLayout layout = new GridBagLayout();
        this.setLayout(layout);
        
		FormLayoutHelper formLayoutHelper = new FormLayoutHelper();

		JPanel wrapperPanel = new JPanel();
		wrapperPanel.setBorder(fileBorder);
		wrapperPanel.setLayout(layout);
		
		this.controlPanel = new ControlPanel();
		this.fileChooser = new JFileChooser();
		this.fileName = new JTextField(25);
		this.fileName.setEditable(false);
		
		this.selectFileButton = new JButton("Choose File...");
		this.selectFileButton.addActionListener(this);
		
		this.testButton = new JButton("Load Selected Engine");
		this.testButton.addActionListener(this);
		
		this.loggerPanel = new LoggerPanel("Recognizer Results");
		
		formLayoutHelper.addLabel(this.fileName, wrapperPanel);
		formLayoutHelper.addLabel(this.selectFileButton, wrapperPanel);
		formLayoutHelper.addLastField(new JLabel(""), wrapperPanel);
		
		formLayoutHelper.addLabel(this.testButton, wrapperPanel);
		formLayoutHelper.addLastField(new JLabel(""), wrapperPanel);
		
		formLayoutHelper.addLastField(wrapperPanel, this);
		formLayoutHelper.addLastField(this.controlPanel, this);
		formLayoutHelper.addLastField(this.loggerPanel, this);
		
		this.isEngineLoaded = false;
	}
	
	// Handles button events
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == this.selectFileButton)
		{
			handleFileChooserDialog();
		}
		else if(e.getSource() == this.testButton)
		{
			loadRecognizerEngineFile(this.fileName.getText());
		}
	}
	
	// Displays the file chooser and adds the file to the training list if applicable
	public void handleFileChooserDialog()
	{
		int fileStatus = this.fileChooser.showOpenDialog(this.selectFileButton);
		
		if (fileStatus == JFileChooser.APPROVE_OPTION) {
            File file = this.fileChooser.getSelectedFile();
            this.fileName.setText(file.getAbsolutePath());
        } 
	}
	
	// Loads a gesture recognizer engine from a file
	public void loadRecognizerEngineFile(String fileName)
	{
		File file = new File(fileName);
		FileReader reader = null;
		GestureRecognizer gestureRecognizer = new GestureRecognizer();
		
		try 
		{
			reader = new FileReader(file);
			gestureRecognizer.loadRecognizerEngine(reader);
			
			this.gestureRecognizer = gestureRecognizer;
			this.isEngineLoaded = true;
			this.loggerPanel.appendLogText("Engine '" + file.getName() + "' loaded successfully!\n");
		} 
		catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			this.loggerPanel.appendLogText("Attempted to load invalid engine.\n");
		}
	}
	
	// Tests a gesture instance against the loaded engine
	public void testGestureInstance(GestureInstance instance)
	{
		GestureRecognitionResult result = this.gestureRecognizer.recognizePlayerGesture(instance);  
		this.loggerPanel.appendLogText(result.toString() + "\n");
	}
	
	
	// Returns whether a recognition engine has been loaded 
	public boolean isEngineLoaded()
	{
		return this.isEngineLoaded;
	}
}
