'''

@author: Callum Hay
'''

from calibration_data import CalibrationData

class GameState:

    def __init__(self, ssfGame):
        assert(ssfGame != None)
        self.ssfGame = ssfGame
    
    # Override these functions - they are the 'event' functions
    # that will cause state changes
    def Tick(self, dT):  pass
    def Calibrate(self): pass
    def StartGame(self): pass
    def StopGame(self):  pass
    def UpdateWithHeadsetData(self, p1HeadsetData, 
                              p2HeadsetData, dT, timeStamp): pass
    def UpdateWithGloveData(self, p1LGloveData, p1RGloveData, 
                            p2LGloveData, p2RGloveData, dT, timeStamp): pass

# The idle state does nothing - just waits for a signal to start the game or to
# calibrate inputs to the game
class IdleGameState(GameState):
    def __init__(self, ssfGame):
        GameState.__init__(self, ssfGame)
        
    def Calibrate(self):
        self.ssfGame._SetState(CalibrationGameState(self.ssfGame))
    def StartGame(self):
        self.ssfGame._SetState(RoundInPlayGameState(self.ssfGame))

# The Calibration state is a state where both players hold their gloves
# still and point them at the opposing player - this allows us to collect
# the default data from the gloves and provide the gesture recognition system
# with the appropriate offsets before the game begins
class CalibrationGameState(GameState):
    LENGTH_IN_SECONDS = 5.0
    
    def __init__(self, ssfGame):
        GameState.__init__(self, ssfGame)
        self.timeCounter = 0.0
        self.calibrationData = CalibrationData()
        
    def Tick(self, dT):
        # If we're done calibrating then we go back to the idle game state
        if self.timeCounter >= CalibrationGameState.LENGTH_IN_SECONDS:
            # Accumulate and average the calibration data that was collected
            self._FinializeCalibrationData()
            # Go back to the idle state
            self.ssfGame._SetState(IdleGameState(self.ssfGame))
            return
        self.timeCounter += dT

    def UpdateWithHeadsetData(self, p1HeadsetData, 
                              p2HeadsetData, dT, timeStamp):
        # Currently head-set data is not calibrated...
        pass

    def UpdateWithGloveData(self, p1LGloveData, p1RGloveData, 
                            p2LGloveData, p2RGloveData, dT, timeStamp):
        self.calibrationData.AddGloveDataSample(p1LGloveData, p1RGloveData, \
                                                p2LGloveData, p2RGloveData)
    
    def _FinializeCalibrationData(self):
        # Average/Normalize the calibration data and then set it
        # in the gesture recognition object of the game
        self.calibrationData.FinishSampling()
        self.ssfGame.gestureRecognizer.SetCalibrationData(self.calibrationData)


# The 'round in play' state is the state active while the game is actually
# being played over the course of a single round (there are 3 rounds to a match)
class RoundInPlayGameState(GameState):
    ROUND_TIME_IN_SECONDS = 60.0
    
    def __init__(self, ssfGame):
        GameState.__init__(self, ssfGame)
        
        # A list of all active actions during this round
        self._activeActions = []
        # There's always a game timer, which counts down throughout a match
        self.roundTime = RoundInPlayGameState.ROUND_TIME_IN_SECONDS
                
    def Tick(self, dT):
        # Check for any newly recognized gestures, execute any that get found
        if self.ssfGame.gestureRecognizer.HasNewActionsAvailable():
            newActions = self.ssfGame.gestureRecognizer.PopActions()
            assert(len(newActions) > 0)
            
            # Initialize all the new actions
            for action in newActions:
                action.Initialize(self)
            self._activeActions.extend(newActions)
            
        # Tick any actions (e.g., attacks, blocks) that are currently active within the game
        # This will update the state of the fire emitters and the game in general
        actionsToRemove = []
        for action in self._activeActions:
            if action.IsFinished():
                actionsToRemove.append(action)
            else:
                action.Tick(self, dT)
        
        # Clear up all finished actions
        for action in actionsToRemove:
            self._activeActions.remove(action)
        
        # Diminish the round timer
        self.roundTime -= dT
        
        '''
        # Check to see if the current round is over...
        if self.IsRoundOver():
            # Reset the round timer and increment the round number
            self.roundTime    = RoundInPlayGameState.ROUND_TIME_IN_SECONDS
            self.roundNumber += 1
            
            if self.IsGameOver():
                gameWinnerNum = self.GetGameWinner()
                assert(gameWinnerNum >= 0)
                # TODO
            else:
                roundWinnerNum = self.GetRoundWinner()
                assert(roundWinnerNum >= 0)
                # TODO
        '''
        
    def StopGame(self):
        # Immediately end the game by going to the idle state...
        self.ssfGame._SetState(IdleGameState(self.ssfGame))
        pass
    
    def UpdateWithHeadsetData(self, p1HeadsetData, 
                              p2HeadsetData, dT, timeStamp):
        # TODO: What do we do with head set data??
        pass
                              
    def UpdateWithGloveData(self, p1LGloveData, p1RGloveData, 
                            p2LGloveData, p2RGloveData, dT, timeStamp):
        self.ssfGame.gestureRecognizer.UpdateWithGloveData(p1LGloveData, p1RGloveData, \
                                                           p2LGloveData, p2RGloveData, \
                                                           dT, timeStamp)
        
        
    