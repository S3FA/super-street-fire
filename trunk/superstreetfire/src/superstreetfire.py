'''
superstreetfire.py

The entry point for the superstreetfire server/application.
'''

#import re
#import test
#from ioserver.client_datatypes import *
    
import sys    
#from settings import Settings
from optparse import OptionParser

if __name__ == '__main__':
    #appSettings = Settings(sys.argv)
    #print appSettings

    # Parse options from the command line
    #usage = "usage: %prog [options]"
    argParser = OptionParser()
    argParser.add_option("-i", "--input_port", action="store", type="string", dest="inputPort", \
                         help="The serial port name/number that provides input from clients, this value can be omitted for debug.", \
                         default="")
    argParser.add_option("-o", "--output_port", action="store", type="string", dest="outputPort", \
                         help="The serial port name/number that sends output to clients, this value can be omitted for debug.", \
                         default="")

    (options, args) = argParser.parse_args()
    print options
    