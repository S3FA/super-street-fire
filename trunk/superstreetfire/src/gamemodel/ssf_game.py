'''
ssf_game.py


@author: Callum Hay
'''

import player
from fire_emitter import FireEmitter
from gesture_recognizer import GestureRecognizer

class SSFGame:
    ROUND_TIME_IN_SECONDS = 60.0

    def __init__(self, gestureRecognizer):
        assert(gestureRecognizer != None)
        self.gestureRecognizer = gestureRecognizer
        
        # There are two players, facing off against each other
        self.player1 = player.Player(1)
        self.player2 = player.Player(2)
        
        # There's always a game timer, which counts down throughout a match
        self.roundTime = 0.0
        
        # There are two arcs of fire emitters (one on the left and one on the right
        # of player 1) each with eight emitters
        self.leftEmitters  = []
        self.rightEmitters = []
        for i in range(0, FireEmitter.NUM_FIRE_EMITTERS_PER_ARC):
            self.leftEmitters.append(FireEmitter(i, FireEmitter.LEFT_ARC))
            self.rightEmitters.append(FireEmitter(i, FireEmitter.RIGHT_ARC))

    def Reset(self):
        self.player1.Reset()
        self.player2.Reset()
        for leftEmitter, rightEmitter in self.leftEmitters, self.rightEmitters:
            leftEmitter.Reset()
            rightEmitter.Reset()
    
    def IsRoundOver(self):
        return (self.player1.IsKnockedOut() or self.player2.IsKnockedOut() or \
               self.roundTime >= SSFGame.ROUND_TIME_IN_SECONDS)
    
    def Tick(self, dT):
        # Check for any newly recognized gestures, execute any that get found
        if self.gestureRecognizer.GetP1HasNewGesture():
            p1Gesture = self.gestureRecognizer.PopP1Gesture()
            self._ExecuteGesture(1, p1Gesture)
        if self.gestureRecognizer.GetP2HasNewGesture():
            p2Gesture = self.gestureRecognizer.PopP2Gesture()
            self._ExecuteGesture(2, p2Gesture)

        # Tick any actions (e.g., attacks, blocks) that are currently active within the game
        # TODO
        
        # Diminish the round timer
        self.roundTime += dT
        
        # Check to see if the current round is over...
        #if self.IsRoundOver():

    def Hurt(self, playerNum, dmgAmt):
        assert(playerNum == 1 or playerNum == 2)
        if playerNum == 1:
            self.player1.DoDamage(dmgAmt)
        else:
            self.player2.DoDamage(dmgAmt)
    
  
  
    # Private functions *****************************************    
    
    '''
    # Sets the next and previous emitters for the emitter arc list
    # passed to this function
    def _SetupPrevNextEmitters(self, emitters):
        emitterArrayLength = len(emitters)
        for i in range(0, emitterArrayLength):
            prevEmitter = None
            nextEmitter = None
            if i != 0:
                prevEmitter = emitters[i-1]
            if i != emitterArrayLength-1:
                nextEmitter = emitters[i+1]
            
            emitters[i].SetPrevAndNextEmitters(prevEmitter, nextEmitter)
    '''
    
    def _ExecuteGestureAction(self, gestureAction):
        assert(gestureAction != None)
        # TODO

    