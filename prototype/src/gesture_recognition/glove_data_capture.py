'''
glove_data_capture.py
Captures data from a pair of gloves for the super street fire game
and stores it into a context specific file.
'''

# Python standard library imports
import os
import time
import traceback
import threading
from optparse import OptionParser

# 3rd Party Python library imports
#import pybrain

# SSF Python imports
import ioserver.xbeeio
from ioserver.receiver_queue_mgr import SimpleReceiverQueue
from util.euclid import Vector3
import test_data_io 

PLAYER_1_IDX = 0
PLAYER_2_IDX = 1

def CountDown():
    print "3..."
    time.sleep(0.5)
    print "2..." 
    time.sleep(0.5)
    print "1..."
    time.sleep(0.5)

def PromptForAndCaptureCalibration(leftGloveFunction, rightGloveFunction):
    print "Prepare to calibrate the forward facing direction:"
    print "Point both arms, outstretched, with hands pointed outwards towards the direction in which"
    print "you will be performing gestures/actions in. i.e., if you are punching towards something,"
    print "point your hands towards what you will be punching."
    CountDown()
    
    print "Capturing Data..."

    calibrationTime = 5.0
    lastFrameTime = time.time()
    
    calibratedLForwardVec = Vector3(0,0,0)
    calibratedRForwardVec = Vector3(0,0,0)
    calibratedLRotationVec = Vector3(0,0,0)
    calibratedRRotationVec = Vector3(0,0,0)
    
    while calibrationTime > 0.0:
        leftGloveData  = leftGloveFunction()
        rightGloveData = rightGloveFunction()
        
        # Make sure that there's at least some data coming in
        if leftGloveData == None and rightGloveData == None:
            continue
        
        # We are attempting to evaluate the forward facing vector by averaging
        # the forward facing vectors of both gloves, together
        if leftGloveData != None:
            calibratedLForwardVec  += leftGloveData.heading
            calibratedLRotationVec += leftGloveData.rotation
        if rightGloveData != None:
            calibratedRForwardVec  += rightGloveData.heading
            calibratedRRotationVec += rightGloveData.rotation
            
        currTime = time.time()
        deltaTime = currTime - lastFrameTime
        lastFrameTime = currTime
        calibrationTime -= deltaTime
                
    calibratedLForwardVec.normalize()
    calibratedRForwardVec.normalize()
    calibratedLRotationVec.normalize()
    calibratedRRotationVec.normalize()
    print "Calibrated Left Glove Forward Vector:   ", calibratedLForwardVec
    print "Calibrated Right Glove Forward Vector:  ", calibratedRForwardVec
    print "Calibrated Left Glove Rotation Vector:  ", calibratedLRotationVec
    print "Calibrated Right Glove Rotation Vector: ", calibratedRRotationVec
    
    
    return [calibratedLForwardVec, calibratedRForwardVec]

def PromptAndCaptureOneHandGesture(gesturePrefix, handPrefix, gloveFunction):
    
    # Prompt the user to get them ready to perform the jab
    print "Prepare to perform a " + gesturePrefix + " with your " + handPrefix + " hand..."
    raw_input("<Press Enter to Start>")
    inputThread = test_data_io.InputThread("<Press Enter when finished>")
    inputThread.start()
    
    # Clean out the queues
    rightGloveData = gloveFunction()
    
    # Capture data from the gesture while the user hasn't hit a key
    # in the inputThread
    capturedGloveData = []
    fileIsEmpty = True
    while inputThread.isAlive():
        rightGloveData = gloveFunction()
        if rightGloveData != None:
            capturedGloveData.append(rightGloveData)
            fileIsEmpty = False
    
    # Setup a file on disk to save the gesture data to...
    if not fileIsEmpty:
        test_data_io.WriteDataCaptureOneHandedGestureFile(handPrefix, gesturePrefix, capturedGloveData)
    else:
        print "No data was recorded!"

def PromptAndCaptureTwoHandedGesture(gesturePrefix, rGloveFunction, lGloveFunction):
    # Prompt the user to get them ready to perform the jab
    print "Prepare to perform a " + gesturePrefix + "..."
    print "<Press Enter to Start>"
    raw_input()
    inputThread = test_data_io.InputThread("<Press Enter when finished>")
    inputThread.start()
    
    # Clean out the queues
    rGloveFunction()
    lGloveFunction()
    rightGloveData = None
    leftGloveData  = None
    
    # Capture data from the gesture while the user hasn't hit a key
    # in the inputThread
    capturedRGloveData = []
    capturedLGloveData = []
    fileIsEmpty = True
    while inputThread.isAlive():
        if rightGloveData == None:
            rightGloveData = rGloveFunction()
        if leftGloveData == None:
            leftGloveData  = lGloveFunction()
        
        if rightGloveData != None and leftGloveData != None:
            capturedRGloveData.append(rightGloveData)
            capturedLGloveData.append(leftGloveData)
            fileIsEmpty = False
            rightGloveData = None
            leftGloveData  = None

    # Setup a file on disk to save the gesture data to...
    if not fileIsEmpty:
        test_data_io.WriteDataCaptureTwoHandedGestureFile(gesturePrefix, capturedRGloveData, capturedLGloveData)
    else:
        print "No data was recorded!"

