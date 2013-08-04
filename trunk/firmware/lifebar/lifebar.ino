/* SSF Life and Charge Bar
 * Starter code taken from using OctoWS2811 library on Teensy 3.0
 * 
 * Author Trevyn Watson
 * Contributions by Peter Rogers
 *
 * 8 strips in 4 rows, 2 columns of 22 LEDs each
 * Looks like 4 continuous rows but updates faster
 * Arrange strips sequentially in the columns to simplify the code:
 * 1 and 2, 3 and 4, etc
 * 
 * LEDs are addressed sequentially left to right, top to bottom
 * 
 *   Required Connections
 *   --------------------
 *   pin 2:  LED Strip #1    OctoWS2811 drives 8 LED Strips.
 *   pin 14: LED strip #2    All 8 are the same length.
 *   pin 7:  LED strip #3
 *   pin 8:  LED strip #4    A 100 ohm resistor should used
 *   pin 6:  LED strip #5    between each Teensy pin and the
 *   pin 20: LED strip #6    wire to the LED strip, to minimize
 *   pin 21: LED strip #7    high frequency ringing & noise.
 *   pin 5:  LED strip #8
 *   pin 15 & 16 - Connect together, but do not use
 *   pin 4 - Do not use
 *   pin 3 - Do not use as PWM.  Normal use is ok.
 *
 */

#include <OctoWS2811.h>
#include "SSF_Heartbeat.h"
#include "SSF_Message.h"

/*************/
/* Constants */
/*************/

#define DEBUG          1

#define NODE_ADDRESS   33
#define HEART_LED      13

#define RED            0xFF0000
#define GREEN          0x00FF00
#define BLUE           0x0000FF
#define YELLOW         0xFFFF00
#define PINK           0xFF1088
#define ORANGE         0xE05800
#define WHITE          0xFFFFFF
#define BLACK          0x000000

/*
 * LED strip arrangement:
 *
 * Lifebar 1a | Lifebar 1b      0...21  |  22...43
 * Lifebar 2a | Lifebar 2b     44...65  |  66...87
 * Lifebar 3a | Lifebar 3b     88..109  | 110..131
 * Charge 1a  | Charge 1b     132..153  | 154..175
 */
#define LEDS_PER_STRIP   22 //8 strips in 4 rows, 2 columns of 22 LEDs each
#define BAR_LENGTH       (LEDS_PER_STRIP*2)

#define LIFEBAR1         0
#define LIFEBAR2         BAR_LENGTH
#define LIFEBAR3         (2*BAR_LENGTH)
#define CHARGEBAR        (3*BAR_LENGTH)

enum STATE //overall state
{
  IDLE,
  SHOW_LIFE,
  SHOW_CHARGE,
  IDLE_ANIM,
} state;

enum ANIMATION //animation state
{
  AN_BLACKOUT,
  AN_WHITEOUT,
  AN_FLASH,
  AN_LARSON,
  AN_DIAMOND,
  AN_MATRIX,
  AN_RAINBOW,
} animation;

/***********/
/* Globals */
/***********/

DMAMEM byte displayMemory[LEDS_PER_STRIP*8*3];
byte drawingMemory[LEDS_PER_STRIP*8*3];
OctoWS2811 leds(LEDS_PER_STRIP, displayMemory, drawingMemory, WS2811_GRB | WS2811_800kHz);
Heartbeat heartbeat;

/* The MessageBuf for receiving data from RS485 */
MessageBuf messageBuf;

unsigned long previousMillis = 0;

int animPos = 0; //can these all be one iterator used to store the position in
byte animDir = 0; //a place to save the state of the anim direction

byte lifeValue = 0;
long lifeColour = 0xFF0000; //default life colour is red
byte chargeValue = 0;
long chargeColour= 0x0000FF; //default charge colour is blue

byte animPeriod = 0;
long animColour1 = 0;
long animColour2 = 0;

/*************/
/* Functions */
/*************/

