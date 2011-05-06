'''
superstreetfire.py

The entry point for the superstreetfire server/application.
'''

import time
import traceback
from optparse import OptionParser

import os
import logging
import ioserver.receiver 
#import ioserver.receive_direct_serial
#import ioserver.receive_from_file
#from ioserver.client_emulator import ClientEmulator
from ioserver.receiver_queue_mgr import ReceiverQueueMgr

from gamemodel.gesture_recognizer import GestureRecognizer
from gamemodel.ssf_game import SSFGame

'''
def KillEverything(threadList):
    logging.warn("Killing all threads...")
    for thread in threadList:
        thread.ExitThread()
        thread.join()
'''

if __name__ == '__main__':

    #snb: hate typing but didn't want to change the default..
    IN_PORT="/dev/slave"
    if (os.name.find("nt") > -1):
        IN_PORT = "COM5"
    
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
    
    # setup some default log config
    logging.basicConfig(level=logging.DEBUG,
                    format='%(asctime)s %(name)-12s %(levelname)-8s %(message)s',
                    datefmt='%m-%d %H:%M',
                    filename='ssf.log',
                    filemode='w')
    # create console handler with a higher log level, add it to the root logger
    ch = logging.StreamHandler()
    ch.setLevel(logging.WARN)
    logging.getLogger('').addHandler(ch)

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
        ioserver.receiver.recieverQueueMgr = receiverQueueMgr
        receiverObj = ioserver.receiver.Receiver(options.inputPort, DEFAULT_BAUDRATE)
        
        #print "Running receiver with file input ..."
        #receiver = ioserver.receive_from_file.FileReceiver(receiverQueueMgr)
        #print "Running receiving with direct serial connection .."
        #receiver = ioserver.receive_direct_serial.LineReceiver(receiverQueueMgr, options.inputPort, DEFAULT_BAUDRATE)

        #TODO
        #sender = ioserver.sender.Sender(options.outputPort, DEFAULT_BAUDRATE)
        
        
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
        ssfGame = SSFGame()
        
        while True:
            
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
            ssfGame.UpdateWithGloveData(receiverQueueMgr.PopP1LeftGloveData(),  \
                                        receiverQueueMgr.PopP1RightGloveData(), \
                                        receiverQueueMgr.PopP2LeftGloveData(),  \
                                        receiverQueueMgr.PopP2RightGloveData(), \
                                        deltaFrameTime, lastFrameTime)
            ssfGame.UpdateWithHeadsetData(receiverQueueMgr.PopP1HeadsetData(), \
                                          receiverQueueMgr.PopP2HeadsetData(), \
                                          deltaFrameTime, lastFrameTime)
            
            ssfGame.Tick(deltaFrameTime)
            
            # TODO: Now that the simulation has iterated, grab
            # any resulting outputs to actuator clients (i.e., fire, lights, etc.)
            # and place them on the sender queues
            
            #...
            
            # Sync to the specified frequency
            if deltaFrameTime < FIXED_FRAME_TIME:
                time.sleep(FIXED_FRAME_TIME - deltaFrameTime)
            
    except KeyboardInterrupt:
        print "Ctrl+c Issued..."
    except:
        logging.warn("Unexpected state! (Is everything turned on?)")
        traceback.print_exc()

    #KillEverything([receiverThread])
    receiverObj.Kill()
    #sender.Kill()
    print "Exiting..."
    
    
    
    
