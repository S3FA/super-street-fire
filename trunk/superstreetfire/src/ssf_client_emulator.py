'''
client_emulator.py

This contains a client emulator - this can be used to emulate
all of the clients that will be sending data to the superstreetfire
server.

@author: Callum Hay
'''


import random
#import threading
import serial
import time
import sys

from optparse import OptionParser

'''
class ClientEmulator(threading.Thread):

    def __init__(self, outputSerialPortName, baudRate):
        threading.Thread.__init__(self)
        try:
            # NOTE: timeout=x means we wait up to x seconds to read from the serial port
            self.serialOutputPort = serial.Serial(outputSerialPortName, baudrate=baudRate, timeout=1)
        except serial.SerialException:
            print "ERROR: Serial port " + outputSerialPortName + " was invalid/not found."
            exit(-1)
    
    def run(self):
        # Make sure this object is in a proper state before running...
        if self.serialOutputPort == None:
            print "ERROR: Serial port was invalid/not found, could not run receiver."
            return
        
        while True:
            # Generate a fake serial input...
            outputData = GenerateSerialInput(random.randrange(1, 10)) 
            self.serialOutputPort.write(outputData)
            time.sleep(0.02) # Simulate 50Hz
'''
            
    
def GenerateSerialInput(num=1):
    finalString = ""
    
    randomCount = 0
    if num != None:
        randomCount = num
    
    while randomCount > 0:
        randomNum = random.randrange(0, 7)
        
        randomRotXYZ = GenerateRandomXYZDeg()
        randomAccXYZ = GenerateRandomXYZAcc()
        randomMagXYZ = GenerateRandomXYZMag()
        gloveStr = randomRotXYZ + randomAccXYZ + randomMagXYZ
        
        elevenRandomVals = [random.random() for i in range(11)] 
        
        if randomNum == 0:
            finalString += "1L:" + ",".join(map(str, gloveStr))
        elif randomNum == 1:
            finalString += "1R:" + ",".join(map(str, gloveStr))
        elif randomNum == 2:
            finalString += "2L:" + ",".join(map(str, gloveStr))
        elif randomNum == 3:
            finalString += "2R:" + ",".join(map(str, gloveStr))
        elif randomNum == 4:
            finalString += "1H:" + ",".join(map(str, elevenRandomVals)) 
        elif randomNum == 5:
            finalString += "2H:" + ",".join(map(str, elevenRandomVals)) 
        finalString += "\n"
        randomCount -= 1
    
    return finalString

def GenerateRandomDeg():
    return (2 * random.random() - 1) * 180.0
def GenerateRandomAcc():
    return (2 * random.random() - 1) * 500.0
def GenerateRandomMag():
    return (2 * random.random() - 1) * 500.0
    
def GenerateRandomXYZDeg():
    return [GenerateRandomDeg(), GenerateRandomDeg(), GenerateRandomDeg()]
def GenerateRandomXYZAcc():
    return [GenerateRandomAcc(), GenerateRandomAcc(), GenerateRandomAcc()]
def GenerateRandomXYZMag():
    return [GenerateRandomMag(), GenerateRandomMag(), GenerateRandomMag()]


if __name__ == '__main__':

    
    # Parse options from the command line
    usageStr = "usage: %prog -p EMULATOR_PORT"
    cmdLineDescStr = "The client emulator for the superstreetfire application..."
    argParser = OptionParser(usage=usageStr, description=cmdLineDescStr)
    argParser.add_option("-p", action="store", type="string", dest="emulatorPort", \
                         help="The serial port name/number that is used to emulate all the clients. [%default]", \
                         default="/dev/master")       
    
    (options, args) = argParser.parse_args()
    DEFAULT_BAUDRATE = 57600
    
    if options.emulatorPort == None:
        argParser.error("An emulator port must be specified")
        
        
    try:    
        serialOutputPort = serial.Serial(options.emulatorPort, baudrate=DEFAULT_BAUDRATE, timeout=1)
    except serial.SerialException:
        print "ERROR: Serial port " + options.emulatorPort + " was invalid/not found."
        exit(-1)
         
    while True:
        # Generate a fake serial input...
        outputData = GenerateSerialInput(random.randrange(1, 10)) 
        serialOutputPort.write(outputData)
        #time.sleep(1)
        time.sleep(0.02) # Simulate 50Hz
    
    serialOutputPort.close()