void processMessage()
{
  if (!messageBuf.complete) {
    return;
  }
  if (messageBuf.destination != NODE_ADDRESS) {
    /* Command is not for this node */
    return;
  }
  /* Parse the payload and look for commands */
  byte *incomingMsg = messageBuf.payload;
  for (int parsePos = 0; parsePos < messageBuf.payloadLen; )
  {
    switch(incomingMsg[parsePos++])
    {
      case 'L': //life value
        lifeValue = incomingMsg[parsePos++];
        state = SHOW_LIFE;
        break;

      case 'l': //life colour
        lifeColour = (incomingMsg[parsePos] << 16) | (incomingMsg[parsePos + 1] << 8) | (incomingMsg[parsePos + 2]);
        state = SHOW_LIFE;
        parsePos += 3; //skip the index over the data to the next potential command
        break;

      case 'C': //charge value
        chargeValue = incomingMsg[parsePos++];
        state = SHOW_LIFE;
        break;

      case 'c': //charge colour
        chargeColour = (incomingMsg[parsePos] << 16) | (incomingMsg[parsePos + 1] << 8) | (incomingMsg[parsePos + 2]);
        state = SHOW_LIFE;
        parsePos += 3; //skip the index over the data to the next potential command
        break;

      case 'A': //idle animation pattern
        state = IDLE_ANIM;
        animation = (ANIMATION)incomingMsg[parsePos++];
        break;

      case 'a': //idle animation period
        animPeriod = incomingMsg[parsePos++];
        break;
    }
  }
}

void setup()
{
  pinMode(HEART_LED, OUTPUT); //heartbeat LED on teensy board
  Serial1.begin(57600);
  Serial.begin(38400);

  state = IDLE;
    
  leds.begin();
  leds.show();
  
  showLife(lifeValue, lifeColour);
  showCharge(chargeValue, chargeColour);
}

void loop()
{
  // 1 Hz heartbeat
  heartbeat.update();

  /* Handle serial input and dump it into the message buffer */
  readSerial();

  /* Manage the state machine */  
  switch(state) {
    case IDLE:
      break;

    case SHOW_LIFE:
      showLife(lifeValue, lifeColour);
      showCharge(chargeValue, chargeColour);
      state = IDLE;
      break;

    case IDLE_ANIM:
      switch(animation)
      {
        case AN_BLACKOUT:
          blackoutAnim(animPeriod);
        case AN_WHITEOUT:
          whiteoutAnim(animPeriod);
        case AN_FLASH:
          flashAnim(animPeriod, animColour1, animColour2);
        case AN_LARSON:
          larsonAnim(animPeriod, animColour1);
        case AN_DIAMOND:
          diamondAnim(animPeriod, animColour1);
          break;
        default:
          break;
      }
      break;      

    default:
      break;
  }
}

//read a serial message
//it looks like this:
//0xAA 0xAA [length] [dest] [command] [value] ... [checksum]
void readSerial()
{
  /* Consume as much serial data as available. The buffer will fill up until there is an entire package of 
   * data, in which case we process it below. */
  while (Serial1.available()) {
    messageBuf.receiveByte(Serial1.read());
  }
  messageBuf.update();

  if (messageBuf.complete) {
    /* Process and clear the message */
    processMessage();
    messageBuf.clear();
  }
}

void showLife(byte lifeValue, uint32_t lifeColour) { //display the current life value

#if DEBUG
  Serial.print("show life ");
  Serial.print(lifeValue);
  Serial.print(" ");
  Serial.println(lifeColour);
#endif

  uint32_t col;
  for(int i = 0; i < BAR_LENGTH; i++) {
    if (i <= lifeValue) {
      col = lifeColour;
    } else {
      col = 0;
    }
    leds.setPixel(LIFEBAR1 + i, col);
    leds.setPixel(LIFEBAR2 + i, col);
    leds.setPixel(LIFEBAR3 + i, col);
  }
  leds.show();
}

void showCharge(byte chargeValue, long chargeColour) {
#if DEBUG
  Serial1.print("show charge ");
  Serial1.print(chargeValue);
  Serial1.print(" ");
  Serial1.println(chargeColour);
#endif

  uint32_t col;
  for(int i=0; i < BAR_LENGTH; i++) {
    if (i <= chargeValue) {
      col = chargeColour;
    } else {
      col = 0;
    }
    leds.setPixel(CHARGEBAR + i, col);
  }
  leds.show();  
}

