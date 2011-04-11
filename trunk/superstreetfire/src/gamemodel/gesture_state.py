'''
gesture_state.py

Contains the 'abstract' super-class for all gesture states
that are visited within the GestureRecognizer's state machine.

A gesture state analyzes the overall gesture for the combination
of the left and right hands of a particular player in the
Super Street Fire game.

@author: Callum Hay
'''

import player

class GestureState:
    
    def __init__(self, gestureRecognizer, playerNum):
        assert(gestureRecognizer != None)
        self.gestureRecognizer = gestureRecognizer
        self.playerNum         = playerNum
        self._changeStateFunc  = None
        
        # Set the state change function based on the player number...
        if playerNum == 1:
            self._changeStateFunc = gestureRecognizer._ChangeP1State
        elif playerNum == 2:
            self._changeStateFunc = gestureRecognizer._ChangeP2State
        else:
            assert(False)
            
    # Abstract method used to update the state with new glove data
    def Update(self, leftGloveData, rightGloveData, dT, timeStamp): assert(False)
    
    
    
    
    
    
    