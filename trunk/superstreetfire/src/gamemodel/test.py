'''
Created on Apr 10, 2011

@author: callumhay
'''
import time
import logging

from gesture_recognizer import *
from ssf_game import *

import attack
from attack import Attack

def RunActionLoop(game, actionList):
    for i in actionList:
        i.Initialize(game)
    
    deltaTime = 0.0
    lastTime  = time.time()
    finished = False
    while not finished:
        #print deltaTime
        
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
    
    p1LeftJab = attack.BuildLeftJabAttack(1)
    p2LeftJab = attack.BuildLeftJabAttack(2)
    
    p1LeftHook = attack.BuildLeftHookAttack(1)
    p2LeftHook = attack.BuildLeftHookAttack(2)
    
    p1Hadouken = attack.BuildHadoukenAttack(1)
    p2Hadouken = attack.BuildHadoukenAttack(2)
    
    logger = logging.getLogger('fire_state_logger')
    logger.setLevel(logging.DEBUG)
    logger.addHandler(logging.StreamHandler())        
    
    # Standalone jabs
    #RunActionLoop(game, [p1LeftJab])
    #RunActionLoop(game, [p2LeftJab])
    
    # Standalone hooks
    #RunActionLoop(game, [p1LeftHook])
    #RunActionLoop(game, [p2LeftHook])
    
    # Standalone specials
    RunActionLoop(game, [p1Hadouken])
    RunActionLoop(game, [p2Hadouken])
    
    print "Finished."
    