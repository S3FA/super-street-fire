package ca.site3.ssf.gamemodel;

import java.util.ArrayList;

public class PlayerAttackAction extends Action {
	
	public enum AttackType {
		CUSTOM_UNDEFINED_ATTACK(Integer.MAX_VALUE, Integer.MAX_VALUE),
		
		// Basic Attacks
		LEFT_JAB_ATTACK(Integer.MAX_VALUE, Integer.MAX_VALUE),
		RIGHT_JAB_ATTACK(Integer.MAX_VALUE, Integer.MAX_VALUE), 
		LEFT_HOOK_ATTACK(Integer.MAX_VALUE, Integer.MAX_VALUE),
		RIGHT_HOOK_ATTACK(Integer.MAX_VALUE, Integer.MAX_VALUE),
		LEFT_UPPERCUT_ATTACK(Integer.MAX_VALUE, Integer.MAX_VALUE),
		RIGHT_UPPERCUT_ATTACK(Integer.MAX_VALUE, Integer.MAX_VALUE),
		LEFT_CHOP_ATTACK(Integer.MAX_VALUE, Integer.MAX_VALUE),
		RIGHT_CHOP_ATTACK(Integer.MAX_VALUE, Integer.MAX_VALUE),
		
		// Special Attacks
		HADOUKEN_ATTACK(Integer.MAX_VALUE, 2, 2),
		LEFT_SHORYUKEN_ATTACK(Integer.MAX_VALUE, 3, 3),
		RIGHT_SHORYUKEN_ATTACK(Integer.MAX_VALUE, 3, 3),
		SONIC_BOOM_ATTACK(Integer.MAX_VALUE, 5, 5),
		DOUBLE_LARIAT_ATTACK(Integer.MAX_VALUE, 2, 2),
		QUADRUPLE_LARIAT_ATTACK(Integer.MAX_VALUE, 1, 1),
		SUMO_HEADBUTT_ATTACK(Integer.MAX_VALUE, 2, 2),
		LEFT_ONE_HUNDRED_HAND_SLAP_ATTACK(Integer.MAX_VALUE, 1, 1),
		RIGHT_ONE_HUNDRED_HAND_SLAP_ATTACK(Integer.MAX_VALUE, 1, 1),
		TWO_HANDED_ONE_HUNDRED_HAND_SLAP_ATTACK(Integer.MAX_VALUE, 1, 1),
		PSYCHO_CRUSHER_ATTACK(Integer.MAX_VALUE, 1, 1),
		
		// Easter Egg Attacks
		YMCA_ATTACK(1, 1, 1),
		NYAN_CAT_ATTACK(1, 1, 1),
		DISCO_STU_ATTACK(1, 1),
		ARM_WINDMILL_ATTACK(Integer.MAX_VALUE, 1, 1),
		SUCK_IT_ATTACK(1, 1, 1),
		LEFT_VAFANAPOLI_ATTACK(Integer.MAX_VALUE, 1, 1),
		RIGHT_VAFANAPOLI_ATTACK(Integer.MAX_VALUE, 1, 1);
		
		private final int maxUsesPerRound;         // Maximum of this attack type that are allowed per-round
		private final int numAllowedActiveAtATime; // Maximum of this attack type that are allowed to be active at any given time in a round
		
		private final boolean isActivationGroupLimited;  // Whether this attack type is limited by the global number of active group attacks for a given player
		private final int numActivationsInGroupAtATime;  // If this attack type is activation group limited, then this is the limit
		
		AttackType(int maxUsesPerRound, int numAllowedActiveAtATime, int numActivationsInGroupAtATime) {
			
			assert(maxUsesPerRound >= 0);
			assert(numAllowedActiveAtATime >= 0);
			this.maxUsesPerRound = maxUsesPerRound;
			this.numAllowedActiveAtATime = numAllowedActiveAtATime;
			this.isActivationGroupLimited = true;
			this.numActivationsInGroupAtATime = numActivationsInGroupAtATime;
		} 
		AttackType(int maxUsesPerRound, int numAllowedActiveAtATime) {
			
			assert(maxUsesPerRound >= 0);
			assert(numAllowedActiveAtATime >= 0);
			this.maxUsesPerRound = maxUsesPerRound;
			this.numAllowedActiveAtATime = numAllowedActiveAtATime;
			this.isActivationGroupLimited = false;
			this.numActivationsInGroupAtATime = Integer.MAX_VALUE;
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
	};

	final private AttackType type;
	final private Player attacker;
	final private Player attackee;
	
	final private float damagePerFlame; // Amount of damage dealt to the attackee per flame delivered
		
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
	}
	
	/**
	 * Inform this action that a block (i.e., an attack from the attacker was carried out on the same
	 * emitter where a block was simultaneously occurring from the attackee) has occurred on one of 
	 * its simulators. This function will ensure that the block cancels one of the attack flames in this
	 * attack action.
	 */
	void blockOccurred(int waveIndex, int simulatorIndex) {
		assert(waveIndex >= 0);
		
		// If the waveIndex is out of bounds then just exit, this attack has already
		// been blocked/finished/cleared
		if (waveIndex >= this.wavesOfOrderedFireSims.size()) {
			return;
		}
		
		// Chip damage...
		this.attackee.doChipDamage(this.damagePerFlame);
		
		// When a block occurs on a particular simulator we need to propagate the effects
		// of that block to each of the simulators that are after it - this will cancel out
		// one of the flames on each of the successive simulators
		ArrayList<FireEmitterSimulator> simulatorWave = this.wavesOfOrderedFireSims.get(waveIndex);
		
		// If the simulatorIndex is out of bounds then we just exit, this attack has
		// already finished
		assert(simulatorIndex >= 0);
		if (simulatorIndex >= simulatorWave.size()) {
			return;
		}
		
		for (int i = simulatorIndex+1; i < simulatorWave.size(); i++) {
			simulatorWave.get(i).flameBlocked();
		}
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
		// Raise an event for the action...
		GameModelActionSignaller actionSignaller = this.fireEmitterModel.getActionSignaller();
		assert(actionSignaller != null);
		actionSignaller.fireOnPlayerAttackAction(this.getAttacker().getPlayerNumber(), this.type);
	}
}
