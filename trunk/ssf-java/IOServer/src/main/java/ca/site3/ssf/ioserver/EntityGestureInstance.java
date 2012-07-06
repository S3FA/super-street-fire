package ca.site3.ssf.ioserver;

import java.util.List;

import ca.site3.ssf.gamemodel.IGameModel;
import ca.site3.ssf.gesturerecognizer.GestureInstance;
import ca.site3.ssf.gesturerecognizer.GloveData;

/**
 * GestureInstance that also stores the entity the gesture belongs to along with
 * all of the gesture data (via the super class).
 * 
 * @author greg, Callum
 */
public class EntityGestureInstance extends GestureInstance {

	private final IGameModel.Entity entity;
	
	// TODO: Gestures will need to be associated with headset values - these are the distilled headset values
	// that occurred over the course of the player carrying out the gesture.
	//private final double headsetAttention;
	//private final double headsetMeditation;
	
	public EntityGestureInstance(IGameModel.Entity entity) {
		super();
		assert(entity != null);
		this.entity = entity;
	}

	public EntityGestureInstance(IGameModel.Entity entity, List<GloveData> leftGloveData,
								 List<GloveData> rightGloveData, List<Double> timePts) {
		
		super(leftGloveData, rightGloveData, timePts);
		this.entity = entity;
	}

	public IGameModel.Entity getEntity() {
		return this.entity;
	}
}
