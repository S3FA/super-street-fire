
import Serial

class Receiver:
    # We try to make parsing as fast as possible by using a hash table with 1H,1L,1R,etc.
    # for keys and parse function references for values
    PARSER_FUNCTION_DICT = {
        '1L' : Player1LeftGloveParser,
        '1R' : Player1RightGloveParser,
        '1H' : Player1HeadsetSerialInputParser,
        '2L' : Player2LeftGloveParser,
        '2R' : Player2RightGloveParser,
        '2H' : Player2HeadsetSerialInputParser
    }

    def __init__(self, inputSerialPort, baudRate):
        self.serial = None
        try:
            # NOTE: timeout=x means we wait up to x seconds to read from the serial port
            self.serial = Serial.serial(inputSerialPort, baudrate=baudRate, timeout=1)
        except serial.SerialException:
            print "ERROR: Serial port " + inputSerialPort + " was invalid/not found."
            exit(-1)
    
    def __del__(self):
        if self.serial != None:
            self.serial.close()

    def run(self):
        
        # Make sure this object is in a proper state before running...
        if self.serial == None:
            print "ERROR: Serial port " + inputSerialPort + " was invalid/not found, could not run receiver."
            return
        
        # Temporary variables used in the while loop
        currSerialDataStr = ""
        

        while True:
            # Listen for input on the serial port
            currSerialDataStr = self.serial.readline()
            self.ParseSerialData(currSerialDataStr)

            
        

    def ParseSerialData(self, serialDataStr):
        # Break the serial input up based on the various known headers that
        # designate our input sources...
        splitInputList  = string.split(serialDataStr, ":")
        splitListLength = len(splitInputList)

        # splitListLength should likely be 2 here, but just for robustness
        # we pretend like it could be longer
        for i in range(0, splitListLength-1):
            func = Receiver.PARSER_FUNCTION_DICT.get(splitInputList[i]);
            if func != None:
                # We're dealing with an expected package type from a client, parse it
                # and queue it for the game simulation to deal with
                func(splitInputList[i+1])
