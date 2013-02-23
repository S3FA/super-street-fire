from xbee import ZigBee
import serial
import struct
import re
import os 

eegNames = ['Link', 'Att', 'Med', 'D', 'Th', 'LAl', 'HAl', 'LBe', 'HBe', 'LGa', 'HGa']

P1H = '0x13a20040690a34L';
# seth's device ..
SERIAL = '/dev/tty.usbserial-A600e0xG'
# windows serial
if (os.name.find('nt')>-1):
    SERIAL = 'COM5'
    
ser = serial.Serial(SERIAL, 57600)

xbee = ZigBee(ser, escaped=True)

f = open("xbee_random.txt", 'w+')

# Continuously read and print packets
while True:
    try:
        response = xbee.wait_read_frame()
        #if response['id'] == 'rx':
        #    source = hex(struct.unpack('>Q', response['source_addr_long'])[0])
        #    if source == P1H:
        #        source = 'P1H'
        #        eeg = response['rf_data'].rstrip().split(',');
        #        out = 'RX ' + source + ': '
        #        for index,item in enumerate(eeg):
        #            out += eegNames[index] + ' ' + item + ' '
        #        print out
        print response
        source = struct.unpack(">q", response['source_addr_long'])
        data = "%d:%s\n" % ( source[0] , response['rf_data'] )
        f.write(data)
    except KeyboardInterrupt:
        break
        
ser.close()
