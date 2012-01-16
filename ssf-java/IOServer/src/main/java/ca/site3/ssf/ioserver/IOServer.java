package ca.site3.ssf.ioserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Entry point for the Super Street Fire I/O server. Handles initialization etc.
 * 
 * @author greg
 *
 */
public class IOServer {
	
	final Logger log = LoggerFactory.getLogger(getClass());
	
	
	public void start() {
		
		log.info("Starting I/O server");
	}
	
	
	public static void main(String[] args) {
		
		IOServer server = new IOServer();
		server.start();
		
	}
}
