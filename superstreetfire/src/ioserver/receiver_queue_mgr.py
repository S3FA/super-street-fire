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

import Queue

class ReceiverQueueMgr:
    MAX_QUEUE_SIZE = 8
    
    def __init__(self):
        self.p1LeftGloveQueue  = Queue.Queue(ReceiverQueueMgr.MAX_QUEUE_SIZE)
        self.p1RightGloveQueue = Queue.Queue(ReceiverQueueMgr.MAX_QUEUE_SIZE)
        self.p1HeadsetQueue    = Queue.Queue(ReceiverQueueMgr.MAX_QUEUE_SIZE)
        
        self.p2LeftGloveQueue  = Queue.Queue(ReceiverQueueMgr.MAX_QUEUE_SIZE)
        self.p2RightGloveQueue = Queue.Queue(ReceiverQueueMgr.MAX_QUEUE_SIZE)
        self.p2HeadsetQueue    = Queue.Queue(ReceiverQueueMgr.MAX_QUEUE_SIZE)
        
    
    def PushQueueData(self, queue, data):
        print "Data is being placed on a receiver queue"
        # In the case where the queue is already full then we drop the least
        # recent item from the queue
        if queue.full():
            print "Tried to push data onto a full receiver queue, dumping data."
            queue.get(block=False)
        queue.put(data, block=False)
        
    
    def PopQueueData(self, queue):
        print "Data is being popped off a receiver queue"
        if queue.empty():
            print "Tried to retrieve data from an empty receiver queue."
            return None
        else:
            return queue.get(block=False)
    
    
    
    