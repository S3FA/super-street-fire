
# Python Imports
import sys
import os
import math

# Tools Imports
import common
import gesturefileinfo


def print_usage_and_exit():
    print "Usage:"
    print os.path.basename(sys.argv[0]) + " [-n (#)] [-a (#)] [-s (#)] (base_dir) (dir_name_for_analysis)"
    print "-n : Number of files to display with the least and most number of recorded points"
    print "-a : Number of files to display with the lowest and highest accelerometer values"
    print "-s : Number of files to display with the lowest and highest standard deviation in comparsion to all other gestures of that type"
    exit(1) 


if __name__ == "__main__":
    
    if len(sys.argv) < 3 or len(sys.argv) > 9:
        print_usage_and_exit()
    
    baseDirIdx = 1
    numPtsStatsNum = 0
    numAccelStatsNum = 0
    numStdDeviationsStatsNum = 0
    for i in range(0, len(sys.argv) - 3, 2):

        optIdx = i + 1
        numIdx = optIdx + 1
        
        currOptStr = sys.argv[optIdx]
        currNumStr = sys.argv[numIdx]
        
        if currOptStr == "-n":
            numPtsStatsNum = int(currNumStr)
        elif currOptStr == "-a":
            numAccelStatsNum = int(currNumStr)
        elif currOptStr == "-s":
            numStdDeviationsStatsNum = int(currNumStr)
        else:
            print_usage_and_exit()
            
        baseDirIdx += 2
    
    # Make sure the directory to analyze is valid
    if len(sys.argv) <= baseDirIdx+1:
        print_usage_and_exit()

    dirBasePath      = sys.argv[baseDirIdx]
    dirNameToAnalyze = sys.argv[baseDirIdx+1]
    if not os.path.isdir(dirBasePath):
        print "Invalid directory: " + dirBasePath
        exit(1)
    
    gestureInfoList = gesturefileinfo.build_gesture_file_info_list(dirBasePath, dirNameToAnalyze)
        
    # The final analysis involves going through all of the acquired gesture information
    # and finding the outliers
    if len(sys.argv) == 4:
        numOutliersToShow = int(sys.argv[3])
    
    sortedDataPts   = sorted(gestureInfoList, key=lambda info: info.num_data_pts)
    sortedAccelMags = sorted(gestureInfoList, key=lambda info: info.max_accel_mag)
    
    # Print the results...
    FILENAME_LJUST = 75
    DATA_LJUST = 10
    SEPARATOR_LINE = ""
    for i in range(FILENAME_LJUST + DATA_LJUST): SEPARATOR_LINE += "-"
    SEPARATOR_LINE += "---------------"
    print ""
    print "========================================================================================"
    print "Results for gesture files found under " + dirBasePath
    print "========================================================================================"
    
    if numPtsStatsNum > 0:
        
        numPtsStatsNum = min(numPtsStatsNum, len(sortedDataPts))
        
        print ""
        print "Number of data points ------------------------------------------------------------------"
        print "HIGHEST:"
        print "Filename".ljust(FILENAME_LJUST) + "Number of data points".ljust(DATA_LJUST)
        print SEPARATOR_LINE
        
        for i in range(len(sortedDataPts)-1, len(sortedDataPts) - numPtsStatsNum - 1, -1):
            print sortedDataPts[i].name.ljust(FILENAME_LJUST) + str(sortedDataPts[i].num_data_pts).ljust(DATA_LJUST)
        
        print ""
        print "LOWEST:"
        print "Filename".ljust(FILENAME_LJUST) + "Number of data points".ljust(DATA_LJUST)
        print SEPARATOR_LINE
        for i in range(numPtsStatsNum):
            print sortedDataPts[i].name.ljust(FILENAME_LJUST) + str(sortedDataPts[i].num_data_pts).ljust(DATA_LJUST)
        
    if numAccelStatsNum > 0:
        
        numAccelStatsNum = min(numAccelStatsNum, len(sortedAccelMags))
        
        print ""
        print "Acceleration Magnitude -----------------------------------------------------------------"
        print "HIGHEST:"
        print "Filename".ljust(FILENAME_LJUST) + "Acceleration Magnitude".ljust(DATA_LJUST)
        print SEPARATOR_LINE
        for i in range(len(sortedAccelMags)-1, len(sortedAccelMags) - numAccelStatsNum - 1, -1):
            print sortedAccelMags[i].name.ljust(FILENAME_LJUST) + str(sortedAccelMags[i].max_accel_mag).ljust(DATA_LJUST)
        
        print ""
        print "LOWEST:"
        print "Filename".ljust(FILENAME_LJUST) + "Acceleration Magnitude".ljust(DATA_LJUST)
        print SEPARATOR_LINE
        for i in range(numAccelStatsNum):
            print sortedAccelMags[i].name.ljust(FILENAME_LJUST) + str(sortedAccelMags[i].max_accel_mag).ljust(DATA_LJUST)
    
    if numStdDeviationsStatsNum > 0:
        
        numStdDeviationsStatsNum = min(numStdDeviationsStatsNum, len(sortedDataPts))
        
        # Find the overall averages of every single data set for the gesture to get an 'average/mean gesture'
        avgGesture = gesturefileinfo.GestureFileInfo("Mean Gesture")
        for gestureInfo in gestureInfoList:
            avgGesture.add(gestureInfo)
        
        avgGesture.div(len(gestureInfoList))
        
        # Calculate the standard deviations...
        for gestureInfo in gestureInfoList:
            
            for i in xrange(0, len(avgGesture.interpolated_accel_left_pts)):
                stdDeviation = math.pow(common.pt_3d_dist(gestureInfo.interpolated_accel_left_pts[i], avgGesture.interpolated_accel_left_pts[i]), 2)
                gestureInfo.std_deviations.append(stdDeviation)
                
            for i in xrange(0, len(avgGesture.interpolated_accel_right_pts)):
                stdDeviation = math.pow(common.pt_3d_dist(gestureInfo.interpolated_accel_right_pts[i], avgGesture.interpolated_accel_right_pts[i]), 2)
                gestureInfo.std_deviations.append(stdDeviation)
        
            total = sum(gestureInfo.std_deviations)
            gestureInfo.total_std_deviation = math.sqrt(total / len(gestureInfo.std_deviations))

        sortedStdDeviations = sorted(gestureInfoList, key=lambda info: info.total_std_deviation)
        
        print ""
        print "Standard Deviation ---------------------------------------------------------------------"
        print "WROST:"
        print "Filename".ljust(FILENAME_LJUST) + "Standard Deviation".ljust(DATA_LJUST)
        print SEPARATOR_LINE
        for i in range(len(sortedStdDeviations)-1, len(sortedStdDeviations) - numStdDeviationsStatsNum - 1, -1):
            print sortedStdDeviations[i].name.ljust(FILENAME_LJUST) + str(sortedStdDeviations[i].total_std_deviation).ljust(DATA_LJUST)
        
        print ""
        print "BEST:"
        print "Filename".ljust(FILENAME_LJUST) + "Standard Deviation".ljust(DATA_LJUST)
        print SEPARATOR_LINE
        for i in range(numStdDeviationsStatsNum):
            print sortedStdDeviations[i].name.ljust(FILENAME_LJUST) + str(sortedStdDeviations[i].total_std_deviation).ljust(DATA_LJUST)
       

    print "========================================================================================"
    
