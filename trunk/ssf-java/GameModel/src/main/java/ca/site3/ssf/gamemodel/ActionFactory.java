package ca.site3.ssf.gamemodel;


import java.util.List;
import java.util.ArrayList;
import java.util.Random;

import ca.site3.ssf.common.MultiLerp;
import ca.site3.ssf.gamemodel.PlayerAttackAction.AttackType;

/**
 * Publicly exposed factory class for building the various actions/moves for players and
 * the ringmaster in the SSF game.
 * @author Callum
 *
 */
final public class ActionFactory {
	
	public enum ActionType {
		// Basic moves
		BLOCK(true),
		JAB_ATTACK(true),
		HOOK_ATTACK(true),
		UPPERCUT_ATTACK(true),
		CHOP_ATTACK(true),
		
		// Special moves
		HADOUKEN_ATTACK(true),
		SHORYUKEN_ATTACK(true),
		SONIC_BOOM_ATTACK(true),
		DOUBLE_LARIAT_ATTACK(true),
		//QUADRUPLE_LARIAT_ATTACK(true),
		SUMO_HEADBUTT_ATTACK(true),
		ONE_HUNDRED_HAND_SLAP_ATTACK(true),
		PSYCHO_CRUSHER_ATTACK(true),
		
		// Easter egg moves
		YMCA_ATTACK(true),
		NYAN_CAT_ATTACK(true),
		//DISCO_STU_ATTACK(true),
		ARM_WINDMILL_ATTACK(true),
		SUCK_IT_ATTACK(true),
		//VAFANAPOLI_ATTACK(true),
		
		// Ringmaster moves
		RINGMASTER_HALF_RING_ACTION(false),
		RINGMASTER_JAB_ACTION(false),
		RINGMASTER_ERUPTION_ACTION(false),
		RINGMASTER_CIRCLE_ACTION(false),
		RINGMASTER_HADOUKEN_ACTION(false),
		RINGMASTER_DRUM_ACTION(false);
		
		final private boolean isPlayerAction;
		
		ActionType(boolean isPlayerAction) {
			this.isPlayerAction = isPlayerAction;
		}
		
		public boolean getIsPlayerAction() {
			return this.isPlayerAction;
		}
		
	};
	
	final static private double EPSILON = 0.000000001;

	final static public double DEFAULT_FULL_ON_FRACTION  = 0.50;
	final static public double DEFAULT_FULL_OFF_FRACTION = 0.50;
	
	final private GameModel gameModel;
	
	ActionFactory(GameModel gameModel) {
		this.gameModel = gameModel;
		assert(this.gameModel != null);
	}
	
