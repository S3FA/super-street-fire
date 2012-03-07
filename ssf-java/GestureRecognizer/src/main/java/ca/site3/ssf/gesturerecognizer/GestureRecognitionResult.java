package ca.site3.ssf.gesturerecognizer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import ca.site3.ssf.common.MapUtil;

/**
 * Immutable class for holding the results of a full run of the Gesture Recognition Process.
 * @author Callum
 *
 */
final public class GestureRecognitionResult {

	final private Map<GestureType, Double> resultMapping;
	final private ArrayList<GestureType> sortedGestures;
	final private ArrayList<Double> sortedProbabilities;

	public GestureRecognitionResult(Map<GestureType, Double> resultMapping) {
		this.resultMapping = resultMapping;
		assert(resultMapping != null);
		
		List<Entry<GestureType, Double>> sortedEntries = MapUtil.sortMapToList(resultMapping);
		
		this.sortedGestures      = new ArrayList<GestureType>(sortedEntries.size());
		this.sortedProbabilities = new ArrayList<Double>(sortedEntries.size());
		
		for (Entry<GestureType, Double> entry : sortedEntries) {
			this.sortedGestures.add(entry.getKey());
			this.sortedProbabilities.add(entry.getValue());
		}
	}
	
	public final double getProbability(GestureType gestureType) {
		return this.resultMapping.get(gestureType);
	}
	
	public final double getWinnerProbability() {
		assert(this.sortedProbabilities.size() > 0);
		return this.sortedProbabilities.get(this.sortedProbabilities.size()-1);
	}
	public final GestureType getWinnerGesture() {
		assert(this.sortedGestures.size() > 0);
		return this.sortedGestures.get(this.sortedGestures.size()-1);
	}
	
	public String toString() {
		String result = "";

		assert(this.sortedGestures.size() == this.sortedProbabilities.size());
		for (int i = this.sortedGestures.size()-1; i >= 0 ; i--) {
			result += this.sortedGestures.get(i).toString() + " : " + this.sortedProbabilities.get(i).doubleValue() + "\n";
		}
		
		return result;
	}

	public static void main(String[] args) {
		Map<GestureType, Double> resultTemp = new HashMap<GestureType, Double>();
		for (GestureType type : GestureType.values()) {
			resultTemp.put(type, Math.random());
		}
		
		GestureRecognitionResult test = new GestureRecognitionResult(resultTemp);
		System.out.println(test.toString());
		System.out.println("Winner: " + test.getWinnerGesture().toString());
	}
	
}


