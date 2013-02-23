import serial
from xbee import ZigBee
from binascii import hexlify

ser = serial.Serial('COM6', 57600)
xbee = ZigBee(ser, escaped=True)

wifirename  = ''
wifireaddr  = ''
wifireaddrS = ''

# find node
xbee.at(command='ND')
nodefound = 0
numnodes = 2

# this currently works with a known number of nodes
# maybe have it work with a timeout instead?
while nodefound < numnodes:
    response = xbee.wait_read_frame()
    if response['id'] == 'at_response':
        if response['command'] == 'ND':
            parameter = response['parameter']
            wifireaddrS = parameter[0:2]
            wifireaddr  = parameter[2:10]
            wifirename  = parameter[10:]
            index = wifirename.find('\x00')
            wifirename = wifirename[:index]
            #where is hexlify ? 
            print "received node reply for node %s short %s long %s" \
                % (wifirename, hexlify(wifireaddrS), hexlify(wifireaddr))
            nodefound += 1
