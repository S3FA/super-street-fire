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
JAB_FWDACC_L1 = 94
JAB_FWDACC_L2 = 110

HOOK_LATGYR    = 80
HOOK_FWDACC_L1 = 90
HOOK_FWDACC_L2 = 110
HOOK_LATACC_L1 = 130
HOOK_LATACC_L2 = 200

BLOCK_VER_ANGLE = 70
BLOCK_RELEASE_ANGLE = 50

TIME_BETWEEN_MOVES = 1.0
TIME_TO_CLEAR_MOVE = 0.8

class GestureState: 
    LOGGER_NAME = 'gestures'
    
    LEFT=0
    RIGHT=1
    BOTH=2
    # High-level attack/defend gesture enumeration values
    NO_GESTURE    = 0
    LEFT_JAB      = 1
    RIGHT_JAB     = 2
    LEFT_HOOK     = 3 
    RIGHT_HOOK    = 4
    L_UPPERCUT    = 5
    R_UPPERCUT    = 6
    L_DRAGONPUNCH = 7
    R_DRAGONPUNCH = 8

    HADOUKEN     = 10
    BOTH_FWD     = 110
    SONIC_BOOM   = 12
    SONIC_SIDE   = 112
    
    LEFT_BLOCK   = 100
    RIGHT_BLOCK  = 101
    FULL_BLOCK   = 102
    
    def __init__(self, gestureRecognizer, playerNum):
        self._logger = logging.getLogger(GestureState.LOGGER_NAME)
        assert(gestureRecognizer != None)
        self.gestureRecognizer = gestureRecognizer
        self.playerNum         = playerNum
        self._changeStateFunc  = None
        
        
        # Set the state change function based on the player number...
        if playerNum == 1:
            self._changeStateFunc = gestureRecognizer.ChangeP1State
        elif playerNum == 2:
            self._changeStateFunc = gestureRecognizer.ChangeP2State
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
            return GestureState.LEFT_BLOCK + glove.hand

        # last move was some kind of punch, detect reverse state
        # needs to be adjusted for handedness
        if (prevMove > 0 and prevMove < 10):
            # the end of the punch-forward, decelerate
            if (glove.acceleration[X] < 0):
                self._logger.debug(str(prevMove) + ' Punch release ' + str(glove.acceleration[X]))
                return GestureState.NO_GESTURE
