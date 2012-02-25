package ca.site3.ssf.gesturerecordergui;

import java.awt.Color;
import java.awt.GridBagLayout;

import java.io.BufferedWriter;
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
		this.fileName = new JTextField(25);
		
		JLabel gestureNameLabel = new JLabel("Gesture Name:");
		gestureNameLabel.setForeground(Color.black);
		formLayoutHelper.addLabel(gestureNameLabel, this);
		formLayoutHelper.addLastField(this.gestureName, this);
		
		JLabel fileNameLabel = new JLabel("File Name:");
		fileNameLabel.setForeground(Color.black);
		formLayoutHelper.addLabel(fileNameLabel, this);
		formLayoutHelper.addLastField(this.fileName, this);
	}
	
	public void recordFileInformation(String gyroscope, String magnetometer, String accelerometer, String gestureName, String fileName){
		try
		{
			DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	        Date date = new Date();

	        FileWriter writer = new FileWriter(fileName + ".txt", true);
		    writer.write(gestureName + ',' + gyroscope + ',' + magnetometer + ',' + accelerometer + ',' + dateFormat.format(date));
	 
		    writer.flush();
		    writer.close();
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
		}
	}
}
