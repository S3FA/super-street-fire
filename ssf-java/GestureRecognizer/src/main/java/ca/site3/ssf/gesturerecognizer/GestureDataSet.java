package ca.site3.ssf.gesturerecognizer;

public class GestureDataSet {
	private GestureInstance[] instances;
	
	public GestureDataSet() {
		this.instances = new GestureInstance[1];
		this.instances[0] = new GestureInstance();
	}
	
	public GestureDataSet(GestureInstance[] instances) {
		this.instances = instances;
		assert(instances != null);
	}
	
	// TODO
	
}
