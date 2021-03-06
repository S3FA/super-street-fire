package ca.site3.ssf.gamemodel;

/**
 * A class that represents a player of the Super Street Fire game.
 * @author Callum
 *
 */
class Player {
	
	public static final float KO_HEALTH   = 0.0f;
	public static final float FULL_HEALTH = 100.0f;
	public static final float NO_ACTION_POINTS = 0.0f;
	public static final float FULL_ACTION_POINTS = 100.0f;
	
	private int playerNum;              // The player number (1 or 2)
	private float health;               // Current life/health of this player
	private float actionPoints;         // Current number of action points that can be spent by this player on attacks
	private int numRoundWins;           // Number of round wins that this player currently has
	private boolean isInvincible;       // Whether 'god-mode'/invincibility is enabled or not
	private boolean hasInfiniteMoves;   // Whether or not the player has the ability to do any type of move as much as they want
	private float lastDmgAmount;        // The amount of damage that this player suffered last

	private GameModelActionSignaller actionSignaller = null;
	
	Player(int playerNum, GameModelActionSignaller actionSignaller) {
		assert(playerNum == 1 || playerNum == 2);
		
		// Set the signaller before doing anything else!
		this.actionSignaller = actionSignaller;
		assert(this.actionSignaller != null);
	
		this.matchReset();
		this.resetHealth();
		this.resetActionPoints();
		
		this.isInvincible = false;
		this.hasInfiniteMoves = false;
		this.playerNum = playerNum;
	}
	
	/**
	 * Reset the player's win/loss record and health. (e.g., do this whenever
	 * starting an new match).
	 */
	void matchReset() {
		this.numRoundWins  = 0;
	}

	void setHasInfiniteMoves(boolean activated) {
		this.hasInfiniteMoves = activated;
	}
	boolean getHasInfiniteMoves() {
		return this.hasInfiniteMoves;
	}

	/**
	 * Reset a player's health.
	 */
	void resetHealth() {
		this.setHealth(Player.FULL_HEALTH);
		this.lastDmgAmount = 0;
	}
	
	void clearHealth() {
		this.setHealth(Player.KO_HEALTH);
		this.lastDmgAmount = 0;
	}
	
	void tick(double dT) {
		
		// Players will constantly regenerate action points while the game is being played...
		this.setActionPoints((float)Math.min(Player.FULL_ACTION_POINTS, 
				this.actionPoints + dT * GameModel.getGameConfig().getActionPointRegenRate()));
	}
	
	/**
	 * Do damage to this player of the following amount.
	 * @param damageAmt The amount of damage to do.
	 */
	void doDamage(float damageAmt) {
		assert(damageAmt > 0);
		if (this.isInvincible) {
			return;
		}
		this.setHealth(this.health - damageAmt);
		this.lastDmgAmount = damageAmt;
	}
	
	/**
	 * Sets the health of this player to the given amount, this will also cause
	 * an event to occur within the game model signaling a health change.
	 * @param health The amount to set for this player's health.
	 */
	void setHealth(float health) {
		float healthBefore = this.health;
		this.health = Math.min(Player.FULL_HEALTH, health);
		
		// If the health actually changed then trigger an event to indicate the change to all gamemodel listeners
		if (Math.floor(this.health) != Math.floor(healthBefore)) {
			this.actionSignaller.fireOnPlayerHealthChanged(this.playerNum, healthBefore, this.health);
		}
	}
	
	void resetActionPoints() {
		this.setActionPoints(Player.FULL_ACTION_POINTS);
	}
	void clearActionPoints() {
		this.setActionPoints(Player.NO_ACTION_POINTS);
	}
	
	void removeActionPoints(float actionPts) {
		if (actionPts > this.actionPoints) {
			assert(false);
			this.setActionPoints(0);
			return;
		}
		
		this.setActionPoints(this.actionPoints - actionPts);
	}
	
	/**
	 * Sets the action points of this player to the given amount, this will also cause
	 * an event to occur within the game model signaling a action point change.
	 * @param actionPts The amount to set for this player's action points.
	 */
	void setActionPoints(float actionPts) {
		assert(actionPts >= Player.NO_ACTION_POINTS && actionPts <= Player.FULL_ACTION_POINTS);
		
		float actionPtsBefore = this.actionPoints;
		this.actionPoints = actionPts;
		
		// If the action points actually changed then trigger an event to indicate the change to all gamemodel listeners
		if (Math.floor(this.actionPoints) != Math.floor(actionPtsBefore)) {
			this.actionSignaller.fireOnPlayerActionPointsChanged(this.playerNum, actionPtsBefore, this.actionPoints);
		}
	}
	
	
	/**
	 * Set this player as invincible.
	 * @param invincibilityOn true if invincible, false if not.
	 */
	void setInvincible(boolean invincibilityOn) {
		this.isInvincible = invincibilityOn;
	}
	boolean getIsInvincible() {
		return this.isInvincible;
	}
	/**
	 * Query whether this player is knocked-out.
	 * @return true if KOed, false if not.
	 */
	boolean isKOed() {
		return (this.health <= Player.KO_HEALTH);
	}
	
	/**
	 * Gets this player's number in the game.
	 * @return 1 or 2, depending on this player's number.
	 */
	int getPlayerNumber() {
		return this.playerNum;
	}
	
	/**
	 * Gets the health of this player, truncated to ensure it's in the domain/interval of [KO_HEALTH, FULL_HEALTH].
	 * @return The health amount.
	 */
	float getHealth() {
		return Math.max(this.health, Player.KO_HEALTH);
	}
	
	/**
	 * Gets the number of action points for this player.
	 * @return The action point amount, in the interval [NO_ACTION_POINTS, FULL_ACTION_POINTS].
	 */
	float getActionPoints() {
		return this.actionPoints;
	}
	
	/**
	 * Gets the health of this player, not truncated to the domain/interval... it can assume any value in
	 * [-Float.MAX_VALUE, FULL_HEALTH].
	 * @return The non-truncated health amount.
	 */
	float getNonTruncatedHealth() {
		return this.health;
	}
	
	/**
	 * Gets the number of rounds this player has one during the current match.
	 * @return The number of rounds won.
	 */
	int getNumRoundWins() {
		return this.numRoundWins;
	}
	
	/**
	 * Gets the amount of damage that was done to this player during the last damage taking
	 * it had. This is reset whenever health is reset.
	 * @return The amount of damage this player took last.
	 */
	float getLastDamageAmount() {
		return this.lastDmgAmount;
	}
	
	/**
	 * Increments the number of rounds won for this player.
	 */
	void incrementNumRoundWins() {
		this.numRoundWins++;
	}
	
	/**
	 * Gets the enumerated entity represented by this player.
	 * @return The entity enumeration of the player.
	 */
	GameModel.Entity getEntity() {
		switch (this.playerNum) {
		case 1: return GameModel.Entity.PLAYER1_ENTITY;
		case 2: return GameModel.Entity.PLAYER2_ENTITY;
		default:
			assert(false);
			return GameModel.Entity.PLAYER1_ENTITY;
		}
	}
	
	/**
	 * Gets the number of the player that isn't this the one given.
	 * @param playerNum The player whose opponent's number we want.
	 * @return The opposing player number of the given playerNum.
	 */
	static int getOpposingPlayerNum(int playerNum) {
		switch (playerNum) {
		case 1: return 2;
		case 2: return 1;
		default:
			assert(false);
			return 1;
		}
	}
	
}
