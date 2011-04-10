'''
attack.py

@author: Callum Hay
'''

from ssf_game import SSFGame
from fire_emitter import FireEmitter
import player

class Attack:
    # Enumeration constants for the fire emitter arcs that an Attack
    # may apply to
    LEFT_SIDE            = 0
    RIGHT_SIDE           = 1
    LEFT_AND_RIGHT_SIDES = 2
    
    # Enumeration constants for active/inactive attack parts
    ACTIVE_ATTACK_PART   = True
    INACTIVE_ATTACK_PART = False
    
    # Constructor for the Attack class
    # playerNum:  The number of the player (1 or 2) that initiated the attack.
    # sideEnum:   Must be one of the side enumerations (to define 
    #             which fire arc the attack takes place on).
    # thickness:  The number of fire emitters thick that the attack is.
    # timeLength: The total time that the attack will take to travel
    #             from one side to the other of the arc.
    def __init__(self, playerNum, sideEnum, thickness, timeLength):
        assert(timeLength > 0.0)
        assert(thickness >= 1)
        
        self.playerNum  = playerNum

        self._sideEnum       = sideEnum
        self._thickness      = thickness
        self._timeLength     = timeLength
        self._timePerEmitter = timeLength / float(SSFGame.NUM_FIRE_EMITTERS_PER_ARC)
        
        # Track the amount of time that the attack has been active for
        self._currAttackTime       = 0.0
        
        # Track the amount of time that the current emitter has been on for so that
        # We know when to shift the attack window
        self._currDeltaLEmitterTime = 0.0
        self._currDeltaREmitterTime = 0.0
        
        # Track the number of hits on either side
        self._currNumLHits = 0
        self._currNumRHits = 0 
        
        # An attack is like a shift register - basically we keep a array that acts as the 
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
        # Attack Window:      |a|a|a| ---->
        # Indices:            -2-1 0 1 2 3 4 5 6 7 
        
        # The window index is always moving from lower to higher numbers
        self._attackLWindowIdx  = -thickness + 1
        self._attackRWindowIdx  = self._attackLWindowIdx
        self._leftAttackWindow  = None
        self._rightAttackWindow = None
    
    def Initialize(self, ssfGame):
        assert(self._lastLeftEmitters == None)
        assert(self._lastRightEmitters == None)
        
        self._currAttackTime       = 0.0
        self._currDeltaEmitterTime = 0.0
        self._attackLWindowIdx     = -self._thickness + 1
        self._attackRWindowIdx     = self._attackLWindowIdx
        
        self._currNumLHits = 0
        self._currNumRHits = 0
                
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
        return self._currAttackTime >= self.timeLength
    
    def Execute(self, ssfGame, dT):
        # Don't execute anything if the attack is finished
        if self.IsFinished():
            return
        
        # Increment the time tracker(s)...
        self._currAttackTime        += dT
        
        # NOTE: For now the delta emitter times for the left and right arcs are the same
        self._currDeltaLEmitterTime += dT
        self._currDeltaREmitterTime  = self._currDeltaLEmitterTime
        
        if self._sideEnum == Attack.LEFT_SIDE:
            self._LeftAttack(ssfGame, dT)
        elif self._sideEnum == Attack.RIGHT_SIDE:
            self._RightAttack(ssfGame, dT)
        elif self._sideEnum == Attack.LEFT_AND_RIGHT_SIDES:
            self._LeftAttack(ssfGame, dT)
            self._RightAttack(ssfGame, dT)
        else:
            assert(False)
            
    def _LeftAttack(self, ssfGame, dT):
        assert(self._leftAttackWindow != None)
        assert(not self.IsFinished())
        
        windowLastIdx  = self._attackLWindowIdx + self._thickness
        emitterArcSize = len(ssfGame.leftEmitters)
        
        # Shift the left attack window if we've exceeded the amount of time per emitter... 
        if self._currDeltaLEmitterTime >= self._timePerEmitter:
            # We'll first need to turn off the emitter that we're about to pass
            passedEmitter = self._GetEmitter(ssfGame.leftEmitters, self._attackLWindowIdx)
            # We only need to turn it off if the attack is still active on that part
            # and the emitter is actually inside the arc (it may be the case that the attack
            # is still starting up and has a thickness > 1)
            if passedEmitter != None and self._leftAttackWindow[0] == Attack.ACTIVE_ATTACK_PART:
                passedEmitter.FireOff(self.playerNum, FireEmitter.ATTACK_FLAME)
            
            # Shift the attack window
            self._attackLWindowIdx += 1
            
            # Check to see if any new hits may have occurred...
            totalNumFinishedAttackParts  = windowLastIdx - emitterArcSize
            if totalNumFinishedAttackParts >= 0:
                # Go through all of the hits so far if we encounter new hits,
                # each new hit will affect the game state...
                for i in range(emitterArcSize, windowLastIdx):
                    if self._leftAttackWindow[i] == Attack.ACTIVE_ATTACK_PART:
                        # A new attack was just landed
                        ssfGame.Hurt(player.GetOtherPlayerNum(self.playerNum), 5)
                        self._leftAttackWindow[i] = Attack.INACTIVE_ATTACK_PART
            
            
            # If the attack window just shifted right off the end the of the emitter arc
            # then the attack is finished and we get out immediately
            if totalNumFinishedAttackParts == self._thickness:
                self._currAttackTime = self._timeLength
                return

            self._currDeltaLEmitterTime = self._currDeltaLEmitterTime - self._timePerEmitter
        
        # Go through the current attack window and update it based on the fire
        # emitters that are being touched by the attack
        firstIdx = max(self._attackLWindowIdx, 0)
        lastIdx  = min(emitterArcSize, windowLastIdx)
        for i in range(firstIdx, lastIdx):
            if self._leftAttackWindow[i] == Attack.ACTIVE_ATTACK_PART:
                currEmitter = self._GetEmitter(ssfGame.leftEmitters, i)
                wasBlocked = currEmitter.FireOn(self.playerNum, FireEmitter.ATTACK_FLAME)
                self._leftAttackWindow[i] = wasBlocked

    
    def _RightAttack(self, ssfGame, dT):
        assert(self._RightAttackWindow != None)

        # Shift the right attack window if we've exceeded the amount of time per emitter... 
        if self._currDeltaREmitterTime >= self._timePerEmitter:
            
            self._currDeltaREmitterTime = self._currDeltaREmitterTime - self._timePerEmitter

    

    def _GetEmitter(self, arcEmitters, idx):
        if idx < 0 or idx >= SSFGame.NUM_FIRE_EMITTERS_PER_ARC:
            return None
        
        if self.playerNum == 1:
            return arcEmitters[idx]
        else:
            return arcEmitters[-idx-1]

