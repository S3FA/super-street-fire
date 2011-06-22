'''
xbeeio.py

The receiver contains the XBeeIO class, which runs as a separate thread
in the super street fire server. This thread is responsible for
receiving data from clients via a specified serial port on this machine,
parsing that data and placing it onto the queues of its given
ReceiverQueueMgr object.

@author: Callum Hay
'''

from xbee import ZigBee # http://code.google.com/p/python-xbee/
import serial
import parser
import os
import logging
import struct
from binascii import hexlify
from time import sleep

# Since the xbee library requires a non-member function for its callbacks, we
# need to make the variables available to that function non-members as well...
receiverQueueMgr = None
parser = parser.Parser()

# Callback function for asynchronous receiving of data from the xbee library
def XBeeCallback(xbeeDataFrame):
    parser.ParseWirelessData(xbeeDataFrame, receiverQueueMgr)

class XBeeIO:
    LOGGER_NAME = 'xbee-tx'
    DISCOVERY_TIMEOUT = 5
    INPUT_PORTS = ['/dev/tty.xbee', 'COM10','COM3','COM5','COM6','COM8']

    def __init__(self, inputSerialPort, baudRate):
        self._logger = logging.getLogger(XBeeIO.LOGGER_NAME)
        self.serialIn    = None
        self.xbee        = None
        self.fireData    = None
        self.timerData   = None
        self.p1LifeData  = None
        self.p2LifeData  = None
        
        if (inputSerialPort != None): XBeeIO.INPUT_PORTS.insert(0, inputSerialPort)
        for port in XBeeIO.INPUT_PORTS:
            try:
                self.serialIn = serial.Serial( port,baudrate=57600 )
                self.xbee = ZigBee(self.serialIn, callback=XBeeCallback, escaped=True)
                break
            except serial.serialutil.SerialException:
                pass
        
        if self.xbee == None: 
            print "ERROR: Serial search failed " + str(XBeeIO.INPUT_PORTS)
            print "************ Killing XBee IO Thread ****************"
            exit(-1)    
        
        # send out node discovery -- responses will be added to ADDR_TABLE in the parser
        self.xbee.at(command='ND')
        print "Searching for wireless nodes ... wait for it."
        sleep(self.DISCOVERY_TIMEOUT)
        self._logger.info("Found Devices:" + str( parser.ADDR_TABLE ) )
        
    def _sendFire(self):
        
        fireEmitterData = self.fireData
        self._logger.debug('sending fire emitter data ' + hexlify(fireEmitterData))
         
        # Make sure this object is in a proper state before running...
        if self.xbee == None:
            print "Send Fire ERROR: Output port was invalid/not found, can not send."
            print "************ Killing XBee IO Thread ****************"
            return
        
        try:
            # Write data to the xbee->wifire interpreter
            # print "sending %s to S %s L %s" % (hexlify(fireEmitterData),hexlify(parser.ADDR_TABLE['SSFFIRE'][1]), hexlify(parser.ADDR_TABLE['SSFFIRE'][0]))
            self.xbee.send('tx', dest_addr=parser.ADDR_TABLE['SSFFIRE'][1], dest_addr_long=parser.ADDR_TABLE['SSFFIRE'][0], data=fireEmitterData)                   
        except:
            self._logger.warn("FIRE send error -- perhaps address not in ADDR_TABLE")


    def _sendTimer(self):
        timerData = self.timerData
        # Make sure this object is in a proper state before running...
        if self.xbee == None:
            print "ERROR: Output port was invalid/not found, can not send."
            print "************ Killing XBee IO Thread ****************"
            return
        
        self._logger.debug('sending timer data ' + str(timerData))
        data = struct.pack("H", timerData)
        try:
            self.xbee.send('tx', dest_addr=parser.ADDR_TABLE['SSFTIMER'][1], dest_addr_long=parser.ADDR_TABLE['SSFTIMER'][0], data=data)                   
        except:
            self._logger.warn("TIMER send error -- perhaps address not in ADDR_TABLE")

    def _sendKO(self):
        koData = self.koData
        
        # Make sure this object is in a proper state before running...
        if self.xbee == None:
            print "ERROR: Output port was invalid/not found, can not send."
            print "************ Killing XBee IO Thread ****************"
            return
        
        self._logger.debug('sending ko data ' + str(koData))
        data = struct.pack("H", koData)
        try:
            self.xbee.send('tx', dest_addr=parser.ADDR_TABLE['SSFKO'][1], dest_addr_long=parser.ADDR_TABLE['SSFKO'][0], data=data)                   
        except:
            self._logger.warn("KO send error -- perhaps address not in ADDR_TABLE")


    def _sendND(self):
        
        self._logger.debug('sending node discovery message')
        
        # Make sure this object is in a proper state before running...
        if self.xbee == None:
            print "Send ND ERROR: Output port was invalid/not found, can not send."
            print "************ Killing XBee IO Thread ****************"
            return
        
        self.xbee.at(command='ND')                  

    def SendTimerNum(self, value):    
        # based on the following digit map:
        #
        #   x0x          x8x
        # x     x      x     x
        # 5     1      d     9
        # x     x      x     x
        #   x6x          xex
        # x     x      x     x
        # 4     2      c     a
        # x     x      x     x
        #   x3x          xbx     
        if (value < 0) or (value > 99):
            return   
        digitMap = [0x3F, 0x06, 0x5B, 0x4F, 0x66, 0x6D, 0x7D, 0x07, 0x7F, 0x6F]
        timerData = (digitMap[value / 10] << 8) | digitMap[value % 10]
        self.timerData = timerData
        self._sendTimer()
    
    def SendLifeBarData(self, p1Life, p2Life): 
        p1LifeBars = p1Life / 6.25 
        if p1LifeBars == 16:
            out = 0xffff
        if p1Life == 0:
            out = 0
        else:
            out = (1<<int(p1LifeBars) + 1) - 1
        data = struct.pack(">I", out)
        try:
            if (data != self.p1LifeData):
                self.p2LifeData = data
                #self._logger.warn('p1 life send ' + hexlify(data))
                # Write data to the xbee: SSFP1LIFE destination address
                self.xbee.send('tx', dest_addr=parser.ADDR_TABLE['SSFP1LIFE'][1], dest_addr_long=parser.ADDR_TABLE['SSFP1LIFE'][0], data=data)                   
        except:
            self._logger.warn("SSFP1LIFE send error -- perhaps address not in ADDR_TABLE")
            pass
        
        p2LifeBars = p2Life / 6.25 
        if p2LifeBars == 16:
            out = 0xffff
        if p2Life == 0:
            out = 0
        else:
            out = (1<<int(p2LifeBars) + 1) - 1
        data = struct.pack(">I", out)
        try:
            if (data != self.p2LifeData):
                self.p2LifeData = data
                self._logger.warn('p2 life send ' + hexlify(data))
                # Write data to the xbee: SSFP2LIFE destination address
                self.xbee.send('tx', dest_addr=parser.ADDR_TABLE['SSFP2LIFE'][1], dest_addr_long=parser.ADDR_TABLE['SSFP2LIFE'][0], data=data)                   
        except:
            self._logger.warn("SSFP2LIFE send error -- perhaps address not in ADDR_TABLE ")
            pass
        
    def SendFireEmitterData(self, leftEmitters, rightEmitters):
        
        # assemble and send all data at once
        fire = ['0'] * 16
        p1c = ['0'] * 16
        p2c = ['0'] * 16
        
        # Fire and color control:
        #  2 bytes for fire control:   16[x x x x x x x x][x x x x x x x x]1
        #  2 bytes for player 1 color: 16[x x x x x x x x][x x x x x x x x]1
        #  2 bytes for player 2 color: 16[x x x x x x x x][x x x x x x x x]1
        
        for emitter in leftEmitters:
            #print 'left emitter: %s ' % (emitter)
            fire[emitter.arcIndex] = str(int(emitter.flameIsOn))
            
            p1c[emitter.arcIndex] =  str(int(emitter.p1ColourIsOn))
            p2c[emitter.arcIndex+8] =  str(int(emitter.p2ColourIsOn))
        
        for emitter in rightEmitters:
            fire[15-emitter.arcIndex] =  str(int(emitter.flameIsOn))
            
            p1c[15-emitter.arcIndex] =  str(int(emitter.p1ColourIsOn))
            p2c[emitter.arcIndex] =  str(int(emitter.p2ColourIsOn))
                    
        fire.reverse()
        p1c.reverse()
        p2c.reverse()
        
        fireInt = int(''.join(fire),2)
        p1cInt  = int(''.join(p1c),2)
        p2cInt  = int(''.join(p2c),2)
        
        dataset = struct.pack("HHH", fireInt, p1cInt, p2cInt)
        
        if (self.fireData != dataset):
            #self._logger.debug('fire=%s, p1c=%s, p2c=%s' % (fire, p1c, p2c) )
            self.fireData = dataset
            #print 'send wifire data=%s' % (dataset)
            self._sendFire()

    def sendKO(self,state):
        if(state):
            out = 0xff
        else:
            out = 0x00
        self.koData = struct.pack("H", out)
        self._sendKO()

    def NodeDiscovery(self):
        self._logger.info('Looking for hardware ...')
        self._sendND()
        
    def Kill(self):
        if self.xbee != None:
            self.xbee.halt()
        if self.serialIn != None:
            self.serialIn.close()
            
