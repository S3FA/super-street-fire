package ca.site3.ssf.gesturerecognizer;

import java.util.List;
import java.util.Arrays;

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
	
	LEFT_BLOCK(7, PlayerActionType.BLOCK, GestureGenre.BASIC, 0, 3, true, false, Arrays.asList("left_blocks")),
	RIGHT_BLOCK(7, PlayerActionType.BLOCK, GestureGenre.BASIC, 0, 3, false, true, Arrays.asList("right_blocks")),
	TWO_HANDED_BLOCK(9, PlayerActionType.BLOCK, GestureGenre.BASIC, 0, 3, true, true, Arrays.asList("two_handed_blocks")),
	
	LEFT_JAB(7, PlayerActionType.JAB_ATTACK, GestureGenre.BASIC, 6200, 8, true, false, Arrays.asList("left_jabs")),
	LEFT_HOOK(7, PlayerActionType.HOOK_ATTACK, GestureGenre.BASIC, 8000, 10, true, false, Arrays.asList("left_hooks")),
	LEFT_UPPERCUT(7, PlayerActionType.UPPERCUT_ATTACK, GestureGenre.BASIC, 11000, 10, true, false, Arrays.asList("left_uppercuts")),
	LEFT_CHOP(7, PlayerActionType.CHOP_ATTACK, GestureGenre.BASIC, 8800, 10, true, false, Arrays.asList("left_chops")),
	RIGHT_JAB(7, PlayerActionType.JAB_ATTACK, GestureGenre.BASIC, 6200, 8, false, true, Arrays.asList("right_jabs")),
	RIGHT_HOOK(7, PlayerActionType.HOOK_ATTACK, GestureGenre.BASIC, 8000, 10, false, true, Arrays.asList("right_hooks")),
	RIGHT_UPPERCUT(7, PlayerActionType.UPPERCUT_ATTACK, GestureGenre.BASIC, 11000, 10, false, true, Arrays.asList("right_uppercuts")),
	RIGHT_CHOP(7, PlayerActionType.CHOP_ATTACK, GestureGenre.BASIC, 8800, 10, false, true, Arrays.asList("right_chops")),
	
	HADOUKEN(9, PlayerActionType.HADOUKEN_ATTACK, GestureGenre.SPECIAL, 7000, 10, true, true, Arrays.asList("hadoukens")),
	LEFT_SHORYUKEN(7, PlayerActionType.SHORYUKEN_ATTACK, GestureGenre.SPECIAL, 20000, 21, true, false, Arrays.asList("left_shoryukens")),
	RIGHT_SHORYUKEN(7, PlayerActionType.SHORYUKEN_ATTACK, GestureGenre.SPECIAL, 20000, 21, false, true, Arrays.asList("right_shoryukens")),
	SONIC_BOOM(9, PlayerActionType.SONIC_BOOM_ATTACK, GestureGenre.SPECIAL, 8400, 15, true, true, Arrays.asList("sonic_booms")),
	DOUBLE_LARIAT(9, PlayerActionType.DOUBLE_LARIAT_ATTACK, GestureGenre.SPECIAL, 6200, 40, true, true, Arrays.asList("double_lariats")),
	//This gets confused with the double lariat way too easily
	//QUADRUPLE_LARIAT(6, PlayerActionType.QUADRUPLE_LARIAT_ATTACK, GestureGenre.SPECIAL, 6200, 73, true, true, Arrays.asList("quadruple_lariats")),
	SUMO_HEADBUTT(9, PlayerActionType.SUMO_HEADBUTT_ATTACK, GestureGenre.SPECIAL, 7500, 13, true, true, Arrays.asList("sumo_headbutts")),
	LEFT_ONE_HUNDRED_HAND_SLAP(7, PlayerActionType.ONE_HUNDRED_HAND_SLAP_ATTACK, GestureGenre.SPECIAL, 16000, 38, true, false, Arrays.asList("left_100_hand_slaps")),
	RIGHT_ONE_HUNDRED_HAND_SLAP(7, PlayerActionType.ONE_HUNDRED_HAND_SLAP_ATTACK, GestureGenre.SPECIAL, 16000, 38, false, true, Arrays.asList("right_100_hand_slaps")),
	TWO_HANDED_ONE_HUNDRED_HAND_SLAP(9, PlayerActionType.ONE_HUNDRED_HAND_SLAP_ATTACK, GestureGenre.SPECIAL, 16000, 38, true, true, Arrays.asList("two_handed_100_hand_slaps")),
	PSYCHO_CRUSHER(9, PlayerActionType.PSYCHO_CRUSHER_ATTACK, GestureGenre.SPECIAL, 8000, 38, true, true, Arrays.asList("psycho_crushers")),
	
	YMCA(9, PlayerActionType.YMCA_ATTACK, GestureGenre.EASTER_EGG, 10000, 56, true, true, Arrays.asList("ymcas")),
	NYAN_CAT(9, PlayerActionType.NYAN_CAT_ATTACK, GestureGenre.EASTER_EGG, 6500, 30, true, true, Arrays.asList("nyan_cats")),
	// Disco Stu interferes with everything... ugh
	//DISCO_STU(7, PlayerActionType.DISCO_STU_ATTACK, GestureGenre.EASTER_EGG, 10000, 14, true, true, Arrays.asList("disco_stus")),
	ARM_WINDMILL(9, PlayerActionType.ARM_WINDMILL_ATTACK, GestureGenre.EASTER_EGG, 18000, 38, true, true, Arrays.asList("arm_windmills")),
	SUCK_IT(9, PlayerActionType.SUCK_IT_ATTACK, GestureGenre.EASTER_EGG, 12000, 10, true, true, Arrays.asList("suck_its")),
	LEFT_VAFANAPOLI_ATTACK(7, PlayerActionType.VAFANAPOLI_ATTACK, GestureGenre.EASTER_EGG, 5700, 9, true, false, Arrays.asList("left_vafanapoli")),
	RIGHT_VAFANAPOLI_ATTACK(7, PlayerActionType.VAFANAPOLI_ATTACK, GestureGenre.EASTER_EGG, 5700, 9, false, true, Arrays.asList("right_vafanapoli"));
	
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

	final private List<String> parentDirNames;
	
	GestureType(int numHmmNodes, PlayerActionType actionFactoryType,
			    GestureGenre genre,
				double minimumFiercenessDiff,
				int minimumNumDataPts,
			    boolean leftHand, boolean rightHand,
			    List<String> parentDirNames) {
		
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
		
		this.parentDirNames = parentDirNames;
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
	public List<String> getParentDirNameList() {
		return this.parentDirNames;
	}
	int getNumHmmNodes() {
		return this.numHmmNodes;
	}	
	
}
