'''
Created on 2011-06-12

@author: syber
'''

from gamemodel.game_model_listener import GameModelListener
from gamemodel.game_states import *
import parser

class SenderListener(GameModelListener):
    def __init__(self, ssfGame, ioManager):
        GameModelListener.__init__(self)
        self._logger = logging.getLogger("sender")
        self.game = ssfGame
        self.xbeeio = ioManager
        self.game.RegisterListener(self)
        self.curTimer = 0
        self.p1Health = 0
        self.p2Health = 0
    
    def Kill(self):
        self.game = None
        self.xbeeio = None
    
    def DetectDevices(self):
        self.xbeeio.NodeDiscovery()        
        self.game._listenerCmdr.HWAddrChanged(parser.Parser.ADDR_TABLE)


    def AllFireOn(self):
        self.xbeeio.SendFire(1)

    def AllFireOff(self):
        self.xbeeio.SendFire(0)

    def OnEmitterStateChanged(self):
        #TODO-- the state of emitters has already changed when this runs
        #self.xbeeio.AddFireEmitterData(self.game.GetLeftEmitterArc(1), \
        #                                self.game.GetRightEmitterArc(1))
        pass
            
    def OnTimerStateChanged(self, newTime):
        timer = int(round(newTime))
        if (timer != self.curTimer):
            self.curTimer = timer
            #print 'send timer update ' + str(timer)
            self.xbeeio.SendTimerNum(timer)
    
    def OnPlayerHealthChanged(self, players):
        if (players[0].GetHealth() != self.p1Health):
            self.p1Health = players[0].GetHealth()
            if self.p1Health <= 0:
                self.p1Health = 0
                # person is dead.. send KO
            #print 'send health p1: ' + str(self.p1Health) )
            self.xbeeio.SendP1LifeBar(self.p1Health)
        
        if (players[1].GetHealth() != self.p2Health):
            self.p2Health = players[1].GetHealth()
            if self.p2Health <= 0:
                self.p2Health = 0
                # person is dead.. send KO
            #print 'send health p2: ' + str(self.p2Health)
            self.xbeeio.SendP2LifeBar(self.p2Health)
            
        self.xbeeio.SendFire(0)
        
    def OnGameStateChanged(self, state):
        GameModelListener.OnGameStateChanged(self, state)
        cur_state = state.GetStateType()
        # update round and match won, etc
        
        if cur_state == ROUND_BEGIN_GAME_STATE:
            self.xbeeio.SendP1LifeBar(100)
            self.xbeeio.SendP2LifeBar(100)
            self.xbeeio.SendTimerNum(60)
            
        if cur_state == ROUND_ENDED_GAME_STATE:
            self._logger.info('Sender ----- Round ' + str(state.roundNumber) + ' won by player ' + str(state.roundWinner))
        if cur_state == MATCH_OVER_GAME_STATE:
            self._logger.info('Sender ----- Match won by ' + str(state.winnerPlayerNum))
                
    def OnHWAddrChanged(self, hwaddr):
        pass
    
    def OnRSSIChanged(self, rssi_dict):
        pass
        
    def OnPlayerMoves(self, actions):
        pass

