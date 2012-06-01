package ca.site3.ssf.gamemodel;

public final class HeadsetData {
	private final double attention;
	private final double meditation;
	
	public HeadsetData(double attention, double meditation) {
		assert(attention >= 0.0 && attention <= 1.0);
		this.attention  = attention;
		assert(meditation >= 0.0 && meditation <= 1.0);
		this.meditation = meditation;
	}
	
	public double getAttention() {
		return this.attention;
	}
	public double getMeditation() {
		return this.meditation;
	}
}
