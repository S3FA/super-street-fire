package ca.site3.ssf.gamemodel;

import java.util.ArrayList;

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
	
	void merge(PlayerBlockAction actionToMerge) {
		if (actionToMerge.blocker != this.blocker) {
			return;
		}

		int minSize = Math.min(actionToMerge.wavesOfOrderedFireSims.size(),
				this.wavesOfOrderedFireSims.size());
		for (int i = 0; i < minSize; i++) {
			ArrayList<FireEmitterSimulator> thisWave  = this.wavesOfOrderedFireSims.get(i);
			ArrayList<FireEmitterSimulator> mergeWave = actionToMerge.wavesOfOrderedFireSims.get(i);
			int minWaveSize = Math.min(thisWave.size(), mergeWave.size());
			
			for (int j = 0; j < minWaveSize; j++) {
				FireEmitterSimulator thisSim = thisWave.get(j);
				FireEmitterSimulator mergeSim = mergeWave.get(j);
				
				thisSim.merge(mergeSim);
			}
		}
		
	}
	
	@Override
	boolean tickSimulator(double dT, FireEmitterSimulator simulator) {
		simulator.tick(this, dT);
		
		//if (simulator.isFinished()) {
		//	this.fireEmitterModel.
		//}
		
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
		// Raise an event for the action...
		GameModelActionSignaller actionSignaller = this.fireEmitterModel.getActionSignaller();
		assert(actionSignaller != null);
		actionSignaller.fireOnPlayerBlockAction(this.getBlocker().getPlayerNumber());
	}	
}
