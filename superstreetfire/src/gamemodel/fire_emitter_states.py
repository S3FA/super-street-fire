'''
fire_emitter_states.py

@author: Callum Hay
'''

import logging
import time

# This is the 'abstract' superclass for all fire emitter states
class FireState:
    LOGGER_NAME = 'fire_state_logger'
    
    def __init__(self, fireEmitter):
        assert(fireEmitter != None)
        self._fireEmitter = fireEmitter
        self._logger = logging.getLogger(FireState.LOGGER_NAME)
        pass

    # Initializer function - called when this state is set in the owning FireEmitter object
    def StartState(self): pass

    # Event functions - these are called to alter the state when an external event happens
    def TurnOnP1Attack(self): pass
    def TurnOnP1Block(self):  pass
    def TurnOnP2Attack(self): pass
    def TurnOnP2Block(self):  pass
    
    def TurnOffP1Attack(self): pass
    def TurnOffP1Block(self):  pass
    def TurnOffP2Attack(self): pass
    def TurnOffP2Block(self):  pass

    def GetPlayerOwners(self): return [] # Gets a list of players that are associated with this state
    def GetAttackOwners(self): return [] # Gets a list of players that are associated with an attack in this state
    def GetBlockOwners(self):  return [] # Gets a list of players that are associated with a block in this state

# FireOffState represents a state where the fire is completely turned off
class FireOffState(FireState):
    def __init__(self, fireEmitter):
        FireState.__init__(self, fireEmitter)
        
    def StartState(self):
        self._logger.debug("Entering Fire Off State (Emitter: " + \
                           self._fireEmitter.arc + " #" + str(self._fireEmitter.arcIndex) + ")")
        self._fireEmitter._TurnFireOff()
        
    def TurnOnP1Attack(self):
        self._fireEmitter._SetState(P1AttackFireOnState(self._fireEmitter, 0.0))
    def TurnOnP1Block(self):
        self._fireEmitter._SetState(P1BlockFireOnState(self._fireEmitter))
    def TurnOnP2Attack(self):
        self._fireEmitter._SetState(P2AttackFireOnState(self._fireEmitter, 0.0))
    def TurnOnP2Block(self):
        self._fireEmitter._SetState(P2BlockFireOnState(self._fireEmitter))
        
    # All the TurnOff* states are ignored since everything is already off


# BlockedFireState represents a state where (part of - i.e., one emitter of) 
# the attack from one player was blocked by the other player.
# NOTE: This is currently NOT a sustained state - the state will immediately transition
# to another specified state, this state exists incase we want to do something fancy/
# special when a block occurs
class BlockedFireState(FireState):
    
    # The constructor for BlockedFireState
    # fireEmitter - The FireEmitter object that owns this state
    # nextState   - The next state that the FireEmitter will go to immediately
    #               when StartState is called.
    # elapsedAtkTimeBeforeBlk - The time (in seconds) that elapsed that an attack was active
    # before the block occurred.
    # If this value is > 0 then it means that an attack was present on the emitter before the
    # block was that caused this state.
    def __init__(self, fireEmitter, nextState, elapsedAtkTimeBeforeBlk=0):
        FireState.__init__(self, fireEmitter)
        # Currently the blocked state does nothing: it immediately
        # jumps to the next state...
        self._nextState = nextState
        self._elapsedAtkTimeBeforeBlk = elapsedAtkTimeBeforeBlk
        
    def StartState(self):
        self._logger.debug("Entering Blocking State (Emitter: " + \
                           self._fireEmitter.arc + " #" + str(self._fireEmitter.arcIndex) + ")")
        self._logger.debug("Elapsed attack time before this block: " + str(self._elapsedAtkTimeBeforeBlk))
        self._fireEmitter._SetState(self._nextState)
        
    def TurnOnP1Attack(self): assert(False)
    def TurnOnP1Block(self):  assert(False)
    def TurnOnP2Attack(self): assert(False)
    def TurnOnP2Block(self):  assert(False)
    
    def TurnOffP1Attack(self): assert(False)
    def TurnOffP1Block(self):  assert(False)
    def TurnOffP2Attack(self): assert(False)
    def TurnOffP2Block(self):  assert(False)

