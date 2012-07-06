package ca.site3.ssf.gamemodel;

/**
 * Used to raise an event from the game model that's associated with an unrecognized gesture
 * for a particular player. This will indicate that the event needs to be raised.
 * 
 * @author Callum
 * 
 */
public final class RaiseUnrecognizedGestureEventCommand extends AbstractGameModelCommand {

	private final IGameModel.Entity gestureExecutorEntity;
	
	public RaiseUnrecognizedGestureEventCommand(IGameModel.Entity gestureExecutorEntity) {
		super();
		
		assert(gestureExecutorEntity != null);
		this.gestureExecutorEntity = gestureExecutorEntity;
	}
	
	@Override
	void execute(GameModel gameModel) {
		assert(gameModel != null);
		gameModel.raiseUnrecognizedGestureEvent(this.gestureExecutorEntity);
	}

}