	/**
	 * Builds an enumerated player action type.
	 * @param playerNum The player who is initiating the action.
	 * @param playerActionType The type of player action.
	 * @param leftHand Whether the player's left hand is being used in the action.
	 * @param rightHand Whether the player's right hand is being used in the action.
	 * @return The resulting action, null on failure.
	 */
	final public Action buildPlayerAction(int playerNum, ActionType playerActionType,
			                              boolean leftHand, boolean rightHand) {
		
		// Player number should be valid
		if (playerNum != 1 && playerNum != 2) {
			assert(false);
			return null;
		}
		
		Action action = null;
		
		FireEmitterModel fireEmitterModel    = this.gameModel.getFireEmitterModel();
		FireEmitterConfig fireEmitterConfig  = fireEmitterModel.getConfig();
		
		Player blockerOrAttacker = this.gameModel.getPlayer(playerNum);
		Player attackee = this.gameModel.getPlayer(Player.getOpposingPlayerNum(playerNum));
		
		boolean success = true;
		
		switch (playerActionType) {
			case BLOCK:
				action = new PlayerBlockAction(fireEmitterModel, blockerOrAttacker);
				
				// Doesn't matter if it's left or right handed, a block will block
				// attacks coming in on both rails...
				final int BLOCK_WIDTH = 1;
				final int BLOCK_NUM_BURSTS = 1;
				final double BLOCK_DURATION_IN_SECS = 2.0;
				final double BLOCK_PERCENT_ON = 0.99;
				final double BLOCK_PERCENT_OFF = 1.0 - BLOCK_PERCENT_ON;
				final double BLOCK_DELAY = 0.0;
				
				// Surround the player in flames
				// Turn on the flames directly infront of the player on the rails
				success &= this.addBurstToAction(action, fireEmitterModel.getPlayerLeftHandStartEmitterIter(playerNum), 
						BLOCK_WIDTH, BLOCK_NUM_BURSTS, BLOCK_DURATION_IN_SECS, BLOCK_PERCENT_ON, BLOCK_PERCENT_OFF, BLOCK_DELAY);
				success &= this.addBurstToAction(action, fireEmitterModel.getPlayerRightHandStartEmitterIter(playerNum), 
						BLOCK_WIDTH, BLOCK_NUM_BURSTS, BLOCK_DURATION_IN_SECS, BLOCK_PERCENT_ON, BLOCK_PERCENT_OFF, BLOCK_DELAY);
				
				// Turn on the outer ring emitters to either side behind the player
				success &= this.addBurstToAction(action, 
						fireEmitterModel.getOuterRingStartEmitterIter(fireEmitterModel.getSemanticOuterRingEmitterIndex(playerNum, true, 0), true),
						BLOCK_WIDTH, BLOCK_NUM_BURSTS, BLOCK_DURATION_IN_SECS, BLOCK_PERCENT_ON, BLOCK_PERCENT_OFF, BLOCK_DELAY);
				success &= this.addBurstToAction(action, 
						fireEmitterModel.getOuterRingStartEmitterIter(fireEmitterModel.getSemanticOuterRingEmitterIndex(playerNum, false, 0), false),
						BLOCK_WIDTH, BLOCK_NUM_BURSTS, BLOCK_DURATION_IN_SECS, BLOCK_PERCENT_ON, BLOCK_PERCENT_OFF, BLOCK_DELAY);
				break;
				
			case JAB_ATTACK: {
				assert(leftHand || rightHand);
				
				final double JAB_BASE_ACCELERATION   = 0.0;
				final double JAB_TIME_LENGTH_IN_SECS = 1.5;
				final float JAB_DAMAGE_PER_FLAME     = 3.5f;
				final int JAB_NUM_FLAMES             = 1;
				
				if (leftHand) {
					action = new PlayerAttackAction(fireEmitterModel, PlayerAttackAction.AttackType.LEFT_JAB_ATTACK, blockerOrAttacker, attackee, JAB_DAMAGE_PER_FLAME);
					success &= this.addAcceleratingWaveToAction(action, fireEmitterModel.getPlayerLeftHandStartEmitterIter(playerNum), fireEmitterConfig.getNumEmittersPerRail(),
							JAB_NUM_FLAMES, JAB_TIME_LENGTH_IN_SECS, JAB_BASE_ACCELERATION, DEFAULT_FULL_ON_FRACTION, DEFAULT_FULL_OFF_FRACTION, 0.0);
				}
				else {
					action = new PlayerAttackAction(fireEmitterModel, PlayerAttackAction.AttackType.RIGHT_JAB_ATTACK, blockerOrAttacker, attackee, JAB_DAMAGE_PER_FLAME);
					success &= this.addAcceleratingWaveToAction(action, fireEmitterModel.getPlayerRightHandStartEmitterIter(playerNum), fireEmitterConfig.getNumEmittersPerRail(),
							JAB_NUM_FLAMES, JAB_TIME_LENGTH_IN_SECS, JAB_BASE_ACCELERATION, DEFAULT_FULL_ON_FRACTION, DEFAULT_FULL_OFF_FRACTION, 0.0);
				}
				break;
			}
			
			// Both hook and uppercut get mistaken A LOT in the gesture recognition, so I've made both attacks do the same thing :P
			case HOOK_ATTACK:
			case UPPERCUT_ATTACK: {
				assert(leftHand || rightHand);
				
				final double BASE_ACCELERATION   = 0.0;
				final double TIME_LENGTH_IN_SECS = 2.25;
				final float DAMAGE_PER_FLAME     = 5.0f;
				final int NUM_FLAMES             = 1;
				
				if (leftHand) {
					action = new PlayerAttackAction(fireEmitterModel, PlayerAttackAction.AttackType.LEFT_HOOK_ATTACK, blockerOrAttacker, attackee, DAMAGE_PER_FLAME);
					success &= this.addAcceleratingWaveToAction(action, fireEmitterModel.getPlayerLeftHandStartEmitterIter(playerNum), fireEmitterConfig.getNumEmittersPerRail(),
							NUM_FLAMES, TIME_LENGTH_IN_SECS, BASE_ACCELERATION, DEFAULT_FULL_ON_FRACTION, DEFAULT_FULL_OFF_FRACTION, 0.0);
				}
				else {
					action = new PlayerAttackAction(fireEmitterModel, PlayerAttackAction.AttackType.RIGHT_HOOK_ATTACK, blockerOrAttacker, attackee, DAMAGE_PER_FLAME);
					success &= this.addAcceleratingWaveToAction(action, fireEmitterModel.getPlayerRightHandStartEmitterIter(playerNum), fireEmitterConfig.getNumEmittersPerRail(),
							NUM_FLAMES, TIME_LENGTH_IN_SECS, BASE_ACCELERATION, DEFAULT_FULL_ON_FRACTION, DEFAULT_FULL_OFF_FRACTION, 0.0);
				}
				break;
			}
			
			case CHOP_ATTACK: {
				assert(leftHand || rightHand);
				
				final double CHOP_BASE_ACCELERATION   = 0.25;
				final double CHOP_TIME_LENGTH_IN_SECS = 1.8;
				final float CHOP_DAMAGE_PER_FLAME     = 4.0f;
				final int CHOP_NUM_FLAMES             = 1;
				
				if (leftHand) {
					action = new PlayerAttackAction(fireEmitterModel, PlayerAttackAction.AttackType.LEFT_CHOP_ATTACK, blockerOrAttacker, attackee, CHOP_DAMAGE_PER_FLAME);
					success &= this.addAcceleratingWaveToAction(action, fireEmitterModel.getPlayerLeftHandStartEmitterIter(playerNum), fireEmitterConfig.getNumEmittersPerRail(),
							CHOP_NUM_FLAMES, CHOP_TIME_LENGTH_IN_SECS, CHOP_BASE_ACCELERATION, DEFAULT_FULL_ON_FRACTION, DEFAULT_FULL_OFF_FRACTION, 0.0);
				}
				else {
					action = new PlayerAttackAction(fireEmitterModel, PlayerAttackAction.AttackType.RIGHT_CHOP_ATTACK, blockerOrAttacker, attackee, CHOP_DAMAGE_PER_FLAME);
					success &= this.addAcceleratingWaveToAction(action, fireEmitterModel.getPlayerRightHandStartEmitterIter(playerNum), fireEmitterConfig.getNumEmittersPerRail(),
							CHOP_NUM_FLAMES, CHOP_TIME_LENGTH_IN_SECS, CHOP_BASE_ACCELERATION, DEFAULT_FULL_ON_FRACTION, DEFAULT_FULL_OFF_FRACTION, 0.0);
				}
				break;
			}
			
			case HADOUKEN_ATTACK: {
				
				final double HADOUKEN_BASE_ACCELERATION   = 0.0;
				final double HADOUKEN_TIME_LENGTH_IN_SECS = 3.0;
				final float HADOUKEN_DAMAGE_PER_FLAME     = 3.0f;
				final int HADOUKEN_NUM_FLAMES             = 2;
				
				action = this.buildPlayerTwoHandedSymetricalAttack(PlayerAttackAction.AttackType.HADOUKEN_ATTACK, playerNum,
						HADOUKEN_TIME_LENGTH_IN_SECS, HADOUKEN_BASE_ACCELERATION, HADOUKEN_NUM_FLAMES, HADOUKEN_DAMAGE_PER_FLAME);
				
				// Decoration on the outer ring of flame effects: Two-flame-wide wave starting at the attacking player
				// and moving to behind the opposing player
				success &= this.addConstantVelocityWaveToAction(action,
						fireEmitterModel.getOuterRingStartEmitterIter(
								fireEmitterModel.getSemanticOuterRingEmitterIndex(playerNum, true, 0), true),
						fireEmitterConfig.getNumOuterRingEmitters()/2, 1, HADOUKEN_TIME_LENGTH_IN_SECS,
						DEFAULT_FULL_ON_FRACTION, DEFAULT_FULL_OFF_FRACTION, 0.0);
				success &= this.addConstantVelocityWaveToAction(action,
						fireEmitterModel.getOuterRingStartEmitterIter(
								fireEmitterModel.getSemanticOuterRingEmitterIndex(playerNum, false, 0), false),
						fireEmitterConfig.getNumOuterRingEmitters()/2, 1, HADOUKEN_TIME_LENGTH_IN_SECS,
						DEFAULT_FULL_ON_FRACTION, DEFAULT_FULL_OFF_FRACTION, 0.0);
				break;
			}
			
			case SHORYUKEN_ATTACK: {
				
				final double SHORYUKEN_BASE_ACCELERATION   = 0.25;
				final double SHORYUKEN_TIME_LENGTH_IN_SECS = 3.0;
				final float SHORYUKEN_DAMAGE_PER_FLAME    = 6.0f;
				final int SHORYUKEN_NUM_FLAMES_PUNCH_HAND = 2;
				final int SHORYUKEN_NUM_FLAMES_OFFHAND    = 1;

				if (leftHand) {
					action = new PlayerAttackAction(fireEmitterModel, PlayerAttackAction.AttackType.LEFT_SHORYUKEN_ATTACK, blockerOrAttacker, attackee, SHORYUKEN_DAMAGE_PER_FLAME);
					
					success &= this.addAcceleratingWaveToAction(action, fireEmitterModel.getPlayerLeftHandStartEmitterIter(playerNum), fireEmitterConfig.getNumEmittersPerRail(),
							SHORYUKEN_NUM_FLAMES_PUNCH_HAND, SHORYUKEN_TIME_LENGTH_IN_SECS, SHORYUKEN_BASE_ACCELERATION,
							DEFAULT_FULL_ON_FRACTION, DEFAULT_FULL_OFF_FRACTION, 0.0);
					success &= this.addAcceleratingWaveToAction(action, fireEmitterModel.getPlayerRightHandStartEmitterIter(playerNum), fireEmitterConfig.getNumEmittersPerRail(),
							SHORYUKEN_NUM_FLAMES_OFFHAND, SHORYUKEN_TIME_LENGTH_IN_SECS, SHORYUKEN_BASE_ACCELERATION,
							DEFAULT_FULL_ON_FRACTION, DEFAULT_FULL_OFF_FRACTION, 0.0);
					
					// Decoration on the outer ring is similar to the rails: Have flames start at the attacker and move to the attackee, but have it off
					// balanced based on the dominant hand...
					success &= this.addConstantVelocityWaveToAction(action,
							fireEmitterModel.getOuterRingStartEmitterIter(
									fireEmitterModel.getSemanticOuterRingEmitterIndex(playerNum, true, 0), true),
							fireEmitterConfig.getNumOuterRingEmitters()/2, SHORYUKEN_NUM_FLAMES_PUNCH_HAND, SHORYUKEN_TIME_LENGTH_IN_SECS,
							DEFAULT_FULL_ON_FRACTION, DEFAULT_FULL_OFF_FRACTION, 0.0);
					success &= this.addConstantVelocityWaveToAction(action,
							fireEmitterModel.getOuterRingStartEmitterIter(
									fireEmitterModel.getSemanticOuterRingEmitterIndex(playerNum, false, 0), false),
							fireEmitterConfig.getNumOuterRingEmitters()/2, SHORYUKEN_NUM_FLAMES_OFFHAND, SHORYUKEN_TIME_LENGTH_IN_SECS,
							DEFAULT_FULL_ON_FRACTION, DEFAULT_FULL_OFF_FRACTION, 0.0);
				}
				else {
					action = new PlayerAttackAction(fireEmitterModel, PlayerAttackAction.AttackType.RIGHT_SHORYUKEN_ATTACK, blockerOrAttacker, attackee, SHORYUKEN_DAMAGE_PER_FLAME);
					
					success &= this.addAcceleratingWaveToAction(action, fireEmitterModel.getPlayerRightHandStartEmitterIter(playerNum), fireEmitterConfig.getNumEmittersPerRail(),
							SHORYUKEN_NUM_FLAMES_PUNCH_HAND, SHORYUKEN_TIME_LENGTH_IN_SECS, SHORYUKEN_BASE_ACCELERATION,
							DEFAULT_FULL_ON_FRACTION, DEFAULT_FULL_OFF_FRACTION, 0.0);
					success &= this.addAcceleratingWaveToAction(action, fireEmitterModel.getPlayerLeftHandStartEmitterIter(playerNum), fireEmitterConfig.getNumEmittersPerRail(),
							SHORYUKEN_NUM_FLAMES_OFFHAND, SHORYUKEN_TIME_LENGTH_IN_SECS, SHORYUKEN_BASE_ACCELERATION,
							DEFAULT_FULL_ON_FRACTION, DEFAULT_FULL_OFF_FRACTION, 0.0);
					
					// Decoration on the outer ring is similar to the rails: Have flames start at the attacker and move to the attackee, but have it off
					// balanced based on the dominant hand...
					success &= this.addConstantVelocityWaveToAction(action,
							fireEmitterModel.getOuterRingStartEmitterIter(
									fireEmitterModel.getSemanticOuterRingEmitterIndex(playerNum, false, 0), true),
							fireEmitterConfig.getNumOuterRingEmitters()/2, SHORYUKEN_NUM_FLAMES_PUNCH_HAND, SHORYUKEN_TIME_LENGTH_IN_SECS,
							DEFAULT_FULL_ON_FRACTION, DEFAULT_FULL_OFF_FRACTION, 0.0);
					success &= this.addConstantVelocityWaveToAction(action,
							fireEmitterModel.getOuterRingStartEmitterIter(
									fireEmitterModel.getSemanticOuterRingEmitterIndex(playerNum, true, 0), false),
							fireEmitterConfig.getNumOuterRingEmitters()/2, SHORYUKEN_NUM_FLAMES_OFFHAND, SHORYUKEN_TIME_LENGTH_IN_SECS,
							DEFAULT_FULL_ON_FRACTION, DEFAULT_FULL_OFF_FRACTION, 0.0);
				}
				
				break;
			}
			
			case SONIC_BOOM_ATTACK: {
				
				final double SONIC_BOOM_BASE_ACCELERATION   = 0.0;
				final double SONIC_BOOM_TIME_LENGTH_IN_SECS = 2.0;
				final float SONIC_BOOM_DAMAGE_PER_FLAME     = 3.5f;
				final int SONIC_BOOM_NUM_FLAMES             = 1;
				
				action = this.buildPlayerTwoHandedSymetricalAttack(PlayerAttackAction.AttackType.SONIC_BOOM_ATTACK, playerNum,
						SONIC_BOOM_TIME_LENGTH_IN_SECS, SONIC_BOOM_BASE_ACCELERATION,
						SONIC_BOOM_NUM_FLAMES, SONIC_BOOM_DAMAGE_PER_FLAME);
				
				// Decoration on the outer ring of flame effects: One-flame-wide wave starting at the attacking player
				// and moving to behind the opposing player
				success &= this.addConstantVelocityWaveToAction(action,
						fireEmitterModel.getOuterRingStartEmitterIter(
								fireEmitterModel.getSemanticOuterRingEmitterIndex(playerNum, true, 0), true),
						fireEmitterConfig.getNumOuterRingEmitters()/2, 1, SONIC_BOOM_TIME_LENGTH_IN_SECS,
						DEFAULT_FULL_ON_FRACTION, DEFAULT_FULL_OFF_FRACTION, 0.0);
				success &= this.addConstantVelocityWaveToAction(action,
						fireEmitterModel.getOuterRingStartEmitterIter(
								fireEmitterModel.getSemanticOuterRingEmitterIndex(playerNum, false, 0), false),
						fireEmitterConfig.getNumOuterRingEmitters()/2, 1, SONIC_BOOM_TIME_LENGTH_IN_SECS,
						DEFAULT_FULL_ON_FRACTION, DEFAULT_FULL_OFF_FRACTION, 0.0);
				
				break;
			}
			
			case DOUBLE_LARIAT_ATTACK: {
				
				final double DOUBLE_LARIAT_BASE_ACCELERATION   = 0.0;
				final double DOUBLE_LARIAT_TIME_LENGTH_IN_SECS = 4.0;
				final float DOUBLE_LARIAT_DAMAGE_PER_FLAME     = 3.5f;
				final int DOUBLE_LARIAT_NUM_FLAMES             = 2;
				
				action = this.buildPlayerTwoHandedSymetricalAttack(PlayerAttackAction.AttackType.DOUBLE_LARIAT_ATTACK, playerNum,
						DOUBLE_LARIAT_TIME_LENGTH_IN_SECS, DOUBLE_LARIAT_BASE_ACCELERATION,
						DOUBLE_LARIAT_NUM_FLAMES, DOUBLE_LARIAT_DAMAGE_PER_FLAME);
				
				// Decorate the outer ring with fire effects that move in a twin, staggered spiral around the ring
				final int NUM_OUTER_RING_CYCLES = 1;
				success &= this.addConstantVelocityWaveToAction(action,
						fireEmitterModel.getOuterRingStartEmitterIter(
								fireEmitterModel.getSemanticOuterRingEmitterIndex(playerNum, true, 0), true),
						NUM_OUTER_RING_CYCLES * fireEmitterConfig.getNumOuterRingEmitters(), 1, DOUBLE_LARIAT_TIME_LENGTH_IN_SECS,
						DEFAULT_FULL_ON_FRACTION, DEFAULT_FULL_OFF_FRACTION, 0.0);
				success &= this.addConstantVelocityWaveToAction(action,
						fireEmitterModel.getOuterRingStartEmitterIter(
								fireEmitterModel.getSemanticOuterRingEmitterIndex(attackee.getPlayerNumber(), true, 0), true),
						NUM_OUTER_RING_CYCLES * fireEmitterConfig.getNumOuterRingEmitters(), 1, DOUBLE_LARIAT_TIME_LENGTH_IN_SECS,
						DEFAULT_FULL_ON_FRACTION, DEFAULT_FULL_OFF_FRACTION, 0.0);
				
				break;
			}
			
			/*
			case QUADRUPLE_LARIAT_ATTACK: {
				final double QUADRUPLE_LARIAT_BASE_ACCELERATION   = 0.0;
				final double QUADRUPLE_LARIAT_TIME_LENGTH_IN_SECS = 7.5;
				final float QUADRUPLE_LARIAT_DAMAGE_PER_FLAME     = 3.0f;
				final int QUADRUPLE_LARIAT_NUM_FLAMES             = 4;
				
				action = this.buildPlayerTwoHandedSymetricalAttack(PlayerAttackAction.AttackType.QUADRUPLE_LARIAT_ATTACK, playerNum,
						QUADRUPLE_LARIAT_TIME_LENGTH_IN_SECS, QUADRUPLE_LARIAT_BASE_ACCELERATION,
						QUADRUPLE_LARIAT_NUM_FLAMES, QUADRUPLE_LARIAT_DAMAGE_PER_FLAME);
				
				// Decorate the outer ring with fire effects that move in a twin, staggered spiral around the ring
				final int NUM_OUTER_RING_CYCLES = 4;
				success &= this.addConstantVelocityWaveToAction(action,
						fireEmitterModel.getOuterRingStartEmitterIter(
								fireEmitterModel.getSemanticOuterRingEmitterIndex(playerNum, true, 0), true),
						NUM_OUTER_RING_CYCLES * fireEmitterConfig.getNumOuterRingEmitters(), 2, QUADRUPLE_LARIAT_TIME_LENGTH_IN_SECS,
						DEFAULT_FULL_ON_FRACTION, DEFAULT_FULL_OFF_FRACTION, 0.0);
				success &= this.addConstantVelocityWaveToAction(action,
						fireEmitterModel.getOuterRingStartEmitterIter(
								fireEmitterModel.getSemanticOuterRingEmitterIndex(attackee.getPlayerNumber(), true, 0), true),
						NUM_OUTER_RING_CYCLES * fireEmitterConfig.getNumOuterRingEmitters(), 2, QUADRUPLE_LARIAT_TIME_LENGTH_IN_SECS,
						DEFAULT_FULL_ON_FRACTION, DEFAULT_FULL_OFF_FRACTION, 0.0);
				
				break;
			}
			*/
			
			case SUMO_HEADBUTT_ATTACK: {
				
				final double SUMO_HEADBUTT_BASE_ACCELERATION   = 0.0;
				final double SUMO_HEADBUTT_TIME_LENGTH_IN_SECS = 3.0;
				final float SUMO_HEADBUTT_DAMAGE_PER_FLAME     = 2.0f;
				final int SUMO_HEADBUTT_NUM_FLAMES             = 1;
				
				action = this.buildPlayerTwoHandedSymetricalAttack(PlayerAttackAction.AttackType.SUMO_HEADBUTT_ATTACK, playerNum,
						SUMO_HEADBUTT_TIME_LENGTH_IN_SECS, SUMO_HEADBUTT_BASE_ACCELERATION,
						SUMO_HEADBUTT_NUM_FLAMES, SUMO_HEADBUTT_DAMAGE_PER_FLAME);
				
				// Decoration on the outer ring of flame effects: One-flame-wide wave starting at the attacking player
				// and moving to behind the opposing player
				success &= this.addConstantVelocityWaveToAction(action,
						fireEmitterModel.getOuterRingStartEmitterIter(
								fireEmitterModel.getSemanticOuterRingEmitterIndex(playerNum, true, 0), true),
						fireEmitterConfig.getNumOuterRingEmitters()/2, 1, SUMO_HEADBUTT_TIME_LENGTH_IN_SECS,
						DEFAULT_FULL_ON_FRACTION, DEFAULT_FULL_OFF_FRACTION, 0.0);
				success &= this.addConstantVelocityWaveToAction(action,
						fireEmitterModel.getOuterRingStartEmitterIter(
								fireEmitterModel.getSemanticOuterRingEmitterIndex(playerNum, false, 0), false),
						fireEmitterConfig.getNumOuterRingEmitters()/2, 1, SUMO_HEADBUTT_TIME_LENGTH_IN_SECS,
						DEFAULT_FULL_ON_FRACTION, DEFAULT_FULL_OFF_FRACTION, 0.0);
				
				break;
			}
			
			case ONE_HUNDRED_HAND_SLAP_ATTACK: {
				
				final double ONE_HUND_HAND_SLAP_BASE_ACCELERATION   = 0.0;
				final double ONE_HUND_HAND_SLAP_TIME_LENGTH_IN_SECS = 3.0;
				final float ONE_HUND_HAND_SLAP_DAMAGE_PER_FLAME     = 2.0f;
				final int ONE_HUND_HAND_SLAP_NUM_FLAMES             = 3;
				
				final int TWO_HANDED_NUM_RANDOM_BURSTS = 12;
				final int ONE_HANDED_NUM_RANDOM_BURSTS = TWO_HANDED_NUM_RANDOM_BURSTS / 2;
				
				Random randomNumGen = new Random();
				double currDelayCount = ONE_HUND_HAND_SLAP_TIME_LENGTH_IN_SECS * 0.1;
				
				if (leftHand && rightHand) {
					action = this.buildPlayerTwoHandedSymetricalAttack(PlayerAttackAction.AttackType.TWO_HANDED_ONE_HUNDRED_HAND_SLAP_ATTACK, playerNum,
							ONE_HUND_HAND_SLAP_TIME_LENGTH_IN_SECS, ONE_HUND_HAND_SLAP_BASE_ACCELERATION,
							ONE_HUND_HAND_SLAP_NUM_FLAMES, ONE_HUND_HAND_SLAP_DAMAGE_PER_FLAME);
				
					// Decoration in the outer ring of fire emitters is random bursts all over the ring on the 6 closest
					// emitters to the player being attacked					
					final double TIME_PER_RANDOM_BURST = (ONE_HUND_HAND_SLAP_TIME_LENGTH_IN_SECS-currDelayCount) / (double)TWO_HANDED_NUM_RANDOM_BURSTS;
					
					for (int i = 0; i < TWO_HANDED_NUM_RANDOM_BURSTS; i++) {
						success &= this.addBurstToAction(action, fireEmitterModel.getOuterRingStartEmitterIter(
								fireEmitterModel.getSemanticOuterRingEmitterIndex(attackee.getPlayerNumber(), randomNumGen.nextBoolean(), i % 2), true),
								1, 1, TIME_PER_RANDOM_BURST, DEFAULT_FULL_ON_FRACTION, DEFAULT_FULL_OFF_FRACTION, currDelayCount);
						currDelayCount += TIME_PER_RANDOM_BURST;
					}
					
				}
				else {

					if (leftHand) {
						action = new PlayerAttackAction(fireEmitterModel, PlayerAttackAction.AttackType.LEFT_ONE_HUNDRED_HAND_SLAP_ATTACK,
								blockerOrAttacker, attackee, ONE_HUND_HAND_SLAP_DAMAGE_PER_FLAME);
						
						success &= this.addAcceleratingWaveToAction(action, fireEmitterModel.getPlayerLeftHandStartEmitterIter(playerNum), fireEmitterConfig.getNumEmittersPerRail(),
								ONE_HUND_HAND_SLAP_NUM_FLAMES, ONE_HUND_HAND_SLAP_TIME_LENGTH_IN_SECS, ONE_HUND_HAND_SLAP_BASE_ACCELERATION,
								DEFAULT_FULL_ON_FRACTION, DEFAULT_FULL_OFF_FRACTION, 0.0);
						
						// Decoration in the outer ring of fire emitters is random bursts all over the left side (relative to the player) of the ring
						final double TIME_PER_RANDOM_BURST = (ONE_HUND_HAND_SLAP_TIME_LENGTH_IN_SECS - currDelayCount) / (double)ONE_HANDED_NUM_RANDOM_BURSTS;
						for (int i = 0; i < ONE_HANDED_NUM_RANDOM_BURSTS; i++) {
							success &= this.addBurstToAction(action, fireEmitterModel.getOuterRingStartEmitterIter(
									fireEmitterModel.getSemanticOuterRingEmitterIndex(attackee.getPlayerNumber(), false, i % 2), true),
									1, 1, TIME_PER_RANDOM_BURST, DEFAULT_FULL_ON_FRACTION, DEFAULT_FULL_OFF_FRACTION, currDelayCount);
							currDelayCount += TIME_PER_RANDOM_BURST;
						}
					}
					else {
						action = new PlayerAttackAction(fireEmitterModel, PlayerAttackAction.AttackType.RIGHT_ONE_HUNDRED_HAND_SLAP_ATTACK,
								blockerOrAttacker, attackee, ONE_HUND_HAND_SLAP_DAMAGE_PER_FLAME);
						
						success &= this.addAcceleratingWaveToAction(action, fireEmitterModel.getPlayerRightHandStartEmitterIter(playerNum), fireEmitterConfig.getNumEmittersPerRail(),
								ONE_HUND_HAND_SLAP_NUM_FLAMES, ONE_HUND_HAND_SLAP_TIME_LENGTH_IN_SECS, ONE_HUND_HAND_SLAP_BASE_ACCELERATION,
								DEFAULT_FULL_ON_FRACTION, DEFAULT_FULL_OFF_FRACTION, 0.0);
						
						// Decoration in the outer ring of fire emitters is random bursts all over the right side (relative to the player) of the ring
						final double TIME_PER_RANDOM_BURST = (ONE_HUND_HAND_SLAP_TIME_LENGTH_IN_SECS - currDelayCount) / (double)ONE_HANDED_NUM_RANDOM_BURSTS;
						for (int i = 0; i < ONE_HANDED_NUM_RANDOM_BURSTS; i++) {
						
							success &= this.addBurstToAction(action, fireEmitterModel.getOuterRingStartEmitterIter(
									fireEmitterModel.getSemanticOuterRingEmitterIndex(attackee.getPlayerNumber(), true, i % 2), true),
									1, 1, TIME_PER_RANDOM_BURST, DEFAULT_FULL_ON_FRACTION, DEFAULT_FULL_OFF_FRACTION, currDelayCount);
							currDelayCount += TIME_PER_RANDOM_BURST;
						}
					}
				}
				
				break;
			}
			
			case PSYCHO_CRUSHER_ATTACK: {
				
				final double PSYCHO_CRUSHER_TIME_LENGTH_IN_SECS = 2.25;
				final float PSYCHO_CRUSHER_DAMAGE_PER_FLAME     = 2.5f;
				final int PSYCHO_CRUSHER_NUM_FLAMES             = 3;
				final double PSYCHO_CRUSHER_BASE_ACCELERATION   = 0.0;
				
				action = this.buildPlayerTwoHandedSymetricalAttack(PlayerAttackAction.AttackType.PSYCHO_CRUSHER_ATTACK, playerNum,
						PSYCHO_CRUSHER_TIME_LENGTH_IN_SECS, PSYCHO_CRUSHER_BASE_ACCELERATION,
						PSYCHO_CRUSHER_NUM_FLAMES, PSYCHO_CRUSHER_DAMAGE_PER_FLAME);
				
				// Decoration on the outer ring of flame effects: Two flames starting at the attacking player
				// and moving to behind the opposing player
				success &= this.addConstantVelocityWaveToAction(action,
						fireEmitterModel.getOuterRingStartEmitterIter(
								fireEmitterModel.getSemanticOuterRingEmitterIndex(playerNum, true, 0), true),
						fireEmitterConfig.getNumOuterRingEmitters()/2, PSYCHO_CRUSHER_NUM_FLAMES, PSYCHO_CRUSHER_TIME_LENGTH_IN_SECS,
						DEFAULT_FULL_ON_FRACTION, DEFAULT_FULL_OFF_FRACTION, 0.0);
				success &= this.addConstantVelocityWaveToAction(action,
						fireEmitterModel.getOuterRingStartEmitterIter(
								fireEmitterModel.getSemanticOuterRingEmitterIndex(playerNum, false, 0), false),
						fireEmitterConfig.getNumOuterRingEmitters()/2, PSYCHO_CRUSHER_NUM_FLAMES, PSYCHO_CRUSHER_TIME_LENGTH_IN_SECS,
						DEFAULT_FULL_ON_FRACTION, DEFAULT_FULL_OFF_FRACTION, 0.0);
				
				break;
			}
			
			case YMCA_ATTACK: {
				
				final double YMCA_BASE_ACCELERATION   = 0.0;
				final double YMCA_TIME_LENGTH_IN_SECS = 3.5;
				final float YMCA_DAMAGE_PER_FLAME     = 2.5f;
				final int YMCA_NUM_FLAMES             = 4;
				
				action = this.buildPlayerTwoHandedSymetricalAttack(PlayerAttackAction.AttackType.YMCA_ATTACK, playerNum,
						YMCA_TIME_LENGTH_IN_SECS, YMCA_BASE_ACCELERATION, YMCA_NUM_FLAMES, YMCA_DAMAGE_PER_FLAME);
				
				// Decoration on the outer ring of flame effects: Two flames starting on either side of the player and each travelling
				// around the ring completely
				success &= this.addConstantVelocityWaveToAction(action,
						fireEmitterModel.getOuterRingStartEmitterIter(
								fireEmitterModel.getSemanticOuterRingEmitterIndex(playerNum, true, 0), true),
						fireEmitterConfig.getNumOuterRingEmitters(), 1, YMCA_TIME_LENGTH_IN_SECS*1.2,
						DEFAULT_FULL_ON_FRACTION, DEFAULT_FULL_OFF_FRACTION, 0.0);
				success &= this.addConstantVelocityWaveToAction(action,
						fireEmitterModel.getOuterRingStartEmitterIter(
								fireEmitterModel.getSemanticOuterRingEmitterIndex(playerNum, false, 0), false),
						fireEmitterConfig.getNumOuterRingEmitters(), 1, YMCA_TIME_LENGTH_IN_SECS*1.2,
						DEFAULT_FULL_ON_FRACTION, DEFAULT_FULL_OFF_FRACTION, 0.0);
				
				break;
			}
			
			case NYAN_CAT_ATTACK: {
				
				final double NYAN_CAT_BASE_ACCELERATION   = 0.0;
				final double NYAN_CAT_TIME_LENGTH_IN_SECS = 3.33333333333333333333;
				final float NYAN_CAT_DAMAGE_PER_FLAME     = 1.11111111111111111111f;
				final int NYAN_CAT_NUM_FLAMES_PER_WAVE    = 2;
				final int NYAN_CAT_NUM_WAVES              = 3;
				
				action = new PlayerAttackAction(fireEmitterModel, PlayerAttackAction.AttackType.NYAN_CAT_ATTACK,
						blockerOrAttacker, attackee, NYAN_CAT_DAMAGE_PER_FLAME);
				
				for (int i = 0; i < NYAN_CAT_NUM_WAVES; i++) {
					double currentDelay = i * (NYAN_CAT_TIME_LENGTH_IN_SECS / 1.33);
					
					success &= this.addAcceleratingWaveToAction(action, fireEmitterModel.getPlayerRightHandStartEmitterIter(playerNum), fireEmitterConfig.getNumEmittersPerRail(),
							NYAN_CAT_NUM_FLAMES_PER_WAVE, NYAN_CAT_TIME_LENGTH_IN_SECS, NYAN_CAT_BASE_ACCELERATION,
							DEFAULT_FULL_ON_FRACTION, DEFAULT_FULL_OFF_FRACTION, currentDelay);
					success &= this.addAcceleratingWaveToAction(action, fireEmitterModel.getPlayerLeftHandStartEmitterIter(playerNum), fireEmitterConfig.getNumEmittersPerRail(),
							NYAN_CAT_NUM_FLAMES_PER_WAVE, NYAN_CAT_TIME_LENGTH_IN_SECS, NYAN_CAT_BASE_ACCELERATION,
							DEFAULT_FULL_ON_FRACTION, DEFAULT_FULL_OFF_FRACTION, currentDelay);
				}
				
				// Decoration on the outer ring of flame effects: A flame starting at the attacking player wraps around
				// the arena 3 times
				Random randomValGen = new Random();
				success &= this.addConstantVelocityWaveToAction(action,
						fireEmitterModel.getOuterRingStartEmitterIter(
								fireEmitterModel.getSemanticOuterRingEmitterIndex(playerNum, randomValGen.nextBoolean(), 0), randomValGen.nextBoolean()),
						fireEmitterConfig.getNumOuterRingEmitters()*NYAN_CAT_NUM_WAVES, 1, 2 * NYAN_CAT_TIME_LENGTH_IN_SECS,
						DEFAULT_FULL_ON_FRACTION, DEFAULT_FULL_OFF_FRACTION, 0.0);
				
				break;
			}
			
			/*
			case DISCO_STU_ATTACK: {
				
				final double DISCO_STU_BASE_ACCELERATION   = 1.0;
				final double DISCO_STU_TIME_LENGTH_IN_SECS = 4.5;
				final float DISCO_STU_DAMAGE_PER_FLAME     = 3.0f;
				final int FIRST_RAIL_NUM_FLAMES            = 3;
				final int SECOND_RAIL_NUM_FLAMES           = 2;
				
				action = new PlayerAttackAction(fireEmitterModel, PlayerAttackAction.AttackType.DISCO_STU_ATTACK,
						blockerOrAttacker, attackee, DISCO_STU_DAMAGE_PER_FLAME);
				
				// There is a random chance of which of the rails receives the different number of flames over the other...
				if (Math.random() >= 0.5) {
					success &= this.addAcceleratingWaveToAction(action, fireEmitterModel.getPlayerLeftHandStartEmitterIter(playerNum), fireEmitterConfig.getNumEmittersPerRail(),
							FIRST_RAIL_NUM_FLAMES, DISCO_STU_TIME_LENGTH_IN_SECS, DISCO_STU_BASE_ACCELERATION,
							DEFAULT_FULL_ON_FRACTION, DEFAULT_FULL_OFF_FRACTION, 0.0);
					
					success &= this.addAcceleratingWaveToAction(action, fireEmitterModel.getPlayerRightHandStartEmitterIter(playerNum), fireEmitterConfig.getNumEmittersPerRail(),
							SECOND_RAIL_NUM_FLAMES, DISCO_STU_TIME_LENGTH_IN_SECS, DISCO_STU_BASE_ACCELERATION,
							DEFAULT_FULL_ON_FRACTION, DEFAULT_FULL_OFF_FRACTION, 0.0);
				}
				else {
					success &= this.addAcceleratingWaveToAction(action, fireEmitterModel.getPlayerLeftHandStartEmitterIter(playerNum), fireEmitterConfig.getNumEmittersPerRail(),
							SECOND_RAIL_NUM_FLAMES, DISCO_STU_TIME_LENGTH_IN_SECS, DISCO_STU_BASE_ACCELERATION,
							DEFAULT_FULL_ON_FRACTION, DEFAULT_FULL_OFF_FRACTION, 0.0);
					
					success &= this.addAcceleratingWaveToAction(action, fireEmitterModel.getPlayerRightHandStartEmitterIter(playerNum), fireEmitterConfig.getNumEmittersPerRail(),
							FIRST_RAIL_NUM_FLAMES, DISCO_STU_TIME_LENGTH_IN_SECS, DISCO_STU_BASE_ACCELERATION,
							DEFAULT_FULL_ON_FRACTION, DEFAULT_FULL_OFF_FRACTION, 0.0);
				}
				
				// For decoration just add a single wave from the attacker to the attackee on the outer ring
				success &= this.addConstantVelocityWaveToAction(action,
						fireEmitterModel.getOuterRingStartEmitterIter(
								fireEmitterModel.getSemanticOuterRingEmitterIndex(playerNum, true, 0), false),
						fireEmitterConfig.getNumOuterRingEmitters()/2, 1, DISCO_STU_TIME_LENGTH_IN_SECS,
						DEFAULT_FULL_ON_FRACTION, DEFAULT_FULL_OFF_FRACTION, 0.0);
				success &= this.addConstantVelocityWaveToAction(action,
						fireEmitterModel.getOuterRingStartEmitterIter(
								fireEmitterModel.getSemanticOuterRingEmitterIndex(playerNum, false, 0), true),
						fireEmitterConfig.getNumOuterRingEmitters()/2, 1, DISCO_STU_TIME_LENGTH_IN_SECS,
						DEFAULT_FULL_ON_FRACTION, DEFAULT_FULL_OFF_FRACTION, 0.0);
				
				break;
			}
			*/
			
			case ARM_WINDMILL_ATTACK: {
				
				final double ARM_WINDMILL_BASE_ACCELERATION   = 0.75;
				final double ARM_WINDMILL_TIME_LENGTH_IN_SECS = 2.5;
				final float ARM_WINDMILL_DAMAGE_PER_FLAME     = 2.0f;
				final int ARM_WINDMILL_NUM_FLAMES             = 3;
				
				action = new PlayerAttackAction(fireEmitterModel, PlayerAttackAction.AttackType.ARM_WINDMILL_ATTACK,
						blockerOrAttacker, attackee, ARM_WINDMILL_DAMAGE_PER_FLAME);
				
				final int ARM_WINDMILL_DOUBLE_NUM_FLAMES = 2 * ARM_WINDMILL_NUM_FLAMES;
				
				for (int i = 0; i < ARM_WINDMILL_DOUBLE_NUM_FLAMES; i++) {
					double currentDelay = i * (ARM_WINDMILL_TIME_LENGTH_IN_SECS / (double)ARM_WINDMILL_NUM_FLAMES) * 0.75;
					
					if (i % 2 == 0) {
						success &= this.addAcceleratingWaveToAction(action, fireEmitterModel.getPlayerRightHandStartEmitterIter(playerNum),
								fireEmitterConfig.getNumEmittersPerRail(),
								1, ARM_WINDMILL_TIME_LENGTH_IN_SECS, ARM_WINDMILL_BASE_ACCELERATION,
								DEFAULT_FULL_ON_FRACTION, DEFAULT_FULL_OFF_FRACTION, currentDelay);
					}
					else {
						success &= this.addAcceleratingWaveToAction(action, fireEmitterModel.getPlayerLeftHandStartEmitterIter(playerNum),
								fireEmitterConfig.getNumEmittersPerRail(),
								1, ARM_WINDMILL_TIME_LENGTH_IN_SECS, ARM_WINDMILL_BASE_ACCELERATION,
								DEFAULT_FULL_ON_FRACTION, DEFAULT_FULL_OFF_FRACTION, currentDelay);
					}
				}
				
				// No decoration on outer ring, currently...
				
				break;
			}
			
			case SUCK_IT_ATTACK: {
				
				final double SUCK_IT_BASE_ACCELERATION   = 0;
				final double SUCK_IT_TIME_LENGTH_IN_SECS = 1.5f;
				final float SUCK_IT_DAMAGE_PER_FLAME     = 3.0f;
				final int SUCK_IT_NUM_FLAMES = 3;
				
				action = this.buildPlayerTwoHandedSymetricalAttack(AttackType.SUCK_IT_ATTACK,
						playerNum, SUCK_IT_TIME_LENGTH_IN_SECS, SUCK_IT_BASE_ACCELERATION, SUCK_IT_NUM_FLAMES, SUCK_IT_DAMAGE_PER_FLAME);
				
				// Decoration for the suck it is a massive burst on all emitters for a small amount of time...
				success = this.addBurstToAction(action, fireEmitterModel.getOuterRingStartEmitterIter(0, true),
						fireEmitterConfig.getNumOuterRingEmitters(), 1, 1.0, DEFAULT_FULL_ON_FRACTION, DEFAULT_FULL_OFF_FRACTION, 0.0);

				break;
			}
			
			/*
			case VAFANAPOLI_ATTACK: {
				
				final double FAFANAPOLI_BASE_ACCELERATION   = 0;
				final double FAFANAPOLI_TIME_LENGTH_IN_SECS = 4.25;
				final float FAFANAPOLI_DAMAGE_PER_FLAME     = 5.0f;
				final int FAFANAPOLI_MAIN_HAND_NUM_FLAMES   = 2;
				final int FAFANAPOLI_OFF_HAND_NUM_FLAMES    = 1;
								
				if (leftHand) {
					action = new PlayerAttackAction(fireEmitterModel, PlayerAttackAction.AttackType.LEFT_VAFANAPOLI_ATTACK,
							blockerOrAttacker, attackee, FAFANAPOLI_DAMAGE_PER_FLAME);
					
					success &= this.addAcceleratingWaveToAction(action, fireEmitterModel.getPlayerLeftHandStartEmitterIter(playerNum), fireEmitterConfig.getNumEmittersPerRail(),
							FAFANAPOLI_MAIN_HAND_NUM_FLAMES, FAFANAPOLI_TIME_LENGTH_IN_SECS, FAFANAPOLI_BASE_ACCELERATION,
							DEFAULT_FULL_ON_FRACTION, DEFAULT_FULL_OFF_FRACTION, 0.0);
					success &= this.addAcceleratingWaveToAction(action, fireEmitterModel.getPlayerRightHandStartEmitterIter(playerNum), fireEmitterConfig.getNumEmittersPerRail(),
							FAFANAPOLI_OFF_HAND_NUM_FLAMES, FAFANAPOLI_TIME_LENGTH_IN_SECS, FAFANAPOLI_BASE_ACCELERATION,
							DEFAULT_FULL_ON_FRACTION, DEFAULT_FULL_OFF_FRACTION, 0.0);
				}
				else {
					action = new PlayerAttackAction(fireEmitterModel, PlayerAttackAction.AttackType.RIGHT_VAFANAPOLI_ATTACK,
							blockerOrAttacker, attackee, FAFANAPOLI_DAMAGE_PER_FLAME);
					
					success &= this.addAcceleratingWaveToAction(action, fireEmitterModel.getPlayerLeftHandStartEmitterIter(playerNum), fireEmitterConfig.getNumEmittersPerRail(),
							FAFANAPOLI_OFF_HAND_NUM_FLAMES, FAFANAPOLI_TIME_LENGTH_IN_SECS, FAFANAPOLI_BASE_ACCELERATION,
							DEFAULT_FULL_ON_FRACTION, DEFAULT_FULL_OFF_FRACTION, 0.0);
					success &= this.addAcceleratingWaveToAction(action, fireEmitterModel.getPlayerRightHandStartEmitterIter(playerNum), fireEmitterConfig.getNumEmittersPerRail(),
							FAFANAPOLI_MAIN_HAND_NUM_FLAMES, FAFANAPOLI_TIME_LENGTH_IN_SECS, FAFANAPOLI_BASE_ACCELERATION,
							DEFAULT_FULL_ON_FRACTION, DEFAULT_FULL_OFF_FRACTION, 0.0);
				}
				
				// Decoration involves flames starting midway between both players and then having a flame going towards each player from there
				success &= this.addConstantVelocityWaveToAction(action,
						fireEmitterModel.getOuterRingStartEmitterIter(4, false),
						fireEmitterConfig.getNumOuterRingEmitters()/4, 1, FAFANAPOLI_TIME_LENGTH_IN_SECS,
						DEFAULT_FULL_ON_FRACTION, DEFAULT_FULL_OFF_FRACTION, 0.0);
				success &= this.addConstantVelocityWaveToAction(action,
						fireEmitterModel.getOuterRingStartEmitterIter(3, true),
						fireEmitterConfig.getNumOuterRingEmitters()/4, 1, FAFANAPOLI_TIME_LENGTH_IN_SECS,
						DEFAULT_FULL_ON_FRACTION, DEFAULT_FULL_OFF_FRACTION, 0.0);
				
				success &= this.addConstantVelocityWaveToAction(action,
						fireEmitterModel.getOuterRingStartEmitterIter(12, false),
						fireEmitterConfig.getNumOuterRingEmitters()/4, 1, FAFANAPOLI_TIME_LENGTH_IN_SECS,
						DEFAULT_FULL_ON_FRACTION, DEFAULT_FULL_OFF_FRACTION, 0.0);
				success &= this.addConstantVelocityWaveToAction(action,
						fireEmitterModel.getOuterRingStartEmitterIter(11, true),
						fireEmitterConfig.getNumOuterRingEmitters()/4, 1, FAFANAPOLI_TIME_LENGTH_IN_SECS,
						DEFAULT_FULL_ON_FRACTION, DEFAULT_FULL_OFF_FRACTION, 0.0);
				
				break;
			}
			*/
			
			default:
				return null;
		}
		
		if (!success) {
			return null;
		}
		
		return action;
	}
	