# P1AttackFireOnState represents a state where only player 1 owns an attack
# on the fire emitter 
class P1AttackFireOnState(FireState):
    
    # Constructor for P1AttackFireOnState
    # fireEmitter - The FireEmitter object that owns this state
    # prevAtkTime - The total time that an attack has so far been active on this emitter
    # that has been owned by player 1 (used to track time between attacks and blocks if necessary)
    def __init__(self, fireEmitter, prevAtkTime):
        FireState.__init__(self, fireEmitter)
        self._prevAtkTime = prevAtkTime
        
    def StartState(self):
        self._startStateTime = time.time()
        self._logger.debug("Entering Player 1 Attack Fire On State (Emitter: " + \
                           self._fireEmitter.arc + " #" + str(self._fireEmitter.arcIndex) + ")")
        self._fireEmitter._TurnFireOnWithColour(1)
        self._fireEmitter._TurnP2ColourOff()

    def TurnOnP1Attack(self): pass
    def TurnOnP1Block(self):
        totalP1AtkTime = self._GetTotalAtkTime()
        self._fireEmitter._SetState(P1AttackAndBlockFireOnState(self._fireEmitter, totalP1AtkTime))
    def TurnOnP2Attack(self):
        totalP1AtkTime = self._GetTotalAtkTime()
        self._fireEmitter._SetState(P1AndP2AttackFireOnState(self._fireEmitter, totalP1AtkTime, 0.0))
    def TurnOnP2Block(self):
        totalP1AtkTime = self._GetTotalAtkTime()
        self._fireEmitter._SetState(BlockedFireState(self._fireEmitter, \
                                                     P2BlockFireOnState(self._fireEmitter), \
                                                     totalP1AtkTime))
    
    def TurnOffP1Attack(self):
        self._fireEmitter._SetState(FireOffState(self._fireEmitter))
    def TurnOffP1Block(self):  pass
    def TurnOffP2Attack(self): pass
    def TurnOffP2Block(self):  pass
    
    def GetPlayerOwners(self): return [1]
    def GetAttackOwners(self): return [1]

    def _GetTotalAtkTime(self):
        return self._prevAtkTime + (time.time() - self._stateStartTime)
        
# P2AttackFireOnState represents a state where only player 2 owns an attack
# on the fire emitter 
class P2AttackFireOnState(FireState):
    
    # Constructor for P2AttackFireOnState
    # fireEmitter - The FireEmitter object that owns this state
    # prevAtkTime - The total time that an attack has so far been active on this emitter
    # that has been owned by player 2 (used to track time between attacks and blocks if necessary)    
    def __init__(self, fireEmitter, prevAtkTime):
        FireState.__init__(self, fireEmitter)
        self._prevAtkTime = prevAtkTime

    def StartState(self):
        self._startStateTime = time.time()
        self._logger.debug("Entering Player 2 Attack Fire On State (Emitter: " + \
                           self._fireEmitter.arc + " #" + str(self._fireEmitter.arcIndex) + ")")
        self._fireEmitter._TurnFireOnWithColour(2)
        self._fireEmitter._TurnP1ColourOff()
        
    def TurnOnP1Attack(self):
        totalP2AtkTime = self._GetTotalAtkTime()
        self._fireEmitter._SetState(P1AndP2AttackFireOnState(self._fireEmitter, 0.0, totalP2AtkTime))
    def TurnOnP1Block(self):
        totalP2AtkTime = self._GetTotalAtkTime()
        self._fireEmitter._SetState(BlockedFireState(self._fireEmitter, \
                                                     P1BlockFireOnState(self._fireEmitter), \
                                                     totalP2AtkTime))
    def TurnOnP2Attack(self): pass
    def TurnOnP2Block(self):
        totalP2AtkTime = self._GetTotalAtkTime()
        self._fireEmitter._SetState(P2AttackAndBlockFireOnState(self._fireEmitter, totalP2AtkTime))
    
    def TurnOffP1Attack(self): pass
    def TurnOffP1Block(self):  pass
    def TurnOffP2Attack(self):
        self._fireEmitter._SetState(FireOffState(self._fireEmitter))
    def TurnOffP2Block(self):  pass

    def GetPlayerOwners(self): return [2]
    def GetAttackOwners(self): return [2]

    def _GetTotalAtkTime(self):
        return self._prevAtkTime + (time.time() - self._stateStartTime)

