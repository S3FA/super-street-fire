package ca.site3.ssf.ioserver;

import java.util.Collection;
import java.util.Iterator;

import ca.site3.ssf.gamemodel.IGameModel.Entity;
import ca.site3.ssf.ioserver.DeviceConstants.DeviceType;

/**
 * Represents the state of a headset's sensors
 * 
 * @author greg
 */
public final class HeadsetEvent extends DeviceEvent {

	private final double attention;
	private final double meditation;
	
	/**
	 * @param intensity between 0 and 1
	 */
	public HeadsetEvent(Entity src, DeviceType dvc, long timestamp, double attention, double meditation) {
		super(Type.HeadsetEvent, src, dvc, timestamp);
		
		if (attention < 0 || attention > 1) {
			throw new IllegalArgumentException("Headset attention value should be normalized between 0 and 1");
		} else if (meditation < 0 || meditation > 1) {
			throw new IllegalArgumentException("Headset meditation value should be normalized between 0 and 1");
		}
		
		this.attention = attention;
		this.meditation = meditation;
	}

	/**
	 * @return the attention value, between 0 and 1 inclusive
	 */
	public double getAttention() {
		return attention;
	}

	/**
	 * @return the meditation value, between 0 and 1 inclusive
	 */
	public double getMeditation() {
		return meditation;
	}
	
	public static HeadsetEvent getAverage(Collection<HeadsetEvent> headsetEvents) {
		
		// There should be at least one event in the provided collection
		if (headsetEvents.isEmpty()) {
			assert(false);
			return null;
		}
		
		double totalAttention  = 0.0;
		double totalMeditation = 0.0;
		
		for (HeadsetEvent event : headsetEvents) {
			totalAttention  += event.getAttention();
			totalMeditation += event.getMeditation();
		}
		
		Iterator<HeadsetEvent> iter = headsetEvents.iterator();
		HeadsetEvent firstEvent = iter.next();
		return new HeadsetEvent(firstEvent.getSource(), firstEvent.getDevice(), System.currentTimeMillis(),
				(totalAttention / (double)headsetEvents.size()), (totalMeditation / (double)headsetEvents.size()));
	}
	
}
