package ca.site3.ssf.ioserver;

import java.net.InetAddress;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.site3.ssf.ioserver.DeviceConstants.Device;



/**
 * Parses glove data using the old python prototype format.
 * 
 * XXX: this assumes a one-to-one datagram to message mapping, which seems okay
 * with the new wifi boards but didn't work with the old radios
 * 
 * @author greg
 */
public class LegacyGloveDataParser implements IDeviceDataParser {

	Logger log = LoggerFactory.getLogger(getClass());
	
	
	public DeviceEvent parseDeviceData(byte[] data, InetAddress src) throws Exception {
		
		String dataStr = new String(data);
		log.debug("Parsing data string '{}'",dataStr);
		if (dataStr.endsWith("|") == false || dataStr.charAt(2) != ':') {
			log.warn("Discarding what looks like an incomplete message: {}",dataStr);
			return null;
		}
		
		Device d = Device.fromLegacyId(dataStr.substring(0,2));
		
		// head, accel, gyros
		String[] sensors = dataStr.substring(3, dataStr.length()-1).split("_");
		if (sensors.length != 3) {
			log.warn("Malformed sensor data: {}",dataStr);
			return null;
		}
		
		double parsedData[][] = new double[3][3];
		try {
			for (int sensorIdx=0; sensorIdx<3; sensorIdx++) {
				String[] components = sensors[sensorIdx].split(",");
				if (components.length != 3) {
					log.warn("Could not split sensor {} into 3 components",sensorIdx);
					return null;
				}
				for (int i=0; i<3; i++) {
					parsedData[sensorIdx][i] = Float.parseFloat(components[i]);
				}
			}
		} catch (NumberFormatException ex) {
			log.error("Could not parse numerical data",ex);
			for (int i=0; i<3; i++) {
				System.out.println("sensor "+i+": "+sensors[i]);
				System.out.println("\t(split):"+i+": "+Arrays.toString(sensors[i].split(",")));
			}
			return null;
		}
		
		return new GloveEvent(d.entity, d.type, System.currentTimeMillis(), true, parsedData[2], parsedData[1], parsedData[0]);
	}
}
