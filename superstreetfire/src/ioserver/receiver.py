'''
receiver.py

The receiver contains the Receiver class, which runs as a separate thread
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

# Since the xbee library requires a non-member function for its callbacks, we
# need to make the variables available to that function non-members as well...
recieverQueueMgr = None

# Callback function for asynchronous receiving of data from the xbee library
def XBeeCallback(xbeeDataFrame):
    parser.ParseWirelessData(xbeeDataFrame, recieverQueueMgr)

class Receiver:
    LOGGER_NAME = 'xbee-tx'

    #sample xbee address
    p1leftAddrL = '\x00\x13\xa2\x00\x40\x69\x0a\x35'
    p1leftAddrS = '\x00\x13'

    def __init__(self, inputSerialPort, baudRate):
        self._logger = logging.getLogger(Receiver.LOGGER_NAME)
        self.serialIn    = None
        self.xbee        = None
        self.visualizer  = None
        self.outputData  = None
        
        try:
            self.serialIn = serial.Serial(inputSerialPort, baudrate=baudRate)
            self.xbee = ZigBee(self.serialIn, callback=XBeeCallback, escaped=True)
            
        except serial.SerialException:
            print "ERROR: Serial port " + inputSerialPort + " was invalid/not found."
            print "************ Killing XBee IO Thread ****************"
            exit(-1)    
        
        try:
            port = '/dev/master'
            if (os.name.find("nt") > -1):
                port = "COM1" # using virtual serial port driver for windows
            self.visualizer = serial.Serial(port, baudrate=57600)
        
        except serial.SerialException:
            print 'ERROR: Serial'+port+' for visualizer was invalid/not found.'
        
    def _send(self):
        
        fireEmitterData = self.outputData
        self._logger.debug('sending fire emitter data ' + fireEmitterData)
        
        try:
            self.visualizer.write(fireEmitterData)
        except:
            pass
        
        # Make sure this object is in a proper state before running...
        if self.xbee == None:
            print "ERROR: Output port was invalid/not found, can not send."
            print "************ Killing XBee IO Thread ****************"
            return
        
        try:
            # Write data to the xbee->wifire interpreter
            self.xbee.send('tx', dest_addr=Receiver.p1leftAddrS, dest_addr_long=Receiver.p1leftAddrL, data=fireEmitterData);                   
        except TypeError:
            print 'Type error on xbee sender '
            #pass
        

        
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
            fire[emitter.arcIndex+8] =  str(int(emitter.flameIsOn))
            p1c[emitter.arcIndex+8] =  str(int(emitter.p1ColourIsOn))
            p2c[emitter.arcIndex] =  str(int(emitter.p2ColourIsOn))
                    
        #print 'fire=%s,p1c=%s,p2c=%s' % (fire, p1c, p2c)
        dataset = ''.join(fire) + ':' + ''.join(p1c) + ':' + ''.join(p2c)
        if (self.outputData != dataset):
            self.outputData = dataset
            #print 'send wifire data=%s' % (dataset)
            self._send()
        
    def Kill(self):
        if self.xbee != None:
            self.xbee.halt()
        if self.serialIn != None:
            self.serialIn.close()
            
