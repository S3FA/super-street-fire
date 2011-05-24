'''
receiver_queue_mgr.py

The Receiver Queue Manager is a class responsible for managing
the queues that hold data that has been aggregated from the incoming
serial port data stream from all of the super street fire clients.

The queues of the manager are thread safe, the Receiver dumps data
onto the appropriate queues and analyzes for the game state pick the
data up. 

@author: callumhay
'''

import collections
import threading

class ReceiverQueueMgr:
    # why is this value chosen?
    MAX_QUEUE_SIZE = 4
    
    def __init__(self):
        self.p1LeftGloveQueue  = collections.deque(list(),ReceiverQueueMgr.MAX_QUEUE_SIZE)
        self.p1RightGloveQueue = collections.deque(list(),ReceiverQueueMgr.MAX_QUEUE_SIZE)
        self.p1HeadsetQueue    = collections.deque()
        self.p1RightGlove = None
        self.p1LeftGlove = None
        
        self.p2LeftGloveQueue  = collections.deque(list(),ReceiverQueueMgr.MAX_QUEUE_SIZE)
        self.p2RightGloveQueue = collections.deque(list(),ReceiverQueueMgr.MAX_QUEUE_SIZE)
        self.p2HeadsetQueue    = collections.deque()
        self.p2RightGlove = None
        self.p2LeftGlove = None
        
        self.p1LeftGloveLock  = threading.Semaphore()
        self.p1RightGloveLock = threading.Semaphore()
        self.p1HeadsetLock    = threading.Semaphore()
        
        self.p2LeftGloveLock  = threading.Semaphore()
        self.p2RightGloveLock = threading.Semaphore()
        self.p2HeadsetLock    = threading.Semaphore()  
    
    def PushP1LeftGloveData(self, data):
        self.p1LeftGloveLock.acquire()
        if (self.p1LeftGlove == None):
            self.p1LeftGlove = data
        self._PushQueueData(self.p1LeftGloveQueue, data - self.p1LeftGlove)
        self.p1LeftGlove = data
        self.p1LeftGloveLock.release()
        
    def PushP1RightGloveData(self, data):
        self.p1RightGloveLock.acquire()
        if (self.p1RightGlove == None):
            self.p1RightGlove = data
        self._PushQueueData(self.p1RightGloveQueue, data - self.p1RightGlove)
        self.p1RightGlove = data
        self.p1RightGloveLock.release()
                
    def PushP1HeadsetData(self, data):
        self.p1HeadsetLock.acquire()
        self._PushQueueData(self.p1HeadsetQueue, data)
        self.p1HeadsetLock.release()
        
    def PushP2LeftGloveData(self, data):
        self.p2LeftGloveLock.acquire()
        if (self.p2LeftGlove == None):
            self.p2LeftGlove = data
        self._PushQueueData(self.p2LeftGloveQueue, self.p2LeftGlove.__sub__(data))
        self.p2LeftGlove = data
        self.p2LeftGloveLock.release()
           
    def PushP2RightGloveData(self, data):
        self.p2RightGloveLock.acquire()
        if (self.p2RightGlove == None):
            self.p2RightGlove = data
        self._PushQueueData(self.p2RightGloveQueue, self.p2RightGlove.__sub__(data))
        self.p2RightGlove = data
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
            #print "WARNING: Receiver queue overflow"
            queue.popleft()
            
        #print "Data is being placed on a receiver queue"
        queue.append(data)
    
    def _PopQueueData(self, queue):
        if len(queue) < 2:
            #print "Tried to retrieve data from an empty receiver queue."
            return None
        else:
            # Average all of the data on the queue and return the result,
            #print "Averaging data "
            count    = len(queue)
            avgSum = queue.pop()
            for i in range(1, count):
                avgSum += queue.pop()
                
            assert(avgSum != None)
            assert(count > 0)
            
            av = avgSum/count
            #print "count %d avg=%s" % (count, av)
            return av
    
    
    
    