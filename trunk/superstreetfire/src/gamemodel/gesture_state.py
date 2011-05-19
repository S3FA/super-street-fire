'''
gesture_state.py

Contains the 'abstract' super-class for all gesture states
that are visited within the GestureRecognizer's state machine.

A gesture state analyzes the overall gesture for the combination
of the left and right hands of a particular player in the
Super Street Fire game.

'''
import time
import logging
import pygame  # http://www.pygame.org/download.shtml
from collections import deque
from action import Action
import attack
import block
from resources.sounds import sounds

X = 0
Y = 1
Z = 2
ROLL = 0
PITCH = 1
YAW = 2

# testing the values
JAB_FWDACC_L1 = 400
JAB_FWDACC_L2 = 600

HOOK_LATGYR    = 220
HOOK_FWDACC_L1 = 400
HOOK_FWDACC_L2 = 600
HOOK_LATACC_L1 = 300
HOOK_LATACC_L2 = 400

BLOCK_VER_ANGLE = 70
BLOCK_RELEASE_ANGLE = 45

TIME_BETWEEN_MOVES = 0.1

class GestureState: 
    LOGGER_NAME = 'gestures'
    
    # High-level attack/defend gesture enumeration values
    NO_GESTURE                  = 0
    LEFT_JAB_ATTACK_GESTURE     = 1
    RIGHT_JAB_ATTACK_GESTURE    = 2
    
    LEFT_HOOK_ATTACK_GESTURE    = 3 
    RIGHT_HOOK_ATTACK_GESTURE   = 4
    
    HADOUKEN_ATTACK_GESTURE     = 10
    
    LEFT_BLOCK_DEFENSE_GESTURE  = 100
    RIGHT_BLOCK_DEFENSE_GESTURE = 101
    FULL_BLOCK_DEFENSE_GESTURE = 102
    
    def __init__(self, gestureRecognizer, playerNum):
        self._logger = logging.getLogger(GestureState.LOGGER_NAME)
        assert(gestureRecognizer != None)
        self.gestureRecognizer = gestureRecognizer
        self.playerNum         = playerNum
        self._changeStateFunc  = None
        
        
        # Set the state change function based on the player number...
        if playerNum == 1:
            self._changeStateFunc = gestureRecognizer._ChangeP1State
        elif playerNum == 2:
            self._changeStateFunc = gestureRecognizer._ChangeP2State
        else:
            assert(False)
    
    def __str__(self):
        return "State for " + str(self.playerNum)
       
    # Abstract method used to update the state with new glove data
    def Update(self, leftGloveData, rightGloveData, dT, timeStamp): assert(False)
    

    def getHandedMove(self, prevMove, player, glove):            
        # must release from block to read next move
        if (prevMove >= 100 and glove.heading[PITCH] < BLOCK_RELEASE_ANGLE):
            return GestureState.NO_GESTURE
        if (self._isHandBlocking(glove)):
            return GestureState.LEFT_BLOCK_DEFENSE_GESTURE + glove.hand

        # last move was some kind of punch, detect reverse state
        # needs to be adjusted for handedness
        if (prevMove > 0 and prevMove < 10):
            # the end of the punch-forward, decelerate
            if (glove.acceleration[X] < 0):
                self._logger.debug(str(prevMove) + ' Punch release ' + str(glove.acceleration[X]))
                return GestureState.NO_GESTURE

        if (glove.acceleration[X] > HOOK_FWDACC_L1 and abs(glove.rotation[X]) > HOOK_LATGYR):
            # swinging punch - lateral movement
            if (glove.acceleration[X] > HOOK_FWDACC_L2 and abs(glove.rotation[X]) > 350):
                player.power = 2
            return GestureState.LEFT_HOOK_ATTACK_GESTURE + glove.hand

        # punch is positive/negative during swing 
        if (glove.acceleration[X] > JAB_FWDACC_L1 and abs(glove.rotation[X]) < HOOK_LATGYR):
            # straight punch - less lateral movement
            if (glove.acceleration[X] > JAB_FWDACC_L2):
                player.power = 2
            return GestureState.LEFT_JAB_ATTACK_GESTURE + glove.hand
    
    def _isHandBlocking(self, glove):
        return abs(glove.acceleration[Z]) < 80 and glove.heading[PITCH] > BLOCK_VER_ANGLE
    
    def _determineMove(self, prevMove, player):
        lGlove = player.left
        rGlove = player.right
        
        newMove = -1
        # these may need a bit of shuffling to handle both/single handed moves
        # each line comes through at diff times  
        if (lGlove != None and rGlove != None):
            # keep these threshold values spaced out to avoid "bounce"
            if (self._isHandBlocking(lGlove) and self._isHandBlocking(rGlove)):
                newMove = GestureState.FULL_BLOCK_DEFENSE_GESTURE
                
            # hadouken - has a well defined end position at the end of the move..
            if (lGlove.heading[PITCH] > 45 and lGlove.heading[ROLL] > 45 and \
                rGlove.heading[PITCH] < -45 and rGlove.heading[ROLL] < -45):
                newMove = GestureState.HADOUKEN_ATTACK_GESTURE
            if (lGlove.heading[PITCH] < -45 and lGlove.heading[ROLL] < -45 and \
                rGlove.heading[PITCH] > 45 and rGlove.heading[ROLL] > 45):
                newMove = GestureState.HADOUKEN_ATTACK_GESTURE
            
            #if (newMove > -1): self._logger.info( 'BOTH:' + str(newMove)) 

        if (newMove == -1 and lGlove != None):
            newMove = self.getHandedMove(prevMove, player, lGlove)
            if (newMove > -1): self._logger.info( 'L:' + str(newMove)) 

        if (newMove == -1 and rGlove != None):  
            newMove = self.getHandedMove(prevMove, player, rGlove)
            if (newMove > -1): self._logger.debug( 'R:' + str(newMove)) 

        # do we consider this a new/valid gesture?
        deltaMoveTime = time.time()-player.lastMoveTs
        if (newMove > -1):
            # same move, or not enough time elapsed.. do nothing
            if (newMove == prevMove):
                self._logger.info( 'Same move; ' + str(prevMove)) 
                return 0
            if (deltaMoveTime < TIME_BETWEEN_MOVES):
                self._logger.warn( 'Not enough time between moves:' + str(deltaMoveTime)) 
                return 0
            
            # ensure we record a return to "No Gesture" base state. 
            self.recordMove( newMove )

        return newMove
            
       
    def _interpretState(self, playerState):
        prevMove = None
        if (len(playerState.moves) > 0): prevMove = playerState.moves[-1]
        
        newMove = self._determineMove(prevMove, playerState)
        if (newMove > 0):
            self._logger.info( 'Gesture state change; ' + str(playerState)) 
            hand = Action.LEFT_AND_RIGHT_SIDES
            #return a block..
            if (newMove >= 100):
                sounds.blockSound.play()
                hand = newMove - 100
                print ' --- BLOCK ! --- ' + str(hand)
                return block.Block(playerState.playerNum, hand, playerState.power, 5)
            
            # print some debug info on what kind of attack this was:
            if (newMove >= 10):
                sounds.hadouken.play()
                print ' *** ATTACK ! *** HADOUKEN '
                return attack.BuildHadoukenAttack(playerState.playerNum)
            
            if (newMove >= 3): 
                sounds.punchFierceSound.play()
                hand = newMove - 3
                print ' *** ATTACK ! *** HOOK ' + str(hand)
                return attack.Attack(playerState.playerNum, hand, newMove, playerState.power, 5)
                
            # standard jab punch attack
            hand = newMove - 1
            sounds.punchJabSound.play()
            print ' *** ATTACK ! *** JAB ' + str(hand)
            return attack.Attack(playerState.playerNum, hand, newMove, playerState.power, 5)
            

