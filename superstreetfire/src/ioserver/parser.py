
import re
import string
from client_datatypes import GloveData, HeadsetData

def ParseSerialData(serialDataStr):
    # Break the serial input up based on the various known headers that
    # designate our input sources...
    splitInputList  = string.split(serialDataStr, ":")
    splitListLength = len(splitInputList)

    # splitListLength should likely be 2 here, but just for robustness
    # we pretend like it could be longer
    for i in range(0, splitListLength-1):
        func = PARSER_FUNCTION_DICT.get(splitInputList[i]);
        if func != None:
            # We're dealing with an expected package type from a client, parse it
            # and queue it for the game simulation to deal with
            func(splitInputList[i+1])


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

# We try to make parsing as fast as possible by using a hash table with 1H,1L,1R,etc.
# for keys and parse function references for values
PARSER_FUNCTION_DICT = {
    '1L' : Player1LeftGloveParser,
    '1R' : Player1RightGloveParser,
    '1H' : Player1HeadsetSerialInputParser,
    '2L' : Player2LeftGloveParser,
    '2R' : Player2RightGloveParser,
    '2H' : Player2HeadsetSerialInputParser
}