	/**
	 * Builds a ringmaster action type as specified by the given enumeration.
	 * @param ringmasterActionType The type of ringmaster action.
	 * @param leftHand Whether the ringmaster's left hand is being used in the action.
	 * @param rightHand Whether the ringmaster's right hand is being used in the action.
	 * @return The resulting action, null on failure.
	 */
	final public Action buildRingmasterAction(ActionType ringmasterActionType,
											  boolean leftHand, boolean rightHand) {
		
		boolean success = true;
		
		FireEmitterModel fireEmitterModel   = this.gameModel.getFireEmitterModel();
		FireEmitterConfig fireEmitterConfig = fireEmitterModel.getConfig();
		
		Action action = null;
		
		switch (ringmasterActionType) {
		
		case RINGMASTER_HALF_RING_ACTION: {
			final double HALF_RING_DURATION_IN_SECS = 1.0;
			final int HALF_RING_NUM_FLAME_BURSTS = 1;
			
			FireEmitterIterator fireEmitterIter = null;
			
			if (leftHand) {
				action = new RingmasterAction(fireEmitterModel, RingmasterAction.ActionType.RINGMASTER_LEFT_HALF_RING_ACTION);
				fireEmitterIter = fireEmitterModel.getOuterRingStartEmitterIter(FireEmitterModel.RINGMASTER_6OCLOCK_OUTER_RING_LEFT_EMITTER, true);
			}
			else {
				action = new RingmasterAction(fireEmitterModel, RingmasterAction.ActionType.RINGMASTER_RIGHT_HALF_RING_ACTION);
				fireEmitterIter = fireEmitterModel.getOuterRingStartEmitterIter(FireEmitterModel.RINGMASTER_12OCLOCK_OUTER_RING_RIGHT_EMITTER, true);
			}
			
			assert(fireEmitterIter != null);
			success = this.addBurstToAction(action, fireEmitterIter, fireEmitterConfig.getNumOuterRingEmitters()/2, HALF_RING_NUM_FLAME_BURSTS,
					HALF_RING_DURATION_IN_SECS, DEFAULT_FULL_ON_FRACTION, DEFAULT_FULL_OFF_FRACTION, 0.0);
			break;
		}
		
		case RINGMASTER_JAB_ACTION: {
			final double JAB_DURATION_IN_SECS = 1.3333333;
			final int JAB_FLAME_WIDTH = 2;
			
			FireEmitterIterator fireEmitterIter = null;
			
			if (leftHand) {
				action = new RingmasterAction(fireEmitterModel, RingmasterAction.ActionType.RINGMASTER_LEFT_JAB_ACTION);
				fireEmitterIter = fireEmitterModel.getOuterRingStartEmitterIter(FireEmitterModel.RINGMASTER_6OCLOCK_OUTER_RING_LEFT_EMITTER, true);
			}
			else {
				action = new RingmasterAction(fireEmitterModel, RingmasterAction.ActionType.RINGMASTER_RIGHT_JAB_ACTION);
				fireEmitterIter = fireEmitterModel.getOuterRingStartEmitterIter(FireEmitterModel.RINGMASTER_6OCLOCK_OUTER_RING_RIGHT_EMITTER, false);
			}
			
			assert(fireEmitterIter != null);
			success = this.addConstantVelocityWaveToAction(action, fireEmitterIter, fireEmitterConfig.getNumOuterRingEmitters()/2,
					JAB_FLAME_WIDTH, JAB_DURATION_IN_SECS, DEFAULT_FULL_ON_FRACTION, DEFAULT_FULL_OFF_FRACTION, 0.0);
			break;
		}

		case RINGMASTER_ERUPTION_ACTION: {
			final double ERUPTION_DURATION_IN_SECS = 1.0;
			final int ERUPTION_NUM_BURSTS = 1;
			
			success = true;
			action = new RingmasterAction(fireEmitterModel, RingmasterAction.ActionType.RINGMASTER_ERUPTION_ACTION);
			
			// Full outer ring sustained burst
			success &= this.addBurstToAction(action, fireEmitterModel.getOuterRingStartEmitterIter(0, true), fireEmitterConfig.getNumOuterRingEmitters(),
					ERUPTION_NUM_BURSTS, ERUPTION_DURATION_IN_SECS, DEFAULT_FULL_ON_FRACTION, DEFAULT_FULL_OFF_FRACTION, 0.0);
			// Full right rail sustained burst
			success &= this.addBurstToAction(action, fireEmitterModel.getRightRailStartEmitterIter(0), fireEmitterConfig.getNumEmittersPerRail(),
					ERUPTION_NUM_BURSTS, ERUPTION_DURATION_IN_SECS, DEFAULT_FULL_ON_FRACTION, DEFAULT_FULL_OFF_FRACTION, 0.0);
			// Full left rail sustained burst
			success &= this.addBurstToAction(action, fireEmitterModel.getLeftRailStartEmitterIter(0), fireEmitterConfig.getNumEmittersPerRail(),
					ERUPTION_NUM_BURSTS, ERUPTION_DURATION_IN_SECS, DEFAULT_FULL_ON_FRACTION, DEFAULT_FULL_OFF_FRACTION, 0.0);
			
			break;
		}
		
		case RINGMASTER_CIRCLE_ACTION: {
			final double CIRCLE_DURATION_IN_SECS = 3.0;
			final int CIRCLE_WAVE_WIDTH = 2;
			
			FireEmitterIterator fireEmitterIter1 = null;
			FireEmitterIterator fireEmitterIter2 = null;
			if (leftHand) {
				action = new RingmasterAction(fireEmitterModel, RingmasterAction.ActionType.RINGMASTER_LEFT_CIRCLE_ACTION);
				fireEmitterIter1 = fireEmitterModel.getOuterRingStartEmitterIter(FireEmitterModel.RINGMASTER_3OCLOCK_OUTER_RING_CLOSE_EMITTER, true);
				fireEmitterIter2 = fireEmitterModel.getOuterRingStartEmitterIter(FireEmitterModel.RINGMASTER_9OCLOCK_OUTER_RING_FAR_EMITTER, true);
			}
			else {
				action = new RingmasterAction(fireEmitterModel, RingmasterAction.ActionType.RINGMASTER_RIGHT_CIRCLE_ACTION);
				fireEmitterIter1 = fireEmitterModel.getOuterRingStartEmitterIter(FireEmitterModel.RINGMASTER_3OCLOCK_OUTER_RING_FAR_EMITTER, false);
				fireEmitterIter2 = fireEmitterModel.getOuterRingStartEmitterIter(FireEmitterModel.RINGMASTER_9OCLOCK_OUTER_RING_CLOSE_EMITTER, false);
			}
			assert(fireEmitterIter1 != null);
			assert(fireEmitterIter2 != null);
			
			success = true;
			
			success &= this.addConstantVelocityWaveToAction(action, fireEmitterIter1, fireEmitterConfig.getNumOuterRingEmitters(), CIRCLE_WAVE_WIDTH,
					CIRCLE_DURATION_IN_SECS, DEFAULT_FULL_ON_FRACTION, DEFAULT_FULL_OFF_FRACTION, 0.0);
			success &= this.addConstantVelocityWaveToAction(action, fireEmitterIter2, fireEmitterConfig.getNumOuterRingEmitters(), CIRCLE_WAVE_WIDTH,
					CIRCLE_DURATION_IN_SECS, DEFAULT_FULL_ON_FRACTION, DEFAULT_FULL_OFF_FRACTION, 0.0);
			
			break;
		}

		case RINGMASTER_HADOUKEN_ACTION: {
			final double HADOUKEN_DURATION_IN_SECS = 2.0;
			final int HADOUKEN_FLAME_WIDTH = 2;
					
			FireEmitterIterator fireEmitterIter1 = fireEmitterModel.getOuterRingStartEmitterIter(FireEmitterModel.RINGMASTER_6OCLOCK_OUTER_RING_LEFT_EMITTER, true);
			FireEmitterIterator fireEmitterIter2 = fireEmitterModel.getOuterRingStartEmitterIter(FireEmitterModel.RINGMASTER_6OCLOCK_OUTER_RING_RIGHT_EMITTER, false);

			success = true;
			action = new RingmasterAction(fireEmitterModel, RingmasterAction.ActionType.RINGMASTER_HADOUKEN_ACTION);
			
			success &= this.addConstantVelocityWaveToAction(action, fireEmitterIter1, fireEmitterConfig.getNumOuterRingEmitters()/2,
					HADOUKEN_FLAME_WIDTH, HADOUKEN_DURATION_IN_SECS, DEFAULT_FULL_ON_FRACTION, DEFAULT_FULL_OFF_FRACTION, 0.0);
			success &= this.addConstantVelocityWaveToAction(action, fireEmitterIter2, fireEmitterConfig.getNumOuterRingEmitters()/2,
					HADOUKEN_FLAME_WIDTH, HADOUKEN_DURATION_IN_SECS, DEFAULT_FULL_ON_FRACTION, DEFAULT_FULL_OFF_FRACTION, 0.0);
			
			break;
		}
		
		case RINGMASTER_DRUM_ACTION: {
			
			final double DRUM_DURATION_IN_SECS = 5.0;
			final int NUM_RANDOM_BURSTS = 25;
			final double BURST_DURATION_IN_SECS = 1.0;
			final double BURST_DIV_SECS = (DRUM_DURATION_IN_SECS / (double)NUM_RANDOM_BURSTS);
			
			// Decoration in the outer ring of fire emitters is random bursts all over the ring
			double currDelayCount = 0.0;
			Random randomNumGen = new Random();
			
			success = true;
			action = new RingmasterAction(fireEmitterModel, RingmasterAction.ActionType.RINGMASTER_DRUM_ACTION);
			
			for (int i = 0; i < NUM_RANDOM_BURSTS; i++) {
				
				if (randomNumGen.nextBoolean()) {
					// Outer ring random effect...
					success &= this.addBurstToAction(action, fireEmitterModel.getOuterRingStartEmitterIter(
							Math.abs(randomNumGen.nextInt()) % fireEmitterConfig.getNumOuterRingEmitters(), true),
							1, 1, BURST_DURATION_IN_SECS, DEFAULT_FULL_ON_FRACTION, DEFAULT_FULL_OFF_FRACTION, currDelayCount);
				}
				else {
					if (randomNumGen.nextBoolean()) {
						// Left rail random effect...
						success &= this.addBurstToAction(action, fireEmitterModel.getLeftRailStartEmitterIter(
								Math.abs(randomNumGen.nextInt()) % fireEmitterConfig.getNumEmittersPerRail()),
								1, 1, BURST_DURATION_IN_SECS, DEFAULT_FULL_ON_FRACTION, DEFAULT_FULL_OFF_FRACTION, currDelayCount);
					}
					else {
						// Right rail random effect...
						success &= this.addBurstToAction(action, fireEmitterModel.getRightRailStartEmitterIter(
								Math.abs(randomNumGen.nextInt()) % fireEmitterConfig.getNumEmittersPerRail()),
								1, 1, BURST_DURATION_IN_SECS, DEFAULT_FULL_ON_FRACTION, DEFAULT_FULL_OFF_FRACTION, currDelayCount);
					}
				}
				
				currDelayCount += BURST_DIV_SECS;
			}
			
			break;
		}
			
		default:
			return null;
		}
		
		if (!success) {
			return null;
		}
		
		return action;
	}

	
	final public Action buildCustomPlayerAttackAction(int playerNum, int flameWidth, float dmgPerFlame, double acceleration,
											          boolean leftHand, boolean rightHand, double durationInSecs,
											          double fractionFullOn, double fractionFullOff) {
		// Player number should be valid
		if (playerNum != 1 && playerNum != 2) {
			assert(false);
			return null;
		}
		
		FireEmitterModel fireEmitterModel    = this.gameModel.getFireEmitterModel();
		FireEmitterConfig fireEmitterConfig  = fireEmitterModel.getConfig();
		
		Player blockerOrAttacker = this.gameModel.getPlayer(playerNum);
		Player attackee = this.gameModel.getPlayer(Player.getOpposingPlayerNum(playerNum));
		
		boolean success = true;
		
		Action action = new PlayerAttackAction(fireEmitterModel, PlayerAttackAction.AttackType.CUSTOM_UNDEFINED_ATTACK, blockerOrAttacker, attackee, dmgPerFlame);
		if (leftHand) {
			FireEmitterIterator emitterIterLeft  = fireEmitterModel.getPlayerLeftHandStartEmitterIter(playerNum);
			if (acceleration <= 0.0) {
				success &= this.addConstantVelocityWaveToAction(action, emitterIterLeft, fireEmitterConfig.getNumEmittersPerRail(),
						flameWidth, durationInSecs, fractionFullOn, fractionFullOff, 0.0); 
			}
			else {
				success &= this.addAcceleratingWaveToAction(action, emitterIterLeft, fireEmitterConfig.getNumEmittersPerRail(),
						flameWidth, durationInSecs, acceleration, fractionFullOn, fractionFullOff, 0.0);
			}
		}
		if (rightHand) {
			FireEmitterIterator emitterIterRight = fireEmitterModel.getPlayerRightHandStartEmitterIter(playerNum);
			if (acceleration <= 0.0) {
				success &= this.addConstantVelocityWaveToAction(action, emitterIterRight, fireEmitterConfig.getNumEmittersPerRail(),
						flameWidth, durationInSecs, fractionFullOn, fractionFullOff, 0.0); 
			}
			else {
				success &= this.addAcceleratingWaveToAction(action, emitterIterRight, fireEmitterConfig.getNumEmittersPerRail(),
						flameWidth, durationInSecs, acceleration, fractionFullOn, fractionFullOff, 0.0);
			}
		}
		
		if (!success) {
			return null;
		}
		
		return action;
	}
	
