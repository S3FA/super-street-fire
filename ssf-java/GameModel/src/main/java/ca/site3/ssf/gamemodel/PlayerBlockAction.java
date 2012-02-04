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
		// The action is now officially over.
	}
	
	@Override
	void tick(double dT) {
		// TODO Auto-generated method stub

	}

}
