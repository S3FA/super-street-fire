package ca.site3.ssf.gamemodel;

class PlayerBlockAction extends Action {

	final private Player blocker;
	
	PlayerBlockAction(FireEmitterModel fireEmitterModel, Player blocker) {
		super(fireEmitterModel);
		this.blocker = blocker;
		assert(blocker != null);
	}

	
	Player getBlocker() {
		return this.blocker;
	}
	
	void blockOccurred() {
		// do nothing currently, the block will stay sustained just like in street fire.
	}
	
	@Override
	void tickSimulator(double dT, FireEmitterSimulator simulator) {
		simulator.tick(this, dT);
	}
	
	@Override
	FireEmitter.FlameType getActionFlameType() {
		return FireEmitter.FlameType.BLOCK_FLAME;
	}

	@Override
	GameModel.Entity getContributorEntity() {
		return this.getBlocker().getEntity();
	}
}
