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
        # Make sure all of the game objects are in their natural, base state
        # (e.g., all emitters are off, players have full life, etc.)
        ssfGame.Reset()
        
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


# We might want a countdown for each round (e.g., 3, 2, 1, FIGHT!) just
# to get the players ready/prepared to start wailing on each other
class RoundBeginGameState(GameState):
    COUNT_DOWN_TIME_IN_SECONDS = 3.0
    
    def __init__(self, ssfGame):
        GameState.__init__(self, ssfGame)
        self.countdownTime = RoundBeginGameState.COUNT_DOWN_TIME_IN_SECONDS
        
        # TODO: Place player health back up to full
        # TODO: Make sure all fire emitters are turned off 
    
    def Tick(self, dT):
        if self.countdownTime <= 0.0:
            # FIGHT!!!
            self.ssfGame._SetState(RoundInPlayGameState(self.ssfGame))
            return
        
        self.countdownTime -= dT

    def StopGame(self):
        # Immediately end the game by going to the idle state...
        self.ssfGame._SetState(IdleGameState(self.ssfGame))

# The 'round in play' state is the state active while the game is actually
# being played over the course of a single round (there are 3 rounds to a match)
class RoundInPlayGameState(GameState):
    ROUND_TIME_IN_SECONDS = 60.0
    
    def __init__(self, ssfGame):
        GameState.__init__(self, ssfGame)
        
        # A list of all active actions during this round
        self._activeActions = []
        # There's always a game timer, which counts down throughout a match
        self._roundTime = RoundInPlayGameState.ROUND_TIME_IN_SECONDS
        
    def Tick(self, dT):
        # Check for any newly recognized gestures, execute any that get found
        if self.ssfGame.gestureRecognizer.HasNewActionsAvailable():
            newActions = self.ssfGame.gestureRecognizer.PopActions()
            assert(len(newActions) > 0)
            
            # Initialize all the new actions
            for action in newActions:
                action.Initialize(self.ssfGame)
            self._activeActions.extend(newActions)
            
        # Tick any actions (e.g., attacks, blocks) that are currently active within the game
        # This will update the state of the fire emitters and the game in general
        actionsToRemove = []
        for action in self._activeActions:
            if action.IsFinished():
                actionsToRemove.append(action)
            else:
                action.Tick(self.ssfGame, dT)
        
        # Clear up all finished actions
        for action in actionsToRemove:
            self._activeActions.remove(action)
        
        # Diminish the round timer
        self._roundTime -= dT
        
        # Check to see if the current round is over...
        if self.IsRoundOver():
            # Switch states to the RoundEndedGameState
            self.ssfGame._SetState(RoundEndedGameState(self.ssfGame, self._GetRoundWinner()))
            return
        
    def StopGame(self):
        # Immediately end the game by going to the idle state...
        self.ssfGame._SetState(IdleGameState(self.ssfGame))
    
    def UpdateWithHeadsetData(self, p1HeadsetData, 
                              p2HeadsetData, dT, timeStamp):
        # TODO: What do we do with head set data??
        pass
                              
    def UpdateWithGloveData(self, p1LGloveData, p1RGloveData, 
                            p2LGloveData, p2RGloveData, dT, timeStamp):
        self.ssfGame.gestureRecognizer.UpdateWithGloveData(p1LGloveData, p1RGloveData, \
                                                           p2LGloveData, p2RGloveData, \
                                                           dT, timeStamp)

    def _IsRoundOver(self):
        return (self.ssfGame.player1.IsKnockedOut() or self.ssfGame.player2.IsKnockedOut() or self.roundTime <= 0.0)

    # Get the enumeration for which player won the round (or whether there was a tie)
    def _GetRoundWinner(self):
        if self.player1.IsKnockedOut():
            if self.player2.IsKnockedOut():
                return RoundEndedGameState.TIE_ROUND
            else:
                return RoundEndedGameState.PLAYER_2_WON_ROUND
        elif self.player2.IsKnockedOut():
            return RoundEndedGameState.PLAYER_1_WON_ROUND
        else:
            # This should NEVER happen - this function should only be called
            # if the round is over! (i.e., self._IsRoundOver() == True)
            return -1

class RoundEndedGameState(GameState):
    NUM_ROUNDS_PER_MATCH = 3
    
    TIE_ROUND          = 0
    PLAYER_1_WON_ROUND = 1
    PLAYER_2_WON_ROUND = 2
    
    def __init__(self, ssfGame, roundWinner):
        assert(roundWinner == RoundEndedGameState.PLAYER_1_WON_ROUND or \
               roundWinner == RoundEndedGameState.PLAYER_2_WON_ROUND or \
               roundWinner == RoundEndedGameState.TIE_ROUND)
        
        GameState.__init__(self, ssfGame)
        self._roundWinner = roundWinner
        # Based on the round winner, increment the number of wins for the
        # corresponding player(s)
        if roundWinner == RoundEndedGameState.TIE_ROUND:
            self.ssfGame.player1.numRoundWins += 1
            self.ssfGame.player2.numRoundWins += 1
        elif roundWinner == RoundEndedGameState.PLAYER_1_WON_ROUND:
            self.ssfGame.player1.numRoundWins += 1
        else:
            self.ssfGame.player2.numRoundWins += 1
        
        # Sanity: Players should NEVER have more than (half a match + 1) round wins
        assert(self.ssfGame.player1.numRoundWins <= (RoundEndedGameState.NUM_ROUNDS_PER_MATCH/2 + 1))
        assert(self.ssfGame.player2.numRoundWins <= (RoundEndedGameState.NUM_ROUNDS_PER_MATCH/2 + 1))
        
    def Tick(self, dT):
        # TODO: Currently we just go directly back to the beginning of the round
        # without any activity in this state, we can change this if need be...
        
        # Check to see whether the match is over via tie or a win/loss condition and if
        # not start up the next round of this match
        if self._IsMatchOver():
            # Check to see whether the match was won/lost or whether it was a complete tie
            if self._IsMatchTie():
                self.ssfGame._SetState(SettleTieGameState(self.ssfGame))
            else:
                # In the case were a player won the match, it must be the player that
                # won the match that brought us to this state
                self.ssfGame._SetState(MatchOverGameState(self.ssfGame, self._roundWinner))
        else:
            self.ssfGame._SetState(RoundBeginGameState(self.ssfGame))
    
    def StopGame(self):
        # Immediately end the game by going to the idle state...
        self.ssfGame._SetState(IdleGameState(self.ssfGame))

    def _IsMatchOver(self):
        return self.player1.numRoundWins == 2 or self.player2.numRoundWins == 2
    def _IsMatchTie(self):
        return self.player1.numRoundWins == 2 and self.player2.numRoundWins == 2

class SettleTieGameState(GameState):
    def __init__(self, ssfGame):
        GameState.__init__(self, ssfGame)
        # TODO: Place player health back up to full
        # TODO: Make sure all fire emitters are turned off 
        
    # TODO: What does the SettleTieGameState look like...?

class MatchOverGameState(GameState):
    def __init__(self, ssfGame, winnerPlayerNum):
        GameState.__init__(self, ssfGame)
        self.winnerPlayerNum = winnerPlayerNum
        
    def Tick(self, dT):
        pass
    
    def StopGame(self):
        # Immediately end the game by going to the idle state...
        self.ssfGame._SetState(IdleGameState(self.ssfGame))
         
    