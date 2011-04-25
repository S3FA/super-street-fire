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
        
        # A list of all active actions in the game
        self._activeActions = []
        
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
    
    
    # These functions provide convenience, when accessing the fire emitter arc lists:
    # The emitters are, by default, layed out from player 1's perspective so 
    # we need to reverse them for player 2...    
    def GetLeftEmitterArc(self, playerNum):
        if playerNum == 1:
            return self.leftEmitters
        else:
            return self.rightEmitters
    def GetRightEmitterArc(self, playerNum):
        if playerNum == 1:
            return self.rightEmitters
        else:
            return self.leftEmitters    
    
    def IsRoundOver(self):
        return (self.player1.IsKnockedOut() or self.player2.IsKnockedOut() or \
               self.roundTime >= SSFGame.ROUND_TIME_IN_SECONDS)
    
    def Tick(self, dT):
        # Check for any newly recognized gestures, execute any that get found
        if self.gestureRecognizer.HasNewActionsAvailable():
            newActions = self.gestureRecognizer.PopActions()
            # assert(len(newActions) > 0)
            # Initialize all the new actions
            for action in newActions:
                action.Initialize(self)
            self._activeActions.extend(newActions)
            
        # Tick any actions (e.g., attacks, blocks) that are currently active within the game
        # This will update the state of the fire emitters and the game in general
        actionsToRemove = []
        for action in self._activeActions:
            if action.IsFinished():
                actionsToRemove.append(action)
            else:
                action.Tick(self, dT)
        
        # Clear up all finished actions
        for action in actionsToRemove:
            self._activeActions.remove(action)
        
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

    