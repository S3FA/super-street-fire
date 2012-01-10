'''
glove_data_training.py
@author: Callum Hay

Aggregates and trains a state machine for recognizing gestures
whose data is captured by glove_data_capture.py. 
'''

# Python core imports
import os
import traceback
import pickle
from optparse import OptionParser

# PyBrain Imports
from pybrain.structure import RecurrentNetwork, LinearLayer, SigmoidLayer, FullConnection
from pybrain.datasets import SequenceClassificationDataSet
from pybrain.supervised.trainers import BackpropTrainer

# SSF Python imports
import ioserver.xbeeio
from ioserver.receiver_queue_mgr import SimpleReceiverQueue
import test_data_io

DEFAULT_BAUDRATE = 57600

RECOGNIZER_DIR                  = "resources/recognizers/"
HAND_RECOGNIZER_FILENAME_SUFFIX = "_hand_recognizer"

INPUT_SIZE  = 6
OUTPUT_SIZE = 1
HIDDEN_LAYER_SIZE = int((INPUT_SIZE + OUTPUT_SIZE) * 2.0/3.0)

def InitOneHandedGestureRecognizer():
    oneHandRecognizer = None
    
    '''
    # Check to see if the file for the recognizer(s) exists on disk, if so just load them
    recognizerFilename = RECOGNIZER_DIR + test_data_io.RIGHT_PREFIX + HAND_RECOGNIZER_FILENAME_SUFFIX
    if os.path.isfile(recognizerFilename):
        print "Previously created right hand recognizer found on file, loading into memory."
        recognizerFile = open(recognizerFilename, 'r')
        oneHandRecognizer = pickle.load(recognizerFile)
        recognizerFile.close()
    else:
    '''
    
    # Otherwise instance the recognizer for the first time
    oneHandRecognizer = RecurrentNetwork()
    
    print "Building neural network:"
    print "input size: " + str(INPUT_SIZE)
    print "hidden layer size: " + str(HIDDEN_LAYER_SIZE)
    print "output size: " + str(OUTPUT_SIZE)
    oneHandRecognizer.addInputModule(LinearLayer(INPUT_SIZE, name='in'))
    oneHandRecognizer.addModule(SigmoidLayer(HIDDEN_LAYER_SIZE, name='hidden0'))
    oneHandRecognizer.addOutputModule(LinearLayer(OUTPUT_SIZE, name='out'))
    
    oneHandRecognizer.addConnection(FullConnection(oneHandRecognizer['in'], \
                                                     oneHandRecognizer['hidden0'], name='in_to_hidden0'))
    oneHandRecognizer.addConnection(FullConnection(oneHandRecognizer['hidden0'], \
                                                     oneHandRecognizer['out'], name='hidden0_to_out'))
    oneHandRecognizer.addRecurrentConnection(FullConnection(oneHandRecognizer['hidden0'], \
                                                              oneHandRecognizer['hidden0'], \
                                                              name='recurrent_hidden0_hidden0'))
    oneHandRecognizer.addRecurrentConnection(FullConnection(oneHandRecognizer['hidden0'], \
                                                            oneHandRecognizer['in'], \
                                                            name='recurrent_hidden0_in'))    
    #oneHandRecognizer.addRecurrentConnection(FullConnection(oneHandRecognizer['out'], \
    #                                                          oneHandRecognizer['hidden0'], \
    #                                                          name='recurrent_out_hidden0'))
    oneHandRecognizer.sortModules()
        
    return oneHandRecognizer

def AppendOneHandedTrainingData(dataSet, handPrefix, gesturePrefix, outputIdx):
    trainingData = None
    if gesturePrefix == test_data_io.IDLE_PREFIX:
        trainingData = test_data_io.ReadOneHandedGestureFromTwoHandedGestureFiles(handPrefix, \
                                                                                  gesturePrefix)
    else:
        trainingData = test_data_io.ReadAllOneHandedGestureFiles(handPrefix, \
                                                                 gesturePrefix)
    
    for dataSetSequence in trainingData:
        dataSet.newSequence()
        for dataPiece in dataSetSequence:
            currData = dataPiece[0]           # acceleration
            currData.extend(dataPiece[1])     # rotation
            dataSet.appendLinked(currData, [outputIdx])
    
    return len(trainingData)
  

