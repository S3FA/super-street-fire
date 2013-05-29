package ca.site3.ssf.devgui;

class TimedBlockWindow {
	
	private final double timeLengthInSecs;
	private double countdownTime;
	
	TimedBlockWindow(double timeLengthInSecs) {
		assert(timeLengthInSecs > 0);
		this.timeLengthInSecs = timeLengthInSecs;
		this.countdownTime = timeLengthInSecs;
	}
	
	double getBlockTimeLength() {
		return this.timeLengthInSecs;
	}
	
	double getCountdownTime() {
		return this.countdownTime;
	}
	
	void tick(double dT) {
		this.countdownTime = Math.max(0.0, this.countdownTime - dT);
	}
	
	boolean isFinished() {
		return (this.countdownTime <= 0);
	}
}
