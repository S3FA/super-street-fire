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
        self.lock       = threading.Semaphore()
        try:
            # NOTE: timeout=x means we wait up to x seconds to read from the serial port
            self.serialInputPort = serial.Serial(inputSerialPort, baudrate=baudRate, timeout=1)
            self.serialInputPort.timeout = 1
        except serial.SerialException:
            print "ERROR: Serial port " + inputSerialPort + " was invalid/not found."
            exit(-1)    

    def ExitThread(self):
        self.lock.acquire()
        self.exitThread = True
        self.lock.release()

    def run(self):
        # Make sure this object is in a proper state before running...
        if self.serialInputPort == None:
            print "ERROR: Serial port was invalid/not found, could not run receiver."
            return
        
        # Temporary variables used in the while loop
        currSerialDataStr = ""
        print "Running receiver..."
        
        self.lock.acquire() 
        while not self.exitThread:
            self.lock.release()
            
            print "Listening on serial port"
            # Listen for input on the serial port
            currSerialDataStr = self.serialInputPort.readline()
            parser.ParseSerialData(currSerialDataStr, self.queueMgr)
            
            self.lock.acquire()
            
        self.lock.release()
        self.serialInputPort.close()
        
        
        