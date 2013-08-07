/* LEDTimer.cpp */
#include "LEDTimer.h"
#include "Segment.h"

Adafruit_NeoPixel ones(NEO_STRIP_PIXELS, 5);
Adafruit_NeoPixel tens(NEO_STRIP_PIXELS, 3);

#define BLINK_TIME_MS 200

LEDTimer::LEDTimer() {
  timeDisplayed = 0;
  timeColour = 0xFF0000;
  doUpdate = true;
  //blinkingOn = false;
  //lastBlinkTimeInMillis = 0;
  //blinkState = BlinkOffState;
}

void LEDTimer::begin() {
  onesDigit.begin(&ones);
  tensDigit.begin(&tens);
}

void LEDTimer::showTime(int tm /*, boolean turnBlinkingOn*/) {
  timeDisplayed = tm;
  doUpdate = true;
  
  /*
  if (turnBlinkingOn) {
    
    // We only reset the blinking parameters if blinking is being turned on
    // for the first/initial time
    if (!blinkingOn) {
      lastBlinkTimeInMillis = millis();
      blinkState = BlinkOnState;
      doUpdate = true;
    }
  }
  else {
    doUpdate = true;
  }
  
  blinkingOn = turnBlinkingOn;
  */
}

void LEDTimer::setColour(uint32_t colour) {
  timeColour = colour;
  doUpdate   = true;
}

void LEDTimer::update() {
  /*
  if (blinkingOn) {
    
    millisDiff = (millis() - lastBlinkTimeInMillis);
    switch (blinkState) {
      
      case BlinkOffState:
        if (millisDiff >= BLINK_TIME_MS) {
          blinkState = BlinkOnState;
          lastBlinkTimeInMillis = millis();
          doUpdate = true;
        }
        break;
        
      case BlinkOnState:
        if (millisDiff >= BLINK_TIME_MS) {
          blinkState = BlinkOffState;
          lastBlinkTimeInMillis = millis();
          doUpdate = true;
        }
        break;
        
      default:
        break;
    }
    
    if (doUpdate) {
      if (blinkState == BlinkOffState) {
        onesDigit.clearAllSegments();
        tensDigit.clearAllSegments();
      }
      else {
        onesDigit.displayDigit('0' + (timeDisplayed % 10), timeColour);
        tensDigit.displayDigit('0' + (timeDisplayed / 10), timeColour);
      }
      doUpdate = false;
    }
  }
  else if (doUpdate) {
    // Normal, non-blinking display
    onesDigit.displayDigit('0' + (timeDisplayed % 10), timeColour);
    tensDigit.displayDigit('0' + (timeDisplayed / 10), timeColour);
    doUpdate = false;
  } 
  */

  if (doUpdate) {
    // Normal, non-blinking display
    onesDigit.displayDigit('0' + (timeDisplayed % 10), timeColour);
    tensDigit.displayDigit('0' + (timeDisplayed / 10), timeColour);
    doUpdate = false;
  }
}

