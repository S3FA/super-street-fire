/* SSF Life and Charge Bar
 * Starter code taken from using OctoWS2811 library on Teensy 3.0
 * 
 * Author Trevyn Watson
 * Contributions by Peter Rogers, Callum Hay
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

/*************/
/* Constants */
/*************/

#define DEBUG          1

#define NODE_ADDRESS   37
#define HEART_LED      13

#define RED            0xFF0000
#define GREEN          0x00FF00
#define BLUE           0x0000FF
#define YELLOW         0xFFFF00
#define PINK           0xFF1088
#define ORANGE         0xE05800
#define WHITE          0xFFFFFF
#define BLACK          0x000000

#define BLOCK_COLOUR 0xFF00EE

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

#define BLOCK_FLASH_BLINK_TIME_IN_MS 200

enum STATE //overall state
{
  IDLE,
  SHOW_LIFE_AND_CHARGE,
  SHOW_CHARGE,
  SHOW_BLOCK,
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

unsigned long previousMillis = 0;

int animPos = 0; //can these all be one iterator used to store the position in
byte animDir = 0; //a place to save the state of the anim direction

byte lifeValue = 0;
long lifeColour = RED;
byte chargeValue = 0;
long chargeColour= BLUE;

byte animPeriod = 0;
long animColour1 = 0;
long animColour2 = 0;

unsigned long blockLengthInMillis = 0;
unsigned long blockStartTimeInMillis = 0;

int i;        // loop counter
int tempInt;  // temporary holder for int values

/* Messages (payload + framing) look like this: 
 *
 * 0xAA        ---+
 * 0xAA           |  Header bytes
 * [length]    ---+
 * 
 * [dest]      ---+
 * [command]      |  Payload
 * [value]        |
 * ...         ---+
 *
 * [checksum]  ---+  Trailer byte
 *
 */

#define FRAMING_BYTE        0xAA
#define NUM_HEADER_BYTES    3
#define NUM_FRAMING_BYTES   (NUM_HEADER_BYTES + 1)
#define PAYLOAD_START       NUM_HEADER_BYTES

#define MIN_MESSAGE_LEN  5   // Minumum possible packet length - 0xAA 0xAA length dest checksum
#define MAX_MESSAGE_LEN  15  // Maximum possible packet length - 0xAA 0xAA length dest checksum
#define MAX_PAYLOAD_LEN  (MAX_MESSAGE_LEN - NUM_FRAMING_BYTES)

#define TIMEOUT_IN_MILLIS 800

// Serial Read and Message Cache Variables
unsigned long messageStartTimeInMillis; // holds the starting time that a message was recieved in
int payloadLength = -1;
int messageLength = -1;
int checksum      = -1;

byte payloadBuffer[MAX_PAYLOAD_LEN];

/*************/
/* Functions */
/*************/

void processMessage() {

  // Parse the payload and look for commands
  for (i = 0; i < payloadLength;) {

    switch (payloadBuffer[i++]) {
      
      case 'L': //life value
        lifeValue = payloadBuffer[i++];
        if (state != SHOW_BLOCK) {
          state = SHOW_LIFE_AND_CHARGE;
        }
        break;

      case 'l': //life colour
        lifeColour = (payloadBuffer[i] << 16) | (payloadBuffer[i + 1] << 8) | (payloadBuffer[i + 2]);
        if (state != SHOW_BLOCK) {
          state = SHOW_LIFE_AND_CHARGE;
        }
        i += 3; //skip the index over the data to the next potential command
        break;

      case 'C': //charge value
        chargeValue = payloadBuffer[i++];
        if (state != SHOW_BLOCK) {
          state = SHOW_LIFE_AND_CHARGE;
        }
        break;

      case 'c': //charge colour
        chargeColour = (payloadBuffer[i] << 16) | (payloadBuffer[i + 1] << 8) | (payloadBuffer[i + 2]);
        if (state != SHOW_BLOCK) {
          state = SHOW_LIFE_AND_CHARGE;
        }
        i += 3; //skip the index over the data to the next potential command
        break;

      case 'b': // Tells the board that there's a block window available to the player... overlay it on the lifebar/chargebar
        state = SHOW_BLOCK;
        blockLengthInMillis = (payloadBuffer[i] << 16) | (payloadBuffer[i + 1] << 8) | (payloadBuffer[i + 2]);
        blockStartTimeInMillis = millis();
        break;
        
      case 'B': // Tells the board to cancel any currently executing block
        blockLengthInMillis = 0;
        state = SHOW_LIFE_AND_CHARGE;
        break;

      case 'A': //idle animation pattern
        state = IDLE_ANIM;
        animation = (ANIMATION)payloadBuffer[i++];
        break;

      case 'a': //idle animation period
        animPeriod = payloadBuffer[i++];
        break;
        
      default:
        break;
    }

  }
}

void setup() {
  
  pinMode(HEART_LED, OUTPUT); //heartbeat LED on teensy board
  Serial1.begin(57600);

  state = IDLE;
 
  leds.begin();
  leds.show();
  
  lifeValue = 0;
  lifeColour = 0x000000;
  chargeValue = 0;
  chargeColour = 0x000000;
  showLife(lifeValue, lifeColour, false);
  showCharge(chargeValue, chargeColour, true);
}

void loop() {
  // 1 Hz heartbeat
  heartbeat.update();
  // Handle serial input and dump it into the message buffer
  updateSerial();
  // Update the state
  updateState();
}

boolean isTimeout() {
  return (millis() - messageStartTimeInMillis) > TIMEOUT_IN_MILLIS;
}

//read a serial message
//it looks like this:
//0xAA 0xAA [length] [dest] [command] [value] ... [checksum]
void updateSerial() {

  if (Serial1.available() <= 0) {
    return;
  }

  tempInt = Serial1.read(); // Read the first framing byte
  if (tempInt != FRAMING_BYTE) {
    return;
  }

  // Set the start time for reading the rest of the message, this acts
  // as a comparison value to check for timeouts
  messageStartTimeInMillis = millis();
    
  // Wait for the next framing byte...
  while (Serial1.available() <= 0) { if (isTimeout()) { return; } }

  tempInt = Serial1.read(); // Read the second framing byte
  if (tempInt != FRAMING_BYTE) {
    return;
  }
  
  // Wait for the next byte, which will indicate the length of the payload
  while (Serial1.available() <= 0) { if (isTimeout()) { return; } }
    
  // Read the length of the payload...
  payloadLength = Serial1.read();
  messageLength = payloadLength + NUM_FRAMING_BYTES;
  
  // If the payload is an invalid length then exit
  if ((payloadLength <= 0) || (payloadLength > MAX_PAYLOAD_LEN)) {
    return;
  }
  
  // Wait until we have the entire payload available on the serial line...
  while (Serial1.available() < payloadLength) { if (isTimeout()) { return; } }
  
  // The first byte of the payload buffer will be the destination address - if it isn't
  // addressed to this node then we're outta here
  payloadBuffer[0] = Serial1.read();
  if (payloadBuffer[0] != NODE_ADDRESS) {
    return;
  }
  
  // Read in the payload buffer and zero out the rest   
  for (i = 1; i < payloadLength; i++) {
    payloadBuffer[i] = Serial1.read();
  }
  for (; i < MAX_PAYLOAD_LEN; i++) {
    payloadBuffer[i] = 0;
  }
  
  // Wait for the final byte in the message: the checksum
  while (Serial1.available() <= 0) { if (isTimeout()) { return; } }
  
  // Read in the checksum and then calculate and compare, we don't do further
  // processing on invalid checksums
  checksum = Serial1.read();
  tempInt = 0;
  for (i = 0; i < payloadLength; i++) {
    tempInt += payloadBuffer[i];
  }
  if (((byte)~tempInt) != checksum) {
    return;
  }
  
  // The checksum is good, yay! Continue processing the message...
  processMessage();
}

void updateState() {
   /* Manage the state machine */  
  switch(state) {
    case IDLE:
      break;

    case SHOW_LIFE_AND_CHARGE:
      showLife(lifeValue, lifeColour, false);
      showCharge(chargeValue, chargeColour, true);
      state = IDLE;
      break;

    case SHOW_BLOCK: {
      
      // Check to see if we should be showing the block window...
      unsigned long diffTimeInMillis = millis() - blockStartTimeInMillis;
      
      if (diffTimeInMillis <= blockLengthInMillis) { 
        // TODO: Optimize so we don't overdraw/redraw these each time?
        showLife(lifeValue, lifeColour, false);
        showCharge(chargeValue, chargeColour, false);
        showBlockWindowBar(diffTimeInMillis);
      }
      else {
        // The block window time has expired, don't show the block window any more, overwrite
        // it with the life and charge bars and go back to the typical 'IDLE' state
        state = IDLE;
        showLife(lifeValue, lifeColour, false);
        showCharge(chargeValue, chargeColour, true);
      }
      
      break;
    }

    case IDLE_ANIM:
      switch (animation) {
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

// display the current life value
void showLife(byte lifeValue, uint32_t lifeColour, boolean doShow) {
  
  if (lifeValue == 0) {
    for (int i = 0; i < BAR_LENGTH; i++) {
      leds.setPixel(LIFEBAR1 + i, BLACK);
      leds.setPixel(LIFEBAR2 + i, BLACK);
      leds.setPixel(LIFEBAR3 + i, BLACK);
    }
  }
  else {
    uint32_t col;
    for (int i = 0; i < BAR_LENGTH; i++) {
      if (i <= lifeValue) {
        col = lifeColour;
      }
      else {
        col = 0;
      }
      leds.setPixel(LIFEBAR1 + i, col);
      leds.setPixel(LIFEBAR2 + i, col);
      leds.setPixel(LIFEBAR3 + i, col);
    }
  }
  
  if (doShow) {
    leds.show();
  }
}

void showCharge(byte chargeValue, long chargeColour, boolean doShow) {
  
  if (chargeValue == 0) {
   for (int i = 0; i < BAR_LENGTH; i++) {
      leds.setPixel(CHARGEBAR + i, BLACK);
    }
  }
  else {
    uint32_t col;
    for (int i = 0; i < BAR_LENGTH; i++) {
      if (i <= chargeValue) {
        col = chargeColour;
      }
      else {
        col = BLACK;
      }
      leds.setPixel(CHARGEBAR + i, col);
    }
  }
  
  if (doShow) {
    leds.show();
  }
}

/**
 * Prints/shows a block "window" over one of the life rows, this indicates the length
 * of time available to the player for them to block an incoming attack.
 */
void showBlockWindowBar(unsigned long timeSinceBlockStart) {
  int blockBarValue = map(timeSinceBlockStart, 0, blockLengthInMillis, BAR_LENGTH, 0);
  if (blockBarValue == 0) {
    leds.show();
    return;
  }
  
  uint32_t col;
  for (int i = 0; i < BAR_LENGTH; i++) {
    if (i <= blockBarValue) {
      col = BLOCK_COLOUR;
    }
    else {
      col = BLACK;
    }
    leds.setPixel(LIFEBAR1 + i, col);
  }
  leds.show();
}

/**
 * Prints the word "block" in lower-case over and across the charge and life bars.
 * Used when the player is being told to block an incoming attack.
 */
void showBlockWord() {
  setPixelsForOneBlockWord(WHITE, LEDS_PER_STRIP/2);
  leds.show();
}

void setPixelsForOneBlockWord(uint32_t blockWordColour, int offset) {
  // Here's the layout for the lower-case word "block", starting at the given offset
  // LIFEBAR1  o o x o o o x o o o o o o o o o x o x o o o 
  // LIFEBAR2  o o x x x o x o x x x o x x x o x x o o o o
  // LIFEBAR3  o o x o x o x o x o x o x o o o x o x o o o
  // CHARGEBAR o o x x x o x o x x x o x x x o x o x o o o
  
  leds.setPixel(LIFEBAR1 + 2 + offset,  blockWordColour);
  leds.setPixel(LIFEBAR1 + 6 + offset,  blockWordColour);
  leds.setPixel(LIFEBAR1 + 16 + offset, blockWordColour);
  leds.setPixel(LIFEBAR1 + 18 + offset, blockWordColour);
  
  leds.setPixel(LIFEBAR2 + 2 + offset,  blockWordColour);
  leds.setPixel(LIFEBAR2 + 3 + offset,  blockWordColour);
  leds.setPixel(LIFEBAR2 + 4 + offset,  blockWordColour);
  leds.setPixel(LIFEBAR2 + 6 + offset,  blockWordColour);
  leds.setPixel(LIFEBAR2 + 8 + offset,  blockWordColour);
  leds.setPixel(LIFEBAR2 + 9 + offset,  blockWordColour);
  leds.setPixel(LIFEBAR2 + 10 + offset, blockWordColour);
  leds.setPixel(LIFEBAR2 + 12 + offset, blockWordColour);
  leds.setPixel(LIFEBAR2 + 13 + offset, blockWordColour);
  leds.setPixel(LIFEBAR2 + 14 + offset, blockWordColour);  
  leds.setPixel(LIFEBAR2 + 16 + offset, blockWordColour);
  leds.setPixel(LIFEBAR2 + 17 + offset, blockWordColour); 
  
  leds.setPixel(LIFEBAR3 + 2 + offset,  blockWordColour);
  leds.setPixel(LIFEBAR3 + 4 + offset,  blockWordColour);
  leds.setPixel(LIFEBAR3 + 6 + offset,  blockWordColour);
  leds.setPixel(LIFEBAR3 + 8 + offset,  blockWordColour);
  leds.setPixel(LIFEBAR3 + 10 + offset, blockWordColour);
  leds.setPixel(LIFEBAR3 + 12 + offset, blockWordColour);
  leds.setPixel(LIFEBAR3 + 16 + offset, blockWordColour);
  leds.setPixel(LIFEBAR3 + 18 + offset, blockWordColour);
  
  leds.setPixel(CHARGEBAR + 2 + offset,  blockWordColour);  
  leds.setPixel(CHARGEBAR + 3 + offset,  blockWordColour);
  leds.setPixel(CHARGEBAR + 4 + offset,  blockWordColour);
  leds.setPixel(CHARGEBAR + 6 + offset,  blockWordColour);
  leds.setPixel(CHARGEBAR + 8 + offset,  blockWordColour);
  leds.setPixel(CHARGEBAR + 9 + offset,  blockWordColour);
  leds.setPixel(CHARGEBAR + 10 + offset, blockWordColour);
  leds.setPixel(CHARGEBAR + 12 + offset, blockWordColour);
  leds.setPixel(CHARGEBAR + 13 + offset, blockWordColour);
  leds.setPixel(CHARGEBAR + 14 + offset, blockWordColour);
  leds.setPixel(CHARGEBAR + 16 + offset, blockWordColour);
  leds.setPixel(CHARGEBAR + 18 + offset, blockWordColour);
}

//flying state monster something something - the FSM saves the value of period and calls larson
//when appropriate, calling it repeatedly when in the "animate larson scanner" state, right?
//so it will pass in period and colour every time, right? so i don't need to worry about
//saving them at this point?

void larsonAnim(int period, long colour) { //larson scanner effect without delays
//period is the time for one complete cycle, colour is an RGB triplet
  int i = 0;
    
  unsigned long currentMillis = millis();
  
  if (currentMillis - previousMillis >= (period / (2 * leds.numPixels() / 8))) {
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

    if (animDir <= 0) {
      leds.setPixel(min(0, animPos + 1), BLACK);
      leds.setPixel(min(0, animPos + 2), colour2);
      leds.setPixel(min(0, animPos + 3), colour1);
      leds.setPixel(animPos, colour);
      leds.show();
      animPos--;
    }

    if (animDir >= 1) {
      leds.setPixel(animPos, colour);
      leds.setPixel(min(0, animPos + 1), colour1);
      leds.setPixel(min(0, animPos + 2), colour2);
      leds.setPixel(min(0, animPos + 3), BLACK);
      leds.show();
    }
    
    //if position tracker is at either end, switch direction
    if (animPos >= leds.numPixels() / 8) {
      animDir = 0;
    }
    else if (animPos <= 0)
    {
      animDir = 1;
    }
    
    //increment or decrement the position tracker depending on direction
    if (animDir <= 0) {
      animPos--;
    }
    else if (animDir >= 1) {
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

void colorWipe(int color, int wait) {
  for (int i=0; i < leds.numPixels(); i++) {
    leds.setPixel(i, color);

    //delayMicroseconds(wait);
  }
  leds.show();
}

void larsonDelay() {
  for (int i = 2; i <= (leds.numPixels() / 8) - 2; i++) {
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
  
  for(int i = (leds.numPixels() / 8) - 3; i > 2; i--) {
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