	// Crowd-Pleasing Actions (internal package use only) *******************************************************
	
	/**
	 * Create an end-of-round burst of flames at the given location of fire emitters in the arena.
	 * ya know.. cause we gotta wow the crowd.
	 * @param colourEntity The entity that will drive the colour of the flames.
	 * @param location The emitters location in the game arena.
	 * @param totalDurationInSecs Length of the whole burst action in seconds.
	 * @param numBursts The number of bursts.
	 * @return The resulting action, null on failure.
	 */
	final Action buildCrowdPleaserBurstAction(GameModel.Entity colourEntity, FireEmitter.Location location,
								              double totalDurationInSecs, int numBursts, double delayInSecs) {
		
		FireEmitterModel fireEmitterModel = this.gameModel.getFireEmitterModel();
		FireEmitterConfig fireEmitterConfig = fireEmitterModel.getConfig();
		
		int numEmitters = 0;
		
		FireEmitterIterator emitterIter = null;
		switch (location) {
			case LEFT_RAIL:
				emitterIter = fireEmitterModel.getLeftRailStartEmitterIter(0);
				numEmitters = fireEmitterConfig.getNumEmittersPerRail();
				break;
			case RIGHT_RAIL:
				emitterIter = fireEmitterModel.getRightRailStartEmitterIter(0);
				numEmitters = fireEmitterConfig.getNumEmittersPerRail();
				break;
			case OUTER_RING:
				emitterIter = fireEmitterModel.getOuterRingStartEmitterIter(0, false);
				numEmitters = fireEmitterConfig.getNumOuterRingEmitters();
				break;
			default:
				assert(false);
				return null;
		}

		Action action = new CrowdPleaserAction(fireEmitterModel, colourEntity);
		this.addBurstToAction(action, emitterIter, numEmitters, numBursts, totalDurationInSecs, 0.8f, 0.05f, delayInSecs);
		return action;
	}
	
