package ca.site3.ssf.gamemodel;

import java.util.ArrayList;

import ca.site3.ssf.gamemodel.FireEmitter.Location;

public class PlayerAttackAction extends Action {
	
	public enum AttackType {
		CUSTOM_UNDEFINED_ATTACK(Integer.MAX_VALUE, Integer.MAX_VALUE, 0),
		
		// Basic Attacks
		LEFT_JAB_ATTACK(Integer.MAX_VALUE, Integer.MAX_VALUE, 10.0f),
		RIGHT_JAB_ATTACK(Integer.MAX_VALUE, Integer.MAX_VALUE, 10.0f), 
		LEFT_HOOK_ATTACK(Integer.MAX_VALUE, Integer.MAX_VALUE, 10.0f),
		RIGHT_HOOK_ATTACK(Integer.MAX_VALUE, Integer.MAX_VALUE, 10.0f),
		LEFT_UPPERCUT_ATTACK(Integer.MAX_VALUE, Integer.MAX_VALUE, 10.0f),
		RIGHT_UPPERCUT_ATTACK(Integer.MAX_VALUE, Integer.MAX_VALUE, 10.0f),
		LEFT_CHOP_ATTACK(Integer.MAX_VALUE, Integer.MAX_VALUE, 10.0f),
		RIGHT_CHOP_ATTACK(Integer.MAX_VALUE, Integer.MAX_VALUE, 10.0f),
		
		// Special Attacks
		HADOUKEN_ATTACK(Integer.MAX_VALUE, 2, 2, 34.0f),
		LEFT_SHORYUKEN_ATTACK(Integer.MAX_VALUE, 2, 2, 34.0f),
		RIGHT_SHORYUKEN_ATTACK(Integer.MAX_VALUE, 2, 2, 34.0f),
		SONIC_BOOM_ATTACK(Integer.MAX_VALUE, 2, 2, 25.0f),
		DOUBLE_LARIAT_ATTACK(Integer.MAX_VALUE, 2, 2, 50.0f),
		SUMO_HEADBUTT_ATTACK(Integer.MAX_VALUE, 2, 2, 50.0f),
		LEFT_ONE_HUNDRED_HAND_SLAP_ATTACK(Integer.MAX_VALUE, 1, 1, 50.0f),
		RIGHT_ONE_HUNDRED_HAND_SLAP_ATTACK(Integer.MAX_VALUE, 1, 1, 50.0f),
		TWO_HANDED_ONE_HUNDRED_HAND_SLAP_ATTACK(Integer.MAX_VALUE, 1, 1, 75.0f),
		PSYCHO_CRUSHER_ATTACK(Integer.MAX_VALUE, 1, 1, 75.0f),
		
		// Easter Egg Attacks
		YMCA_ATTACK(1, 1, 1, 95.0f),
		NYAN_CAT_ATTACK(1, 1, 1, 95.0f),
		ARM_WINDMILL_ATTACK(Integer.MAX_VALUE, 1, 1, 95.0f),
		SUCK_IT_ATTACK(1, 1, 1, 95.0f);
		
		private final int maxUsesPerRound;         // Maximum of this attack type that are allowed per-round
		private final int numAllowedActiveAtATime; // Maximum of this attack type that are allowed to be active at any given time in a round
		private final float actionPointCost;       // Cost, in action points [0,100], required to perform the attack
		
		private final boolean isActivationGroupLimited;  // Whether this attack type is limited by the global number of active group attacks for a given player
		private final int numActivationsInGroupAtATime;  // If this attack type is activation group limited, then this is the limit
		
		AttackType(int maxUsesPerRound, int numAllowedActiveAtATime, int numActivationsInGroupAtATime, float actionPointCost) {
			
			assert(maxUsesPerRound >= 0);
			assert(numAllowedActiveAtATime >= 0);
			this.maxUsesPerRound = maxUsesPerRound;
			this.numAllowedActiveAtATime = numAllowedActiveAtATime;
			this.isActivationGroupLimited = true;
			this.numActivationsInGroupAtATime = numActivationsInGroupAtATime;
			this.actionPointCost = actionPointCost;
		} 
		AttackType(int maxUsesPerRound, int numAllowedActiveAtATime, float actionPointCost) {
			
			assert(maxUsesPerRound >= 0);
			assert(numAllowedActiveAtATime >= 0);
			this.maxUsesPerRound = maxUsesPerRound;
			this.numAllowedActiveAtATime = numAllowedActiveAtATime;
			this.isActivationGroupLimited = false;
			this.numActivationsInGroupAtATime = Integer.MAX_VALUE;
			this.actionPointCost = actionPointCost;
		}
		