'''
    def _GetInitEmitters(self, arcEmitterList):
        assert(not self.IsFinished())
        
        if self.playerNum == 1:
            # The first player has emitter indices that go from the start to the
            # end of an arc emitter list
            return arcEmitterList[0:self._thickness]
        else:
            # The second player has emitter indices that go from the end to the
            # start of an arc emitter list
            return arcEmitterList[-1:-self._thickness:-1]
    
    def _GetCurrentEmitters(self, prevArcEmitterList):
        startEmitterNum = int(self._currAttackTime / self._timePerEmitter)
        if self.playerNum == 1:
            # The first player has emitter indices that go from the start to the
            # end of an arc emitter list
            return prevArcEmitterList[startEmitterNum:self._thickness]
        else:
            # The second player has emitter indices that go from the end to the
            # start of an arc emitter list
            startEmitterNum -= 1
            return prevArcEmitterList[-startEmitterNum:-self._thickness:-1]  
'''

    
# Factory/Builder Methods for various Super Street Fire Attacks 
def BuildLeftJabAttack(playerNum):
    return Attack(playerNum, Attack.LEFT_SIDE, 1, 2.0)
def BuildRightJabAttack(playerNum):
    return Attack(playerNum, Attack.RIGHT_SIDE, 1, 2.0)
def BuildLeftHookAttack(playerNum):
    return Attack(playerNum, Attack.LEFT_SIDE, 2, 3.0)
def BuildRightHookAttack(playerNum):    
    return Attack(playerNum, Attack.RIGHT_SIDE, 2, 3.0)
def BuildHadoukenAttack(playerNum):
    return Attack(playerNum, Attack.LEFT_AND_RIGHT_SIDES, 2, 4.0)
