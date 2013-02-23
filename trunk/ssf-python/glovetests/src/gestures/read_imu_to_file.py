# Test for Razor 9DOF IMU - Jose Julio @2009
# This script needs Python2.7, VPhyton, pyserial and pywin modules
from visual import *
import serial
import string
import math
import time
import operator
import collections
import matplotlib.pyplot as plot
from mpl_toolkits.mplot3d import Axes3D

grad2rad = 3.141592/180.0

# Check your COM port and baud rate
ser = serial.Serial(port='COM3',baudrate=57600, timeout=1)

# Main scene
scene=display(title="9DOF Razor IMU test")
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
label(pos=(0,0,0.8),text="9DOF Razor IMU test",box=0,opacity=0)
label(pos=(1,0,0),text="X",box=0,opacity=0)
label(pos=(0,-1,0),text="Y",box=0,opacity=0)
label(pos=(0,0,-1),text="Z",box=0,opacity=0)
# IMU object
platform = box(length=1, height=0.05, width=1, color=color.red)
p_line = box(length=1,height=0.08,width=0.1,color=color.yellow)
plat_arrow = arrow(color=color.green,axis=(1,0,0), shaftwidth=0.06, fixedwidth=1)


f = open("sdata"+str(time.time())+"-clean.txt", 'w')
fav = open("sdata"+str(time.time())+"-av.txt", 'w')
fr = open("sdata"+str(time.time())+"-deltas.txt", 'w')

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

print " CALIBRATION BEGINS ( be still and level ) "
isStarted = False
start = time.time();
elapsed = 0

# after x seconds, break
# base calibration takes ~5seconds
while elapsed < 18: 
    now = time.time();
    elapsed = now-start;

    #print/log line
    line = ser.read(256)
    
    if line.find("1R")  == -1:
        if elapsed < 3: print "   CALIBRATING .. "
        f.write(line)
        continue  # skip the non-movement-data lines

    #put in the time elapsed & just one newline
    clean = '{:.3f}_{}\n'.format(elapsed, line.replace(' ',''));
    f.write(clean)                     # Write to the output log file

    # we're going to throw away a bunch of data on either end - but that's OK
    lines = clean.split("|")
    for line in lines:
    
        head = []
        dataline = line.partition(":")    # Fields split (five items, unless we're just starting)
        player = dataline[0]
        blocks = dataline[2].split("_")
        
        if len(blocks) > 3:
            if isStarted == False:
                print " *********** GO ************ "
                isStarted = True
    
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
            
            # slightly less than raw data
            #print '%.3f Heading: %.2f,%.2f,%.2f  avAcc: %s  Gyros: %s ' % (elapsed, pR[-1],pP[-1],pY[-1], str(avA[0]), str(dG))        
    
            # write out the raw
            rout = '{}:{:4.0f},{:4.0f},{:4.0f}_{:4.0f},{:4.0f},{:4.0f}_{:4.0f},{:4.0f},{:4.0f}_{:.3f}\n'.format(player,pR[-1],pP[-1],pY[-1],
                                                                                                    dA[x],dA[y],dA[z],dG[x],dG[y],dG[z],elapsed)
            fr.write(rout)
            # write out the data with average accelerations.
            aout = '{}:{:4.0f},{:4.0f},{:4.0f}_{:>7.2f},{:>7.2f},{:>7.2f}_{:4.0f},{:4.0f},{:4.0f}_{:.3f}\n'.format(player,pR[-1],pP[-1],pY[-1],
                                                                                                    avA[0][x],avA[0][y],avA[0][z], dG[x],dG[y],dG[z], elapsed)
            fav.write(aout)
    
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
acx = list(a[x] for a in avA)
acy = list(a[y] for a in avA)
acz = list(a[z] for a in avA)
ax.plot(acx, acy, acz)
#ax.plot(pR, pP, pY)
plot.show()