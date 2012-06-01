package ca.site3.ssf.gamemodel;


import java.util.List;
import java.util.ArrayList;
import ca.site3.ssf.common.MultiLerp;

/**
 * Publicly exposed factory class for building the various actions/moves for players and
 * the ringmaster in the SSF game.
 * @author Callum
 *
 */
final public class ActionFactory {
	
	public enum PlayerActionType {
		BLOCK, JAB_ATTACK, HOOK_ATTACK, UPPERCUT_ATTACK, CHOP_ATTACK,
		HADOUKEN_ATTACK, SHORYUKEN_ATTACK, SONIC_BOOM_ATTACK, DOUBLE_LARIAT_ATTACK,
		SUMO_HEADBUTT_ATTACK, ONE_HUNDRED_HAND_SLAP_ATTACK
	};

	final static public float DEFAULT_FULL_ON_FRACTION  = 0.45f;
	final static public float DEFAULT_FULL_OFF_FRACTION = 0.25f;
	
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
	final public Action buildPlayerAction(int playerNum, PlayerActionType playerActionType,
			                              boolean leftHand, boolean rightHand) {
		
		// Player number should be valid
		if (playerNum != 1 && playerNum != 2) {
			assert(false);
			return null;
		}
		
		Action action = null;
		
		FireEmitterModel fireEmitterModel    = this.gameModel.getFireEmitterModel();
		FireEmitterIterator emitterIterLeft  = fireEmitterModel.getPlayerLeftHandStartEmitterIter(playerNum);
		FireEmitterIterator emitterIterRight = fireEmitterModel.getPlayerRightHandStartEmitterIter(playerNum);
		FireEmitterConfig fireEmitterConfig  = fireEmitterModel.getConfig();
		
		Player blockerOrAttacker = this.gameModel.getPlayer(playerNum);
		Player attackee = this.gameModel.getPlayer(Player.getOpposingPlayerNum(playerNum));
		
		boolean success = true;
		
		switch (playerActionType) {
			case BLOCK:
				action = new PlayerBlockAction(fireEmitterModel, blockerOrAttacker);

				success &= this.addBurstToAction(action, emitterIterLeft, 1, 1, 3.0, 0.99, 0.01);
				success &= this.addBurstToAction(action, emitterIterRight, 1, 1, 3.0, 0.99, 0.01);
				break;
				
			case JAB_ATTACK: {
				assert(leftHand || rightHand);
				
				final float JAB_TIME_LENGTH_IN_SECS = 4.0f;
				final float JAB_DAMAGE_PER_FLAME = 5.0f;
				if (leftHand) {
					action = new PlayerAttackAction(fireEmitterModel, PlayerAttackAction.AttackType.LEFT_JAB_ATTACK, blockerOrAttacker, attackee, JAB_DAMAGE_PER_FLAME);
					success &= this.addConstantVelocityWaveToAction(action, emitterIterLeft, fireEmitterConfig.getNumEmittersPerRail(),
							1, JAB_TIME_LENGTH_IN_SECS, DEFAULT_FULL_ON_FRACTION, DEFAULT_FULL_OFF_FRACTION);
				}
				else {
					action = new PlayerAttackAction(fireEmitterModel, PlayerAttackAction.AttackType.RIGHT_JAB_ATTACK, blockerOrAttacker, attackee, JAB_DAMAGE_PER_FLAME);
					success &= this.addConstantVelocityWaveToAction(action, emitterIterRight, fireEmitterConfig.getNumEmittersPerRail(),
							1, JAB_TIME_LENGTH_IN_SECS, DEFAULT_FULL_ON_FRACTION, DEFAULT_FULL_OFF_FRACTION);
				}
				break;
			}
			
			case HOOK_ATTACK: {
				assert(leftHand || rightHand);
				
				final float HOOK_TIME_LENGTH_IN_SECS = 4.5f;
				final float HOOK_DAMAGE_PER_FLAME    = 8.0f;
				if (leftHand) {
					action = new PlayerAttackAction(fireEmitterModel, PlayerAttackAction.AttackType.LEFT_HOOK_ATTACK, blockerOrAttacker, attackee, HOOK_DAMAGE_PER_FLAME);
					success &= this.addConstantVelocityWaveToAction(action, emitterIterLeft, fireEmitterConfig.getNumEmittersPerRail(),
							1, HOOK_TIME_LENGTH_IN_SECS, DEFAULT_FULL_ON_FRACTION, DEFAULT_FULL_OFF_FRACTION);
				}
				else {
					action = new PlayerAttackAction(fireEmitterModel, PlayerAttackAction.AttackType.RIGHT_HOOK_ATTACK, blockerOrAttacker, attackee, HOOK_DAMAGE_PER_FLAME);
					success &= this.addConstantVelocityWaveToAction(action, emitterIterRight, fireEmitterConfig.getNumEmittersPerRail(),
							1, HOOK_TIME_LENGTH_IN_SECS, DEFAULT_FULL_ON_FRACTION, DEFAULT_FULL_OFF_FRACTION);
				}
				break;
			}
			
			case UPPERCUT_ATTACK: {
				assert(leftHand || rightHand);
				
				final float UPPERCUT_TIME_LENGTH_IN_SECS = 5.5f;
				final float UPPERCUT_DAMAGE_PER_FLAME    = 10.0f;
				final int UPPERCUT_NUM_FLAMES            = 2;
				
				if (leftHand) {
					action = new PlayerAttackAction(fireEmitterModel, PlayerAttackAction.AttackType.LEFT_UPPERCUT_ATTACK, blockerOrAttacker, attackee, UPPERCUT_DAMAGE_PER_FLAME);
					success &= this.addConstantVelocityWaveToAction(action, emitterIterLeft, fireEmitterConfig.getNumEmittersPerRail(),
							UPPERCUT_NUM_FLAMES, UPPERCUT_TIME_LENGTH_IN_SECS, DEFAULT_FULL_ON_FRACTION, DEFAULT_FULL_OFF_FRACTION);
				}
				else {
					action = new PlayerAttackAction(fireEmitterModel, PlayerAttackAction.AttackType.RIGHT_UPPERCUT_ATTACK, blockerOrAttacker, attackee, UPPERCUT_DAMAGE_PER_FLAME);
					success &= this.addConstantVelocityWaveToAction(action, emitterIterRight, fireEmitterConfig.getNumEmittersPerRail(),
							UPPERCUT_NUM_FLAMES, UPPERCUT_TIME_LENGTH_IN_SECS, DEFAULT_FULL_ON_FRACTION, DEFAULT_FULL_OFF_FRACTION);
				}
				break;
			}
			
			case CHOP_ATTACK: {
				assert(leftHand || rightHand);
				
				final float CHOP_TIME_LENGTH_IN_SECS = 6.0f;
				final float CHOP_DAMAGE_PER_FLAME    = 8.75f;
				if (leftHand) {
					action = new PlayerAttackAction(fireEmitterModel, PlayerAttackAction.AttackType.LEFT_CHOP_ATTACK, blockerOrAttacker, attackee, CHOP_DAMAGE_PER_FLAME);
					success &= this.addConstantVelocityWaveToAction(action, emitterIterLeft, fireEmitterConfig.getNumEmittersPerRail(),
							1, CHOP_TIME_LENGTH_IN_SECS, DEFAULT_FULL_ON_FRACTION, DEFAULT_FULL_OFF_FRACTION);
				}
				else {
					action = new PlayerAttackAction(fireEmitterModel, PlayerAttackAction.AttackType.RIGHT_CHOP_ATTACK, blockerOrAttacker, attackee, CHOP_DAMAGE_PER_FLAME);
					success &= this.addConstantVelocityWaveToAction(action, emitterIterRight, fireEmitterConfig.getNumEmittersPerRail(),
							1, CHOP_TIME_LENGTH_IN_SECS, DEFAULT_FULL_ON_FRACTION, DEFAULT_FULL_OFF_FRACTION);
				}
				break;
			}
			
			case HADOUKEN_ATTACK: {
				
				final float HADOUKEN_TIME_LENGTH_IN_SECS = 4.0f;
				final float HADOUKEN_DAMAGE_PER_FLAME    = 5.0f;
				final int HADOUKEN_NUM_FLAMES            = 2;
				
				action = this.buildPlayerTwoHandedSymetricalAttack(PlayerAttackAction.AttackType.HADOUKEN_ATTACK, playerNum,
						HADOUKEN_TIME_LENGTH_IN_SECS, HADOUKEN_NUM_FLAMES, HADOUKEN_DAMAGE_PER_FLAME);
				break;
			}
			
			case SHORYUKEN_ATTACK: {
				final float SHORYUKEN_TIME_LENGTH_IN_SECS = 3.25f;
				final float SHORYUKEN_DAMAGE_PER_FLAME    = 9.0f;
				final int SHORYUKEN_NUM_FLAMES_PUNCH_HAND = 2;
				final int SHORYUKEN_NUM_FLAMES_OFFHAND    = 1;
				
				action = new PlayerAttackAction(fireEmitterModel, PlayerAttackAction.AttackType.SHORYUKEN_ATTACK, blockerOrAttacker, attackee, SHORYUKEN_DAMAGE_PER_FLAME);
				
				if (leftHand) {
					success &= this.addConstantVelocityWaveToAction(action, emitterIterLeft, fireEmitterConfig.getNumEmittersPerRail(),
							SHORYUKEN_NUM_FLAMES_PUNCH_HAND, SHORYUKEN_TIME_LENGTH_IN_SECS, DEFAULT_FULL_ON_FRACTION, DEFAULT_FULL_OFF_FRACTION);
					success &= this.addConstantVelocityWaveToAction(action, emitterIterRight, fireEmitterConfig.getNumEmittersPerRail(),
							SHORYUKEN_NUM_FLAMES_OFFHAND, SHORYUKEN_TIME_LENGTH_IN_SECS, DEFAULT_FULL_ON_FRACTION, DEFAULT_FULL_OFF_FRACTION);
				}
				else {
					success &= this.addConstantVelocityWaveToAction(action, emitterIterRight, fireEmitterConfig.getNumEmittersPerRail(),
							SHORYUKEN_NUM_FLAMES_PUNCH_HAND, SHORYUKEN_TIME_LENGTH_IN_SECS, DEFAULT_FULL_ON_FRACTION, DEFAULT_FULL_OFF_FRACTION);
					success &= this.addConstantVelocityWaveToAction(action, emitterIterLeft, fireEmitterConfig.getNumEmittersPerRail(),
							SHORYUKEN_NUM_FLAMES_OFFHAND, SHORYUKEN_TIME_LENGTH_IN_SECS, DEFAULT_FULL_ON_FRACTION, DEFAULT_FULL_OFF_FRACTION);
				}
				break;
			}
			
			case SONIC_BOOM_ATTACK: {
				
				final float SONIC_BOOM_TIME_LENGTH_IN_SECS = 2.5f;
				final float SONIC_BOOM_DAMAGE_PER_FLAME    = 5.0f;
				final int SONIC_BOOM_NUM_FLAMES            = 1;
				
				action = this.buildPlayerTwoHandedSymetricalAttack(PlayerAttackAction.AttackType.SONIC_BOOM_ATTACK, playerNum,
						SONIC_BOOM_TIME_LENGTH_IN_SECS, SONIC_BOOM_NUM_FLAMES, SONIC_BOOM_DAMAGE_PER_FLAME);
				break;
			}
			
			case DOUBLE_LARIAT_ATTACK: {
				final float DOUBLE_LARIAT_TIME_LENGTH_IN_SECS = 7.5f;
				final float DOUBLE_LARIAT_DAMAGE_PER_FLAME    = 8.0f;
				final int DOUBLE_LARIAT_NUM_FLAMES            = 2;
				
				action = this.buildPlayerTwoHandedSymetricalAttack(PlayerAttackAction.AttackType.DOUBLE_LARIAT_ATTACK, playerNum,
						DOUBLE_LARIAT_TIME_LENGTH_IN_SECS, DOUBLE_LARIAT_NUM_FLAMES, DOUBLE_LARIAT_DAMAGE_PER_FLAME);
				break;
			}
			
			case SUMO_HEADBUTT_ATTACK: {
				final float SUMO_HEADBUTT_TIME_LENGTH_IN_SECS = 5.0f;
				final float SUMO_HEADBUTT_DAMAGE_PER_FLAME    = 6.0f;
				final int SUMO_HEADBUTT_NUM_FLAMES            = 2;
				
				action = this.buildPlayerTwoHandedSymetricalAttack(PlayerAttackAction.AttackType.SUMO_HEADBUTT_ATTACK, playerNum,
						SUMO_HEADBUTT_TIME_LENGTH_IN_SECS, SUMO_HEADBUTT_NUM_FLAMES, SUMO_HEADBUTT_DAMAGE_PER_FLAME);
				break;
			}
			
			case ONE_HUNDRED_HAND_SLAP_ATTACK: {
				
				final float ONE_HUND_HAND_SLAP_TIME_LENGTH_IN_SECS = 4.33f;
				final float ONE_HUND_HAND_SLAP_DAMAGE_PER_FLAME    = 5.0f;
				final int ONE_HUND_HAND_SLAP_NUM_FLAMES            = 3;
				
				if (leftHand && rightHand) {
					action = this.buildPlayerTwoHandedSymetricalAttack(PlayerAttackAction.AttackType.ONE_HUNDRED_HAND_SLAP_ATTACK, playerNum,
							ONE_HUND_HAND_SLAP_TIME_LENGTH_IN_SECS, ONE_HUND_HAND_SLAP_NUM_FLAMES, ONE_HUND_HAND_SLAP_DAMAGE_PER_FLAME);
				}
				else {
					action = new PlayerAttackAction(fireEmitterModel, PlayerAttackAction.AttackType.ONE_HUNDRED_HAND_SLAP_ATTACK,
							blockerOrAttacker, attackee, ONE_HUND_HAND_SLAP_DAMAGE_PER_FLAME);
					
					if (leftHand) {
						success &= this.addConstantVelocityWaveToAction(action, emitterIterLeft, fireEmitterConfig.getNumEmittersPerRail(),
								ONE_HUND_HAND_SLAP_NUM_FLAMES, ONE_HUND_HAND_SLAP_TIME_LENGTH_IN_SECS, DEFAULT_FULL_ON_FRACTION, DEFAULT_FULL_OFF_FRACTION);
					}
					else {
						success &= this.addConstantVelocityWaveToAction(action, emitterIterRight, fireEmitterConfig.getNumEmittersPerRail(),
								ONE_HUND_HAND_SLAP_NUM_FLAMES, ONE_HUND_HAND_SLAP_TIME_LENGTH_IN_SECS, DEFAULT_FULL_ON_FRACTION, DEFAULT_FULL_OFF_FRACTION);
					}
				}
				
				break;
			}
			
			default:
				assert(false);
				return null;
		}
		
		if (!success) {
			return null;
		}
		
		return action;
	}
	
