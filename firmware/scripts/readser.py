#!/usr/bin/env python

import serial

dev = "/dev/ttyUSB0"

ser = serial.Serial(dev, 38400)
print "Reading...", dev
# Read back the response
while True:
	print(ser.readline().strip())
