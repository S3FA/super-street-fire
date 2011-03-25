
import serial
import parser
import threading

from receiver_queue_mgr import ReceiverQueueMgr

class Receiver(threading.Thread):

    def __init__(self, inputSerialPort, baudRate):
        threading.Thread.__init__(self)
        self.queueMgr   = ReceiverQueueMgr()
        self.serial     = None
        self.exitThread = False
        try:
            # NOTE: timeout=x means we wait up to x seconds to read from the serial port
            self.serialInputPort = serial.Serial(inputSerialPort, baudrate=baudRate, timeout=1)
        except serial.SerialException:
            print "ERROR: Serial port " + inputSerialPort + " was invalid/not found."
            exit(-1)
    
    def __del__(self):
        if self.serialInputPort != None:
            self.serialInputPort.close()
            

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