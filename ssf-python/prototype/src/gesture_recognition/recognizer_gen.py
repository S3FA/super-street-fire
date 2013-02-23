'''
Created on Sep 5, 2011

@author: callumhay
'''

import test_data_io
from util.euclid import Vector3

class RecognizerNode:
    QUANTIZE_RADIUS_THRESHOLD = 100.0
    
    def __init__(self):
        self.center = None
        self.numPtsAdded = 0
        pass

    def DistanceFromCenter(self, pt):
        if self.center == None:
            return 0.0
        else:
            return abs(pt - self.center)

    def PointInRadius(self, pt):
        if self.center == None:
            return True
        distance = abs(pt - self.center)
        return (distance <= self.QUANTIZE_RADIUS_THRESHOLD)

    # Attempt to add a test data point to this node - if the test data point
    # is within a certain threshold of the center value of this node (or the
    # node hasn't been initialized yet) then it will affect the center of the
    # node and True will be returned. Otherwise, False is returned.
    def AddPoint(self, pt):
        if self.center == None:
            self.center = pt
            self.numPtsAdded = 1
            return True
        distance = abs(pt - self.center)
        if distance <= self.QUANTIZE_RADIUS_THRESHOLD:
            self.center = (self.center + pt) / 2.0
            self.numPtsAdded += 1
            return True
        else:
            return False

    def Merge(self, otherNode):
        self.AddPoint(otherNode.center)

class Recognizer:
    
    def __init__(self):
        self.nodes = []
        pass

    def AddNode(self):
        self.nodes.append(RecognizerNode())
        return self.nodes[len(self.nodes)-1]
    
    def Merge(self, otherRecognizer):
        DATA_PT_IDX_DISPARITY = 5
        
        # Find the closest match to the beginning, either merge the node by adding
        # the point or create a new node before or after it based on the surrounding values
        initialOtherNode = otherRecognizer.nodes[0]
        thisNodeIdx      = 0
        #smallestDistance = 99999999.999
        #smallestDistIdx  = -1
        
        for thisNode in self.nodes:
            if thisNodeIdx < DATA_PT_IDX_DISPARITY:
                # If the node can be merged then we merge it an exit, yay!
                if thisNode.PointInRadius(initialOtherNode):
                    thisNode.Merge(initialOtherNode)
                    break
                '''
                else:
                    # We need to continue searching for a node suitable for merging with
                    distance = thisNode.DistanceFromCenter(initialOtherNode.center)
                    if smallestDistance > distance:
                        smallestDistance = distance
                        smallestDistIdx  = thisNodeIdx
                ''' 
                thisNodeIdx += 1
            else:
                # Insert at the beginning of the list
                thisNodeIdx = 0
                self.nodes.insert(0, initialOtherNode)
                break
        
        nextNodeIdx = thisNodeIdx + 1
        for otherNode in otherRecognizer.nodes[1:]:
            
        

def GenerateOneHandedGestureRecognizer(self, handPrefix, gesturePrefix):
    trainingData = test_data_io.ReadAllOneHandedGestureFiles(handPrefix, gesturePrefix)
    
    gestureRecognizers = []
    
    for dataSequence in trainingData:
        
        # Right now we're just interested in using acceleration 
        # Use the data from each data sequence (test run) to build a
        # recognizer with a single data set in it, after this well place
        # all the recognizers into a list and then reconcile them after
        currGestureRec = Recognizer()
        for dataPiece in dataSequence:

            currAccel = dataPiece[0] # acceleration
            recognizerNode = currGestureRec.AddNode()
            recognizerNode.AddPoint(Vector3(currAccel[0], currAccel[1], currAccel[2]))
            
        # Add the recognizer to the list of all recognizers from each test data set
        gestureRecognizers.append(currGestureRec)
    
    # All the data from each test set are in the recognizers list, compare them
    # all and create a finalized, single recognizer
    finalRecognizer = gestureRecognizers[0]
    
    for currRecognizer in gestureRecognizers[1:]:
        finalRecognizer.Merge(currRecognizer)
            

    
    