//flying state monster something something - the FSM saves the value of period and calls larson
//when appropriate, calling it repeatedly when in the "animate larson scanner" state, right?
//so it will pass in period and colour every time, right? so i don't need to worry about
//saving them at this point?

void larsonAnim(int period, long colour) { //larson scanner effect without delays
//period is the time for one complete cycle, colour is an RGB triplet
  int i = 0;
    
  unsigned long currentMillis = millis();
  
  if(currentMillis - previousMillis >= (period / (2 * leds.numPixels() / 8)))
  {
    previousMillis = currentMillis;
    
    //calculate the two faded colours - subtract a value from each component
    //docs say don't do arithmetic in min() or i'd collapse this all into two lines
    
    byte colour1R = (colour & 0xFF0000) - 0xAA0000;
    byte colour1G = (colour & 0x00FF00) - 0x00AA00;
    byte colour1B = (colour & 0x0000FF) - 0x0000AA;
    
    byte colour2R = (colour & 0xFF0000) - 0xEA0000;
    byte colour2G = (colour & 0x00FF00) - 0x00EA00;
    byte colour2B = (colour & 0x0000FF) - 0x0000EA;
    
    colour1R = min(colour1R, 0);
    colour1G = min(colour1G, 0);
    colour1B = min(colour1B, 0);

    colour2R = min(colour2R, 0);
    colour2G = min(colour2G, 0);
    colour2B = min(colour2B, 0);
    
    long colour1 = colour1R + colour1G + colour1B;
    long colour2 = colour2R + colour2G + colour2B;

    if(animDir <= 0) {
      leds.setPixel(min(0, animPos + 1), BLACK);
      leds.setPixel(min(0, animPos + 2), colour2);
      leds.setPixel(min(0, animPos + 3), colour1);
      leds.setPixel(animPos, colour);
      leds.show();
      animPos--;
    }

    if(animDir >= 1) {
      leds.setPixel(animPos, colour);
      leds.setPixel(min(0, animPos + 1), colour1);
      leds.setPixel(min(0, animPos + 2), colour2);
      leds.setPixel(min(0, animPos + 3), BLACK);
      leds.show();
    }
    
    //if position tracker is at either end, switch direction
    if(animPos >= leds.numPixels() / 8) 
    {
      animDir = 0;
    }
    else if(animPos <= 0)
    {
      animDir = 1;
    }
    
    //increment or decrement the position tracker depending on direction
    if(animDir <= 0)
    {
      animPos--;
    }
    else if(animDir >= 1)
    {
      animPos++;
    }
  }
}

void diamondAnim(byte animPeriod, int animColour) {
  
}

void whiteoutAnim(byte animPeriod) {
}

void blackoutAnim(byte animPeriod) {

}

void flashAnim(byte animPeriod, long colour1, long colour2) {
  
}


void colorWipe(int color, int wait)
{
  for (int i=0; i < leds.numPixels(); i++)
  {
    leds.setPixel(i, color);

    //delayMicroseconds(wait);
  }
  leds.show();
}

void larsonDelay()
{
  for(int i = 2; i <= (leds.numPixels() / 8) - 2; i++)
  {
    leds.setPixel(i-3, BLACK);
    leds.setPixel(i-2, 0x150500);
    leds.setPixel(i-1, 0x550000);
    leds.setPixel(i, 0xFF0000);
    leds.setPixel(i+1, 0x550000);
    leds.setPixel(i+2, 0x150500);
    leds.setPixel(i+3, BLACK);
    leds.show();
    delay(75);
  }
  
  for(int i = (leds.numPixels() / 8) - 3; i > 2; i--)
  {
    leds.setPixel(i-3, BLACK);
    leds.setPixel(i-2, 0x150500);
    leds.setPixel(i-1, 0x550000);
    leds.setPixel(i, 0xFF0000);
    leds.setPixel(i+1, 0x550000);
    leds.setPixel(i+2, 0x150500);
    leds.setPixel(i+3, BLACK);
    leds.show();
    delay(75);
  }
}

