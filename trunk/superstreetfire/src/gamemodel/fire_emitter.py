'''
fire_emitter.py

Contains a class FireEmitter for representing a Fire Effect that
can shoot fire to indicate the current attack/defend state of the
Super Street Fire game.

@author: Callum Hay
'''

import player
from fire_emitter_states import FireOffState 

class FireEmitter:
    NUM_FIRE_EMITTERS_PER_ARC = 8
    TOTAL_NUM_FIRE_EMITTERS   = 2 * NUM_FIRE_EMITTERS_PER_ARC    
    
    RIGHT_ARC = "Right Arc"
    LEFT_ARC  = "Left Arc"
    
    # Enumeration constants for defining types of flames that can be turned on/off
    # by either player during the game
    ATTACK_FLAME = 1
    BLOCK_FLAME  = 2
    
    def __init__(self, idx, arc, listenerCmdr):
        assert(listenerCmdr != None)
        assert(idx >= 0 and idx < FireEmitter.NUM_FIRE_EMITTERS_PER_ARC)
        assert(arc == FireEmitter.RIGHT_ARC or arc == FireEmitter.LEFT_ARC)
        
        self._listenerCmdr = listenerCmdr
        
        # Keep this emitter's index within its arc of emitters (starting at zero)
        self.arcIndex     = idx
        self.arc          = arc
        self.flameIsOn    = False
        self.p1ColourIsOn = False
        self.p2ColourIsOn = False
        self.state        = FireOffState(self)
        self.Reset()
    
    def  __str__(self):
        return 'ardIdx=%d, arc=%s, on=%s, p1C=%s, p2C=%s, state=%s' % (self.arcIndex, 
                self.arc, self.flameIsOn, self.p1ColourIsOn, self.p2ColourIsOn, self.state) 
        
    def Reset(self):
        # The initial state is off, for obvious reasons
        self.Kill()

    def HasAttackFlameOwnedByPlayer(self, playerNum):
        return playerNum in self.state.GetAttackOwners()
    def HasBlockFlameOwnedByPlayer(self, playerNum):
        return playerNum in self.state.GetBlockOwners()

    # Turn on this fire effect for the given player with the given flame type
    # Returns: True if the emitter was turned on by this call, False if not
    def FireOn(self, playerNum, flameType):
        assert(playerNum == 1 or playerNum == 2)
        assert(flameType == FireEmitter.BLOCK_FLAME or flameType == FireEmitter.ATTACK_FLAME)
        
        if playerNum == 1:
            if flameType == FireEmitter.ATTACK_FLAME:
                self.state.TurnOnP1Attack()
            else:
                self.state.TurnOnP1Block()
        else:
            if flameType == FireEmitter.ATTACK_FLAME:
                self.state.TurnOnP2Attack()
            else:
                self.state.TurnOnP2Block()
        
        # Query to see whether the emitter is now on for the given player...
        return self.HasAttackFlameOwnedByPlayer(playerNum)
        
    # Turn off this fire effect for the given player
    def FireOff(self, playerNum, flameType):
        assert(playerNum == 1 or playerNum == 2)
        assert(flameType == FireEmitter.BLOCK_FLAME or flameType == FireEmitter.ATTACK_FLAME)
        
        if playerNum == 1:
            if flameType == FireEmitter.ATTACK_FLAME:
                self.state.TurnOffP1Attack()
            else:
                self.state.TurnOffP1Block()
        else:
            if flameType == FireEmitter.ATTACK_FLAME:
                self.state.TurnOffP2Attack()
            else:
                self.state.TurnOffP2Block()        

    def FireOffNoStateChange(self):
        self._TurnFireOff()        

    # Kill the emitter, and do it NOW!!
    def Kill(self):
        self._SetState(FireOffState(self))
        # Just for good measure...
        self._TurnFireOff()

    # Private Functions *************************************
    
    def _SetState(self, newState):
        assert(self.state != None)
        assert(newState != None)
        # End the previous state, set the new state and start it
        self.state = newState
        self.state.StartState()
        
        self._listenerCmdr.EmitterStateChanged()
        
    # This function will ACTUALLY turn the fire on, should only be called from
    # the fire emitter state machine
    def _TurnFireOn(self):
        if not self.flameIsOn:
            # SEND DATA TO WIFIRE FOR FLAME EFFECT!
            self.flameIsOn = True
    
    def _TurnFireOnWithColour(self, playerNum):
        assert(playerNum == 1 or playerNum == 2)
        self._TurnFireOn()
        if playerNum == 1:
            self._TurnP1ColourOn()
        else:
            self._TurnP2ColourOn()
        #print "turn on ",self.arcIndex," color " , playerNum
    
    # This function will ACTUALLY turn the fire off, should only be called from
    # the fire emitter state machine
    def _TurnFireOff(self):
        # SEND DATA TO WIFIRE FOR FLAME EFFECT!
        self.flameIsOn = False
        self._TurnP1ColourOff()
        self._TurnP2ColourOff()
    
    def _TurnP1ColourOn(self):
        if not self.p1ColourIsOn:
            # SEND DATA TO WIFIRE FOR THE COLOUR!
            self.p1ColourIsOn = True
            
    def _TurnP1ColourOff(self):
        # SEND DATA TO WIFIRE FOR THE COLOUR!
        self.p1ColourIsOn = False
        
    def _TurnP2ColourOn(self):
        if not self.p2ColourIsOn:
            # SEND DATA TO WIFIRE FOR THE COLOUR!
            self.p2ColourIsOn = True
            
    def _TurnP2ColourOff(self):
        # SEND DATA TO WIFIRE FOR THE COLOUR!
        self.p2ColourIsOn = False
    
    