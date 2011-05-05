'''
sender_queue_mgr.py

@author: Callum Hay
'''

import collections
import threading

class SenderQueueMgr:

    def __init__(self):
        self.fireEmitterQueue = collections.deque()
        self.fireEmitterLock  = threading.Semaphore()
    
    def PushFireEmitterData(self, data):
        pass
        # TODO
        
        
    def PopFireEmitterData(self):
        pass
    
    
    