	final Action buildPlayerWinAction(int victoryPlayerNum, double totalDurationInSecs, int numBursts, double delayInSecs) {
		
		FireEmitterModel fireEmitterModel = this.gameModel.getFireEmitterModel();
		FireEmitterConfig fireEmitterConfig = fireEmitterModel.getConfig();
		
		FireEmitterIterator fireEmitterIter = null;
		
		Player victoryPlayer = this.gameModel.getPlayer(victoryPlayerNum);
		assert(victoryPlayer != null);
		
		Action result = new CrowdPleaserAction(fireEmitterModel, victoryPlayer.getEntity());
		if (victoryPlayerNum == 1) {
			fireEmitterIter = fireEmitterModel.getOuterRingStartEmitterIter(FireEmitterModel.RINGMASTER_12OCLOCK_OUTER_RING_RIGHT_EMITTER, true);
		}
		else {
			assert(victoryPlayerNum == 2);
			fireEmitterIter = fireEmitterModel.getOuterRingStartEmitterIter(FireEmitterModel.RINGMASTER_6OCLOCK_OUTER_RING_LEFT_EMITTER, true);
		}

		assert(fireEmitterIter != null);
		this.addBurstToAction(result, fireEmitterIter, fireEmitterConfig.getNumOuterRingEmitters()/2, numBursts,
				totalDurationInSecs, DEFAULT_FULL_ON_FRACTION, DEFAULT_FULL_OFF_FRACTION, delayInSecs);
		
		return result;
	}

	
	final Action buildCrowdPleaserTouchAction(GameModel.Entity colourEntity, FireEmitter.Location location,
											  int index, double totalDurationInSecs, int numBursts) {
		
		final double MAX_INTENSITY_DELAY = 0.05;
		if (totalDurationInSecs <= 2*MAX_INTENSITY_DELAY) {
			assert(false);
			return null;
		}
		
		FireEmitterModel fireEmitterModel = this.gameModel.getFireEmitterModel();
		FireEmitter emitter = fireEmitterModel.getEmitter(location, index);
		if (emitter == null) {
			assert(false);
			return null;
		}
		
		CrowdPleaserAction action = new CrowdPleaserAction(fireEmitterModel, colourEntity);
		MultiLerp intensityLerp = this.buildIntensityMultiLerp(
				MAX_INTENSITY_DELAY, totalDurationInSecs - MAX_INTENSITY_DELAY,
				totalDurationInSecs, totalDurationInSecs);
		assert(intensityLerp != null);
		action.addFireBursts(emitter, numBursts, intensityLerp);
		
		return action;
	}
	
	
	// *************************************************************************************************
	
