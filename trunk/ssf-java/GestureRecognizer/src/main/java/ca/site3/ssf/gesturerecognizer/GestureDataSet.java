package ca.site3.ssf.gesturerecognizer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A GestureDataSet is a container for a full set of GestureInstances that are all relevant to
 * one particular type of gesture.
 * 
 * @author Callum
 *
 */
public class GestureDataSet {
	
	private List<GestureInstance> instances;
	
	public GestureDataSet() {
		this.instances = new ArrayList<GestureInstance>();
	}
	
	public GestureDataSet(GestureInstance[] instances) {
		this.instances = new ArrayList<GestureInstance>(instances.length);
		for (int i = 0; i < instances.length; i++) {
			this.instances.add(instances[i]);
		}
		assert(instances != null);
	}
	
	public int getNumGestureInstances() {
		return this.instances.size();
	}
	
	public GestureInstance getGestureInstanceAt(int index) {
		assert(index >= 0 && index < this.instances.size());
		return this.instances.get(index);
	}
	
	public void addGestureInstance(GestureInstance instance) {
		this.instances.add(instance);
	}
	
	public void cleanUpForGestureType(GestureType gestureType) {
		Iterator<GestureInstance> iter = instances.iterator();
		while (iter.hasNext()) {
			GestureInstance currInstance = iter.next();
			
			if (currInstance == null) {
				iter.remove();
				continue;
			}
			
			if (currInstance.hasLeftGloveData() && !gestureType.getUsesLeftHand() ||
				currInstance.hasRightGloveData() && !gestureType.getUsesRightHand() ||
				!currInstance.hasLeftGloveData() && gestureType.getUsesLeftHand() ||
				!currInstance.hasRightGloveData() && gestureType.getUsesRightHand()) {
				
				iter.remove();
				continue;
			}
		}
	}
}
