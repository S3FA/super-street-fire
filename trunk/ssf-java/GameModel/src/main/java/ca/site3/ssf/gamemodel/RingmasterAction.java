package ca.site3.ssf.gamemodel;

import ca.site3.ssf.gamemodel.IGameModel.Entity;

public class RingmasterAction extends Action {
	
	public enum ActionType {
		RINGMASTER_LEFT_HALF_RING_ACTION,
		RINGMASTER_RIGHT_HALF_RING_ACTION,
		RINGMASTER_LEFT_JAB_ACTION,
		RINGMASTER_RIGHT_JAB_ACTION,
		RINGMASTER_ERUPTION_ACTION,
		RINGMASTER_LEFT_CIRCLE_ACTION,
		RINGMASTER_RIGHT_CIRCLE_ACTION,
		RINGMASTER_HADOUKEN_ACTION,
		RINGMASTER_DRUM_ACTION
	}
	
	final private ActionType type;
	
	RingmasterAction(FireEmitterModel fireEmitterModel, RingmasterAction.ActionType type) {
		super(fireEmitterModel);
		this.type = type;
		assert(type != null);
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
		actionSignaller.fireOnRingmasterAction(this.type);
	}

	@Override
	Entity getContributorEntity() {
		return GameModel.Entity.RINGMASTER_ENTITY;
	}
}
