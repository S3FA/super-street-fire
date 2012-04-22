package ca.site3.ssf.gesturerecordergui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Scanner;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import ca.site3.ssf.gesturerecognizer.GestureDataSet;
import ca.site3.ssf.gesturerecognizer.GestureInstance;
import ca.site3.ssf.gesturerecognizer.GestureRecognizer;
import ca.site3.ssf.gesturerecognizer.GestureType;

/**
 * A class to transform gesture instance files into a gesture recognition engine and save them to file
 * @author Mike
 *
 */
class TrainingPanel extends JPanel implements ActionListener {

	private static final long serialVersionUID = 1L;
	
	private JTextField fileName;
	private JButton selectFileButton;
	private JButton trainFilesButton;
	private JComboBox gestureType;
	private LoggerPanel trainingFileListPanel;
	private LoggerPanel loggingPanel;
	private JFileChooser fileChooser;
	
	TrainingPanel() {
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
		this.fileChooser.setMultiSelectionEnabled(true);
		this.fileChooser.setFileFilter(new FileNameExtensionFilter("gesture instance files (*.ins)", "ins"));
		
		this.gestureType = formLayoutHelper.constructGestureComboBox();
		this.fileName = new JTextField(25);
		this.fileName.setEditable(false);
		
		this.selectFileButton = new JButton("Add a File...");
		this.selectFileButton.addActionListener(this);
		
		this.trainFilesButton = new JButton("Train!");
		this.trainFilesButton.addActionListener(this);
		
		// Allow the user to edit the file list
		this.trainingFileListPanel = new LoggerPanel("Training Files");
		this.loggingPanel = new LoggerPanel("Log");
		this.loggingPanel.setTextAreaSize(5, 65);
		
		JLabel gestureTypeLabel = new JLabel("Select Gesture: ");
		gestureTypeLabel.setForeground(Color.black);
		formLayoutHelper.addLabel(gestureTypeLabel, wrapperPanel);
		formLayoutHelper.addLabel(this.gestureType, wrapperPanel);
		formLayoutHelper.addLastField(new JLabel(""), wrapperPanel);
		
		formLayoutHelper.addLabel(this.selectFileButton, wrapperPanel);
		formLayoutHelper.addLabel(this.trainFilesButton, wrapperPanel);
		formLayoutHelper.addLastField(new JLabel(""), wrapperPanel);
	
		formLayoutHelper.addLastField(wrapperPanel, this);
		formLayoutHelper.addLastField(this.trainingFileListPanel, this);
		formLayoutHelper.addLastField(this.loggingPanel, this);
		
	}
	
	// Handles button events
	public void actionPerformed(ActionEvent e)
	{
		// Add the file to the list of files to convert
		if (e.getSource() == this.selectFileButton)
		{
			handleFileChooserDialog();
		}
		else if(e.getSource() == this.trainFilesButton)
		{
			GestureRecognizer gestureRecognizer = buildGestureRecognitionEngineFromFileList(this.trainingFileListPanel.getLogText());
			exportGestureRecognizerEnginer(gestureRecognizer);
		}
	}
	
	// Displays the file chooser and adds the file to the training list if applicable
	public void handleFileChooserDialog()
	{
		int fileStatus = this.fileChooser.showOpenDialog(this.selectFileButton);
		
		if (fileStatus == JFileChooser.APPROVE_OPTION) {
            File[] selectedFiles = this.fileChooser.getSelectedFiles();
            
            for(File file : selectedFiles)
            {
            	this.trainingFileListPanel.appendLogText(file.getAbsolutePath() + "\n");
            }
        } 
	}
	
	// Builds a gesture data set from the selected files
	public GestureRecognizer buildGestureRecognitionEngineFromFileList(String files) 
	{
		// Get a list of files from the log
		GestureDataSet gestureDataSet = new GestureDataSet();
		Scanner fileList = new Scanner(files);
		
		// Iterate through each file and create a gesture instance
		while(fileList.hasNextLine())
		{
			GestureInstance instance = new GestureInstance();
			StringBuilder gestureInstanceString = new StringBuilder();

			try 
			{
				// Read the file text of each file and reconstruct the data string
				String filePath = fileList.nextLine();
				Scanner fileTextScanner = new Scanner(new FileInputStream(filePath));
				
				while (fileTextScanner.hasNextLine())
				{
					gestureInstanceString.append(fileTextScanner.nextLine() + "\n");
				}
				
				// Create the gesture instance from the file's string and add it to the data set
				boolean success = instance.fromDataString(gestureInstanceString.toString());
				if (success) {
					gestureDataSet.addGestureInstance(instance);
				}
				else {
					this.loggingPanel.appendLogText("Failed to load gesture instance from file, bad file format: " + filePath);
				}
			} 
			catch (FileNotFoundException e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
		}
		
		GestureRecognizer gestureRecognizer = new GestureRecognizer();
		boolean success = gestureRecognizer.trainGesture((GestureType)this.gestureType.getSelectedItem(), gestureDataSet);	
		if(success) {
			this.loggingPanel.appendLogText("Training succeeded! (yay!)");
		}
		else {
			this.loggingPanel.appendLogText("The gesture instances selected cannot be trained!\n");
		}
		
		return gestureRecognizer;
	}
	
	// Exports the gesture recognition engine it to a file
	public void exportGestureRecognizerEnginer(GestureRecognizer gestureRecognizer)
	{
		FileInfoPanel fileInfoPanel = new FileInfoPanel();
		fileInfoPanel.exportToRecognizerEngine(gestureRecognizer);
	}
}
