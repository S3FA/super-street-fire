# Test for Razor 9DOF IMU - Jose Julio @2009
# This script needs Python2.7, VPhyton, pyserial and pywin modules
from visual import *
import serial
import string
import sys
import math
import time
import operator
import collections
import matplotlib.pyplot as plot
from mpl_toolkits.mplot3d import Axes3D
from xbee import ZigBee
from ssf_moves import *

grad2rad = 3.141592/180.0

ports = ('COM5','COM6','COM3')

# Check your COM port and baud rate
for port in ports:
    try:
        ser = serial.Serial( port=port,baudrate=57600 )
        xbee = ZigBee(ser, escaped=True)
        break
    except serial.serialutil.SerialException:
        pass

if xbee == None: 
    print "unable to find the xbee serial line.. "
    exit

# Main scene
scene=display(title="Super Street Fire gesture test")
scene.range=(1.2,1.2,1.2)
scene.forward = (1,0,-0.25)
scene.up=(0,0,1)

# Second scene (Roll, Pitch, Yaw)
scene2 = display(title='9DOF Razor IMU test',x=0, y=0, width=500, height=200,center=(0,0,0), background=(0,0,0))
scene2.range=(1,1,1)
scene.width=500
scene.y=200

scene2.select()
#Roll, Pitch, Yaw
cil_roll = cylinder(pos=(-0.4,0,0),axis=(0.2,0,0),radius=0.01,color=color.red)
cil_roll2 = cylinder(pos=(-0.4,0,0),axis=(-0.2,0,0),radius=0.01,color=color.red)
cil_pitch = cylinder(pos=(0.1,0,0),axis=(0.2,0,0),radius=0.01,color=color.green)
cil_pitch2 = cylinder(pos=(0.1,0,0),axis=(-0.2,0,0),radius=0.01,color=color.green)
arrow_course = arrow(pos=(0.6,0,0),color=color.cyan,axis=(-0.2,0,0), shaftwidth=0.02, fixedwidth=1)

#Roll,Pitch,Yaw labels
label(pos=(-0.4,0.3,0),text="Roll",box=0,opacity=0)
label(pos=(0.1,0.3,0),text="Pitch",box=0,opacity=0)
label(pos=(0.55,0.3,0),text="Yaw",box=0,opacity=0)
label(pos=(0.6,0.22,0),text="N",box=0,opacity=0,color=color.yellow)
label(pos=(0.6,-0.22,0),text="S",box=0,opacity=0,color=color.yellow)
label(pos=(0.38,0,0),text="W",box=0,opacity=0,color=color.yellow)
label(pos=(0.82,0,0),text="E",box=0,opacity=0,color=color.yellow)
label(pos=(0.75,0.15,0),height=7,text="NE",box=0,color=color.yellow)
label(pos=(0.45,0.15,0),height=7,text="NW",box=0,color=color.yellow)
label(pos=(0.75,-0.15,0),height=7,text="SE",box=0,color=color.yellow)
label(pos=(0.45,-0.15,0),height=7,text="SW",box=0,color=color.yellow)

L1 = label(pos=(-0.4,0.22,0),text="-",box=0,opacity=0)
L2 = label(pos=(0.1,0.22,0),text="-",box=0,opacity=0)
L3 = label(pos=(0.7,0.3,0),text="-",box=0,opacity=0)

# Main scene objects
scene.select()
# Reference axis (x,y,z)
arrow(color=color.green,axis=(1,0,0), shaftwidth=0.02, fixedwidth=1)
arrow(color=color.green,axis=(0,-1,0), shaftwidth=0.02 , fixedwidth=1)
arrow(color=color.green,axis=(0,0,-1), shaftwidth=0.02, fixedwidth=1)
# labels
label(pos=(0,0,0.8),text="SSF Razor IMU test",box=0,opacity=0)
label(pos=(1,0,0),text="X",box=0,opacity=0)
label(pos=(0,-1,0),text="Y",box=0,opacity=0)
label(pos=(0,0,-1),text="Z",box=0,opacity=0)
# IMU object
platform = box(length=1, height=0.05, width=1, color=color.red)
p_line = box(length=1,height=0.08,width=0.1,color=color.yellow)
plat_arrow = arrow(color=color.green,axis=(1,0,0), shaftwidth=0.06, fixedwidth=1)


