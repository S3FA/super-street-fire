'''
receiver.py

The receiver contains the Receiver class, which runs as a separate thread
in the super street fire server. This thread is responsible for
receiving data from clients via a specified serial port on this machine,
parsing that data and placing it onto the queues of its given
ReceiverQueueMgr object.

@author: Callum Hay
'''

import serial
import parser
import threading

class Receiver(threading.Thread):

    def __init__(self, receiverQueueMgr, inputSerialPort, baudRate):
        threading.Thread.__init__(self)
        
        assert(receiverQueueMgr != None)
        self.queueMgr   = receiverQueueMgr
        self.serial     = None
        self.exitThread = False
        
        try:
            # NOTE: timeout=x means we wait up to x seconds to read from the serial port
            self.serialInputPort = serial.Serial(inputSerialPort, baudrate=baudRate, timeout=1)
        except serial.SerialException:
            print "ERROR: Serial port " + inputSerialPort + " was invalid/not found."
            exit(-1)    

    def run(self):
        # Make sure this object is in a proper state before running...
        if self.serialInputPort == None:
            print "ERROR: Serial port was invalid/not found, could not run receiver."
            return
        
        # Temporary variables used in the while loop
        currSerialDataStr = ""

        while not self.exitThread:
            # Listen for input on the serial port
            currSerialDataStr = self.serialInputPort.readline()
            parser.ParseSerialData(currSerialDataStr, self.queueMgr)

        self.serialInputPort.close()