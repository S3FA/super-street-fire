'''
Created on 2011-04-23

'''
import logging
import time
from client_datatypes import *

X = 0
Y = 1
Z = 2
ROLL = 0
PITCH = 1
YAW = 2
PLAYER_ONE = 1
PLAYER_TWO = 2

# FWD: Forward - x direction
# LAT: Lateral - y direction
# VER: Up or Down - z direction
#using absolute values
JAB_FWDACC_L1 = 300
JAB_FWDACC_L2 = 475

HOOK_FWDACC_L1 = 300
HOOK_FWDACC_L2 = 400
HOOK_LATGYR = 220
HOOK_LATACC_L1 = 300
HOOK_LATACC_L2 = 400

BLOCK_VER_ANGLE = 70
BLOCK_RELEASE_ANGLE = 45


class SSFMoves:
    LOGGER_NAME    = 'moves_logger'
    
    def __init__(self):
        self._logger = logging.getLogger(SSFMoves.LOGGER_NAME)
    

    def getMove(self, prevMove, glove):
        # must release from block to read next move
        # keep these values spaced out to avoid "bounce"
        if (prevMove.find('BLOCK') > -1 and abs(glove.heading[PITCH]) < BLOCK_RELEASE_ANGLE):
            return "RELEASE"
        elif (abs(glove.acceleration[Z]) < 50 and abs(glove.heading[PITCH]) > BLOCK_VER_ANGLE):
            return "BLOCK" 
            
        if (abs(glove.acceleration[X]) > HOOK_FWDACC_L1 and abs(glove.rotation[X]) > HOOK_LATGYR):
            # swinging punch - lateral movement
            if (abs(glove.acceleration[X]) > HOOK_FWDACC_L2 and abs(glove.rotation[X]) > 350):
                return "HOOK2"
            return "HOOK1"

        # using the abs acceleration - punch is positive/negative during swing  
        if (abs(glove.acceleration[X]) > JAB_FWDACC_L1 and abs(glove.rotation[X]) < HOOK_LATGYR):
            # straight punch - less lateral movement
            if (abs(glove.acceleration[X]) > JAB_FWDACC_L2):
                return "JAB2"
            return "JAB1"
    
        if (glove.acceleration[Z] > 700):
            return "DRAGONPUNCH1"

    def determineMove(self, player):
        leftGlove = player.leftGlove
        rightGlove = player.rightGlove
        print '%s | %s ' % (leftGlove,rightGlove)

        if (leftGlove != None):
            leftMove = self.getMove(player.prevMove, leftGlove)
            if leftMove != None:
                player.move = "LEFT_" + leftMove
        
        if (rightGlove != None):
            rightMove = self.getMove(player.prevMove, rightGlove)
            if rightMove != None:
                player.move = "RIGHT_" + rightMove
                
        
       
class SSFPlayer():
    LOGGER_NAME    = 'player_logger'
    prevMove = "RELEASE"
    move = "RELEASE"
    playerNum = 1
    leftGlove = None
    rightGlove = None
    
    def __init__(self, playerNum):
        assert(playerNum == 1 or playerNum == 2)
        self.playerNum = playerNum
        self._logger = logging.getLogger(SSFPlayer.LOGGER_NAME)
  
    def __str__(self):
        return  "Player: " + str(self.playerNum) + ",Left: " + str(self.leftGlove) + ", Right: " + str(self.rightGlove) 

    
    def setupData(self, hand, headings, dG, avAcc):
        # Turn the parsed glove data into an actual object
        if (hand == 'L'):
            self.leftGlove = GloveData(dG, avAcc, headings, PLAYER_ONE, hand)
        else: 
            self.rightGlove = GloveData(dG, avAcc, headings, PLAYER_ONE, hand)
            
            
    def showMove(self):
        #state change - return move 
        if (self.prevMove != self.move ):
            self.prevMove = self.move
            return (self.move)

        return ""
            