f = open("wdata"+str(time.time())+"-clean.txt", 'w')
fav = open("wdata"+str(time.time())+"-av.txt", 'w')
fr = open("wdata"+str(time.time())+"-deltas.txt", 'w')

x=0
y=1
z=2

pAcc = ( collections.deque(), collections.deque() )
pGyro = ( collections.deque(), collections.deque() )
dAXT = ( collections.deque(list(), 5), collections.deque(list(), 5) )
dAYT = ( collections.deque(list(), 5), collections.deque(list(), 5) )
dAZT = ( collections.deque(list(), 5), collections.deque(list(), 5) )
dGXT = ( collections.deque(list(), 5), collections.deque(list(), 5) )
dGYT = ( collections.deque(list(), 5), collections.deque(list(), 5) )
dGZT = ( collections.deque(list(), 5), collections.deque(list(), 5) )
avA = ( list(), list() )
avG = ( list(), list() )
dH = ()
dA = ()
dG = ()
pR = ( list(), list() )
pP = ( list(), list() )
pY = ( list(), list() )

players = ( SSFPlayer(1), SSFPlayer(2) )
ssf = SSFMoves()

print " READY .. "
isStarted = False
start = time.time();
lastmovets = 0
elapsed = 0
line = ''
i = 0

# after x seconds, break
# base calibration takes ~5seconds
while elapsed < 26: 
    now = time.time();
    elapsed = now-start;

    response = xbee.wait_read_frame()
    rfdata = response['rf_data'].replace(' ','')

    if (rfdata.find(':') == -1 or rfdata.find('|') == -1):
        #incomplete line..
        rfdata += response['rf_data'].replace(' ','')

    #put in the time elapsed & with the data
    clean = '{:.3f}_{}'.format(elapsed, rfdata);
    f.write(clean)                     # Write to the output log file

    lines = rfdata.split('|')
    for line in lines:
        head = []
        findNodeId = line.rfind(":")
        # throw away some invalid lines
        if (findNodeId == -1): continue

        nodeId = line[findNodeId-2:findNodeId]
        blocks = line[findNodeId+1:].split("_")
        
        if len(nodeId) == 2 and len(blocks) > 3:
            if isStarted == False:
                print " *********** GO ************ "
                isStarted = True
            
            try:
                playerId = int(nodeId[0]) # either 1/2
                gloveId = nodeId[1] # either L/R
                head = string.split(blocks[0],",")
                gyros = string.split(blocks[1],",")
                accel = string.split(blocks[2],",")
                if (len(head) != 3 or len(gyros) != 3 or len(accel) != 3 or playerId > 2):
                    print "Bad Data: %s - %s - %s " % (nodeId, playerId, blocks)
                    continue
                
                glove = 0
                if (gloveId == 'R'): glove = 1
                pR[glove].append(float(head[0]))
                pP[glove].append(float(head[1]))
                pY[glove].append(float(head[2])) 
    
                if len(pAcc[glove]) == 5:
                    pAcc[glove].pop()
                    pGyro[glove].pop()
        
                pGyro[glove].appendleft( [float(gyros[x]),float(gyros[y]),float(gyros[z])] )    
                pAcc[glove].appendleft( [float(accel[x]),float(accel[y]),float(accel[z])] )
            except: 
                print "Bad Data: %s - %s - %s" % (nodeId, blocks, sys.exc_info()[0])
                continue

    
            dA  = tuple(map(operator.sub, pAcc[glove][0], pAcc[glove][-1] ))
            dAXT[glove].appendleft(dA[x])
            dAYT[glove].appendleft(dA[y])
            dAZT[glove].appendleft(dA[z])
    
            # smooth out the acceleration
            # average the delta A - over "n" ~5 items 
            avA[glove].insert(0, ( float(sum(dAXT[glove])) / len(dAXT[glove]),
                    float(sum(dAYT[glove])) / len(dAYT[glove]),
                    float(sum(dAZT[glove])) / len(dAZT[glove])) )
              
            dG  = tuple(map(operator.sub, pGyro[glove][0], pGyro[glove][-1] ))
            dGXT[glove].appendleft(dG[x])
            dGYT[glove].appendleft(dG[y])
            dGZT[glove].appendleft(dG[z])
    
            # smooth out the acceleration
            # average the delta A - over "n" ~5 items 
            avG[glove].insert(0, ( float(sum(dGXT[glove])) / len(dGXT[glove]),
                    float(sum(dGYT[glove])) / len(dGYT[glove]),
                    float(sum(dGZT[glove])) / len(dGZT[glove])) )
              
            
            # slightly less than raw data
            #print '%.3f Heading: %.2f,%.2f,%.2f  avAcc: %s  Gyros: %s ' % (elapsed, pR[-1],pP[-1],pY[-1], str(avA[0]), str(dG))        
    
            # write out the raw
            rout = '{}:{:4.0f},{:4.0f},{:4.0f}_{:4.0f},{:4.0f},{:4.0f}_{:4.0f},{:4.0f},{:4.0f}_{:.3f}\n'.format(nodeId,pR[glove][-1],pP[glove][-1],pY[glove][-1],
                                                                                                    dA[x],dA[y],dA[z],
                                                                                                    dG[x],dG[y],dG[z],elapsed)
            fr.write(rout)
            # write out the data with average accelerations.
            aout = '{}:{:4.0f},{:4.0f},{:4.0f}_{:>7.2f},{:>7.2f},{:>7.2f}_{:4.0f},{:4.0f},{:4.0f}_{:.3f}\n'.format(nodeId,pR[glove][-1],pP[glove][-1],pY[glove][-1],
                                                                                                    avA[glove][0][x],avA[glove][0][y],avA[glove][0][z], 
                                                                                                    dG[x],dG[y],dG[z], elapsed)
            fav.write(aout)
    
            # detect move from headings, delta gyros and average acceleration 
            players[playerId].setupData(gloveId, (pR[glove][-1],pP[glove][-1],pY[glove][-1]), dG, avA[glove][0])
            
            if players[playerId].bothHandsSet():
                
                ssf.determineMove(players[playerId])            
                # print current move (if changed)
                move = SSFPlayer.showMove(players[playerId])
                if move != "" and (now - lastmovets) > 0.325:
                    print "\n -- %s -- \n" % (move)
                    lastmovets = now;

            if (nodeId == '1L'):
                continue
            # original sample code for the visualizer
            try:
                roll = float(head[0])*grad2rad
                pitch = float(head[1])*grad2rad
                yaw = float(head[2])*grad2rad
                
                axis=(cos(pitch)*cos(yaw),-cos(pitch)*sin(yaw),sin(pitch)) 
                up=(sin(roll)*sin(yaw)+cos(roll)*sin(pitch)*cos(yaw),sin(roll)*cos(yaw)-cos(roll)*sin(pitch)*sin(yaw),-cos(roll)*cos(pitch))
                platform.axis=axis
                platform.up=up
                platform.length=1.0
                platform.width=0.65
                plat_arrow.axis=axis
                plat_arrow.up=up
                plat_arrow.length=0.8
                p_line.axis=axis
                p_line.up=up
                cil_roll.axis=(0.2*cos(roll),0.2*sin(roll),0)
                cil_roll2.axis=(-0.2*cos(roll),-0.2*sin(roll),0)
                cil_pitch.axis=(0.2*cos(pitch),0.2*sin(pitch),0)
                cil_pitch2.axis=(-0.2*cos(pitch),-0.2*sin(pitch),0)
                arrow_course.axis=(0.2*sin(yaw),0.2*cos(yaw),0)
                L1.text = str(float(head[0]))
                L2.text = str(float(head[1]))
                L3.text = str(float(head[2]))      
            except: 
                pass # don't care


print 'Done - closing resources'  
ser.close
fav.close
fr.close
f.close

fig = plot.figure()
ax = fig.add_subplot(111, projection='3d')
#x = list(a[x] for a in avA[1])
#y = list(a[y] for a in avA[1])
#z = list(a[z] for a in avA[1])
x = list(a[x] for a in avG[1])
y = list(a[y] for a in avG[1])
z = list(a[z] for a in avG[1])
ax.plot(x, y, z)
#ax.plot(pR, pP, pY)
plot.show()