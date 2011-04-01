'''
idle_gesture_state.py

A gesture state representing an idle positioning of both of the
player's hands.

@author: Callum Hay
'''

from gesture_state import GestureState

class IdleGestureState(GestureState):

    def __init__(self, gestureRecognizer, playerNum):
        GestureState.__init__(self, gestureRecognizer, playerNum)
        
        
    # Inherited/Override methods from GestureState
    def Update(self, leftGloveData, rightGloveData, dT, timeStamp):
        # TODO... the hard part
        pass