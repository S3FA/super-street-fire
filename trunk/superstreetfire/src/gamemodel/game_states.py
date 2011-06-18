'''
game_states.py

Holds the Game States that make up the game logic for the super
street fire game. These states are what define the 'parts' of the
game when it's being played (e.g., when a round starts, is in progress, ends)
and when it's not being played (e.g., calibration, idle)

@author: Callum Hay
'''

import logging
from calibration_data import CalibrationData
from player import Player
from resources.sounds import sounds

IDLE_GAME_STATE          = 0
CALIBRATION_GAME_STATE   = 1
ROUND_BEGIN_GAME_STATE   = 2
ROUND_IN_PLAY_GAME_STATE = 3
ROUND_ENDED_GAME_STATE   = 4
SETTLE_TIE_GAME_STATE    = 5
MATCH_OVER_GAME_STATE    = 6
PAUSED_GAME_STATE        = 7

class GameState:
    LOGGER_NAME = 'game_state_logger'
    
    def __init__(self, ssfGame):
        assert(ssfGame != None)
        self.ssfGame = ssfGame
        self._logger = logging.getLogger(GameState.LOGGER_NAME)
    
    # Override these functions - they are the 'event' functions
    # that will cause state changes
    def Tick(self, dT):  pass
    def Calibrate(self): pass
    def StartGame(self): pass
    def TogglePauseGame(self): pass
    def StopGame(self):  pass
    def UpdateWithHeadsetData(self, p1HeadsetData, 
                              p2HeadsetData, dT, timeStamp): pass
    def UpdateWithGloveData(self, p1LGloveData, p1RGloveData, 
                            p2LGloveData, p2RGloveData, dT, timeStamp): pass

    def GetStateType(self): assert(False)
    

# The idle state does nothing - just waits for a signal to start the game or to
# calibrate inputs to the game
class IdleGameState(GameState):
    def __init__(self, ssfGame):
        GameState.__init__(self, ssfGame)
        self._logger.debug("Entering Idle Game State")
        
        # Make sure all of the game objects are in their natural, base state
        # (e.g., all emitters are off, players have full life, etc.)
        ssfGame.Reset()
        
        
    def Calibrate(self):
        self.ssfGame._SetState(CalibrationGameState(self.ssfGame))
    def StartGame(self):
        self.ssfGame._SetState(RoundBeginGameState(self.ssfGame, 1))
    def GetStateType(self):
        return IDLE_GAME_STATE

# The Calibration state is a state where both players hold their gloves
# still and point them at the opposing player - this allows us to collect
# the default data from the gloves and provide the gesture recognition system
# with the appropriate offsets before the game begins
class CalibrationGameState(GameState):
    LENGTH_IN_SECONDS = 5.0
    
    def __init__(self, ssfGame):
        GameState.__init__(self, ssfGame)
        self._logger.debug("Entering Calibration Game State")
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

    def GetStateType(self):
        return CALIBRATION_GAME_STATE

# We might want a countdown for each round (e.g., 3, 2, 1, FIGHT!) just
# to get the players ready/prepared to start wailing on each other
class RoundBeginGameState(GameState):
    COUNT_DOWN_TIME_IN_SECONDS = 3.0
    
    def __init__(self, ssfGame, roundNumber):
        GameState.__init__(self, ssfGame)
        self._logger.debug("Entering Round Begin Game State")
        self.countdownTime = RoundBeginGameState.COUNT_DOWN_TIME_IN_SECONDS
        self.roundNumber = roundNumber
        
        # Place player health back up to full
        # Make sure all fire emitters are turned off        
        ssfGame.player1.ResetHealth()
        ssfGame.player2.ResetHealth()
        ssfGame.KillEmitters()
    
    def Tick(self, dT):
        if self.countdownTime <= 0.0:
            # FIGHT!!!
            sounds.announceFight.play()
            self.ssfGame._SetState(RoundInPlayGameState(self.ssfGame, self.roundNumber))
            return
        
        self.countdownTime -= dT

    def TogglePauseGame(self):
        self.ssfGame._SetState(PausedGameState(self.ssfGame, self))

    def StopGame(self):
        # Immediately end the game by going to the idle state...
        self.ssfGame._SetState(IdleGameState(self.ssfGame))

    def GetStateType(self):
        return ROUND_BEGIN_GAME_STATE
        
