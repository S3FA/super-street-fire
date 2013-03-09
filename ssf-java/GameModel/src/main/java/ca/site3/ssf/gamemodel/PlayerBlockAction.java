package ca.site3.ssf.gamemodel;

import java.util.ArrayList;
import java.util.Iterator;

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
		// do nothing currently, the block will stay sustained just like in street fighter.
	}
	
	void merge(PlayerBlockAction actionToMerge) {
		if (actionToMerge.blocker != this.blocker) {
			return;
		}

		int minSize = Math.min(actionToMerge.wavesOfOrderedFireSims.size(),
				this.wavesOfOrderedFireSims.size());
		ArrayList<FireEmitterSimulator> simsToAdd = new ArrayList<FireEmitterSimulator>(2);
		for (int i = 0; i < minSize; i++) {
			ArrayList<FireEmitterSimulator> thisWave  = this.wavesOfOrderedFireSims.get(i);
			ArrayList<FireEmitterSimulator> mergeWave = actionToMerge.wavesOfOrderedFireSims.get(i);
			
			int minWaveSize = Math.min(thisWave.size(), mergeWave.size());
			for (int j = 0; j < minWaveSize; j++) {
				FireEmitterSimulator thisSim = thisWave.get(j);
				FireEmitterSimulator mergeSim = mergeWave.get(j);
				
				if (!thisSim.merge(mergeSim)) {
					simsToAdd.add(mergeSim);
				}
			}
		}
		
		this.wavesOfOrderedFireSims.add(simsToAdd);
	}
	
	void removeLeftHandedBlocks() {
		ArrayList<FireEmitter> leftEmitters = this.fireEmitterModel.getPlayerLeftEmitters(this.blocker.getPlayerNumber());
		Iterator<ArrayList<FireEmitterSimulator>> waveIter = this.wavesOfOrderedFireSims.iterator();
		
		while (waveIter.hasNext()) {
			ArrayList<FireEmitterSimulator> currWave = waveIter.next();
			Iterator<FireEmitterSimulator> simIter   = currWave.iterator();
			
			while (simIter.hasNext()) {
				FireEmitterSimulator currSim = simIter.next();
				if (leftEmitters.contains(currSim.getEmitter())) {
					currSim.kill();
					simIter.remove();
				}
			}
			
			if (currWave.isEmpty()) {
				waveIter.remove();
			}
		}
	}
	
	void removeRightHandedBlocks() {
		ArrayList<FireEmitter> rightEmitters = this.fireEmitterModel.getPlayerRightEmitters(this.blocker.getPlayerNumber());
		Iterator<ArrayList<FireEmitterSimulator>> waveIter = this.wavesOfOrderedFireSims.iterator();
		
		while (waveIter.hasNext()) {
			ArrayList<FireEmitterSimulator> currWave = waveIter.next();
			Iterator<FireEmitterSimulator> simIter   = currWave.iterator();
			
			while (simIter.hasNext()) {
				FireEmitterSimulator currSim = simIter.next();
				if (rightEmitters.contains(currSim.getEmitter())) {
					currSim.kill();
					simIter.remove();
				}
			}
			
			if (currWave.isEmpty()) {
				waveIter.remove();
			}
		}
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
		// Raise an event for the action...
		GameModelActionSignaller actionSignaller = this.fireEmitterModel.getActionSignaller();
		assert(actionSignaller != null);
		actionSignaller.fireOnPlayerBlockAction(this.getBlocker().getPlayerNumber());
	}	
}
