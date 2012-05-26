package ca.site3.ssf.ioserver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.site3.ssf.gamemodel.IGameModel.Entity;
import ca.site3.ssf.gesturerecognizer.GestureInstance;
import ca.site3.ssf.gesturerecognizer.GloveData;
import ca.site3.ssf.ioserver.DeviceConstants.DeviceType;
import ca.site3.ssf.ioserver.GloveEvent.EventType;

/**
 * The GloveEventCoalescer consumes the low-level {@link GloveEvent}s
 * that come in and is responsible for caching them (per-device) and
 * combining them into {@link GestureInstance}s that are put onto
 * a queue to be consumed by something else.
 * 
 * @author greg
 */
public class GloveEventCoalescer implements Runnable {

	private Logger log = LoggerFactory.getLogger(getClass());
	
	// Input queue(s)
	private BlockingQueue<DeviceEvent> deviceEventQueue;
	
	// Output queue(s)
	private BlockingQueue<PlayerGestureInstance> gestureInstanceQueue;
	private BlockingQueue<HeadsetEvent> externalP1HeadsetQueue = new LinkedBlockingQueue<HeadsetEvent>();
	private BlockingQueue<HeadsetEvent> externalP2HeadsetQueue = new LinkedBlockingQueue<HeadsetEvent>();
	
	/**
	 * How many {@link GloveEvent}s to cache before aggregating into
	 * a {@link GestureInstance}.
	 */
	private static final int GLOVE_DATA_CACHE_SIZE   = 128;
	/**
	 * How many {@link HeadsetEvent}s to cache before aggregating into
	 * a {@link GestureInstance}.
	 */
	private static final int INTERNAL_HEADSET_DATA_CACHE_SIZE = GLOVE_DATA_CACHE_SIZE;
	private static final int EXTERNAL_HEADSET_DATA_CACHE_SIZE = GLOVE_DATA_CACHE_SIZE;
	
	protected Queue<GloveEvent> p1LeftQueue  = new LinkedList<GloveEvent>();
	protected Queue<GloveEvent> p1RightQueue = new LinkedList<GloveEvent>();
	protected Queue<GloveEvent> p2LeftQueue  = new LinkedList<GloveEvent>();
	protected Queue<GloveEvent> p2RightQueue = new LinkedList<GloveEvent>();
	
	// These are used to measure headset information for the creation of gestures - these average out
	// the data over the course of a gesture
	protected Queue<HeadsetEvent> internalP1HeadsetQueue = new LinkedList<HeadsetEvent>();
	protected Queue<HeadsetEvent> internalP2HeadsetQueue = new LinkedList<HeadsetEvent>();
	
	protected boolean p1LeftBtnDown  = false;
	protected boolean p1RightBtnDown = false;
	protected boolean p2LeftBtnDown  = false;
	protected boolean p2RightBtnDown = false;
	
	protected final long startTime;
	protected final double bothButtonsDownThreshold; // In milliseconds
	
	/**
	 * 
	 * @param startTime the time the event loop for the game started
	 * @param bothButtonsDownThresholdInSecs for two-handed gestures, max time difference allowed
	 * between the first button push and the second, in seconds
	 * @param deviceEventQueue objects will be consumed from this queue
	 * @param gestureInstanceQueue this queue will be populated
	 */
	public GloveEventCoalescer(long startTime, double bothButtonsDownThresholdInSecs,
							   BlockingQueue<DeviceEvent> deviceEventQueue,
							   BlockingQueue<PlayerGestureInstance> gestureInstanceQueue) {
		
		this.deviceEventQueue = deviceEventQueue;
		this.gestureInstanceQueue = gestureInstanceQueue;
		
		this.startTime = startTime;
		this.bothButtonsDownThreshold = bothButtonsDownThresholdInSecs * 1000.0;
	}
	
	
	public BlockingQueue<HeadsetEvent> getP1HeadsetEventQueue() {
		return this.externalP1HeadsetQueue;
	}
	
	public BlockingQueue<HeadsetEvent> getP2HeadsetEventQueue() {
		return this.externalP2HeadsetQueue;
	}
	
