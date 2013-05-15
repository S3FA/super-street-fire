//
//  ArenaView.m
//  SuperStreetFireIOSGUI
//
//  Created by beowulf on 2013-04-23.
//  Copyright (c) 2013 beowulf. All rights reserved.
//

#import "ArenaView.h"
#import "Emitter.h"
#import "Utilities.h"

// TODO: Replace these constants with configuration info
const int NUM_RAIL_EMITTERS       = 8;
const int NUM_OUTER_RING_EMITTERS = 16;

@interface ArenaView() {
}

@property (nonatomic, retain) NSMutableArray* leftRailEmitters;
@property (nonatomic, retain) NSMutableArray* rightRailEmitters;
@property (nonatomic, retain) NSMutableArray* outerRingEmitters;

- (void)setupEmitters;
- (void)clearEmitters;

+ (void)setIntensityFont:(CGContextRef)context;
+ (void)setPlatformFont:(CGContextRef)context;

@end

@implementation ArenaView

@synthesize leftRailEmitters  = _leftRailEmitters;
@synthesize rightRailEmitters = _rightRailEmitters;
@synthesize outerRingEmitters = _outerRingEmitters;

- (id)initWithFrame:(CGRect)frame {
    self = [super initWithFrame:frame];
    
    if (self != nil) {
        [self setupEmitters];
    }
    
    return self;
}

- (void)dealloc {
    [self clearEmitters];
}

- (void)awakeFromNib {
    [self setupEmitters];
}

- (void)setupEmitters {
    [self clearEmitters];
    
    self.leftRailEmitters  = [[NSMutableArray alloc] initWithCapacity:NUM_RAIL_EMITTERS];
    self.rightRailEmitters = [[NSMutableArray alloc] initWithCapacity:NUM_RAIL_EMITTERS];
    for (int i = 0; i < NUM_RAIL_EMITTERS; i++) {
        [self.leftRailEmitters addObject:[[Emitter alloc] init]];
        [self.rightRailEmitters addObject:[[Emitter alloc] init]];
    }
        
    self.outerRingEmitters = [[NSMutableArray alloc] initWithCapacity:NUM_OUTER_RING_EMITTERS];
    for (int i = 0; i < NUM_OUTER_RING_EMITTERS; i++) {
        [self.outerRingEmitters addObject:[[Emitter alloc] init]];
    }
}

- (void)clearEmitters {
    self.leftRailEmitters  = nil;
    self.rightRailEmitters = nil;
    self.outerRingEmitters = nil;
}

/**
 * Draw the arena consisting of:
 * - The ring and two rails of flame effects
 * - The two player and ringmaster platforms
 */
