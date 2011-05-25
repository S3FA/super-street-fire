'''
client_datatypes.py

This file contains the basic data types that hold the various
expected inputs from the Super Street Fire game sensors

@author: Callum Hay
'''

import operator

PLAYER_ONE = 1
PLAYER_TWO = 2

# The GloveData class defines the data type the holds
# the various inputs expected from the 9-DOF glove sensor
# on each hand of the two players of the game.
class GloveData:
    # Static data
    NUM_GLOVE_DATA       = 9
    
    # Enumeration for left and right hand gloves
    LEFT_HAND_GLOVE = 0
    RIGHT_HAND_GLOVE = 1
    
    def __init__(self, rotation = (0,0,0), 
                 acceleration = (0,0,0), heading = (0,0,0), 
                 player = -1, hand = -1):
        
        # Gyro/rotational data - rotation about the x, y and z axis
        self.rotation = rotation
        # Acceleration data along the x, y and z axis
        self.acceleration = acceleration
        # Heading data - the directional vector,
        # not necessarily normalized
        self.heading = heading
        
        # Store which player this glove data belongs to
        assert(player == PLAYER_ONE or player == PLAYER_TWO)
        self.player = player
        
        # Store which hand the glove is for...
        assert(hand == GloveData.LEFT_HAND_GLOVE or hand == GloveData.RIGHT_HAND_GLOVE)
        self.hand = hand
        
    def __str__(self):
        return "A: " + str(self.acceleration) + ", H: " + str(self.heading) + ",R: " + str(self.rotation)
    
    # delta/sub operator - GloveData must be from same player and hand
    def __sub__(self, other):
        assert(self.player == other.player)
        assert(self.hand == other.hand)
        # don't use delta for heading - it doesn't make sense
        dR = tuple(map(operator.sub, self.rotation, other.rotation))
        dA = tuple(map(operator.sub, self.acceleration, other.acceleration))
        return GloveData(dR, dA, self.heading, self.player, self.hand) 
    
    # add operator - A GloveData may only be added to another GloveData with the 
    # same player and hand
    def __add__(self, other):
        assert(self.player == other.player)
        assert(self.hand == other.hand)
        
        rotSum     = tuple(map(operator.add, self.rotation, other.rotation))
        accelSum   = tuple(map(operator.add, self.acceleration, other.acceleration))
        headingSum = tuple(map(operator.add, self.heading, other.heading))
        return GloveData(rotSum, accelSum, headingSum, self.player, self.hand) 
    
    # divide operator - A GloveData may only be divided by a scalar (int or float) value
    def __div__(self, other):
        rotDiv     = tuple(map(operator.div, self.rotation, (other, other, other)))
        accelDiv   = tuple(map(operator.floordiv, self.acceleration, (other, other, other)))
        headingDiv = tuple(map(operator.div, self.heading, (other, other, other)))
        return GloveData(rotDiv, accelDiv, headingDiv, self.player, self.hand)
        
    
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
    
    def __add__(self, other):
        assert(self.player == other.player)
        return HeadsetData(self.link + other.link, self.attention + other.attention, \
                           self.meditation + other.meditation, self.band1 + other.band1, \
                           self.band2 + other.band2, self.band3 + other.band3, \
                           self.band4 + other.band4, self.band5 + other.band5, \
                           self.band6 + other.band6, self.band7 + other.band7, \
                           self.band8 + other.band8, self.player)
    
    def __div__(self, other):
        return HeadsetData(self.link / other, self.attention / other, self.meditation / other, \
                           self.band1 / other, self.band2 / other, self.band3 / other, \
                           self.band4 / other, self.band5 / other, self.band6 / other, \
                           self.band7 / other, self.band8 / other, self.player)        
    

    
    
    
    
    
    
    
    
    