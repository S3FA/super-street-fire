
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
    if len(numDataMatch.groups()) < 2:
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
    minTimePt = sys.float_info.max
    maxTimePt = -sys.float_info.max
    for match in timePtMatchIter:
        # Match 1 will be a time point
        currTimePt = float(match.group(1))
        minTimePt  = min(minTimePt, currTimePt)
        maxTimePt  = max(maxTimePt, currTimePt)
    
    result.time_length = maxTimePt - minTimePt
    
    return result

def print_usage_and_exit():
    print "Usage:"
    print os.path.basename(sys.argv[0]) + " (dir_for_analysis) [num_outliers_to_show]"
    exit(1) 


if __name__ == "__main__":
    
    if len(sys.argv) < 2 or len(sys.argv) > 3:
        print_usage_and_exit()
    
    # Make sure the directory to analyze is valid
    dirToAnalyze = sys.argv[1]
    if not os.path.isdir(dirToAnalyze):
        print "Invalid directory: " + dirToAnalyze
        exit(1)
    
    # Get a list of all files in the directory to analyze
    fileList = os.listdir(dirToAnalyze)
    #fileList = map((lambda file: os.path.join(dirToAnalyze, file)), fileList)
     
    # Go through each file and parse it into a GestureFileInfo object, keep a list of these for further summary
    gestureInfoList = []
    for file in fileList:
        with open(os.path.join(dirToAnalyze, file), 'r') as fileHandle:
            fileStr = fileHandle.read()
            
            gestureInfo = from_gesture_file_string(file, fileStr)
            if gestureInfo is None:
                print "Warning: Could not parse file " + file
            else:
                gestureInfoList.append(gestureInfo)
            
    
    # The final analysis involves going through all of the acquired gesture information
    # and finding the outliers
    numOutliersToShow = 5
    if len(sys.argv) == 3:
        numOutliersToShow = int(sys.argv[2])
    
    sortedDataPts      = sorted(gestureInfoList, key=lambda info: info.num_data_pts)
    sortedAccelMags    = sorted(gestureInfoList, key=lambda info: info.max_accel_mag)
    soretedTimeLengths = sorted(gestureInfoList, key=lambda info: info.time_length)
    
    numOutliersToShow = min(numOutliersToShow, len(soretedTimeLengths))
    
    # Print the results...
    print ""
    print "========================================================================================"
    print "Results for gesture files in " + dirToAnalyze
    print "========================================================================================"
    
    print ""
    print "Number of data points ------------------------------------------------------------------"
    print "----------------------------------------------------------------------------------------"
    print ""
    print "HIGHEST:"
    print "Filename".ljust(30) + "Number of data points".ljust(10)
    print "---------------------------------------------------"
    for i in range(len(sortedDataPts)-1, len(sortedDataPts) - numOutliersToShow - 1, -1):
        print sortedDataPts[i].filename.ljust(30) + str(sortedDataPts[i].num_data_pts).ljust(10)
    
    print ""
    print "LOWEST:"
    print "Filename".ljust(30) + "Number of data points".ljust(10)
    print "---------------------------------------------------"
    for i in range(numOutliersToShow):
        print sortedDataPts[i].filename.ljust(30) + str(sortedDataPts[i].num_data_pts).ljust(10)
    
    print ""
    print "Acceleration Magnitude -----------------------------------------------------------------"
    print "----------------------------------------------------------------------------------------"
    print ""
    print "HIGHEST:"
    print "Filename".ljust(30) + "Acceleration Magnitude".ljust(10)
    print "---------------------------------------------------"
    for i in range(len(sortedAccelMags)-1, len(sortedAccelMags) - numOutliersToShow - 1, -1):
        print sortedAccelMags[i].filename.ljust(30) + str(sortedAccelMags[i].max_accel_mag).ljust(10)
    
    print ""
    print "LOWEST:"
    print "Filename".ljust(30) + "Acceleration Magnitude".ljust(10)
    print "---------------------------------------------------"
    for i in range(numOutliersToShow):
        print sortedAccelMags[i].filename.ljust(30) + str(sortedAccelMags[i].max_accel_mag).ljust(10)
        
        
    print ""
    print "Time Length ----------------------------------------------------------------------------"
    print "----------------------------------------------------------------------------------------"
    print ""
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
    print ""
        
    print "========================================================================================"
    
