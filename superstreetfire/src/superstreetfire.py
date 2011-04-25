'''
superstreetfire.py

The entry point for the superstreetfire server/application.
'''

import time
import traceback
from optparse import OptionParser

import os
import ioserver.receiver 
import ioserver.receive_from_file
#from ioserver.client_emulator import ClientEmulator
from ioserver.receiver_queue_mgr import ReceiverQueueMgr

from gamemodel.gesture_recognizer import GestureRecognizer
from gamemodel.ssf_game import SSFGame

def KillEverything(threadList):
    print "Killing all threads..."
    for thread in threadList:
        thread.ExitThread()
        thread.join()


if __name__ == '__main__':

    #snb: hate typing but didn't want to change the default..
    IN_PORT="/dev/slave"
    if (os.name.find("nt") > -1):
        IN_PORT = "COM4"
    
    # Parse options from the command line
    usageStr = "usage: %prog [options]"
    cmdLineDescStr = "The server for the superstreetfire application..."
    argParser = OptionParser(usage=usageStr, description=cmdLineDescStr)
    argParser.add_option("-i", "--input_port", action="store", type="string", dest="inputPort", \
                         help="The serial port name/number that provides input from clients. [%default]", \
                         default=IN_PORT)
    argParser.add_option("-o", "--output_port", action="store", type="string", dest="outputPort", \
                         help="The serial port name/number that sends output to clients. [%default]", \
                         default="/dev/slave")
    argParser.add_option("-f", "--frequency", action="store", type="int", dest="frequency", \
                         help="The simulation tick frequency. [%default]", default=50)    
    #argParser.add_option("-b", "--baud_rate", action="store", type="int", dest="baudRate", \
    #                     help="The serial port baud rate defaults to 57600.", default=57600)

    (options, args) = argParser.parse_args()
    
    
    # Error checking on the command line input parameters
    if options.frequency <= 0:
        argParser.error("Specified frequency must be greater than zero.")
    
    print "Supplied Options:"
    print options
    
    DEFAULT_BAUDRATE = 57600
    FIXED_FRAME_TIME = 1.0 / float(options.frequency) 
    
    # The following loop and try/catch is to make sure that we kill the whole
    # process in cases of imposed exceptions
    try:
            # Start the emulator if it was specified as an argument
        #clientEmulator = None
        #if options.emulatorPort != None:
        #    clientEmulator = ClientEmulator(options.emulatorPort, DEFAULT_BAUDRATE)
        #    clientEmulator.start()
        
        # Build the queue managers for holding aggregated send and receive data
        receiverQueueMgr = ReceiverQueueMgr()
    
        # Spawn threads for listening and sending data over the serial port
        receiverThread = ioserver.receiver.Receiver(receiverQueueMgr, options.inputPort, DEFAULT_BAUDRATE)
        #print "Running receiver with file input ..."
        #receiverThread = ioserver.receive_from_file.FileReceiver(receiverQueueMgr)
        receiverThread.start()
        
        #TODO
        #senderThread = ioserver.sender.Sender(options.outputPort, DEFAULT_BAUDRATE)
        #senderThread.start()
        
        
        # TODO: Move time stuff into the gamemodel...
        
        # Time data initialization
        startOfSimulationTime = time.time()
        
        deltaFrameTime = 0            # Holds the current frame's delta time
        lastFrameTime  = time.time()  # Holds the absolute time of the last frame
        currTime       = time.time()  # Temporary variable for the current absolute time
        currTimeStamp  = time.time()  # Holds the total time since the start of the simulation
        
        # Constant for the starting time of the simulation
        SIMULATION_START_TIME = currTime # Don't change this value!
        
        # Game model and gesture recognition system initialization
        # TODO: What the heck is our calibration data and where does it come from?
        gestureRecognizer = GestureRecognizer()
        ssfGame           = SSFGame(gestureRecognizer)
        
        threadsAreAlive = receiverThread.isAlive() #and senderThread.isAlive()
        while threadsAreAlive:
            
            # Keep track of a delta time for each frame, this will be used to 
            # calculate values for the current frame of the simulation and also keep
            # track of the time so far
            currTime       = time.time()
            deltaFrameTime = currTime - lastFrameTime
            lastFrameTime  = currTime
            currTimeStamp  = currTime - startOfSimulationTime
            
            # The receiver has been asynchronously receiving data and dumping it
            # onto the receiverQueueMgr, we need to grab that data and apply it to the simulation...
            
            # Feed any newly received data to the gesture recognizer
            gestureRecognizer.UpdateWithGestureData(receiverQueueMgr.PopP1LeftGloveData(),  \
                                                    receiverQueueMgr.PopP1RightGloveData(), \
                                                    receiverQueueMgr.PopP2LeftGloveData(),  \
                                                    receiverQueueMgr.PopP2RightGloveData(), \
                                                    deltaFrameTime, lastFrameTime)
            
            # TODO: What do we do with head-set data ??
            # is it part of the gesture recognition or is it a complement to it?
            receiverQueueMgr.PopP1HeadsetData()
            receiverQueueMgr.PopP2HeadsetData()
            
            ssfGame.Tick(deltaFrameTime)
            
            # TODO: Now that the simulation has iterated, grab
            # any resulting outputs to actuator clients (i.e., fire, lights, etc.)
            # and place them on the sender queues
            
            #...
            
            threadsAreAlive = receiverThread.isAlive()
            
            # Sync to the specified frequency
            if deltaFrameTime < FIXED_FRAME_TIME:
                time.sleep(FIXED_FRAME_TIME - deltaFrameTime)
            
    except KeyboardInterrupt:
        print "Ctrl+c Issued..."
    except:
        print "Unexpected exception occurred!"
        traceback.print_exc()

    KillEverything([receiverThread])
    print "Exiting..."
    
    
    
    
