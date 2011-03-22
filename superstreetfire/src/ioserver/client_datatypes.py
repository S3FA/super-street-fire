# client_datatypes.py
# This file contains the basic data types that hold the various
# expected inputs from the Super Street Fire game sensors


# The GloveData class defines the data type the holds
# the various inputs expected from the 9-DOF glove sensor
# on each hand of the two players of the game.
class GloveData:
    def __init__(self, aX = 0, aY = 0, aZ = 0, 
                       rX = 0, rY = 0, rZ = 0,
                       hX = 0, hY = 0, hZ = 0):
        # Acceleration data along the x, y and z axis
        self.accelX = aX
        self.accelY = aY
        self.accelZ = aZ
        # Gyro data - rotation about the x, y and z axis
        self.rotX = rX
        self.rotY = rY
        self.rotZ = rZ
        # Heading data - the directional vector,
        # not necessarily normalized
        self.headingX = hX
        self.headingY = hY
        self.headingZ = hZ
    
# Class for representing the head-set (EEG) data
class HeadsetData:
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
    
    
    
    
    
    
    
    
    
    
    