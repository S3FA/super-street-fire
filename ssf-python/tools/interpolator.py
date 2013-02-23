

def finite_difference_slope(t0, t1, t2, p0, p1, p2):
    return ((p2 - p1) / 2*(t2 - t1)) + ((p1 - p0) / 2*(t1 - t0))

def cspline(startT, currT, endT, pPrev, pCurr, pNext):
    
    tDiff = (endT - startT)
    t = (currT - startT) / tDiff
    
    tCubed = t * t * t
    tSquared = t * t
    
    h00 = 2 * tCubed - 3 * tSquared + 1
    h10 = tCubed - 2 * tSquared + t
    h01 = -2 * tCubed + 3 * tSquared
    h11 = tCubed - tSquared
    
    return h00 * pCurr + h10 * tDiff * finite_difference_slope() + \
           h01 * pNext + h11 * tDiff * finite_difference_slope()
           
 
 
def lerp(x, x0, x1, y0, y1):
    return y0 + (x - x0) * (y1 - y0) / (x1 - x0)     
     
def lerp3Tuple(x, x0, x1, y0, y1):
    return (lerp(x, x0, x1, y0[0], y1[0]), lerp(x, x0, x1, y0[1], y1[1]), lerp(x, x0, x1, y0[2], y1[2]))