'''
attack.py

@author: Callum Hay
'''

#import logging

from action import Action
from ssf_game import SSFGame
from fire_emitter import FireEmitter
import player

class Attack(Action):
    # Enumeration constants for the fire emitter arcs that an Attack may apply to.
    LEFT_SIDE            = 0
    RIGHT_SIDE           = 1
    LEFT_AND_RIGHT_SIDES = 2
    
    # Enumeration constants for active/inactive attack parts - an attack part
    # is a single emitter at any given time that is part of an attack - thus
    # if an attack had a thickness of 2 there would be 2 attack parts.
    # Attack parts may be active or inactive - they become inactive by being
    # blocked by the other player or by hitting the other player.
    ACTIVE_ATTACK_PART   = True
    INACTIVE_ATTACK_PART = False
    
    # Constructor for the Attack class
    # playerNum:  The number of the player (1 or 2) that initiated the attack.
    # sideEnum:   Must be one of the side enumerations (to define 
    #             which fire arc the attack takes place on). This is from the same
    #             perspective as seen by playerNum.
    # thickness:  The number of fire emitters thick that the attack is.
    # timeLength: The total time that the attack will take to travel
    #             from one side to the other of the arc.
    # dmgPerFlame: The total damage per flame that hits the other player, in percent 
    def __init__(self, playerNum, sideEnum, thickness, timeLength, dmgPerFlame):
        assert(timeLength > 0.0)
        assert(thickness >= 1)
        
        Action.__init__(self, playerNum)
        
        self._sideEnum       = sideEnum
        self._thickness      = thickness
        self._timeLength     = timeLength        
        self._dmgPerFlame    = dmgPerFlame
        
        # The time per emitter is calculated using the total number of fire emitters AND
        # the thickness of the attack - the thicker the attack the more emitters will actually
        # go off...
        self._timePerEmitter = timeLength / float(FireEmitter.NUM_FIRE_EMITTERS_PER_ARC + thickness - 1)
        
        # Track the amount of time that the attack has been active for
        self._currAttackTime       = 0.0
        
        # Track the amount of time that the current emitter has been on for so that
        # We know when to shift the attack window 
        # We set these to be equal to the time per emitter to start - this will ensure
        # that the window shifts on the first execution of the attack
        self._currDeltaLEmitterTime = self._timePerEmitter
        self._currDeltaREmitterTime = self._timePerEmitter
        
        # We keep a array that acts as the 
        # attack 'window', this window shifts across the relevant flame emitter arc
        # as it does so it turns the emitters on (if that part of the window is still alive)
        # Parts of the window can be diminished when they encounter blocks (or some other feature
        # that diminishes an attack)
        # You can imagine like so: The set of x's are the fire emitters along one of the arcs
        # that the attack will take place on, the set of a's are the attack parts (based on the
        # thickness of the attack), in the example below, the attack is of thickness = 3.
        # At _currAttackTime = 0.0, the first attack will be executed on the first fire emitter
        # in the arc. As time continues to tick the window will shift across the entire arc.
        # The values of each a will be Attack.ACTIVE_ATTACK_PART until the attack part is 
        # diminished and will then become Attack.INACTIVE_ATTACK_PART.
        #
        # Fire Emitter Arc:       |x|x|x|x|x|x|x|x|
        # Attack Window:    |a|a|a| ---->
        # Indices:          -3-2-1 0 1 2 3 4 5 6 7 
        
        # The window index is always moving from lower to higher numbers
        self._attackLWindowIdx  = -thickness
        self._attackRWindowIdx  = -thickness
        self._leftAttackWindow  = None
        self._rightAttackWindow = None
    
    def Initialize(self, ssfGame):
        assert(self._leftAttackWindow  == None)
        assert(self._rightAttackWindow == None)
        
        self._currAttackTime        = 0.0
        self._currDeltaLEmitterTime = self._timePerEmitter
        self._currDeltaREmitterTime = self._timePerEmitter
        self._attackLWindowIdx      = -self._thickness
        self._attackRWindowIdx      = -self._thickness
        
        print "Time per emitter: ", self._timePerEmitter
        
        if self._sideEnum == Attack.LEFT_SIDE:
            self._leftAttackWindow  = [Attack.ACTIVE_ATTACK_PART] * self._thickness
        elif self._sideEnum == Attack.RIGHT_SIDE:
            self._rightAttackWindow = [Attack.ACTIVE_ATTACK_PART] * self._thickness
        elif self._sideEnum == Attack.LEFT_AND_RIGHT_SIDES:
            self._leftAttackWindow  = [Attack.ACTIVE_ATTACK_PART] * self._thickness
            self._rightAttackWindow = [Attack.ACTIVE_ATTACK_PART] * self._thickness
        else:
            assert(False)  
    
    def IsFinished(self):
        return self._currAttackTime >= self._timeLength
    
    def Tick(self, ssfGame, dT):
        # Don't execute anything if the attack is finished
        if self.IsFinished():
            return
        
        # Increment the time tracker(s)...
        self._currAttackTime        += dT
        
        # NOTE: For now the delta emitter times for the left and right arcs are the same
        self._currDeltaLEmitterTime += dT
        self._currDeltaREmitterTime += dT
        
        if self._sideEnum == Attack.LEFT_SIDE:
            assert(self._leftAttackWindow  != None)
            self._TickLeftAttack(ssfGame)
            
        elif self._sideEnum == Attack.RIGHT_SIDE:
            assert(self._rightAttackWindow != None)
            self._TickRightAttack(ssfGame)
            
        elif self._sideEnum == Attack.LEFT_AND_RIGHT_SIDES:
            assert(self._leftAttackWindow  != None)
            self._TickLeftAttack(ssfGame)
            assert(self._rightAttackWindow != None)
            self._TickRightAttack(ssfGame)
        else:
            assert(False)
            
    def _TickLeftAttack(self, ssfGame):
        # We need to make sure we are modifying the correct emitter - the emitters are, by
        # default, layed out from player 1's perspective so we need to reverse them for player 2
        emitterArc = None
        if self.playerNum == 1:
            emitterArc = ssfGame.leftEmitters
        else:
            emitterArc = ssfGame.rightEmitters
        
        # Simulate the attack
        (self._attackLWindowIdx, self._currDeltaLEmitterTime) = \
        self._SimulateAttack(ssfGame, self._leftAttackWindow, self._attackLWindowIdx, \
                             emitterArc, self._currDeltaLEmitterTime)

    def _TickRightAttack(self, ssfGame):
        # We need to make sure we are modifying the correct emitter - the emitters are, by
        # default, layed out from player 1's perspective so we need to reverse them for player 2
        emitterArc = None
        if self.playerNum == 1:
            emitterArc = ssfGame.rightEmitters
        else:
            emitterArc = ssfGame.leftEmitters
        
        # Simulate the attack
        (self._attackRWindowIdx, self._currDeltaREmitterTime) = \
        self._SimulateAttack(ssfGame, self._rightAttackWindow, self._attackRWindowIdx, \
                             emitterArc, self._currDeltaREmitterTime)
        
    # Generalized Attack simulation function - used to update the state of the attack whenever
    # it is being executed/ticked.
    # Returns: A tuple of the (updated attack window index, current delta emitter time)
    def _SimulateAttack(self, ssfGame, attackWindow, attackWindowIdx, arcEmitters, deltaEmitterTime):
        assert(attackWindow != None)
        assert(arcEmitters  != None)
        
        windowLastIdx  = attackWindowIdx + self._thickness
        emitterArcSize = len(arcEmitters)
        assert(emitterArcSize == FireEmitter.NUM_FIRE_EMITTERS_PER_ARC)

        # TRICKY STUFF: WE NEED TO CHECK THE ATTACK WINDOW TO SEE IF ANY HAVE SINCE BEEN DIMINISHED
        # BY A BLOCK THAT MAY HAVE OCCURRED IN THE MEANTIME
        for i, j in zip(range(self._thickness), range(attackWindowIdx, windowLastIdx)):
            # i is the index in the attack window
            # j is the index of the emitter
            if attackWindow[i] == Attack.ACTIVE_ATTACK_PART:
                currEmitter = self._GetEmitter(arcEmitters, j)
                # If the emitter in the arc no longer holds an attack flame for self.playerNum
                # then it must have been extinguished by a block from the other player...
                if currEmitter != None and not currEmitter.HasAttackFlameOwnedByPlayer(self.playerNum):
                    print "CANCELLED BY BLOCK"
                    attackWindow[i] = Attack.INACTIVE_ATTACK_PART
        
        # Shift the attack window if we've exceeded the emitter time
        if deltaEmitterTime >= self._timePerEmitter:
            #print attackWindowIdx, attackWindow[0]
            #print self._currAttackTime
            
            # We'll first need to turn off the emitter that we're about to pass
            passedEmitter = self._GetEmitter(arcEmitters, attackWindowIdx)
            # We only need to turn it off if the attack is still active on that part
            # and the emitter is actually inside the arc (it may be the case that the attack
            # is still starting up and has a thickness > 1)
            if passedEmitter != None and attackWindow[0] == Attack.ACTIVE_ATTACK_PART:
                passedEmitter.FireOff(self.playerNum, FireEmitter.ATTACK_FLAME)
            
            # Shift the attack window
            attackWindowIdx += 1
            windowLastIdx   += 1
            
            # Check to see if any new hits may have occurred...
            totalNumFinishedAttackParts  = windowLastIdx - emitterArcSize
            if totalNumFinishedAttackParts >= 0:
                # Go through all of the hits so far if we encounter new hits,
                # each new hit will affect the game state...
                assert(self._thickness >= totalNumFinishedAttackParts)
                for i in range(self._thickness-1, self._thickness - totalNumFinishedAttackParts - 1, -1):
                    if attackWindow[i] == Attack.ACTIVE_ATTACK_PART:
                        # A new attack was just landed
                        ssfGame.Hurt(player.GetOtherPlayerNum(self.playerNum), self._dmgPerFlame)
                        attackWindow[i] = Attack.INACTIVE_ATTACK_PART
            
            # If the attack window just shifted completely off the end the of the emitter arc
            # then the attack is finished and we get out immediately
            if totalNumFinishedAttackParts == self._thickness:
                assert(self._currAttackTime >= self._timeLength)
                return (attackWindowIdx, deltaEmitterTime)

            deltaEmitterTime = deltaEmitterTime - self._timePerEmitter
        
        # Go through the current attack window and update it based on the fire
        # emitters that are being touched by the attack:
        for i, j in zip(range(self._thickness), range(attackWindowIdx, windowLastIdx)):
            # i is the index in the attack window
            # j is the index of the emitter
            if attackWindow[i] == Attack.ACTIVE_ATTACK_PART:
                currEmitter = self._GetEmitter(arcEmitters, j)
                if currEmitter != None:
                    
                    isOnFire = currEmitter.FireOn(self.playerNum, FireEmitter.ATTACK_FLAME)
                    attackWindow[i] = isOnFire
                    
        return (attackWindowIdx, deltaEmitterTime)
    
    def _GetEmitter(self, arcEmitters, idx):
        if idx < 0 or idx >= FireEmitter.NUM_FIRE_EMITTERS_PER_ARC:
            return None
        
        if self.playerNum == 1:
            return arcEmitters[idx]
        else:
            return arcEmitters[-idx-1]

# Factory/Builder Methods for various Super Street Fire Attacks 
def BuildLeftJabAttack(playerNum):
    return Attack(playerNum, Attack.LEFT_SIDE, 1, 2.0, 5)
def BuildRightJabAttack(playerNum):
    return Attack(playerNum, Attack.RIGHT_SIDE, 1, 2.0, 5)
def BuildLeftHookAttack(playerNum):
    return Attack(playerNum, Attack.LEFT_SIDE, 2, 3.0, 5)
def BuildRightHookAttack(playerNum):
    return Attack(playerNum, Attack.RIGHT_SIDE, 2, 3.0, 5)
def BuildHadoukenAttack(playerNum):
    return Attack(playerNum, Attack.LEFT_AND_RIGHT_SIDES, 2, 4.0, 5)