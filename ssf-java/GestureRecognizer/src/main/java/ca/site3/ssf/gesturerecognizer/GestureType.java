package ca.site3.ssf.gesturerecognizer;

/**
 * Enumeration for the various types of one-handed and two-handed gestures
 * in the Super Street Fire game. Also contains important tweaking information for
 * each gesture's machine learning technique (i.e., how to build the initial machine
 * learning structures for that technique).
 * 
 * @author Callum
 *
 */
public enum GestureType {
	LEFT_JAB(5, 1), LEFT_HOOK(5, 1),   					// One-handed left-handed gestures
	RIGHT_JAB(5, 1), RIGHT_HOOK(5, 1), 					// One-handed right-handed gestures
	BLOCK(2, 2), HADOUKEN(10, 2), SONIC_BOOM(10, 2);  	// Two-handed gestures
	
	final private int numHmmNodes; // The number of Hidden Markov Model nodes
	final private int numHands;    // The number of hands used in the gesture
	
	GestureType(int numHmmNodes, int numHands) {
		assert(numHmmNodes > 0);
		assert(numHands == 1 || numHands == 2);
		
		this.numHmmNodes = numHmmNodes;
		this.numHands = numHands;
	}
	
	int getNumHmmNodes() {
		return this.numHmmNodes;
	}
	int getNumHands() {
		return this.numHands;
	}
}
