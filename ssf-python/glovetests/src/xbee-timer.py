from xbee import ZigBee
import serial
import struct
import re
from time import sleep
from binascii import hexlify
from struct import pack
import time

ADDR_TABLE = {
    'SSFP1L'    : ['',''],
    'SSFP1R'    : ['',''],
    'SSFP1H'    : ['',''],
    'SSFP2L'    : ['',''],
    'SSFP2R'    : ['',''],
    'SSFP2H'    : ['',''],
    'SSFFIRE'   : ['',''],
    'SSFTIMER'  : ['',''],
    'SSFP1LIFE' : ['',''],
    'SSFP2LIFE' : ['',''],
    'SSFLIGHTS' : ['',''],
}

def AddToAddrTable(response):
	#print response
	if response['id'] == 'at_response':
		if response['command'] == 'ND':
			parameter = response['parameter']
			destaddrS = parameter[0:2]
			destaddr  = parameter[2:10]
			destname  = parameter[10:]
			index = destname.find('\x00')
			destname = destname[:index]
			ADDR_TABLE[destname] = [destaddr, destaddrS]
			#ADDR_TABLE[destname].append(destaddr)
			#ADDR_TABLE[destname].append(destaddrS)
			print "received node reply for node %s short %s long %s" \
				% (destname, hexlify(destaddrS), hexlify(destaddr))
			return 1
	return 0

def SendTimerNum(value):
    
	# based on the following digit map:
	#
	#   x0x          x8x
	# x     x      x     x
	# 5     1      d     9
	# x     x      x     x
	#   x6x          xex
	# x     x      x     x
	# 4     2      c     a
	# x     x      x     x
	#   x3x          xbx     

	if (value < 0) or (value > 99):
		return

	digitMap = [0x3F, 0x06, 0x5B, 0x4F, 0x66, 0x6D, 0x7D, 0x07, 0x7F, 0x6F]
	timerData = (digitMap[value / 10] << 8) | digitMap[value % 10]
	timerPacked = pack("H",timerData)
	xbee.send('tx',dest_addr_long=ADDR_TABLE['SSFTIMER'][0], dest_addr=ADDR_TABLE['SSFTIMER'][1], data=timerPacked)

# -------------------

ser = serial.Serial('/dev/tty.xbee', 57600)
xbee = ZigBee(ser, callback=AddToAddrTable, escaped=True)

xbee.at(command='ND')
sleep(10)

print ADDR_TABLE

'''
xbee.send('tx', dest_addr_long=ADDR_TABLE['SSFTIMER'][0], dest_addr=ADDR_TABLE['SSFTIMER'][1], data='\x00\x00')
sleep(1)

while True:

	for i in range(99,0,-1):

		try:
			SendTimerNum(i)
			#xbee.send('tx',dest_addr_long=destaddr, dest_addr=destaddrS, data=struct.pack(">H",1<<i))
			#response = xbee.wait_read_frame()
			#print response['id']
			#if response['id'] == 'tx':
			#print response['rf_data']
		except KeyboardInterrupt:
			break
		sleep(0.5)
'''

ser.close()
