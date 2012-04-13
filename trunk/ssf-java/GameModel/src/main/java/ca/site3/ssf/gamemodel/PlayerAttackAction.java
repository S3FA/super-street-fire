package ca.site3.ssf.gamemodel;

import java.util.ArrayList;

public class PlayerAttackAction extends Action {
	
	public enum AttackType {
		LEFT_JAB_ATTACK, RIGHT_JAB_ATTACK, 
		LEFT_HOOK_ATTACK, RIGHT_HOOK_ATTACK,
		HADOUKEN_ATTACK, SONIC_BOOM_ATTACK
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
