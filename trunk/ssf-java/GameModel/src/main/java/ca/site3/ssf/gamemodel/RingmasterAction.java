package ca.site3.ssf.gamemodel;

import ca.site3.ssf.gamemodel.IGameModel.Entity;

class RingmasterAction extends Action {
	
	public RingmasterAction(FireEmitterModel fireEmitterModel) {
		super(fireEmitterModel);
	}
	
	@Override
	FireEmitter.FlameType getActionFlameType() {
		return FireEmitter.FlameType.NON_GAME_FLAME;
	}

	@Override
	boolean tickSimulator(double dT, FireEmitterSimulator simulator) {
		simulator.tick(this, dT);
		return simulator.isFinished();
	}

	@Override
	void onFirstTick() {
		// Raise an event for the action...
		GameModelActionSignaller actionSignaller = this.fireEmitterModel.getActionSignaller();
		assert(actionSignaller != null);
		actionSignaller.fireOnRingmasterAction();
	}

	@Override
	Entity getContributorEntity() {
		return GameModel.Entity.RINGMASTER_ENTITY;
	}
}
