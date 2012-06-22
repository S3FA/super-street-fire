package ca.site3.ssf.gesturerecordergui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
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
	
	private static final String ENGINE_LOAD_DIALOG_PATH_KEY   = "EngineTrainingLoadDlgPath";
	private static final String ENGINE_SAVE_DIALOG_PATH_KEY   = "EngineTrainingSaveDlgPath";
	private static final String TRAINING_LOAD_DIALOG_PATH_KEY = "TrainingLoadDlgPath";
	private static final String SELECTED_GESTURE_TYPE_KEY     = "SelectedGestureType";
	
	private JButton selectDirButton;
	private JButton trainFilesButton;
	private JButton untrainGestureButton;
	@SuppressWarnings("rawtypes")

	private DefaultListModel gestureListModel = new DefaultListModel();
	private DefaultListModel toTrainListModel = new DefaultListModel();
	private JList gestureList = new JList(gestureListModel);
	private JList toTrainList = new JList(toTrainListModel);
	private JButton toTrainListButton;
	private JButton fromTrainListButton;
	
	private JTextField searchDirText;
	private File selectedBaseGestureDir;
	
	private JButton loadGestureEngineButton;
	private JButton saveGestureEngineButton;
	private JButton clearGestureEngineButton;
	private JLabel loadedEngineLabel;
	
	private LoggerPanel loggingPanel;
	
	private JFileChooser trainingDirChooser;
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
		
		FormLayoutHelper formLayoutHelper = new FormLayoutHelper();

		GridBagLayout layout = new GridBagLayout();
		JPanel wrapperPanel = new JPanel();
		wrapperPanel.setLayout(layout);
		
		this.trainingDirChooser = new JFileChooser();
		this.trainingDirChooser.setMultiSelectionEnabled(false);
		this.trainingDirChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		
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
		this.selectedBaseGestureDir = trainingLoadPath == null ? null : new File(trainingLoadPath);
		this.trainingDirChooser.setCurrentDirectory(this.selectedBaseGestureDir);
		
		this.selectDirButton = new JButton("Select");
		this.selectDirButton.addActionListener(this);
		
		this.trainFilesButton = new JButton("Train!");
		this.trainFilesButton.addActionListener(this);
		
		this.untrainGestureButton = new JButton("Untrain");
		this.untrainGestureButton.addActionListener(this);
		
		// Allow the user to edit the file list
		this.loggingPanel = new LoggerPanel("Log");
		this.loggingPanel.setTextAreaSize(5, 100);
		
		
		this.gestureList.setVisibleRowCount(7);
		this.gestureList.setLayoutOrientation(JList.VERTICAL);
		this.gestureList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		for (GestureType gesture : GestureType.values()) {
			this.gestureListModel.addElement(gesture);
		}
		
		this.toTrainList.setVisibleRowCount(7);
		this.toTrainList.setLayoutOrientation(JList.VERTICAL);
		this.toTrainList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		
		JPanel allGesturesListPanel = new JPanel();
		{
			allGesturesListPanel.setLayout(new BoxLayout(allGesturesListPanel, BoxLayout.Y_AXIS));
			JLabel gestureListLabel = new JLabel("Gestures List");
			gestureListLabel.setForeground(Color.black);
			JPanel tempLblPanel = new JPanel();
			tempLblPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
			tempLblPanel.add(gestureListLabel);
			allGesturesListPanel.add(tempLblPanel);
			allGesturesListPanel.add(new JScrollPane(this.gestureList));
		}
		
		this.toTrainListButton = new JButton(">");
		this.toTrainListButton.addActionListener(this);
		this.fromTrainListButton = new JButton("<");
		this.fromTrainListButton.addActionListener(this);
		
		JPanel toFromButtonsPanel = new JPanel();
		toFromButtonsPanel.setLayout(new BoxLayout(toFromButtonsPanel, BoxLayout.PAGE_AXIS));
		toFromButtonsPanel.add(Box.createVerticalGlue());
		toFromButtonsPanel.add(this.toTrainListButton, Component.CENTER_ALIGNMENT);
		
		toFromButtonsPanel.add(this.fromTrainListButton, Component.CENTER_ALIGNMENT);
		toFromButtonsPanel.add(Box.createVerticalGlue());
		
		JPanel trainGesturesListPanel = new JPanel();
		{
			trainGesturesListPanel.setLayout(new BoxLayout(trainGesturesListPanel, BoxLayout.Y_AXIS));
			JLabel gestureListLabel = new JLabel("Training List");
			gestureListLabel.setForeground(Color.black);
			JPanel tempLblPanel = new JPanel();
			tempLblPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
			tempLblPanel.add(gestureListLabel);
			trainGesturesListPanel.add(tempLblPanel);
			trainGesturesListPanel.add(new JScrollPane(this.toTrainList));
		}
		
		
		JPanel trainingListsPanel = new JPanel();
		trainingListsPanel.setLayout(new BoxLayout(trainingListsPanel, BoxLayout.X_AXIS));
		trainingListsPanel.add(allGesturesListPanel);
		trainingListsPanel.add(toFromButtonsPanel);
		trainingListsPanel.add(trainGesturesListPanel);
		formLayoutHelper.addLastField(trainingListsPanel, wrapperPanel);
		
		searchDirText = new JTextField(50);
		searchDirText.setText(trainingLoadPath);
		
		JLabel searchDirLabel = new JLabel("Gesture Search Directory:");
		searchDirLabel.setForeground(Color.black);
		
		JPanel searchDirPanel = new JPanel();
		searchDirPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		searchDirPanel.add(searchDirLabel);
		searchDirPanel.add(searchDirText);
		searchDirPanel.add(this.selectDirButton);
		
		JPanel gestureButtonPanel = new JPanel();
		gestureButtonPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		gestureButtonPanel.add(this.trainFilesButton);
		gestureButtonPanel.add(this.untrainGestureButton);
		
		JPanel gestureControlsPanel = new JPanel();
		gestureControlsPanel.setLayout(new GridLayout(2, 1));
		gestureControlsPanel.add(searchDirPanel);
		gestureControlsPanel.add(gestureButtonPanel);
		formLayoutHelper.addLastField(gestureControlsPanel, wrapperPanel);
		
		
		JLabel gestureEngineLabel = new JLabel("Gesture Recognition Engine: ");
		gestureEngineLabel.setForeground(Color.black);
		this.loadGestureEngineButton = new JButton("Load Engine...");
		this.loadGestureEngineButton.addActionListener(this);
		this.saveGestureEngineButton = new JButton("Save Engine");
		this.saveGestureEngineButton.addActionListener(this);
		this.clearGestureEngineButton = new JButton("Clear Engine");
		this.clearGestureEngineButton.addActionListener(this);
		this.loadedEngineLabel = new JLabel("");
		this.loadedEngineLabel.setForeground(Color.black);
		
		JPanel engineLoadSavePanel = new JPanel();
		engineLoadSavePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		engineLoadSavePanel.add(gestureEngineLabel);
		engineLoadSavePanel.add(this.loadGestureEngineButton);
		engineLoadSavePanel.add(this.saveGestureEngineButton);
		engineLoadSavePanel.add(this.clearGestureEngineButton);
		formLayoutHelper.addLastField(engineLoadSavePanel, wrapperPanel);
		
		JPanel loadedEnginePanel = new JPanel();
		loadedEnginePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		JLabel loadedEngineTempLbl = new JLabel("Loaded Engine:");
		loadedEngineTempLbl.setForeground(Color.black);
		loadedEnginePanel.add(loadedEngineTempLbl);
		loadedEnginePanel.add(this.loadedEngineLabel);
		formLayoutHelper.addLastField(loadedEnginePanel, wrapperPanel);

		JPanel centerPanel = new JPanel();
		centerPanel.setLayout(new BorderLayout());
		centerPanel.add(this.loggingPanel, BorderLayout.CENTER);
		
		this.setLayout(new BorderLayout());
		this.add(wrapperPanel, BorderLayout.NORTH);
		this.add(centerPanel, BorderLayout.CENTER);
		
		this.trainFilesButton.setEnabled(false);
		this.untrainGestureButton.setEnabled(false);
		this.saveGestureEngineButton.setEnabled(true);
	}
	
	// Handles button events
	public void actionPerformed(ActionEvent e)
	{
		// Add the file to the list of files to convert
		if (e.getSource() == this.selectDirButton) {
			this.handleTrainingDirChooserDialog();
		}
		else if (e.getSource() == this.trainFilesButton) {
			
			// Get a list of the gestures that need to be trained...
			for (int i = 0; i < this.toTrainListModel.size(); i++) {
				
				GestureType gesture = (GestureType)this.toTrainListModel.get(i);
				assert(gesture != null);
				List<File> gestureFiles = this.findTrainingFilesInGestureDir(gesture);
				if (gestureFiles.isEmpty()) {
					this.loggingPanel.appendLogTextLine("No gesture files found for " + gesture.toString());
					continue;
				}
				
				this.trainGestureRecognitionEngineFromFileList(gesture, gestureFiles);
			}

		}
		else if (e.getSource() == this.untrainGestureButton) {

			for (int i = 0; i < this.toTrainListModel.size(); i++) {
				
				GestureType gesture = (GestureType)this.toTrainListModel.get(i);
				this.gestureRecognizer.untrainAndClearGesture(gesture);
				this.loggingPanel.appendLogTextLine("Gesture " + gesture.toString() + " is now cleared / untrained.");
			}
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
		else if (e.getSource() == this.toTrainListButton) {
			Object[] selectedGestures = this.gestureList.getSelectedValues();
			for (Object obj : selectedGestures) {
				GestureType gesture = (GestureType)obj;
				gestureListModel.removeElement(gesture);
				toTrainListModel.addElement(gesture);
			}
			
			if (this.toTrainListModel.isEmpty()) {
				this.trainFilesButton.setEnabled(false);
				this.untrainGestureButton.setEnabled(false);
			}
			else {
				this.trainFilesButton.setEnabled(true);
				this.untrainGestureButton.setEnabled(true);
			}
		}
		else if (e.getSource() == this.fromTrainListButton) {
			Object[] selectedGestures = this.toTrainList.getSelectedValues();
			for (Object obj : selectedGestures) {
				GestureType gesture = (GestureType)obj;
				toTrainListModel.removeElement(gesture);
				gestureListModel.addElement(gesture);
			}
			
			if (this.toTrainListModel.isEmpty()) {
				this.trainFilesButton.setEnabled(false);
				this.untrainGestureButton.setEnabled(false);
			}
			else {
				this.trainFilesButton.setEnabled(true);
				this.untrainGestureButton.setEnabled(true);
			}
		}

	}
	
	private List<File> findTrainingFilesInGestureDir(GestureType gesture) {
		
		if (gesture == null || !this.selectedBaseGestureDir.canRead()) {
			return new ArrayList<File>();
		}
		
		List<File> result = new ArrayList<File>();
		File[] children = this.selectedBaseGestureDir.listFiles();
		for (File child : children) {
			
			// We're only looking for subdirectories with the expected parent directory name for the given gesture 
			if (!child.isDirectory()) {
				continue;
			}
			
			List<String> allowedParentDirNames = gesture.getParentDirNameList();
			for (String allowedName : allowedParentDirNames) {
				
				if (child.getName().compareTo(allowedName) == 0) {
					
					// Grab all of the files in the directory and add them to the result...
					File[] subdirChildren = child.listFiles();
					for (File subdirChild : subdirChildren) {
						if (!subdirChild.isFile()) {
							continue;
						}
						
						if (subdirChild.getName().contains(gesture.toString())) {
							result.add(subdirChild);
						}
					}
				}
				
			}
		}
		
		return result;
	}
	
	// Displays the directory chooser for the base directory for looking for gestures to train
	private void handleTrainingDirChooserDialog() {
		int fileStatus = this.trainingDirChooser.showOpenDialog(this);
		
		if (fileStatus == JFileChooser.APPROVE_OPTION) {
            this.selectedBaseGestureDir = this.trainingDirChooser.getSelectedFile();
            this.searchDirText.setText(this.selectedBaseGestureDir.getAbsolutePath());
            
            Preferences userPreferences = Preferences.userRoot();
            userPreferences.put(TRAINING_LOAD_DIALOG_PATH_KEY, this.searchDirText.getText());
        }

		if (this.selectedBaseGestureDir.isDirectory()) {
			if (this.toTrainListModel.isEmpty()) {
				this.trainFilesButton.setEnabled(false);
				this.untrainGestureButton.setEnabled(false);
			}
			else {
				this.trainFilesButton.setEnabled(true);
				this.untrainGestureButton.setEnabled(true);
			}
		}
		else {
			this.trainFilesButton.setEnabled(false);
			this.untrainGestureButton.setEnabled(false);
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
				this.untrainGestureButton.setEnabled(true);
				this.loadedEngineLabel.setText(selectedFile.getAbsolutePath());
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
					this.loadedEngineLabel.setText(selectedFile.getAbsolutePath());
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
	public void trainGestureRecognitionEngineFromFileList(GestureType gestureToTrain, List<File> files) {
		
		
		// Get a list of files from the log
		GestureDataSet gestureDataSet = new GestureDataSet();

		// Iterate through each file and create a gesture instance
		for (File file : files) {
			
			GestureInstance instance = new GestureInstance();
			StringBuilder gestureInstanceString = new StringBuilder();

			try {
				
				// Read the file text of each file and reconstruct the data string
				Scanner fileTextScanner = new Scanner(new FileInputStream(file));
				
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
					this.loggingPanel.appendLogTextLine("Failed to load gesture instance from file, bad file format: " + file.getAbsolutePath());
				}
			} 
			catch (FileNotFoundException e) {
				e.printStackTrace();
			}	
		}
		
		boolean success = this.gestureRecognizer.trainGesture(gestureToTrain, gestureDataSet);	
		if (success) {
			this.loggingPanel.appendLogTextLine("Training succeeded for gesture " + gestureToTrain.toString() + "!");
			this.loggingPanel.appendLogTextLine("Files used to train " + gestureToTrain.toString() + ":");
			String gestureFiles = "";
			for (File file : files) {
				gestureFiles += file.getName() + ", ";
			}
			this.loggingPanel.appendLogTextLine(gestureFiles.substring(0, gestureFiles.length() - 2));
			
			this.saveGestureEngineButton.setEnabled(true);
			this.untrainGestureButton.setEnabled(true);
		}
		else {
			this.loggingPanel.appendLogTextLine("The gesture instances selected cannot be trained!\n");
		}
	}
	
}
