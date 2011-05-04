import serial
import string


# Check your COM port and baud rate
ser = serial.Serial(port='COM3',baudrate=57600, timeout=1)

while 1:
    line = ser.read(128)
    print line
    

ser.close