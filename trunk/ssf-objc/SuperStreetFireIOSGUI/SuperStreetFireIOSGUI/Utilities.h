//
//  Utilities.h
//  SuperStreetFireIOSGUI
//
//  Created by beowulf on 2013-04-23.
//  Copyright (c) 2013 beowulf. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface Utilities : NSObject

+ (CGPoint)addPoint:(CGPoint)pt1 toPoint:(CGPoint)pt2;

+ (float)sqrDistanceFrom:(CGPoint)pt1 toPt:(CGPoint)pt2;
+ (float)distanceFrom:(CGPoint)pt1 toPt:(CGPoint)pt2;

@end
