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
	
	final private List<FireEmitter> fireEmitterList;
	final private boolean reverseDirection;
	final private boolean wrapAround;
	private ListIterator<FireEmitter> iterator;
	
	FireEmitterIterator(List<FireEmitter> fireEmitterList, int startIdx, 
				        boolean reverseDirection, boolean wrapAround) {
		
		assert(startIdx >= 0 && startIdx <= fireEmitterList.size());
		assert(fireEmitterList != null);
		
		this.fireEmitterList = fireEmitterList;
		this.iterator = fireEmitterList.listIterator(startIdx);
		this.reverseDirection = reverseDirection;
		this.wrapAround = wrapAround;
		
		// Need to accommodate the call to previous for a reversed iterator...
		if (this.reverseDirection) {
			try {
				this.iterator.next();
			}
			catch (NoSuchElementException ex) {
				this.iterator = this.fireEmitterList.listIterator(this.fireEmitterList.size());
			}
		}
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
			if (this.wrapAround) {
				if (this.reverseDirection) {
					this.iterator = this.fireEmitterList.listIterator(this.fireEmitterList.size());
				}
				else {
					this.iterator = this.fireEmitterList.listIterator(0);
				}
			}
		}
		
		// We've fallen through due to a wrap around, try again... this should
		// only fail if the original list was empty
		try {
			if (this.reverseDirection) {
				return this.iterator.previous();
			}
			return this.iterator.next();
		}
		catch (NoSuchElementException ex) {
			assert(this.fireEmitterList.isEmpty());
			return null;
		}
		
	}
}
