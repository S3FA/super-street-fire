'''
Created on 2011-06-12

@author: syber
'''

from gamemodel.game_model_listener import GameModelListener
from gamemodel.game_states import *

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
            self.sender.SendLifeBarData( str(players[0].GetHealth()), str(players[1].GetHealth()) )
        if (players[1].GetHealth() != self.p2Health):
            self.p2Health = players[1].GetHealth()
            #print 'send health p2: ' + str(players[1].GetHealth())
            self.sender.SendLifeBarData( str(players[0].GetHealth()), str(players[1].GetHealth()) )

    def OnGameStateChanged(self, state):
        GameModelListener.OnGameStateChanged(self, state)
        cur_state = state.GetStateType()
        # update KO, round won, etc
        if cur_state == ROUND_ENDED_GAME_STATE:
            print 'Sender ----- Round ' + str(state.roundNumber) + ' won by ' + str(state.roundWinner)
        if cur_state == MATCH_OVER_GAME_STATE:
            print 'Sender ----- Match won by ' + str(state.winnerPlayerNum)
                
    def OnHWAddrChanged(self, hwaddr):
        pass
    
    def OnRSSIChanged(self,rssi_dict):
        pass
        
    def OnPlayerMoves(self, actions):
        pass

