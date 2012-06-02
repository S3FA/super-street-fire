package ca.site3.ssf.ioserver;

import java.util.List;

import ca.site3.ssf.gesturerecognizer.GestureInstance;
import ca.site3.ssf.gesturerecognizer.GloveData;

/**
 * GestureInstance that also stores the player the gesture belongs to
 * 
 * @author greg
 */
public class PlayerGestureInstance extends GestureInstance {

	private final int playerNum;
	
	// TODO: Gestures will need to be associated with headset values - these are the distilled headset values
	// that occurred over the course of the player carrying out the gesture.
	//private final double headsetAttention;
	//private final double headsetMeditation;
	
	public PlayerGestureInstance(int playerNum) {
		super();
		this.playerNum = playerNum;
	}

	public PlayerGestureInstance(int playerNum, List<GloveData> leftGloveData, List<GloveData> rightGloveData, List<Double> timePts) {
		super(leftGloveData, rightGloveData, timePts);
		this.playerNum = playerNum;
	}

	public int getPlayerNum() {
		return playerNum;
	}
}
