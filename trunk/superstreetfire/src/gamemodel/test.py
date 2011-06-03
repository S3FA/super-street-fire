'''
Created on Apr 10, 2011

@author: callumhay
'''
import time
import logging

from gesture_recognizer import *
from ssf_game import *
from fire_emitter_states import FireState
from game_states import *
from player import Player
from action import Action

import attack
from attack import Attack
import block
from block import Block

def RunActionLoop(game, actionList):
    for i in actionList:
        i.Initialize(game)
    
    deltaTime = 0.0
    lastTime  = time.time()
    finished = False
    while not finished:

        finished = True
        for i in actionList:
            i.Tick(game, deltaTime)
            finished = finished and i.IsFinished()
            
        deltaTime = time.time() - lastTime
        lastTime  = time.time()

    for i in actionList:
        if isinstance(i, Attack):
            print "Total time:", i._currAttackTime
        else:
            print "Total time:", i._currBlockTime

def SetupLoggers():
    logger = logging.getLogger(FireState.LOGGER_NAME)
    logger.setLevel(logging.DEBUG)
    logger.addHandler(logging.StreamHandler())
    
    logger = logging.getLogger(Player.LOGGER_NAME)    
    logger.setLevel(logging.DEBUG)
    logger.addHandler(logging.StreamHandler())
    
    logger = logging.getLogger(GameState.LOGGER_NAME)
    logger.setLevel(logging.DEBUG)
    logger.addHandler(logging.StreamHandler())

    logger = logging.getLogger(SSFGame.LOGGER_NAME)
    logger.setLevel(logging.DEBUG)
    logger.addHandler(logging.StreamHandler())

