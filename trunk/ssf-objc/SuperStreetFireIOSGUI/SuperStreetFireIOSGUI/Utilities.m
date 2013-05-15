//
//  Utilities.m
//  SuperStreetFireIOSGUI
//
//  Created by beowulf on 2013-04-23.
//  Copyright (c) 2013 beowulf. All rights reserved.
//

#import "Utilities.h"

@implementation Utilities

+ (CGPoint)addPoint:(CGPoint)pt1 toPoint:(CGPoint)pt2 {
    CGPoint result;
    result.x = pt1.x + pt2.x;
    result.y = pt1.y + pt2.y;
    return result;
}

+ (float)sqrDistanceFrom:(CGPoint)pt1 toPt:(CGPoint)pt2 {
    return (powf((pt1.x - pt2.x), 2) + powf((pt1.y - pt2.y), 2));
}

+ (float)distanceFrom:(CGPoint)pt1 toPt:(CGPoint)pt2 {
    return sqrtf([Utilities sqrDistanceFrom:pt1 toPt:pt2]);
}

@end
