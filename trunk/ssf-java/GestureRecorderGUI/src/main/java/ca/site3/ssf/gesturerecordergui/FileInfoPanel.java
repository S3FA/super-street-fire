package ca.site3.ssf.gesturerecordergui;

import java.awt.BorderLayout;
import java.awt.Checkbox;
import java.awt.Color;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

// A container panel for the file and gesture info to save
class FileInfoPanel extends JPanel {
	
	private static final long serialVersionUID = 1L;
	
	public boolean isNewFile = false;
	private int currentIteration = 0;
	
	public JTextField gestureName;
	public Checkbox exportRecognizer;
	public Checkbox exportCsv;
	
	FileInfoPanel() {
		super();
		
		Color borderColour = Color.black;
		
		TitledBorder border = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(borderColour), "File Info");
		border.setTitleColor(Color.black);
		this.setBorder(border);
		
        GridBagLayout layout = new GridBagLayout();
		this.setLayout(layout);

		FormLayoutHelper formLayoutHelper = new FormLayoutHelper();
		
		this.gestureName = new JTextField(25);
		this.gestureName.setText("Unspecified");
		
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
		
		JLabel gestureNameLabel = new JLabel("Gesture Name:");
		gestureNameLabel.setForeground(Color.black);
		formLayoutHelper.addMiddleField(gestureNameLabel, this);
		formLayoutHelper.addLastField(this.gestureName, this);
	}
	
	// Save the data to a file. Using CSV currently, but if the hardware sends us comma-separated tuples, may need to use pipe-delimiting or something else
	public void recordFileInformation(String gyroDataLeft, String magDataLeft, String accDataLeft, String gyroDataRight, String magDataRight, String accDataRight, String gestureName, String time){
		try
		{		
	        // If the file exists, check if the next iteration of the file exists until we can make a new one
	        while (isNewFile && new File("Data/" + gestureName + Integer.toString(currentIteration) + ".csv").exists())
	        {
	        	currentIteration++;
	        }
	       
	        FileWriter writer = new FileWriter(new File("Data/" + gestureName + Integer.toString(currentIteration) + ".csv"), !isNewFile);
        	
	        // If we just created the file, 
	        if(isNewFile)
	        {
	        	writer.write("GyroDataLeft,MagDataLeft,AccDataLeft,GyroDataRight,MagDataRight,AccDataRight,DateTime");
	        	writer.append("\n");
	        }
	        
		    writer.append(gyroDataLeft);
		    writer.append(",");
		    writer.append(magDataLeft);
		    writer.append(",");
		    writer.append(accDataLeft);
		    writer.append(",");
		    writer.append(gyroDataRight);
		    writer.append(",");
		    writer.append(magDataRight);
		    writer.append(",");
		    writer.append(accDataRight);
		    writer.append(",");
		    writer.append(time);
		    writer.append("\n");
	 
		    writer.flush();
		    writer.close();
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
		}
	}
}
