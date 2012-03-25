package ca.site3.ssf.ioserver;

import ca.site3.ssf.gamemodel.IGameModel.Entity;

/**
 * Constants for describing devices
 * 
 * @author greg
 */
public class DeviceConstants {
	
	
	/**
	 * Enumerates the types of input devices a player or ringmaster may have
	 */
	public enum DeviceType {
		LEFT_GLOVE,
		RIGHT_GLOVE,
		HEADSET
	}
	
	
	/**
	 * A particular device may be identified by its type and the 
	 * Entity the device belongs to.
	 */
	public enum Device {
		P1_LEFT_GLOVE(DeviceType.LEFT_GLOVE, Entity.PLAYER1_ENTITY, "SSFP1L"),
		P1_RIGHT_GLOVE(DeviceType.RIGHT_GLOVE, Entity.PLAYER1_ENTITY, "SSFP1R"),
		P1_HEADSET(DeviceType.HEADSET, Entity.PLAYER1_ENTITY, "SSFP1H"),
		
		P2_LEFT_GLOVE(DeviceType.LEFT_GLOVE, Entity.PLAYER2_ENTITY, "SSFP2L"),
		P2_RIGHT_GLOVE(DeviceType.RIGHT_GLOVE, Entity.PLAYER2_ENTITY, "SSFP2R"),
		P2_HEADSET(DeviceType.HEADSET, Entity.PLAYER2_ENTITY, "SSFP2H"),
		
		RM_LEFT_GLOVE(DeviceType.LEFT_GLOVE, Entity.RINGMASTER_ENTITY, "SSFRML"),
		RM_RIGHT_GLOVE(DeviceType.RIGHT_GLOVE, Entity.RINGMASTER_ENTITY, "SSFRMR");
		
		
		private Device(DeviceType type, Entity entity, String id) {
			this.type = type;
			this.entity = entity;
			this.id = id;
		}
		
		public final DeviceType type;
		public final Entity entity;
		public final String id;
		
		/**
		 * @param deviceid the string sent over the network as device identifier
		 * @return the Device corresponding to deviceid
		 */
		public static Device fromId(String deviceid) {
			for (Device d : Device.values()) {
				if (d.id.equals(deviceid))
					return d;
			}
			throw new IllegalArgumentException("Invalid device id: "+deviceid);
		}
		
		
		public static Device fromLegacyId(String id) {
			if ("1L".equals(id))
				return P1_LEFT_GLOVE;
			else if ("1R".equals(id))
				return P1_RIGHT_GLOVE;
			else if ("2L".equals(id))
				return P2_LEFT_GLOVE;
			else if ("2R".equals(id))
				return P2_RIGHT_GLOVE;
			else
				return null;
		}
	}
}
