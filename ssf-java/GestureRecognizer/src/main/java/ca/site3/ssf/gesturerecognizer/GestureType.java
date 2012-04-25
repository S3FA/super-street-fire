package ca.site3.ssf.gesturerecognizer;

import ca.site3.ssf.gamemodel.ActionFactory.PlayerActionType;

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
	
	LEFT_JAB(3, PlayerActionType.JAB_ATTACK, true, false),
	LEFT_HOOK(3, PlayerActionType.HOOK_ATTACK, true, false),
	RIGHT_JAB(3, PlayerActionType.JAB_ATTACK, false, true),
	RIGHT_HOOK(3, PlayerActionType.HOOK_ATTACK, false, true),
	BLOCK(3, PlayerActionType.BLOCK, true, true),
	HADOUKEN(3, PlayerActionType.HADOUKEN_ATTACK, true, true),
	SONIC_BOOM(3, PlayerActionType.SONIC_BOOM_ATTACK, true, true);
	
	final private PlayerActionType actionFactoryType; // The corresponding gamemodel factory type for when
													  // it comes time to build the gesture for the gamemodel
	
	final private boolean leftHanded;  // Whether a left hand is used to make the gesture
	final private boolean rightHanded; // Whether a right hand is used to make the gesture
	
	final private int numHands;    // The number of hands used to make the gesture
	final private int numHmmNodes; // The number of Hidden Markov Model nodes

	GestureType(int numHmmNodes, PlayerActionType actionFactoryType,
			    boolean leftHand, boolean rightHand) {
		
		assert(numHmmNodes > 0);
		assert(leftHand || rightHand);
		assert(actionFactoryType != null);
		
		this.actionFactoryType = actionFactoryType;
		
		this.leftHanded  = leftHand;
		this.rightHanded = rightHand;
		
		this.numHmmNodes = numHmmNodes;
		
		int count = 0;
		if (this.leftHanded) {
			count++;
		}
		if (this.rightHanded) {
			count++;
		}		
		this.numHands = count;
	}
	
	public PlayerActionType getActionFactoryType() {
		return this.actionFactoryType;
	}
	public int getNumHands() {
		return this.numHands;
	}
	public boolean getUsesLeftHand() {
		return this.leftHanded;
	}
	public boolean getUsesRightHand() {
		return this.rightHanded;
	}
	
	int getNumHmmNodes() {
		return this.numHmmNodes;
	}	
	
}