def RunTraining(rightHandRecognizer):
    dataSet = SequenceClassificationDataSet(INPUT_SIZE, 1, class_labels=['NoGesture', 'Jab'])
    
    totalAmtOfTrainingData = 0
    totalAmtOfTrainingData += AppendOneHandedTrainingData(dataSet, test_data_io.RIGHT_PREFIX, test_data_io.IDLE_PREFIX,  0)
    totalAmtOfTrainingData += AppendOneHandedTrainingData(dataSet, test_data_io.RIGHT_PREFIX, test_data_io.JAB_PREFIX,   1)
    #totalAmtOfTrainingData += AppendOneHandedTrainingData(dataSet, test_data_io.RIGHT_PREFIX, test_data_io.HOOK_PREFIX, 2)

    print "Number of found training data sets: " + str(totalAmtOfTrainingData)
    rightHandRecognizer.reset()
    trainer = BackpropTrainer(rightHandRecognizer, dataSet)
    #trainer.trainEpochs(5)
    trainer.trainUntilConvergence(dataSet, verbose=True, continueEpochs=5)
    
    # Pickle the trainer so we can load it next time
    rightHandRecFile = None
    rightHandRecFilename = RECOGNIZER_DIR + test_data_io.RIGHT_PREFIX + HAND_RECOGNIZER_FILENAME_SUFFIX
    try:
        rightHandRecFile = open(rightHandRecFilename, 'w')
    except:
        print "Failed to write recognizer file: " + rightHandRecFilename
        return

    pickle.dump(rightHandRecognizer, rightHandRecFile)
    rightHandRecFile.close()
    
    

def TestGestureRecognition(rightGloveFunction, leftGloveFunction, \
                           rightHandRecognizer):

    # Spawn threads for listening and sending data over the serial port
    ioManager = ioserver.xbeeio.XBeeIO(options.inputPort, DEFAULT_BAUDRATE)

    if ioManager.xbee == None:
        print "Failed to initialize manager for receiving glove data."
        return

    inputThread = test_data_io.InputThread("<Press Enter when finished>")
    inputThread.start()
    
    currRightHandResult = None
    prevRightHandResult = None
    try:
        while inputThread.isAlive():
            rightGloveData = rightGloveFunction()
            leftGloveData  = leftGloveFunction()
            
            if rightGloveData != None:
                
                
                currRightHandResult = rightHandRecognizer.activate([rightGloveData.acceleration[0], \
                                                                    rightGloveData.acceleration[1], \
                                                                    rightGloveData.acceleration[2],
                                                                    rightGloveData.rotation[0],
                                                                    rightGloveData.rotation[1],
                                                                    rightGloveData.rotation[2]])
                if currRightHandResult != prevRightHandResult:
                    print str(currRightHandResult)
                    prevRightHandResult = currRightHandResult
                
                    
            if leftGloveData != None:
                pass
            if leftGloveData != None and rightGloveData != None:
                pass
    except:
        print "ERROR <Press Enter>"
        inputThread.join()
    ioManager.Kill()

if __name__ == '__main__':
    # Assign the default input port based on the OS
    IN_PORT = "/dev/tty.xbee"
    if (os.name.find("nt") > -1):
        IN_PORT = "COM11"
    
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
    
    
    # Build the queue managers for holding aggregated send and receive data
    receiverQueueMgr = SimpleReceiverQueue()
    ioserver.xbeeio.receiverQueueMgr = receiverQueueMgr
    
    leftGloveFunctions = [receiverQueueMgr.PopP1LeftGloveData, \
                          receiverQueueMgr.PopP2LeftGloveData]
    rightGloveFunctions = [receiverQueueMgr.PopP1RightGloveData, \
                           receiverQueueMgr.PopP2RightGloveData]
    playerIdx = options.playerNum-1
    
    try:
        rightHandRecognizer = InitOneHandedGestureRecognizer()
        
        selectedOption = None
        while True:
            
            print "Select an option:"
            print "1. (Re)Run training and build gesture recognizer model"
            print "2. Test Gesture Recognition"
            selectedOption = str(raw_input("> "))

            if selectedOption == "1":
                RunTraining(rightHandRecognizer)
            elif selectedOption == "2":
                TestGestureRecognition(rightGloveFunctions[playerIdx], leftGloveFunctions[playerIdx], \
                                       rightHandRecognizer)
            else:
                print "Invalid option selected."
            
            
    except KeyboardInterrupt:
        print "Ctrl+c Issued..."
    except:
        traceback.print_exc()       

    print "Exiting..."