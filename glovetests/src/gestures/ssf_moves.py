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

class SSFMoves:
    
    def __init__(self):
        pass
  
    def __str__(self):
        return "Left: " + str(self.leftGlove) + ", Right: " + str(self.rightGlove) 
    
    def setupData(self, hand, headings, dG, avAcc):
        # Turn the parsed glove data into an actual object
        self.leftGlove = GloveData(dG, avAcc, headings, PLAYER_ONE, hand)

    def determineMove(self, player):
        
        # can't make this two-handed yet, only one device :[

        # must release from block to read next move
        # keep these values spaced out to avoid "bounce"
        if (player.prevMove == "BLOCK" and self.leftGlove.heading[PITCH] > -60):
            player.move = "RELEASE"
        elif (abs(self.leftGlove.acceleration[Z]) < 100 and self.leftGlove.heading[PITCH] < -70):
            player.move = "BLOCK" 
        
        # using the abs acceleration - punch is positive/negative during swing  
        if (abs(self.leftGlove.acceleration[X]) > 400 and abs(self.leftGlove.acceleration[Y]) < 380):
            # straight punch - less lateral movement
            player.move = "LEFT_JAB1"
            if (abs(self.leftGlove.acceleration[X]) > 700):
                player.move = "LEFT_JAB2"
        elif (abs(self.leftGlove.acceleration[X]) > 300 and abs(self.leftGlove.acceleration[Y]) > 380):
            # swinging punch - lateral movement
            player.move = "LEFT_HOOK1"
            if (abs(self.leftGlove.acceleration[X]) > 500 and abs(self.leftGlove.acceleration[Y]) > 700):
                player.move = "LEFT_HOOK2"
                
                                
       
class SSFPlayer:
    LOGGER_NAME    = 'player_logger'
    prevMove = "RELEASE"
    move = "RELEASE"
    playerNum = 1
    
    def __init__(self, playerNum):
        assert(playerNum == 1 or playerNum == 2)
        self.playerNum = playerNum
        self._logger = logging.getLogger(SSFPlayer.LOGGER_NAME)

    def __str__(self):
        return "Player: " + str(self.playerNum) + ", Last move: " + self.move

    def showMove(self):
        #state change - return move 
        if (self.prevMove != self.move ):
            self.prevMove = self.move
            return (self.move)

        return ""
            
