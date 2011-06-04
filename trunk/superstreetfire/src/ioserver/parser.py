'''
parser.py

The parser contains a list of functions used to 
a) Parse data received from super street fire clients via serial port
b) Aggregate the parsed data for the appropriate client hardware (and player)
c) Build high-level objects to represent the aggreated data
d) Store the high-level objects on the receiver queues of the ReceiverQueueMgr 

@author: Callum Hay
'''
import logging
import re
import sys
import string
import struct
from collections import deque
from client_datatypes import GloveData, HeadsetData, PLAYER_ONE, PLAYER_TWO
from binascii import hexlify

log = logging.getLogger('parser')
    
# look guys, no wires!
def ParseWirelessData(xbeePacket, queueMgr):
    #print xbeePacket 
    
    if xbeePacket['id'] == 'at_response':
        UpdateAddrTable(xbeePacket)
        return
     
    if (xbeePacket.has_key('rf_data') == False): return  
    #each frame starts with Node ID colon, ends with pipe.. e.g. 1L:x,x,x..|
    rfdata = xbeePacket['rf_data'].replace(' ','')
    print 'rfdata:%s' % (rfdata)
    
    # try to find a device id based on source address
    source = str(struct.unpack(">q", xbeePacket['source_addr_long'])[0])
    nodeId = SOURCE_ADDRESS_MAP.get(source,'0')
    frameData = deque()

    startDataPos = rfdata.find(":")
    # if we're just starting up, read each of the device ids into a map
    if (nodeId == '0'):
        # which device are we? is there a "[digit][char]:"
        if (startDataPos >= 2 and ( rfdata[startDataPos-1:startDataPos].isalpha() or
                                   rfdata[startDataPos-2:startDataPos-1].isalpha() ) ):
            nodeId = rfdata[startDataPos-2:startDataPos]
            if (nodeId == 'ts'):
                log.warn( 'Invalid ts node in rfdata ' )
                return
            SOURCE_ADDRESS_MAP[source] = nodeId
            log.info( 'found a node %s for %s ' % (nodeId, source) )
        else:
            log.warn( 'No node in rfdata ' )
            return
    
    # when we have a full frame, pass it on
    func = PARSER_FUNCTION_DICT.get(nodeId);
    restartFrame = (startDataPos == 2)  
    
    if (nodeId.find('H') > -1):
        func(rfdata[3], queueMgr)
        return        
        
    fullFrame = rfdata[-1] == '|'
    if (restartFrame and fullFrame):
        log.debug( "rfdata:" + rfdata[3:-1] )
        func(rfdata[3:-1], queueMgr)
        return
        
    # the data packet may be disjoint, test it sequentially  
    frameData.append( HOLDING_FRAME[nodeId] )
    for c in rfdata:
        if (restartFrame and c != ":"):
            continue
        elif (restartFrame and c == ":"):
            restartFrame = False
            continue
        frameData[-1] += c
        if (c == "|"):
            if (string.count(frameData[-1], ',') >= 8):
                # data is a bit weird.. check for node headers
                badNode = frameData[-1].find(':')
                if (badNode > -1):
                    frameData[-1] = frameData[-1][badNode+1:]
                #print 'popping frame:%s' % (frameData)
                func(frameData.pop(), queueMgr)
            frameData.append('')
            restartFrame = True
            
    badNode = frameData[-1].find(':')
    if (badNode > -1):
        HOLDING_FRAME[nodeId] = frameData[-1][badNode+1:]
    else:
        HOLDING_FRAME[nodeId] = frameData.pop()   
    

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
    
    blocks = bodyStr.replace('|','').split("_")
    #print 'parsing glove player blocks %s ' % (blocks)
    
    # Get out of here immediately if there's a mismatch of the expected data
    # for the glove.
    if len(blocks) < 3:
        log.error( "Unexpected format in glove parser input, no match." )
        #print "Failed block split"
        return None
    
    head = string.split(blocks[0],",")
    gyros = string.split(blocks[1],",")
    accel = string.split(blocks[2],",")

    try:
        headF = (float(head[0]), float(head[1]), float(head[2]))
        accF = (float(accel[0]), float(accel[1]), float(accel[2]))
        gryoF = (float(gyros[0]), float(gyros[1]), float(gyros[2]))    
        # Turn the parsed glove data into an actual object
        gloveData = GloveData(gryoF, accF, headF, player, hand)
        
        #print 'GloveData setup %s ' % (str(gloveData))
        return gloveData
    except: 
        log.error( "Glove data error " + str(blocks) )

    #print "Failed glove parse"
    return None

