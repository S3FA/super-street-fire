'''
block.py

@author: Callum Hay
'''

from action import Action

class Block(Action):

    def __init__(self, playerNum, sideEnum, thickness, timeLimit):
        Action.__init__(self, playerNum, sideEnum, thickness, timeLimit)
        
        # Track the amount of time that the block has been active for
        self._currBlockTime = 0.0
        self._killMe        = False
        
        # These will be initialized in the initialize method, and will be set to
        # the emitters that the block will reside on, on the emitter arcs of the game
        self._blockLEmitters = None
        self._blockREmitters = None

    def Initialize(self, ssfGame):
        # Setup the emitter list(s) for the block - blocks always stay on the
        # same emitters that they are initialized with until they are killed/expire
        if self._sideEnum == Action.LEFT_SIDE:
            pass
        elif self._sideEnum == Action.RIGHT_SIDE:
            pass
        else:
            assert(self._sideEnum == Action.LEFT_AND_RIGHT_SIDES)
            
        
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

