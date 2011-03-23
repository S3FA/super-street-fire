'''
superstreetfire.py

The entry point for the superstreetfire server/application.
'''

#import re
#import test
#from ioserver.client_datatypes import *
    
import sys    
from settings import Settings
    


serialPort = None
  


if __name__ == '__main__':
    appSettings = Settings(sys.argv)
    print appSettings
