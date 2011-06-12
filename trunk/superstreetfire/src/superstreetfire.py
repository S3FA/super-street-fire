'''
superstreetfire.py

The entry point for the superstreetfire server/application.
'''

import time
import traceback
from optparse import OptionParser

import os
import logging
import ioserver.xbeeio
#import ioserver.receive_direct_serial
#import ioserver.receive_from_file
import gamemodel.game_states
#from ioserver.client_emulator import ClientEmulator
from ioserver.receiver_queue_mgr import ReceiverQueueMgr
from ioserver.output_listener import SenderListener

from gamemodel.ssf_game import SSFGame

import pygame
from gui import gui


if __name__ == '__main__':

    # IN_PORT="/dev/slave"
    IN_PORT = "/dev/tty.xbee"
    if (os.name.find("nt") > -1):
        IN_PORT = "COM10"
    
    # Parse options from the command line
    usageStr = "usage: %prog [options]"
    cmdLineDescStr = "The server for the superstreetfire application..."
    argParser = OptionParser(usage=usageStr, description=cmdLineDescStr)
    argParser.add_option("-i", "--input_port", action="store", type="string", dest="inputPort", \
                         help="The serial port name/number that provides input from clients. [%default]", \
                         default=IN_PORT)
    argParser.add_option("-f", "--frequency", action="store", type="int", dest="frequency", \
                         help="The simulation tick frequency. [%default]", default=50)    

    (options, args) = argParser.parse_args()
    
    # setup some default log config
    logging.basicConfig(level=logging.DEBUG,
                    format='%(asctime)s %(name)-12s %(levelname)-8s %(message)s',
                    datefmt='%m-%d %H:%M',
                    filename='ssf.log',
                    filemode='w')
    # create console handler with a higher log level, add it to the root logger
    ch = logging.StreamHandler()
    ch.setLevel(logging.INFO)
    logging.getLogger('').addHandler(ch)
    
    ioManager = None

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
        ioserver.xbeeio.receiverQueueMgr = receiverQueueMgr
        ioManager = ioserver.xbeeio.XBeeIO(options.inputPort, DEFAULT_BAUDRATE)
        
        #print "Running receiver with file input ..."
        #receiverObj = ioserver.receive_from_file.FileReceiver(receiverQueueMgr)
        #print "Running receiving with direct serial connection .."
        #receiver = ioserver.receive_direct_serial.LineReceiver(receiverQueueMgr, options.inputPort, DEFAULT_BAUDRATE)
        
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
        
        # GUI
        pygame.init ()
        uiController = gui.UIController(ssfGame, ioManager)
        sender = SenderListener(ssfGame, ioManager)
        
        # Jump straight to the round-in-play for testing
        ssfGame._SetState(gamemodel.game_states.RoundBeginGameState(ssfGame, 1))

        while True:
            
            # Keep track of a delta time for each frame, this will be used to 
            # calculate values for the current frame of the simulation and also keep
            # track of the time so far
            currTime       = time.time()
            deltaFrameTime = currTime - lastFrameTime
            lastFrameTime  = currTime
            currTimeStamp  = currTime - startOfSimulationTime
            
            # check for pygame (GUI) events and pass to renderer
            events = pygame.event.get ()
            if pygame.QUIT in [ev.type for ev in events]:
                print "got pygame.QUIT event"
                break
            uiController.renderer.distribute_events(*events)
            uiController.renderer.refresh()
            
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
            
            ssfGame.UpdateRSSI(receiverQueueMgr.GetRSSIMap())
            
            ssfGame.Tick(deltaFrameTime)
            
            # looking at the emitter states from the P1 perspective
            # send the full state to the wifire board
            ioManager.SendFireEmitterData(ssfGame.GetLeftEmitterArc(1), ssfGame.GetRightEmitterArc(1))
            
            # Sync to the specified frequency
            if deltaFrameTime < FIXED_FRAME_TIME:
                time.sleep(FIXED_FRAME_TIME - deltaFrameTime)
            
    except KeyboardInterrupt:
        print "Ctrl+c Issued..."
    except:
        logging.warn("Unexpected state! (Is everything turned on?)")
        traceback.print_exc()

    if ioManager != None: ioManager.Kill()
    #sender.Kill()
    print "Exiting..."
    
    
    
    
