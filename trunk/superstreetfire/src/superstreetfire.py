'''
superstreetfire.py

The entry point for the superstreetfire server/application.
'''

import sys    
from optparse import OptionParser

if __name__ == '__main__':

    # Parse options from the command line
    usageStr = "usage: %prog [options]"
    cmdLineDescStr = "The server for the superstreetfire application..."
    argParser = OptionParser(usage=usageStr, description=cmdLineDescStr)
    argParser.add_option("-i", "--input_port", action="store", type="string", dest="inputPort", \
                         help="The serial port name/number that provides input from clients, this value can be omitted for debug.", \
                         default=None)
    argParser.add_option("-o", "--output_port", action="store", type="string", dest="outputPort", \
                         help="The serial port name/number that sends output to clients, this value can be omitted for debug.", \
                         default=None)
    argParser.add_option("-b", "--baud_rate", action="store", type="int", dest="baudRate", \
                         help="The serial port baud rate defaults to 57600.", default=57600)

    (options, args) = argParser.parse_args()


    print options

    # Spawn threads for listening and sending data over the serial port
    # TODO
