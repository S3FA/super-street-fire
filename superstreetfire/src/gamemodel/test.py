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


if __name__ == "__main__":
    gestureRec = GestureRecognizer()
    game = SSFGame(gestureRec)
    
    p1LeftJab = attack.BuildLeftJabAttack(1)
    p1LeftJab.Initialize(game)

    
    logger = logging.getLogger('fire_state_logger')
    logger.setLevel(logging.DEBUG)
    logger.addHandler(logging.StreamHandler())        
    
    deltaTime = 0.0
    lastTime  = time.time()
    while not p1LeftJab.IsFinished():
        #print deltaTime
        p1LeftJab.Tick(game, deltaTime)
        deltaTime = time.time() - lastTime
        lastTime  = time.time()
    
    print "Finished."
    print "Total time:", p1LeftJab._currAttackTime