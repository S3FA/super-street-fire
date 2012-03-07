package ca.site3.ssf.gesturerecordergui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import ca.site3.ssf.gesturerecognizer.GestureRecognizer;

// A container panel for the sensor data and the file info panels
class TestingPanel extends JPanel implements ActionListener {

	private static final long serialVersionUID = 1L;
	
	public JTextField fileName;
	public JButton selectFileButton;
	public JButton testButton;
	
	public LoggerPanel loggerPanel;
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
		
		this.fileChooser = new JFileChooser();
		this.fileName = new JTextField(25);
		this.fileName.setEditable(false);
		
		this.selectFileButton = new JButton("Choose File...");
		this.selectFileButton.addActionListener(this);
		
		this.testButton = new JButton("Test!");
		this.testButton.addActionListener(this);
		
		this.loggerPanel = new LoggerPanel("Recognizer Results");
		
		formLayoutHelper.addLabel(this.fileName, wrapperPanel);
		formLayoutHelper.addLabel(this.selectFileButton, wrapperPanel);
		formLayoutHelper.addLastField(new JLabel(""), wrapperPanel);
		
		formLayoutHelper.addLabel(this.testButton, wrapperPanel);
		formLayoutHelper.addLastField(new JLabel(""), wrapperPanel);
		
		formLayoutHelper.addLastField(wrapperPanel, this);
		formLayoutHelper.addLastField(this.loggerPanel, this);
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
		} 
		catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//TODO: What now?
	}
}
