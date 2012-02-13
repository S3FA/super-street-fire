package ca.site3.ssf.gamemodel;

import java.util.ArrayList;

class PlayerAttackAction extends Action {
	
	public enum AttackType { UNDEFINED_ATTACK, LEFT_JAB_ATTACK, RIGHT_JAB_ATTACK,
		LEFT_HOOK_ATTACK, RIGHT_HOOK_ATTACK, HADOUKEN_ATTACK, SONIC_BOOM_ATTACK };
	
	final private AttackType type;
	final private Player attacker;
	final private Player attackee;
	
	final private float damagePerFlame; // Amount of damage dealt to the attackee per flame delivered
		
	PlayerAttackAction(FireEmitterModel fireEmitterModel, AttackType type, Player attacker, Player attackee, float dmgPerFlame) {
		
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
	 * @param simIndex The index of the simulator in this action where the block occurred.
	 */
	void blockOccurred(int waveIndex, int simulatorIndex) {
		assert(waveIndex >= 0 && waveIndex < this.wavesOfOrderedFireSims.size());
		
		// When a block occurs on a particular simulator we need to propagate the effects
		// of that block to each of the simulators that are after it - this will cancel out
		// one of the flames on each of the successive simulators
		ArrayList<FireEmitterSimulator> simulatorWave = this.wavesOfOrderedFireSims.get(waveIndex);
		assert(simulatorIndex >= 0 && simulatorIndex < simulatorWave.size());
		
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
	void tickSimulator(double dT, FireEmitterSimulator simulator) {
		simulator.tick(this, dT);
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
