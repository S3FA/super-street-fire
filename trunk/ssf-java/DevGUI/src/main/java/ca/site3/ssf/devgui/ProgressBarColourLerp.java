package ca.site3.ssf.devgui;

import java.awt.Color;

import javax.swing.JProgressBar;

class ProgressBarColourLerp {
	private ProgressBarColourLerp() {
	}
	
	static void setPercentageAndRedToGreenColour(JProgressBar progressBar, float percent) {
		progressBar.setValue((int)percent);
		
		// Linear interpolate the colour of the life bar from green to red
		float red   = 1.0f + ((float)progressBar.getPercentComplete() - 0.0f) * ((0.0f - 1.0f) / (1.0f - 0.0f));
		float green = 0.0f + ((float)progressBar.getPercentComplete() - 0.0f) * ((1.0f - 0.0f) / (1.0f - 0.0f));
		float blue  = 0.0f;
		
		progressBar.setForeground(new Color(red, green, blue));
	}
}
