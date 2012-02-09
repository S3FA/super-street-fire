package ca.site3.ssf.gamemodel;

class RingMasterAction extends Action {

	public RingMasterAction(FireEmitterModel fireEmitterModel) {
		super(fireEmitterModel);
		// TODO Auto-generated constructor stub
	}

	@Override
	void tick(double dT) {
		// TODO Auto-generated method stub

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
		// TODO Auto-generated method stub
		
	}
}
