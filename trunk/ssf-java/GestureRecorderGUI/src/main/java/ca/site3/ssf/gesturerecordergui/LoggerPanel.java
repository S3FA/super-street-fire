package ca.site3.ssf.gesturerecordergui;

import java.awt.Color;
import java.awt.GridBagLayout;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import ca.site3.ssf.gesturerecognizer.GloveData;

import java.util.Calendar;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

// A container panel for the file and gesture info to save
class LoggerPanel extends JPanel {
	
	private static final long serialVersionUID = 1L;
	
	public TextArea log = null;

	LoggerPanel() {
		super();
		
		Color borderColour = Color.black;
		
		TitledBorder border = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(borderColour), "Log");
		border.setTitleColor(Color.black);
		this.setBorder(border);

		this.log = new TextArea(15, 100);
		this.add(this.log);
	}
	
	// Save the data to a file. Using CSV currently, but if the hardware sends us comma-separated tuples, may need to use pipe-delimiting or something else
	public void logGestureData(GloveData data, String gestureName, double time){
		this.log.append(gestureName);
		this.log.append(": ");
	    this.log.append("Gyro: " + Double.toString(data.getGyroData().getX()));
	    this.log.append(", ");
	    this.log.append(Double.toString(data.getGyroData().getY()));
	    this.log.append(", ");
	    this.log.append(Double.toString(data.getGyroData().getZ()));
	    this.log.append(", ");
	    this.log.append("Mag: " + Double.toString(data.getMagnetoData().getX()));
	    this.log.append(", ");
	    this.log.append(Double.toString(data.getMagnetoData().getY()));
	    this.log.append(", ");
	    this.log.append(Double.toString(data.getMagnetoData().getZ()));
	    this.log.append(", ");
	    this.log.append("Acc: " + Double.toString(data.getAccelData().getX()));
	    this.log.append(", ");
	    this.log.append(Double.toString(data.getAccelData().getY()));
	    this.log.append(", ");
	    this.log.append(Double.toString(data.getAccelData().getZ()));
	    this.log.append(", ");
	    this.log.append(Double.toString(time));
	    this.log.append("\n");
	}
}
