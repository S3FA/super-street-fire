package ca.site3.ssf.gesturerecognizer;

import java.util.List;
import java.util.Arrays;

import ca.site3.ssf.gamemodel.ActionFactory.ActionType;

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
	
	// NOTE: We are removing only-left and only-right handed blocks, this will make for better and more directed game-play
	//LEFT_BLOCK(RecognizerManager.NUM_ONE_HANDED_PLAYER_GESTURE_NODES, ActionType.BLOCK, GestureGenre.BASIC, 0, 3, true, false, false, Arrays.asList("left_blocks")),
	//RIGHT_BLOCK(RecognizerManager.NUM_ONE_HANDED_PLAYER_GESTURE_NODES, ActionType.BLOCK, GestureGenre.BASIC, 0, 3, false, true, false, Arrays.asList("right_blocks")),
	TWO_HANDED_BLOCK(RecognizerManager.NUM_TWO_HANDED_PLAYER_GESTURE_NODES, ActionType.BLOCK, GestureGenre.BASIC, 0, 3, true, true, false, Arrays.asList("two_handed_blocks")),

	LEFT_JAB(RecognizerManager.NUM_ONE_HANDED_PLAYER_GESTURE_NODES, ActionType.JAB_ATTACK, GestureGenre.BASIC, 0, 8, true, false, false, Arrays.asList("left_jabs")),
	LEFT_HOOK(RecognizerManager.NUM_ONE_HANDED_PLAYER_GESTURE_NODES, ActionType.HOOK_ATTACK, GestureGenre.BASIC, 0, 8, true, false, false, Arrays.asList("left_hooks")),
	LEFT_UPPERCUT(RecognizerManager.NUM_ONE_HANDED_PLAYER_GESTURE_NODES, ActionType.UPPERCUT_ATTACK, GestureGenre.BASIC, 0, 8, true, false, false, Arrays.asList("left_uppercuts")),
	LEFT_CHOP(RecognizerManager.NUM_ONE_HANDED_PLAYER_GESTURE_NODES, ActionType.CHOP_ATTACK, GestureGenre.BASIC, 0, 8, true, false, false, Arrays.asList("left_chops")),
	RIGHT_JAB(RecognizerManager.NUM_ONE_HANDED_PLAYER_GESTURE_NODES, ActionType.JAB_ATTACK, GestureGenre.BASIC, 0, 8, false, true, false, Arrays.asList("right_jabs")),
	RIGHT_HOOK(RecognizerManager.NUM_ONE_HANDED_PLAYER_GESTURE_NODES, ActionType.HOOK_ATTACK, GestureGenre.BASIC, 0, 8, false, true, false, Arrays.asList("right_hooks")),
	RIGHT_UPPERCUT(RecognizerManager.NUM_ONE_HANDED_PLAYER_GESTURE_NODES, ActionType.UPPERCUT_ATTACK, GestureGenre.BASIC, 0, 8, false, true, false, Arrays.asList("right_uppercuts")),
	RIGHT_CHOP(RecognizerManager.NUM_ONE_HANDED_PLAYER_GESTURE_NODES, ActionType.CHOP_ATTACK, GestureGenre.BASIC, 0, 8, false, true, false, Arrays.asList("right_chops")),

	HADOUKEN(RecognizerManager.NUM_TWO_HANDED_PLAYER_GESTURE_NODES, ActionType.HADOUKEN_ATTACK, GestureGenre.SPECIAL, 0, 8, true, true, false, Arrays.asList("hadoukens")),
	LEFT_SHORYUKEN(RecognizerManager.NUM_ONE_HANDED_PLAYER_GESTURE_NODES, ActionType.SHORYUKEN_ATTACK, GestureGenre.SPECIAL, 0, 18, true, false, false, Arrays.asList("left_shoryukens")),
	RIGHT_SHORYUKEN(RecognizerManager.NUM_ONE_HANDED_PLAYER_GESTURE_NODES, ActionType.SHORYUKEN_ATTACK, GestureGenre.SPECIAL, 0, 18, false, true, false, Arrays.asList("right_shoryukens")),
	SONIC_BOOM(RecognizerManager.NUM_TWO_HANDED_PLAYER_GESTURE_NODES, ActionType.SONIC_BOOM_ATTACK, GestureGenre.SPECIAL, 0, 12, true, true, false, Arrays.asList("sonic_booms")),
	DOUBLE_LARIAT(RecognizerManager.NUM_TWO_HANDED_PLAYER_GESTURE_NODES, ActionType.DOUBLE_LARIAT_ATTACK, GestureGenre.SPECIAL, 0, 30, true, true, false, Arrays.asList("double_lariats")),
	//This gets confused with the double lariat way too easily
	//QUADRUPLE_LARIAT(6, PlayerActionType.QUADRUPLE_LARIAT_ATTACK, GestureGenre.SPECIAL, 0, 55, true, true, Arrays.asList("quadruple_lariats")),
	SUMO_HEADBUTT(RecognizerManager.NUM_TWO_HANDED_PLAYER_GESTURE_NODES, ActionType.SUMO_HEADBUTT_ATTACK, GestureGenre.SPECIAL, 0, 10, true, true, false, Arrays.asList("sumo_headbutts")),
	LEFT_ONE_HUNDRED_HAND_SLAP(RecognizerManager.NUM_ONE_HANDED_PLAYER_GESTURE_NODES, ActionType.ONE_HUNDRED_HAND_SLAP_ATTACK, GestureGenre.SPECIAL, 0, 25, true, false, false, Arrays.asList("left_100_hand_slaps", "left_one_hundred_hand_slaps")),
	RIGHT_ONE_HUNDRED_HAND_SLAP(RecognizerManager.NUM_ONE_HANDED_PLAYER_GESTURE_NODES, ActionType.ONE_HUNDRED_HAND_SLAP_ATTACK, GestureGenre.SPECIAL, 0, 25, false, true, false, Arrays.asList("right_100_hand_slaps", "right_one_hundred_hand_slaps")),
	TWO_HANDED_ONE_HUNDRED_HAND_SLAP(RecognizerManager.NUM_TWO_HANDED_PLAYER_GESTURE_NODES, ActionType.ONE_HUNDRED_HAND_SLAP_ATTACK, GestureGenre.SPECIAL, 0, 25, true, true, false, Arrays.asList("two_handed_100_hand_slaps", "two_handed_one_hundred_hand_slaps")),
	PSYCHO_CRUSHER(RecognizerManager.NUM_TWO_HANDED_PLAYER_GESTURE_NODES, ActionType.PSYCHO_CRUSHER_ATTACK, GestureGenre.SPECIAL, 0, 25, true, true, false, Arrays.asList("psycho_crushers")),

	YMCA(RecognizerManager.NUM_TWO_HANDED_PLAYER_GESTURE_NODES, ActionType.YMCA_ATTACK, GestureGenre.EASTER_EGG, 0, 45, true, true, false, Arrays.asList("ymcas")),
	NYAN_CAT(RecognizerManager.NUM_TWO_HANDED_PLAYER_GESTURE_NODES, ActionType.NYAN_CAT_ATTACK, GestureGenre.EASTER_EGG, 0, 20, true, true, false, Arrays.asList("nyan_cats")),
	// Disco Stu interferes with everything... ugh
	//DISCO_STU(RecognizerManager.NUM_ONE_HANDED_NODES, PlayerActionType.DISCO_STU_ATTACK, GestureGenre.EASTER_EGG, 0, 14, true, true, Arrays.asList("disco_stus")),
	ARM_WINDMILL(RecognizerManager.NUM_TWO_HANDED_PLAYER_GESTURE_NODES, ActionType.ARM_WINDMILL_ATTACK, GestureGenre.EASTER_EGG, 0, 20, true, true, false, Arrays.asList("arm_windmills")),
	SUCK_IT(RecognizerManager.NUM_TWO_HANDED_PLAYER_GESTURE_NODES, ActionType.SUCK_IT_ATTACK, GestureGenre.EASTER_EGG, 0, 8, true, true, false, Arrays.asList("suck_its")),
	//LEFT_VAFANAPOLI_ATTACK(RecognizerManager.NUM_ONE_HANDED_PLAYER_GESTURE_NODES, ActionType.VAFANAPOLI_ATTACK, GestureGenre.EASTER_EGG, 0, 9, true, false, false, Arrays.asList("left_vafanapolis")),
	//RIGHT_VAFANAPOLI_ATTACK(RecognizerManager.NUM_ONE_HANDED_PLAYER_GESTURE_NODES, ActionType.VAFANAPOLI_ATTACK, GestureGenre.EASTER_EGG, 0, 9, false, true, false, Arrays.asList("right_vafanapolis")),
	
	RINGMASTER_LEFT_HALF_RING(RecognizerManager.NUM_ONE_HANDED_RINGMASTER_GESTURE_NODES, ActionType.RINGMASTER_HALF_RING_ACTION, GestureGenre.BASIC, 0, 7, true, false, true, Arrays.asList("rm_left_half_rings", "ringmaster_left_half_rings", "left_uppercuts")),
	RINGMASTER_RIGHT_HALF_RING(RecognizerManager.NUM_ONE_HANDED_RINGMASTER_GESTURE_NODES, ActionType.RINGMASTER_HALF_RING_ACTION, GestureGenre.BASIC, 0, 7, false, true, true, Arrays.asList("rm_right_half_rings", "ringmaster_right_half_rings", "right_uppercuts")),
	RINGMASTER_LEFT_JAB(RecognizerManager.NUM_ONE_HANDED_RINGMASTER_GESTURE_NODES, ActionType.RINGMASTER_JAB_ACTION, GestureGenre.BASIC, 0, 7, true, false, true, Arrays.asList("rm_left_jabs", "ringmaster_left_jabs", "left_jabs")),
	RINGMASTER_RIGHT_JAB(RecognizerManager.NUM_ONE_HANDED_RINGMASTER_GESTURE_NODES, ActionType.RINGMASTER_JAB_ACTION, GestureGenre.BASIC, 0, 7, false, true, true, Arrays.asList("rm_right_jabs", "ringmaster_right_jabs", "right_jabs")),
	RINGMASTER_ERUPTION(RecognizerManager.NUM_TWO_HANDED_RINGMASTER_GESTURE_NODES, ActionType.RINGMASTER_ERUPTION_ACTION, GestureGenre.BASIC, 0, 10, true, true, true, Arrays.asList("rm_eruptions", "ringmaster_eruptions")),
	RINGMASTER_LEFT_CIRCLE(RecognizerManager.NUM_ONE_HANDED_RINGMASTER_GESTURE_NODES, ActionType.RINGMASTER_CIRCLE_ACTION, GestureGenre.BASIC, 0, 12, true, false, true, Arrays.asList("rm_left_circles", "ringmaster_left_circles")),
	RINGMASTER_RIGHT_CIRCLE(RecognizerManager.NUM_ONE_HANDED_RINGMASTER_GESTURE_NODES, ActionType.RINGMASTER_CIRCLE_ACTION, GestureGenre.BASIC, 0, 12, false, true, true, Arrays.asList("rm_right_circles", "ringmaster_right_circles")),
	RINGMASTER_HADOUKEN(RecognizerManager.NUM_TWO_HANDED_RINGMASTER_GESTURE_NODES, ActionType.RINGMASTER_HADOUKEN_ACTION, GestureGenre.BASIC, 0, 10, true, true, true, Arrays.asList("rm_hadoukens", "ringmaster_hadoukens", "hadoukens", "sumo_headbutts")),
	RINGMASTER_DRUM(RecognizerManager.NUM_TWO_HANDED_RINGMASTER_GESTURE_NODES, ActionType.RINGMASTER_DRUM_ACTION, GestureGenre.BASIC, 0, 10, true, true, true, Arrays.asList("rm_drums", "ringmaster_drums", "two_handed_one_hundred_hand_slaps"));
	
	final private ActionType actionFactoryType; // The corresponding gamemodel factory type for when
													  // it comes time to build the gesture for the gamemodel
	
	final private boolean isRingmasterGesture; // Whether this is a ringmaster gesture or not
	
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
	
	GestureType(int numHmmNodes, ActionType actionFactoryType,
			    GestureGenre genre,
				double minimumFiercenessDiff,
				int minimumNumDataPts,
			    boolean leftHand, boolean rightHand, boolean isRingmasterGesture,
			    List<String> parentDirNames) {
		
		assert(numHmmNodes > 0);
		assert(minimumNumDataPts > 0);
		assert(leftHand || rightHand);
		assert(actionFactoryType != null);
		
		this.actionFactoryType = actionFactoryType;
		
		this.isRingmasterGesture = isRingmasterGesture;
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
	
	public ActionType getActionFactoryType() {
		return this.actionFactoryType;
	}
	public int getNumHands() {
		return this.numHands;
	}
	public boolean getIsRingmasterGesture() {
		return this.isRingmasterGesture;
	}
	public boolean getUsesLeftHand() {
		return this.leftHanded;
	}
	public boolean getUsesRightHand() {
		return this.rightHanded;
	}
	public boolean getIsTwoHanded() {
		return this.getUsesLeftHand() && this.getUsesRightHand();
	}
	public boolean getIsOneHanded() {
		return !this.getIsTwoHanded();
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
