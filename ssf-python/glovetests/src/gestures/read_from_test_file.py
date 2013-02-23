import string
import os
from mpl_toolkits.mplot3d import Axes3D
from ssf_moves import *

# read test files in currect directory
DIR = "../../testdata/"
dirList=os.listdir(DIR)
testfile = ""
for fname in dirList: 
    if fname.find("-av") > -1: testfile = DIR+fname
    
print 'found a test file: %s ' % (testfile)
f = open(testfile, 'r')

x=0
y=1
z=2

players = ( SSFPlayer(1), SSFPlayer(2) )
ssf = SSFMoves()

for line in f:
    #print line
    #ignore the file header
    if line.startswith("P"): continue;
    
    #print line
    
    # expecting Heading, Av Acc, dGyro, time
    dataline = line.partition(":")    # Fields split (five items, unless we're just starting)
    blocks = dataline[2].replace(' ','').split("_")
    
    if len(blocks) > 3:
        playerId = int(dataline[0][0]) # either 1/2
        gloveId = dataline[0][1] # either L/R
        
        head = string.split(blocks[0],",")
        accel = string.split(blocks[1],",")
        gyros = string.split(blocks[2],",")
        elapsed = blocks[3].rstrip()
        
        avA = ( float(accel[0]), float(accel[1]), float(accel[2]) )
        dG = ( gyros[0], gyros[1], gyros[2])
        headings = ( float(head[0]), float(head[1]), float(head[2]) )
        
                          
        # detect move from headings, delta gyros and average acceleration 
        ssf.setupData(gloveId, headings, dG, avA)
        # hard-code the right hand - need another test input
        ssf.setupData('R', headings, dG, avA)
        ssf.determineMove(players[playerId])
        
        # print current move (if changed)
        move = SSFPlayer.showMove(players[playerId])   
        if (move != ""):
            print '%s: %s -- headings %s -- acceleration %s -- gyros %s ' % (elapsed, move, str(headings), str(avA), str(dG))

print 'Done - closing resources'  
f.close
