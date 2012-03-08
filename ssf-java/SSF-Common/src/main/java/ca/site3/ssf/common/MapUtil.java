package ca.site3.ssf.common;

import java.util.*;
import java.util.Map.Entry;

public class MapUtil {
	
	public static <K, V extends Comparable<? super V>> List<Entry<K, V>> sortMapToList(Map<K, V> map)     {
	    List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>(map.entrySet());
	    Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
	        public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
	            return o1.getValue().compareTo(o2.getValue());
	        }
	    });
	    return list;
	}
	
}