# The 'round in play' state is the state active while the game is actually
# being played over the course of a single round (there are 3 rounds to a match)
class RoundInPlayGameState(GameState):
    ROUND_TIME_IN_SECONDS = 60.0
    
    def __init__(self, ssfGame, roundNumber):
        GameState.__init__(self, ssfGame)
        self._logger.debug("Entering Round In-Play Game State")
        # A list of all active actions during this round
        self._activeActions = []
        # There's always a game timer, which counts down throughout a match
        self.roundTime = RoundInPlayGameState.ROUND_TIME_IN_SECONDS
        self.roundNumber = roundNumber
        
    def Tick(self, dT):
        self.ssfGame._listenerCmdr.TimerStateChanged(self.roundTime)
        
        # Execute the actions (attacks, blocks, etc.)
        self.ssfGame._ExecuteGameActions(dT, self._activeActions)
        
        # Check to see if the current round is over...
        if self._IsRoundOver():
            self._logger.debug("Round is over.")
            # Switch states to the RoundEndedGameState
            self.ssfGame._SetState(RoundEndedGameState(self.ssfGame, self._GetRoundWinner(), self.roundNumber))
            return
        
        # Diminish the round timer
        self.roundTime -= dT
        
 
    def TogglePauseGame(self):
        self.ssfGame._SetState(PausedGameState(self.ssfGame, self))
                
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
        
    def GetStateType(self):
        return ROUND_IN_PLAY_GAME_STATE

    def _IsRoundOver(self):
        return (self.ssfGame.player1.IsKnockedOut() or self.ssfGame.player2.IsKnockedOut() or \
                self.roundTime <= 0.0)

    # Get the enumeration for which player won the round (or whether there was a tie)
    def _GetRoundWinner(self):
        assert(self._IsRoundOver() == True)
        
        if self.ssfGame.player1.IsKnockedOut():
            if self.ssfGame.player2.IsKnockedOut():
                return RoundEndedGameState.TIE_ROUND
            else:
                return RoundEndedGameState.PLAYER_2_WON_ROUND
        elif self.ssfGame.player2.IsKnockedOut():
            return RoundEndedGameState.PLAYER_1_WON_ROUND
        else:
            return RoundEndedGameState.TIE_ROUND

class RoundEndedGameState(GameState):
    NUM_ROUNDS_PER_MATCH = 3
    
    TIE_ROUND          = 0
    PLAYER_1_WON_ROUND = 1
    PLAYER_2_WON_ROUND = 2
    
    def __init__(self, ssfGame, roundWinner, roundNumber):
        GameState.__init__(self, ssfGame)
        self._logger.info("Round Ended, Winner: " + str(roundWinner))
        assert(roundWinner == RoundEndedGameState.PLAYER_1_WON_ROUND or \
               roundWinner == RoundEndedGameState.PLAYER_2_WON_ROUND or \
               roundWinner == RoundEndedGameState.TIE_ROUND)
        
        GameState.__init__(self, ssfGame)
        self._logger.debug("Entering Round Ended Game State")
        
        self.roundWinner = roundWinner
        
        self.roundNumber = roundNumber
        # Based on the round winner, increment the number of wins for the
        # corresponding player(s)
        if roundWinner == RoundEndedGameState.TIE_ROUND:
            self.ssfGame.player1.numRoundWins += 1
            self.ssfGame.player2.numRoundWins += 1
        elif roundWinner == RoundEndedGameState.PLAYER_1_WON_ROUND:
            self.ssfGame.player1.numRoundWins += 1
        else:
            self.ssfGame.player2.numRoundWins += 1
        
        sounds.announceRound.play()
        # Sanity: Players should NEVER have more than (half a match + 1) round wins
        assert(self.ssfGame.player1.numRoundWins <= (RoundEndedGameState.NUM_ROUNDS_PER_MATCH/2 + 1))
        assert(self.ssfGame.player2.numRoundWins <= (RoundEndedGameState.NUM_ROUNDS_PER_MATCH/2 + 1))
        
    def Tick(self, dT):
        # TODO: Currently we just go directly back to the beginning of the round
        # without any activity in this state, we can change this if need be...
        
        # Check to see whether the match is over via tie or a win/loss condition and if
        # not start up the next round of this match
        if self._IsMatchOver():
            self._logger.debug("Match is over...")
            # Check to see whether the match was won/lost or whether it was a complete tie
            if self._IsMatchTie():
                self.ssfGame._SetState(SettleTieGameState(self.ssfGame))
            else:
                # In the case were a player won the match, it must be the player that
                # won the match that brought us to this state
                self.ssfGame._SetState(MatchOverGameState(self.ssfGame, self.roundWinner))
        else:
            self.ssfGame._SetState(RoundBeginGameState(self.ssfGame, self.roundNumber+1))

    def TogglePauseGame(self):
        self.ssfGame._SetState(PausedGameState(self.ssfGame, self))
            
    def StopGame(self):
        # Immediately end the game by going to the idle state...
        self.ssfGame._SetState(IdleGameState(self.ssfGame))

    def GetStateType(self):
        return ROUND_ENDED_GAME_STATE
        
    def _IsMatchOver(self):
        return self.ssfGame.player1.numRoundWins == 2 or self.ssfGame.player2.numRoundWins == 2
    def _IsMatchTie(self):
        return self.ssfGame.player1.numRoundWins == 2 and self.ssfGame.player2.numRoundWins == 2

