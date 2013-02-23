'''
test_data_io.py

@author: Callum Hay
'''

# Python core imports
import glob
import time
import re
import threading

# SSF Utility Imports
from util.euclid import Vector3

TRAINING_FILE_DIR = "resources/training_data/"

LEFT_PREFIX = "left"
RIGHT_PREFIX = "right"

IDLE_PREFIX       = "idle"
JAB_PREFIX        = "jab"
HOOK_PREFIX       = "hook"
HADOUKEN_PREFIX   = "hadouken"
SONIC_BOOM_PREFIX = "sonic_boom"

class InputThread(threading.Thread):
    def __init__(self, inputMsg):
        threading.Thread.__init__(self)
        self._inputMsg = inputMsg
        
    def run(self):
        print self._inputMsg
        raw_input()
        

def WriteDataCaptureOneHandedGestureFile(handPrefix, gesturePrefix, capturedGloveData):
    filepath = TRAINING_FILE_DIR + handPrefix + "_" + gesturePrefix + "_" + str(time.time()) + ".txt"
    dataFile = None
    try:
        dataFile = open(filepath, 'w')
    except:
        print "Error opening file " + filepath
        return
    
    # Print all the captured data to file
    for i in capturedGloveData:
        #headingVec = Vector3(i.heading[0], i.heading[1], i.heading[2])
        #headingVec.normalize()
        #i.heading = (headingVec.x, headingVec.y, headingVec.z)
        dataFile.write(str(i.acceleration) + ";" + str(i.rotation) + ";" + str(i.heading) + "\n")

    dataFile.close()
    print handPrefix + " " + gesturePrefix + " data has been written to file: " + filepath

def WriteDataCaptureTwoHandedGestureFile(gesturePrefix, capturedRGloveData, capturedLGloveData):
    filepath = TRAINING_FILE_DIR + gesturePrefix + "_" + str(time.time()) + ".txt"
    dataFile = None
    try:
        dataFile = open(filepath, 'w')
    except:
        print "Error opening file " + filepath
        return
    
    # Print all the captured data to file
    for i,j in zip(capturedRGloveData, capturedLGloveData):
        dataFile.write("R:" + str(i.acceleration) + ";" + str(i.rotation) + ";" + str(i.heading) + "\n")
        dataFile.write("L:" + str(j.acceleration) + ";" + str(j.rotation) + ";" + str(j.heading) + "\n")

    dataFile.close()
    print gesturePrefix + " data has been written to file: " + filepath


# The read functions return 3D arrays of all training data for a 
# particular gesture:
# - The first index will give you the training data set
# - The second index will give you either accelerations, rotations or headings from that data set
# - The third index will give you the requested data at that frame of the recorded data
def ReadAllOneHandedGestureFiles(handPrefix, gesturePrefix):
    
    result = []
    
    # Grab an array of all the file names that match for the training data of
    # the given hand and gesture
    filenames = glob.glob(TRAINING_FILE_DIR + handPrefix + "_" + gesturePrefix + "*.txt")
    currFile = None
    for i in filenames:
        try:
            currFile = open(i, 'r')
        except:
            print "Error opening file " + i
            return None
    
        # One handed gestures are written as a series of lines of (Ax,Ay,Az);(Rx,Ry,Rz);(Hx,Hy,Hz)
        trainingDataSet = []
        fileLines = currFile.readlines()
        for currLine in fileLines:
            numbers  = re.split("[\(\),;]", currLine)
            
            # Remove empties and newlines
            numbers[:] = (num for num in numbers if num != '' and num != '\n')
            if len(numbers) == 0:
                continue
            
            # There should always be 9 floats
            if len(numbers) != 9:
                assert(False)
                return None
        
            trainingDataLine = []
            trainingDataLine.append([numbers[0], numbers[1], numbers[2]]) # acceleration
            trainingDataLine.append([numbers[3], numbers[4], numbers[5]]) # rotation
            trainingDataLine.append([numbers[6], numbers[7], numbers[8]]) # heading
            trainingDataSet.append(trainingDataLine)
        
        result.append(trainingDataSet)
        
    return result

def ReadOneHandedGestureFromTwoHandedGestureFiles(handPrefix, gesturePrefix):
    if handPrefix == RIGHT_PREFIX:
        return ReadAllTwoHandedGestureFiles(gesturePrefix, False, True)
    elif handPrefix == LEFT_PREFIX:
        return ReadAllTwoHandedGestureFiles(gesturePrefix, True, False)
    else:
        assert(False)
        return None
    

def ReadAllTwoHandedGestureFiles(gesturePrefix, includeLeft, includeRight):
    assert(includeLeft or includeRight)
    result = []
    
    # Grab an array of all the file names that match for the training data of
    # the given hand and gesture
    filenames = glob.glob(TRAINING_FILE_DIR + gesturePrefix + "*.txt")
    currFile = None
    for i in filenames:
        try:
            currFile = open(i, 'r')
        except:
            print "Error opening file " + i
            return None
        
        # Two handed gestures are written as a series of lines of 
        # alternating:
        # R:(Ax,Ay,Az);(Rx,Ry,Rz);(Hx,Hy,Hz)
        # L:(Ax,Ay,Az);(Rx,Ry,Rz);(Hx,Hy,Hz)
        trainingDataSet = []
        fileLines = currFile.readlines()
        
        count = 0
        handNumbers = None
        
        for currLine in fileLines:
            if count % 2 == 0 and includeRight:
                handNumbers = re.split("[R\:\(\),;]", currLine)
            elif includeLeft:
                handNumbers  = re.split("[L\:\(\),;]", currLine)
            else:
                count += 1
                continue
            
            # Remove empties and newlines
            handNumbers[:] = (num for num in handNumbers if num != '' and num != '\n')
            if len(handNumbers) == 0:
                continue
            
            # There should always be 9 floats per number list
            if len(handNumbers) != 9:
                print handNumbers
                assert(False)
                return None
        
            trainingDataLine = []
            trainingDataLine.append([handNumbers[0], handNumbers[1], handNumbers[2]]) # acceleration
            trainingDataLine.append([handNumbers[3], handNumbers[4], handNumbers[5]]) # rotation
            trainingDataLine.append([handNumbers[6], handNumbers[7], handNumbers[8]]) # heading
            trainingDataSet.append(trainingDataLine)
            
            count += 1
        
        result.append(trainingDataSet)
        
        
    return result
    
    
    