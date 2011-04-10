'''
Created on Apr 10, 2011

@author: callumhay
'''

import logging

class FireState:
    def __init__(self, fireEmitter):
        assert(fireEmitter != None)
        self._fireEmitter = fireEmitter
        self._logger = logging.getLogger('fire_state_logger')
        pass

    def StartState(self): pass

    def TurnOnP1Attack(self): pass
    def TurnOnP1Block(self):  pass
    def TurnOnP2Attack(self): pass
    def TurnOnP2Block(self):  pass
    
    def TurnOffP1Attack(self): pass
    def TurnOffP1Block(self):  pass
    def TurnOffP2Attack(self): pass
    def TurnOffP2Block(self):  pass

    def GetPlayerOwners(self): return []

class FireOffState(FireState):
    def __init__(self, fireEmitter):
        FireState.__init__(self, fireEmitter)
        
    def StartState(self):
        self._logger.debug("Entering Fire Off State")
        self._fireEmitter._TurnFireOff()
        
    def TurnOnP1Attack(self):
        self._fireEmitter._SetState(P1AttackFireOnState(self._fireEmitter))
    def TurnOnP1Block(self):
        self._fireEmitter._SetState(P1BlockFireOnState(self._fireEmitter))
    def TurnOnP2Attack(self):
        self._fireEmitter._SetState(P2AttackFireOnState(self._fireEmitter))
    def TurnOnP2Block(self):
        self._fireEmitter._SetState(P2BlockFireOnState(self._fireEmitter))

class BlockedFireState(FireState):
    def __init__(self, fireEmitter, nextState):
        FireState.__init__(self, fireEmitter)
        # Currently the blocked state does nothing: it immediately
        # jumps to the next state...
        self._nextState = nextState
    
    def StartState(self):
        self._logger.debug("Entering Blocking State")
        self._fireEmitter._SetState(self._nextState)
        
    def TurnOnP1Attack(self): assert(False)
    def TurnOnP1Block(self):  assert(False)
    def TurnOnP2Attack(self): assert(False)
    def TurnOnP2Block(self):  assert(False)
    
    def TurnOffP1Attack(self): assert(False)
    def TurnOffP1Block(self):  assert(False)
    def TurnOffP2Attack(self): assert(False)
    def TurnOffP2Block(self):  assert(False)
    
class P1AttackFireOnState(FireState):
    def __init__(self, fireEmitter):
        FireState.__init__(self, fireEmitter)
        
    def StartState(self):
        self._logger.debug("Entering Player 1 Attack Fire On State")
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
    
class P2AttackFireOnState(FireState):
    def __init__(self, fireEmitter):
        FireState.__init__(self, fireEmitter)

    def StartState(self):
        self._logger.debug("Entering Player 2 Attack Fire On State")
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

class P1AndP2AttackFireOnState(FireState):
    def __init__(self, fireEmitter):
        FireState.__init__(self, fireEmitter)

    def StartState(self):
        self._logger.debug("Entering Player 1 and Player 2 Attack Fire On State")
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

class P1BlockFireOnState(FireState):
    def __init__(self, fireEmitter):
        FireState.__init__(self, fireEmitter)

    def StartState(self):
        self._logger.debug("Entering Player 1 Block Fire On State")
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
    
class P2BlockFireOnState(FireState):
    def __init__(self, fireEmitter):
        FireState.__init__(self, fireEmitter)

    def StartState(self):
        self._logger.debug("Entering Player 2 Block Fire On State")
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

class P1AttackAndBlockFireOnState(FireState):
    def __init__(self, fireEmitter):
        FireState.__init__(self, fireEmitter)
        
    def StartState(self):
        self._logger.debug("Entering Player 1 Attack and Block Fire On State")
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
    
class P2AttackAndBlockFireOnState(FireState):
    def __init__(self, fireEmitter):
        FireState.__init__(self, fireEmitter)

    def StartState(self):
        self._logger.debug("Entering Player 2 Attack and Block Fire On State")
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

if __name__ == "__main__":
    from fire_emitter import FireEmitter
    
    logger = logging.getLogger('fire_state_logger')
    logger.setLevel(logging.DEBUG)
    logger.addHandler(logging.StreamHandler())
    
    fireEmitter = FireEmitter(0)
    assert(fireEmitter.FireOn(1, FireEmitter.ATTACK_FLAME) == True)
    assert(fireEmitter.FireOn(2, FireEmitter.ATTACK_FLAME) == True)
    assert(fireEmitter.FireOn(1, FireEmitter.BLOCK_FLAME)  == True)
    #assert(fireEmitter.FireOn())
    