# Ties are sudden death - the first player to deliver a hit wins
class SettleTieGameState(GameState):
    def __init__(self, ssfGame):
        GameState.__init__(self, ssfGame)
        self._logger.debug("Entering Settle Tie (Sudden Death!) Game State")
        
        # Turn off chip damage, only delivered attacks count...
        self.ssfGame.chipDamageOn = False
        
        # A list of all active actions during this round
        self._activeActions = []
        
        # Reset player health
        self.ssfGame.player1.ResetHealth()
        self.ssfGame.player2.ResetHealth()

        # Turn off all the emitters
        self.ssfGame.KillEmitters()
        
    # The tie breaker is almost the exact same as a typical match, however,
    # whoever delivers the first hit wins...
    def Tick(self, dT):
        # Execute the actions (attacks, blocks, etc.)
        self.ssfGame._ExecuteGameActions(dT, self._activeActions)
        
        # Check to see if any player is hurt - if anyone is hurt then it's game over
        # for that player
        player1IsHurt = self.ssfGame.player1.hitPoints < Player.MAX_HIT_POINTS
        player2IsHurt = self.ssfGame.player2.hitPoints < Player.MAX_HIT_POINTS
        
        if player1IsHurt and player2IsHurt:
            # OK... so this is pretty absurd, the tie is down to the fraction of
            # a second, not really sure what to do here, both players truly deserve
            # to win equally, so let's just ignore this and keep going
            self.ssfGame.player1.ResetHealth()
            self.ssfGame.player2.ResetHealth()
            self._logger.debug("Players just hurt each other simultaneously?!")
            
        elif player1IsHurt:
            # Player 2 wins!!
            self.ssfGame.chipDamageOn = True
            self.ssfGame._SetState(MatchOverGameState(self.ssfGame, 2))
            self._logger.debug("Player 1 was hurt first - player 2 wins!")
        elif player2IsHurt:
            # Player 1 wins!!
            self.ssfGame.chipDamageOn = True
            self.ssfGame._SetState(MatchOverGameState(self.ssfGame, 1))
            self._logger.debug("Player 2 was hurt first - player 1 wins!")

    def TogglePauseGame(self):
        self.ssfGame._SetState(PausedGameState(self.ssfGame, self))

    def StopGame(self):
        # Immediately end the game by going to the idle state...
        self.ssfGame.chipDamageOn = True
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

    def GetStateType(self):
        return SETTLE_TIE_GAME_STATE

class MatchOverGameState(GameState):
    def __init__(self, ssfGame, winnerPlayerNum):
        assert(winnerPlayerNum == 1 or winnerPlayerNum == 2)
        GameState.__init__(self, ssfGame)
        self._logger.debug("Entering Match Over Game State, the winner is player " + str(winnerPlayerNum))
        self.winnerPlayerNum = winnerPlayerNum
        
    def Tick(self, dT):
        # TODO: Make all the fire emitters shoot flames for the player??
        # Do something here!!
        
        self.ssfGame._SetState(IdleGameState(self.ssfGame))
        pass
    
    def StopGame(self):
        # Immediately end the game by going to the idle state...
        self.ssfGame._SetState(IdleGameState(self.ssfGame))

    def GetStateType(self):
        return MATCH_OVER_GAME_STATE

class PausedGameState(GameState):
    def __init__(self, ssfGame, statePaused):
        assert(statePaused != None)
        GameState.__init__(self, ssfGame)
        self._logger.debug("Entering Paused Game State")
        self._statePaused = statePaused
 
    def TogglePauseGame(self):
        # Unpause the game - go back to the state we paused on...
        self.ssfGame._SetState(self._statePaused)

    def StopGame(self):
        sounds.swipeSound.play()
        # Immediately end the game by going to the idle state...
        self.ssfGame._SetState(IdleGameState(self.ssfGame))
    
    def GetStateType(self):
        return PAUSED_GAME_STATE
    