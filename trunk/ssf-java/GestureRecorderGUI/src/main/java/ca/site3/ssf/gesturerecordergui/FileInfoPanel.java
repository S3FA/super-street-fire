package ca.site3.ssf.gesturerecordergui;

import java.awt.Checkbox;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import ca.site3.ssf.gesturerecognizer.GestureInstance;
import ca.site3.ssf.gesturerecognizer.GestureRecognizer;
import ca.site3.ssf.gesturerecognizer.GestureType;
import ca.site3.ssf.gesturerecognizer.GloveData;

/**
 * A class to manage the loading and saving of files
 * @author Mike
 *
 */
class FileInfoPanel extends JPanel implements ActionListener, ItemListener {
	
	public static final String GESTURE_INSTANCE_FILE_EXT = "ins";
	public static final String GESTURE_ENGINE_FILE_EXT   = "eng";
	
	private static final long serialVersionUID = 1L;
	
	private static final String EXPORT_TO_RECOGNIZER_KEY  = "ExportToRecognizer";
	private static final String EXPORT_TO_CSV_KEY         = "ExportToCsv";
	private static final String SELECTED_GESTURE_TYPE_KEY = "SelectedGestureType";
	private static final String EXPORT_DIR_KEY            = "ExportDir";
	
	private boolean isNewFile = false;
	private JComboBox gestureName;
	private Checkbox exportRecognizer;
	private Checkbox exportCsv;
	
	private JTextField saveDirTextBox;
	private JButton dirButton;
	private JFileChooser dirChooser;
	
	FileInfoPanel() {
		super();
		
		Color borderColour = Color.black;
		
		TitledBorder border = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(borderColour), "Gesture Recording Info");
		border.setTitleColor(Color.black);
		this.setBorder(border);
		
        GridBagLayout layout = new GridBagLayout();
		this.setLayout(layout);

		FormLayoutHelper formLayoutHelper = new FormLayoutHelper();
		
		this.gestureName = formLayoutHelper.constructGestureComboBox();		
		this.exportRecognizer = new Checkbox();
		this.exportCsv = new Checkbox();
		
		JLabel exportToRecognizerLabel = new JLabel("Export to Gesture Recognizer");
		exportToRecognizerLabel.setForeground(Color.black);
		formLayoutHelper.addMiddleField(this.exportRecognizer, this);
		formLayoutHelper.addLastField(exportToRecognizerLabel, this);
		
		JLabel exportToCsvLabel = new JLabel("Export to CSV");
		exportToCsvLabel.setForeground(Color.black);
		formLayoutHelper.addMiddleField(this.exportCsv, this);
		formLayoutHelper.addLastField(exportToCsvLabel, this);
		
		JLabel gestureNameLabel = new JLabel("Gesture:");
		gestureNameLabel.setForeground(Color.black);
		formLayoutHelper.addMiddleField(gestureNameLabel, this);
		formLayoutHelper.addMiddleField(this.gestureName, this);
		formLayoutHelper.addLastField(new JLabel(""), this);
		
		JLabel dirLabel = new JLabel("Save Directory:");
		dirLabel.setForeground(Color.black);
		this.saveDirTextBox = new JTextField(50);
		this.saveDirTextBox.setEditable(false);
		this.dirButton = new JButton("...");
		this.dirButton.addActionListener(this);
		
		formLayoutHelper.addMiddleField(dirLabel, this);
		JPanel dirPanel = new JPanel();
		dirPanel.setLayout(new BoxLayout(dirPanel, BoxLayout.X_AXIS));
		dirPanel.add(this.saveDirTextBox);
		dirPanel.add(this.dirButton);
		formLayoutHelper.addLastField(dirPanel, this);
		
		Preferences userPreferences = Preferences.userRoot();
		
		String exportToRecognizerStr = userPreferences.get(EXPORT_TO_RECOGNIZER_KEY, null);
		this.exportRecognizer.setState(exportToRecognizerStr == null ? true : Boolean.valueOf(exportToRecognizerStr));
		this.exportRecognizer.addItemListener(this);
		
