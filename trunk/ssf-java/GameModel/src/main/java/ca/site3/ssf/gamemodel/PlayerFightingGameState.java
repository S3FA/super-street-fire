package ca.site3.ssf.gamemodel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Map;

import ca.site3.ssf.gamemodel.FireEmitter.FlameType;
import ca.site3.ssf.gamemodel.PlayerAttackAction.AttackType;


abstract class PlayerFightingGameState extends GameState {

	protected Collection<Action> activeActions = new ArrayList<Action>();
	
	protected double secsSinceLastP1LeftAction  = Double.MAX_VALUE;
	protected double secsSinceLastP1RightAction = Double.MAX_VALUE;
	protected double secsSinceLastP2LeftAction  = Double.MAX_VALUE;
	protected double secsSinceLastP2RightAction = Double.MAX_VALUE;
	
	// Keep track of the number of attacks executed of each type, for each player, in this fighting state
	protected Map<AttackType, Integer> p1AttacksExecuted = new Hashtable<AttackType, Integer>(AttackType.values().length);
	protected Map<AttackType, Integer> p2AttacksExecuted = new Hashtable<AttackType, Integer>(AttackType.values().length);
	
	// Keep track of the number of active attacks of each type, for each player, in this fighting state
	protected Map<AttackType, Integer> p1AttackTypesCurrentlyActive = new Hashtable<AttackType, Integer>(AttackType.values().length);
	protected Map<AttackType, Integer> p2AttackTypesCurrentlyActive = new Hashtable<AttackType, Integer>(AttackType.values().length);
	
	// Keep track of the number of active attacks that are group limited (i.e., only so many can be active at a given time)
	protected int numP1GroupLimitedActiveAttacks = 0;
	protected int numP2GroupLimitedActiveAttacks = 0;
	
	protected final boolean applyActionLimits;
	
	public PlayerFightingGameState(GameModel gameModel, boolean applyActionLimits) {
		super(gameModel);
		
		this.applyActionLimits = applyActionLimits;
		
		// Clear the complete fire emitter state, just for good measure
		this.gameModel.getFireEmitterModel().resetAllEmitters();
		
		// Make sure both players health is at full
		Player p1 = this.gameModel.getPlayer1();
		Player p2 = this.gameModel.getPlayer2();
		assert(p1 != null && p2 != null);
		
		p1.resetHealth();
		p2.resetHealth();
		
		// Initialize the maps for tracking player attacks
		for (AttackType atkType : AttackType.values()) {
			this.p1AttacksExecuted.put(atkType, new Integer(0));
			this.p2AttacksExecuted.put(atkType, new Integer(0));
			this.p1AttackTypesCurrentlyActive.put(atkType, new Integer(0));
			this.p2AttackTypesCurrentlyActive.put(atkType, new Integer(0));
		}
	}

	boolean isFightingState() {
		return true;
	}
	
	/**
	 * Called from child classes whenever an action is removed from play.
	 * @param action The action being removed.
	 */
	protected void removeAction(Action action) {
		if (action.getActionFlameType() != FlameType.ATTACK_FLAME) {
			return;
		}
		PlayerAttackAction attackAction = (PlayerAttackAction)action;
		assert(attackAction != null);
		
		AttackType attackType = attackAction.getAttackType();
		
		// Remove one of the active attacks from the appropriate map for mapping player attack type to number of active attacks
		switch (action.getContributorEntity()) {
		
		case PLAYER1_ENTITY: {
			if (attackType.getIsActivationGroupLimited()) {
				this.numP1GroupLimitedActiveAttacks--;
			}
			
			Integer value = this.p1AttackTypesCurrentlyActive.get(attackType);
			this.p1AttackTypesCurrentlyActive.put(attackType, Math.max(0, value-1));
			break;
		}
		
		case PLAYER2_ENTITY: {
			if (attackType.getIsActivationGroupLimited()) {
				this.numP2GroupLimitedActiveAttacks--;
			}
			
			Integer value = this.p2AttackTypesCurrentlyActive.get(attackType);
			this.p2AttackTypesCurrentlyActive.put(attackType, Math.max(0, value-1));
			break;
		}
		
		default:
			assert(false);
			break;
		}
		
	}
	
