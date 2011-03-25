'''
client_datatypes.py

This file contains the basic data types that hold the various
expected inputs from the Super Street Fire game sensors

@author: Callum Hay
'''

PLAYER_ONE = 1
PLAYER_TWO = 2

# The GloveData class defines the data type the holds
# the various inputs expected from the 9-DOF glove sensor
# on each hand of the two players of the game.
class GloveData:
    # Static data
    NUM_GLOVE_DATA       = 9
    GLOVE_DATA_REGEX_STR = ""
    for i in range(NUM_GLOVE_DATA-1): GLOVE_DATA_REGEX_STR = GLOVE_DATA_REGEX_STR + '(-?\d+\.\d+),'
    GLOVE_DATA_REGEX_STR = GLOVE_DATA_REGEX_STR + '(-?\d+\.\d+)'
    
    # Enumeration for left and right hand gloves
    LEFT_HAND_GLOVE = 0
    RIGHT_HAND_GLOVE = 1
    
    def __init__(self, rX = 0, rY = 0, rZ = 0,
                       aX = 0, aY = 0, aZ = 0,
                       hX = 0, hY = 0, hZ = 0, 
                       player = -1, hand = -1):
        
        # Gyro/rotational data - rotation about the x, y and z axis
        self.rotation = (rX, rY, rZ)
        # Acceleration data along the x, y and z axis
        self.acceleration = (aX, aY, aZ)
        # Heading data - the directional vector,
        # not necessarily normalized
        self.heading = (hX, hY, hZ)
        
        # Store which player this glove data belongs to
        assert(player == PLAYER_ONE or player == PLAYER_TWO)
        self.player = player
        
        # Store which hand the glove is for...
        assert(hand == GloveData.LEFT_HAND_GLOVE or hand == GloveData.RIGHT_HAND_GLOVE)
        self.hand = hand
        
    def __str__(self):
        return "Rotation: " + str(self.rotation) + ", Acceleration: " + str(self.acceleration) + ", Heading: " + str(self.heading)
    
# Class for representing the head-set (EEG) data
class HeadsetData:
    NUM_HEADSET_DATA       = 11
    HEADSET_DATA_REGEX_STR = ""
    for i in range(NUM_HEADSET_DATA-1): HEADSET_DATA_REGEX_STR = HEADSET_DATA_REGEX_STR + '(-?\d+\.\d+),'
    HEADSET_DATA_REGEX_STR = HEADSET_DATA_REGEX_STR + '(-?\d+\.\d+)'
    
    def __init__(self, link, atten, med,
                 b1, b2, b3, b4, b5, b6, b7, b8, player):
        self.link       = link
        self.attention  = atten
        self.meditation = med
        self.band1      = b1
        self.band2      = b2
        self.band3      = b3
        self.band4      = b4
        self.band5      = b5
        self.band6      = b6
        self.band7      = b7
        self.band8      = b8
        
        # Store which player this head-set data belongs to
        assert(player == PLAYER_ONE or player == PLAYER_TWO)
        self.player = player
 
    def __str__(self):
        result = "Link: " + str(self.link) + ", Attention: " + str(self.attention) + ", Mediation: " + str(self.meditation)
        result = result + ", Bands: (" + str(self.band1) + ", " + str(self.band2) + ", " + str(self.band3)
        result = result + ", " + str(self.band4) + ", " + str(self.band5) + ", " + str(self.band6)
        result = result + ", " + str(self.band7) + ", " + str(self.band8) + ")"
        return result
    
    
# I DOUBT WE'LL ACTUALLY USE THE CLASSES BELOW SINCE WE'LL BE REACTING TO DATA    
# AS IT COMES IN OVER THE PORT
    
# High level encapsulation of a single player's input
# data - holds all the incoming data from both gloves
# and their headset
class PlayerData:
    def __init__(self):
        self.leftGloveData  = GloveData()
        self.rightGloveData = GloveData()
        self.headsetData    = HeadsetData()
    

# The Highest level representation of the input for the entire
# Super Street Fire game - contains all input from all
# relevant player sensors for a given capture of all
# received packets at some instance during game play.
class GameData:
    def __init__(self):
        self.player1Data = PlayerData()
        self.player2Data = PlayerData()
        pass
    
    
    
    
    
    
    
    
    
    
    