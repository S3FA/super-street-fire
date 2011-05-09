'''
player.py

Holds the class that represents a player in the Super Street Fire Game.
Players have a number (1 or 2) and a total number of hitpoints (which is also
a percentage of their total life).

@author: Callum Hay
'''
import logging

class Player:
    LOGGER_NAME    = 'player_logger'
    MAX_HIT_POINTS = 100
    
    def __init__(self, playerNum):
        assert(playerNum == 1 or playerNum == 2)
        self.playerNum    = playerNum
        self._logger      = logging.getLogger(Player.LOGGER_NAME)
        self.Reset()
        
    def Reset(self):
        self.ResetHealth()
        self.numRoundWins = 0
        self.isInvincible = False
    
    def ResetHealth(self):
        self.hitPoints = Player.MAX_HIT_POINTS
    
    def IsKnockedOut(self):
        return (self.hitPoints <= 0)
    
    def DoDamage(self, hpDamage):
        if self.isInvincible:
            return
        self.hitPoints -= hpDamage
        self._logger.debug("Player " + str(self.playerNum) + " has been damaged (damage amt = " + \
                           str(hpDamage) + ", life left = " + str(self.hitPoints) + ")")
        
def GetOtherPlayerNum(playerNum):
    if playerNum == 1:
        return 2
    elif playerNum == 2:
        return 1
    else:
        assert(False)