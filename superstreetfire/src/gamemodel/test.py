'''
Created on Apr 10, 2011

@author: callumhay
'''
import time
import logging

from gesture_recognizer import *
from ssf_game import *
from fire_emitter_states import FireState
from player import Player

import attack
from attack import Attack

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
        print "Total time:", i._currAttackTime

if __name__ == "__main__":
    gestureRec = GestureRecognizer()
    game = SSFGame(gestureRec)
    
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
    
    logger = logging.getLogger(FireState.LOGGER_NAME)
    logger.setLevel(logging.DEBUG)
    logger.addHandler(logging.StreamHandler())
    
    logger = logging.getLogger(Player.LOGGER_NAME)    
    logger.setLevel(logging.DEBUG)
    logger.addHandler(logging.StreamHandler())
    
    # Standalone jabs
    RunActionLoop(game, [p1LeftJab])
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
    
    print "Finished."
    