	@Override
	void initiateNextState(GameState.GameStateType nextState) {
		// This is ignored while the players are fighting - you can't start the next round
		// until the current one is finished!
	}
	
	@Override
	void killToIdle() {
		// Turn off invincibility for both players
		Player p1 = this.gameModel.getPlayer1();
		Player p2 = this.gameModel.getPlayer2();
		assert(p1 != null && p2 != null);
		
		p1.setInvincible(false);
		p2.setInvincible(false);
		
		// Place the game into the idle state within the next tick
		this.clearAndResetAllEmitters();
		this.gameModel.setNextGameState(new IdleGameState(this.gameModel));
	}

	@Override
	void executeAction(Action action) {
		
		switch (action.getContributorEntity()) {
		
		case PLAYER1_ENTITY: {
			if (!this.checkPlayerAction(action, this.gameModel.getPlayer1(),
				this.secsSinceLastP1LeftAction, this.secsSinceLastP1RightAction,
				this.p1AttacksExecuted, this.p1AttackTypesCurrentlyActive,
				this.numP1GroupLimitedActiveAttacks)) {
				
				this.gameModel.getActionSignaller().fireOnUnrecognizedGestureEvent(IGameModel.Entity.PLAYER1_ENTITY);
				return;
			}

			break;
		}
			
		case PLAYER2_ENTITY: {
			if (!this.checkPlayerAction(action, this.gameModel.getPlayer2(),
				this.secsSinceLastP2LeftAction, this.secsSinceLastP2RightAction, 
				this.p2AttacksExecuted, this.p2AttackTypesCurrentlyActive,
				this.numP2GroupLimitedActiveAttacks)) {
				
				this.gameModel.getActionSignaller().fireOnUnrecognizedGestureEvent(IGameModel.Entity.PLAYER2_ENTITY);
				return;
			}

			break;
		}
			
		// We only interpret player actions when the game is in play
		case RINGMASTER_ENTITY:
		default:
			return;
		}
		
		Action.mergeAction(this.activeActions, action);
	}

	@Override
	void togglePause() {
		this.gameModel.setNextGameState(new PausedGameState(this.gameModel, this));
	}

	/**
	 * Helper function to clear all active actions in this state and reset all fire emitters.
	 */
	protected void clearAndResetAllEmitters() {
		this.activeActions.clear();
		this.gameModel.getFireEmitterModel().resetAllEmitters();
	}
	
