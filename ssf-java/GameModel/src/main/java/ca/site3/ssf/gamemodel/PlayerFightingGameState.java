package ca.site3.ssf.gamemodel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Map;

import ca.site3.ssf.gamemodel.PlayerAttackAction.AttackType;


abstract class PlayerFightingGameState extends GameState {

	protected Collection<Action> activeActions = new ArrayList<Action>();
	
	protected double secsSinceLastP1Action = Double.MAX_VALUE;
	protected double secsSinceLastP2Action = Double.MAX_VALUE;
	
	protected Map<AttackType, Integer> p1AttacksExecuted = new Hashtable<AttackType, Integer>(AttackType.values().length);
	protected Map<AttackType, Integer> p2AttacksExecuted = new Hashtable<AttackType, Integer>(AttackType.values().length);
	
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
		
		for (AttackType atkType : AttackType.values()) {
			this.p1AttacksExecuted.put(atkType, new Integer(0));
			this.p2AttacksExecuted.put(atkType, new Integer(0));
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
			if (this.secsSinceLastP1Action < this.gameModel.getConfig().getMinTimeBetweenPlayerActionsInSecs() &&
				action.getActionFlameType() != FireEmitter.FlameType.BLOCK_FLAME) {
				
				// Player 1 has already made an action recently, exit without counting the current action
				return;
			}
			
			Player p1 = this.gameModel.getPlayer1();
			if (applyActionLimits && !p1.getHasInfiniteMoves()) {
				if (action.getActionFlameType() == FireEmitter.FlameType.ATTACK_FLAME) {
					AttackType atkType = ((PlayerAttackAction)action).getAttackType();
					
					Integer numAttacks = this.p1AttacksExecuted.get(atkType);
					assert(numAttacks != null);
					
					// If the attack is not allowed to have more than x use(s) per round then we have to exit immediately...
					if (atkType.getMaxUsesPerRound() <= numAttacks) {
						return;
					}
					
					this.p1AttacksExecuted.put(atkType, numAttacks + 1);
				}
			}
			
			this.secsSinceLastP1Action = 0.0;
			break;
		}
			
		case PLAYER2_ENTITY: {
			if (this.secsSinceLastP2Action < this.gameModel.getConfig().getMinTimeBetweenPlayerActionsInSecs() &&
				action.getActionFlameType() != FireEmitter.FlameType.BLOCK_FLAME) {
				
				// Player 2 has already made an action recently, exit without counting the current action
				return;
			}
			
			Player p2 = this.gameModel.getPlayer2();
			if (applyActionLimits && !p2.getHasInfiniteMoves()) {
				if (action.getActionFlameType() == FireEmitter.FlameType.ATTACK_FLAME) {
					AttackType atkType = ((PlayerAttackAction)action).getAttackType();
					
					Integer numAttacks = this.p2AttacksExecuted.get(atkType);
					assert(numAttacks != null);
					
					// If the attack is not allowed to have more than x use(s) per round then we have to exit immediately...
					if (atkType.getMaxUsesPerRound() <= numAttacks) {
						return;
					}
					
					this.p2AttacksExecuted.put(atkType, numAttacks + 1);
				}
			}
			
			this.secsSinceLastP2Action = 0.0;
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
	
}
