package ca.site3.ssf.gamemodel;

import java.util.Iterator;

class PlayerAttackAction extends Action {
	
	final private Player attacker;
	final private Player attackee;
	
	final private float damagePerFlame; // Amount of damage dealt to the attackee per flame delivered
		
	PlayerAttackAction(FireEmitterModel fireEmitterModel, Player attacker, Player attackee, float dmgPerFlame) {
		
		super(fireEmitterModel);
		
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
	void blockOccurred(int simIndex) {
		// When a block occurs on a particular simulator we need to propogate the effects
		// of that block to each of the simulators that are after it - this will cancel out
		// one of the flames on each of the successive simulators
		for (int i = simIndex+1; i < this.orderedFireSims.size(); i++) {
			this.orderedFireSims.get(i).flameBlocked();
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
	
}