if __name__ == "__main__":
    SetupLoggers()

    game = SSFGame()
    
    p1LeftJab  = attack.BuildLeftJabAttack(1)
    p2LeftJab  = attack.BuildLeftJabAttack(2)
    p1RightJab = attack.BuildRightJabAttack(1)
    p2RightJab = attack.BuildRightJabAttack(2)
    
    p1LeftHook  = attack.BuildLeftHookAttack(1)
    p2LeftHook  = attack.BuildLeftHookAttack(2)
    p1RightHook = attack.BuildRightHookAttack(1)
    p2RightHook = attack.BuildRightHookAttack(2)
    
    p1Hadouken = attack.BuildHadoukenAttack(1)
    p2Hadouken = attack.BuildHadoukenAttack(2)
    
    p1LeftBlock = block.BuildLeftBasicBlock(1)
    p2LeftBlock = block.BuildLeftBasicBlock(2)
    
    p1RightBlock = block.BuildRightBasicBlock(1)
    p2RightBlock = block.BuildRightBasicBlock(2)

    # Standalone jabs
    #RunActionLoop(game, [p1LeftJab])
    #RunActionLoop(game, [p2LeftJab])
    #RunActionLoop(game, [p1RightJab])
    #RunActionLoop(game, [p2RightJab])
    
    # Standalone hooks
    #RunActionLoop(game, [p1LeftHook])
    #RunActionLoop(game, [p2LeftHook])
    #RunActionLoop(game, [p1RightHook])
    #RunActionLoop(game, [p2RightHook])
       
    # Standalone specials
    #RunActionLoop(game, [p1Hadouken])
    #RunActionLoop(game, [p2Hadouken])
    
    # Both players, simultaneous attacks
    #RunActionLoop(game, [p1LeftJab, p2LeftJab])               # Opposite arcs
    #RunActionLoop(game, [p1LeftJab, p2RightJab])              # Same arc same attack
    #RunActionLoop(game, [p1RightJab, p2LeftHook])             # Same arc different attacks
    #RunActionLoop(game, [p1Hadouken, p2LeftHook, p2RightJab]) # Multi attack
    
    
    # Simultaneous Blocks
    #RunActionLoop(game, [p1LeftBlock, p2LeftBlock])
    #RunActionLoop(game, [p1RightBlock, p2RightBlock])
    
    
    # Blocks + Attacks...
    
    # Basic single blocking on the left and right arcs
    #RunActionLoop(game, [p1LeftBlock, p2RightJab])
    #RunActionLoop(game, [p1LeftBlock, p2RightHook])
    #RunActionLoop(game, [p1RightBlock, p2LeftJab])
    #RunActionLoop(game, [p1RightBlock, p2LeftHook])
    
    #RunActionLoop(game, [p2LeftBlock,  p1RightJab])
    #RunActionLoop(game, [p2LeftBlock,  p1RightHook])
    #RunActionLoop(game, [p2RightBlock, p1LeftJab])
    #RunActionLoop(game, [p2RightBlock, p1LeftHook])    
    
    # Blocking with no affect on incoming attacks
    #RunActionLoop(game, [p1RightBlock, p2RightJab])
    #RunActionLoop(game, [p1RightBlock, p2RightHook])
    #RunActionLoop(game, [p1LeftBlock, p2LeftJab])
    #RunActionLoop(game, [p1LeftBlock, p2LeftHook])
    
    #RunActionLoop(game, [p2RightBlock, p1RightJab])
    #RunActionLoop(game, [p2RightBlock, p1RightHook])
    #RunActionLoop(game, [p2LeftBlock,  p1LeftJab])
    #RunActionLoop(game, [p2LeftBlock,  p1LeftHook])
    
    # Same player blocking and attacking simultaneously
    #RunActionLoop(game, [p1LeftBlock,  p1LeftJab])
    #RunActionLoop(game, [p1RightBlock, p1RightJab])
    #RunActionLoop(game, [p1LeftBlock,  p1LeftHook])
    #RunActionLoop(game, [p1RightBlock, p1RightHook])
    
    #RunActionLoop(game, [p2LeftBlock,  p2LeftJab])
    #RunActionLoop(game, [p2RightBlock, p2RightJab])
    #RunActionLoop(game, [p2LeftBlock,  p2LeftHook])
    #RunActionLoop(game, [p2RightBlock, p2RightHook])    
    
    # Some fun combinations...
    
    # Player 2 blocks on both arcs against player 1's Hadouken
    #RunActionLoop(game, [p2LeftBlock, p2RightBlock, p1Hadouken])
    
    # Player 1 attacks on one arc and defends on the other, player 2 does the same
    # but on opposite arcs
    #RunActionLoop(game, [p1LeftBlock, p1RightJab, p2LeftBlock, p2RightJab])
    
    # Idle -> RoundBegin
    game.StartGame()
    
    # RoundBegin -> Pause
    game.TogglePauseGame()
    # Pause -> RoundBegin
    game.TogglePauseGame()
    
    # Finish the RoundBegin state (countdown done) 
    # RoundBegin -> RoundInPlay
    game.Tick(RoundBeginGameState.COUNT_DOWN_TIME_IN_SECONDS)
    game.Tick(0.0)
    
    # The game is now in play, make the players attack each other and stuff
    # play the game until a player dies
    RunActionLoop(game, [Attack(1, Action.LEFT_SIDE, 1, 0.25, 50), Attack(2, Action.RIGHT_SIDE, 1, 0.25, 50)])
    RunActionLoop(game, [Attack(1, Action.LEFT_SIDE, 1, 0.25, 25), Attack(2, Action.LEFT_SIDE, 1, 0.25, 50)])

    # RoundInPlay -> RoundEnded
    game.Tick(0.0)
    # RoundEnded -> RoundBegin
    game.Tick(0.0)
    
    # RoundBegin -> RoundInPlay
    game.Tick(RoundBeginGameState.COUNT_DOWN_TIME_IN_SECONDS)
    game.Tick(0.0)
    
    # Let player 2 win again... this should cause player 2 to win the match
    RunActionLoop(game, [Attack(1, Action.LEFT_SIDE, 1, 0.25, 50), Attack(2, Action.RIGHT_SIDE, 1, 0.25, 50)])
    RunActionLoop(game, [Attack(1, Action.LEFT_SIDE, 1, 0.25, 25), Attack(2, Action.LEFT_SIDE, 1, 0.25, 50)])
    
    # RoundInPlay -> RoundEnded
    game.Tick(0.0)
    # RoundEnded -> MatchOver (player 2 wins!)
    game.Tick(0.0)
    # MatchOver -> Idle
    game.Tick(0.0)
    
    
    print "Finished."
    