		String exportToCsvStr = userPreferences.get(EXPORT_TO_CSV_KEY, null);
		this.exportCsv.setState(exportToCsvStr == null ? false : Boolean.valueOf(exportToCsvStr));
		this.exportCsv.addItemListener(this);
		
		String gestureTypeIdxStr = userPreferences.get(SELECTED_GESTURE_TYPE_KEY, "0");
		try {
			int idx = Integer.parseInt(gestureTypeIdxStr);
			this.gestureName.setSelectedIndex(idx);
		}
		catch (NumberFormatException e) {
		}
		this.gestureName.addActionListener(this);
		
		this.dirChooser = new JFileChooser();
		this.dirChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		
		String exportDirStr = userPreferences.get(EXPORT_DIR_KEY, null);
		File selectedDir = exportDirStr == null ? null : new File(exportDirStr);
		this.dirChooser.setCurrentDirectory(selectedDir);
		if (selectedDir != null) {
			this.saveDirTextBox.setText(selectedDir.getAbsolutePath());
		}
	}
	
	public GestureType getSelectedGesture() {
		return (GestureType)(this.gestureName.getSelectedItem());
	}
	
	// Save the data to a file. Using CSV currently, but if the hardware sends us comma-separated tuples, may need to use pipe-delimiting or something else
	public String exportToCsv(GestureInstance instance) {
		String filename = "";
		try {	
			String suffix = "csv";
			int iteration = getNextFileIteration(suffix);
			
			filename = "Data/" + gestureName.getSelectedItem().toString() + Integer.toString(iteration) + "." + suffix;
			File csvFile = new File(filename);
			filename = csvFile.toString();
	        FileWriter writer = new FileWriter(csvFile, !this.isNewFile);
        	
	        // If we just created the file, 
	        if (this.isNewFile) {
	        	writer.write("GyroLeftX,GyroLeftY,GyroLeftZ,MagLeftX,MagLeftY,MagLeftZ,AccLeftX,AccLeftY,AccLeftZ,GyroRightX,GyroRightY,GyroRightZ,MagRightX,MagRightY,MagRightZ,AccRightX,AccRightY,AccRightZ,Time");
	        	writer.append("\n");
	        }
	        
	        // Save the data to a CSV file
	        for (int i = 0; i < instance.getNumDataPts(); i++)
	        {
	        	GloveData left = instance.getLeftGloveDataAt(i);
	        	GloveData right = instance.getRightGloveDataAt(i);
	        	double time = instance.getTimeAt(i);
	        	
			    writer.append(Double.toString(left.getGyroData().getX()));
			    writer.append(", ");
			    writer.append(Double.toString(left.getGyroData().getY()));
			    writer.append(", ");
			    writer.append(Double.toString(left.getGyroData().getZ()));
			    writer.append(", ");
			    writer.append(Double.toString(left.getMagnetoData().getX()));
			    writer.append(", ");
			    writer.append(Double.toString(left.getMagnetoData().getY()));
			    writer.append(", ");
			    writer.append(Double.toString(left.getMagnetoData().getZ()));
			    writer.append(", ");
			    writer.append(Double.toString(left.getAccelData().getX()));
			    writer.append(", ");
			    writer.append(Double.toString(left.getAccelData().getY()));
			    writer.append(", ");
			    writer.append(Double.toString(left.getAccelData().getZ()));
			    writer.append(", ");
			    writer.append(Double.toString(right.getGyroData().getX()));
			    writer.append(", ");
			    writer.append(Double.toString(right.getGyroData().getY()));
			    writer.append(", ");
			    writer.append(Double.toString(right.getGyroData().getZ()));
			    writer.append(", ");
			    writer.append(Double.toString(right.getMagnetoData().getX()));
			    writer.append(", ");
			    writer.append(Double.toString(right.getMagnetoData().getY()));
			    writer.append(", ");
			    writer.append(Double.toString(right.getMagnetoData().getZ()));
			    writer.append(", ");
			    writer.append(Double.toString(right.getAccelData().getX()));
			    writer.append(", ");
			    writer.append(Double.toString(right.getAccelData().getY()));
			    writer.append(", ");
			    writer.append(Double.toString(right.getAccelData().getZ()));
			    writer.append(", ");
			    writer.append(Double.toString(time));
			    writer.append("\n");
	        }
	 
		    writer.flush();
		    writer.close();
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
			filename = "";
		}
		
		return filename;
	}
	
	// Save the data to a file that can be read by GestureRecognizer's fromDataString() method
	public String exportToRecognizer(GestureInstance instance) {
		String filename = "";
		try {	
			int iteration = getNextFileIteration(GESTURE_INSTANCE_FILE_EXT);
	       
	        // Save the data to a file readable by the GestureRecognizer
			
			filename = this.saveDirTextBox.getText() +
	        		"/" + gestureName.getSelectedItem().toString() + Integer.toString(iteration) + 
	        		"." + GESTURE_INSTANCE_FILE_EXT;
			File recognizerFile = new File(filename);
			filename = recognizerFile.toString();
			
	        FileWriter writer = new FileWriter(recognizerFile, false);
	        writer.write(instance.toDataString());
	 
		    writer.flush();
		    writer.close();
		}
		catch (IOException ex) {
			ex.printStackTrace();
			filename = "";
		}
		return filename;
	}
	
	// Save a recognizer engine file
	public void exportToRecognizerEngine(GestureRecognizer gestureRecognizer) {
		try {	
			int iteration = getNextFileIteration(GESTURE_ENGINE_FILE_EXT);
	       	        
	        // Save the data to a file readable by the Gesture Tester
	        FileWriter writer = new FileWriter(new File(this.saveDirTextBox.getText() + 
	        		"/" + gestureName.getSelectedItem().toString() + Integer.toString(iteration) +
	        		"." + GESTURE_ENGINE_FILE_EXT), false);
	        gestureRecognizer.saveRecognizerEngine(writer);
	 
		    writer.flush();
		    writer.close();
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
		}
	}
	
	// Sets the new file status
	public void setNewFile(boolean isNewFile){
		this.isNewFile = isNewFile;
	}
	
	// Gets the next valid version # of a file 
	public int getNextFileIteration(String suffix) {
		int iteration = 0;
		
        // If the file exists, check if the next iteration of the file exists until we can make a new one
        while (new File(this.saveDirTextBox.getText() + 
        		"/" + gestureName.getSelectedItem().toString() + Integer.toString(iteration) + "." + suffix).exists()) {
        	iteration++;
        }
        
        return iteration;
	}
	
	// Gets the state of the csv export checkbox
	public boolean getCsvExportState()
	{
		return this.exportCsv.getState();
	}
	
	// Gets the state of the recognizer export checkbox
	public boolean getRecognizerExportState()
	{
		return this.exportRecognizer.getState();
	}
	
	// Retrieves the selected gesture name
	public String getGestureName()
	{
		return this.gestureName.getSelectedItem().toString();
	}

	public void actionPerformed(ActionEvent event) {
		if (event.getSource() == this.gestureName) {
			Preferences userPreferences = Preferences.userRoot();
			userPreferences.put(SELECTED_GESTURE_TYPE_KEY, String.valueOf(this.gestureName.getSelectedIndex()));
		}
		else if (event.getSource() == this.dirButton) {
			int dlgResult = this.dirChooser.showOpenDialog(this);
			if (dlgResult == JFileChooser.APPROVE_OPTION && this.dirChooser.getSelectedFile() != null) {
	            Preferences userPreferences = Preferences.userRoot();
	            String selectedDir = this.dirChooser.getSelectedFile().getAbsolutePath();
	            userPreferences.put(EXPORT_DIR_KEY, selectedDir);
	            this.saveDirTextBox.setText(selectedDir);
			}
		}
	}

	public void itemStateChanged(ItemEvent event) {
		Preferences userPreferences = Preferences.userRoot();
		if (event.getSource() == this.exportRecognizer) {
			userPreferences.put(EXPORT_TO_RECOGNIZER_KEY, String.valueOf(this.exportRecognizer.getState()));
		}
		else if (event.getSource() == this.exportCsv) {
			userPreferences.put(EXPORT_TO_CSV_KEY, String.valueOf(this.exportCsv.getState()));
		}
		
	}
}