		int getMaxUsesPerRound() {
			return this.maxUsesPerRound;
		}
		int getNumAllowedActiveAtATime() {
			return this.numAllowedActiveAtATime;
		}
		boolean getIsActivationGroupLimited() {
			return this.isActivationGroupLimited;
		}
		int getNumActivationsInGroupAtATime() {
			return this.numActivationsInGroupAtATime;
		}
		float getActionPointCost() {
			return this.actionPointCost;
		}
	};

	final private AttackType type;
	final private Player attacker;
	final private Player attackee;
	
	// Amount of damage dealt to the attackee per flame delivered
	final private float damagePerFlame;
	
	private double countdownToBlockSignalInSecs;
	private boolean blockWindowSignaled;
	
	private BlockTimingModel blockTimingModel;
	
	PlayerAttackAction(FireEmitterModel fireEmitterModel, AttackType type,
					   Player attacker, Player attackee, float dmgPerFlame) {
		
		super(fireEmitterModel);
		
		this.type = type;
		
		this.attacker = attacker;
		this.attackee = attackee;
		
		assert(attacker != null);
		assert(attackee != null);
		assert(attacker != attackee);
		
		this.damagePerFlame = dmgPerFlame;
		assert(dmgPerFlame > 0.0f);
		
		this.countdownToBlockSignalInSecs = BlockTimingModel.getBlockWindowTimeBeforeAtkFirstHurt();
		this.blockWindowSignaled = false;
		this.blockTimingModel = new BlockTimingModel(this.attackee.getPlayerNumber(), fireEmitterModel.getActionSignaller());
	}
	
	/**
	 * Inform this action that a block has occurred from the opposing player. The block may or may
	 * not be effective based on the current state of the blockTimingModel of this attack.
	 * @return The effectiveness of the block [0,1] -- 0 is completely not effective, 1 is completely effective.
	 */
	float block() {

		if (this.isFinished()) {
			return 0.0f;
		}
		
		float effectivenessOfBlock = this.blockTimingModel.block();
		assert(effectivenessOfBlock >= 0.0);
		if (effectivenessOfBlock == 0.0) {
			return 0.0f;
		}
		
		// Do damage to the attackee/blocker based on the effectiveness...
		float attackDamageBaseAmt = this.damagePerFlame * this.getTotalNumFlames();
		float damageAfterBlock    = attackDamageBaseAmt - attackDamageBaseAmt * effectivenessOfBlock;
		
		// If chip damage is enabled then it's impossible for a block to be COMPLETELY AND UTTERLY effective
		// (i.e., reduce damage to zero). So we apply chip damage...
		if (GameModel.getGameConfig().getChipDamageOn()) {
			damageAfterBlock = Math.max(1, Math.max(damageAfterBlock, attackDamageBaseAmt * GameModel.getGameConfig().getChipDamagePercentage()));
		}
		
		assert(damageAfterBlock <= attackDamageBaseAmt);
		this.attackee.doDamage(damageAfterBlock);
		
		// Completely cancel out this attack
		// IMPORTANT: Make sure to do this AFTER we calculate the damage
		// since the getTotalNumFlames() will be effected by the call to kill()
		this.kill();
		
		return effectivenessOfBlock;
	}
	
	/**
	 * Inform this action that an attack flame was successfully delivered to the attackee from
	 * the attacker.
	 */
	void attackFlameHitOccurred() {
		this.attackee.doDamage(this.damagePerFlame);
	}
	
	Player getAttacker() {
		return this.attacker;
	}
	Player getAttackee() {
		return this.attackee;
	}
	
	AttackType getAttackType() {
		return this.type;
	}
	
	boolean hasLeftHandedAttack() {
		for (ArrayList<FireEmitterSimulator> simWave : this.wavesOfOrderedFireSims) {
			for (FireEmitterSimulator sim : simWave) {
				ArrayList<FireEmitter> emitters = this.fireEmitterModel.getPlayerLeftEmitters(this.attacker.getPlayerNumber());
				if (emitters.contains(sim.getEmitter())) {
					return true;
				}
			}
		}
		return false;
	}

