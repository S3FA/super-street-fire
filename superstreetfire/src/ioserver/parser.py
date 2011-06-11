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
import string
import struct
from collections import deque
from client_datatypes import GloveData, HeadsetData, PLAYER_ONE, PLAYER_TWO
from binascii import hexlify
from gamemodel.game_model_listener import GameModelListenerCmdr

log = logging.getLogger('parser')

# responsible for reading in the xbee node discovery and device data.
class Parser: 
    
    def __init__(self):
        self._logger = logging.getLogger('wireless_parser')
        self._listenerCmdr = GameModelListenerCmdr()

    def ParseWirelessData(self, xbeePacket, queueMgr):
        #print xbeePacket 
        if xbeePacket['id'] == 'at_response':
            self.updateAddrTable(xbeePacket)
            return
        
        # try to find a device id based on source address
        nodeId = self.getDevice(xbeePacket['source_addr_long'])
        # if we're just starting up, read each of the device ids into a map
        if (nodeId == 0):
            source = str(struct.unpack(">q", xbeePacket['source_addr_long'])[0])
            log.debug( 'No node in address table for ' + source )
            return

        # good to go, look for the device data:
        if (xbeePacket.has_key('rf_data') == False): return  
    
        rfdata = xbeePacket['rf_data'].replace(' ','')
        #print 'rfdata:%s' % (rfdata)
        
        
        # when we have a full frame, pass it on
        func = PARSER_FUNCTION_DICT.get(nodeId);
        
        log.debug( nodeId + ' data: ' + rfdata)
        
        # headset data is differently framed .. assume its a full frame
        # this is not always true.. 
        if (nodeId.find('H') > -1):
            if (rfdata.find(':') > 2):
                func(rfdata[rfdata.find(':')+1:], queueMgr)
            return        
            
        # IMU data is sometimes split over multiple reads
        frameData = deque()
        startDataPos = rfdata.find(":")
        restartFrame = (startDataPos == 2)  
        fullFrame = rfdata[-1] == '|'
        if (restartFrame and fullFrame):
            # log.debug( "rfdata:" + rfdata[3:-1] )
            func(rfdata[3:-1], queueMgr)
            return
            
        # the data packet may be disjoint, test it sequentially  
        frameData.append( Parser.HOLDING_FRAME[nodeId] )
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
            Parser.HOLDING_FRAME[nodeId] = frameData[-1][badNode+1:]
        else:
            Parser.HOLDING_FRAME[nodeId] = frameData.pop()

    def getAddrL(self, device):
        return Parser.ADDR_TABLE[device][0]
    
    def getAddrS(self, device):
        return Parser.ADDR_TABLE[device][1]
    
    def getDevice(self, addrL):
        try:
            for k, v in Parser.ADDR_TABLE.iteritems():
                if (v[0] == addrL):
                    return k
        except:
            pass
        return 0
    
    def updateAddrTable(self, response):
        if response['id'] == 'at_response':
            if response['command'] == 'ND':
                parameter = response['parameter']
                destaddrS = parameter[0:2]
                destaddr  = parameter[2:10]
                destname  = parameter[10:]
                index = destname.find('\x00')
                destname = destname[:index]
                Parser.ADDR_TABLE[destname] = [destaddr, destaddrS]
                log.debug("received node reply for node %s short %s long %s" \
                    % (destname, hexlify(destaddrS), hexlify(destaddr)))
                self.isAddressTableSetup = True
                self._listenerCmdr.HWAddrChanged(Parser.ADDR_TABLE)
                return 1
            
        return 0

    
    # holds the current device line data, might be split over multiple reads.
    HOLDING_FRAME = {
        'SSFP1L' : '',
        'SSFP1R' : '',
        'SSFP2L' : '',
        'SSFP2R' : '',
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

    # gloves on the wrong hand fix :)
    #if (hand == '0'): hand = '1'
    #if (hand == '1'): hand = '0'

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
        linkQuality = float(data[0])
        if (linkQuality > 0):
            log.debug( 'Headset has no link for player ' + str(player))
            return None
        
        # Turn the parsed glove data into an actual object
        headsetData = HeadsetData(linkQuality, float(data[1]), float(data[2]), player)
        
        log.debug( 'HeadsetData setup %s ' + str(headsetData))
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
    #print "Player 1 headset data received."
    headsetData = HeadsetParser(PLAYER_ONE, bodyStr)
    if headsetData != None:
        queueMgr.PushP1HeadsetData(headsetData)

def Player2HeadsetSerialInputParser(bodyStr, queueMgr):
    #print "Player 2 headset data received."
    headsetData = HeadsetParser(PLAYER_TWO, bodyStr)
    if headsetData != None:
        queueMgr.PushP2HeadsetData(headsetData)


# We try to make parsing as fast as possible by using a hash table with 1H,1L,1R,etc.
# for keys and parse function references for values
PARSER_FUNCTION_DICT = {
    'SSFP1L' : Player1LeftGloveParser,
    'SSFP1R' : Player1RightGloveParser,
    'SSFP1H' : Player1HeadsetSerialInputParser,
    'SSFP2L' : Player2LeftGloveParser,
    'SSFP2R' : Player2RightGloveParser,
    'SSFP2H' : Player2HeadsetSerialInputParser
}