## 1-L:A: (1.0, -9.0, -3.0), H: (-15.9, 55.15, 104.0),R: (3.0, -6.75, 4.5)-R:A: (3.0, -3.0, 24.0), H: (-60.72, 81.23, -28.28),R: (4.25, -1.0, -6.75)

        # uppercut .. 
        if ( abs(glove.acceleration[X]) > 85 and abs(glove.rotation[X]) > 40 and \
             abs(glove.heading[PITCH]) > 50 ):
            if ( abs(glove.acceleration[Y]) > 100 ):
                player.power = 2
            return GestureState.L_UPPERCUT + glove.hand

        if (glove.acceleration[X] > HOOK_FWDACC_L1 and abs(glove.rotation[X]) > HOOK_LATGYR):
            # swinging punch - lateral movement
            if (glove.acceleration[X] > HOOK_FWDACC_L2 and abs(glove.rotation[X]) > 350):
                player.power = 2
            return GestureState.LEFT_HOOK + glove.hand

        # punch is straight - positive/negative accel during swing 
        if (abs(glove.heading[ROLL]) < 50 and 
            abs(glove.acceleration[X]) > JAB_FWDACC_L1 and abs(glove.rotation[X]) < 50 ):
            # straight punch - less lateral movement
            if (abs(glove.acceleration[X]) > JAB_FWDACC_L2):
                player.power = 2
            return GestureState.LEFT_JAB + glove.hand

    # detecting a block is a combination of no acceleration and hand position
    # straight up     
    def _isHandBlocking(self, glove):
        return abs(glove.acceleration[X]) < 40 and abs(glove.acceleration[Y]) < 50 and \
            abs(glove.acceleration[Z]) < 50 and \
            glove.heading[PITCH] > BLOCK_VER_ANGLE
    
    # figure out the move, looking for two-handed moves first, then by hand.
    def _determineMove(self, player):
        lGlove = player.left
        rGlove = player.right
        
        prevMove = 0
        if (len(player.allMoves[GestureState.BOTH]) > 0):
            prevMove = player.allMoves[GestureState.BOTH][-1]
        newMove = -1
        # these may need a bit of shuffling to handle both/single handed moves
        # each line comes through at diff times  
        # allow two-handed moves to be the final move in this tick
        if (lGlove != None and rGlove != None):
            totalx = (abs(lGlove.acceleration[X]) + abs(rGlove.acceleration[X]))
            totaly = (abs(lGlove.acceleration[Y]) + abs(rGlove.acceleration[Y]))
            # sonic boom - two handed move - use accel + gyros rolling inward
            # fists sideways: roll is L:-80 R:80
            if (lGlove.heading[ROLL] > -105 and lGlove.heading[ROLL] < -65 and \
                rGlove.heading[ROLL] < 105 and rGlove.heading[ROLL] > 65 and \
                lGlove.heading[PITCH] < 28 and rGlove.heading[PITCH] < 28 and \
                totaly < 120):
                # both hands sideways, set "no move" because it's probably
                # going to be a sonic boom when accel+gyros are higher
                newMove = GestureState.SONIC_SIDE
                print "SONIC SIDE POSITION ______________"
                self.recordMove( GestureState.BOTH, newMove, player.lastMoveTs )
            if (prevMove == GestureState.SONIC_SIDE and \
                totalx > 190 ):
                newMove = GestureState.SONIC_BOOM
                self.recordMove( GestureState.BOTH, newMove, player.lastMoveTs )
                return

            # hadouken - check for forward accel - both hands
            if ( totalx > 140 ):
                newMove = GestureState.BOTH_FWD
                print "BOTH FORWARD MOVE ______________"
                self.recordMove( GestureState.BOTH, newMove, player.lastMoveTs )
            # hadouken - has a well defined end position at the end of the move..
            # the problem with these values is that they are relative to the 
            # initial calibration.. 
            # right handed hadouken
            if (prevMove == GestureState.BOTH_FWD):
                diffYaw = lGlove.heading[YAW] - rGlove.heading[YAW]
                diffRoll = lGlove.heading[ROLL] - rGlove.heading[ROLL]
                #print 'diffYaw ' + str(diffYaw) + ' diffRoll ' + str(diffRoll)
                if (abs(diffYaw) > 110 and abs(diffRoll) > 160):
                    newMove = GestureState.HADOUKEN
                    self.recordMove( GestureState.BOTH, newMove, player.lastMoveTs )
                    return

            # must release from block to read next move
            if (prevMove == 102 and lGlove.heading[PITCH] < BLOCK_RELEASE_ANGLE and \
                rGlove.heading[PITCH] < BLOCK_RELEASE_ANGLE):
                newMove = GestureState.NO_GESTURE 
                self.recordMove( GestureState.BOTH, newMove, player.lastMoveTs )
            elif (self._isHandBlocking(lGlove) and self._isHandBlocking(rGlove)):
                # keep these threshold values spaced out to avoid "bounce"
                newMove = GestureState.FULL_BLOCK 
                self.recordMove( GestureState.BOTH, newMove, player.lastMoveTs )
                return
            
        # if not a two-handed move, record each; left + right.
        if (lGlove != None):
            if (len(player.allMoves[GestureState.LEFT]) > 0):
                prevMove = player.allMoves[GestureState.LEFT][-1]
            newMove = self.getHandedMove(prevMove, player, lGlove)
            if (newMove > -1): 
                #self._logger.info( 'L:' + str(newMove)) 
                self.recordMove( GestureState.LEFT, newMove,  player.lastMoveTs )

        if (rGlove != None):  
            if (len(player.allMoves[GestureState.RIGHT]) > 0):
                prevMove = player.allMoves[GestureState.RIGHT][-1]
            newMove = self.getHandedMove(prevMove, player, rGlove)
            if (newMove > -1): 
                #self._logger.debug( 'R:' + str(newMove)) 
                self.recordMove(  GestureState.RIGHT, newMove, player.lastMoveTs )

        return newMove        



