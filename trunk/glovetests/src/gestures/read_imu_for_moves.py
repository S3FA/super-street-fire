import serial
import string
import time
import collections
import operator
import matplotlib.pyplot as plot
from mpl_toolkits.mplot3d import Axes3D
from ssf_moves import *

# Check your COM port and baud rate
ser = serial.Serial(port='COM3',baudrate=57600, timeout=1)

f = open("sdata"+str(time.time())+"-clean.txt", 'w')
fav = open("sdata"+str(time.time())+"-av.txt", 'w')

x=0
y=1
z=2

pAcc = collections.deque()
pGyro = collections.deque()

dAXT = collections.deque(list(), 5)
dAYT = collections.deque(list(), 5)
dAZT = collections.deque(list(), 5)
avA = list()
dH = ()
dA = ()
dG = ()
pR = list()
pP = list()
pY = list()

player = SSFPlayer(1)
ssf = SSFMoves()

print " CALIBRATION BEGINS ( be still and level ) "
isStarted = False
start = time.time()
elapsed = 0;

# after x seconds, break - calibration takes ~5s
while elapsed < 15:
    now = time.time();
    elapsed = now-start;

    line = ser.read(256)
    
    if line.find("1R")  == -1:
        if elapsed < 3: print "  CALIBRATING .. "
        #print line
        f.write(line)
        continue  # skip the non-movement-data lines
    

    #put in the time elapsed & just one newline
    clean = '{:.3f}_{}\n'.format(elapsed, line.replace(' ',''));
    f.write(clean)                     # Write to the output log file

    lines = clean.split("|")
    for line in lines:
        dataline = line.partition(":")    # Fields split player ID to data
        
        blocks = dataline[2].split("_")   # split the blocks of sensors
        
        head = []
        if len(blocks) > 3:
            if isStarted == False:
                print " *********** GO ************ "
                isStarted = True
    
            print "dataline %s=%s" % (dataline[0], blocks)
            playerId = dataline[0][0] # either 1/2
            gloveId = dataline[0][1] # either L/R
            head = string.split(blocks[0],",")
            gyros = string.split(blocks[1],",")
            accel = string.split(blocks[2],",")
            
            if len(pAcc) == 5:
                pAcc.pop()
                pGyro.pop()
    
            pGyro.appendleft( [float(gyros[x]),float(gyros[y]),float(gyros[z])] )
            dG  = tuple(map(operator.sub, pGyro[0], pGyro[-1] ))
    
            pAcc.appendleft( [float(accel[x]),float(accel[y]),float(accel[z])] )
    
            dA  = tuple(map(operator.sub, pAcc[0], pAcc[-1] ))
            dAXT.appendleft(dA[x])
            dAYT.appendleft(dA[y])
            dAZT.appendleft(dA[z])
    
            # smooth out the acceleration
            # average the delta A - over "n" ~5 items 
            avA.insert(0, ( float(sum(dAXT)) / len(dAXT),
                    float(sum(dAYT)) / len(dAYT),
                    float(sum(dAZT)) / len(dAZT)) )
              
            pR.append(float(head[0]))
            pP.append(float(head[1]))
            pY.append(float(head[2])) 
            
            # detect move from headings, delta gyros and average acceleration 
            ssf.setupData(gloveId, (pR[-1],pP[-1],pY[-1]), dG, avA[0])
            # hard-code the right hand - need another test input
            ssf.setupData('R', (pR[-1],pP[-1],pY[-1]), dG, avA[0])
            ssf.determineMove(player)
            
            # print current move (if changed)
            move = SSFPlayer.showMove(player)
            if move != "":
                print "\n -- %s -- \n" % (move)
            
            # slightly less than raw data
            print 'Headings: %.2f,%.2f,%.2f  Av Accel: %s  Gyros: %s ' % (pR[-1],pP[-1],pY[-1], str(avA[0]), str(dG))        
    
            # write out the data with average accelerations.
            aout = '{}:{:4.0f},{:4.0f},{:4.0f}_{:>7.2f},{:>7.2f},{:>7.2f}_{:4.0f},{:4.0f},{:4.0f}_{:.3f}\n'.format(
                        dataline[0],
                        pR[-1],pP[-1],pY[-1],
                        avA[0][x],avA[0][y],avA[0][z], 
                        dG[x],dG[y],dG[z], elapsed)
            fav.write(aout)



print 'Done - closing resources'  
ser.close
fav.close
f.close
