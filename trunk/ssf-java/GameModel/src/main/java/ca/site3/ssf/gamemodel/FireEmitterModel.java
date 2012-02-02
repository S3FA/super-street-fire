package ca.site3.ssf.gamemodel;

import java.util.ArrayList;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class FireEmitterModel {
	private FireEmitterConfig config = null;
	private GameModelActionSignaller actionSignaller = null;
	private Logger logger = null;
	
	private ArrayList<FireEmitter> outerRingEmitters = null; // The outer ring of emitters, starting at the first emitter to the right
															 // of player one's back
	
	private ArrayList<GamePlayFireEmitter> leftRailEmitters  = null; // Left rail is the rail of emitters on the left from player one's perspective,
																	 // they are ordered starting at player one going towards player two
	
	private ArrayList<GamePlayFireEmitter> rightRailEmitters = null; // Right rail is the rail of emitters on the right from player one's perspective,
																	 // they are ordered starting at player one going towards player two
	
	FireEmitterModel(FireEmitterConfig config, GameModelActionSignaller actionSignaller) {
		this.logger = LoggerFactory.getLogger(getClass());
		
		this.config = config;
		assert(this.config != null);
	
		this.actionSignaller = actionSignaller;
		assert(this.actionSignaller != null);
		
		int globalEmitterIDCounter = 0;
		
		// Setup the outer ring emitters...
		if (this.config.isOuterRingEnabled()) {
			this.outerRingEmitters = new ArrayList<FireEmitter>(this.config.getNumOuterRingEmitters());
			for (int i = 0; i < this.config.getNumOuterRingEmitters(); i++) {
				this.outerRingEmitters.add(new FireEmitter(globalEmitterIDCounter++, i, FireEmitter.Location.OUTER_RING));
			}
		}
		
		// Setup the two inner rails of gameplay emitters on either side of the players...
		this.leftRailEmitters  = new ArrayList<GamePlayFireEmitter>(this.config.getNumEmittersPerRail());
		this.rightRailEmitters = new ArrayList<GamePlayFireEmitter>(this.config.getNumEmittersPerRail());
		for (int i = 0; i < this.config.getNumEmittersPerRail(); i++) {
			this.leftRailEmitters.add(new GamePlayFireEmitter(globalEmitterIDCounter++, i, FireEmitter.Location.LEFT_RAIL));
		}
		for (int i = 0; i < this.config.getNumEmittersPerRail(); i++) {
			this.rightRailEmitters.add(new GamePlayFireEmitter(globalEmitterIDCounter++, i, FireEmitter.Location.RIGHT_RAIL));
		}
	}
	
	
	FireEmitter GetOuterRingEmitter(int index) {
		if (!this.config.isOuterRingEnabled()) {
			return null;
		}
		
		if (index >= this.outerRingEmitters.size() || index < 0) {
			assert(false);
			return null;
		}
		
		return this.outerRingEmitters.get(index);
	}
	
	GamePlayFireEmitter GetLeftRailEmitter(int index) {
		if (index >= this.leftRailEmitters.size() || index < 0) {
			assert(false);
			return null;
		}
		return this.leftRailEmitters.get(index);
	}
	
	GamePlayFireEmitter GetRightRailEmitter(int index) {
		if (index >= this.rightRailEmitters.size() || index < 0) {
			assert(false);
			return null;
		}
		return this.rightRailEmitters.get(index);
	}
	

	public static void main(String[] args) {
		FireEmitterModel model = new FireEmitterModel(new FireEmitterConfig(true, 16, 8), new GameModelActionSignaller());
		
	}
	
}
