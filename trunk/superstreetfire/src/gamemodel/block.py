'''
block.py

@author: Callum Hay
'''

from action import Action

class Block(Action):

    def __init__(self, playerNum, sideEnum, timeLimit):
        Action.__init__(self, playerNum, sideEnum, timeLimit)
        
        # Track the amount of time that the block has been active for
        self._currBlockTime = 0.0
        self._killMe        = False

    def Initialize(self, ssfGame):
        pass
    
    def IsFinished(self):
        return self._currBlockTime >= self._timeLength or self._killMe

    def Tick(self, ssfGame, dT):
        if self.IsFinished():
            return
        
        self._currBlockTime += dT
        # TODO
        
    
    # Used to kill this block -
    # it will then be cleaned up during the next simulation tick
    # The state machine will call this on the block when the player
    # actively stops blocking - otherwise this block will end only
    # once it reaches its time limit
    def Kill(self):
        self._killMe = True

# Factory/Builder Methods for various Super Street Fire Blocks 
def BuildLeftBasicBlock(playerNum):
    return Block(playerNum, Action.LEFT_SIDE, 4.0)
def BuildRightBasicBlock(playerNum):
    return Block(playerNum, Action.RIGHT_SIDE, 4.0)

