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
	
	LEFT_BLOCK(3, PlayerActionType.BLOCK, GestureGenre.BASIC, 0, 3, true, false),
	RIGHT_BLOCK(3, PlayerActionType.BLOCK, GestureGenre.BASIC, 0, 3, false, true),
	TWO_HANDED_BLOCK(3, PlayerActionType.BLOCK, GestureGenre.BASIC, 0, 3, true, true),
	
	LEFT_JAB(5, PlayerActionType.JAB_ATTACK, GestureGenre.BASIC, 5600, 8, true, false),
	LEFT_HOOK(5, PlayerActionType.HOOK_ATTACK, GestureGenre.BASIC, 8000, 10, true, false),
	LEFT_UPPERCUT(5, PlayerActionType.UPPERCUT_ATTACK, GestureGenre.BASIC, 11000, 10, true, false),
	LEFT_CHOP(5, PlayerActionType.CHOP_ATTACK, GestureGenre.BASIC, 7000, 10, true, false),
	RIGHT_JAB(5, PlayerActionType.JAB_ATTACK, GestureGenre.BASIC, 5600, 8, false, true),
	RIGHT_HOOK(5, PlayerActionType.HOOK_ATTACK, GestureGenre.BASIC, 8000, 10, false, true),
	RIGHT_UPPERCUT(5, PlayerActionType.UPPERCUT_ATTACK, GestureGenre.BASIC, 11000, 10, false, true),
	RIGHT_CHOP(5, PlayerActionType.CHOP_ATTACK, GestureGenre.BASIC, 7000, 10, false, true),
	
	HADOUKEN(7, PlayerActionType.HADOUKEN_ATTACK, GestureGenre.SPECIAL, 7000, 10, true, true),
	LEFT_SHORYUKEN(5, PlayerActionType.SHORYUKEN_ATTACK, GestureGenre.SPECIAL, 19000, 21, true, false),
	RIGHT_SHORYUKEN(5, PlayerActionType.SHORYUKEN_ATTACK, GestureGenre.SPECIAL, 19000, 21, false, true),
	SONIC_BOOM(7, PlayerActionType.SONIC_BOOM_ATTACK, GestureGenre.SPECIAL, 8000, 15, true, true),
	DOUBLE_LARIAT(7, PlayerActionType.DOUBLE_LARIAT_ATTACK, GestureGenre.SPECIAL, 6200, 40, true, true),
	//This gets confused with the double lariat way too easily
	//QUADRUPLE_LARIAT(6, PlayerActionType.QUADRUPLE_LARIAT_ATTACK, GestureGenre.SPECIAL, 6200, 73, true, true),
	SUMO_HEADBUTT(7, PlayerActionType.SUMO_HEADBUTT_ATTACK, GestureGenre.SPECIAL, 7000, 12, true, true),
	LEFT_ONE_HUNDRED_HAND_SLAP(5, PlayerActionType.ONE_HUNDRED_HAND_SLAP_ATTACK, GestureGenre.SPECIAL, 16000, 38, true, false),
	RIGHT_ONE_HUNDRED_HAND_SLAP(5, PlayerActionType.ONE_HUNDRED_HAND_SLAP_ATTACK, GestureGenre.SPECIAL, 16000, 38, false, true),
	TWO_HANDED_ONE_HUNDRED_HAND_SLAP(7, PlayerActionType.ONE_HUNDRED_HAND_SLAP_ATTACK, GestureGenre.SPECIAL, 16000, 38, true, true),
	PSYCHO_CRUSHER(7, PlayerActionType.PSYCHO_CRUSHER_ATTACK, GestureGenre.SPECIAL, 6500, 35, true, true),
	
	YMCA(7, PlayerActionType.YMCA_ATTACK, GestureGenre.EASTER_EGG, 10000, 56, true, true),
	NYAN_CAT(7, PlayerActionType.NYAN_CAT_ATTACK, GestureGenre.EASTER_EGG, 6500, 30, true, true),
	// Disco Stu interferes with everything... ugh
	//DISCO_STU(7, PlayerActionType.DISCO_STU_ATTACK, GestureGenre.EASTER_EGG, 10000, 14, true, true),
	ARM_WINDMILL(7, PlayerActionType.ARM_WINDMILL_ATTACK, GestureGenre.EASTER_EGG, 18000, 35, true, true),
	SUCK_IT(7, PlayerActionType.SUCK_IT_ATTACK, GestureGenre.EASTER_EGG, 7600, 10, true, true),
	LEFT_VAFANAPOLI_ATTACK(5, PlayerActionType.VAFANAPOLI_ATTACK, GestureGenre.EASTER_EGG, 5000, 9, true, false),
	RIGHT_VAFANAPOLI_ATTACK(5, PlayerActionType.VAFANAPOLI_ATTACK, GestureGenre.EASTER_EGG, 5000, 9, false, true);
	
	final private PlayerActionType actionFactoryType; // The corresponding gamemodel factory type for when
													  // it comes time to build the gesture for the gamemodel
	
	final private boolean leftHanded;   // Whether a left hand is used to make the gesture
	final private boolean rightHanded;  // Whether a right hand is used to make the gesture
	
	final private GestureGenre genre;   // The genre of this gesture
	
	final private double minimumFiercenessDiff;    // Minimum fierceness difference (maxAccel-minAccel) threshold (where anything below this is not considered an attack)
	//final private double fullFiercenessDiff;     // Maximum/Full attack fierceness  difference (maxAccel-minAccel) threshold
												   // (where anything at or above this counts as the fastest and strongest possible version of the attack)

	final private int minimumNumDataPts; // Minimum required number of data points for the gesture
	
	final private int numHands;    // The number of hands used to make the gesture
	final private int numHmmNodes; // The number of Hidden Markov Model nodes

	GestureType(int numHmmNodes, PlayerActionType actionFactoryType,
			    GestureGenre genre,
				double minimumFiercenessDiff,
				int minimumNumDataPts,
			    boolean leftHand, boolean rightHand) {
		
		assert(numHmmNodes > 0);
		assert(minimumNumDataPts > 0);
		assert(leftHand || rightHand);
		assert(actionFactoryType != null);
		
		this.actionFactoryType = actionFactoryType;
		
		this.leftHanded  = leftHand;
		this.rightHanded = rightHand;
		
		this.genre = genre;
		
		this.numHmmNodes = numHmmNodes;
		
		this.minimumFiercenessDiff = minimumFiercenessDiff;
		this.minimumNumDataPts     = minimumNumDataPts;
		
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
	public GestureGenre getGenre() {
		return this.genre;
	}
	public double getMinFierceDiffThreshold() {
		return this.minimumFiercenessDiff;
	}
	public int getMinNumDataPts() {
		return this.minimumNumDataPts;
	}
	
	int getNumHmmNodes() {
		return this.numHmmNodes;
	}	
	
}