	// TODO
	//final public Action buildRingmasterAction(double totalDurationInSecs, int width, int startEmitterIdx) {
	//}
	
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
						flameWidth, durationInSecs, fractionFullOn, fractionFullOff); 
			}
			else {
				success &= this.addAcceleratingWaveToAction(action, emitterIterLeft, fireEmitterConfig.getNumEmittersPerRail(),
						flameWidth, durationInSecs, acceleration, fractionFullOn, fractionFullOff);
			}
		}
		if (rightHand) {
			FireEmitterIterator emitterIterRight = fireEmitterModel.getPlayerRightHandStartEmitterIter(playerNum);
			if (acceleration <= 0.0) {
				success &= this.addConstantVelocityWaveToAction(action, emitterIterRight, fireEmitterConfig.getNumEmittersPerRail(),
						flameWidth, durationInSecs, fractionFullOn, fractionFullOff); 
			}
			else {
				success &= this.addAcceleratingWaveToAction(action, emitterIterRight, fireEmitterConfig.getNumEmittersPerRail(),
						flameWidth, durationInSecs, acceleration, fractionFullOn, fractionFullOff);
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
								              double totalDurationInSecs, int numBursts) {
		
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
		this.addBurstToAction(action, emitterIter, numEmitters, numBursts, totalDurationInSecs, 0.8f, 0.05f);
		return action;
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
													  double totalDurationInSecs, int width, float baseDmgPerFlame) {

		int attackeePlayerNum = Player.getOpposingPlayerNum(attackerPlayerNum);
		FireEmitterModel fireEmitterModel = this.gameModel.getFireEmitterModel();
		
		FireEmitterIterator startingLHEmitterIter = fireEmitterModel.getPlayerLeftHandStartEmitterIter(attackerPlayerNum);
		FireEmitterIterator startingRHEmitterIter = fireEmitterModel.getPlayerRightHandStartEmitterIter(attackerPlayerNum);		
		
		Player attacker = this.gameModel.getPlayer(attackerPlayerNum);
		Player attackee = this.gameModel.getPlayer(attackeePlayerNum);
		
		PlayerAttackAction atkAction = new PlayerAttackAction(fireEmitterModel, type, attacker, attackee, baseDmgPerFlame);
		
		// Add the left and right handed attacks...
		boolean success = true;
		
		success &= this.addConstantVelocityWaveToAction(atkAction, startingLHEmitterIter,
				fireEmitterModel.getConfig().getNumEmittersPerRail(), width, totalDurationInSecs,
				ActionFactory.DEFAULT_FULL_ON_FRACTION, ActionFactory.DEFAULT_FULL_OFF_FRACTION);

		success &= this.addConstantVelocityWaveToAction(atkAction, startingRHEmitterIter,
				fireEmitterModel.getConfig().getNumEmittersPerRail(), width, totalDurationInSecs,
				ActionFactory.DEFAULT_FULL_ON_FRACTION, ActionFactory.DEFAULT_FULL_OFF_FRACTION);
		
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
				ActionFactory.DEFAULT_FULL_ON_FRACTION, ActionFactory.DEFAULT_FULL_OFF_FRACTION);
		
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
	 * @return true on success, false on failure.
	 */
	final private boolean addConstantVelocityWaveToAction(Action action, FireEmitterIterator emitterIter, int travelLength,
										                  int width, double totalDurationInSecs, double fullOnFraction, double fullOffFraction) {
		
		// Make sure all the provided parameters are correct
		if (action == null || emitterIter == null || totalDurationInSecs < 0.001 || travelLength <= 0 || width <= 0 ||
			fullOnFraction < 0.0 || fullOnFraction > 1.0 || fullOffFraction < 0.0 || fullOffFraction > 1.0 ||
			(fullOnFraction + fullOffFraction) > 1.0) {
			
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
		double startMaxIntensityTime = (onTimePerLerp - maxOnTimePerLerp) / 2.0;
		double endMaxIntensityTime   = startMaxIntensityTime + maxOnTimePerLerp;
		double startDelayTime        = onTimePerLerp;
		
		MultiLerp intensityLerp = this.buildIntensityMultiLerp(startMaxIntensityTime,
				endMaxIntensityTime, startDelayTime, durationPerLerp);
		if (intensityLerp == null) {
			assert(false);
			return false;
		}
		
		return action.addConstantVelocityFireEmitterWave(emitterIter, travelLength, width, intensityLerp);
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
	 * @return true on success, false on failure.
	 */
	final private boolean addAcceleratingWaveToAction(Action action, FireEmitterIterator emitterIter, int travelLength,
			                                          int width, double totalDurationInSecs, double acceleration, 
			                                          double fullOnFraction, double fullOffFraction) {
		
		// Make sure all the provided parameters are correct
		if (action == null || emitterIter == null || totalDurationInSecs < 0.001 || travelLength <= 0 || width <= 0 ||
			fullOnFraction < 0.0 || fullOnFraction > 1.0 || fullOffFraction < 0.0 || fullOffFraction > 1.0 ||
			(fullOnFraction + fullOffFraction) > 1.0 || acceleration <= 0.0) {
			
			assert(false);
			return false;
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
		
		return action.addFireEmitterWave(emitterIter, width, intensityLerps);
	}
	
	final private boolean addBurstToAction(Action action, FireEmitterIterator emitterIter, int width, int numBursts,
										   double totalDurationInSecs, double fullOnFraction, double fullOffFraction) {
		
		// Make sure all the provided parameters are correct
		if (action == null || emitterIter == null || totalDurationInSecs < 0.001 || width <= 0 || numBursts <= 0 ||
			fullOnFraction < 0.0 || fullOnFraction > 1.0 || fullOffFraction < 0.0 || fullOffFraction > 1.0 ||
			(fullOnFraction + fullOffFraction) > 1.0) {
			
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
		
		return action.addFireEmitterBurst(emitterIter, width, numBursts, intensityLerp);
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
		if (startMaxIntensityTime < 0.0 || startMaxIntensityTime > endMaxIntensityTime ||
		    endMaxIntensityTime > startDelayTime || startDelayTime > endDelayTime) {
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
	
	public static void main(String[] args) {
		GameModel model = new GameModel(new GameConfig(true, 1.0, 60, 3, 0.1f));
		ActionFactory actionFactory = model.getActionFactory();
		
		// One handed attacks...
		
		/*
		// Player one, left handed attack, 1 flame...
		Action p1LHAttack = actionFactory.buildPlayerLeftHandAttack(1, 10.0, 1, 10.0f);
		assert(p1LHAttack != null);
		while (!p1LHAttack.isFinished()) {
			p1LHAttack.tick(0.05);
			
			for (int i = 0; i < 8; i++) {
				System.out.print("Emitter " + i + ": " + model.getFireEmitterModel().getLeftRailEmitter(i).getIntensity() + "|");
			}
			System.out.println();
		}
		
		
		// Player one, right handed attack, 1 flame...
		Action p1RHAttack = actionFactory.buildPlayerRightHandAttack(1, 10.0, 1, 10.0f);
		assert(p1RHAttack != null);
		while (!p1RHAttack.isFinished()) {
			p1RHAttack.tick(0.01666);
			
			for (int i = 0; i < 8; i++) {
				System.out.print("Emitter " + i + ": " + model.getFireEmitterModel().getRightRailEmitter(i).getIntensity() + "|");
			}
			System.out.println();
		}
		
		
		// Player two, left handed attack, 1 flame...
		Action p2LHAttack = actionFactory.buildPlayerLeftHandAttack(2, 10.0, 1, 10.0f);
		assert(p2LHAttack != null);
		while (!p2LHAttack.isFinished()) {
			p2LHAttack.tick(0.05);
			
			for (int i = 0; i < 8; i++) {
				System.out.print("Emitter " + i + ": " + model.getFireEmitterModel().getRightRailEmitter(i).getIntensity() + "|");
			}
			System.out.println();
		}
		
		
		// Player two, right handed attack, 1 flame...
		Action p2RHAttack = actionFactory.buildPlayerRightHandAttack(2, 10.0, 1, 10.0f);
		assert(p2RHAttack != null);
		while (!p2RHAttack.isFinished()) {
			p2RHAttack.tick(0.01666);
			
			for (int i = 0; i < 8; i++) {
				System.out.print("Emitter " + i + ": " + model.getFireEmitterModel().getLeftRailEmitter(i).getIntensity() + "|");
			}
			System.out.println();
		}
		
		// Player one, two handed attack, 1 flame
		Action p1TwoHAttack = actionFactory.buildPlayerTwoHandedSymetricalAttack(1, 10.0, 1, 10.0f);
		assert(p1TwoHAttack != null);
		while (!p1TwoHAttack.isFinished()) {
			p1TwoHAttack.tick(0.01666);
			
			System.out.print("LEFT RAIL: ");
			for (int i = 0; i < 8; i++) {
				System.out.print("Emitter " + i + ": " + model.getFireEmitterModel().getLeftRailEmitter(i).getIntensity() + "|");
			}
			System.out.print(" || RIGHT RAIL: ");
			for (int i = 0; i < 8; i++) {
				System.out.print("Emitter " + i + ": " + model.getFireEmitterModel().getRightRailEmitter(i).getIntensity() + "|");
			}			
			System.out.println();
		}

		Action outerRingVictoryAction = actionFactory.buildBurstAction(GameModel.Entity.RINGMASTER_ENTITY, FireEmitter.Location.OUTER_RING, 1.0, 1);
		assert(outerRingVictoryAction != null);
		while (!outerRingVictoryAction.isFinished()) {
			outerRingVictoryAction.tick(0.01666);
			
			System.out.print("OUTER RING: ");
			for (int i = 0; i < 16; i++) {
				System.out.print("Emitter " + i + ": " + model.getFireEmitterModel().getOuterRingEmitter(i, false).getIntensity() + "|");
			}	
			System.out.println();
		}
		
		
		Action leftRailBurst = actionFactory.buildBurstAction(GameModel.Entity.PLAYER1_ENTITY, FireEmitter.Location.LEFT_RAIL, 1.0, 1);
		assert(leftRailBurst != null);
		while (!leftRailBurst.isFinished()) {
			leftRailBurst.tick(0.01666);
			
			System.out.print("LEFT RAIL: ");
			for (int i = 0; i < 8; i++) {
				System.out.print("Emitter " + i + ": " + model.getFireEmitterModel().getLeftRailEmitter(i).getIntensity() + "|");
			}
			System.out.println();
		}
		*/
		Action rightRailBurst = actionFactory.buildCrowdPleaserBurstAction(GameModel.Entity.PLAYER1_ENTITY, FireEmitter.Location.RIGHT_RAIL, 1.0, 1);
		assert(rightRailBurst != null);
		while (!rightRailBurst.isFinished()) {
			rightRailBurst.tick(0.01666);
			
			System.out.print("RIGHT RAIL: ");
			for (int i = 0; i < 8; i++) {
				System.out.print("Emitter " + i + ": " + model.getFireEmitterModel().getRightRailEmitter(i).getIntensity() + "|");
			}
			System.out.println();
		}		
		
	}
}
