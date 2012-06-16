
# Python Imports
import sys
import os
import re


class GestureFileInfo:
    
    def __init__(self, filename):
        self.filename = filename
        self.num_data_pts  = 0
        self.max_accel_mag = 0
        self.max_gyro_mag  = 0
        self.time_length   = 0


'''
Function for parsing apart a gesture instance file for Super Street Fire.
Returns: A newly created GestureFileInfo object on success, None on failure.
'''
def from_gesture_file_string(fileName, fileStr):
    result = GestureFileInfo(fileName)
    
    # First thing we do is get the total number of data points in the file...
    numDataMatch = re.match(r"^\s*(L|R|LR)\s+(\d+)", fileStr)
    if numDataMatch is None or len(numDataMatch.groups()) < 2:
        return None
    result.num_data_pts = int(numDataMatch.group(2))
    
    # Parse all of the accelerometer data and find the maximum absolute value
    dataMatchIter = re.finditer(r"^\s*(-?\d+\.\d+),\s+(-?\d+\.\d+),\s+(-?\d+\.\d+),\s+(-?\d+\.\d+),\s+(-?\d+\.\d+),\s+(-?\d+\.\d+),\s+(-?\d+\.\d+),\s+(-?\d+\.\d+),\s+(-?\d+\.\d+)\s*$", fileStr, re.MULTILINE)
    for match in dataMatchIter:
        # Matches 1, 2, and 3 are the gyro data
        result.max_gyro_mag = max(abs(float(match.group(1))), abs(float(match.group(2))), abs(float(match.group(3))), result.max_gyro_mag)
        # Matches 4, 5, and 6 are the accelerometer data
        result.max_accel_mag = max(abs(float(match.group(4))), abs(float(match.group(5))), abs(float(match.group(6))), result.max_accel_mag)

    
    TIME_PTS_KEYWORD = "TIME_PTS"
    idxOfTimePts = fileStr.find(TIME_PTS_KEYWORD)
    idxOfTimePts += len(TIME_PTS_KEYWORD)
    
    # Get all of the time points in the gesture file
    timePtMatchIter = re.finditer(r"^\s*(-?\d+\.\d+)\s*$", fileStr[idxOfTimePts:], re.MULTILINE)
    minTimePt = 99999999.999
    maxTimePt = -999999999.999
    for match in timePtMatchIter:
        # Match 1 will be a time point
        currTimePt = float(match.group(1))
        minTimePt  = min(minTimePt, currTimePt)
        maxTimePt  = max(maxTimePt, currTimePt)
    
    result.time_length = maxTimePt - minTimePt
    
    return result

def print_usage_and_exit():
    print "Usage:"
    print os.path.basename(sys.argv[0]) + " (base_dir) (dir_name_for_analysis) [num_outliers_to_show]"
    exit(1) 


