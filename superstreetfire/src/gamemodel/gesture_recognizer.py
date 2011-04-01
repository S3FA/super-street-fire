'''
gesture_recognizer.py

@author: Callum Hay
'''

from idle_gesture_state import IdleGestureState

class GestureRecognizer:
    # High-level attack and defense gesture enumeration values
    NO_GESTURE                  = 0
    LEFT_JAB_ATTACK_GESTURE     = 1
    RIGHT_JAB_ATTACK_GESTURE    = 2
    LEFT_HOOK_ATTACK_GESTURE    = 3 
    RIGHT_HOOK_ATTACK_GESTURE   = 4
    HADOUKEN_ATTACK_GESTURE     = 5
    LEFT_BLOCK_DEFENSE_GESTURE  = 101
    RIGHT_BLOCK_DEFENSE_GESTURE = 102
    
    def __init__(self, calibrationData): 
        # TODO: What the heck is our calibration data and where does it come from?
        self._p1GestureState = IdleGestureState(self, 1)
        self._p2GestureState = IdleGestureState(self, 2)
        
        self._p1LastUnconsumedGesture = GestureRecognizer.NO_GESTURE
        self._p2LastUnconsumedGesture = GestureRecognizer.NO_GESTURE
    
    # These functions determine whether a player has a newly available
    # move that has recently been recognized (recent in-so-far as it has
    # not yet been consumed via the Pop* methods).
    def GetP1HasNewGesture(self):
        return (self._p1LastUnconsumedGesture != GestureRecognizer.NO_GESTURE)
    def GetP2HasNewGesture(self):
        return (self._p2LastUnconsumedGesture != GestureRecognizer.NO_GESTURE)
    
    # These functions will 'consume' any recent recognized gestures
    # for either player if any are available and return the high-level
    # enumeration for the gesture
    def PopP1Gesture(self):
        temp = self._p1LastUnconsumedGesture
        self._p1LastUnconsumedGesture = GestureRecognizer.NO_GESTURE
        return temp
    def PopP2Gesture(self):
        temp = self._p2LastUnconsumedGesture
        self._p2LastUnconsumedGesture = GestureRecognizer.NO_GESTURE
        return temp    
    
    # Update the gesture recognizer with a new 'frame' of data,
    # any piece of the provided data may be equal to None, in such
    # a case that value is maintained/unchanged within the recognizer
    # from the last time it was set 
    def UpdateWithGestureData(self, p1LGloveData, p1RGloveData,
                                    p2LGloveData, p2RGloveData, dT, timeStamp):
        
        self._p1GestureState.Update(p1LGloveData, p1RGloveData, dT, timeStamp)
        self._p2GestureState.Update(p2LGloveData, p2RGloveData, dT, timeStamp)
        
    def _ChangeP1State(self, newState):
        self._p1GestureState = newState
    def _ChangeP2State(self, newState):
        self._p2GestureState = newState