	/**
	 * Private helper method for limiting player actions and determining whether an action will actually become active or not.
	 * @param action The action to check.
	 * @param player The player executing the action.
	 * @param secsSinceLastLeftAction The time since the last left-handed action for the given player.
	 * @param secsSinceLastRightAction The time since the last right-handed action for the given player.
	 * @param attacksExecuted The number of attacks of each type currently being executed for the given player.
	 * @param attacksCurrentlyActive The number of attacks of each type that are currently active for the given player.
	 * @param numGroupLimitedActiveAttacks The number of attacks that are currently active which are 'group limited'.
	 * @return false if the action doesn't pass the check, true if it does.
	 */
	private boolean checkPlayerAction(Action action, Player player, double secsSinceLastLeftAction, double secsSinceLastRightAction,
									  Map<AttackType, Integer> attacksExecuted, Map<AttackType, Integer> attacksCurrentlyActive,
									  int numGroupLimitedActiveAttacks) {
		

		// We never ignore/filter blocks, blocks should ALWAYS work, immediately!
		if (action.getActionFlameType() == FireEmitter.FlameType.BLOCK_FLAME) {
			return true;
		}
		
		if (action.getActionFlameType() == FireEmitter.FlameType.ATTACK_FLAME) {
			
			PlayerAttackAction attackAction = ((PlayerAttackAction)action);
			AttackType attackType = attackAction.getAttackType();
			

			int playerNum = player.getPlayerNumber();
			FireEmitterModel fireEmitterModel = this.gameModel.getFireEmitterModel();
			assert(fireEmitterModel != null);
			
			boolean isLeftHandedAttack  = attackAction.hasLeftHandedAttack();
			boolean isRightHandedAttack = attackAction.hasRightHandedAttack();
			
			// If there is already an attack present on the first relevant fire emitter(s) for the player
			// then don't allow it, also check to see whether the timer between attacks allows the player to make another attack for
			// its handedness as well...

			if (isLeftHandedAttack) {
				if (playerNum == 1) {
					if (this.secsSinceLastP1LeftAction < this.gameModel.getConfig().getMinTimeBetweenPlayerActionsInSecs()) {
						return false;
					}
				}
				else {
					if (this.secsSinceLastP2LeftAction < this.gameModel.getConfig().getMinTimeBetweenPlayerActionsInSecs()) {
						return false;
					}
				}
				
				FireEmitter firstLeftEmitter  = fireEmitterModel.getPlayerLeftEmitters(playerNum).get(0);
				
				if (firstLeftEmitter.getContributingEntityFlameTypes(player.getEntity()).contains(FlameType.ATTACK_FLAME)) {
					return false;
				}
			}
			
			if (isRightHandedAttack) {
				if (playerNum == 1) {
					if (this.secsSinceLastP1RightAction < this.gameModel.getConfig().getMinTimeBetweenPlayerActionsInSecs()) {
						return false;
					}
				}
				else {
					if (this.secsSinceLastP2RightAction < this.gameModel.getConfig().getMinTimeBetweenPlayerActionsInSecs()) {
						return false;
					}
				}
				
				FireEmitter firstRightEmitter = fireEmitterModel.getPlayerRightEmitters(playerNum).get(0);
				
				if (firstRightEmitter.getContributingEntityFlameTypes(player.getEntity()).contains(FlameType.ATTACK_FLAME)) {
					return false;
				}
			}
			
			int numAttacks = attacksExecuted.get(attackType);
			int numAttacksCurrentlyActive = attacksCurrentlyActive.get(attackType);
			
			if (this.applyActionLimits) {
			
				// Determine whether the attack can even be executed based on how many of the same type are
				// already currently active - if there are already too many of the same attack active then we do not allow it
				if (numAttacksCurrentlyActive >= attackType.getNumAllowedActiveAtATime()) {
					return false;
				}
				
				// If the player is not allowed to have unlimited attacks and if the attack is not allowed to have
				// more than x use(s) per round then we do not allow it
				
				if (!player.getHasInfiniteMoves()) {
					if (attackType.getMaxUsesPerRound() <= numAttacks) {
						return false;
					}
				}
				
				// Determine whether the attack is group limited, if it is check to see the number of group limited attacks
				// that are currently active, if the number exceeds or is equal to the limit number then we do not allow it
				if (attackType.getIsActivationGroupLimited()) {
					if (numGroupLimitedActiveAttacks > attackType.getNumActivationsInGroupAtATime()) {
						return false;
					}
					
					if (playerNum == 1) {
						this.numP1GroupLimitedActiveAttacks++;
					}
					else {
						this.numP2GroupLimitedActiveAttacks++;
					}
				}
			}
			
			// Reset the time on each of the player's handed attacks since we're about to let this attack go through
			if (playerNum == 1) {
				if (isLeftHandedAttack) {
					this.secsSinceLastP1LeftAction = 0.0;
				}
				if (isRightHandedAttack) {
					this.secsSinceLastP1RightAction = 0.0;
				}
			}
			else {
				if (isLeftHandedAttack) {
					this.secsSinceLastP2LeftAction = 0.0;
				}
				if (isRightHandedAttack) {
					this.secsSinceLastP2RightAction = 0.0;
				}
			}
			
			// Update the attacks that are active and that have been executed to include the current attack action
			attacksExecuted.put(attackType, numAttacks + 1);
			attacksCurrentlyActive.put(attackType, numAttacksCurrentlyActive + 1);
		}
	
		return true;
	}
}
