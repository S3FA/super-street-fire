package ca.site3.ssf.gamemodel;

import java.util.ArrayList;
import java.util.Iterator;

class PlayerBlockAction extends Action {

	final private Player blocker;
	
	private ArrayList<PlayerAttackAction> relevantIncomingAttacks = new ArrayList<PlayerAttackAction>(5);
	
	PlayerBlockAction(FireEmitterModel fireEmitterModel, Player blocker) {
		super(fireEmitterModel);
		this.blocker = blocker;
		assert(blocker != null);
	}

	Player getBlocker() {
		return this.blocker;
	}
	
	/**
	 * Add an attack action that might be blocked by this block action, when the block
	 * is activated, it will be considered as one of the attacks to block against.
	 * @param attack The attack to add.
	 */
	void addRelevantIncomingAttackToBlock(PlayerAttackAction attack) {
		
		// The attack isn't relevant if the attacker is the blocker!
		if (attack.getAttacker() == blocker) {
			assert(false);
			return;
		}
		
		this.relevantIncomingAttacks.add(attack);
	}
	
	
	@Override
	boolean tickSimulator(double dT, FireEmitterSimulator simulator) {
		
		simulator.tick(this, dT);
		return simulator.isFinished();
		
	}
	
	@Override
	FireEmitter.FlameType getActionFlameType() {
		return FireEmitter.FlameType.BLOCK_FLAME;
	}

	@Override
	GameModel.Entity getContributorEntity() {
		return this.getBlocker().getEntity();
	}
	
	@Override
	void onFirstTick() {
		
		// Check to see whether the block is going to do anything at all 
		// (i.e., is it effective against any of the attacks that were happening when it was initiated?)...
		float effectiveness = 0.0f;
		boolean wasBlockingEffective = false;
		
		Iterator<PlayerAttackAction> iter = this.relevantIncomingAttacks.iterator();
		while (iter.hasNext()) {
			PlayerAttackAction attack = iter.next();
			effectiveness = attack.block();
			if (effectiveness > 0.0) {
				wasBlockingEffective = true;
			}
		}
		
		// Raise an event for the action...
		GameModelActionSignaller actionSignaller = this.fireEmitterModel.getActionSignaller();
		assert(actionSignaller != null);
		actionSignaller.fireOnPlayerBlockAction(this.getBlocker().getPlayerNumber(), wasBlockingEffective);
		
		// If the block wasn't at all effective then we just kill any effects associated with it
		if (!wasBlockingEffective) {
			this.kill();
		}
	}	
}
