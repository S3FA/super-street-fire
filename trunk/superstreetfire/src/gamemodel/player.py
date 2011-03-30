'''
player.py



@author: Callum Hay
'''

class Player:
    MAX_HIT_POINTS = 100
    
    def __init__(self):
        self.Reset()
    
    def Reset(self):
        self.hitPoints = Player.MAX_HIT_POINTS 
    
    def IsKnockedOut(self):
        return (self.hitPoints <= 0)
    
    def SetDamage(self, hpDamage):
        self.hitPoints -= hpDamage