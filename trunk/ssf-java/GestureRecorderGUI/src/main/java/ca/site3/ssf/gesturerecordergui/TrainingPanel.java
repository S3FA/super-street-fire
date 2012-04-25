package ca.site3.ssf.gesturerecordergui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
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
	
	private static final String ENGINE_LOAD_DIALOG_PATH_KEY   = "EngineLoadDlgPath";
	private static final String ENGINE_SAVE_DIALOG_PATH_KEY   = "EngineSaveDlgPath";
	private static final String TRAINING_LOAD_DIALOG_PATH_KEY = "TrainingLoadDlgPath";
	private static final String SELECTED_GESTURE_TYPE_KEY     = "SelectedGestureType";
	
	private JButton selectFileButton;
	private JButton trainFilesButton;
	@SuppressWarnings("rawtypes")
	private JComboBox gestureType;
	
	private JButton loadGestureEngineButton;
	private JButton saveGestureEngineButton;
	private JButton clearGestureEngineButton;
	
	private LoggerPanel trainingFileListPanel;
	private LoggerPanel loggingPanel;
	
	private JFileChooser trainingFileChooser;
	private JFileChooser engineFileSaver;
	private JFileChooser engineFileLoader;
	
	private GestureRecognizer gestureRecognizer;
	
	TrainingPanel() {
		super();
		
		this.gestureRecognizer = new GestureRecognizer();
		
		Color borderColour = Color.black;
		
		TitledBorder border = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(borderColour), "Gesture Trainer");
		border.setTitleColor(borderColour);
		this.setBorder(border);
		
        GridBagLayout layout = new GridBagLayout();
        this.setLayout(layout);
        
		FormLayoutHelper formLayoutHelper = new FormLayoutHelper();

		JPanel wrapperPanel = new JPanel();
		wrapperPanel.setLayout(layout);
		
		this.trainingFileChooser = new JFileChooser();
		this.trainingFileChooser.setMultiSelectionEnabled(true);
		this.trainingFileChooser.setFileFilter(new FileNameExtensionFilter("gesture instance files (*." + FileInfoPanel.GESTURE_INSTANCE_FILE_EXT + ")",
				FileInfoPanel.GESTURE_INSTANCE_FILE_EXT));
		
		this.engineFileSaver = new JFileChooser();
		this.engineFileSaver.setMultiSelectionEnabled(false);
		this.engineFileSaver.setFileFilter(new FileNameExtensionFilter("recognizer engine files (*." + FileInfoPanel.GESTURE_ENGINE_FILE_EXT + ")",
				FileInfoPanel.GESTURE_ENGINE_FILE_EXT));
		
		this.engineFileLoader = new JFileChooser();
		this.engineFileLoader.setMultiSelectionEnabled(false);
		this.engineFileLoader.setFileFilter(new FileNameExtensionFilter("recognizer engine files (*." + FileInfoPanel.GESTURE_ENGINE_FILE_EXT + ")",
				FileInfoPanel.GESTURE_ENGINE_FILE_EXT));
		
		Preferences userPreferences = Preferences.userRoot();
		
		String engineLoadPath = userPreferences.get(ENGINE_LOAD_DIALOG_PATH_KEY, null);
		this.engineFileLoader.setCurrentDirectory(engineLoadPath == null ? null : new File(engineLoadPath));
		
		String engineSavePath = userPreferences.get(ENGINE_SAVE_DIALOG_PATH_KEY, null);
		this.engineFileSaver.setCurrentDirectory(engineSavePath == null ? null : new File(engineSavePath));
		
		String trainingLoadPath = userPreferences.get(TRAINING_LOAD_DIALOG_PATH_KEY, null);
		this.trainingFileChooser.setCurrentDirectory(trainingLoadPath == null ? null : new File(trainingLoadPath));
		
		this.gestureType = formLayoutHelper.constructGestureComboBox();
		String gestureTypeIdxStr = userPreferences.get(SELECTED_GESTURE_TYPE_KEY, "0");
		try {
			int idx = Integer.parseInt(gestureTypeIdxStr);
			this.gestureType.setSelectedIndex(idx);
		}
		catch (NumberFormatException e) {
		}
		this.gestureType.addActionListener(this);
		
		this.selectFileButton = new JButton("Add a Files...");
		this.selectFileButton.addActionListener(this);
		
		this.trainFilesButton = new JButton("Train!");
		this.trainFilesButton.addActionListener(this);
		
		// Allow the user to edit the file list
		this.trainingFileListPanel = new LoggerPanel("Training Files");
		this.loggingPanel = new LoggerPanel("Log");
		this.loggingPanel.setTextAreaSize(5, 65);
		
		JLabel gestureTypeLabel = new JLabel("Select Gesture: ");
		gestureTypeLabel.setForeground(Color.black);

		JPanel selectGestureControlsPanel = new JPanel();
		selectGestureControlsPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		selectGestureControlsPanel.add(gestureTypeLabel);
		selectGestureControlsPanel.add(this.gestureType);
		selectGestureControlsPanel.add(this.selectFileButton);
		selectGestureControlsPanel.add(this.trainFilesButton);
		formLayoutHelper.addLastField(selectGestureControlsPanel, wrapperPanel);
		
		JLabel gestureEngineLabel = new JLabel("Gesture Recognition Engine: ");
		gestureEngineLabel.setForeground(Color.black);
		this.loadGestureEngineButton = new JButton("Load Engine...");
		this.loadGestureEngineButton.addActionListener(this);
		this.saveGestureEngineButton = new JButton("Save Engine");
		this.saveGestureEngineButton.addActionListener(this);
		this.clearGestureEngineButton = new JButton("Clear Engine");
		this.clearGestureEngineButton.addActionListener(this);
		
		JPanel engineLoadSavePanel = new JPanel();
		engineLoadSavePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		engineLoadSavePanel.add(gestureEngineLabel);
		engineLoadSavePanel.add(this.loadGestureEngineButton);
		engineLoadSavePanel.add(this.saveGestureEngineButton);
		engineLoadSavePanel.add(this.clearGestureEngineButton);
		formLayoutHelper.addLastField(engineLoadSavePanel, wrapperPanel);
		
		formLayoutHelper.addLastField(wrapperPanel, this);
		formLayoutHelper.addLastField(this.trainingFileListPanel, this);
		formLayoutHelper.addLastField(this.loggingPanel, this);
		
		this.trainFilesButton.setEnabled(false);
		this.saveGestureEngineButton.setEnabled(false);
	}
	
	// Handles button events
	public void actionPerformed(ActionEvent e)
	{
		// Add the file to the list of files to convert
		if (e.getSource() == this.selectFileButton) {
			this.handleTrainingFileChooserDialog();
		}
		else if (e.getSource() == this.trainFilesButton) {
			this.trainGestureRecognitionEngineFromFileList(this.trainingFileListPanel.getLogText());
			//this.exportGestureRecognizerEngine(gestureRecognizer);
		}
		else if (e.getSource() == this.loadGestureEngineButton) {
			this.handleEngineLoadDialog();
		}
		else if (e.getSource() == this.saveGestureEngineButton) {
			this.handleEngineSaveDialog();
		}
		else if (e.getSource() == this.clearGestureEngineButton) {
			int result = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to clear the currently loaded engine?", "Clear Engine",
                    JOptionPane.YES_NO_CANCEL_OPTION);
			if (result != JOptionPane.YES_OPTION) {
				return;
			}
			
			this.gestureRecognizer.clearEngine();
		}
		else if (e.getSource() == this.gestureType) {
			Preferences userPreferences = Preferences.userRoot();
			userPreferences.put(SELECTED_GESTURE_TYPE_KEY, String.valueOf(this.gestureType.getSelectedIndex()));
		}
	}
	
	// Displays the file chooser and adds the file to the training list if applicable
	private void handleTrainingFileChooserDialog()
	{
		int fileStatus = this.trainingFileChooser.showOpenDialog(this);
		
		if (fileStatus == JFileChooser.APPROVE_OPTION) {
            Preferences userPreferences = Preferences.userRoot();
            userPreferences.put(TRAINING_LOAD_DIALOG_PATH_KEY, this.trainingFileChooser.getCurrentDirectory().getAbsolutePath());
            
            File[] selectedFiles = this.trainingFileChooser.getSelectedFiles();
            
            for(File file : selectedFiles)
            {
            	this.trainingFileListPanel.appendLogTextLine(file.getAbsolutePath());
            }

        }
		
		String[] lines = this.trainingFileListPanel.getLogText().split("\r\n|\r|\n");
		if (lines.length == 0) {
			this.trainFilesButton.setEnabled(false);
		}
		else {
			this.trainFilesButton.setEnabled(true);
		}
		
	}
	
	private void handleEngineLoadDialog() {
		int status = this.engineFileLoader.showOpenDialog(this);
		if (status == JFileChooser.APPROVE_OPTION) {
            Preferences userPreferences = Preferences.userRoot();
            userPreferences.put(ENGINE_LOAD_DIALOG_PATH_KEY, this.engineFileLoader.getCurrentDirectory().getAbsolutePath());
			
			File selectedFile = this.engineFileLoader.getSelectedFile();
			if (selectedFile == null) {
				return;
			}
			
			boolean success = false;
			try {
				success = this.gestureRecognizer.loadRecognizerEngine(new FileReader(selectedFile));
			}
			catch (FileNotFoundException e) {
				e.printStackTrace();
				this.loggingPanel.appendLogTextLine("Failed to load gesture recognizer engine file, could not find file: " + selectedFile.getAbsolutePath());
				return;
			}
			
			if (success) {
				this.loggingPanel.appendLogTextLine("Gesture recognizer engine loaded successfully!");
				this.saveGestureEngineButton.setEnabled(false);
			}
			else {
				this.loggingPanel.appendLogTextLine("Failed to load gesture recognizer engine file, bad file format: " + selectedFile.getAbsolutePath());
			}
			
		}
	}
	
	private void handleEngineSaveDialog() {
		int status = this.engineFileSaver.showSaveDialog(this);
		if (status == JFileChooser.APPROVE_OPTION) {
			
            Preferences userPreferences = Preferences.userRoot();
            userPreferences.put(ENGINE_SAVE_DIALOG_PATH_KEY, this.engineFileSaver.getCurrentDirectory().getAbsolutePath());
            
			File selectedFile = this.engineFileSaver.getSelectedFile();
			if (selectedFile == null) {
				return;
			}
			
			String filePath = selectedFile.getAbsolutePath();
			filePath = filePath.endsWith("." + FileInfoPanel.GESTURE_ENGINE_FILE_EXT) ? filePath : filePath + "." + FileInfoPanel.GESTURE_ENGINE_FILE_EXT;
			selectedFile = new File(filePath);	
			
			if (selectedFile.exists()) {
				int result = JOptionPane.showConfirmDialog(this,
	                    "The file exists, overwrite?", "Existing file",
	                    JOptionPane.YES_NO_CANCEL_OPTION);
				if (result != JOptionPane.YES_OPTION) {
					return;
				}
			}
			
			try {
				FileWriter writer = new FileWriter(selectedFile);
				boolean success = this.gestureRecognizer.saveRecognizerEngine(writer);
				if (success) {
					this.loggingPanel.appendLogTextLine("Gesture recognizer engine saved successfully!");
				}
				else {
					this.loggingPanel.appendLogTextLine("Failed to save gesture recognizer engine file: " + selectedFile.getAbsolutePath());
				}
				writer.close();
			}
			catch (IOException e) {
				e.printStackTrace();
				this.loggingPanel.appendLogTextLine("Failed to save gesture recognizer engine file, could not write file: " + selectedFile.getAbsolutePath());
				return;
			}
			
		}
	}
	
	// Trains the gesture recognizer using the data set from the selected files
	public void trainGestureRecognitionEngineFromFileList(String files) 
	{
		// Get a list of files from the log
		GestureDataSet gestureDataSet = new GestureDataSet();
		Scanner fileList = new Scanner(files);
		
		// Iterate through each file and create a gesture instance
		while(fileList.hasNextLine())
		{
			GestureInstance instance = new GestureInstance();
			StringBuilder gestureInstanceString = new StringBuilder();

			try {
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
					this.loggingPanel.appendLogTextLine("Failed to load gesture instance from file, bad file format: " + filePath);
				}
			} 
			catch (FileNotFoundException e) {
				e.printStackTrace();
			}	
		}
		
		boolean success = this.gestureRecognizer.trainGesture((GestureType)this.gestureType.getSelectedItem(), gestureDataSet);	
		if (success) {
			this.loggingPanel.appendLogTextLine("Training succeeded! (yay!)");
			this.saveGestureEngineButton.setEnabled(true);
		}
		else {
			this.loggingPanel.appendLogTextLine("The gesture instances selected cannot be trained!\n");
		}
	}
	
}
