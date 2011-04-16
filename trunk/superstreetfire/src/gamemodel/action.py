'''
action.py

The superclass for Attack and Block actions executed by the two players
in the Super Street Fire Game.

@author: Callum Hay
'''

from fire_emitter import FireEmitter 

class Action:
    # Enumeration constants for the fire emitter arcs that an Action may apply to.
    LEFT_SIDE            = 0
    RIGHT_SIDE           = 1
    LEFT_AND_RIGHT_SIDES = 2
    
    def __init__(self, playerNum, sideEnum, thickness, timeLength):
        assert(playerNum == 1 or playerNum == 2)
        assert(sideEnum == Action.LEFT_SIDE or sideEnum == Action.RIGHT_SIDE or \
               sideEnum == Action.LEFT_AND_RIGHT_SIDES)
        assert(timeLength > 0.0)
        assert(thickness >= 1 and thickness <= FireEmitter.NUM_FIRE_EMITTERS_PER_ARC)
        
        self.playerNum   = playerNum
        self._sideEnum   = sideEnum
        self._thickness  = thickness
        self._timeLength = timeLength
        self._isKilled   = False
        
    # Functions for getting the emitter from the given arc emitter list and the given
    # zero-based index, depending on which player this action is executed by.
    # If the index is out of range, None is returned.
    # Explanation: The emitter arc lists are stored in their lists from the POV
    # of player 1 and are therefore completely reversed for player 2.      
    def _GetEmitter(self, arcEmitters, idx):
        if idx < 0 or idx >= FireEmitter.NUM_FIRE_EMITTERS_PER_ARC:
            return None
        
        if self.playerNum == 1:
            return arcEmitters[idx]
        else:
            return arcEmitters[-idx-1]
                
            
    # Abstract methods for overriding in the Attack and Block classes
    def Initialize(self, ssfGame): assert(False)
    def IsFinished(self):          assert(False)
    def Tick(self, ssfGame, dT):   assert(False)
    def Kill(self, ssfGame):
        self._isKilled = True