	/**
	 * Build a left-handed player attack action.
	 * @param attackerPlayerNum The player number of the attacker.
	 * @param totalDurationInSecs The total duration (how long it will be in play) of the attack in seconds.
	 * @param width The width of the attack in fire emitters.
	 * @param dmgPerFlame The base damage per flame delivered to the opposing player.
	 * @return The resulting action, null on failure.
	 */
	final Action buildPlayerLeftHandAttack(PlayerAttackAction.AttackType type, int attackerPlayerNum,
			                               double totalDurationInSecs, int width, float baseDmgPerFlame) {
		
		int attackeePlayerNum = Player.getOpposingPlayerNum(attackerPlayerNum);
		FireEmitterModel fireEmitterModel = this.gameModel.getFireEmitterModel();
		FireEmitterIterator emitterIter = fireEmitterModel.getPlayerLeftHandStartEmitterIter(attackerPlayerNum);	

		return this.buildPlayerAttack(type, this.gameModel.getPlayer(attackerPlayerNum), this.gameModel.getPlayer(attackeePlayerNum),
				emitterIter, totalDurationInSecs, width, baseDmgPerFlame);
	}
	final Action buildPlayerRightHandAttack(PlayerAttackAction.AttackType type, int attackerPlayerNum,
			                                double totalDurationInSecs, int width, float baseDmgPerFlame) {
		
		int attackeePlayerNum = Player.getOpposingPlayerNum(attackerPlayerNum);
		FireEmitterModel fireEmitterModel = this.gameModel.getFireEmitterModel();
		FireEmitterIterator emitterIter = fireEmitterModel.getPlayerRightHandStartEmitterIter(attackerPlayerNum);		

		return this.buildPlayerAttack(type, this.gameModel.getPlayer(attackerPlayerNum), this.gameModel.getPlayer(attackeePlayerNum),
				emitterIter, totalDurationInSecs, width, baseDmgPerFlame);
	}
	
	
	final Action buildPlayerTwoHandedSymetricalAttack(PlayerAttackAction.AttackType type, int attackerPlayerNum,
													  double totalDurationInSecs, double acceleration, 
													  int width, float baseDmgPerFlame) {

		int attackeePlayerNum = Player.getOpposingPlayerNum(attackerPlayerNum);
		FireEmitterModel fireEmitterModel = this.gameModel.getFireEmitterModel();
		
		FireEmitterIterator startingLHEmitterIter = fireEmitterModel.getPlayerLeftHandStartEmitterIter(attackerPlayerNum);
		FireEmitterIterator startingRHEmitterIter = fireEmitterModel.getPlayerRightHandStartEmitterIter(attackerPlayerNum);		
		
		Player attacker = this.gameModel.getPlayer(attackerPlayerNum);
		Player attackee = this.gameModel.getPlayer(attackeePlayerNum);
		
		PlayerAttackAction atkAction = new PlayerAttackAction(fireEmitterModel, type, attacker, attackee, baseDmgPerFlame);
		
		// Add the left and right handed attacks...
		boolean success = true;
		
		if (acceleration <= 0) {
			success &= this.addConstantVelocityWaveToAction(atkAction, startingLHEmitterIter,
					fireEmitterModel.getConfig().getNumEmittersPerRail(), width, totalDurationInSecs,
					ActionFactory.DEFAULT_FULL_ON_FRACTION, ActionFactory.DEFAULT_FULL_OFF_FRACTION, 0.0);
			success &= this.addConstantVelocityWaveToAction(atkAction, startingRHEmitterIter,
					fireEmitterModel.getConfig().getNumEmittersPerRail(), width, totalDurationInSecs,
					ActionFactory.DEFAULT_FULL_ON_FRACTION, ActionFactory.DEFAULT_FULL_OFF_FRACTION, 0.0);
		}
		else {
			success &= this.addAcceleratingWaveToAction(atkAction, startingLHEmitterIter,
					fireEmitterModel.getConfig().getNumEmittersPerRail(), width, totalDurationInSecs, acceleration,
					ActionFactory.DEFAULT_FULL_ON_FRACTION, ActionFactory.DEFAULT_FULL_OFF_FRACTION, 0.0);
			success &= this.addAcceleratingWaveToAction(atkAction, startingRHEmitterIter,
					fireEmitterModel.getConfig().getNumEmittersPerRail(), width, totalDurationInSecs, acceleration,
					ActionFactory.DEFAULT_FULL_ON_FRACTION, ActionFactory.DEFAULT_FULL_OFF_FRACTION, 0.0);
		}
		
		if (!success) {
			assert(false);
			return null;
		}
		
		return atkAction;
	}
	
	
	final private Action buildPlayerAttack(PlayerAttackAction.AttackType type, Player attacker, Player attackee,
										   FireEmitterIterator emitterIter, double totalDurationInSecs,
										   int width, float baseDmgPerFlame) {
		assert(attacker != null);
		assert(attackee != null);
		assert(emitterIter != null);
		
		FireEmitterModel fireEmitterModel = this.gameModel.getFireEmitterModel();
		PlayerAttackAction atkAction = new PlayerAttackAction(fireEmitterModel, type, attacker, attackee, baseDmgPerFlame);
		
		boolean success = this.addConstantVelocityWaveToAction(atkAction, emitterIter,
				fireEmitterModel.getConfig().getNumEmittersPerRail(), width, totalDurationInSecs,
				ActionFactory.DEFAULT_FULL_ON_FRACTION, ActionFactory.DEFAULT_FULL_OFF_FRACTION, 0.0);
		
		if (!success) {
			assert(false);
			return null;
		}
		
		return atkAction;
	}
	
