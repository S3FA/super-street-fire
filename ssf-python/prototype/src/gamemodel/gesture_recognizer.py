'''
gesture_recognizer.py

@author: Callum Hay
'''
import logging
from gesture_state import PlayerGestureState
from calibration_data import CalibrationData

class GestureRecognizer:
    LOGGER_NAME = 'gesture_rec'
    
    def __init__(self): 
        self._logger = logging.getLogger(GestureRecognizer.LOGGER_NAME)
        self._p1GestureState = PlayerGestureState(self, 1)
        self._p2GestureState = PlayerGestureState(self, 2)
        self._calibrationData = CalibrationData()
        
        # Keep lists of all actions that have been generated by the gesture
        # statemachine - these will be picked up by the SSFGame and executed 
        self._lastUnconsumedActions = []
        
    # Sets the calibration data for the gesture recognition system    
    def SetCalibrationData(self, calibrationData):
        # Make sure the calibration exists and is finished before setting it
        assert(calibrationData != None)
        assert(calibrationData.isFinishedSampling)

        self._calibrationData = calibrationData
    
    # Determine whether there are any newly available
    # actions that have recently been recognized (recent in-so-far as it has
    # not yet been consumed via the PopActions() method).
    def HasNewActionsAvailable(self):
        #print 'HasNewActionsAvailable %d ' % (len(self._lastUnconsumedActions))
        return len(self._lastUnconsumedActions) > 0
    
    # Pop all of the available actions from the action list - these
    # are actions that have been generated by the gesture state machine
    # since the last call to this function
    # This will return a list of all the actions and then empty
    # this object's list of actions
    def PopActions(self):
        temp = self._lastUnconsumedActions
        self._lastUnconsumedActions = []
        return temp
    
    # Update the gesture recognizer with a new 'frame' of data,
    # any piece of the provided data may be equal to None, in such
    # a case that value is maintained/unchanged within the recognizer
    # from the last time it was set 
    def UpdateWithGloveData(self, p1LGloveData, p1RGloveData,
                            p2LGloveData, p2RGloveData, dT, timeStamp):
        self._p1GestureState.Update(p1LGloveData, p1RGloveData, dT, timeStamp)
        self._p2GestureState.Update(p2LGloveData, p2RGloveData, dT, timeStamp)
                        

    # doesn't really need to be two functions here, but i'll change it later.
    def ChangeP1State(self, newAction):
        self._lastUnconsumedActions.append(newAction);
    
    def ChangeP2State(self, newAction):
        self._lastUnconsumedActions.append(newAction);
        
    