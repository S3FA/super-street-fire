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
	
	/**
	 * Add a wave of fire emitter simulation to the given action.
	 * @param action The action to add a wave to.
	 * @param location The location in the SSF arena where the wave will take place.
	 * @param startIndex The index of the fire emitter in the given location where the wave will start.
	 * @param travelLength The length of travel of the action.
	 * @param width The width of the action (i.e., how many simultaneous flames as the wave travels).
	 * @param totalDurationInSecs Total duration of the wave (length of time it will have fire emitters simulating for, in total).
	 * @param fullOnFraction The fraction [0,1] of time that the emitters will be turned completely on.
	 * @param offFraction The fraction of time that the emitters will be turned completely off.
	 * @return true on success, false on failure.
	 */
	final private boolean AddWaveToAction(Action action, FireEmitter.Location location, int startIndex, int travelLength,
										  int width, double totalDurationInSecs, double fullOnFraction, double offFraction) {
		
		// Make sure all the provided parameters are correct
		if (action == null || totalDurationInSecs < 0.001 || travelLength <= 0 || width <= 0 ||
			fullOnFraction < 0.0 || fullOnFraction > 1.0 || offFraction < 0.0 || offFraction > 1.0 ||
			(fullOnFraction + offFraction) > 1.0) {
			
			assert(false);
			return false;
		}

		// Calculate values on a per-lerp basis (i.e., time of each on/off cycle per fire emitter in the wave)
		// NOTE: Since the wave form cascades over the travel length of emitters, it causes the
		// duration for each lerp cycle to be (totalDuration / (double)(width + travelLength - 1)).
		double durationPerLerp  = totalDurationInSecs / (double)(width + travelLength - 1);
		double offTimePerLerp   = durationPerLerp * offFraction;
		double onTimePerLerp    = durationPerLerp - offTimePerLerp;
		double maxOnTimePerLerp = durationPerLerp * fullOnFraction;
		
		assert(onTimePerLerp >= maxOnTimePerLerp);
		double startMaxIntensityTime = (onTimePerLerp - maxOnTimePerLerp) / 2.0;
		double endMaxIntensityTime   = startMaxIntensityTime + maxOnTimePerLerp;
		double startDelayTime        = onTimePerLerp;
		
		MultiLerp intensityLerp = this.BuildIntensityMultiLerp(startMaxIntensityTime,
				endMaxIntensityTime, startDelayTime, durationPerLerp);
		if (intensityLerp == null) {
			assert(false);
			return false;
		}
		
		return action.AddFireEmitterWave(location, startIndex, travelLength, width, intensityLerp);
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
		if (startMaxIntensityTime <= 0.0 || startMaxIntensityTime > endMaxIntensityTime ||
		    endMaxIntensityTime > startDelayTime || startDelayTime > endDelayTime) {
			assert(false);
			return null;
		}
		
		double[] timeValues      = { 0.0, startMaxIntensityTime, endMaxIntensityTime, startDelayTime, endDelayTime };
		double[] intensityValues = { 0.0, 1.0, 1.0, 0.0, 0.0 };
		
		return new MultiLerp(timeValues, intensityValues);
	}
	
	
}
