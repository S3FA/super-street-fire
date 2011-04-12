'''
Created on Apr 11, 2011

@author: callumhay
'''

from action import Action

class Block(Action):

    def __init__(self, playerNum, sideEnum):
        Action.__init__(self, playerNum, sideEnum)

    def Initialize(self, ssfGame):
        pass

    def Tick(self, ssfGame, dT):
        pass

    

# Factory/Builder Methods for various Super Street Fire Blocks 
def BuildLeftBasicBlock(playerNum):
    return Block(playerNum, Action.LEFT_SIDE)
def BuildRightBasicBlock(playerNum):
    return Block(playerNum, Action.RIGHT_SIDE)