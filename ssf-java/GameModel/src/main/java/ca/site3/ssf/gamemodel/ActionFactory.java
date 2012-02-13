package ca.site3.ssf.gamemodel;

import ca.site3.ssf.common.MultiLerp;

/**
 * Publicly exposed factory class for building the various actions/moves for players and
 * the ringmaster in the SSF game.
 * @author Callum
 *
 */
final public class ActionFactory {
	
	public enum PlayerActionType { BLOCK, JAB_ATTACK, HOOK_ATTACK, HADOUKEN_ATTACK, SONIC_BOOM_ATTACK };

	final static private float DEFAULT_FULL_ON_FRACTION  = 0.45f;
	final static private float DEFAULT_FULL_OFF_FRACTION = 0.25f;
	
	final private GameModel gameModel;
	
	ActionFactory(GameModel gameModel) {
		this.gameModel = gameModel;
		assert(gameModel != null);
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
		
		FireEmitterModel fireEmitterModel = this.gameModel.getFireEmitterModel();
		FireEmitterIterator emitterIterLeft  = fireEmitterModel.getPlayerLeftHandStartEmitterIter(playerNum);
		FireEmitterIterator emitterIterRight = fireEmitterModel.getPlayerRightHandStartEmitterIter(playerNum);
		FireEmitterConfig fireEmitterConfig = fireEmitterModel.getConfig();
		
		Player blockerOrAttacker = this.gameModel.getPlayer(playerNum);
		Player attackee = this.gameModel.getPlayer(Player.getOpposingPlayerNum(playerNum));
		
		boolean success = true;
		
		switch (playerActionType) {
			case BLOCK:
				action = new PlayerBlockAction(fireEmitterModel, blockerOrAttacker);

				success &= this.addBurstToAction(action, emitterIterLeft, 1, 1, 1.0, 0.9, 0.01);
				success &= this.addBurstToAction(action, emitterIterRight, 1, 1, 1.0, 0.9, 0.01);
				break;
				
			case JAB_ATTACK:
				assert(leftHand || rightHand);
				if (leftHand) {
					action = new PlayerAttackAction(fireEmitterModel, PlayerAttackAction.AttackType.LEFT_HOOK_ATTACK, blockerOrAttacker, attackee, 4.0f);
					success &= this.addWaveToAction(action, emitterIterLeft, fireEmitterConfig.getNumEmittersPerRail(),
							1, 2.0, DEFAULT_FULL_ON_FRACTION, DEFAULT_FULL_OFF_FRACTION);
				}
				else {
					action = new PlayerAttackAction(fireEmitterModel, PlayerAttackAction.AttackType.RIGHT_HOOK_ATTACK, blockerOrAttacker, attackee, 4.0f);
					success &= this.addWaveToAction(action, emitterIterRight, fireEmitterConfig.getNumEmittersPerRail(),
							1, 2.0, DEFAULT_FULL_ON_FRACTION, DEFAULT_FULL_OFF_FRACTION);
				}
				break;
			
			case HOOK_ATTACK:
				assert(leftHand || rightHand);
				if (leftHand) {
					action = new PlayerAttackAction(fireEmitterModel, PlayerAttackAction.AttackType.LEFT_HOOK_ATTACK, blockerOrAttacker, attackee, 4.0f);
					success &= this.addWaveToAction(action, emitterIterLeft, fireEmitterConfig.getNumEmittersPerRail(),
							2, 2.0, DEFAULT_FULL_ON_FRACTION, DEFAULT_FULL_OFF_FRACTION);
				}
				else {
					action = new PlayerAttackAction(fireEmitterModel, PlayerAttackAction.AttackType.RIGHT_HOOK_ATTACK, blockerOrAttacker, attackee, 4.0f);
					success &= this.addWaveToAction(action, emitterIterRight, fireEmitterConfig.getNumEmittersPerRail(),
							2, 2.0, DEFAULT_FULL_ON_FRACTION, DEFAULT_FULL_OFF_FRACTION);
				}
				break;
				
			case HADOUKEN_ATTACK:
				action = this.buildPlayerTwoHandedSymetricalAttack(PlayerAttackAction.AttackType.HADOUKEN_ATTACK, playerNum, 4.0, 3, 5.0f);
				break;
				
			case SONIC_BOOM_ATTACK:
				action = this.buildPlayerTwoHandedSymetricalAttack(PlayerAttackAction.AttackType.SONIC_BOOM_ATTACK, playerNum, 4.0, 3, 5.0f);
				break;
			
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
		
		success &= this.addWaveToAction(atkAction, startingLHEmitterIter,
				fireEmitterModel.getConfig().getNumEmittersPerRail(), width, totalDurationInSecs,
				ActionFactory.DEFAULT_FULL_ON_FRACTION, ActionFactory.DEFAULT_FULL_OFF_FRACTION);

		success &= this.addWaveToAction(atkAction, startingRHEmitterIter,
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
		
		boolean success = this.addWaveToAction(atkAction, emitterIter,
				fireEmitterModel.getConfig().getNumEmittersPerRail(), width, totalDurationInSecs,
				ActionFactory.DEFAULT_FULL_ON_FRACTION, ActionFactory.DEFAULT_FULL_OFF_FRACTION);
		
		if (!success) {
			assert(false);
			return null;
		}
		
		return atkAction;
	}
	
	/**
	 * Add a wave of fire emitter simulation to the given action.
	 * @param action The action to add a wave to.
	 * @param emitterIter The fire emitter iterator for the wave.
	 * @param travelLength The length of travel of the action.
	 * @param width The width of the action (i.e., how many simultaneous flames as the wave travels).
	 * @param totalDurationInSecs Total duration of the wave (length of time it will have fire emitters simulating for, in total).
	 * @param fullOnFraction The fraction [0,1] of time that the emitters will be turned completely on.
	 * @param fullOffFraction The fraction of time that the emitters will be turned completely off.
	 * @return true on success, false on failure.
	 */
	final private boolean addWaveToAction(Action action, FireEmitterIterator emitterIter, int travelLength,
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
		
		return action.addFireEmitterWave(emitterIter, travelLength, width, intensityLerp);
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
		if (startMaxIntensityTime <= 0.0 || startMaxIntensityTime > endMaxIntensityTime ||
		    endMaxIntensityTime > startDelayTime || startDelayTime > endDelayTime) {
			assert(false);
			return null;
		}
		
		double[] timeValues      = { 0.0, startMaxIntensityTime, endMaxIntensityTime, startDelayTime, endDelayTime };
		double[] intensityValues = { 0.0, 1.0, 1.0, 0.0, 0.0 };
		
		return new MultiLerp(timeValues, intensityValues);
	}
	
	public static void main(String[] args) {
		GameModel model = new GameModel(new GameConfig(true, 1.0, 60, 3));
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
