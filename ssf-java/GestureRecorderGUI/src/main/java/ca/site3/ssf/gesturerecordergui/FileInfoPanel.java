package ca.site3.ssf.gesturerecordergui;

import java.awt.Color;
import java.awt.GridBagLayout;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.BorderFactory;
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
	
	public JTextField gestureName;
	public JTextField fileName;
	
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
		
		this.fileName = new JTextField(25);
		this.fileName.setText("RecorderData");
		
		JLabel gestureNameLabel = new JLabel("Gesture Name:");
		gestureNameLabel.setForeground(Color.black);
		formLayoutHelper.addLabel(gestureNameLabel, this);
		formLayoutHelper.addLastField(this.gestureName, this);
		
		JLabel fileNameLabel = new JLabel("File Name:");
		fileNameLabel.setForeground(Color.black);
		formLayoutHelper.addLabel(fileNameLabel, this);
		formLayoutHelper.addLastField(this.fileName, this);
	}
	
	// Save the data to a file. Using CSV currently, but if the hardware sends us comma-separated tuples, may need to use pipe-delimiting or something else
	public void recordFileInformation(String gyroDataLeft, String magDataLeft, String accDataLeft, String gyroDataRight, String magDataRight, String accDataRight, String gestureName, String fileName){
		try
		{		
			DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");
	        Date date = new Date();

	        FileWriter writer = new FileWriter(fileName + ".csv", true);
	        
	        // If creating a new file then put the column names at the top of the file
	        if(!new File(fileName).exists())
	        {
	        	writer.write("GestureName,GyroDataLeft,MagDataLeft,AccDataLeft,GyroDataRight,MagDataRight,AccDataRight,DateTime\n");
	        }
	        
		    writer.append(gestureName);
		    writer.append(",");
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
		    writer.append(dateFormat.format(date));
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