- (void)drawRect:(CGRect)rect {

    
    // Get the drawing objects that we need from the API
    CGContextRef context = UIGraphicsGetCurrentContext();
    CGColorSpaceRef colourSpace = CGColorSpaceCreateDeviceRGB();

    // Setup colours...
    const CGFloat WHITE[] = {1.0, 1.0, 1.0, 1.0};
    
    // Thicknesses of various lines
    const CGFloat RAIL_LINE_THICKNESS       = 2.0f;
    const CGFloat OUTER_RING_LINE_THICKNESS = 1.5f;
    const CGFloat EMITTER_OUTLINE_THICKNESS = 1.0f;
    
    // Set any initial options for drawing...
    // TODO...
    

    CGSize drawRectSize = rect.size;
    
    const float CENTER_X = drawRectSize.width / 2.0f;
    const float CENTER_Y = drawRectSize.height / 2.0f;
    
    const float MIN_DIMENSION             = MIN(drawRectSize.width, drawRectSize.height);
    const float WIDTH_BETWEEN_RAILS       = MIN_DIMENSION * 0.2f;
    const float HALF_WIDTH_BETWEEN_RAILS  = WIDTH_BETWEEN_RAILS / 2.0f;
    const float FULL_RAIL_LENGTH          = WIDTH_BETWEEN_RAILS * 3.0f;
    const float HALF_RAIL_LENGTH          = FULL_RAIL_LENGTH / 2.0f;
    
    const float LENGTH_PER_EMITTER_AND_SPACE_ON_RAIL = FULL_RAIL_LENGTH / (float)(NUM_RAIL_EMITTERS - 1);
    const float EMITTER_DIAMETER                     = 0.66f * LENGTH_PER_EMITTER_AND_SPACE_ON_RAIL;
    const float EMITTER_RADIUS                       = EMITTER_DIAMETER / 2.0f;
    const float DISTANCE_BETWEEN_RAIL_EMITTERS       = LENGTH_PER_EMITTER_AND_SPACE_ON_RAIL - EMITTER_DIAMETER;
    
    const float RAIL_TOP_Y    = CENTER_Y - HALF_RAIL_LENGTH;
    const float RAIL_BOTTOM_Y = CENTER_Y + HALF_RAIL_LENGTH;
    
    // Start by drawing the two central rails between the player podiums
    const float LEFT_RAIL_CENTER_X  = CENTER_X - HALF_WIDTH_BETWEEN_RAILS - EMITTER_RADIUS;
    const float RIGHT_RAIL_CENTER_X = CENTER_X + HALF_WIDTH_BETWEEN_RAILS + EMITTER_RADIUS;
    
    
    // Draw the rails
    CGContextSetStrokeColor(context, WHITE);
    CGContextSetLineWidth(context, RAIL_LINE_THICKNESS);

    // Left rail line...
    CGContextMoveToPoint(context, LEFT_RAIL_CENTER_X, RAIL_TOP_Y);
    CGContextAddLineToPoint(context, LEFT_RAIL_CENTER_X, RAIL_BOTTOM_Y);
    // Right rail line...
    CGContextMoveToPoint(context, RIGHT_RAIL_CENTER_X, RAIL_TOP_Y);
    CGContextAddLineToPoint(context, RIGHT_RAIL_CENTER_X, RAIL_BOTTOM_Y);
    
    CGContextStrokePath(context);
    
    // Draw the emitters on the rails
    CGContextSetLineWidth(context, EMITTER_OUTLINE_THICKNESS);
    //[ArenaView setIntensityFont:context];

    float currPosition = RAIL_BOTTOM_Y;
    for (int i = 0; i < NUM_RAIL_EMITTERS; i++) {
        
        Emitter* currLeftEmitter  = [self.leftRailEmitters  objectAtIndex:i];
        Emitter* currRightEmitter = [self.rightRailEmitters objectAtIndex:i];
        
        CGPoint leftRailEmitterPos;
        leftRailEmitterPos.x = LEFT_RAIL_CENTER_X;
        leftRailEmitterPos.y = currPosition;
        currLeftEmitter.center = leftRailEmitterPos;
        currLeftEmitter.radius = EMITTER_RADIUS;
        
        CGPoint rightRailEmitterPos;
        rightRailEmitterPos.x = RIGHT_RAIL_CENTER_X ;
        rightRailEmitterPos.y = currPosition;
        currRightEmitter.center = rightRailEmitterPos;
        currRightEmitter.radius = EMITTER_RADIUS;
        
        CGRect leftEmitterShape  = CGRectMake(leftRailEmitterPos.x - EMITTER_RADIUS, leftRailEmitterPos.y - EMITTER_RADIUS,
                                              EMITTER_DIAMETER, EMITTER_DIAMETER);
        CGRect rightEmitterShape = CGRectMake(rightRailEmitterPos.x - EMITTER_RADIUS, rightRailEmitterPos.y - EMITTER_RADIUS,
                                              EMITTER_DIAMETER, EMITTER_DIAMETER);
        
        // Draw black circles first (This is to block out the rail)
        CGContextSetFillColorWithColor(context, [UIColor blackColor].CGColor);
        CGContextFillEllipseInRect(context, leftEmitterShape);
        CGContextStrokePath(context);
        CGContextSetFillColorWithColor(context, [UIColor blackColor].CGColor);
        CGContextFillEllipseInRect(context, rightEmitterShape);
        CGContextStrokePath(context);
        
        // Draw the coloured circles
        CGContextSetFillColorWithColor(context, currLeftEmitter.colour.CGColor);
        CGContextFillEllipseInRect(context, leftEmitterShape);
        CGContextAddEllipseInRect(context, leftEmitterShape);
        CGContextStrokePath(context);
        
        CGContextSetFillColorWithColor(context, currRightEmitter.colour.CGColor);
        CGContextFillEllipseInRect(context, rightEmitterShape);
        CGContextAddEllipseInRect(context, rightEmitterShape);
        CGContextStrokePath(context);

        currPosition -= (EMITTER_DIAMETER + DISTANCE_BETWEEN_RAIL_EMITTERS);
    }
    
    // Draw the outer ring of emitters
    const float OUTER_RING_RADIUS   = WIDTH_BETWEEN_RAILS * 2.25f;
    const float OUTER_RING_DIAMETER = 2.0f * OUTER_RING_RADIUS;
    
    const int HALF_NUM_OUTER_RING_EMITTERS = NUM_OUTER_RING_EMITTERS / 2;
    const float INCREMENT_ANGLE = (float)(M_PI / (double)(HALF_NUM_OUTER_RING_EMITTERS+1));
    const float HALF_PI = (float)(M_PI / 2.0);
    
    const float OUTER_RING_X = CENTER_X - OUTER_RING_RADIUS;
    const float OUTER_RING_Y = CENTER_Y - OUTER_RING_RADIUS;
    
    // Draw the rail for the outer ring
    CGContextSetStrokeColor(context, WHITE);
    CGContextSetLineWidth(context, OUTER_RING_LINE_THICKNESS);
    CGContextAddEllipseInRect(context, CGRectMake(OUTER_RING_X, OUTER_RING_Y, OUTER_RING_DIAMETER, OUTER_RING_DIAMETER));
    CGContextStrokePath(context);
    
    // Draw the emitters on the outer ring...
    CGContextSetLineWidth(context, EMITTER_OUTLINE_THICKNESS);
    
    // Start by drawing the right-hand side of the outer ring, staring with the bottom-right emitter and moving
    // up and around the outer ring to the top-right emitter...
    float currAngle = -HALF_PI + INCREMENT_ANGLE;
    
    for (int i = 0; i < HALF_NUM_OUTER_RING_EMITTERS; i++) {
        
        Emitter* currEmitter = [self.outerRingEmitters objectAtIndex:i];
        
        CGPoint outerRingEmitterPos;
        outerRingEmitterPos.x = CENTER_X + OUTER_RING_RADIUS * cosf(currAngle);
        outerRingEmitterPos.y = CENTER_Y - OUTER_RING_RADIUS * sinf(currAngle);
        currEmitter.center    = outerRingEmitterPos;
        currEmitter.radius    = EMITTER_RADIUS;
        
        CGRect emitterShape = CGRectMake(outerRingEmitterPos.x - EMITTER_RADIUS, outerRingEmitterPos.y - EMITTER_RADIUS,
                                         EMITTER_DIAMETER, EMITTER_DIAMETER);

        // Draw black circle first (This is to block out the rail)
        CGContextSetFillColorWithColor(context, [UIColor blackColor].CGColor);
        CGContextFillEllipseInRect(context, emitterShape);
        CGContextStrokePath(context);
        
        // Draw the actual emitter
        CGContextSetFillColorWithColor(context, currEmitter.colour.CGColor);
        CGContextFillEllipseInRect(context, emitterShape);
        CGContextAddEllipseInRect(context, emitterShape);
        CGContextStrokePath(context);
        
        currAngle += INCREMENT_ANGLE;
    }
    
    // Now draw the left-hand side of the outer ring...
    currAngle = HALF_PI + INCREMENT_ANGLE;
    for (int i = HALF_NUM_OUTER_RING_EMITTERS; i < NUM_OUTER_RING_EMITTERS; i++) {
        
        Emitter* currEmitter = [self.outerRingEmitters objectAtIndex:i];
        
        CGPoint outerRingEmitterPos;
        outerRingEmitterPos.x = CENTER_X + OUTER_RING_RADIUS * cosf(currAngle);
        outerRingEmitterPos.y = CENTER_Y - OUTER_RING_RADIUS * sinf(currAngle);
        currEmitter.center    = outerRingEmitterPos;
        currEmitter.radius    = EMITTER_RADIUS;
        
        CGRect emitterShape = CGRectMake(outerRingEmitterPos.x - EMITTER_RADIUS, outerRingEmitterPos.y - EMITTER_RADIUS,
                                         EMITTER_DIAMETER, EMITTER_DIAMETER);

        // Draw black circle first (This is to block out the rail)
        CGContextSetFillColorWithColor(context, [UIColor blackColor].CGColor);
        CGContextFillEllipseInRect(context, emitterShape);
        CGContextStrokePath(context);
        
        // Draw the actual emitter
        CGContextSetFillColorWithColor(context, currEmitter.colour.CGColor);
        CGContextFillEllipseInRect(context, emitterShape);
        CGContextAddEllipseInRect(context, emitterShape);
        CGContextStrokePath(context);
        
        currAngle += INCREMENT_ANGLE;
    }
    
    CGColorSpaceRelease(colourSpace);
}

