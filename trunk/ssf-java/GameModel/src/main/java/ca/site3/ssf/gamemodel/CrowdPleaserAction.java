package ca.site3.ssf.gamemodel;


/**
 * Builds a versatile action for pleasing the crowd - has no effect on the game whatsoever,
 * just meant to be showy and glitzy and mark some event that occurred in the game (e.g.,
 * end of a round or match).
 * 
 * @author Callum
 *
 */
class CrowdPleaserAction extends Action {

	final private IGameModel.Entity colourEntity;
	
	CrowdPleaserAction(FireEmitterModel fireEmitterModel, IGameModel.Entity colourEntity) {
		super(fireEmitterModel);
		this.colourEntity = colourEntity;
	}

	@Override
	void tickSimulator(double dT, FireEmitterSimulator simulator) {
		simulator.tick(this, dT);
	}

	@Override
	void onFirstTick() {

	}

	@Override
	IGameModel.Entity getContributorEntity() {
		return this.colourEntity;
	}

	@Override
	FireEmitter.FlameType getActionFlameType() {
		return FireEmitter.FlameType.NON_GAME_FLAME;
	}

}
