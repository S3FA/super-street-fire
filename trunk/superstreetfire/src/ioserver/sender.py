'''
sender.py

@author: Callum Hay
'''

import serial
import threading

class Sender(threading.Thread):

    def __init__(self, senderQueueMgr, outputSerialPort, baudRate):
        threading.Thread.__init__(self)
        self.queueMgr           = senderQueueMgr
        self.serialOutputPort   = None
        self.exitThread         = False
        self.lock               = threading.Semaphore()
        try:
            self.serialOutputPort = serial.Serial(outputSerialPort, baudrate=baudRate, timeout=1)
            self.serialOutputPort.timeout = 1
        except serial.SerialException:
            print "ERROR: Serial port " + outputSerialPort + " was invalid/not found."
            print "************ Killing Sender Thread ****************"
            exit(-1)           
        
    
    def ExitThread(self):
        self.lock.acquire()
        self.exitThread = True
        self.lock.release()    
    
    def run(self):
        # Make sure this object is in a proper state before running...
        if self.serialOutputPort == None:
            print "ERROR: Serial port was invalid/not found, could not run receiver."
            print "************ Killing Sender Thread ****************"
            return
        
        print "Running receiver..."
        
        self.lock.acquire() 
        while not self.exitThread:
            self.lock.release()
            
            print "Sending data out on serial port"
            # Write data to the serial port
            
            
            self.lock.acquire()
            
        self.lock.release()
        self.serialOutputPort.close()
        