'''
fire_emitter_states.py

@author: Callum Hay
'''

import logging

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
        self._fireEmitter._SetState(P1AttackFireOnState(self._fireEmitter))
    def TurnOnP1Block(self):
        self._fireEmitter._SetState(P1BlockFireOnState(self._fireEmitter))
    def TurnOnP2Attack(self):
        self._fireEmitter._SetState(P2AttackFireOnState(self._fireEmitter))
    def TurnOnP2Block(self):
        self._fireEmitter._SetState(P2BlockFireOnState(self._fireEmitter))
        
    # All the TurnOff* states are ignored since everything is already off


# BlockedFireState represents a state where (part of - i.e., one emitter of) 
# the attack from one player was blocked by the other player.
# NOTE: This is currently NOT a sustained - the state will immediately transition
# to another specified state, this state exists incase we want to do something fancy/
# special when a block occurs
class BlockedFireState(FireState):
    def __init__(self, fireEmitter, nextState):
        FireState.__init__(self, fireEmitter)
        # Currently the blocked state does nothing: it immediately
        # jumps to the next state...
        self._nextState = nextState
    
    def StartState(self):
        self._logger.debug("Entering Blocking State (Emitter: " + \
                           self._fireEmitter.arc + " #" + str(self._fireEmitter.arcIndex) + ")")
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
    def __init__(self, fireEmitter):
        FireState.__init__(self, fireEmitter)
        
    def StartState(self):
        self._logger.debug("Entering Player 1 Attack Fire On State (Emitter: " + \
                           self._fireEmitter.arc + " #" + str(self._fireEmitter.arcIndex) + ")")
        self._fireEmitter._TurnFireOnWithColour(1)
        self._fireEmitter._TurnP2ColourOff()
        
    def TurnOnP1Attack(self): pass
    def TurnOnP1Block(self):
        self._fireEmitter._SetState(P1AttackAndBlockFireOnState(self._fireEmitter))
    def TurnOnP2Attack(self):
        self._fireEmitter._SetState(P1AndP2AttackFireOnState(self._fireEmitter))
    def TurnOnP2Block(self):
        self._fireEmitter._SetState(BlockedFireState(self._fireEmitter, FireOffState(self._fireEmitter)))
    
    def TurnOffP1Attack(self):
        self._fireEmitter._SetState(FireOffState(self._fireEmitter))
    def TurnOffP1Block(self):  pass
    def TurnOffP2Attack(self): pass
    def TurnOffP2Block(self):  pass
    
    def GetPlayerOwners(self): return [1]
    def GetAttackOwners(self): return [1]

# P2AttackFireOnState represents a state where only player 2 owns an attack
# on the fire emitter 
class P2AttackFireOnState(FireState):
    def __init__(self, fireEmitter):
        FireState.__init__(self, fireEmitter)

    def StartState(self):
        self._logger.debug("Entering Player 2 Attack Fire On State (Emitter: " + \
                           self._fireEmitter.arc + " #" + str(self._fireEmitter.arcIndex) + ")")
        self._fireEmitter._TurnFireOnWithColour(2)
        self._fireEmitter._TurnP1ColourOff()
        
    def TurnOnP1Attack(self):
        self._fireEmitter._SetState(P1AndP2AttackFireOnState(self._fireEmitter))
    def TurnOnP1Block(self):
        self._fireEmitter._SetState(BlockedFireState(self._fireEmitter, FireOffState(self._fireEmitter)))
    def TurnOnP2Attack(self): pass
    def TurnOnP2Block(self):
        self._fireEmitter._SetState(P2AttackAndBlockFireOnState(self._fireEmitter))
    
    def TurnOffP1Attack(self): pass
    def TurnOffP1Block(self):  pass
    def TurnOffP2Attack(self):
        self._fireEmitter._SetState(FireOffState(self._fireEmitter))
    def TurnOffP2Block(self):  pass

    def GetPlayerOwners(self): return [2]
    def GetAttackOwners(self): return [2]

