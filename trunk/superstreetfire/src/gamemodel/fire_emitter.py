'''
fire_emitter.py

Contains a class FireEmitter for representing a Fire Effect that
can shoot fire to indicate the current attack/defend state of the
Super Street Fire game.

@author: Callum Hay
'''

class FireEmitter:
    # State Constants:
    # The emitter can be in the following states
    TURNED_OFF_STATE = 0
    TURNED_ON_STATE  = 1
    #ARMED_STATE ?
    
    # Speed/Time Constraint Constants:
    #MIN_FLAME_ON_TIME = 1.0
    #MAX_FLAME_ON_TIME = 2.0
    
    def __init__(self, idx):
        # Keep this emitter's index within its arc of emitters (starting at zero)
        self.arcIndex = idx
        
        # Hold a mapping of each state to its associated "Tick" function
        self.STATE_TO_FUNC_DICT = {
            FireEmitter.TURNED_OFF_STATE : self._TurnedOnStateTick,
            FireEmitter.TURNED_ON_STATE  : self._TurnedOffStateTick                   
        }
               
        self.Reset()
        
    def Reset(self):
        # The initial state is off, for obvious reasons
        self._SetState(FireEmitter.TURNED_OFF_STATE)
        # The fire counter is used when the fire is turned on,
        # we only allow the fire to be on for a specified length of time...
        self._fireOnTimeCounter = 0.0
        self._currFireTime      = 0.0
        
    # Simulate a tick over some given delta time in seconds
    def Tick(self, dT):
        self._stateTickFunc(dT)
    
    # Turn on this fire effect for the given length of time
    # If the fire is already on then this will just keep it on
    # and overwrite the previous time length to the one given
    def TurnFireOn(self, timeLength):
        assert(timeLength >= 0)
        self._currFireTime      = timeLength
        self._fireOnTimeCounter = 0.0
        self._SetState(FireEmitter.TURNED_ON_STATE)

    # Turn off this fire effect, immediately
    def TurnFireOff(self):
        self._currFireTime      = 0.0
        self._fireOnTimeCounter = 0.0
        self._SetState(FireEmitter.TURNED_OFF_STATE)

    # Private Functions *************************************

    def _SetState(self, state):
        self._stateTickFunc = self.STATE_TO_FUNC_DICT.get(state, default=None)
        assert(self._stateTickFunc != None)
        self.state = state

    # Tick function for when the fire is on    
    def _TurnedOnStateTick(self, dT):
        assert(self.state == FireEmitter.TURNED_ON_STATE)
        
        self._fireOnTimeCounter += dT
        if self._fireOnTimeCounter >= self._currFireTime:
            self.TurnFireOff()

    # Tick function for when the fire is off
    def _TurnedOffStateTick(self, dT):
        assert(self.state == FireEmitter.TURNED_OFF_STATE)
        pass # Do nothing