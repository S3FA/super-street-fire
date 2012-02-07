package ca.site3.ssf.gamemodel;

import ca.site3.ssf.common.MultiLerp;


final public class ActionFactory {
	private enum PlayerHand { LEFT_HAND, RIGHT_HAND};
	
	final private FireEmitterModel fireEmitterModel;
	
	ActionFactory(FireEmitterModel fireEmitterModel) {
		this.fireEmitterModel = fireEmitterModel;
		assert(fireEmitterModel != null);
	}
	
	
	//final public Action BuildPlayerLeftHandAction(int playerNum, PlayerHand hand, double totalDurationInSecs, double burstDelayPercent, int width) {
	//}
	//final public Action BuildPlayerRightHandAction(int playerNum, PlayerHand hand, double totalDurationInSecs, double burstDelayPercent, int width) {
	//}
	//final public Action BuildPlayerTwoHandedSymetricalAction(int playerNum, PlayerHand hand, double totalDurationInSecs, double burstDelayPercent, int width) {
	//}
	
	final private boolean AddWaveToAction(Action action, FireEmitter.Location location, int startIndex, int travelLength,
										  int width, double totalDurationInSecs, double burstDelayFraction) {
		
		// Make sure all the provided parameters are correct
		if (action == null || totalDurationInSecs < 0.001) {
			assert(false);
			return false;
		}

		
	}
	
	/**
	 * Builds a piece-wise linear function that describes a stepped wave function for changing
	 * the intensity value of a fire emitter. The step will look as follows:
	 * 
	 * intensity
	 *    1|   ___________________
	 *     |  /                   \
	 *     | /                     \
	 *    0|/_______________________\__________   time
	 *      0  B                 C   D        E      
	 *
	 * @param startMaxIntensityTime (B) The time where the first maximum value of the intensity is achieved
	 * @param endMaxIntensityTime (C) The time where the last maximum value of the intensity is achieved
	 * @param startDelayTime (D) The time where the value of the intensity is zero and will continue to be zero.
	 * @param endDelayTime (E) The last time value of the function.
	 * @return The resulting MultiLerp object with the above function in it, null on failure.
	 */
	final private MultiLerp BuildIntensityMultiLerp(double startMaxIntensityTime, double endMaxIntensityTime,
			                                        double startDelayTime, double endDelayTime) {
		
		// Make sure the provided parameters are in sequential order from least to greatest
		if (startMaxIntensityTime > endMaxIntensityTime || endMaxIntensityTime > startDelayTime || startDelayTime > endDelayTime) {
			assert(false);
			return null;
		}
		
		double[] timeValues      = { 0.0, startMaxIntensityTime, endMaxIntensityTime, startDelayTime, endDelayTime };
		double[] intensityValues = { 0.0, 1.0, 1.0, 0.0, 0.0 };
		
		return new MultiLerp(timeValues, intensityValues);
	}
	
	
}
