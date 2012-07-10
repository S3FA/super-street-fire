
# Python imports
import re
import os
import math

# Tools imports
import interpolator

class GestureFileInfo:
    
    NUM_DEVIATION_COMPARISON_PTS = 100
    
    def __init__(self, name):
        self.name = name
        self.num_data_pts  = 0
        self.max_accel_mag = 0
        self.max_gyro_mag  = 0
        
        self.total_std_deviation = 0
        self.std_deviations = []
        
        self.normalized_accel_left_pts    = []
        self.normalized_accel_right_pts   = []
        self.normalized_time_pts          = []  # A list of doubles in [0,1]
        self.interpolated_accel_left_pts  = []
        self.interpolated_accel_right_pts = []

    def add(self, other):
        self.num_data_pts  += other.num_data_pts
        self.max_accel_mag += other.max_accel_mag
        self.max_gyro_mag  += other.max_gyro_mag
        
        if len(self.interpolated_accel_left_pts) != len(other.interpolated_accel_left_pts):
            self.interpolated_accel_left_pts = [(0,0,0)] * len(other.interpolated_accel_left_pts)
            
        if len(self.interpolated_accel_right_pts) != len(other.interpolated_accel_right_pts):
            self.interpolated_accel_right_pts = [(0,0,0)] * len(other.interpolated_accel_right_pts)
            
        assert(len(self.interpolated_accel_left_pts) == len(other.interpolated_accel_left_pts))
        for i in xrange(0, len(self.interpolated_accel_left_pts)):
            self.interpolated_accel_left_pts[i] = \
            (self.interpolated_accel_left_pts[i][0] + other.interpolated_accel_left_pts[i][0], \
             self.interpolated_accel_left_pts[i][1] + other.interpolated_accel_left_pts[i][1],  \
             self.interpolated_accel_left_pts[i][2] + other.interpolated_accel_left_pts[i][2])
        
        assert(len(self.interpolated_accel_right_pts) == len(other.interpolated_accel_right_pts))
        for i in xrange(0, len(self.interpolated_accel_right_pts)):
            self.interpolated_accel_right_pts[i] = \
            (self.interpolated_accel_right_pts[i][0] + other.interpolated_accel_right_pts[i][0], \
             self.interpolated_accel_right_pts[i][1] + other.interpolated_accel_right_pts[i][1],  \
             self.interpolated_accel_right_pts[i][2] + other.interpolated_accel_right_pts[i][2])
    
    
    def div(self, d):
        self.num_data_pts  /= d
        self.max_accel_mag /= d
        self.max_gyro_mag  /= d
        
        for i in xrange(0, len(self.interpolated_accel_left_pts)):
            self.interpolated_accel_left_pts[i] = \
            (self.interpolated_accel_left_pts[i][0] / d, \
             self.interpolated_accel_left_pts[i][1] / d,  \
             self.interpolated_accel_left_pts[i][2] / d)
            
        for i in xrange(0, len(self.interpolated_accel_right_pts)):
            self.interpolated_accel_right_pts[i] = \
            (self.interpolated_accel_right_pts[i][0] / d, \
             self.interpolated_accel_right_pts[i][1] / d,  \
             self.interpolated_accel_right_pts[i][2] / d)
    
    def get_normalized_left_accel_at_normalized_time_pt(self, normalizedTimePt):
        return self._get_normalized_accel_at_normalized_time_pt(normalizedTimePt, self.normalized_time_pts, self.normalized_accel_left_pts)
      
    def get_normalized_right_accel_at_normalized_time_pt(self, normalizedTimePt):
        return self._get_normalized_accel_at_normalized_time_pt(normalizedTimePt, self.normalized_time_pts, self.normalized_accel_right_pts)      
      
    def _get_normalized_accel_at_normalized_time_pt(self, normalizedTimePt, normalized_time_pts, normalized_accel_pts):
        for i in xrange(0, len(normalized_time_pts), 2):
            pt0, pt1 = normalized_time_pts[i:i+2]
            
            if pt0 == normalizedTimePt:
                return normalized_accel_pts[i]
            
            
            if pt0 < normalizedTimePt:
                if pt1 == normalizedTimePt:
                    return normalized_accel_pts[i+1]
            
                if pt1 > normalizedTimePt:
                    # Normal case where we need to interpolate between two points...
                    return interpolator.lerp3Tuple(normalizedTimePt, pt0, pt1, \
                                                   normalized_accel_pts[i], normalized_accel_pts[i+1])
                    
                elif pt1 < normalizedTimePt:
                    for j in xrange(i+2, len(normalized_time_pts)):
                        ptJ = normalized_time_pts[j]
                        if ptJ == normalizedTimePt:
                            return normalized_accel_pts[j]
                        if ptJ > normalizedTimePt:
                            return interpolator.lerp3Tuple(normalizedTimePt, pt0, ptJ, \
                                                           normalized_accel_pts[i], normalized_accel_pts[j])
            
            
        assert(False)
        
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
    GLOVE_DATA_RE_STR = r"^\s*(-?\d+\.\d+),\s+(-?\d+\.\d+),\s+(-?\d+\.\d+),\s+(-?\d+\.\d+),\s+(-?\d+\.\d+),\s+(-?\d+\.\d+),\s+(-?\d+\.\d+),\s+(-?\d+\.\d+),\s+(-?\d+\.\d+)\s*$"
    dataMatchIter = re.finditer(GLOVE_DATA_RE_STR, fileStr, re.MULTILINE)
    for match in dataMatchIter:
        # Matches 1, 2, and 3 are the gyro data
        result.max_gyro_mag = max(abs(float(match.group(1))), abs(float(match.group(2))), abs(float(match.group(3))), result.max_gyro_mag)
        # Matches 4, 5, and 6 are the accelerometer data
        result.max_accel_mag = max(abs(float(match.group(4))), abs(float(match.group(5))), abs(float(match.group(6))), result.max_accel_mag)

    LEFT_GLOVE_DATA_KEYWORD = "LEFT_GLOVE_DATA"
    idxOfLeftGloveData = fileStr.find(LEFT_GLOVE_DATA_KEYWORD)
    idxOfLeftGloveData += len(LEFT_GLOVE_DATA_KEYWORD)
    
    RIGHT_GLOVE_DATA_KEYWORD = "RIGHT_GLOVE_DATA"
    idxOfRightGloveData = fileStr.find(RIGHT_GLOVE_DATA_KEYWORD)
    idxOfRightGloveData += len(RIGHT_GLOVE_DATA_KEYWORD)
    
    leftDataMatchIter = None
    rightDataMatchIter = None
    
    if idxOfLeftGloveData < idxOfRightGloveData:
        leftDataMatchIter  = re.finditer(GLOVE_DATA_RE_STR, fileStr[idxOfLeftGloveData:idxOfRightGloveData], re.MULTILINE)
        rightDataMatchIter = re.finditer(GLOVE_DATA_RE_STR, fileStr[idxOfRightGloveData:], re.MULTILINE)
    else:
        leftDataMatchIter = re.finditer(GLOVE_DATA_RE_STR, fileStr[idxOfLeftGloveData:], re.MULTILINE)
        rightDataMatchIter = re.finditer(GLOVE_DATA_RE_STR, fileStr[idxOfRightGloveData:idxOfLeftGloveData], re.MULTILINE)
    
    assert(leftDataMatchIter != None)
    assert(rightDataMatchIter != None)
    
    # Parse only the left handed accel values...
    for match in leftDataMatchIter:
        accel = (float(match.group(4)), float(match.group(5)), float(match.group(6)))
        norm  = math.sqrt(accel[0] * accel[0] + accel[1] * accel[1] + accel[2] * accel[2])
        result.normalized_accel_left_pts.append((accel[0] / norm, accel[1] / norm, accel[2] / norm))

    # Parse only the right handed accel values...
    for match in rightDataMatchIter:
        accel = (float(match.group(4)), float(match.group(5)), float(match.group(6)))
        norm  = math.sqrt(accel[0] * accel[0] + accel[1] * accel[1] + accel[2] * accel[2])
        result.normalized_accel_right_pts.append((accel[0] / norm, accel[1] / norm, accel[2] / norm))
       
    
    TIME_PTS_KEYWORD = "TIME_PTS"
    idxOfTimePts = fileStr.find(TIME_PTS_KEYWORD)
    idxOfTimePts += len(TIME_PTS_KEYWORD)
    
    # Get all of the time points in the gesture file
    timePtMatchIter = re.finditer(r"^\s*(-?\d+\.\d+)\s*$", fileStr[idxOfTimePts:], re.MULTILINE)
    for match in timePtMatchIter:
        # Match 1 will be a time point
        currTimePt = float(match.group(1))
        result.normalized_time_pts.append(currTimePt)
    

    assert(len(result.normalized_accel_left_pts) == 0 or len(result.normalized_accel_left_pts) == len(result.normalized_time_pts))
    assert(len(result.normalized_accel_right_pts) == 0 or len(result.normalized_accel_right_pts) == len(result.normalized_time_pts))
    
    # Normalize all of the time points in to [0, 1]
    startingTimePt = result.normalized_time_pts[0]
    endingTimePt   = result.normalized_time_pts[len(result.normalized_time_pts)-1]
    timeLength     = endingTimePt - startingTimePt
    result.normalized_time_pts = [(item - startingTimePt) / timeLength for item in result.normalized_time_pts]
    
    # Get full normalized results, interpolated over the gesture
    for i in range(GestureFileInfo.NUM_DEVIATION_COMPARISON_PTS+1):
        
        percent = float(i) / float(GestureFileInfo.NUM_DEVIATION_COMPARISON_PTS) 
        result.interpolated_accel_left_pts.append(result.get_normalized_left_accel_at_normalized_time_pt(percent))
        result.interpolated_accel_right_pts.append(result.get_normalized_right_accel_at_normalized_time_pt(percent))
        
    return result


def build_gesture_file_info_list(dirBasePath, dirNameToAnalyze):
    
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
    
    # Go through each file and parse it into a GestureFileInfo object, keep a list of these for further summary
    gestureInfoList = []
    for file in fileList:
        
        if not os.path.isfile(file):
            continue
        
        fileHandle = open(file, 'r')
        fileStr = fileHandle.read()
        fileHandle.close()
        
        gestureInfo = from_gesture_file_string(file[len(dirBasePath):], fileStr)
        if gestureInfo is None:
            print "Warning: Could not parse file " + file
        else:
            gestureInfoList.append(gestureInfo)

    return gestureInfoList
