'''
receiver_queue_mgr.py

The Receiver Queue Manager is a class responsible for managing
the queues that hold data that has been aggregated from the incoming
serial port data stream from all of the super street fire clients.

The queues of the manager are thread safe, the Receiver dumps data
onto the appropriate queues and analyzers for the game state pick the
data up. 

@author: callumhay
'''

import collections
import threading

class ReceiverQueueMgr:
    MAX_QUEUE_SIZE = 8
    
    def __init__(self):
        self.p1LeftGloveQueue  = collections.deque()
        self.p1RightGloveQueue = collections.deque()
        self.p1HeadsetQueue    = collections.deque()
        
        self.p2LeftGloveQueue  = collections.deque()
        self.p2RightGloveQueue = collections.deque()
        self.p2HeadsetQueue    = collections.deque()
        
        self.p1LeftGloveLock  = threading.Semaphore()
        self.p1RightGloveLock = threading.Semaphore()
        self.p1HeadsetLock    = threading.Semaphore()
        
        self.p2LeftGloveLock  = threading.Semaphore()
        self.p2RightGloveLock = threading.Semaphore()
        self.p2HeadsetLock    = threading.Semaphore()  
    
    def PushP1LeftGloveData(self, data):
        self.p1LeftGloveLock.acquire()
        self._PushQueueData(self.p1LeftGloveQueue, data)
        self.p1LeftGloveLock.release()
        
    def PushP1RightGloveData(self, data):
        self.p1RightGloveLock.acquire()
        self._PushQueueData(self.p1RightGloveQueue, data)
        self.p1RightGloveLock.release()
                
    def PushP1HeadsetData(self, data):
        self.p1HeadsetLock.acquire()
        self._PushQueueData(self.p1HeadsetQueue, data)
        self.p1HeadsetLock.release()
        
    def PushP2LeftGloveData(self, data):
        self.p2LeftGloveLock.acquire()
        self._PushQueueData(self.p2LeftGloveQueue, data)
        self.p2LeftGloveLock.release()
           
    def PushP2RightGloveData(self, data):
        self.p2RightGloveLock.acquire()
        self._PushQueueData(self.p2RightGloveQueue, data)
        self.p2RightGloveLock.release()
                
    def PushP2HeadsetData(self, data):
        self.p2HeadsetLock.acquire()
        self._PushQueueData(self.p2HeadsetQueue, data)
        self.p2HeadsetLock.release()    
    
    def PopP1LeftGloveData(self):
        self.p1LeftGloveLock.acquire()
        data = self._PopQueueData(self.p1LeftGloveQueue)
        self.p1LeftGloveLock.release()
        return data
        
    def PopP1RightGloveData(self):
        self.p1RightGloveLock.acquire()
        data = self._PopQueueData(self.p1RightGloveQueue)
        self.p1RightGloveLock.release()
        return data
                
    def PopP1HeadsetData(self):
        self.p1HeadsetLock.acquire()
        data = self._PopQueueData(self.p1HeadsetQueue)
        self.p1HeadsetLock.release()
        return data
        
    def PopP2LeftGloveData(self):
        self.p2LeftGloveLock.acquire()
        data = self._PopQueueData(self.p2LeftGloveQueue)
        self.p2LeftGloveLock.release()
        return data
           
    def PopP2RightGloveData(self):
        self.p2RightGloveLock.acquire()
        data = self._PopQueueData(self.p2RightGloveQueue)
        self.p2RightGloveLock.release()
        return data
                
    def PopP2HeadsetData(self):
        self.p2HeadsetLock.acquire()
        data = self._PopQueueData(self.p2HeadsetQueue)
        self.p2HeadsetLock.release()
        return data
    
    def _PushQueueData(self, queue, data):
        # Don't push empty data
        if data == None:
            return
        
        if len(queue) == ReceiverQueueMgr.MAX_QUEUE_SIZE:
            print "WARNING: Receiver queue overflow"
            queue.popleft()
            
        print "Data is being placed on a receiver queue"
        queue.append(data)
    
    def _PopQueueData(self, queue):
        if len(queue) == 0:
            print "Tried to retrieve data from an empty receiver queue."
            return None
        else:
            print "Averaging and popping data"
            # Average all of the data on the queue and return the result,
            # emptying the entire queue in the process
            
            count    = len(queue)
            avgValue = queue.popleft()
            for i in range(1, count):
                avgValue += queue.popleft()

            assert(avgValue != None)
            assert(count > 0)
            
            avgValue = avgValue / count
            return avgValue
    
    
    
    