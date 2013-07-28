/* LEDTimer.cpp */
#include "LEDTimer.h"
#include "Segment.h"

Adafruit_NeoPixel ones(NEO_STRIP_PIXELS, 5);
Adafruit_NeoPixel tens(NEO_STRIP_PIXELS, 3);

LEDTimer::LEDTimer()
{
  timeDisplayed = 0;
  timeColour = 0xFF0000;
  doUpdate = true;
}

void LEDTimer::begin()
{
  onesDigit.begin(&ones);
  tensDigit.begin(&tens);
}

void LEDTimer::showTime(int tm)
{
  timeDisplayed = tm;
  doUpdate = true;
}

void LEDTimer::setColour(uint32_t colour)
{
  timeColour = colour;
  doUpdate = true;
}

void LEDTimer::update()
{
  if (doUpdate) {
    onesDigit.displayDigit('0' + (timeDisplayed % 10), timeColour);
    tensDigit.displayDigit('0' + (timeDisplayed / 10), timeColour);
    doUpdate = false;
  }
}