# P1AndP2AttackFireOnState represents a state where both player 1 and player 2 own an attack
# on the fire emitter - for example, when both players have attacks on the same arc of fire emitters
# the two flame colours will cross over each other as they move to towards their opposite sides
class P1AndP2AttackFireOnState(FireState):
    
    # Constructor for P1AndP2AttackFireOnState
    # fireEmitter - The FireEmitter object that owns this state
    # prevP1AtkTime - The total time that an attack has so far been active on this emitter
    # that has been owned by player 1 (used to track time between attacks and blocks if necessary)
    # prevP2AtkTime - ditto but for player 2    
    def __init__(self, fireEmitter, prevP1AtkTime, prevP2AtkTime):
        FireState.__init__(self, fireEmitter)
        self._prevP1AtkTime = prevP1AtkTime
        self._prevP2AtkTime = prevP2AtkTime

    def StartState(self):
        self._startStateTime = time.time()
        self._logger.debug("Entering Player 1 and Player 2 Attack Fire On State (Emitter: " + \
                           self._fireEmitter.arc + " #" + str(self._fireEmitter.arcIndex) + ")")
        self._fireEmitter._TurnFireOn()
        self._fireEmitter._TurnP1ColourOn()
        self._fireEmitter._TurnP2ColourOn()
        
    def TurnOnP1Attack(self): pass
    def TurnOnP1Block(self):
        totalP1AtkTime = self._GetTotalP1AtkTime()
        totalP2AtkTime = self._GetTotalP2AtkTime()
        self._fireEmitter._SetState(BlockedFireState(self._fireEmitter, \
                                                     P1AttackAndBlockFireOnState(self._fireEmitter, totalP1AtkTime), \
                                                     totalP2AtkTime))
    def TurnOnP2Attack(self): pass
    def TurnOnP2Block(self):
        totalP1AtkTime = self._GetTotalP1AtkTime()
        totalP2AtkTime = self._GetTotalP2AtkTime()        
        self._fireEmitter._SetState(BlockedFireState(self._fireEmitter, \
                                                     P2AttackAndBlockFireOnState(self._fireEmitter, totalP2AtkTime), \
                                                     totalP1AtkTime))
    
    def TurnOffP1Attack(self):
        totalP2AtkTime = self._GetTotalP2AtkTime()  
        self._fireEmitter._SetState(P2AttackFireOnState(self._fireEmitter, totalP2AtkTime))
    def TurnOffP1Block(self):  pass
    def TurnOffP2Attack(self):
        totalP1AtkTime = self._GetTotalP1AtkTime()
        self._fireEmitter._SetState(P1AttackFireOnState(self._fireEmitter, totalP1AtkTime))
    def TurnOffP2Block(self):  pass
    
    def GetPlayerOwners(self): return [1, 2]
    def GetAttackOwners(self): return [1, 2]

    def _GetTotalP1AtkTime(self):
        return self._prevP1AtkTime + (time.time() - self._stateStartTime)
    def _GetTotalP2AtkTime(self):
        return self._prevP2AtkTime + (time.time() - self._stateStartTime)