# P1AndP2AttackFireOnState represents a state where both player 1 and player 2 own an attack
# on the fire emitter - for example, when both players have attacks on the same arc of fire emitters
# the two flame colours will cross over each other as they move to towards their opposite sides
class P1AndP2AttackFireOnState(FireState):
    def __init__(self, fireEmitter):
        FireState.__init__(self, fireEmitter)

    def StartState(self):
        self._logger.debug("Entering Player 1 and Player 2 Attack Fire On State (Emitter: " + \
                           self._fireEmitter.arc + " #" + str(self._fireEmitter.arcIndex) + ")")
        self._fireEmitter._TurnFireOn()
        self._fireEmitter._TurnP1ColourOn()
        self._fireEmitter._TurnP2ColourOn()
        
    def TurnOnP1Attack(self): pass
    def TurnOnP1Block(self):
        self._fireEmitter._SetState(BlockedFireState(self._fireEmitter, P1AttackFireOnState(self._fireEmitter)))
    def TurnOnP2Attack(self): pass
    def TurnOnP2Block(self):
        self._fireEmitter._SetState(BlockedFireState(self._fireEmitter, P2AttackFireOnState(self._fireEmitter)))
    
    def TurnOffP1Attack(self):
        self._fireEmitter._SetState(P2AttackFireOnState(self._fireEmitter))
    def TurnOffP1Block(self):  pass
    def TurnOffP2Attack(self):
        self._fireEmitter._SetState(P1AttackFireOnState(self._fireEmitter))
    def TurnOffP2Block(self):  pass
    
    def GetPlayerOwners(self): return [1, 2]
    def GetAttackOwners(self): return [1, 2]

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
        self._fireEmitter._SetState(P1AttackAndBlockFireOnState(self._fireEmitter))
    def TurnOnP1Block(self): pass
    def TurnOnP2Attack(self):
        self._fireEmitter._SetState(BlockedFireState(self._fireEmitter, FireOffState(self._fireEmitter)))
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
        self._fireEmitter._SetState(BlockedFireState(self._fireEmitter, FireOffState(self._fireEmitter)))
    def TurnOnP1Block(self): assert(False)
    def TurnOnP2Attack(self):
        self._fireEmitter._SetState(P2AttackAndBlockFireOnState(self._fireEmitter))
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
    def __init__(self, fireEmitter):
        FireState.__init__(self, fireEmitter)
        
    def StartState(self):
        self._logger.debug("Entering Player 1 Attack and Block Fire On State (Emitter: " + \
                           self._fireEmitter.arc + " #" + str(self._fireEmitter.arcIndex) + ")")
        self._fireEmitter._TurnFireOnWithColour(1)
        self._fireEmitter._TurnP2ColourOff()
        
    def TurnOnP1Attack(self): pass
    def TurnOnP1Block(self):  pass
    def TurnOnP2Attack(self):
        self._fireEmitter._SetState(BlockedFireState(self._fireEmitter, P1AttackFireOnState(self._fireEmitter)))
    def TurnOnP2Block(self):  assert(False)
    
    def TurnOffP1Attack(self):
        self._fireEmitter._SetState(P1BlockFireOnState(self._fireEmitter))
    def TurnOffP1Block(self):
        self._fireEmitter._SetState(P1AttackFireOnState(self._fireEmitter))
    def TurnOffP2Attack(self): pass
    def TurnOffP2Block(self):  pass
    
    def GetPlayerOwners(self): return [1]  
    def GetAttackOwners(self): return [1]
    def GetBlockOwners(self):  return [1]

# P2AttackAndBlockFireOnState represents a state where only player 2 owns
# both an attack and a block on the fire emitter - for example, this may happen
# when a player attacks and then throws up a block on the same arc of fire emitters 
class P2AttackAndBlockFireOnState(FireState):
    def __init__(self, fireEmitter):
        FireState.__init__(self, fireEmitter)

    def StartState(self):
        self._logger.debug("Entering Player 2 Attack and Block Fire On State (Emitter: " + \
                           self._fireEmitter.arc + " #" + str(self._fireEmitter.arcIndex) + ")")
        self._fireEmitter._TurnFireOnWithColour(2)
        self._fireEmitter._TurnP1ColourOff()

    def TurnOnP1Attack(self):
        self._fireEmitter._SetState(BlockedFireState(self._fireEmitter, P2AttackFireOnState(self._fireEmitter)))
    def TurnOnP1Block(self):  assert(False)
    def TurnOnP2Attack(self): pass
    def TurnOnP2Block(self):  pass
    
    def TurnOffP1Attack(self): pass
    def TurnOffP1Block(self):  pass
    def TurnOffP2Attack(self):
        self._fireEmitter._SetState(P2BlockFireOnState(self._fireEmitter))
    def TurnOffP2Block(self):   
        self._fireEmitter._SetState(P2AttackFireOnState(self._fireEmitter))

    def GetPlayerOwners(self): return [2]
    def GetAttackOwners(self): return [2]   
    def GetBlockOwners(self):  return [2]

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
    
