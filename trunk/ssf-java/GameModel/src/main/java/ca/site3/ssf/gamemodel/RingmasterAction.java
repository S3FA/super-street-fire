package ca.site3.ssf.gamemodel;

class RingmasterAction extends Action {
	
	public RingmasterAction(FireEmitterModel fireEmitterModel) {
		super(fireEmitterModel);
	}

	@Override
	FireEmitter.FlameType getActionFlameType() {
		return FireEmitter.FlameType.NON_GAME_FLAME;
	}
	
	@Override
	GameModel.Entity getContributorEntity() {
		return GameModel.Entity.RINGMASTER_ENTITY;
	}

	@Override
	void tickSimulator(double dT, FireEmitterSimulator simulator) {
		simulator.tick(this, dT);
	}

	@Override
	void onFirstTick() {
		// Raise an event for the action...
		GameModelActionSignaller actionSignaller = this.fireEmitterModel.getActionSignaller();
		assert(actionSignaller != null);
		actionSignaller.fireOnRingmasterAction();
	}
}