def HeadsetParser(player, bodyStr):
    data = string.split(bodyStr,",")
    try:
        # Turn the parsed glove data into an actual object
        headsetData = HeadsetData(float(data[0]), float(data[1]), float(data[2]), player)
        
        #print 'HeadsetData setup %s ' % (str(headsetData))
        return headsetData
    except: 
        log.error( "Headset data error " + str(data) )

    #print "Failed glove parse"
    return None
    

def Player1LeftGloveParser(bodyStr, queueMgr):
    gloveData = GloveParser(PLAYER_ONE, GloveData.LEFT_HAND_GLOVE, bodyStr)
    if gloveData != None:
        #log.debug( "Player 1 left glove data received." )
        queueMgr.PushP1LeftGloveData(gloveData)
    else:
        log.warn( "Player 1 left glove BAD data received." )
    
def Player1RightGloveParser(bodyStr, queueMgr):
    gloveData = GloveParser(PLAYER_ONE, GloveData.RIGHT_HAND_GLOVE, bodyStr)
    if gloveData != None:
        #log.debug( "Player 1 right glove data received." )
        queueMgr.PushP1RightGloveData(gloveData)
    else:
        log.warn( "Player 1 right glove BAD data received." )
        
def Player2LeftGloveParser(bodyStr, queueMgr):        
    gloveData = GloveParser(PLAYER_TWO, GloveData.LEFT_HAND_GLOVE, bodyStr)
    if gloveData != None:
        #log.debug( "Player 2 left glove data received." )
        queueMgr.PushP2LeftGloveData(gloveData)
    
def Player2RightGloveParser(bodyStr, queueMgr):    
    gloveData = GloveParser(PLAYER_TWO, GloveData.RIGHT_HAND_GLOVE, bodyStr)
    if gloveData != None:
        #log.debug( "Player 2 right glove data received." )
        queueMgr.PushP2RightGloveData(gloveData)
        
def Player1HeadsetSerialInputParser(bodyStr, queueMgr):
    print "Player 1 headset data received."
    headsetData = HeadsetParser(PLAYER_ONE, bodyStr)
    if headsetData != None:
        queueMgr.PushP1HeadsetData(headsetData)

def Player2HeadsetSerialInputParser(bodyStr, queueMgr):
    print "Player 2 headset data received."
    headsetData = HeadsetParser(PLAYER_TWO, bodyStr)
    if headsetData != None:
        queueMgr.PushP2HeadsetData(headsetData)

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

# holds the current device line data, might be split over multiple reads.
HOLDING_FRAME = {
    '1L' : '',
    '1R' : '',
    'H1' : '',
    '2L' : '',
    '2R' : '',
    '2H' : ''
}

# looks for nodes within the output data, and assigns a mapping 
# device source address to player device.
# TODO: we can probably get rid of this and use reverse lookups on ADDR_TABLE now
SOURCE_ADDRESS_MAP = {
}

# Address table for all XBee devices on the network
# Names are set on the XBee radios themselves, this is a complete list of devices
# This gets populated by UpdateAddrTable() whenever a ND reply is seen
ADDR_TABLE = {
    'SSFP1L'    : ['',''],
    'SSFP1R'    : ['',''],
    'SSFP1H'    : ['',''],
    'SSFP2L'    : ['',''],
    'SSFP2R'    : ['',''],
    'SSFP2H'    : ['',''],
    'SSFFIRE'   : ['',''],
    'SSFTIMER'  : ['',''],
    'SSFP1LIFE' : ['',''],
    'SSFP2LIFE' : ['',''],
    'SSFLIGHTS' : ['',''],
}

def GetAddrL(device):
    return ADDR_TABLE[device][0]

def GetAddrS(device):
    return ADDR_TABLE[device][1]

def UpdateAddrTable(response):
    if response['id'] == 'at_response':
        if response['command'] == 'ND':
            parameter = response['parameter']
            destaddrS = parameter[0:2]
            destaddr  = parameter[2:10]
            destname  = parameter[10:]
            index = destname.find('\x00')
            destname = destname[:index]
            ADDR_TABLE[destname] = [destaddr, destaddrS]
            log.debug("received node reply for node %s short %s long %s" \
                % (destname, hexlify(destaddrS), hexlify(destaddr)))
            return 1
    return 0
