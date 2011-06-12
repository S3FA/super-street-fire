'''
Created on 2011-06-12

@author: syber
'''

from gamemodel.game_model_listener import GameModelListener

class SenderListener(GameModelListener):
    def __init__(self, ssfGame, senderObj):
        GameModelListener.__init__(self)
        self.game = ssfGame
        self.sender = senderObj
        self.game.RegisterListener(self)
        self.curTimer = '0'
        self.p1Health = 100
        self.p2Health = 100
    
    def Kill(self):
        self.game = None
        self.sender = None
            
    def OnTimerStateChanged(self, newTime):
        timer = str(int(round(newTime)))
        if (timer != self.curTimer):
            self.curTimer = timer
            #print 'send timer update ' + timer
            self.sender.SendTimerNum(timer)
    
    def OnPlayerHealthChanged(self, players):
        if (players[0].GetHealth() != self.p1Health):
            self.p1Health = players[0].GetHealth()
            #print 'send health p1: ' + str(players[0].GetHealth())
        if (players[1].GetHealth() != self.p2Health):
            self.p2Health = players[1].GetHealth()
            #print 'send health p2: ' + str(players[1].GetHealth())
                
    def OnHWAddrChanged(self, hwaddr):
        pass
    
    def OnRSSIChanged(self,rssi_dict):
        pass
        
    def OnPlayerMoves(self, actions):
        pass

    def OnGameStateChanged(self, state):
        pass
       