	/**
	 * Add a wave of fire emitter simulation (where the fire moves at a constant velocity across the emitters) to the given action.
	 * @param action The action to add a wave to.
	 * @param emitterIter The fire emitter iterator for the wave.
	 * @param travelLength The length of travel of the action.
	 * @param width The width of the action (i.e., how many simultaneous flames as the wave travels).
	 * @param totalDurationInSecs Total duration of the wave (length of time it will have fire emitters simulating for, in total).
	 * @param fullOnFraction The fraction [0,1] of time that the emitters will be turned completely on.
	 * @param fullOffFraction The fraction of time that the emitters will be turned completely off.
	 * @param delayInSecs The delay in seconds before the wave starts.
	 * @return true on success, false on failure.
	 */
	final private boolean addConstantVelocityWaveToAction(Action action, FireEmitterIterator emitterIter, int travelLength,
										                  int width, double totalDurationInSecs, double fullOnFraction,
										                  double fullOffFraction, double delayInSecs) {
		
		// Make sure all the provided parameters are correct
		if (action == null || emitterIter == null || totalDurationInSecs < 0.001 || travelLength <= 0 || width <= 0 ||
			fullOnFraction < 0.0 || fullOnFraction > 1.0 || fullOffFraction < 0.0 || fullOffFraction > 1.0 ||
			(fullOnFraction + fullOffFraction) > 1.0 + EPSILON || delayInSecs < 0.0) {
			
			assert(false);
			return false;
		}

		// Calculate values on a per-lerp basis (i.e., time of each on/off cycle per fire emitter in the wave)
		// NOTE: Since the wave form cascades over the travel length of emitters, it causes the
		// duration for each lerp cycle to be (totalDuration / (double)(width + travelLength - 1)).
		double durationPerLerp  = totalDurationInSecs / (double)(width + travelLength - 1);
		double offTimePerLerp   = durationPerLerp * fullOffFraction;
		double onTimePerLerp    = durationPerLerp - offTimePerLerp;
		double maxOnTimePerLerp = durationPerLerp * fullOnFraction;
		
		assert(onTimePerLerp >= maxOnTimePerLerp);
		double startMaxIntensityTime = Math.max(0.0, (onTimePerLerp - maxOnTimePerLerp) / 2.0);
		double endMaxIntensityTime   = startMaxIntensityTime + maxOnTimePerLerp;
		double startDelayTime        = onTimePerLerp;
		
		MultiLerp intensityLerp = this.buildIntensityMultiLerp(startMaxIntensityTime,
				endMaxIntensityTime, startDelayTime, durationPerLerp);
		if (intensityLerp == null) {
			assert(false);
			return false;
		}
		
		return action.addConstantVelocityFireEmitterWave(emitterIter, travelLength, width, intensityLerp, delayInSecs);
	}
	
