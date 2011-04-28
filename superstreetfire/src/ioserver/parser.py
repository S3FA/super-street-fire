'''
parser.py

The parser contains a list of functions used to 
a) Parse data received from super street fire clients via serial port
b) Aggregate the parsed data for the appropriate client hardware (and player)
c) Build high-level objects to represent the aggreated data
d) Store the high-level objects on the receiver queues of the ReceiverQueueMgr 

@author: Callum Hay
'''

import re
import string
import struct
from client_datatypes import GloveData, HeadsetData, PLAYER_ONE, PLAYER_TWO

# look guys, no wires!
def ParseWirelessData(xbeePacket, queueMgr):
    #print xbeePacket
    # pull out the useful stuff..
    data = xbeePacket['rf_data']
    
    # try to find a device id based on source address
    source = str(struct.unpack(">q", xbeePacket['source_addr_long'])[0])
    if (SOURCE_ADDRESS_MAP.get(source,'0') == '0'):
        # which device are we? is there a "NODE:" delimiter
        nodePos = data.find(":");
        if (nodePos > 2 and data[nodePos-1:nodePos].isalpha() ):
            nodeId = data[nodePos-2:nodePos]
            SOURCE_ADDRESS_MAP[source] = nodeId
            print "found a node %s for %s " % (nodeId, source)
        else:
            print "no node identifier in rf_data." 
            return
        
    # the data packet is likely still disjoint, pass it on anyway
    func = PARSER_FUNCTION_DICT.get(SOURCE_ADDRESS_MAP[source]);
    if func != None:
        # We're dealing with an expected package type from a client, parse it
        # and queue it for the game simulation to deal with
        func(data, queueMgr)
    else:
        print "oh noes, no parser, junking packet.. " 

def ParseSerialData(serialDataStr, queueMgr):
    # Break the serial input up based on the various known headers that
    # designate our input sources...
    splitNodeData  = string.split(serialDataStr, ":")
    splitListLength = len(splitNodeData)
    #print 'Player %s - Data: %s ' % (splitNodeData[0], splitNodeData[1])

    # splitListLength should likely be 2 here, but just for robustness
    # we pretend like it could be longer
    for i in range(0, splitListLength-1):
        func = PARSER_FUNCTION_DICT.get(splitNodeData[i]);
        if func != None:
            # We're dealing with an expected package type from a client, parse it
            # and queue it for the game simulation to deal with
            func(splitNodeData[i+1], queueMgr)


def GloveParser(player, hand, bodyStr):
    
    blocks = bodyStr.split("_")
    
    # Get out of here immediately if there's a mismatch of the expected data
    # for the glove.
    if len(blocks) < 3:
        print "Unexpected format in glove parser input, no match."
        return None
    
    head = string.split(blocks[0],",")
    accel = string.split(blocks[1],",")
    gyros = string.split(blocks[2],",")

    # Turn the parsed glove data into an actual object
    gloveData = GloveData((float(gyros[0]), float(gyros[1]), float(gyros[2])), \
                          (float(accel[0]), float(accel[1]), float(accel[2])), \
                          (float(head[0]), float(head[1]), float(head[2])), \
                          player, hand)
    #print 'GloveData setup %s ' % (str(gloveData))
    return gloveData

def HeadsetParser(player, bodyStr):
    matchResult = re.match(HeadsetData.HEADSET_DATA_REGEX_STR, bodyStr)
    
    # Get out of here immediately if there's a mismatch of the expected data
    # for the head-set, this really should never happen unless the serial input
    # is being garbled somehow
    if matchResult == None:
        print "Failed match result in headset parser, no match."
        return None
    elif len(matchResult.groups()) != HeadsetData.NUM_HEADSET_DATA:
        print "Failed match result in headset parser: " + matchResult.group()
        return None
    
    # Turn the parsed head-set data into an actual object
    headsetData = HeadsetData(float(matchResult.group(1)), float(matchResult.group(2)),  \
                              float(matchResult.group(3)), float(matchResult.group(4)),  \
                              float(matchResult.group(5)), float(matchResult.group(6)),  \
                              float(matchResult.group(7)), float(matchResult.group(8)),  \
                              float(matchResult.group(9)), float(matchResult.group(10)), \
                              float(matchResult.group(11)), player)
    
    return headsetData
    

def Player1LeftGloveParser(bodyStr, queueMgr):
    print "Player 1 left glove data received."
    queueMgr.PushP1LeftGloveData(GloveParser(PLAYER_ONE, GloveData.LEFT_HAND_GLOVE, bodyStr))
    
def Player1RightGloveParser(bodyStr, queueMgr):
    print "Player 1 right glove data received."
    queueMgr.PushP1RightGloveData(GloveParser(PLAYER_ONE, GloveData.RIGHT_HAND_GLOVE, bodyStr))
        
def Player2LeftGloveParser(bodyStr, queueMgr):        
    print "Player 2 left glove data received."
    queueMgr.PushP2LeftGloveData(GloveParser(PLAYER_TWO, GloveData.LEFT_HAND_GLOVE, bodyStr))
    
def Player2RightGloveParser(bodyStr, queueMgr):    
    print "Player 2 right glove data received."
    queueMgr.PushP2RightGloveData(GloveParser(PLAYER_TWO, GloveData.RIGHT_HAND_GLOVE, bodyStr))
        
def Player1HeadsetSerialInputParser(bodyStr, queueMgr):
    print "Player 1 headset data received."
    queueMgr.PushP1HeadsetData(HeadsetParser(PLAYER_ONE, bodyStr))

def Player2HeadsetSerialInputParser(bodyStr, queueMgr):
    print "Player 2 headset data received."
    queueMgr.PushP2HeadsetData(HeadsetParser(PLAYER_TWO, bodyStr))

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

CURRENT_FRAME = {
    '1L' : '',
    '1R' : '',
    '1H' : '',
    '2L' : '',
    '2R' : '',
    '2H' : ''
}

SOURCE_ADDRESS_MAP = {
}

