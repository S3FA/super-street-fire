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

TIME_BETWEEN_MOVES = 0.325

class GestureState: 
    LOGGER_NAME = 'gesture_state_logger'
    
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
            
    # Abstract method used to update the state with new glove data
    def Update(self, leftGloveData, rightGloveData, dT, timeStamp): assert(False)
    

    def getMove(self, player, glove):
        # must release from block to read next move
        # keep these values spaced out to avoid "bounce"
        if (player.prevMove > 100 and abs(glove.heading[1]) < BLOCK_RELEASE_ANGLE):
            return GestureState.NO_GESTURE
        elif (abs(glove.acceleration[Z]) < 50 and abs(glove.heading[1]) > BLOCK_VER_ANGLE):
            return GestureState.LEFT_BLOCK_DEFENSE_GESTURE + glove.hand
            
        if (abs(glove.acceleration[X]) > HOOK_FWDACC_L1 and abs(glove.rotation[X]) > HOOK_LATGYR):
            # swinging punch - lateral movement
            if (abs(glove.acceleration[X]) > HOOK_FWDACC_L2 and abs(glove.rotation[X]) > 350):
                player.power = 2
            return GestureState.LEFT_HOOK_ATTACK_GESTURE + glove.hand

        # using the abs acceleration - punch is positive/negative during swing  
        if (abs(glove.acceleration[X]) > JAB_FWDACC_L1 and abs(glove.rotation[X]) < HOOK_LATGYR):
            # straight punch - less lateral movement
            if (abs(glove.acceleration[X]) > JAB_FWDACC_L2):
                player.power = 2
            return GestureState.LEFT_JAB_ATTACK_GESTURE + glove.hand
    
       
    def _interpretState(self, playerState):
        # interpret each hand individually for now (each line comes through at diff times)  
        newMove = None
        hand = Action.LEFT_AND_RIGHT_SIDES
        
        if (playerState.left != None):            
            hand = Action.LEFT_SIDE
            newMove = self.getMove(playerState, playerState.left)

        if (playerState.right != None):  
            hand = Action.RIGHT_SIDE
            newMove = self.getMove(playerState, playerState.right)

        deltaMoveTime = time.time()-playerState.lastMoveTs
        if (deltaMoveTime > TIME_BETWEEN_MOVES):
            playerState.prevMove = None
            
        if (newMove != None and newMove > 0):
            if (newMove == self.prevMove or deltaMoveTime < TIME_BETWEEN_MOVES):
                # same move, or not enough time elapsed.. do nothing
                self._logger.info( 'Same move; ' + str(newMove) + ' or not enough time:' + str(deltaMoveTime)) 
                return None
            
            self._logger.info( 'Gesture state change; ' + str(playerState)) 
            self.moveDetected( newMove )
                
            if (newMove > 100):
                print ' --- BLOCK ! --- '
                return block.Block(playerState.playerNum, hand, playerState.power, 5)
            
            if (newMove > GestureState.RIGHT_JAB_ATTACK_GESTURE): print ' *** ATTACK ! *** HOOK '
            else: print ' *** ATTACK ! *** JAB '
            return attack.Attack(playerState.playerNum, hand, newMove, playerState.power, 5)
            

class PlayerGestureState(GestureState):
    lastMoveTs = 0
    left = None
    right = None
    prevMove = None
    move = None
    power = 1
    
    def __init__(self, gestureRecognizer, playerNum):
        GestureState.__init__(self, gestureRecognizer, playerNum)
        
    def moveDetected(self, move):
        self.prevMove = move
        self.lastMoveTs = time.time()
        self.left = None
        self.right = None
        
    def Update(self, leftGloveData, rightGloveData, dT, timeStamp):
        if (leftGloveData == None and rightGloveData == None): return
        if (leftGloveData != None):
            self.left = leftGloveData
        if (rightGloveData != None):
            self.right = rightGloveData

        self._logger.debug("Updating Gesture: " + str(timeStamp-self.lastMoveTs) + " L:"+ str(leftGloveData) + "--R:" + str(rightGloveData) )
        
        action = None
        if (self.left != None or self.right != None):
            #print "got some data for both hands " % ( self.left, self.right )
            action = self._interpretState(self)
        
        if (action != None):
            self._logger.info("Send action to queue: " + str(action) )
            self._changeStateFunc(action)