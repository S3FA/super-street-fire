#ifndef __SEGMENT_H__
#define __SEGMENT_H__

#include <Arduino.h>
#include <Adafruit_NeoPixel.h>

#define PIXELS_PER_SEGMENT     6
#define NEO_STRIP_PIXELS       7*PIXELS_PER_SEGMENT

class Segment
{
  public:
    Adafruit_NeoPixel *strip;

    Segment();

    void begin(Adafruit_NeoPixel *strip);
    /* Set all of the pixels of one segment to a given colour */
    void setSegment (int seg, uint32_t colour);
    void clearAllSegments();
    /* Display a digit between 0 and 9 */
    void displayDigit(char digit, uint32_t colour);
    

};

#endif

