'''
settings.py

Holds the Settings class which encapsulates all application settings
data for use across the superstreetfire application.

@author: Callum Hay
'''

import re

class Settings:
    # Constants for parsing the command line arguments for the settings
    SERIAL_PORT_IDENTIFIER    = "-p"
    SERIAL_PORT_CONTENT_REGEX = "([\d\w]+)"

    # The globally available settings
    serialPort = None

    def __init__(self, argv):
        # Parse the command line arguments
        self._ParseCommandLineArguments(argv)

    def __str__(self):
        result =  "Super Street Fire Settings:\n"
        
        # Print the serial port
        result += "Serial Port: "
        if Settings.serialPort == None:
            result += "Not defined (test mode)"
        else:
            result += Settings.serialPort
        result += "\n"
        
        return result
        
    def _PrintUsage(self):
        print "Usage: superstreetfire [-p serial_port]"
        print "-p serial_port : The serial port name/number where I/O for the server"
        print "                 takes place, this value can be omitted for debug."
        exit(-1)
    
    def _ParseSerialArgument(self, argStr):
        parseResult = re.match(Settings.SERIAL_PORT_CONTENT_REGEX, argStr)
        if parseResult == None:
            self._PrintUsage()
        else:
            return parseResult.group(1)
        
    def _ParseCommandLineArguments(self, argv):
        # Parse the command line arguments
        numArgs = len(argv); 
        
        for i, currArg in enumerate(argv):
                    
            regExMatch = re.match(Settings.SERIAL_PORT_IDENTIFIER, currArg);
            if regExMatch != None:
                if i == numArgs-1:
                    self._PrintUsage()
                Settings.serialPort = self._ParseSerialArgument(argv[i+1])
                
                
      
                
                
                
                