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
    ROUND_TIME_IN_SECONDS     = 60.0

    def __init__(self, gestureRecognizer):
        assert(gestureRecognizer != None)
        self.gestureRecognizer = gestureRecognizer
        
        # There are two players, facing off against each other
        self.player1 = player.Player()
        self.player2 = player.Player()
        
        # There's always a game timer, which counts down throughout a match
        self.roundTime = 0.0
        
        # There are two arcs of fire emitters (one on the left and one on the right
        # of player 1) each with eight emitters
        self.leftEmitters  = []
        self.rightEmitters = []
        for i in range(0, SSFGame.NUM_FIRE_EMITTERS_PER_ARC):
            self.leftEmitters.append(fire_emitter.FireEmitter(i))
            self.rightEmitters.append(fire_emitter.FireEmitter(i))
        
        #self._SetupPrevNextEmitters(self.leftEmitters)
        #self._SetupPrevNextEmitters(self.rightEmitters)
        
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
    
    def _ExecuteGesture(self, playerNum, gesture):
        assert(gesture != GestureRecognizer.NO_GESTURE)
        gestureFunction = self.GESTURE_FUNCTIONS.get(gesture)
        assert(gestureFunction != None)
        gestureFunction(playerNum)


    def _Attack(self, atkStartEmitter, atkEndEmitter, tripLength, jetThickness):
        # TODO
        pass
    
    def _Block(self, blockEmitter, timeLength):
        # TODO
        pass
    
    def _LeftJabGesture(self, playerNum):
        if playerNum == 1:
            # Start fire at the leftEmitter[0] fire emitter and propagate 
            # to leftEmitter[-1] for a 2 second trip, jet is 1 emitter thick
            self._Attack(self.leftEmitter[0], self.leftEmitter[-1], 2.0, 1)
        elif playerNum == 2:
            # Start fire at the rightEmitter[-1] fire emitter and propagate 
            # to rightEmitter[0] for a 2 second trip, jet is 1 emitter thick
            self._Attack(self.rightEmitter[-1], self.rightEmitter[0], 2.0, 1)
        else:
            assert(False)
        
    def _RightJabGesture(self, playerNum):
        if playerNum == 1:
            # Start fire at the rightEmitter[0] fire emitter and propagate 
            # to rightEmitter[-1] for a 2 second trip, jet is 1 emitter thick
            self._Attack(self.rightEmitter[0], self.rightEmitter[-1], 2.0, 1)
        elif playerNum == 2:
            # Start fire at the leftEmitter[-1] fire emitter and propagate 
            # to leftEmitter[0] for a 2 second trip, jet is 1 emitter thick
            self._Attack(self.leftEmitter[-1], self.leftEmitter[0], 2.0, 1)
        else:
            assert(False)
    
    def _LeftHookGesture(self, playerNum):
        if playerNum == 1:
            # Start fire at the leftEmitter[0] fire emitter and propagate 
            # to leftEmitter[-1] for a 3 second trip, jet is 2 emitters thick
            self._Attack(self.leftEmitter[0], self.leftEmitter[-1], 3.0, 2)
        elif playerNum == 2:
            # Start fire at the rightEmitter[-1] fire emitter and propagate 
            # to rightEmitter[0] for a 3 second trip, jet is 2 emitters thick
            self._Attack(self.rightEmitter[-1], self.rightEmitter[0], 3.0, 2)
        else:
            assert(False)
    
    def _RightHookGesture(self, playerNum):
        if playerNum == 1:
            # Start fire at the rightEmitter[0] fire emitter and propagate 
            # to rightEmitter[-1] for a 3 second trip, jet is 2 emitter thick
            self._Attack(self.rightEmitter[0], self.rightEmitter[-1], 3.0, 2)
        elif playerNum == 2:
            # Start fire at the leftEmitter[-1] fire emitter and propagate 
            # to leftEmitter[0] for a 3 second trip, jet is 2 emitter thick
            self._Attack(self.leftEmitter[-1], self.leftEmitter[0], 3.0, 2)
        else:
            assert(False)
    
    def _HadoukenGesture(self, playerNum):
        # Fire on both sides of the player with a 4 second travel time on both side,
        # jets are 2 emitters thick on both sides
        if playerNum == 1:
            self._Attack(self.leftEmitter[0],  self.leftEmitter[-1],  4.0, 2)
            self._Attack(self.rightEmitter[0], self.rightEmitter[-1], 4.0, 2)
        elif playerNum == 2:
            self._Attack(self.rightEmitter[-1], self.rightEmitter[0], 4.0, 2)
            self._Attack(self.leftEmitter[-1],  self.leftEmitter[0],  4.0, 2)
        else:
            assert(False)
    
    def _LeftBlockGesture(self, playerNum):
        if playerNum == 1:
            self._Block(self.leftEmitter[0], 4.0)
        elif playerNum == 2:
            self._Block(self.rightEmitter[-1], 4.0)
        else:
            assert(False)
            
    def _RightBlockGesture(self, playerNum):
        if playerNum == 1:
            self._Attack(self.rightEmitter[0], 4.0)
        elif playerNum == 2:
            self._Attack(self.leftEmitter[-1], 4.0)
        else:
            assert(False)
    
    