	/**
	 * Add a wave of fire emitter simulation (where the fire moves with a given acceleration across the emitters) to the given action.
	 * @param action The action to add a wave to.
	 * @param emitterIter The fire emitter iterator for the wave.
	 * @param travelLength The length of travel of the action.
	 * @param width The width of the action (i.e., how many simultaneous flames as the wave travels).
	 * @param totalDurationInSecs Total duration of the wave (length of time it will have fire emitters simulating for, in total).
	 * @param acceleration The acceleration of the fire as it moves across the emitters.
	 * @param fullOnFraction The fraction [0,1] of time that the emitters will be turned completely on.
	 * @param fullOffFraction The fraction of time that the emitters will be turned completely off.
	 * @param delayInSecs The delay in seconds before the wave starts.
	 * @return true on success, false on failure.
	 */
	final private boolean addAcceleratingWaveToAction(Action action, FireEmitterIterator emitterIter, int travelLength,
			                                          int width, double totalDurationInSecs, double acceleration, 
			                                          double fullOnFraction, double fullOffFraction, double delayInSecs) {
		
		// Make sure all the provided parameters are correct
		if (action == null || emitterIter == null || totalDurationInSecs < 0.001 || travelLength <= 0 || width <= 0 ||
			fullOnFraction < 0.0 || fullOnFraction > 1.0 || fullOffFraction < 0.0 || fullOffFraction > 1.0 ||
			(fullOnFraction + fullOffFraction) > 1.0 + EPSILON || delayInSecs < 0.0) {
			
			assert(false);
			return false;
		}
		
		if (acceleration <= 0.0) {
			return this.addConstantVelocityWaveToAction(action, emitterIter, travelLength, width,
					totalDurationInSecs, fullOnFraction, fullOffFraction, delayInSecs);
		}

		
		// First thing we do is calculate what the initial velocity has to be based on the parameters:
		// We use high-school kinematics to do this... [ d = (Vi * t) + 0.5 * a * t^2 ], switch the formula around to solve for Vi
		
		// d = travelLength
		// t = totalDurationInSecs
		// a = acceleration (duh)
		
		double initialVelocity = (travelLength - 0.5 * acceleration * totalDurationInSecs * totalDurationInSecs) / totalDurationInSecs;
		if (initialVelocity < 0.0) {
			initialVelocity = 0.0;
			acceleration    = (2.0 * travelLength) / (totalDurationInSecs * totalDurationInSecs);
		}

		// Unlike a constant velocity wave, we need to build a multilerp for each emitter since the acceleration
		// will cause each emitter's linear interpolation to have different durations
		List<MultiLerp> intensityLerps = new ArrayList<MultiLerp>(travelLength);
		for (int i = 0; i < travelLength; i++) {
			
			// Using that initial velocity we will feed values back into the equation to solve for each step of the distance,
			// by doing this we can figure out the time spent on each emitter...
			double initialDist = i;
			double finalDist   = i+1;
			
			// Solve for time using kinematics and the quadratic equation - since the initial velocity
			// is positive, the positive square root in the quadratic is always the right choice
			double initialTime = (-initialVelocity + Math.sqrt(initialVelocity * initialVelocity + 2 * acceleration * initialDist)) / acceleration;
			double finalTime   = (-initialVelocity + Math.sqrt(initialVelocity * initialVelocity + 2 * acceleration * finalDist))   / acceleration;
			
			double lerpDuration  = finalTime - initialTime;
			assert(lerpDuration > 0.0);
			
			double lerpOffTime   = lerpDuration * fullOffFraction;
			double lerpOnTime    = lerpDuration - lerpOffTime;
			double lerpMaxOnTime = lerpDuration * fullOnFraction;
			
			assert(lerpOnTime >= lerpMaxOnTime);
			double startMaxIntensityTime = (lerpOnTime - lerpMaxOnTime) / 2.0;
			double endMaxIntensityTime   = startMaxIntensityTime + lerpMaxOnTime;
			double startDelayTime        = lerpOnTime;
			
			MultiLerp intensityLerp = this.buildIntensityMultiLerp(startMaxIntensityTime,
					endMaxIntensityTime, startDelayTime, lerpDuration);
			if (intensityLerp == null) {
				assert(false);
				return false;
			}
			
			intensityLerps.add(intensityLerp);
		}
		
		return action.addFireEmitterWave(emitterIter, width, intensityLerps, delayInSecs);
	}
	
	final private boolean addBurstToAction(Action action, FireEmitterIterator emitterIter, int width, int numBursts,
										   double totalDurationInSecs, double fullOnFraction, double fullOffFraction,
										   double delayInSecs) {
		
		// Make sure all the provided parameters are correct
		if (action == null || emitterIter == null || totalDurationInSecs < 0.001 || width <= 0 || numBursts <= 0 ||
			fullOnFraction < 0.0 || fullOnFraction > 1.0 || fullOffFraction < 0.0 || fullOffFraction > 1.0 ||
			(fullOnFraction + fullOffFraction) > 1.0 + EPSILON || delayInSecs < 0.0) {
			
			assert(false);
			return false;
		}

		// Calculate values on a per-lerp basis (i.e., time of each on/off cycle per fire emitter in the wave)
		// NOTE: Since the wave form cascades over the travel length of emitters, it causes the
		// duration for each lerp cycle to be (totalDuration / (double)(width + travelLength - 1)).
		double durationPerLerp  = totalDurationInSecs / (double)numBursts;
		double offTimePerLerp   = durationPerLerp * fullOffFraction;
		double onTimePerLerp    = durationPerLerp - offTimePerLerp;
		double maxOnTimePerLerp = durationPerLerp * fullOnFraction;
		
		assert(onTimePerLerp >= maxOnTimePerLerp);
		double startMaxIntensityTime = (onTimePerLerp - maxOnTimePerLerp) / 2.0;
		double endMaxIntensityTime   = startMaxIntensityTime + maxOnTimePerLerp;
		double startDelayTime        = onTimePerLerp;
		
		MultiLerp intensityLerp = this.buildIntensityMultiLerp(startMaxIntensityTime,
				endMaxIntensityTime, startDelayTime, durationPerLerp);
		if (intensityLerp == null) {
			assert(false);
			return false;
		}
		
		return action.addFireEmitterBurst(emitterIter, width, numBursts, intensityLerp, delayInSecs);
	}
	
	/**
	 * Builds a piece-wise linear function that describes a stepped wave function for changing
	 * the intensity value of a fire emitter. The step will look as follows:
	 * 
	 * intensity
	 *    1|   ___________________
	 *     |  /                   \
	 *     | /                     \
	 *    0|/_______________________\__________   time
	 *      0  B                 C   D        E      
	 *
	 * @param startMaxIntensityTime (B) The time where the first maximum value of the intensity is achieved
	 * @param endMaxIntensityTime (C) The time where the last maximum value of the intensity is achieved
	 * @param startDelayTime (D) The time where the value of the intensity is zero and will continue to be zero.
	 * @param endDelayTime (E) The last time value of the function.
	 * @return The resulting MultiLerp object with the above function in it, null on failure.
	 */
	final private MultiLerp buildIntensityMultiLerp(double startMaxIntensityTime, double endMaxIntensityTime,
			                                        double startDelayTime, double endDelayTime) {
		
		// Make sure the provided parameters are in sequential order from least to greatest
		if (startMaxIntensityTime < -EPSILON || startMaxIntensityTime > (endMaxIntensityTime + EPSILON) ||
		    endMaxIntensityTime > (startDelayTime + EPSILON) || startDelayTime > (endDelayTime + EPSILON)) {
			assert(false);
			return null;
		}
		
		List<Double> timeValues      = new ArrayList<Double>();
		List<Double> intensityValues = new ArrayList<Double>();
		
		timeValues.add(0.0);
		if (startMaxIntensityTime != 0.0) {
			
			timeValues.add(startMaxIntensityTime);
			intensityValues.add(0.0);
		}
		intensityValues.add(1.0);
		
		if (startMaxIntensityTime != endMaxIntensityTime) {
			timeValues.add(endMaxIntensityTime);
			intensityValues.add(1.0);
		}
		
		if (startDelayTime != endMaxIntensityTime) {
			timeValues.add(startDelayTime);
			intensityValues.add(0.0);
		}
		
		if (startDelayTime != endDelayTime) {
			timeValues.add(endDelayTime);
			intensityValues.add(0.0);
		}

		return new MultiLerp(timeValues, intensityValues);
	}
	
}
