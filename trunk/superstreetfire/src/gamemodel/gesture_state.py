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
from collections import deque
from action import Action
import attack
import block

X = 0
Y = 1
Z = 2

#using absolute values
JAB_FWDACC_L1 = 400
JAB_FWDACC_L2 = 600

HOOK_LATGYR    = 220
HOOK_FWDACC_L1 = 300
HOOK_FWDACC_L2 = 400
HOOK_LATACC_L1 = 300
HOOK_LATACC_L2 = 400

BLOCK_VER_ANGLE = 70
BLOCK_RELEASE_ANGLE = 45

TIME_BETWEEN_MOVES = 0.1

class GestureState: 
    LOGGER_NAME = 'gesture_state'
    
    # High-level attack/defend gesture enumeration values
    NO_GESTURE                  = 0
    LEFT_JAB_ATTACK_GESTURE     = 1
    RIGHT_JAB_ATTACK_GESTURE    = 2
    
    LEFT_HOOK_ATTACK_GESTURE    = 3 
    RIGHT_HOOK_ATTACK_GESTURE   = 4
    
    HADOUKEN_ATTACK_GESTURE     = 10
    
    LEFT_BLOCK_DEFENSE_GESTURE  = 101
    RIGHT_BLOCK_DEFENSE_GESTURE = 102
    
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
    

    def getMove(self, prevMove, player, glove):
        # must release from block to read next move
        # keep these values spaced out to avoid "bounce"
        if (prevMove > 100 and abs(glove.heading[1]) < BLOCK_RELEASE_ANGLE):
            return GestureState.NO_GESTURE
        elif (abs(glove.acceleration[Z]) < 50 and abs(glove.heading[1]) > BLOCK_VER_ANGLE):
            return GestureState.LEFT_BLOCK_DEFENSE_GESTURE + glove.hand
            
        # last move was a punch, detect reverse state
        if (prevMove > 0 and prevMove < 10):
            # the end of the punch-forward, decelerate
            if (glove.acceleration[X] < 0):
                self._logger.warn('Punch release ' + str(glove.acceleration[X]))
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
    
       
    def _interpretState(self, playerState):
        # interpret each hand individually for now (each line comes through at diff times)  
        newMove = None
        hand = Action.LEFT_AND_RIGHT_SIDES
        prevMove = None
        
        if (len(playerState.moves) > 0): prevMove = playerState.moves[-1]
        
        if (playerState.left != None):            
            hand = Action.LEFT_SIDE
            newMove = self.getMove(prevMove, playerState, playerState.left)

        if (playerState.right != None):  
            hand = Action.RIGHT_SIDE
            newMove = self.getMove(prevMove, playerState, playerState.right)

        deltaMoveTime = time.time()-playerState.lastMoveTs
        
        if (newMove != None):
            if (newMove == prevMove):
                # same move, or not enough time elapsed.. do nothing
                self._logger.info( 'Same move; ' + str(prevMove)) 
                return None
            if (deltaMoveTime < TIME_BETWEEN_MOVES):
                # same move, or not enough time elapsed.. do nothing
                self._logger.warn( 'Not enough time between moves:' + str(deltaMoveTime)) 
                return None
            
            # ensure we record a No Gesture base state. 
            self.moveDetected( newMove )
            if (newMove == 0): return None;
            
            self._logger.info( 'Gesture state change; ' + str(playerState)) 
                
            #return a block..
            if (newMove > 100):
                print ' --- BLOCK ! --- '
                return block.Block(playerState.playerNum, hand, playerState.power, 5)
            # print some debug info on what kind of attack this was:
            if (newMove > GestureState.RIGHT_JAB_ATTACK_GESTURE): print ' *** ATTACK ! *** HOOK '
            else: print ' *** ATTACK ! *** JAB '
            
            return attack.Attack(playerState.playerNum, hand, newMove, playerState.power, 5)
            

class PlayerGestureState(GestureState):
    
    def __init__(self, gestureRecognizer, playerNum):
        GestureState.__init__(self, gestureRecognizer, playerNum)
        self.lastMoveTs = time.time()
        self.left = None
        self.right = None
        self.moves = deque(list(), 6)
        self.power = 1
        
    def __str__(self):
        return 'GestureState P' + str(self.playerNum) + ' dMoveTs=' + str(self.lastMoveTs)

    def moveDetected(self, move):
        self.moves.append(move)
        self.lastMoveTs = time.time()
        self.left = None
        self.right = None
        
    def Update(self, leftGloveData, rightGloveData, dT, timeStamp):
        # do we have anything to do?
        if (leftGloveData == None and rightGloveData == None): return
        
        if (leftGloveData != None):
            #self._logger.debug(str(timeStamp-self.lastMoveTs) + " L:"+ str(leftGloveData) )
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