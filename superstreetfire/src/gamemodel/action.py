'''
action.py

The superclass for Attack and Block actions executed by the two players
in the Super Street Fire Game.

@author: Callum Hay
'''

class Action:
    # Enumeration constants for the fire emitter arcs that an Action may apply to.
    LEFT_SIDE            = 0
    RIGHT_SIDE           = 1
    LEFT_AND_RIGHT_SIDES = 2
    
    def __init__(self, playerNum, sideEnum):
        assert(playerNum == 1 or playerNum == 2)
        assert(sideEnum == Action.LEFT_SIDE or sideEnum == Action.RIGHT_SIDE or \
               sideEnum == Action.LEFT_AND_RIGHT_SIDES)
        self.playerNum = playerNum
        self._sideEnum = sideEnum
        
    # Abstract methods for overriding in the Attack and Block classes
    def Initialize(self, ssfGame): assert(False)
    def Tick(self, ssfGame, dT):   assert(False)