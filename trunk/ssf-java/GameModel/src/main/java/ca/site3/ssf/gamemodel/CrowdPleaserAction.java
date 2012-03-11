package ca.site3.ssf.gamemodel;

import java.util.ArrayList;

import ca.site3.ssf.common.MultiLerp;

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
	
	/**
	 * Allows the appending of fire bursts to the existing action.
	 * @param fireEmitter The fire emitter that will be emitting flames.
	 * @param numBursts The number of bursts of the given intensity lerp to perform.
	 * @param intensityLerp The intensity interpolation(s) to perform.
	 * @return true on success, false on failure.
	 */
	boolean addFireBursts(FireEmitter fireEmitter, int numBursts, MultiLerp intensityLerp) {
		// Make sure the parameters are at least moderately correct
		if (intensityLerp == null || fireEmitter == null || numBursts <= 0) {
			assert(false);
			return false;
		}
		
		if (this.wavesOfOrderedFireSims.isEmpty()) {
			this.wavesOfOrderedFireSims.add(new ArrayList<FireEmitterSimulator>(10));
		}
		ArrayList<FireEmitterSimulator> fireSims = this.wavesOfOrderedFireSims.get(0);
		fireSims.add(new FireEmitterSimulator(this, fireEmitter, 0, 0, 0.0, numBursts, (MultiLerp)intensityLerp.clone()));
		return true;
	}	
	
	@Override
	boolean tickSimulator(double dT, FireEmitterSimulator simulator) {
		simulator.tick(this, dT);
		return simulator.isFinished();
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
