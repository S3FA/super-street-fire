'''
receiver_from_file.py

Allows us to use IMU data previously recorded to file.
'''

import parser
import threading
import os
import time

class FileReceiver(threading.Thread):

    def __init__(self, receiverQueueMgr):
        threading.Thread.__init__(self)
        
        assert(receiverQueueMgr != None)
        self.queueMgr           = receiverQueueMgr
        self.exitThread         = False
        self.lock               = threading.Semaphore()

        # read test files in designated directory
        dirList=os.listdir(".")
        testfile = ""
        for fname in dirList: 
            if fname.find("-av") > -1: testfile = fname
    
        print 'found a test file: %s ' % (testfile)
        self.inputfile = open(testfile, 'r')

    def ExitThread(self):
        self.lock.acquire()
        self.exitThread = True
        self.lock.release()

    def run(self):
        if self.inputfile == None:
            print "ERROR: unable to read from input file "
            return
        
        print "Running file receiver..."
        cleandata = ""
        
        self.lock.acquire() 
        for line in self.inputfile:
            self.lock.release()
            
            time.sleep(0.01)
            cleandata = line.replace(' ','')
            #print cleandata
            parser.ParseSerialData(cleandata, self.queueMgr)
            self.lock.acquire()
            
        self.lock.release()
        self.inputfile.close()
        
        
        