class PlayerGestureState(GestureState):
    
    def __init__(self, gestureRecognizer, playerNum):
        GestureState.__init__(self, gestureRecognizer, playerNum)
        self.lastMoveTs = time.time()
        # store the glove data - because it doesn't arrive at the same time
        self.left = None
        self.right = None
        #store some history - the past 6 moves.
        self.moves = deque(list(), 6)
        # allmoves - left, right, both
        self.allMoves = ( list(), list(), list() )
        # store a "power" of movement (i.e. higher acceleration, greater power)
        self.power = 1
        
    def __str__(self):
        return 'GestureState P' + str(self.playerNum) + ' dMoveTs=' + str(self.lastMoveTs)

    def recordMove(self, type, move, lastTs):
        # do we consider this a new/valid gesture?
        deltaMoveTime = time.time()-lastTs
        
        # clear the move after a certain time, so the same move can be executed.
        if (move > 0 and len(self.allMoves[type]) > 0 and move == self.allMoves[type][-1]):
            if (deltaMoveTime > TIME_TO_CLEAR_MOVE):
                move = 0
            else:
                if (move < 110): self._logger.info( 'Same move; ' + str(move)) 
                return 
        
        # same move, or not enough time elapsed.. do nothing
        if (move > 0 and deltaMoveTime < TIME_BETWEEN_MOVES):
            self._logger.warn( 'Not enough time between moves:' + str(deltaMoveTime) + " move:" + str(move)) 
            return
            
        if (move > 0): self._logger.warn( '--======-> Adding move; ' + str(move)) 
        # record the move value, and reset all the data
        self.moves.append(move)
        self.allMoves[type].append(move)
        self.lastMoveTs = time.time()
        self.left = None
        self.right = None
        self.power = 1
        
    def Update(self, leftGloveData, rightGloveData, dT, timeStamp):
        # do we have anything to do?
        if (leftGloveData == None and rightGloveData == None): return
        
        if (leftGloveData != None):
            self.left = leftGloveData
            
        if (rightGloveData != None):
            self.right = rightGloveData

        if (self.left != None and self.right != None):
            self._logger.warn(str(self.playerNum)+"-L:" + str(self.left) + "-R:" + str(self.right) )
            #print "got some data for both hands " % ( self.left, self.right )
            self._determineMove(self)
            #if (len(playerState.moves) > 0):
            # send the attack/block whatever to the change state function
            self._doMoves()

        
    def _doMoves(self):
        for newMove in self.moves:            
            self._logger.debug( 'Gesture state change; ' + str(self) + " move=" + str(newMove)) 
            #return a block..
            if (newMove >= 100 and newMove <= 102):
                hand = newMove - 100
                print ' --- BLOCK ! --- ' + str(hand)
                sounds.blockSound.play()
                self._changeStateFunc(block.Block(self.playerNum, hand, self.power, 5))
            
            # print some debug info on what kind of attack this was:
            if (newMove == 10):
                print ' *** ATTACK ! *** HADOUKEN '
                self._changeStateFunc(attack.BuildHadoukenAttack(self.playerNum))
                sounds.hadouken.play()

            if (newMove == 12):
                print ' *** ATTACK ! *** SONIC BOOM '
                sounds.sonicBoom.play()
                self._changeStateFunc(attack.BuildSonicBoomAttack(self.playerNum))            

            if (newMove == 5 or newMove == 6):
                print ' *** ATTACK ! *** UPPERCUT ' + str(newMove)
                if (newMove == 5): 
                    self._changeStateFunc(attack.BuildLeftUppercutAttack(self.playerNum))            
                else: 
                    self._changeStateFunc(attack.BuildRightUppercutAttack(self.playerNum))            
                sounds.uppercut.play()

            if (newMove == 3 or newMove == 4): 
                print ' *** ATTACK ! *** HOOK ' + str(newMove)
                if (newMove == 3): 
                    self._changeStateFunc(attack.BuildLeftHookAttack(self.playerNum))
                else:
                    self._changeStateFunc(attack.BuildRightHookAttack(self.playerNum))
                sounds.punchFierceSound.play()
                
            if (newMove == 1 or newMove == 2):
                # standard jab punch attack
                print ' *** ATTACK ! *** JAB ' + str(newMove)
                if (newMove == 1): 
                    self._changeStateFunc(attack.BuildLeftJabAttack(self.playerNum))
                else:
                    self._changeStateFunc(attack.BuildRightJabAttack(self.playerNum))
                sounds.punchJabSound.play()

        # done.. clear the newly aquired moves
        self.moves.clear()