if __name__ == "__main__":
    
    if len(sys.argv) < 3 or len(sys.argv) > 4:
        print_usage_and_exit()
    
    # Make sure the directory to analyze is valid
    dirBasePath      = sys.argv[1]
    dirNameToAnalyze = sys.argv[2]
    if not os.path.isdir(dirBasePath):
        print "Invalid directory: " + dirBasePath
        exit(1)
    
    # Recursively walk the base directory to find subdirectories with the given dirNameToAnalyze
    dirsToAnalyze = []
    for dirpath, dirnames, filenames in os.walk(dirBasePath):
        for dirname in dirnames:
            if dirname == dirNameToAnalyze:
                dirsToAnalyze.append(os.path.join(dirpath, dirname))
    
    # Get a list of all files in the directory to analyze
    fileList = []
    for dir in dirsToAnalyze:
        currFileList = os.listdir(dir)
        currFileList = map((lambda file: os.path.join(dir, file)), currFileList)
        fileList.extend(currFileList)
    
    print fileList
    
    # Go through each file and parse it into a GestureFileInfo object, keep a list of these for further summary
    gestureInfoList = []
    for file in fileList:
        
        if not os.path.isfile(file):
            continue
        
        fileHandle = open(file, 'r')
        fileStr = fileHandle.read()
        fileHandle.close()
        
        gestureInfo = from_gesture_file_string(file, fileStr)
        if gestureInfo is None:
            print "Warning: Could not parse file " + file
        else:
            gestureInfoList.append(gestureInfo)
            
    # The final analysis involves going through all of the acquired gesture information
    # and finding the outliers
    numOutliersToShow = 5
    if len(sys.argv) == 4:
        numOutliersToShow = int(sys.argv[3])
    
    sortedDataPts      = sorted(gestureInfoList, key=lambda info: info.num_data_pts)
    sortedAccelMags    = sorted(gestureInfoList, key=lambda info: info.max_accel_mag)
    soretedTimeLengths = sorted(gestureInfoList, key=lambda info: info.time_length)
    
    numOutliersToShow = min(numOutliersToShow, len(soretedTimeLengths))
    
    # Print the results...
    FILENAME_LJUST = 80
    DATA_LJUST = 10
    print ""
    print "========================================================================================"
    print "Results for gesture files found under " + dirBasePath
    print "========================================================================================"
    
    print "Number of data points ------------------------------------------------------------------"
    print "HIGHEST:"
    print "Filename".ljust(FILENAME_LJUST) + "Number of data points".ljust(DATA_LJUST)
    print "--------------------------------------------------------------"
    for i in range(len(sortedDataPts)-1, len(sortedDataPts) - numOutliersToShow - 1, -1):
        print sortedDataPts[i].filename.ljust(FILENAME_LJUST) + str(sortedDataPts[i].num_data_pts).ljust(DATA_LJUST)
    
    print ""
    print "LOWEST:"
    print "Filename".ljust(FILENAME_LJUST) + "Number of data points".ljust(DATA_LJUST)
    print "--------------------------------------------------------------"
    for i in range(numOutliersToShow):
        print sortedDataPts[i].filename.ljust(FILENAME_LJUST) + str(sortedDataPts[i].num_data_pts).ljust(DATA_LJUST)
    
    print ""
    print "Acceleration Magnitude -----------------------------------------------------------------"
    print "HIGHEST:"
    print "Filename".ljust(FILENAME_LJUST) + "Acceleration Magnitude".ljust(DATA_LJUST)
    print "--------------------------------------------------------------"
    for i in range(len(sortedAccelMags)-1, len(sortedAccelMags) - numOutliersToShow - 1, -1):
        print sortedAccelMags[i].filename.ljust(FILENAME_LJUST) + str(sortedAccelMags[i].max_accel_mag).ljust(DATA_LJUST)
    
    print ""
    print "LOWEST:"
    print "Filename".ljust(FILENAME_LJUST) + "Acceleration Magnitude".ljust(DATA_LJUST)
    print "--------------------------------------------------------------"
    for i in range(numOutliersToShow):
        print sortedAccelMags[i].filename.ljust(FILENAME_LJUST) + str(sortedAccelMags[i].max_accel_mag).ljust(DATA_LJUST)
        
    '''
    print ""
    print "Time Length ----------------------------------------------------------------------------"
    print "HIGHEST:"
    print "Filename".ljust(30) + "Time Length".ljust(10)
    print "---------------------------------------------------"
    for i in range(len(soretedTimeLengths)-1, len(soretedTimeLengths) - numOutliersToShow - 1, -1):
        print soretedTimeLengths[i].filename.ljust(30) + str(soretedTimeLengths[i].time_length).ljust(10)
    
    print ""
    print "LOWEST:"
    print "Filename".ljust(30) + "Time Length".ljust(10)
    print "---------------------------------------------------"
    for i in range(numOutliersToShow):
        print soretedTimeLengths[i].filename.ljust(30) + str(soretedTimeLengths[i].time_length).ljust(10)
    '''

    print "========================================================================================"
    