- (void)changeColours {
    for (int i = 0; i < [self.outerRingEmitters count]; i++) {
        [self.outerRingEmitters replaceObjectAtIndex:i withObject:[[Emitter alloc] initWithColour:[UIColor purpleColor]]];
    }
    [self setNeedsDisplay];
}

- (void)touchesBegan:(NSSet *)touches withEvent:(UIEvent *)event {
    
    UITouch* touch = [touches anyObject];
    CGPoint touchLoc = [touch locationInView:self];
    //NSLog(@"TOUCH BEGAN -- Touch Loc (x: %f, y: %f)", touchLoc.x, touchLoc.y);
    
    // Check to see if the touch is within any of the emitters
    for (int i = 0; i < [self.outerRingEmitters count]; i++) {
        
        Emitter* currEmitter = [self.outerRingEmitters objectAtIndex:i];
        
        float sqrDistFromTouchToEmitter = [Utilities sqrDistanceFrom:touchLoc toPt:currEmitter.center];
        if (sqrDistFromTouchToEmitter <= (currEmitter.radius * currEmitter.radius)) {
            NSLog(@"IN OUTER EMITTER!");
        }
    }
    
    
}
- (void)touchesMoved:(NSSet *)touches withEvent:(UIEvent *)event {
    NSLog(@"TOUCH MOVED");
}
- (void)touchesEnded:(NSSet*)touches withEvent:(UIEvent*)event {
    NSLog(@"TOUCH ENDED");
}

+ (void)setIntensityFont:(CGContextRef)context {
    CGContextSelectFont(context, "Helvetica", 12, kCGEncodingMacRoman);
}
+ (void)setPlatformFont:(CGContextRef)context {
    CGContextSelectFont(context, "Helvetica", 16, kCGEncodingMacRoman);
}

@end
