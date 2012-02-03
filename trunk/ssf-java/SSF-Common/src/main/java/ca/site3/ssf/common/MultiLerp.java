package ca.site3.ssf.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MultiLerp {
	
	private double interpolant = 0.0;
	private double x           = 0.0;
	
	private List<Double> interpolationPts = null;
	private List<Double> timePts = null;
	
	private int tracker = 0; // Tracks the index into the *Pts arrays
	
	public MultiLerp() {
		this.clearLerp();
	}
	
	public double getInterpolantValue() {
		return this.interpolant;
	}
	
	/**
	 * Determine whether the lerp is finished lerping.
	 * @return true if finished, false otherwise.
	 */
	public boolean isFinished() {
		return (this.x == this.timePts.get(this.timePts.size()-1));
	}
	
	public void setLerp(List<Double> times, List<Double> values) {
		assert(times.size() >= 2);
		assert(times.size() == values.size());
		
		// Copy the time and interpolation point arrays
		this.timePts = new ArrayList<Double>(times);
		Collections.copy(this.timePts, times);
		this.interpolationPts = new ArrayList<Double>(values);
		Collections.copy(this.interpolationPts, values);
		
		this.resetLerp();
	}
	
	public void setLerp(double[] times, double[] values) {
		assert(times.length >= 2);
		assert(times.length == values.length);
		
		// Copy the time and interpolation point arrays
		this.timePts = new ArrayList<Double>(times.length);
		for (double d : times) {
			this.timePts.add(new Double(d));
		}
		this.interpolationPts = new ArrayList<Double>(values.length);
		for (double d : values) {
			this.interpolationPts.add(new Double(d));
		}
		
		this.resetLerp();
	}	
	
	public void clearLerp() {
		this.x = 0.0;
		this.tracker = 0;
		this.interpolationPts = new ArrayList<Double>(2);
		this.timePts          = new ArrayList<Double>(2);
		this.interpolant      = this.interpolationPts.get(0);
	}
	
	public void resetLerp() {
		this.x = 0.0;
		this.interpolant = this.interpolationPts.get(0);
		this.tracker = 0;
	}
	
	public boolean tick(double dT) {
		
		// Make sure everything is setup properly
		assert(this.timePts.size() == this.interpolationPts.size());
		assert(this.timePts.size() >= 2);
		
		if (this.tracker == this.timePts.size()-1) {
			// Animation is finished
			return true;
		}
		
		// If the current amount of time is less than the initial time for the
		// animation then we just increment the time and do nothing else
		if (x < this.timePts.get(0).doubleValue()) {
			assert(this.tracker == 0);
			x += dT;
			return false;
		}
		
		// Get the current interpolation interval in the multi-lerp
		double valueStart = this.interpolationPts.get(this.tracker).doubleValue();
		double valueEnd   = this.interpolationPts.get(this.tracker+1).doubleValue();
		double timeStart  = this.timePts.get(this.tracker).doubleValue();
		double timeEnd    = this.timePts.get(this.tracker+1).doubleValue();
		
		// Watch for the problematic case where the two times are very close to being the same (or are the same)
		// in this case we immediately increment to the next interval in the lerp
		if (Math.abs(timeEnd - timeStart) < 0.000001) {
			x = timeEnd;
			this.interpolant = valueEnd;
			this.tracker++;
			return (this.tracker == this.timePts.size()-1);
		}
		
		// Linearly interpolate the current interval
		this.interpolant = valueStart + (x - timeStart) * (valueEnd - valueStart) / (timeEnd - timeStart);
		
		// Check to see if the lerp is done
		if (this.x < timeEnd) {
			x += dT;
			return false;
		}
		else {
			x = timeEnd;
			this.interpolant = valueEnd;
			this.tracker++;
		}
		
		return (this.tracker == this.timePts.size()-1);
	}
	
}
