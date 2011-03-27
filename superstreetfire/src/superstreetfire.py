'''
superstreetfire.py

The entry point for the superstreetfire server/application.
'''

#import sys
import time
from optparse import OptionParser


import ioserver.receiver 
#from ioserver.client_emulator import ClientEmulator
from ioserver.receiver_queue_mgr import ReceiverQueueMgr

def KillEverything(threadList):
    print "Killing all threads..."
    for thread in threadList:
        thread.ExitThread()
        thread.join()


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
    
    # Build the queue managers for holding aggregated send and receive data
    receiverQueueMgr = ReceiverQueueMgr()

    # Spawn threads for listening and sending data over the serial port
    receiverThread = ioserver.receiver.Receiver(receiverQueueMgr, options.inputPort, DEFAULT_BAUDRATE)
    receiverThread.start()
    
    #TODO
    #senderThread = ioserver.sender.Sender(options.outputPort, DEFAULT_BAUDRATE)
    #senderThread.start()
    
    # The following loop and try/catch is to make sure that Ctrl+c still works
    # even though we're running separate threads
    
    try:
    
        threadsAreAlive = receiverThread.isAlive() #and senderThread.isAlive()
        while threadsAreAlive:
            # The receiver has been asynchronously receiving data and dumping it
            # onto the receiverQueueMgr, we need to grab that data and apply it to the simulation...
            
            # TODO: Execute the new data on the current game model thus furthering the simulation
            receiverQueueMgr.PopP1LeftGloveData()
            receiverQueueMgr.PopP2LeftGloveData()
            
            receiverQueueMgr.PopP1RightGloveData()
            receiverQueueMgr.PopP2RightGloveData()
            
            receiverQueueMgr.PopP1HeadsetData()
            receiverQueueMgr.PopP2HeadsetData()
            
            # TODO: Now that the simulation has iterated, grab
            # any resulting outputs to actuator clients (i.e., fire, lights, etc.)
            # and place them on the sender queues
            
            #senderQueueMgr.PushQueueData(senderQueueMgr.fireFx1, simulation.fireFx1)
            #...
            
            threadsAreAlive = receiverThread.isAlive()
            time.sleep(0.02)
            
    except KeyboardInterrupt:
        print "Ctrl+c Issued..."
    finally:
        KillEverything([receiverThread])
        print "Exiting..."
    
    
    
    
