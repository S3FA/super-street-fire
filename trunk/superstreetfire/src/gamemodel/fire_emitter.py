'''
fire_emitter.py

Contains a class FireEmitter for representing a Fire Effect that
can shoot fire to indicate the current attack/defend state of the
Super Street Fire game.

@author: Callum Hay
'''

import player

class FireEmitter:
    # State Constants/Enumeration:
    # The emitter can be in the following states
    TURNED_OFF_STATE = 0
    TURNED_ON_STATE  = 1
    
    # Enumeration constant for no player fire owners
    NO_PLAYER_OWNER    = 0
    BOTH_PLAYERS_OWNER = 3
    
    # Enumeration constants to describe the nature/type of the Emitter's flame :
    # The emitter can either have no nature at all (because it's not on)
    # or it can either be an 'attack' flame or a 'block' flame
    NO_FLAME     = 0
    BLOCK_FLAME  = 1
    ATTACK_FLAME = 2
    
    def __init__(self, idx):
        # Keep this emitter's index within its arc of emitters (starting at zero)
        self.arcIndex   = idx
        self._playerNum = FireEmitter.NO_PLAYER_OWNER
        self._flameType = FireEmitter.NO_FLAME
        self.state      = FireEmitter.TURNED_OFF_STATE

        self.Reset()
    
    def SetPrevAndNextEmitters(self, prevEmitter, nextEmitter):
        assert(prevEmitter != None or nextEmitter != None)
        self.prevEmitter = prevEmitter
        self.nextEmitter = nextEmitter
        
    def Reset(self):
        # The initial state is off, for obvious reasons
        self.KillFire()

    # Turn on this fire effect for the given player with the given flame type
    # Returns: True if the emitter was turned on by this call, False if not
    def TurnFireOn(self, playerNum, flameType):
        assert(flameType == FireEmitter.BLOCK_FLAME or flameType == FireEmitter.ATTACK_FLAME)
        assert(playerNum == 1 or playerNum == 2)
        
        # First check to see whether this flame is currently turned on and active
        # for some other purpose...
        if self.state == FireEmitter.TURNED_ON_STATE:
            # The flame is already on - if it's turned on for the player that requested
            # it then we don't need to do anything extra - however, if the player is different
            # then we may be encountering a block or attack from the another player 
            if self._playerNum != playerNum:
                
                if self._flameType == FireEmitter.BLOCK_FLAME:
                    if flameType == FireEmitter.ATTACK_FLAME:
                        # A block had already existed on this emitter and attack was attempted,
                        # thus, an attack was just blocked
                        self.TurnFireOff()
                        return False
                    else:
                        # This should never happen - can't have blocks from both players in the same emitter
                        assert(False)
                        return False
                    
                elif self._flameType == FireEmitter.ATTACK_FLAME:
                    if flameType == FireEmitter.BLOCK_FLAME:
                        # A attack had already existed on this emitter and a block was just issued
                        # to defend it...
                        
                        # TODO: TIMING???
                        # For now this counts as blocked
                        self.TurnFireOff()
                        return False
                    
                    else flameType == FireEmitter.ATTACK_FLAME:
                        # The attacks from both players are passing through this emitter
                        self._playerNum = FireEmitter.BOTH_PLAYERS_OWNER
                        self._flameType = flameType
                        self._SetState(FireEmitter.TURNED_ON_STATE)
                        return True
            else:
                # The emitter is already turned on for the player
                return True
            
        self._playerNum = playerNum
        self._flameType = flameType
        self._SetState(FireEmitter.TURNED_ON_STATE)
        return True

    def KillFire(self):
        self._playerNum = FireEmitter.NO_PLAYER_OWNER
        self._flameType = FireEmitter.NO_FLAME
        self._SetState(FireEmitter.TURNED_OFF_STATE)

    # Turn off this fire effect for the given player
    def TurnFireOff(self, playerNum):
        assert(playerNum == 1 or playerNum == 2)
        
        # We have to check to see who owns this flame...
        if self._playerNum != FireEmitter.NO_PLAYER_OWNER:
            if self._playerNum == FireEmitter.BOTH_PLAYERS_OWNER:
                # Both players had owned the flame,
                # remove the ownership of the player who just asked to turn off the fire
                self._playerNum = player.GetOtherPlayerNum(playerNum)
                return
            elif self._playerNum != playerNum
                # The other player owns this flame, therefore we just ignore this request...
                return
            
        assert(self._playerNum == FireEmitter.NO_PLAYER_OWNER or \
               self._playerNum == playerNum)
        self.KillFire()

    # Private Functions *************************************

    def _SetState(self, state):
        assert(self._stateTickFunc != None)
        self.state = state


