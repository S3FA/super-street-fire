package ca.site3.ssf.gamemodel;

import ca.site3.ssf.common.MultiLerp;

/**
 * Publically exposed factory class for building the various actions/moves for players and
 * the ringmaster in the SSF game.
 * @author Callum
 *
 */
final public class ActionFactory {
	

	final static private float DEFAULT_FULL_ON_FRACTION  = 0.45f;
	final static private float DEFAULT_FULL_OFF_FRACTION = 0.25f;
	
	final private GameModel gameModel;
	
	ActionFactory(GameModel gameModel) {
		this.gameModel = gameModel;
		assert(gameModel != null);
	}
	
	/**
	 * Build a left-handed player attack action.
	 * @param attackerPlayerNum The player number of the attacker.
	 * @param totalDurationInSecs The total duration (how long it will be in play) of the attack in seconds.
	 * @param width The width of the attack in fire emitters.
	 * @param dmgPerFlame The base damage per flame delivered to the opposing player.
	 * @return The resulting action, null on failure.
	 */
	final public Action buildPlayerLeftHandAttack(int attackerPlayerNum, double totalDurationInSecs, int width, float baseDmgPerFlame) {
		
		int attackeePlayerNum = Player.getOpposingPlayerNum(attackerPlayerNum);
		FireEmitterModel fireEmitterModel = this.gameModel.getFireEmitterModel();
		FireEmitterIterator emitterIter = fireEmitterModel.getPlayerLeftHandStartEmitterIter(attackerPlayerNum);	

		return this.buildPlayerAttack(this.gameModel.getPlayer(attackerPlayerNum), this.gameModel.getPlayer(attackeePlayerNum),
				emitterIter, totalDurationInSecs, width, baseDmgPerFlame);
	}
	final public Action buildPlayerRightHandAttack(int attackerPlayerNum, double totalDurationInSecs, int width, float baseDmgPerFlame) {
		int attackeePlayerNum = Player.getOpposingPlayerNum(attackerPlayerNum);
		FireEmitterModel fireEmitterModel = this.gameModel.getFireEmitterModel();
		FireEmitterIterator emitterIter = fireEmitterModel.getPlayerRightHandStartEmitterIter(attackerPlayerNum);		

		return this.buildPlayerAttack(this.gameModel.getPlayer(attackerPlayerNum), this.gameModel.getPlayer(attackeePlayerNum),
				emitterIter, totalDurationInSecs, width, baseDmgPerFlame);
	}
	
	
	final public Action buildPlayerTwoHandedSymetricalAttack(int attackerPlayerNum, double totalDurationInSecs,
															 int width, float baseDmgPerFlame) {

		int attackeePlayerNum = Player.getOpposingPlayerNum(attackerPlayerNum);
		FireEmitterModel fireEmitterModel = this.gameModel.getFireEmitterModel();
		
		FireEmitterIterator startingLHEmitterIter = fireEmitterModel.getPlayerLeftHandStartEmitterIter(attackerPlayerNum);
		FireEmitterIterator startingRHEmitterIter = fireEmitterModel.getPlayerRightHandStartEmitterIter(attackerPlayerNum);		
		
		Player attacker = this.gameModel.getPlayer(attackerPlayerNum);
		Player attackee = this.gameModel.getPlayer(attackeePlayerNum);
		
		PlayerAttackAction atkAction = new PlayerAttackAction(fireEmitterModel, attacker, attackee, baseDmgPerFlame);
		
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
	
	
	final private Action buildPlayerAttack(Player attacker, Player attackee, FireEmitterIterator emitterIter,
										   double totalDurationInSecs, int width, float baseDmgPerFlame) {
		assert(attacker != null);
		assert(attackee != null);
		assert(emitterIter != null);
		
		FireEmitterModel fireEmitterModel = this.gameModel.getFireEmitterModel();
		PlayerAttackAction atkAction = new PlayerAttackAction(fireEmitterModel, attacker, attackee, baseDmgPerFlame);
		
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
		GameModel model = new GameModel(new GameConfig());
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
		*/
	}
}
