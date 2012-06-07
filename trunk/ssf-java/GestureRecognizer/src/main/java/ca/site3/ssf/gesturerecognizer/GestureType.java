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
	
	// TODO: Maximum/Full attack fierceness threshold (where anything at or above this counts as the
	// fastest and strongest possible version of the attack)
	
	LEFT_BLOCK(3, PlayerActionType.BLOCK, 0, true, false),
	RIGHT_BLOCK(3, PlayerActionType.BLOCK, 0, false, true),
	TWO_HANDED_BLOCK(3, PlayerActionType.BLOCK, 0, true, true),
	
	LEFT_JAB(4, PlayerActionType.JAB_ATTACK, 9000, true, false),
	LEFT_HOOK(4, PlayerActionType.HOOK_ATTACK, 16000, true, false),
	LEFT_UPPERCUT(4, PlayerActionType.UPPERCUT_ATTACK, 25000, true, false),
	LEFT_CHOP(4, PlayerActionType.CHOP_ATTACK, 0, true, false),
	RIGHT_JAB(4, PlayerActionType.JAB_ATTACK, 9000, false, true),
	RIGHT_HOOK(4, PlayerActionType.HOOK_ATTACK, 16000, false, true),
	RIGHT_UPPERCUT(4, PlayerActionType.UPPERCUT_ATTACK, 25000, false, true),
	RIGHT_CHOP(4, PlayerActionType.CHOP_ATTACK, 0, false, true),
	
	HADOUKEN(4, PlayerActionType.HADOUKEN_ATTACK, 0, true, true),
	LEFT_SHORYUKEN(4, PlayerActionType.SHORYUKEN_ATTACK, 0, true, false),
	RIGHT_SHORYUKEN(4, PlayerActionType.SHORYUKEN_ATTACK, 0, false, true),
	SONIC_BOOM(4, PlayerActionType.SONIC_BOOM_ATTACK, 0, true, true),
	DOUBLE_LARIAT(4, PlayerActionType.DOUBLE_LARIAT_ATTACK, 0, true, true),
	SUMO_HEADBUTT(4, PlayerActionType.SUMO_HEADBUTT_ATTACK, 0, true, true),
	LEFT_ONE_HUNDRED_HAND_SLAP(4, PlayerActionType.ONE_HUNDRED_HAND_SLAP_ATTACK, 0, true, false),
	RIGHT_ONE_HUNDRED_HAND_SLAP(4, PlayerActionType.ONE_HUNDRED_HAND_SLAP_ATTACK, 0, false, true),
	TWO_HANDED_ONE_HUNDRED_HAND_SLAP(4, PlayerActionType.ONE_HUNDRED_HAND_SLAP_ATTACK, 0, true, true),
	PSYCHO_CRUSHER(4, PlayerActionType.PSYCHO_CRUSHER_ATTACK, 0, true, true),
	
	YMCA(6, PlayerActionType.YMCA_ATTACK, 0, true, true),
	NYAN_CAT(4, PlayerActionType.NYAN_CAT_ATTACK, 0, true, true);
	
	final private PlayerActionType actionFactoryType; // The corresponding gamemodel factory type for when
													  // it comes time to build the gesture for the gamemodel
	
	final private boolean leftHanded;   // Whether a left hand is used to make the gesture
	final private boolean rightHanded;  // Whether a right hand is used to make the gesture

	final private double minimumFiercenessDiff;    // Minimum fierceness difference (maxAccel-minAccel) threshold (where anything below this is not considered an attack)
	//final private double fullFiercenessDiff;     // Maximum/Full attack fierceness  difference (maxAccel-minAccel) threshold
												   // (where anything at or above this counts as the fastest and strongest possible version of the attack)
	
	final private int numHands;    // The number of hands used to make the gesture
	final private int numHmmNodes; // The number of Hidden Markov Model nodes

	GestureType(int numHmmNodes, PlayerActionType actionFactoryType,
				double minimumFiercenessDiff,
			    boolean leftHand, boolean rightHand) {
		
		assert(numHmmNodes > 0);
		assert(leftHand || rightHand);
		assert(actionFactoryType != null);
		
		this.actionFactoryType = actionFactoryType;
		
		this.leftHanded  = leftHand;
		this.rightHanded = rightHand;
		
		this.numHmmNodes = numHmmNodes;
		
		this.minimumFiercenessDiff = minimumFiercenessDiff;
		
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
	public double getMinFierceDiffThreshold() {
		return this.minimumFiercenessDiff;
	}
	
	int getNumHmmNodes() {
		return this.numHmmNodes;
	}	
	
}
