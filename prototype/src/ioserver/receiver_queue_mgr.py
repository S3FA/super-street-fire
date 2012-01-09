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

'''
 setup each queue to be "MAX_QUEUE_SIZE", add data as delta values for 
 acceleration & rotation, headings are absolute. 
 *only* return an updated average when new data is added to the queues
'''
class ReceiverQueueMgr:
    MAX_QUEUE_SIZE = 4
    
    # keys for rssi_info dict
    P1_LEFT_RSSI = '1L'
    P1_RIGHT_RSSI = '1R'
    P1_HEADSET_RSSI = '1H'
    P2_LEFT_RSSI = '2L'
    P2_RIGHT_RSSI = '2R'
    P2_HEADSET_RSSI = '2H'
    
    
    def __init__(self):
        self.p1LeftGloveQueue  = collections.deque(list(),ReceiverQueueMgr.MAX_QUEUE_SIZE)
        self.p1RightGloveQueue = collections.deque(list(),ReceiverQueueMgr.MAX_QUEUE_SIZE)
        self.p1HeadsetQueue    = collections.deque()
        self.p1 = { 'L' : None, 'newL' : 0, 'R' : None, 'newR' : 0 }
        
        self.p2LeftGloveQueue  = collections.deque(list(),ReceiverQueueMgr.MAX_QUEUE_SIZE)
        self.p2RightGloveQueue = collections.deque(list(),ReceiverQueueMgr.MAX_QUEUE_SIZE)
        self.p2HeadsetQueue    = collections.deque()
        self.p2 = { 'L' : None, 'newL' : 0, 'R' : None, 'newR' : 0 }
        
        self.p1LeftGloveLock  = threading.Semaphore()
        self.p1RightGloveLock = threading.Semaphore()
        self.p1HeadsetLock    = threading.Semaphore()
        
        self.p2LeftGloveLock  = threading.Semaphore()
        self.p2RightGloveLock = threading.Semaphore()
        self.p2HeadsetLock    = threading.Semaphore()
        
        self.rssi_info = {ReceiverQueueMgr.P1_LEFT_RSSI: 0, ReceiverQueueMgr.P1_RIGHT_RSSI: 0, ReceiverQueueMgr.P1_HEADSET_RSSI: 0,
                          ReceiverQueueMgr.P2_LEFT_RSSI: 0, ReceiverQueueMgr.P2_RIGHT_RSSI: 0, ReceiverQueueMgr.P2_HEADSET_RSSI: 0}
        
    
    def PushP1LeftGloveData(self, data, rssi=0):
        self.p1LeftGloveLock.acquire()
        if (self.p1['L'] == None):
            self.p1['L'] = data
        self._PushQueueData(self.p1LeftGloveQueue, data - self.p1['L'])
        self.p1['newL'] = 1
        self.p1['L'] = data
        self.rssi_info[ReceiverQueueMgr.P1_LEFT_RSSI] = rssi
        self.p1LeftGloveLock.release()
        
    def PushP1RightGloveData(self, data, rssi=0):
        self.p1RightGloveLock.acquire()
        if (self.p1['R'] == None):
            self.p1['R'] = data
        self._PushQueueData(self.p1RightGloveQueue, data - self.p1['R'])
        self.p1['newR'] = 1
        self.p1['R'] = data
        self.rssi_info[ReceiverQueueMgr.P1_RIGHT_RSSI] = rssi
        self.p1RightGloveLock.release()
                
    def PushP1HeadsetData(self, data):
        self.p1HeadsetLock.acquire()
        self._PushQueueData(self.p1HeadsetQueue, data)
        self.rssi_info[ReceiverQueueMgr.P1_HEADSET_RSSI] = data.link
        self.p1HeadsetLock.release()
        
    def PushP2LeftGloveData(self, data, rssi=0):
        self.p2LeftGloveLock.acquire()
        if (self.p2['L'] == None):
            self.p2['L'] = data
        self._PushQueueData(self.p2LeftGloveQueue, data - self.p2['L'])
        self.p2['newL'] = 1
        self.p2['L'] = data
        self.rssi_info[ReceiverQueueMgr.P2_LEFT_RSSI] = rssi
        self.p2LeftGloveLock.release()
           
    def PushP2RightGloveData(self, data, rssi=0):
        self.p2RightGloveLock.acquire()
        if (self.p2['R'] == None):
            self.p2['R'] = data
        self._PushQueueData(self.p2RightGloveQueue, data - self.p2['R'])
        self.p2['newR'] = 1
        self.p2['R'] = data
        self.rssi_info[ReceiverQueueMgr.P2_RIGHT_RSSI] = rssi
        self.p2RightGloveLock.release()
                
    def PushP2HeadsetData(self, data):
        self.p2HeadsetLock.acquire()
        self._PushQueueData(self.p2HeadsetQueue, data)
        self.rssi_info[ReceiverQueueMgr.P2_HEADSET_RSSI] = data.link
        self.p2HeadsetLock.release()    
    
    def PopP1LeftGloveData(self):
        if self.p1['newL'] == 0: return
        self.p1LeftGloveLock.acquire()
        data = self._PopQueueData(self.p1LeftGloveQueue)
        self.p1LeftGloveLock.release()
        if (data != None): self.p1['newL'] = 0
        return data
        
    def PopP1RightGloveData(self):
        if self.p1['newR'] == 0: return
        self.p1RightGloveLock.acquire()
        data = self._PopQueueData(self.p1RightGloveQueue)
        self.p1RightGloveLock.release()
        if (data != None): self.p1['newR'] = 0
        return data
                
    def PopP1HeadsetData(self):
        self.p1HeadsetLock.acquire()
        data = self._PopQueueData(self.p1HeadsetQueue)
        self.p1HeadsetLock.release()
        return data
        
    def PopP2LeftGloveData(self):
        if self.p2['newL'] == 0: return
        self.p2LeftGloveLock.acquire()
        data = self._PopQueueData(self.p2LeftGloveQueue)
        self.p2LeftGloveLock.release()
        if (data != None): self.p2['newL'] = 0
        return data
           
    def PopP2RightGloveData(self):
        if self.p2['newR'] == 0: return
        self.p2RightGloveLock.acquire()
        data = self._PopQueueData(self.p2RightGloveQueue)
        self.p2RightGloveLock.release()
        if (data != None): self.p2['newR'] = 0
        return data
                
    def PopP2HeadsetData(self):
        self.p2HeadsetLock.acquire()
        data = self._PopQueueData(self.p2HeadsetQueue)
        self.p2HeadsetLock.release()
        return data
    
    
    def GetRSSIMap(self):
        return self.rssi_info
    
    
    def _PushQueueData(self, queue, data):
        # Don't push empty data
        if data == None:
            return
        # deque will automatically keep within maxlength
        #print "Data is being placed on a receiver queue"
        queue.append(data)
    
    def _PopQueueData(self, queue):
        if len(queue) < ReceiverQueueMgr.MAX_QUEUE_SIZE:
            #print "Tried to retrieve data from an empty receiver queue."
            return None
        else:
            # Average all of the data on the queue and return the result,
            count    = len(queue)
            avgSum = queue[0]
            for i in range(1, count):
                avgSum += queue[i]
                
            assert(avgSum != None)
            assert(count > 0)
            
            av = avgSum/count
            #print "count %d avg=%s" % (count, av)
            return av
    
    
    
    