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
