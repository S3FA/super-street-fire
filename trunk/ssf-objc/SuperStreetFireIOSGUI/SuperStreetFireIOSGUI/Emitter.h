//
//  Emitter.h
//  SuperStreetFireIOSGUI
//
//  Created by beowulf on 2013-04-23.
//  Copyright (c) 2013 beowulf. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface Emitter : NSObject {
}
@property (nonatomic, readonly, assign) float intensity;
@property (nonatomic, readonly, retain) UIColor* colour;
@property (nonatomic, assign) CGPoint center;
@property (nonatomic, assign) float radius;

- (Emitter*)initWithColour:(UIColor*)colour;
- (Emitter*)initWithColours:(NSArray*)colours andIntensities:(NSArray*)intensities;

@end
