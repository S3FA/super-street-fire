'''
client_datatypes.py

This file contains the basic data types that hold the various
expected inputs from the Super Street Fire game sensors

@author: Callum Hay
'''

# The GloveData class defines the data type the holds
# the various inputs expected from the 9-DOF glove sensor
# on each hand of the two players of the game.
class GloveData:
    NUM_GLOVE_DATA       = 9
    GLOVE_DATA_REGEX_STR = ""
    for i in range(NUM_GLOVE_DATA-1): GLOVE_DATA_REGEX_STR = GLOVE_DATA_REGEX_STR + '(-?\d+\.\d+),'
    GLOVE_DATA_REGEX_STR = GLOVE_DATA_REGEX_STR + '(-?\d+\.\d+)'
    
    def __init__(self, rX = 0, rY = 0, rZ = 0,
                       aX = 0, aY = 0, aZ = 0, 
                       hX = 0, hY = 0, hZ = 0):
        
        # Gyro data - rotation about the x, y and z axis
        self.rotation = (rX, rY, rZ)
        # Acceleration data along the x, y and z axis
        self.acceleration = (aX, aY, aZ)
        # Heading data - the directional vector,
        # not necessarily normalized
        self.heading = (hX, hY, hZ)
        
    def __str__(self):
        return "Rotation: " + str(self.rotation) + ", Acceleration: " + str(self.acceleration) + ", Heading: " + str(self.heading)
    
# Class for representing the head-set (EEG) data
class HeadsetData:
    NUM_HEADSET_DATA       = 11
    HEADSET_DATA_REGEX_STR = ""
    for i in range(NUM_HEADSET_DATA-1): HEADSET_DATA_REGEX_STR = HEADSET_DATA_REGEX_STR + '(-?\d+\.\d+),'
    HEADSET_DATA_REGEX_STR = HEADSET_DATA_REGEX_STR + '(-?\d+\.\d+)'
    
    def __init__(self):
        self.link       = 0
        self.attention  = 0
        self.meditation = 0
        self.band1      = 0
        self.band2      = 0
        self.band3      = 0
        self.band4      = 0
        self.band5      = 0
        self.band6      = 0
        self.band7      = 0
        self.band8      = 0
 
    def __str__(self):
        result = "Link: " + str(self.link) + ", Attention: " + str(self.attention) + ", Mediation: " + str(self.meditation)
        result = result + ", Bands: (" + str(self.band1) + ", " + str(self.band2) + ", " + str(self.band3)
        result = result + ", " + str(self.band4) + ", " + str(self.band5) + ", " + str(self.band6)
        result = result + ", " + str(self.band7) + ", " + str(self.band8) + ")"
        return result
    
    
    
    
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
    
    
    
    
    
    
    
    
    
    
    