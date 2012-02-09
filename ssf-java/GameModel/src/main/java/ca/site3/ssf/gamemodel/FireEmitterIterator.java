package ca.site3.ssf.gamemodel;

import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

/**
 * Simple wrapper around a list iterator for FireEmitters. This allows abstracted
 * iterating to the next FireEmitter whether its forward or backwards through the
 * provided FireEmitter list. A useful abstraction when iterating over a rail of fire emitters
 * for player 1 attacks vs. player 2 attacks, where one is the reverse direction of the other.
 * 
 * @author Callum
 *
 */
class FireEmitterIterator {
	
	private ListIterator<FireEmitter> iterator = null;
	private boolean reverseDirection = false;
	
	FireEmitterIterator(List<FireEmitter> fireEmitterList, int startIdx, boolean reverseDirection) {
		assert(startIdx >= 0 && startIdx <= fireEmitterList.size());
		this.iterator = fireEmitterList.listIterator(startIdx);
		this.reverseDirection = reverseDirection;
	}
	
	boolean hasNext() {
		if (this.reverseDirection) {
			return this.iterator.hasPrevious();
		}
		return this.iterator.hasNext();
	}
	
	FireEmitter next() {
		try {
			if (this.reverseDirection) {
				return this.iterator.previous();
			}
			return this.iterator.next();
		}
		catch (NoSuchElementException ex) {
			assert(false);
			return null;
		}
	}
}