class PlayerGestureState(GestureState):
    
    def __init__(self, gestureRecognizer, playerNum):
        GestureState.__init__(self, gestureRecognizer, playerNum)
        self.lastMoveTs = time.time()
        # store the glove data - because it doesn't arrive at the same time
        self.left = None
        self.right = None
        #store some history - the past 6 moves.
        self.moves = deque(list(), 6)
        # store a "power" of movement (i.e. higher acceleration, greater power)
        self.power = 1
        
    def __str__(self):
        return 'GestureState P' + str(self.playerNum) + ' dMoveTs=' + str(self.lastMoveTs)

    def recordMove(self, move):
        # record the move value, and reset all the data
        self.moves.append(move)
        self.lastMoveTs = time.time()
        self.left = None
        self.right = None
        self.power = 1
        
    def Update(self, leftGloveData, rightGloveData, dT, timeStamp):
        # do we have anything to do?
        if (leftGloveData == None and rightGloveData == None): return
        
        if (leftGloveData != None):
            self._logger.debug(str(timeStamp-self.lastMoveTs) + " L:"+ str(leftGloveData) )
            self.left = leftGloveData
            
        if (rightGloveData != None):
            self._logger.debug(str(timeStamp-self.lastMoveTs) + " R:" + str(rightGloveData) )
            self.right = rightGloveData

        
        action = None
        if (self.left != None or self.right != None):
            #print "got some data for both hands " % ( self.left, self.right )
            action = self._interpretState(self)
        
        if (action != None):
            self._logger.info("Send action to queue: " + str(action) )
            self._changeStateFunc(action)