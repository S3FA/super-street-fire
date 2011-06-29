'''
ssf_game.py


@author: Callum Hay
'''

import logging
import player
import game_states
from fire_emitter import FireEmitter
from gesture_recognizer import GestureRecognizer
from game_model_listener import GameModelListenerCmdr
from gamemodel import *

class SSFGame:
    LOGGER_NAME = 'ssf_game_logger'
    
    def __init__(self, ioManager):
        self._logger           = logging.getLogger(SSFGame.LOGGER_NAME)
        self._listenerCmdr     = GameModelListenerCmdr()
        self.gestureRecognizer = GestureRecognizer()
        self.ioManager         = ioManager
        
        # There are two players, facing off against each other
        self.player1 = player.Player(1)
        self.player2 = player.Player(2)
        self.chipDamageOn = True
        self.demoMode     = False
        self.roundNumber  = 1
        
        # There are two arcs of fire emitters (one on the left and one on the right
        # of player 1) each with eight emitters
        self.leftEmitters  = []
        self.rightEmitters = []
        for i in range(0, FireEmitter.NUM_FIRE_EMITTERS_PER_ARC):
            self.leftEmitters.append(FireEmitter(i, FireEmitter.LEFT_ARC, self._listenerCmdr))
            self.rightEmitters.append(FireEmitter(i, FireEmitter.RIGHT_ARC, self._listenerCmdr))

        # Set the first state for the game
        # NOTE: Always be sure to set the state LAST in the constructor since
        # it might make use of members that belong to this object
        self._SetState(game_states.IdleGameState(self))
    
    def RegisterListener(self, listener):
        self._listenerCmdr.RegisterListener(listener)
    
    def StopAll(self):
        self.Reset()
        self.StopGame()
    
    def Reset(self):
        self.ioManager.GoTheFuckToSleep()
        self.chipDamageOn = True
        self.player1.Reset()
        self.player2.Reset()
        self.KillEmitters()
    
    def KillEmitters(self):
        self._logger.debug("Killing fire emitters")
        self.ioManager.GoTheFuckToSleep()
        for leftEmitter, rightEmitter in map(None, self.leftEmitters, self.rightEmitters):
            leftEmitter.Reset()
            rightEmitter.Reset()
    
    # These functions provide convenience, when accessing the fire emitter arc lists:
    # The emitters are, by default, layed out from player 1's perspective so 
    # we need to reverse them for player 2...
    # 20110627 snb: trick the fire output into burning for half the time by pretending
    # there are twice as many emitters (but let the game tick through all)
    def GetLeftEmitters(self, playerNum, isFireRead):
        left = list()
        step = 1
        if (isFireRead): step = 2
        if playerNum == 1:
            for i in range(0, len(self.leftEmitters), step):
                left.append( self.leftEmitters[i] )
        else:
            for i in range(0, len(self.rightEmitters), step):
                left.append( self.rightEmitters[i] )
        return left
    
    def GetLeftEmitterArc(self, playerNum):
        return self.GetLeftEmitters(playerNum, False)
    
    def GetRightEmitters(self, playerNum, isFireRead):
        right = list()
        step = 1
        if (isFireRead): step = 2
        if playerNum == 1:
            for i in range(0, len(self.rightEmitters), step):
                right.append( self.rightEmitters[i] )
        else:
            for i in range(0, len(self.leftEmitters), step):
                right.append( self.leftEmitters[i] )
        return right

    def GetRightEmitterArc(self, playerNum):
        return self.GetRightEmitters(playerNum, False)

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
    
    def DemoGame(self):
        self.player1.isInvincible = True
        self.player2.isInvincible = True
        self.demoMode = True
        self.state.StartGame()
    
    def StartGame(self):
        self.state.StartGame()
    def TogglePauseGame(self):
        self.state.TogglePauseGame()
    def StopGame(self):
        self.state.StopGame()
    def UpdateWithHeadsetData(self, p1HeadsetData, 
                              p2HeadsetData, dT, timeStamp):
        self.state.UpdateWithHeadsetData(p1HeadsetData, p2HeadsetData, dT, timeStamp)
    def UpdateWithGloveData(self, p1LGloveData, p1RGloveData, 
                            p2LGloveData, p2RGloveData, dT, timeStamp):
        self.state.UpdateWithGloveData(p1LGloveData, p1RGloveData, p2LGloveData, \
                                       p2RGloveData, dT, timeStamp)
    def UpdateRSSI(self, rssi_dict):
        self._listenerCmdr.ReceivedSignalStrengthIndicatorChanged(rssi_dict)
    
    def Hurt(self, playerNum, dmgAmt, isChipDmg):
        assert(playerNum == 1 or playerNum == 2)
        
        # If chip damage is disabled and the damage being dealt
        # is chip damage then we ignore it
        if isChipDmg and not self.chipDamageOn:
            self._logger.debug("Attack landed while blocking - chip damage is off, no damage dealt")
            return
        
        if playerNum == 1:
            self.player1.DoDamage(dmgAmt)
        else:
            self.player2.DoDamage(dmgAmt)    
        
        self._listenerCmdr.PlayerHealthChanged((self.player1,self.player2))
        
        
    # Private functions *****************************************    

    def _SetState(self, gameState):
        self._logger.debug("Changing game state to " + str(gameState))
        assert(gameState != None)
        self.state = gameState
        # EVENT: Game state just changed
        self._listenerCmdr.GameStateChanged(gameState)
    
    def _ExecuteGameActions(self, dT, actionsQueue):
        # Check for any newly recognized gestures, execute any that get found
        if self.gestureRecognizer.HasNewActionsAvailable():
            newActions = self.gestureRecognizer.PopActions()
            assert(len(newActions) > 0)
            
            # Initialize all the new actions
            for action in newActions:
                action.Initialize(self)
            actionsQueue.extend(newActions)
            self._listenerCmdr.PlayerMoves(newActions)
            
        # Tick any actions (e.g., attacks, blocks) that are currently active within the game
        # This will update the state of the fire emitters and the game in general
        actionsToRemove = []
        for action in actionsQueue:
            if action.IsFinished():
                actionsToRemove.append(action)
            else:
                action.Tick(self, dT)
            #TODO-- check if this updates better/faster..
            self.ioManager.SendFireEmitterData(self.GetLeftEmitters(1, True), \
                                               self.GetRightEmitters(1, True))
        
        # Clear up all finished actions
        for action in actionsToRemove:
            actionsQueue.remove(action)

    # Gui test methods *****************************************    
    def TestP1Jab(self, side='l'):
        if side.lower() == 'l':
            self.gestureRecognizer.ChangeP1State(attack.BuildLeftJabAttack(1))
        else:
            self.gestureRecognizer.ChangeP1State(attack.BuildRightJabAttack(1))
    def TestP2Jab(self, side='l'):
        if side.lower() == 'l':
            self.gestureRecognizer.ChangeP1State(attack.BuildLeftJabAttack(2))
        else:
            self.gestureRecognizer.ChangeP1State(attack.BuildRightJabAttack(2))
    def TestP1Hook(self, side='l'):
        if side.lower() == 'l':
            self.gestureRecognizer.ChangeP1State(attack.BuildLeftHookAttack(1))
        else:
            self.gestureRecognizer.ChangeP1State(attack.BuildRightHookAttack(1))
    def TestP2Hook(self, side='l'):
        if side.lower() == 'l':
            self.gestureRecognizer.ChangeP1State(attack.BuildLeftHookAttack(2))
        else:
            self.gestureRecognizer.ChangeP1State(attack.BuildRightHookAttack(2))
    def TestP1SonicBoom(self):
        self.gestureRecognizer.ChangeP1State(attack.BuildSonicBoomAttack(1))
    def TestP2SonicBoom(self):
        self.gestureRecognizer.ChangeP1State(attack.BuildSonicBoomAttack(2))
    def TestP1Hadouken(self):
        self.gestureRecognizer.ChangeP1State(attack.BuildHadoukenAttack(1))
    def TestP2Hadouken(self):
        self.gestureRecognizer.ChangeP1State(attack.BuildHadoukenAttack(2))
    def TestP1Block(self, side='l'):
        if side.lower() == 'l':
            self.gestureRecognizer.ChangeP1State(block.BuildLeftBasicBlock(1))
        else:
            self.gestureRecognizer.ChangeP1State(block.BuildRightBasicBlock(1))
    def TestP2Block(self, side='l'):
        if side.lower() == 'l':
            self.gestureRecognizer.ChangeP1State(block.BuildLeftBasicBlock(2))
        else:
            self.gestureRecognizer.ChangeP1State(block.BuildRightBasicBlock(2))
        
    