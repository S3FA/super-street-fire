

import random
import re

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

def SerialParser(serialInput):
    #(1H|2H|1L|1R|2L|2R):
    pass



if __name__ == '__main__':
    
    
    
    # THIS IS A TESTING GROUND FOR PARSING SERIAL INPUT FROM SENSORS *********
    genInputStr = GenerateSerialInput()
    
    # TODO: Make this faster by using a hash table with 1H,1L,1R,etc.
    # keys and function reference values
    
    # Attempt to match a 9-DOF glove's serial input first 
    gloveMatchStr = '(1L|1R|2L|2R):'
    for i in range(7): gloveMatchStr = gloveMatchStr + '(-?\d+\.\d+),'
    gloveMatchStr = gloveMatchStr + '(-?\d+\.\d+)'
    
    regExMatch = re.match(gloveMatchStr, genInputStr)
    
    # If we failed to find the glove's input then try to match the EEG headset's input
    if regExMatch == None:
        headMatchStr = '(1H|2H):'
        for i in range(10): headMatchStr = headMatchStr + '(-?\d+\.\d+),'
        headMatchStr = headMatchStr + '(-?\d+\.\d+)'
        
        regExMatch = re.match(headMatchStr, genInputStr)
        
        # 1st or 2nd player head-set?
        if regExMatch.group(1) == '1H':
            print 'Player 1 headset data received.'
            # TODO
        else:
            print 'Player 1 headset data received.'
            # TODO
    else:
        # We're dealing with glove data right now
        if (regExMatch.group(1) == )

    print regExMatch.group()

#    from client_datatypes import GloveData, HeadsetData, PlayerData, GameData
#    g = GloveData()
#    h = HeadsetData()
#    p = PlayerData()
#    q = GameData()
#    
#    print g.accelX
    
    
