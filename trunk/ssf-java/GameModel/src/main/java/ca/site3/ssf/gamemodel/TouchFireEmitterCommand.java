package ca.site3.ssf.gamemodel;

import java.util.EnumSet;

import ca.site3.ssf.gamemodel.IGameModel.Entity;

public final class TouchFireEmitterCommand extends AbstractGameModelCommand {

	private final FireEmitter.Location location;
	private final int index;
	private final float intensity;
	private final EnumSet<Entity> contributors;
	
	public TouchFireEmitterCommand(FireEmitter.Location location, int index,
								 float intensity, EnumSet<Entity> contributors) {
		this.location = location;
		this.index = index;
		this.intensity = intensity;
		this.contributors = contributors.clone();
	}
	
	@Override
	void execute(GameModel gameModel) {
		assert(gameModel != null);
		gameModel.touchFireEmitter(this.location, this.index, this.intensity, this.contributors);
	}

}