	boolean hasRightHandedAttack() {
		for (ArrayList<FireEmitterSimulator> simWave : this.wavesOfOrderedFireSims) {
			for (FireEmitterSimulator sim : simWave) {
				ArrayList<FireEmitter> emitters = this.fireEmitterModel.getPlayerRightEmitters(this.attacker.getPlayerNumber());
				if (emitters.contains(sim.getEmitter())) {
					return true;
				}
			}
		}
		return false;
	}
	
	double getMinimumTimeUntilAttackHurtsAttackee() {
		// We need to figure out what the smallest amount of time before the attackee is hurt will be...
		double minTimeToHurt = Double.MAX_VALUE;
		for (ArrayList<FireEmitterSimulator> simWave : this.wavesOfOrderedFireSims) {
			
			// Make sure the wave is on a damage-able location of the arena
			assert(!simWave.isEmpty());
			if (!Location.CanDamageHappenOnLocation(simWave.get(0).getEmitter().getLocation())) {
				continue;
			}
			
			for (FireEmitterSimulator sim : simWave) {
				FireEmitter emitter = sim.getEmitter();
				if (this.fireEmitterModel.isDamageEmitter(this.attackee.getPlayerNumber(), emitter)) {
					minTimeToHurt = Math.min(minTimeToHurt, sim.getInitialDelayInSecs());
				}
			}
		}
		return minTimeToHurt;
	}
	
	@Override
	void kill() {
		super.kill();
		this.blockTimingModel.stopBlockWindow();
	}
	
	@Override
	void tick(double dT) {
		super.tick(dT);
		
		// Figure out when to raise the block window for this attack
		this.countdownToBlockSignalInSecs = Math.max(0.0, this.countdownToBlockSignalInSecs - dT);
		if (this.countdownToBlockSignalInSecs <= 0.0) {
			
			if (this.blockWindowSignaled) {
				this.blockTimingModel.tick(dT);
			}
			else {
				// The one and only chance for the attackee to block the attack is being signalled...
				this.blockTimingModel.startBlockWindow();
				this.blockWindowSignaled = true;
			}
		}
		
	}
	
	@Override
	boolean tickSimulator(double dT, FireEmitterSimulator simulator) {
		simulator.tick(this, dT);
		return simulator.isFinished();
	}
	
	@Override
	FireEmitter.FlameType getActionFlameType() {
		return FireEmitter.FlameType.ATTACK_FLAME;
	}

	@Override
	GameModel.Entity getContributorEntity() {
		return this.getAttacker().getEntity();
	}
	
	@Override
	void onFirstTick() {
		
		GameModelActionSignaller actionSignaller = this.fireEmitterModel.getActionSignaller();
		assert(actionSignaller != null);
		
		// Check to see if the attacker has enough action points to pull off the attack
		if (this.attacker.getActionPoints() >= this.type.getActionPointCost()) {
			// Attacker has enough action points; remove the action point cost of this attack from the player that executed it
			this.attacker.removeActionPoints(this.type.getActionPointCost());
		}
		else {
			// Attacker doesn't have requisite action points -- kill this attack
			this.kill();
			// Signal that this attack failed due to lack of action points
			actionSignaller.fireOnAttackFailedAction(this.attacker.getPlayerNumber(), this.type, PlayerAttackActionFailedEvent.Reason.NOT_ENOUGH_ACTION_POINTS);
			return;
		}
		
		// Calculate the amount of time in seconds before a block signal is raised for this attack...
		// NOTE: The attack must be long enough for a block window to actually happen
		double timeToFirstHurtInSecs = this.getMinimumTimeUntilAttackHurtsAttackee();
		assert(timeToFirstHurtInSecs >= BlockTimingModel.getBlockWindowTimeBeforeAtkFirstHurt());
		this.countdownToBlockSignalInSecs = Math.max(BlockTimingModel.getBlockWindowTimeBeforeAtkFirstHurt(), 
				timeToFirstHurtInSecs - BlockTimingModel.getBlockWindowTimeBeforeAtkFirstHurt());
		this.blockWindowSignaled = false;
		
		// Raise an event for the action...
		actionSignaller.fireOnPlayerAttackAction(this.getAttacker().getPlayerNumber(), this.type);
	}
}
