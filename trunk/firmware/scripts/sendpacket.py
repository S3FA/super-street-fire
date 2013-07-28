#!/usr/bin/env python

import math
import sys
import serial
import time

def send_packet(ser, addr, data):
    checksum = 0
    for n in data:
        checksum += n
    checksum = (255 ^ checksum) & 0xFF

    lst = [
        0xAA,
        0xAA,
        len(data),
        addr]
    lst += data
    lst += [checksum]

    ser.write("".join([chr(n) for n in lst]))
    ser.flush()
    time.sleep(0.008)

ser = serial.Serial("/dev/ttyUSB0", 38400)
# Sleep to avoid talking to the bootloader, in case we auto-reset
time.sleep(1)

def demo_timer():
    while 1:
        print "Sending data"
        for n in range(100):
            tm = time.time()
            r = 128 + 124*math.sin(tm*2)
            g = 128 + 124*math.sin(tm*3)
            b = 128 + 124*math.sin(tm*4)

            send_packet(ser, 35, [ord("T"), n])
            time.sleep(0.1)
            send_packet(ser, 35, [ord("t"), int(r), int(g), int(b)])

def demo_life():
    print "Sending life change"
    #while 1:
    addr = 34
    while 1:
        tm = time.time()
        life = 10 + int(9*math.sin(tm*2))
        chg = 10 + int(9*math.sin(tm*3))

        r = 128 + int(100*math.sin(tm*2))
        g = 128 + int(100*math.sin(tm*3))
        b = 128 + int(100*math.sin(tm*5))

        send_packet(ser, addr, [ord("L"), life])
        send_packet(ser, addr, [ord("C"), chg])
        send_packet(ser, addr, [ord("l"), b, g, r])
        send_packet(ser, addr, [ord("c"), r, g, b])

#demo_life()
demo_timer()

print "Waiting response"
# Read back the response
while True:
    print(ser.readline().strip())
