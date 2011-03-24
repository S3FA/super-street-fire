'''
superstreetfire.py

The entry point for the superstreetfire server/application.
'''

import sys
from optparse import OptionParser
#from ioserver.client_emulator import ClientEmulator
import ioserver.receiver 

if __name__ == '__main__':

    # Parse options from the command line
    usageStr = "usage: %prog [options]"
    cmdLineDescStr = "The server for the superstreetfire application..."
    argParser = OptionParser(usage=usageStr, description=cmdLineDescStr)
    argParser.add_option("-i", "--input_port", action="store", type="string", dest="inputPort", \
                         help="The serial port name/number that provides input from clients. [%default]", \
                         default="/dev/slave")
    argParser.add_option("-o", "--output_port", action="store", type="string", dest="outputPort", \
                         help="The serial port name/number that sends output to clients. [%default]", \
                         default="/dev/slave")
    #argParser.add_option("-b", "--baud_rate", action="store", type="int", dest="baudRate", \
    #                     help="The serial port baud rate defaults to 57600.", default=57600)

    (options, args) = argParser.parse_args()
    DEFAULT_BAUDRATE = 57600
    
    print "Supplied Options:"
    print options
    
    # Start the emulator if it was specified as an argument
    #clientEmulator = None
    #if options.emulatorPort != None:
    #    clientEmulator = ClientEmulator(options.emulatorPort, DEFAULT_BAUDRATE)
    #    clientEmulator.start()
    

    # Spawn threads for listening and sending data over the serial port
    receiverThread = ioserver.receiver.Receiver(options.inputPort, DEFAULT_BAUDRATE)
    receiverThread.start()
    
    # The following loop and try/catch is to make sure that Ctrl+C still works
    # even though we're running separate threads 
    while receiverThread.isAlive():
        try:
            receiverThread.join(1)
            
        except KeyboardInterrupt:
            print "Ctrl+c Issued, killing all threads and exiting..."
            receiverThread.exitThread = True

    
    
    
    