# P1BlockFireOnState represents a state where only player 1 owns a block
# on the fire emitter 
class P1BlockFireOnState(FireState):
    def __init__(self, fireEmitter):
        FireState.__init__(self, fireEmitter)

    def StartState(self):
        self._logger.debug("Entering Player 1 Block Fire On State (Emitter: " + \
                           self._fireEmitter.arc + " #" + str(self._fireEmitter.arcIndex) + ")")
        self._fireEmitter._TurnFireOnWithColour(1)
        self._fireEmitter._TurnP2ColourOff()

    def TurnOnP1Attack(self):
        self._fireEmitter._SetState(P1AttackAndBlockFireOnState(self._fireEmitter, 0.0))
    def TurnOnP1Block(self): pass
    def TurnOnP2Attack(self):
        self._fireEmitter._SetState(BlockedFireState(self._fireEmitter, \
                                                     P1BlockFireOnState(self._fireEmitter)))
    def TurnOnP2Block(self): assert(False)
    
    def TurnOffP1Attack(self): pass
    def TurnOffP1Block(self):
        self._fireEmitter._SetState(FireOffState(self._fireEmitter))
    def TurnOffP2Attack(self): pass
    def TurnOffP2Block(self):  pass
    
    def GetPlayerOwners(self): return [1]
    def GetBlockOwners(self):  return [1]

# P2BlockFireOnState represents a state where only player 2 owns a block
# on the fire emitter 
class P2BlockFireOnState(FireState):
    def __init__(self, fireEmitter):
        FireState.__init__(self, fireEmitter)

    def StartState(self):
        self._logger.debug("Entering Player 2 Block Fire On State (Emitter: " + \
                           self._fireEmitter.arc + " #" + str(self._fireEmitter.arcIndex) + ")")
                           
        self._fireEmitter._TurnFireOnWithColour(2)
        self._fireEmitter._TurnP1ColourOff()

    def TurnOnP1Attack(self):
        self._fireEmitter._SetState(BlockedFireState(self._fireEmitter, \
                                                     P2BlockFireOnState(self._fireEmitter)))
    def TurnOnP1Block(self): assert(False)
    def TurnOnP2Attack(self):
        self._fireEmitter._SetState(P2AttackAndBlockFireOnState(self._fireEmitter, 0.0))
    def TurnOnP2Block(self): pass
    
    def TurnOffP1Attack(self): pass
    def TurnOffP1Block(self):  pass
    def TurnOffP2Attack(self): pass
    def TurnOffP2Block(self):
        self._fireEmitter._SetState(FireOffState(self._fireEmitter))
    
    def GetPlayerOwners(self): return [2]
    def GetBlockOwners(self):  return [2]

# P1AttackAndBlockFireOnState represents a state where only player 1 owns
# both an attack and a block on the fire emitter - for example, this may happen
# when a player attacks and then throws up a block on the same arc of fire emitters 
class P1AttackAndBlockFireOnState(FireState):
    
    # Constructor for P1AttackAndBlockFireOnState
    # fireEmitter - The FireEmitter object that owns this state
    # prevAtkTime - The total time that an attack has so far been active on this emitter
    # that has been owned by player 1 (used to track time between attacks and blocks if necessary)    
    def __init__(self, fireEmitter, prevAtkTime):
        FireState.__init__(self, fireEmitter)
        self._prevAtkTime = prevAtkTime
        
    def StartState(self):
        self._stateStartTime = time.time()
        self._logger.debug("Entering Player 1 Attack and Block Fire On State (Emitter: " + \
                           self._fireEmitter.arc + " #" + str(self._fireEmitter.arcIndex) + ")")
        self._fireEmitter._TurnFireOnWithColour(1)
        self._fireEmitter._TurnP2ColourOff()
        
    def TurnOnP1Attack(self): pass
    def TurnOnP1Block(self):  pass
    def TurnOnP2Attack(self):
        totalP1AtkTime = self._GetTotalAtkTime()
        self._fireEmitter._SetState(BlockedFireState(self._fireEmitter, \
                                                     P1AttackAndBlockFireOnState(self._fireEmitter, totalP1AtkTime)))
    def TurnOnP2Block(self):  assert(False)
    
    def TurnOffP1Attack(self):
        self._fireEmitter._SetState(P1BlockFireOnState(self._fireEmitter))
    def TurnOffP1Block(self):
        totalP1AtkTime = self._GetTotalAtkTime()
        self._fireEmitter._SetState(P1AttackFireOnState(self._fireEmitter, totalP1AtkTime))
    def TurnOffP2Attack(self): pass
    def TurnOffP2Block(self):  pass
    
    def GetPlayerOwners(self): return [1]  
    def GetAttackOwners(self): return [1]
    def GetBlockOwners(self):  return [1]

    def _GetTotalAtkTime(self):
        return self._prevAtkTime + (time.time() - self._stateStartTime)


