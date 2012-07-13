package ca.site3.ssf.guiprotocol;

/**
 * Commands that can be sent from a GUI (or wherever) to the system, e.g.
 * to query hardware status.
 */
public class SystemCommand {

	public enum SystemCommandType {
		/**
		 * Queries attached hardware for status
		 */
		QUERY_SYSTEM_INFO
	}
	
	private final SystemCommandType type;
	
	public SystemCommand(SystemCommandType t) {
		type = t;
	}
	
	public SystemCommandType getType() {
		return type;
	}
}
