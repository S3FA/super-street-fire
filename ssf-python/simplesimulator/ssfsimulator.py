'''
simple simulator for SUPER STREET FIRE

reads in bytes from the serial port in the same format that the wifire boards will expect:
  2 bytes for fire control:   16[x x x x x x x x][x x x x x x x x]1
  2 bytes for player 1 color: 16[x x x x x x x x][x x x x x x x x]1
  2 bytes for player 2 color: 16[x x x x x x x x][x x x x x x x x]1


'''

from visual import *
import serial
import sys
import os
from struct import unpack

p0 = color.white
p1 = color.red
p2 = color.blue
p3 = color.magenta

def seteffectcolor(e, n, c):
    e[n].color = c
    return

display(title='SUPER STREET FIRE Simulator', width=600, height=600, range=7)

effect = []
locs = [(-5,1,0), (-4,2,0), (-2,4,0), (-1,5,0),
        (1,5,0), (2,4,0), (4,2,0), (5,1,0),
        (5,-1,0), (4,-2,0), (2,-4,0), (1,-5,0),
        (-1,-5,0), (-2,-4,0), (-4,-2,0), (-5,-1,0)
        ]

for v in locs:
    e = sphere(pos=v, color=color.white, radius=0.5)
    effect.append(e)

player = []
p = sphere(pos=(-3,0,0), color=color.red, radius=1)
player.append(p)
p = sphere(pos=(3,0,0), color=color.blue, radius=1)
player.append(p)

simPort = '/dev/slave'
if (os.name.find("nt") > -1):
    simPort = "COM2"
s = serial.Serial(port=simPort, baudrate=57600)

#rate(4)

firestate = ''
p1colorstate = ''
p2colorstate = ''

while True:
    if( s.inWaiting() >= 6 ):
        firestate, p1colorstate, p2colorstate = unpack("<HHH", s.read(6))
        print firestate 
        for x in range(15,-1,-1):
            fireon = firestate & 1<<x
            p1on = p1colorstate & 1<<x
            p2on = p2colorstate & 1<<x
            if fireon:
                if p1on and p2on:
                    seteffectcolor(effect, x, p3)
                elif p2on:
                    seteffectcolor(effect, x, p2)
                elif p1on:
                    seteffectcolor(effect, x, p1)
            else:
                seteffectcolor(effect, x, p0)
                



    
