'''
ssf_game.py


@author: Callum Hay
'''

import player
import fire_emitter

class SSFGame:
    NUM_FIRE_EMITTERS_PER_ARC = 8
    TOTAL_NUM_FIRE_EMITTERS   = 2 * NUM_FIRE_EMITTERS_PER_ARC

    def __init__(self):
        # There are two players, facing off against each other
        self.player1 = player.Player()
        self.player2 = player.Player()
        
        # There are two arcs of fire emitters (on on the left and one on the right
        # of player 1) each with eight emitters
        self.leftEmitters  = []
        self.rightEmitters = []
        for i in range(0, SSFGame.NUM_FIRE_EMITTERS_PER_ARC):
            self.leftEmitters.append(fire_emitter.FireEmitter(i))
            self.rightEmitters.append(fire_emitter.FireEmitter(i))
        
    def Reset(self):
        self.player1.Reset()
        self.player2.Reset()
        for leftEmitter, rightEmitter in self.leftEmitters, self.rightEmitters:
            leftEmitter.Reset()
            rightEmitter.Reset()
    
    
    def Tick(self, dT):
        pass        
    
    