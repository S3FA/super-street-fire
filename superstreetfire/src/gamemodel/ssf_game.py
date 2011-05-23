'''
ssf_game.py


@author: Callum Hay
'''

import player
from game_states import IdleGameState
from fire_emitter import FireEmitter
from gesture_recognizer import GestureRecognizer


class SSFGame:
    def __init__(self):
        self.state = IdleGameState(self)
        self.gestureRecognizer = GestureRecognizer()
        
        # There are two players, facing off against each other
        self.player1 = player.Player(1)
        self.player2 = player.Player(2)
        self.chipDamageOn = True

        self.roundNumber = 1
        
        # There are two arcs of fire emitters (one on the left and one on the right
        # of player 1) each with eight emitters
        self.leftEmitters  = []
        self.rightEmitters = []
        for i in range(0, FireEmitter.NUM_FIRE_EMITTERS_PER_ARC):
            self.leftEmitters.append(FireEmitter(i, FireEmitter.LEFT_ARC))
            self.rightEmitters.append(FireEmitter(i, FireEmitter.RIGHT_ARC))

    def Reset(self):
        self.chipDamageOn = True
        self.player1.Reset()
        self.player2.Reset()
        self.KillEmitters()
    
    def KillEmitters(self):
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


    # Gets the winner of the game - if there is no winner -1 is returned,
    # if the game is a tie then 0 is returned.
    def GetGameWinner(self):
        if self.player1.numRoundWins >= 2:
            if self.player2.numRoundWins >= 2:
                # TIE...
                return 0
            else:
                return 1
        elif self.player2.numRoundWins >= 2:
            return 2
        else:
            return -1
    
    # State Functions - the bodies of these functions are defined by the currently
    # set state machine (see game_states.py)
    def Tick(self, dT):
        self.state.Tick(dT)
    def StartGame(self):
        self.state.StartGame()
    def StopGame(self):
        self.state.StopGame()
    def UpdateWithHeadsetData(self, p1HeadsetData, 
                              p2HeadsetData, dT, timeStamp):
        self.state.UpdateWithHeadsetData(p1HeadsetData, p2HeadsetData, dT, timeStamp)
    def UpdateWithGloveData(self, p1LGloveData, p1RGloveData, 
                            p2LGloveData, p2RGloveData, dT, timeStamp):
        self.state.UpdateWithGloveData(p1LGloveData, p1RGloveData, p2LGloveData, \
                                       p2RGloveData, dT, timeStamp)
    
    def Hurt(self, playerNum, dmgAmt, isChipDmg):
        assert(playerNum == 1 or playerNum == 2)
        
        # If chip damage is disabled and the damage being dealt
        # is chip damage then we ignore it
        if isChipDmg and not self.chipDamageOn:
            return
        
        if playerNum == 1:
            self.player1.DoDamage(dmgAmt)
        else:
            self.player2.DoDamage(dmgAmt)    
  
    # Private functions *****************************************    

    def _SetState(self, gameState):
        assert(gameState != None)
        self.state = gameState
    
    def _ExecuteGameActions(self, dT, actionsQueue):
        # Check for any newly recognized gestures, execute any that get found
        if self.gestureRecognizer.HasNewActionsAvailable():
            newActions = self.gestureRecognizer.PopActions()
            assert(len(newActions) > 0)
            
            # Initialize all the new actions
            for action in newActions:
                action.Initialize(self)
            actionsQueue.extend(newActions)
            
        # Tick any actions (e.g., attacks, blocks) that are currently active within the game
        # This will update the state of the fire emitters and the game in general
        actionsToRemove = []
        for action in actionsQueue:
            if action.IsFinished():
                actionsToRemove.append(action)
            else:
                action.Tick(self, dT)
        
        # Clear up all finished actions
        for action in actionsToRemove:
            actionsQueue.remove(action)
    