	public void run() {
		
		while (true) {
			try {
				
				DeviceEvent e = deviceEventQueue.take();
				if (e.getDevice() == DeviceType.HEADSET) {
					
					HeadsetEvent headsetEvent = (HeadsetEvent)e;
					
					// We only append the event to the internal queue if we're in the middle of a recording gesture...
					if (this.isAnyButtonDownForDeviceEvent(headsetEvent)) {
					
						Queue<HeadsetEvent> internalEventQueue = this.internalHeadsetEventQueueForDeviceEvent(headsetEvent);
						if (internalEventQueue == null) {
							// This should never happen, but just to be robust...
							continue;
						}
						
						internalEventQueue.add(headsetEvent);
						if (internalEventQueue.size() > INTERNAL_HEADSET_DATA_CACHE_SIZE) {
							log.info("Full HeadsetEvent queue. Aggregating internal headset data.");
							this.aggregateHeadsetEventQueue(internalEventQueue);
						}
					}
					
					// We ALWAYS append data to the external headset device queues
					BlockingQueue<HeadsetEvent> externalEventQueue = this.externalHeadsetEventQueueForDeviceEvent(headsetEvent);
					if (externalEventQueue == null) {
						continue;
					}
					
					externalEventQueue.add(headsetEvent);
					if (externalEventQueue.size() > EXTERNAL_HEADSET_DATA_CACHE_SIZE) {
						log.info("Full HeadsetEvent queue. Aggregating external headset data.");
						this.aggregateHeadsetEventQueue(externalEventQueue);
					}

					continue;
				}
				
				GloveEvent ge = (GloveEvent)e;
				if (ge.getEventType() == EventType.DATA_EVENT) {
					
					// currently assume we only get these when the button is down.
					Queue<GloveEvent> eventQueue = this.gloveEventQueueForDeviceEvent(ge);
					if (eventQueue == null) {
						// This should never happen, but just to be robust...
						continue;
					}
					
					eventQueue.add(ge);
					if (eventQueue.size() > GLOVE_DATA_CACHE_SIZE) {
						log.info("Full GloveEvent queue. Creating GestureInstance.");
						List<PlayerGestureInstance> gestures = this.aggregateForPlayer(ge.getSource());
						if (gestures != null) {
							gestureInstanceQueue.addAll(gestures);
						}
					}
				}
				else {
					this.changeButtonDownState(ge);
					
					/*
					 * This is where decisions get made about what to do after a change in button
					 * down state.
					 */
					if (ge.getEventType() == EventType.BUTTON_UP_EVENT && isOtherButtonDown(ge) == false) {
						// button was released and other glove's button is not down.
						// create one or more GestureInstances.
						List<PlayerGestureInstance> gestures = this.aggregateForPlayer(e.getSource());
						if (gestures != null) {
							gestureInstanceQueue.addAll(gestures);
						}
					}
					
					// When we start a new gesture (i.e., the button is pressed down) we need to clear any previous
					// headset data for the gestures.
					if (ge.getEventType() == EventType.BUTTON_DOWN_EVENT) {
						Queue<HeadsetEvent> eventQueue = this.internalHeadsetEventQueueForDeviceEvent(ge);
						eventQueue.clear();
					}
					
				}
			} catch (InterruptedException ex) {
				log.warn("Interrupted waiting for DeviceEvent",ex);
			}
			
		}
	}
	
