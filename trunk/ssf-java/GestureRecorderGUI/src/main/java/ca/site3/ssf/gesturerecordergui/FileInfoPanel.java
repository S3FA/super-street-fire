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

import ca.site3.ssf.gesturerecognizer.GestureDataSet;
import ca.site3.ssf.gesturerecognizer.GestureInstance;
import ca.site3.ssf.gesturerecognizer.GestureRecognizer;
import ca.site3.ssf.gesturerecognizer.GestureType;
import ca.site3.ssf.gesturerecognizer.GloveData;

import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

// A container panel for the file and gesture info to save
class FileInfoPanel extends JPanel {
	
	private static final long serialVersionUID = 1L;
	
	public boolean isNewFile = false;
	
	public JComboBox gestureName;
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
		
		JLabel gestureNameLabel = new JLabel("Gesture Name:");
		gestureNameLabel.setForeground(Color.black);
		formLayoutHelper.addMiddleField(gestureNameLabel, this);
		formLayoutHelper.addLastField(this.gestureName, this);
	}
	
	// Save the data to a file. Using CSV currently, but if the hardware sends us comma-separated tuples, may need to use pipe-delimiting or something else
	public void exportToCsv(GestureInstance instance){
		try
		{	
			int iteration = 0;
			
	        // If the file exists, check if the next iteration of the file exists until we can make a new one
	        while (isNewFile && new File("Data/" + gestureName + Integer.toString(iteration) + ".csv").exists())
	        {
	        	iteration++;
	        }
	       
	        FileWriter writer = new FileWriter(new File("Data/" + gestureName + Integer.toString(iteration) + ".csv"), !isNewFile);
        	
	        // If we just created the file, 
	        if(isNewFile)
	        {
	        	writer.write("GyroLeftX,GyroLeftY,GyroLeftZ,MagLeftX,MagLeftY,MagLeftZ,AccLeftX,AccLeftY,AccLeftZ,GyroLeftX,GyroLeftY,GyroLeftZ,MagLeftX,MagLeftY,MagLeftZ,AccLeftX,AccLeftY,AccLeftZ,Time");
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
		}
	}
	
	// Save the data to a file that can be read by GestureRecognizer's fromDataString() method
	public void exportToRecognizer(GestureInstance instance){
		try
		{	
			int iteration = 0;
			
	        // If the file exists, check if the next iteration of the file exists until we can make a new one
	        while (new File("Data/" + gestureName + Integer.toString(iteration) + "_Recognizer.txt").exists())
	        {
	        	iteration++;
	        }
	       
	        // Save the data to a file readable by the GestureRecognizer
	        FileWriter writer = new FileWriter(new File("Data/" + gestureName + Integer.toString(iteration) + "_Recognizer.txt"), false);
	        writer.write(instance.toDataString());
	 
		    writer.flush();
		    writer.close();
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
		}
	}
	
	// Save a recognizer engine file
	public void exportToRecognizerEngine(GestureRecognizer gestureRecognizer)
	{
		try
		{	
			int iteration = 0;
			
	        // If the file exists, check if the next iteration of the file exists until we can make a new one
	        while (new File("Data/" + gestureName + Integer.toString(iteration) + "_RecognizerEngine.txt").exists())
	        {
	        	iteration++;
	        }
	       	        
	        // Save the data to a file readable by the Gesture Tester
	        FileWriter writer = new FileWriter(new File("Data/" + gestureName + Integer.toString(iteration) + "_RecognizerEngine.txt"), false);
	        gestureRecognizer.saveRecognizerEngine(writer);
	 
		    writer.flush();
		    writer.close();
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
		}
	}
}
