package ca.site3.ssf.gesturerecognizer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;

import ca.site3.ssf.common.MapUtil;

/**
 * Immutable class for holding the results of a full run of the Gesture Recognition Process.
 * @author Callum
 *
 */
final public class GestureRecognitionResult {

	final private Map<GestureType, Probabilities> resultMapping;

	public GestureRecognitionResult(Map<GestureType, Probabilities> resultMapping) {
		this.resultMapping = resultMapping;
		assert(resultMapping != null);
	}
	
	public final Probabilities getProbabilities(GestureType gestureType) {
		return this.resultMapping.get(gestureType);
	}
	
	public String toString() {
		String result = "";
		
		Map<GestureType, Double> probabilitiesMap = new HashMap<GestureType, Double>(this.resultMapping.size());
		for (Entry<GestureType, Probabilities> entry : this.resultMapping.entrySet()) {
			probabilitiesMap.put(entry.getKey(), Math.max(entry.getValue().getBaseProbability(), entry.getValue().getKMeansProbability()));
		}
		
		List<Entry<GestureType, Double>> sortedList = MapUtil.sortMapToList(probabilitiesMap);
		
		ListIterator<Entry<GestureType, Double>> iter = sortedList.listIterator(sortedList.size());
		while (iter.hasPrevious()) {
			Entry<GestureType, Double> entry = iter.previous();
			Probabilities currProbs = this.resultMapping.get(entry.getKey());
			result += entry.getKey().toString() + " - Base: " + currProbs.getBaseProbability() + 
					", KMeans: " + currProbs.getKMeansProbability() + "\n";
		}
		
		return result;
	}
}