	/**
	 * Create a GestureInstance for the given player based on the current state of the
	 * GloveEvent caches.
	 * 
	 * @param player the player to create a GestureInstance for
	 * @return the created {@link PlayerGestureInstance}
	 */
	protected List<PlayerGestureInstance> aggregateForPlayer(Entity player) {
		assert (player == Entity.PLAYER1_ENTITY || player == Entity.PLAYER2_ENTITY);
		
		Queue<GloveEvent> left;
		Queue<GloveEvent> right;
		int playerNum;
		if (player == Entity.PLAYER1_ENTITY) {
			playerNum = 1;
			left = p1LeftQueue;
			right = p1RightQueue;
		}
		else if (player == Entity.PLAYER2_ENTITY) {
			playerNum = 2;
			left = p2LeftQueue;
			right = p2RightQueue;
		}
		else {
			throw new IllegalArgumentException("Invalid player for glove data aggregation");
		}
		
		List<PlayerGestureInstance> gestures = new ArrayList<PlayerGestureInstance>();
		
		List<Double> timePts = null;
		List<GloveData> leftGloveData = null;
		List<GloveData> rightGloveData = null;
		
		// easy case: one-handed gestures
		if (right.isEmpty() && left.isEmpty()) {
			// no data -- this should never happen
			log.warn("Trying to create GestureInstance without any glove data. What nonsense!");
		}
		else if (right.isEmpty() == false && left.isEmpty() == true) {
			// Right-handed gesture
			timePts = new ArrayList<Double>(right.size());
			leftGloveData = Collections.emptyList();
			rightGloveData = new ArrayList<GloveData>(right.size());
			while ( ! right.isEmpty()) {
				GloveEvent ge = right.remove();
				rightGloveData.add(createGloveData(ge));
				timePts.add((ge.getTimestamp() - startTime) / 1000.0);
			}
			gestures.add(new PlayerGestureInstance(playerNum, leftGloveData, rightGloveData, timePts));
		}
		else if (left.isEmpty() == false && right.isEmpty() == true) {
			// Left-handed gesture
			timePts = new ArrayList<Double>(left.size());
			leftGloveData = new ArrayList<GloveData>(left.size());
			rightGloveData = Collections.emptyList();
			while ( ! left.isEmpty()) {
				GloveEvent ge = left.remove();
				leftGloveData.add(createGloveData(ge));
				timePts.add((ge.getTimestamp() - startTime) / 1000.0);
			}
			gestures.add(new PlayerGestureInstance(playerNum, leftGloveData, rightGloveData, timePts));
		}
		else {
			
			/*
			 * We have events in both left and right glove caches.
			 * This gets mildly tricky. GestureInstances need to have an equal number of
			 * left/right GloveEvents (unless one of the gloves has no data). Also need to provide a
			 * single timestamp (as the # of seconds elapsed since game started, Double precision) for
			 * each glove data pair.
			 */
			
			// Get the times of the data points at the head of the queue to compare start times
			long t_left = left.peek().getTimestamp();
			long t_right = right.peek().getTimestamp();
			
			if (Math.abs(t_left - t_right) > bothButtonsDownThreshold) {
				
				// If the buttons were not both initially pressed at the same (close enough) time,
				// split into two single handed gestures
				
				timePts = new ArrayList<Double>(left.size());
				leftGloveData = new ArrayList<GloveData>(left.size());
				rightGloveData = Collections.emptyList();
				while ( ! left.isEmpty()) {
					GloveEvent ge = left.remove();
					leftGloveData.add(createGloveData(ge));
					timePts.add((ge.getTimestamp() - startTime) / 1000.0);
				}
				gestures.add(new PlayerGestureInstance(playerNum, leftGloveData, rightGloveData, timePts));
				
				timePts = new ArrayList<Double>(right.size());
				leftGloveData = Collections.emptyList();
				rightGloveData = new ArrayList<GloveData>(right.size());
				while ( ! right.isEmpty()) {
					GloveEvent ge = right.remove();
					leftGloveData.add(createGloveData(ge));
					timePts.add((ge.getTimestamp() - startTime) / 1000.0);
				}
				gestures.add(new PlayerGestureInstance(playerNum, leftGloveData, rightGloveData, timePts));
			}
			else {
				
				/*
				 *  Two-handed gesture. Our approach here (for now at least) is to simply truncate the larger
				 *  cache and grab the timestamps from the smaller.
				 */
				
				// true if the 'main' (smaller) cache is for the left glove
				boolean usingLeft = left.size() <= right.size();
				Queue<GloveEvent> mainCache  = usingLeft ? left  : right;
				Queue<GloveEvent> otherCache = usingLeft ? right : left;
				
				timePts = new ArrayList<Double>(mainCache.size());
				leftGloveData = new ArrayList<GloveData>(mainCache.size());
				rightGloveData = new ArrayList<GloveData>(mainCache.size());
				
				while ( ! mainCache.isEmpty()) {
					
					GloveEvent mainEvent  = mainCache.remove();
					GloveEvent otherGlove = otherCache.remove();
					timePts.add((mainEvent.getTimestamp() - startTime) / 1000.0);
					
					if (usingLeft) {
						leftGloveData.add(createGloveData(mainEvent));
						rightGloveData.add(createGloveData(otherGlove));
					}
					else {
						rightGloveData.add(createGloveData(mainEvent));
						leftGloveData.add(createGloveData(otherGlove));
					}
				}
				
				gestures.add(new PlayerGestureInstance(playerNum, leftGloveData, rightGloveData, timePts));
				otherCache.clear();
			}
		}
		
		return gestures;
	}
	
	/**
	 * Averages all of the headset events in the given queue and then clears the queue and places
	 * the resulting averaged event into the queue.
	 * @param headsetEventQueue The queue to aggregate/average.
	 */
	private void aggregateHeadsetEventQueue(Queue<HeadsetEvent> headsetEventQueue) {
		HeadsetEvent avgHeadsetEvent = HeadsetEvent.getAverage(headsetEventQueue);
		if (avgHeadsetEvent == null) {
			assert(false);
			return;
		}
		
		headsetEventQueue.clear();
		headsetEventQueue.add(avgHeadsetEvent);
	}
	
