/* Class for managing the LED timer at a high-level. You can set the number to 
 * display and the colour to display it in. */

#ifndef __LEDTIMER_H__
#define __LEDTIMER_H__

#include "Segment.h"

class LEDTimer
{
  public:
    /* The tens and ones digits */
    Segment onesDigit;
    Segment tensDigit;

    /* Whether we need to update the strips */
    boolean doUpdate;
    /* The time to display */
    byte timeDisplayed;
    /* WHat colour to use */
    uint32_t timeColour;

    LEDTimer();
    
    void begin();
    /* Change the timer colour. Note changes are made on the next call to 'update' */
    void setColour(uint32_t colour);
    /* Change the time displayed. Note changes are made on the next call to 'update' */
    void showTime(int tm /*, boolean turnBlinkingOn*/);
    
    /* Call this function periodically to have the colour strip be updated. Note this
     * class is smart about doing updates - it only updates the strip when it needs
     * to. so it's safe to call this function a lot. */
    void update();
    
  //private:
    //boolean blinkingOn;
    //uint32_t lastBlinkTimeInMillis;
    //uint32_t millisDiff;
    //enum BlinkState { BlinkOffState, BlinkOnState } blinkState;
};

#endif

