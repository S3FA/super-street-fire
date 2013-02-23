

import math


def pt_3d_dist(pt1, pt2):
    return math.sqrt(math.pow(pt1[0] - pt2[0], 2) + math.pow(pt1[1] - pt2[1], 2) + math.pow(pt1[2] - pt2[2], 2))