	protected GloveData createGloveData(GloveEvent ge) {
		GloveData gd = new GloveData(ge.getGyro()[0], ge.getGyro()[1], ge.getGyro()[2], 
									 ge.getAcceleration()[0], ge.getAcceleration()[1], ge.getAcceleration()[2], 
									 ge.getMagnetometer()[0], ge.getMagnetometer()[1], ge.getMagnetometer()[2]);
		return gd;
	}
	
	
	private Queue<GloveEvent> gloveEventQueueForDeviceEvent(DeviceEvent e) {
		if (e.getSource() == Entity.PLAYER1_ENTITY && e.getDevice() == DeviceType.LEFT_GLOVE) {
			return p1LeftQueue;
		}
		else if (e.getSource() == Entity.PLAYER1_ENTITY && e.getDevice() == DeviceType.RIGHT_GLOVE) {
			return p1RightQueue;
		}
		else if (e.getSource() == Entity.PLAYER2_ENTITY && e.getDevice() == DeviceType.LEFT_GLOVE) {
			return p2LeftQueue;
		}
		else if (e.getSource() == Entity.PLAYER2_ENTITY && e.getDevice() == DeviceType.RIGHT_GLOVE) {
			return p2RightQueue;
		}
		
		log.error("No queue for event with device type: ", e.getDevice());
		return null;
	}
	
	private Queue<HeadsetEvent> internalHeadsetEventQueueForDeviceEvent(DeviceEvent e) {
		switch(e.getSource()) {
		case PLAYER1_ENTITY:
			return this.internalP1HeadsetQueue;
			
		case PLAYER2_ENTITY:
			return this.internalP2HeadsetQueue;
			
		default:
			break;
		}
		
		log.error("No queue for event with device type: ", e.getDevice());
		return null;
	}
	
	private BlockingQueue<HeadsetEvent> externalHeadsetEventQueueForDeviceEvent(DeviceEvent e) {
		switch(e.getSource()) {
		case PLAYER1_ENTITY:
			return this.externalP1HeadsetQueue;
			
		case PLAYER2_ENTITY:
			return this.externalP2HeadsetQueue;
			
		default:
			break;
		}
		
		log.error("No queue for event with device type: ", e.getDevice());
		return null;
	}
	
	private boolean isAnyButtonDownForDeviceEvent(DeviceEvent e) {
		switch(e.getSource()) {
		case PLAYER1_ENTITY:
			return this.p1LeftBtnDown || this.p1RightBtnDown;
			
		case PLAYER2_ENTITY:
			return this.p2LeftBtnDown || this.p2RightBtnDown;
			
		default:
			break;
		}

		return false;
	}
	
	/**
	 * Sets the p{1,2}{Left,Right}BtnDown boolean to false if e is a button up event,
	 * or to true if it's a button down event.
	 * 
	 * @param e the GloveEvent used to determine the device/player and event type
	 */
	private void changeButtonDownState(GloveEvent e) {
		if (e.getSource() == Entity.PLAYER1_ENTITY && e.getDevice() == DeviceType.LEFT_GLOVE) {
			p1LeftBtnDown = e.getEventType() == EventType.BUTTON_DOWN_EVENT;
		}
		else if (e.getSource() == Entity.PLAYER1_ENTITY && e.getDevice() == DeviceType.RIGHT_GLOVE) {
			p1RightBtnDown = e.getEventType() == EventType.BUTTON_DOWN_EVENT;
		}
		else if (e.getSource() == Entity.PLAYER2_ENTITY && e.getDevice() == DeviceType.LEFT_GLOVE) {
			p2LeftBtnDown = e.getEventType() == EventType.BUTTON_DOWN_EVENT;
		}
		else if (e.getSource() == Entity.PLAYER2_ENTITY && e.getDevice() == DeviceType.RIGHT_GLOVE) {
			p2RightBtnDown = e.getEventType() == EventType.BUTTON_DOWN_EVENT;
		}
	}
	
	
	/**
	 * Returns true if the button corresponding on <code>e</code>'s player's opposite glove is currently down.
	 * @param e the event
	 */
	private boolean isOtherButtonDown(GloveEvent e) {
		if (e.getSource() == Entity.PLAYER1_ENTITY && e.getDevice() == DeviceType.LEFT_GLOVE) {
			return p1RightBtnDown;
		}
		else if (e.getSource() == Entity.PLAYER1_ENTITY && e.getDevice() == DeviceType.RIGHT_GLOVE) {
			return p1LeftBtnDown;
		}
		else if (e.getSource() == Entity.PLAYER2_ENTITY && e.getDevice() == DeviceType.LEFT_GLOVE) {
			return p2RightBtnDown;
		}
		else if (e.getSource() == Entity.PLAYER2_ENTITY && e.getDevice() == DeviceType.RIGHT_GLOVE) {
			return p2LeftBtnDown;
		}
		
		throw new IllegalArgumentException("Non-player GloveEvent passed to isOtherButtonDown");
	}
}
