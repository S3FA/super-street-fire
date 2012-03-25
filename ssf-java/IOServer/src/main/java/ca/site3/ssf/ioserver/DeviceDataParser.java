package ca.site3.ssf.ioserver;

import java.net.InetAddress;

import ca.site3.ssf.gamemodel.IGameModel.Entity;
import ca.site3.ssf.ioserver.DeviceConstants.DeviceType;

/**
 * Parses data that comes in over UDP into a {@link DeviceEvent}.
 * 
 * Expected data format is:
 * <pre>
 * TBD!
 * </pre>
 * 
 * @author greg
 */
public class DeviceDataParser implements IDeviceDataParser {

	public DeviceEvent parseDeviceData(byte[] data, InetAddress srcIP) throws Exception {
		
		// TODO figure out actual datagram structure and parse it
		
		Entity src = Entity.PLAYER1_ENTITY;
		DeviceType device = DeviceType.LEFT_GLOVE;
		long timestamp = -1;
		boolean buttonDown = false;
		
		double[] rot = new double[3];
		double[] acc = new double[3];
		double[] heading = new double[3];
		
		return new GloveEvent(src, device, timestamp, buttonDown, rot, acc, heading);
	}

}
