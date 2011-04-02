'''
ssf_game.py


@author: Callum Hay
'''

import player
import fire_emitter
from gesture_recognizer import GestureRecognizer

class SSFGame:
    NUM_FIRE_EMITTERS_PER_ARC = 8
    TOTAL_NUM_FIRE_EMITTERS   = 2 * NUM_FIRE_EMITTERS_PER_ARC

    def __init__(self, gestureRecognizer):
        assert(gestureRecognizer != None)
        self.gestureRecognizer = gestureRecognizer
        
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
        # Gesture dictionary - maps various gestures from the gesture recognizer
        # to functions that handle those gestures
        self.GESTURE_FUNCTIONS = {
            GestureRecognizer.LEFT_JAB_ATTACK_GESTURE     : self._LeftJabGesture,
            GestureRecognizer.RIGHT_JAB_ATTACK_GESTURE    : self._RightJabGesture,
            GestureRecognizer.LEFT_HOOK_ATTACK_GESTURE    : self._LeftHookGesture,
            GestureRecognizer.RIGHT_HOOK_ATTACK_GESTURE   : self._RightHookGesture,
            GestureRecognizer.HADOUKEN_ATTACK_GESTURE     : self._HadoukenGesture,
            GestureRecognizer.LEFT_BLOCK_DEFENSE_GESTURE  : self._LeftBlockGesture,
            GestureRecognizer.RIGHT_BLOCK_DEFENSE_GESTURE : self._RightBlockGesture
        }
        
        
    def Reset(self):
        self.player1.Reset()
        self.player2.Reset()
        for leftEmitter, rightEmitter in self.leftEmitters, self.rightEmitters:
            leftEmitter.Reset()
            rightEmitter.Reset()
    
    
    def Tick(self, dT):
        # Check for any newly recognized gestures, execute any that get found
        if self.gestureRecognizer.GetP1HasNewGesture():
            p1Gesture = self.gestureRecognizer.PopP1Gesture()
            self._ExecuteGesture(1, p1Gesture)
        if self.gestureRecognizer.GetP2HasNewGesture():
            p2Gesture = self.gestureRecognizer.PopP2Gesture()
            self._ExecuteGesture(2, p2Gesture)
        
        # Tick the fire emitters...
        for leftEmitter, rightEmitter in self.leftEmitters, self.rightEmitters:
            leftEmitter.Tick(dT)
            rightEmitter.Tick(dT)
        
    # Private functions *****************************************    
    
    def _ExecuteGesture(self, playerNum, gesture):
        assert(gesture != GestureRecognizer.NO_GESTURE)
        gestureFunction = self.GESTURE_FUNCTIONS.get(gesture)
        gestureFunction(playerNum)

    def _LeftJabGesture(self, playerNum):
        pass
    def _RightJabGesture(self, playerNum):
        pass
    
    def _LeftHookGesture(self, playerNum):
        pass
    def _RightHookGesture(self, playerNum):
        pass
    
    def _HadoukenGesture(self, playerNum):
        pass
    
    def _LeftBlockGesture(self, playerNum):
        pass
    def _RightBlockGesture(self, playerNum):
        pass
    
    