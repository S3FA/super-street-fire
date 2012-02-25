package ca.site3.ssf.gamemodel;

/**
 * Interface for the various events that are raised by the gamemodel.
 * @author Callum
 */
public interface IGameModelEvent {
	public enum Type {
		FireEmitterChanged,
		GameStateChanged,
		PlayerHealthChanged,
		RoundPlayTimerChanged,
		RoundBeginTimerChanged,
		RoundEnded,
		MatchEnded,
		PlayerAttackAction,
		PlayerBlockAction,
		RingmasterAction
	};
		
	public Type getType();
}
