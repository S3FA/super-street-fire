//
//  Emitter.m
//  SuperStreetFireIOSGUI
//
//  Created by beowulf on 2013-04-23.
//  Copyright (c) 2013 beowulf. All rights reserved.
//

#import "Emitter.h"

static const float DEFAULT_RED   = 1.0f;
static const float DEFAULT_GREEN = 1.0f;
static const float DEFAULT_BLUE  = 1.0f;
static const float DEFAULT_ALPHA = 0.5f;

static const float DEFAULT_INTENSITY = 0.0f;

static const float DEFAULT_CENTER_X = 0.0f;
static const float DEFAULT_CENTER_Y = 0.0f;

static const float DEFAULT_RADIUS = 0.0f;

@interface Emitter() {
}
@end

@implementation Emitter

@synthesize colour    = _colour;
@synthesize intensity = _intensity;
@synthesize center    = _center;
@synthesize radius    = _radius;

- (Emitter*)init {
    self = [super init];
    if (self != nil) {
        _colour    = [[UIColor alloc] initWithRed:DEFAULT_RED green:DEFAULT_GREEN blue:DEFAULT_BLUE alpha:DEFAULT_ALPHA];
        _intensity = DEFAULT_INTENSITY;
        _center.x  = DEFAULT_CENTER_X;
        _center.y  = DEFAULT_CENTER_Y;
        _radius    = DEFAULT_RADIUS;
    }
    
    return self;
}

- (Emitter*)initWithColour:(UIColor*)colour {
    self = [super init];
    if (self != nil) {
        _colour = colour;
        _intensity = DEFAULT_INTENSITY;
        _center.x  = DEFAULT_CENTER_X;
        _center.y  = DEFAULT_CENTER_Y;
        _radius    = DEFAULT_RADIUS;
    }
    
    return self;
}

- (Emitter*)initWithColours:(NSArray*)colours andIntensities:(NSArray*)intensities {
    self = [super init];
    if (self != nil) {
		assert([intensities count] == [colours count]);
		
		float totalRed     = 0.0f;
		float totalGreen   = 0.0f;
		float totalBlue    = 0.0f;
		float maxIntensity = 0.0f;
		
		for (int i = 0; i < [colours count]; i++) {
            assert([[intensities objectAtIndex:i] isKindOfClass:[NSNumber class]]);
            assert([[colours objectAtIndex:i] isKindOfClass:[UIColor class]]);
            
            NSNumber* currIntensity = (NSNumber*)[intensities objectAtIndex:i];
            UIColor*  currColour    = (UIColor*)[colours objectAtIndex:i];
            
            float currIntensityAsFlt = [currIntensity floatValue];
            float currRed, currGreen, currBlue;
            [currColour getRed:&currRed green:&currGreen blue:&currBlue alpha:nil];
            
			totalRed   = MIN(1.0f, totalRed   + currIntensityAsFlt * currRed);
			totalGreen = MIN(1.0f, totalGreen + currIntensityAsFlt * currGreen);
			totalBlue  = MIN(1.0f, totalBlue  + currIntensityAsFlt * currBlue);
            
			maxIntensity = MAX(maxIntensity, currIntensityAsFlt);
		}
		
        if (totalRed == 0.0f && totalGreen == 0.0f && totalBlue == 0.0f) {
            _colour = [[UIColor alloc] initWithRed:DEFAULT_RED green:DEFAULT_GREEN blue:DEFAULT_BLUE alpha:DEFAULT_ALPHA];
        }
        else {
            _colour = [[UIColor alloc] initWithRed:totalRed green:totalGreen blue:totalBlue alpha:maxIntensity];
        }
        
        _intensity = maxIntensity;
        _center.x  = DEFAULT_CENTER_X;
        _center.y  = DEFAULT_CENTER_Y;
        _radius    = DEFAULT_RADIUS;
    }

    return self;
}

- (void)dealloc {
    _colour = nil;
}

@end
