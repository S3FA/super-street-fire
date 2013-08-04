#include "Segment.h"

#define NUM_SEGMENTS 7

/* The digits that can be displayed by the seven segment */
const char *DIGITS = "0123456789";

/* This array stores the bit patterns to display the above digits. The nth
 * bit corresponds to whether you want the nth segment on or not. The segments
 * are numbered as follows:
 *
 *      1
 *    +---+
 *  6 |   | 2
 *    | 7 |
 *    +---+
 *  5 |   | 3
 *    |   |
 *    +---+
 *      4
 */
const byte SEGMENTS[] = {
  0B00111111, // 0
  0B00000110, // 1
  0B01011011, // 2
  0B01001111, // 3
  0B01100110, // 4
  0B01101101, // 5
  0B01111101, // 6
  0B00000111, // 7
  0B01111111, // 8
  0B01101111, // 9
};

Segment::Segment()
{
}

void Segment::begin(Adafruit_NeoPixel *str)
{
  strip = str;
  strip->begin();
  strip->show();
}

void Segment::setSegment(int seg, uint32_t colour)
{
  int start = seg*PIXELS_PER_SEGMENT;
  for(int n = 0; n < PIXELS_PER_SEGMENT; n++) {
    strip->setPixelColor(start + n, colour);
  }
}

void Segment::clearAllSegments() {
  for (int i = 0; i < NUM_SEGMENTS; i++) {
    this->setSegment(i, 0x000000); 
  }
  strip->show();
}

void Segment::displayDigit(char digit, uint32_t colour)
{
  /* Lookup the character to display */
  char *ch = strchr(DIGITS, digit);
  if (ch == NULL) {
    return;
  }
  /* Find the bit pattern to set which segments are on */
  int pos = (int)(ch - DIGITS);
  byte pattern = SEGMENTS[pos];
  
  for (int n = 0; n < 7; n++) {
    if ((pattern >> n) & 1) {
      setSegment(n, colour);
    } else {
      setSegment(n, 0x000000);
    }
  }
  strip->show();
}

