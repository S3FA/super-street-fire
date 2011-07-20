'''
block.py

@author: Callum Hay
'''

from action import Action
from fire_emitter import FireEmitter

BLOCK_LENGTH = 1.0

class Block(Action):

    def __init__(self, playerNum, sideEnum, thickness, timeLimit):
        Action.__init__(self, playerNum, sideEnum, thickness, timeLimit)
        
        # Track the amount of time that the block has been active for
        self._currBlockTime = 0.0

        # These will be initialized in the initialize method, and will be set to
        # the emitters that the block will reside on, on the emitter arcs of the game
        self._blockLEmitters = None
        self._blockREmitters = None

    def Initialize(self, ssfGame):
        self._currBlockTime = 0.0
        
        # The list of emitters in each arc in the game model is reversed for player 2
        # (since they're stored from the perspective of player 1), we need to account
        # for this when specifying which emitters will be activated by this block
        rangeList = None
        if self.playerNum == 1:
            rangeList = range(self._thickness)
        else:
            rangeList = range(FireEmitter.NUM_FIRE_EMITTERS_PER_ARC - 1, \
                              FireEmitter.NUM_FIRE_EMITTERS_PER_ARC - self._thickness - 1, -1)
            
        # Setup the emitter list(s) for the block - blocks always stay on the
        # same emitters that they are initialized with until they are killed/expire
        if self._sideEnum == Action.LEFT_SIDE:
            leftEmitters = ssfGame.GetLeftEmitterArc(self.playerNum)
            self._blockLEmitters = [leftEmitters[i] for i in rangeList]
              
        elif self._sideEnum == Action.RIGHT_SIDE:
            rightEmitters = ssfGame.GetRightEmitterArc(self.playerNum)
            self._blockREmitters = [rightEmitters[i] for i in rangeList]  
        else:
            assert(self._sideEnum == Action.LEFT_AND_RIGHT_SIDES)
            leftEmitters  = ssfGame.GetLeftEmitterArc(self.playerNum)
            rightEmitters = ssfGame.GetRightEmitterArc(self.playerNum)
            self._blockLEmitters = [leftEmitters[i] for i in rangeList]
            self._blockREmitters = [rightEmitters[i] for i in rangeList]    
    
    def IsFinished(self):
        return self._currBlockTime >= self._timeLength or self._isKilled

    def Tick(self, ssfGame, dT):
        if self._isKilled or self.IsFinished():
            return
        
        # Turn the block emitters on the first time this function is called...
        if self._currBlockTime == 0.0:
            if self._blockLEmitters != None:
                for emitter in self._blockLEmitters:
                    emitter.FireOn(self.playerNum, FireEmitter.BLOCK_FLAME)
            if self._blockREmitters != None:
                for emitter in self._blockREmitters:
                    emitter.FireOn(self.playerNum, FireEmitter.BLOCK_FLAME)
        
        self._currBlockTime += dT

        # If the block is finished then kill the emitters
        if self._currBlockTime >= self._timeLength:
            self._KillEmitters()
        
    def __str__(self):
        return 'P' + str(self.playerNum) + ' Block'
    
    # Used to kill this block -
    # it will then be cleaned up during the next simulation tick
    # The state machine will call this on the block when the player
    # actively stops blocking - otherwise this block will end only
    # once it reaches its time limit
    def Kill(self, ssfGame):
        Action.Kill(self)
        self._KillEmitters()
        
    def _KillEmitters(self):
        # Turn the block flames off on any emitters that this block may
        # have been affecting.
        if self._blockLEmitters != None:
            for emitter in self._blockLEmitters:
                emitter.FireOff(self.playerNum, FireEmitter.BLOCK_FLAME)
        if self._blockREmitters != None:
            for emitter in self._blockREmitters:
                emitter.FireOff(self.playerNum, FireEmitter.BLOCK_FLAME)

# Factory/Builder Methods for various Super Street Fire Blocks 
def BuildLeftBasicBlock(playerNum):
    return Block(playerNum, Action.LEFT_SIDE, 1, BLOCK_LENGTH)
def BuildRightBasicBlock(playerNum):
    return Block(playerNum, Action.RIGHT_SIDE, 1, BLOCK_LENGTH)

