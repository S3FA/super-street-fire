package ca.site3.ssf.gamemodel;

/**
 * If you want to be notified of interesting things that happen during the game,
 * register an IGameModelListener with an {@link IGameModel}.
 * 
 * @author Callum
 * @author Greg
 */
public interface IGameModelListener {
	
	/**
	 * Called for any event that can be listened for in the gamemodel.
	 * @param event The object holding the event information.
	 */
	public void onGameModelEvent(IGameModelEvent event);
	
}
