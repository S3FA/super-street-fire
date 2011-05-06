'''
calibration_data.py

Holds the data structure containing all the relevant values for the calibration
of both player's gloves. The CalibrationData class also contains routines for
conveniently adding samples and average/normalizing sample collections after sampling
is finished.

@author: Callum Hay
'''

from util.euclid import Vector3

class CalibrationData:

    def __init__(self):
        # Initialize variables that will hold the average directions (heading)
        # for each glove's attack vector (i.e., the vector from that glove's
        # player to the opposing player)
        self.p1LGloveAtkDirAvg = Vector3(0,0,0)
        self.p1RGloveAtkDirAvg = Vector3(0,0,0)
        self.p2LGloveAtkDirAvg = Vector3(0,0,0)
        self.p2RGloveAtkDirAvg = Vector3(0,0,0)

        # 

        # Initialize variables that will hold the 'zero' acceleration vector
        # coming from each of the player gloves - since sensors are never perfect
        # this can give us any default offset the sensors have when not moving much
        self.p1LGloveNoAccelAvg = Vector3(0,0,0)
        self.p1RGloveNoAccelAvg = Vector3(0,0,0)
        self.p2LGloveNoAccelAvg = Vector3(0,0,0)
        self.p2RGloveNoAccelAvg = Vector3(0,0,0)

        self.numP1LGloveSamples = 0
        self.numP1RGloveSamples = 0
        self.numP2LGloveSamples = 0
        self.numP2RGloveSamples = 0
        
        self.isFinishedSampling = False
    
    # Call this function to add a sample to the calibration - all samples will
    # be properly averaged and normalized when FinishSampling is called
    def AddGloveDataSample(self, p1LGloveData, p1RGloveData, 
                           p2LGloveData, p2RGloveData):
        if self.isFinishedSampling:
            assert(False)
            return
        
        if p1LGloveData != None:
            self.p1LGloveAtkDirAvg  += apply(Vector3, p1LGloveData.heading)
            self.p1LGloveNoAccelAvg += apply(Vector3, p1LGloveData.acceleration)
            self.numP1LGloveSamples += 1
        
        if p1RGloveData != None: 
            self.p1RGloveAtkDirAvg  += apply(Vector3, p1RGloveData.heading)
            self.p1RGloveNoAccelAvg += apply(Vector3, p1RGloveData.acceleration)
            self.numP1RGloveSamples += 1
        
        if p2LGloveData != None:
            self.p2LGloveAtkDirAvg  += apply(Vector3, p2LGloveData.heading)
            self.p2LGloveNoAccelAvg += apply(Vector3, p2LGloveData.acceleration)
            self.numP2LGloveSamples += 1
        
        if p2RGloveData != None:
            self.p2RGloveAtkDirAvg  += apply(Vector3, p2RGloveData.heading)
            self.p2RGloveNoAccelAvg += apply(Vector3, p2RGloveData.acceleration)
            self.numP2RGloveSamples += 1
    
    # Call this to finish sampling the calibration data and finalize it
    def FinishSampling(self):
        if self.isFinishedSampling:
            assert(False)
            return
        
        # Average and Normalize all of the collected calibration data
        self.p1LGloveAtkDirAvg /= self.numP1LGloveSamples
        self.p1LGloveAtkDirAvg.normalize()
        self.p1RGloveAtkDirAvg /= self.numP1RGloveSamples
        self.p1RGloveAtkDirAvg.normalize()
        self.p2LGloveAtkDirAvg /= self.numP2LGloveSamples
        self.p2LGloveAtkDirAvg.normalize()
        self.p2RGloveAtkDirAvg /= self.numP2RGloveSamples
        self.p2RGloveAtkDirAvg.normalize()
        
        self.p1LGloveNoAccelAvg /= self.numP1LGloveSamples
        self.p1LGloveNoAccelAvg.normalize()
        self.p1RGloveNoAccelAvg /= self.numP1RGloveSamples
        self.p1RGloveNoAccelAvg.normalize()
        self.p2LGloveNoAccelAvg /= self.numP2LGloveSamples
        self.p2LGloveNoAccelAvg.normalize()
        self.p2RGloveNoAccelAvg /= self.numP2RGloveSamples
        self.p2RGloveNoAccelAvg.normalize()
        
        self.isFinishedSampling = True

    def __str__(self):
        result =  str(self.p1LGloveAtkDirAvg) + " " + str(self.p1RGloveAtkDirAvg) + " " + \
               str(self.p2LGloveAtkDirAvg) + " " + str(self.p2RGloveAtkDirAvg) + "\n"
        result += str(self.p1LGloveNoAccelAvg) + " " + str(self.p1RGloveNoAccelAvg) + " " + \
               str(self.p2LGloveNoAccelAvg) + " " + str(self.p2RGloveNoAccelAvg)
        return result
        
        
if __name__ == "__main__":
    from ioserver.client_datatypes import *
    
    p1R_0 = GloveData((1,1,1), (1,1,1), (0,0,0), PLAYER_ONE, GloveData.RIGHT_HAND_GLOVE)
    p1L_0 = GloveData((2,3,5), (2,2,2), (0,0,0), PLAYER_ONE, GloveData.RIGHT_HAND_GLOVE)
    p2R_0 = GloveData((1,1,1), (1,1,1), (0,0,0), PLAYER_ONE, GloveData.RIGHT_HAND_GLOVE)
    p2L_0 = GloveData((2,3,5), (2,2,2), (0,0,0), PLAYER_ONE, GloveData.RIGHT_HAND_GLOVE)
    
    calibrationData = CalibrationData()
    calibrationData.AddGloveDataSample(p1L_0, p1R_0, p2L_0, p2R_0)
    calibrationData.AddGloveDataSample(p1L_0, p1R_0, p2L_0, p2R_0)
    print calibrationData
    
    calibrationData.FinishSampling()
    print calibrationData
    
    
    
