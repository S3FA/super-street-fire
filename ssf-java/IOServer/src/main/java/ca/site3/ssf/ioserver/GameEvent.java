package ca.site3.ssf.ioserver;

import ca.site3.ssf.gamemodel.IGameModel;

/**
 * Package of information received from the {@link IGameModel}.
 * 
 * @author greg
 */
public interface GameEvent {

	enum EventType {
		
	}
	
	EventType getType();
}