if __name__ == '__main__':
    
    # Assign the default input port based on the OS
    IN_PORT = "/dev/tty.xbee"
    if (os.name.find("nt") > -1):
        IN_PORT = "COM11"
    
    DEFAULT_BAUDRATE = 57600
    
    # Parse options from the command line
    usageStr = "usage: %prog [options]"
    cmdLineDescStr = "The glove training data capture application for superstreetfire..."
    argParser = OptionParser(usage=usageStr, description=cmdLineDescStr)
    argParser.add_option("-i", "--input_port", action="store", type="string", dest="inputPort", \
                         help="The serial port name/number that provides input from clients. [%default]", \
                         default=IN_PORT)
    argParser.add_option("-p", "--player_num", action="store", type="int", dest="playerNum", \
                         help="The player number of the gloves to capture data from (can be 1 or 2). [%default]", \
                         default=1)
    
    (options, args) = argParser.parse_args()
    
    # Check the provided options for validity...
    if options.playerNum != 1 and options.playerNum != 2:
        argParser.error("Invalid player number.")
    
    try:
        # Build the queue managers for holding aggregated send and receive data
        receiverQueueMgr = SimpleReceiverQueue()
        # Spawn threads for listening and sending data over the serial port
        ioserver.xbeeio.receiverQueueMgr = receiverQueueMgr
        ioManager = ioserver.xbeeio.XBeeIO(options.inputPort, DEFAULT_BAUDRATE)
    
        if ioManager.xbee == None:
            exit(-1)
        
        leftGloveFunctions = [receiverQueueMgr.PopP1LeftGloveData, \
                              receiverQueueMgr.PopP2LeftGloveData]
        rightGloveFunctions = [receiverQueueMgr.PopP1RightGloveData, \
                               receiverQueueMgr.PopP2RightGloveData]
        
        playerIdx = options.playerNum-1
        #[calibratedLForwardVec, calibratedRForwardVec] = \
        #    PromptForAndCaptureCalibration(leftGloveFunctions[playerIdx], rightGloveFunctions[playerIdx])
        
        while True:
            print "Select option:"
            print "1. Capture Right Jab"
            print "2. Capture Right Hook"
            print "3. Capture Left Jab"
            print "4. Catpure Left Hook"
            print "5. Capture Hadouken"
            print "6. Capture Sonic Boom"
            print "7. Capture Idle"
    
            selectedOption = None
            try:
                selectedOption = str(raw_input("> "))
            except ValueError:
                print "Invalid option"
                continue
            
            #if selectedOption == 'c' or selectedOption == 'C' or selectedOption == '1':
            #    [calibratedLForwardVec, calibratedRForwardVec] = \
            #        PromptForAndCaptureCalibration(leftGloveFunctions[playerIdx], rightGloveFunctions[playerIdx])
            if selectedOption == '1':
                PromptAndCaptureOneHandGesture(test_data_io.JAB_PREFIX, test_data_io.RIGHT_PREFIX, \
                                               rightGloveFunctions[playerIdx])
            elif selectedOption == '2':
                PromptAndCaptureOneHandGesture(test_data_io.HOOK_PREFIX, test_data_io.RIGHT_PREFIX, \
                                               rightGloveFunctions[playerIdx])
            elif selectedOption == '3':
                PromptAndCaptureOneHandGesture(test_data_io.JAB_PREFIX, test_data_io.LEFT_PREFIX, \
                                               leftGloveFunctions[playerIdx])
            elif selectedOption == '4':
                PromptAndCaptureOneHandGesture(test_data_io.HOOK_PREFIX, test_data_io.LEFT_PREFIX, \
                                               leftGloveFunctions[playerIdx])
            elif selectedOption == '5':
                PromptAndCaptureTwoHandedGesture(test_data_io.HADOUKEN_PREFIX, \
                                                 rightGloveFunctions[playerIdx], leftGloveFunctions[playerIdx])
            elif selectedOption == '6':
                PromptAndCaptureTwoHandedGesture(test_data_io.SONIC_BOOM_PREFIX, \
                                                 rightGloveFunctions[playerIdx], leftGloveFunctions[playerIdx])
            elif selectedOption == '7':
                PromptAndCaptureTwoHandedGesture(test_data_io.IDLE_PREFIX, \
                                                 rightGloveFunctions[playerIdx], leftGloveFunctions[playerIdx])
            else:
                continue
            
    except KeyboardInterrupt:
        print "Ctrl+c Issued..."
    except:
        traceback.print_exc()       
    
    if ioManager != None: ioManager.Kill()
    print "Exiting..."
    
    
    
