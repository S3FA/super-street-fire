'''
action.py

The superclass for Attack and Block actions executed by the two players
in the Super Street Fire Game.

@author: Callum Hay
'''

class Action:

    def __init__(self, playerNum):
        assert(playerNum == 1 or playerNum == 2)
        self.playerNum = playerNum
    
    # Abstract methods for overriding in the Attack and Block classes
    def Initialize(self, ssfGame): assert(False)
    def Tick(self, ssfGame, dT):   assert(False)