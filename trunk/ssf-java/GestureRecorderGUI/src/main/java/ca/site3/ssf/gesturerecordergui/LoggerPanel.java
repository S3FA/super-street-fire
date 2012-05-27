package ca.site3.ssf.gesturerecordergui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import ca.site3.ssf.gesturerecognizer.GloveData;

/**
 * A container for the output log of each tab 
 * @author Mike
 *
 */
class LoggerPanel extends JPanel implements ActionListener {
	
	private static final long serialVersionUID = 1L;
	private JButton clearLogButton;
	private TextArea log = null;

	LoggerPanel(String logTitle) {
		super();
		
		Color borderColour = Color.black;
		
		TitledBorder border = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(borderColour), logTitle);
		border.setTitleColor(Color.black);
		this.setBorder(border);

		this.clearLogButton = new JButton("Clear");
		this.clearLogButton.addActionListener(this);
		
		this.log = new TextArea(23, 100);
		this.log.setEditable(false);
		
		this.setLayout(new BorderLayout());
		this.add(this.log, BorderLayout.CENTER);
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		buttonPanel.add(this.clearLogButton);
		this.add(buttonPanel, BorderLayout.EAST);
	}
	
	void setTextAreaSize(int rows, int cols) {
		this.log.setRows(rows);
		this.log.setColumns(cols);
	}
	
	// Handles button events
	public void actionPerformed(ActionEvent e)
	{
		if(e.getSource() == this.clearLogButton)
		{
			this.clearLog();
		}
	}
	
	// Log the data 
	public void logGestureData(GloveData data, String gestureName, double time){
		this.log.append(gestureName);
		this.log.append(": ");
	    this.log.append("Gyro (L): " + Double.toString(data.getGyroData().getX()));
	    this.log.append(", ");
	    this.log.append(Double.toString(data.getGyroData().getY()));
	    this.log.append(", ");
	    this.log.append(Double.toString(data.getGyroData().getZ()));
	    this.log.append(", ");
	    this.log.append("Mag (L): " + Double.toString(data.getMagnetoData().getX()));
	    this.log.append(", ");
	    this.log.append(Double.toString(data.getMagnetoData().getY()));
	    this.log.append(", ");
	    this.log.append(Double.toString(data.getMagnetoData().getZ()));
	    this.log.append(", ");
	    this.log.append("Acc (L): " + Double.toString(data.getAccelData().getX()));
	    this.log.append(", ");
	    this.log.append(Double.toString(data.getAccelData().getY()));
	    this.log.append(", ");
	    this.log.append(Double.toString(data.getAccelData().getZ()));
	    this.log.append(", ");
	    this.log.append("Gyro (R): " + Double.toString(data.getGyroData().getX()));
	    this.log.append(", ");
	    this.log.append(Double.toString(data.getGyroData().getY()));
	    this.log.append(", ");
	    this.log.append(Double.toString(data.getGyroData().getZ()));
	    this.log.append(", ");
	    this.log.append("Mag (R): " + Double.toString(data.getMagnetoData().getX()));
	    this.log.append(", ");
	    this.log.append(Double.toString(data.getMagnetoData().getY()));
	    this.log.append(", ");
	    this.log.append(Double.toString(data.getMagnetoData().getZ()));
	    this.log.append(", ");
	    this.log.append("Acc (R): " + Double.toString(data.getAccelData().getX()));
	    this.log.append(", ");
	    this.log.append(Double.toString(data.getAccelData().getY()));
	    this.log.append(", ");
	    this.log.append(Double.toString(data.getAccelData().getZ()));
	    this.log.append(", ");
	    this.log.append(Double.toString(time));
	    this.log.append("\n");
	}
	
	// Retrieves the log's text
	public String getLogText()
	{
		return this.log.getText();
	}
	
	// Sets the log's text
	public void setLogText(String text)
	{
		this.log.setText(text);
	}
	
	// Appends to the log
	public void appendLogText(String text)
	{
		this.log.append(text);
	}
	
	public void appendLogTextLine(String line) {
		this.log.append(line + "\n");
	}
	
	// Clears the log
	public void clearLog()
	{
		this.log.setText("");
	}
}
