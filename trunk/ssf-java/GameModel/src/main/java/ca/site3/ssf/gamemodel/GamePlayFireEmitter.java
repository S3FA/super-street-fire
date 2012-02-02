package ca.site3.ssf.gamemodel;

import java.util.EnumSet;


class GamePlayFireEmitter extends FireEmitter {

	enum EmitterColour { PLAYER_ONE_COLOUR, PLAYER_TWO_COLOUR } 
	
	private EnumSet<EmitterColour> colourFlag = EnumSet.noneOf(EmitterColour.class);
	
	GamePlayFireEmitter(int globalEmitterID, int index, FireEmitter.Location location) {
		super(globalEmitterID, index, location);
		// For the time being, right and left rail emitters are the only places allowed for gameplay
		assert(location == FireEmitter.Location.LEFT_RAIL || location == FireEmitter.Location.RIGHT_RAIL);
	}

	@Override
	void reset() {
		super.reset();
		this.colourFlag = EnumSet.noneOf(EmitterColour.class);
	}
	
	void addColour(EmitterColour colour) {
		this.colourFlag.add(colour);
	}
	void removeColour(EmitterColour colour) {
		this.colourFlag.remove(colour);
	}
	boolean hasColour(EmitterColour colour) {
		return this.colourFlag.contains(colour);
	}
	EnumSet<EmitterColour> getOnColours() {
		return this.colourFlag;
	}
	
	public static void main(String[] args) {
		GamePlayFireEmitter emitter = new GamePlayFireEmitter(0, 0, FireEmitter.Location.LEFT_RAIL);
		
		System.out.println(emitter.hasColour(GamePlayFireEmitter.EmitterColour.PLAYER_ONE_COLOUR));
		System.out.println(emitter.hasColour(GamePlayFireEmitter.EmitterColour.PLAYER_TWO_COLOUR));
		
		//emitter.addColour(EmitterColour.PLAYER_ONE_COLOUR);
		emitter.addColour(EmitterColour.PLAYER_TWO_COLOUR);

		System.out.println(emitter.hasColour(GamePlayFireEmitter.EmitterColour.PLAYER_ONE_COLOUR));
		System.out.println(emitter.hasColour(GamePlayFireEmitter.EmitterColour.PLAYER_TWO_COLOUR));
		
	}
}