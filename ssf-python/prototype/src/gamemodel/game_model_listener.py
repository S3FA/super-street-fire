'''
game_model_listener.py

Holds the super class for all listener events for the gamemodel module.
This file also holds the 'commander' (GameModelListenerCmdr), which is
used to tell all registered listeners that an event has occurred (only used
by the gamemodel code). 

@author: Callum Hay
'''

class GameModelListener:

    def __init__(self):
        pass
    
    def OnGameStateChanged(self, newGameState):
        pass
    
    def OnTimerStateChanged(self, timerValue):
        pass
    
    def OnEmitterStateChanged(self):
        pass
    
    def OnPlayerHealthChanged(self, players):
        pass
    
    def OnRSSIChanged(self,rssi_dict):
        pass
    
    def OnHWAddrChanged(self, hwaddr):
        pass
    
    def OnPlayerMoves(self, actions):
        pass
    
    # TODO: Place more listener events here
    
    
class GameModelListenerCmdr:
    def __init__(self):
        self._listenerList = []
    
    # Register a listener with this object - that listener will now
    # be notified by the gamemodel whenever events occur
    def RegisterListener(self, listener):
        assert(isinstance(listener, GameModelListener))
        self._listenerList.append(listener)
    
    # Tell all listeners that a OnGameStateChanged event occurred
    def GameStateChanged(self, newGameState):
        for i in self._listenerList: i.OnGameStateChanged(newGameState)
    
    def PlayerMoves(self, actions):
        for i in self._listenerList: i.OnPlayerMoves(actions)
    
    # Tell all listeners that a TimerStateChange occurred
    def TimerStateChanged(self, timerValue):
        for i in self._listenerList: i.OnTimerStateChanged(timerValue)
    
    def EmitterStateChanged(self):
        for i in self._listenerList: i.OnEmitterStateChanged()
    
    def PlayerHealthChanged(self, players):
        for i in self._listenerList: i.OnPlayerHealthChanged(players)
    
    def ReceivedSignalStrengthIndicatorChanged(self, rssi_dict):
        for i in self._listenerList: i.OnRSSIChanged(rssi_dict)
        
    def HWAddrChanged(self, hwaddr):
        for i in self._listenerList: i.OnHWAddrChanged(hwaddr)

    
    