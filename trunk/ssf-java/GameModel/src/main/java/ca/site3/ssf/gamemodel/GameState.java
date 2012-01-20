package ca.site3.ssf.gamemodel;


/**
 * Models a state of the game. Immutable.
 * 
 * @author greg
 */
public class GameState {

	public final double p1HealthPercent;
	public final double p2HealthPercent;
	
	public final int roundNum;
	
	public final int timeLeftInSecs;
	
	
	
	public GameState(double p1HealthPercent, double p2HealthPercent, int roundNum, int timeLeftInSecs) {
		this.p1HealthPercent = p1HealthPercent;
		this.p2HealthPercent = p2HealthPercent;
		this.roundNum = roundNum;
		this.timeLeftInSecs = timeLeftInSecs;
	}
	
	
	
	public double getHealth(int playerNum) {
		if (playerNum == IGameModel.PLAYER_1) {
			return p1HealthPercent;
		} else if (playerNum == IGameModel.PLAYER_2) {
			return p2HealthPercent;
		} else {
			throw new IllegalArgumentException("No such player: "+playerNum);
		}
	}
	
	
}
