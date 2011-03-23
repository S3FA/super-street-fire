'''
test.py

This file contains various functions used for testing the functionality
of the superstreetfire ioserver.
Run this as the main script in order to use it as a sandbox for prototyping
and testing parts of the system that are under development.

@author: Callum Hay
'''

import random
import re
from client_datatypes import *

def GenerateRandomDeg():
    return (2 * random.random() - 1) * 180.0
def GenerateRandomAcc():
    return (2 * random.random() - 1) * 500.0
def GenerateRandomMag():
    return (2 * random.random() - 1) * 500.0

def GenerateRandomXYZDeg():
    return [GenerateRandomDeg(), GenerateRandomDeg(), GenerateRandomDeg()]
def GenerateRandomXYZAcc():
    return [GenerateRandomAcc(), GenerateRandomAcc(), GenerateRandomAcc()]
def GenerateRandomXYZMag():
    return [GenerateRandomMag(), GenerateRandomMag(), GenerateRandomMag()]

def GenerateSerialInput(num = None):
    finalString = ""
    
    randomCount = random.randrange(1, 11)
    if num != None:
        randomCount = num
    
    while randomCount > 0:
        randomNum = random.randrange(0, 7)
        
        randomRotXYZ = GenerateRandomXYZDeg()
        randomAccXYZ = GenerateRandomXYZAcc()
        randomMagXYZ = GenerateRandomXYZMag()
        gloveStr = randomRotXYZ + randomAccXYZ + randomMagXYZ
        
        elevenRandomVals = [random.random() for i in range(11)] 
        
        if randomNum == 0:
            finalString = finalString + "1L:" + ",".join(map(str, gloveStr))
        elif randomNum == 1:
            finalString = finalString + "1R:" + ",".join(map(str, gloveStr))
        elif randomNum == 2:
            finalString = finalString + "2L:" + ",".join(map(str, gloveStr))
        elif randomNum == 3:
            finalString = finalString + "2R:" + ",".join(map(str, gloveStr))
        elif randomNum == 4:
            finalString = finalString + "1H:" + ",".join(map(str, elevenRandomVals)) 
        elif randomNum == 5:
            finalString = finalString + "2H:" + ",".join(map(str, elevenRandomVals)) 
        
        randomCount -= 1
        
    return finalString

def GloveParser(bodyStr):
    matchResult = re.match(GloveData.GLOVE_DATA_REGEX_STR, bodyStr)
    
    # Get out of here immediately if there's a mismatch of the expected data
    # for the glove, this really should never happen unless the serial input
    # is being garbled somehow
    if matchResult == None:
        return
    elif len(matchResult.groups()) != GloveData.NUM_GLOVE_DATA:
        return
    
    # Turn the parsed glove data into an actual object
    gloveData = GloveData(float(matchResult.group(1)), float(matchResult.group(2)), float(matchResult.group(3)), \
                          float(matchResult.group(4)), float(matchResult.group(5)), float(matchResult.group(6)), \
                          float(matchResult.group(7)), float(matchResult.group(8)), float(matchResult.group(9)))
    return gloveData

def HeadsetParser(bodyStr):
    matchResult = re.match(HeadsetData.HEADSET_DATA_REGEX_STR, bodyStr)
    
    # Get out of here immediately if there's a mismatch of the expected data
    # for the head-set, this really should never happen unless the serial input
    # is being garbled somehow
    if matchResult == None:
        return
    elif len(matchResult.groups()) != HeadsetData.NUM_HEADSET_DATA:
        return
    
    # Turn the parsed head-set data into an actual object
    headsetData = HeadsetData(float(matchResult.group(1)), float(matchResult.group(2)),  \
                              float(matchResult.group(3)), float(matchResult.group(4)),  \
                              float(matchResult.group(5)), float(matchResult.group(6)),  \
                              float(matchResult.group(7)), float(matchResult.group(8)),  \
                              float(matchResult.group(9)), float(matchResult.group(10)), \
                              float(matchResult.group(11)))
    
    return headsetData
    

def Player1LeftGloveParser(bodyStr):
    print "Player 1 Left Glove: " + bodyStr
    print GloveParser(bodyStr)
    
def Player1RightGloveParser(bodyStr):
    print "Player 1 Right Glove: " + bodyStr
    print GloveParser(bodyStr)
        
def Player2LeftGloveParser(bodyStr):        
    print "Player 2 Left Glove: " + bodyStr
    print GloveParser(bodyStr)
    
def Player2RightGloveParser(bodyStr):    
    print "Player 2 Right Glove: " + bodyStr
    print GloveParser(bodyStr)
        
def Player1HeadsetSerialInputParser(bodyStr):
    print "Player 1 Headset: " + bodyStr
    print HeadsetParser(bodyStr)

def Player2HeadsetSerialInputParser(bodyStr):
    print "Player 2 Headset: " + bodyStr
    print HeadsetParser(bodyStr)

if __name__ == '__main__':
    
    # THIS IS A TESTING GROUND FOR PARSING SERIAL INPUT FROM SENSORS *********
    genInputStr = GenerateSerialInput()
    print genInputStr
    print ''
    
    # We try to make parsing as fast as possible by using a hash table with 1H,1L,1R,etc.
    # for keys and parse function references for values
    parserFuncDict = {
        '1L' : Player1LeftGloveParser,
        '1R' : Player1RightGloveParser,
        '1H' : Player1HeadsetSerialInputParser,
        '2L' : Player2LeftGloveParser,
        '2R' : Player2RightGloveParser,
        '2H' : Player2HeadsetSerialInputParser
    }
    
    # Break the serial input up based on the various known headers that
    # designate our input sources
    splitInputList  = re.split('(1L|1R|2L|2R|1H|2H):', genInputStr)
    splitListLength = len(splitInputList);
    
    for i in range(0, splitListLength):
        # Try to find the corresponding parser for the data in the list
        func = parserFuncDict.get(splitInputList[i]);
        if func == None or i == splitListLength-1:
            continue
        else:
            # We're dealing with proper, expected data from the client, parse it...
            func(splitInputList[i+1])
            
#    from client_datatypes import GloveData, HeadsetData, PlayerData, GameData
#    g = GloveData()
#    h = HeadsetData()
#    p = PlayerData()
#    q = GameData()
#    
#    print g.accelX