# P2AttackAndBlockFireOnState represents a state where only player 2 owns
# both an attack and a block on the fire emitter - for example, this may happen
# when a player attacks and then throws up a block on the same arc of fire emitters 
class P2AttackAndBlockFireOnState(FireState):
    
    # Constructor for P2AttackAndBlockFireOnState
    # fireEmitter - The FireEmitter object that owns this state
    # prevAtkTime - The total time that an attack has so far been active on this emitter
    # that has been owned by player 2 (used to track time between attacks and blocks if necessary)      
    def __init__(self, fireEmitter, prevAtkTime):
        FireState.__init__(self, fireEmitter)
        self._prevAtkTime = prevAtkTime

    def StartState(self):
        self._stateStartTime = time.time()
        self._logger.debug("Entering Player 2 Attack and Block Fire On State (Emitter: " + \
                           self._fireEmitter.arc + " #" + str(self._fireEmitter.arcIndex) + ")")
        self._fireEmitter._TurnFireOnWithColour(2)
        self._fireEmitter._TurnP1ColourOff()

    def TurnOnP1Attack(self):
        totalP2AtkTime = self._GetTotalAtkTime()
        self._fireEmitter._SetState(BlockedFireState(self._fireEmitter, \
                                                     P2AttackAndBlockFireOnState(self._fireEmitter, totalP2AtkTime)))
    def TurnOnP1Block(self):  assert(False)
    def TurnOnP2Attack(self): pass
    def TurnOnP2Block(self):  pass
    
    def TurnOffP1Attack(self): pass
    def TurnOffP1Block(self):  pass
    def TurnOffP2Attack(self):
        self._fireEmitter._SetState(P2BlockFireOnState(self._fireEmitter))
    def TurnOffP2Block(self):
        totalP2AtkTime = self._GetTotalAtkTime()
        self._fireEmitter._SetState(P2AttackFireOnState(self._fireEmitter, totalP2AtkTime))

    def GetPlayerOwners(self): return [2]
    def GetAttackOwners(self): return [2]   
    def GetBlockOwners(self):  return [2]

    def _GetTotalAtkTime(self):
        return self._prevAtkTime + (time.time() - self._stateStartTime)

if __name__ == "__main__":
    from fire_emitter import FireEmitter
    
    logger = logging.getLogger('fire_state_logger')
    logger.setLevel(logging.DEBUG)
    logger.addHandler(logging.StreamHandler())
    
    fireEmitter = FireEmitter(0) # Fire OFF state
    assert(fireEmitter.FireOn(1, FireEmitter.ATTACK_FLAME) == True) # Player 1 Attack state
    assert(fireEmitter.FireOn(2, FireEmitter.ATTACK_FLAME) == True) # Player 1 & 2 attack state
    assert(fireEmitter.FireOn(1, FireEmitter.BLOCK_FLAME)  == True) # Blocking ---> Player 1 Attack state
    fireEmitter.FireOff(1, FireEmitter.ATTACK_FLAME)                # Fire OFF state
    
    assert(fireEmitter.FireOn(1, FireEmitter.BLOCK_FLAME)  == True)  # Player 1 Block state
    assert(fireEmitter.FireOn(2, FireEmitter.ATTACK_FLAME) == False) # Blocking ---> Fire OFF state
    
    #assert(fireEmitter.FireOn())
    
