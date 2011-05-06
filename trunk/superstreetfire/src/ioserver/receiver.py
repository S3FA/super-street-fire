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

# Since the xbee library requires a non-member function for its callbacks, we
# need to make the variables available to that function non-members as well...
recieverQueueMgr = None

# Callback function for asynchronous receiving of data from the xbee library
def XBeeCallback(xbeeDataFrame):
    parser.ParseWirelessData(xbeeDataFrame, recieverQueueMgr)

class Receiver:

    def __init__(self, inputSerialPort, baudRate):
        self.serialIn    = None
        self.xbee        = None
        
        try:
            self.serialIn = serial.Serial(inputSerialPort, baudrate=baudRate)
            self.xbee = ZigBee(self.serialIn, callback=XBeeCallback, escaped=True)
            
        except serial.SerialException:
            print "ERROR: Serial port " + inputSerialPort + " was invalid/not found."
            print "************ Killing Receiver Thread ****************"
            exit(-1)    
        
        print "Running receiver..."
        
    def Kill(self):
        if self.xbee != None:
            self.xbee.halt()
        if self.serialIn != None:
            self.serialIn.close()
            
