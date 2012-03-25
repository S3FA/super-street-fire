package ca.site3.ssf.devgui;

import java.awt.Color;
import java.awt.geom.Ellipse2D;

class EmitterData {
	
	final float maxIntensity;
	final Color colour;
	private Ellipse2D.Float shape = null;
	
	
	EmitterData() {
		this.colour = Color.black;
		this.maxIntensity = 0.0f;
	}
	
	EmitterData(float[] intensities, Color[] colours) {
		assert(intensities.length == colours.length);
		
		int totalRed = 0;
		int totalGreen = 0;
		int totalBlue = 0;
		float maxIntensity = 0.0f;
		
		for (int i = 0; i < intensities.length; i++) {
			totalRed   = Math.min(255, (int)(totalRed + intensities[i] * colours[i].getRed()));
			totalGreen = Math.min(255, (int)(totalGreen + intensities[i] * colours[i].getGreen()));
			totalBlue  = Math.min(255, (int)(totalBlue + intensities[i] * colours[i].getBlue()));
			maxIntensity = Math.max(maxIntensity, intensities[i]);
		}
		
		this.maxIntensity = maxIntensity;
		
		if (totalRed == 0 && totalGreen == 0 && totalBlue == 0) {
			this.colour = Color.black;
		}
		else {
			this.colour = new Color(totalRed, totalGreen, totalBlue);
		}
	}
	
	void setShape(Ellipse2D.Float shape) {
		assert(shape != null);
		this.shape = shape;
	}
	
	boolean contains(float x, float y) {
		if (this.shape == null) {
			return false;
		}
		return this.shape.contains(x, y);
	}
	
}
