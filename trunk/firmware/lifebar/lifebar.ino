/* SSF Life and Charge Bar
Using OctoWS2811 library on Teensy 3.0

8 strips in 4 rows, 2 columns of 22 LEDs each
Looks like 4 continuous rows but updates faster
Arrange strips sequentially in the columns to simplify the code:
1 and 2, 3 and 4, etc

LEDs are addressed sequentially left to right, top to bottom

  Required Connections
  --------------------
    pin 2:  LED Strip #1    OctoWS2811 drives 8 LED Strips.
    pin 14: LED strip #2    All 8 are the same length.
    pin 7:  LED strip #3
    pin 8:  LED strip #4    A 100 ohm resistor should used
    pin 6:  LED strip #5    between each Teensy pin and the
    pin 20: LED strip #6    wire to the LED strip, to minimize
    pin 21: LED strip #7    high frequency ringing & noise.
    pin 5:  LED strip #8
    pin 15 & 16 - Connect together, but do not use
    pin 4 - Do not use
    pin 3 - Do not use as PWM.  Normal use is ok.
    

*/

#include <OctoWS2811.h>

#define NODE_ADDRESS 35
#define MIN_PACKET_LEN 1 //minumum possible packet length is 5 - 0xAA 0xAA length dest checksum
#define MAX_PACKET_LEN 15 //max payload plus overhead, by spec

#define RED    0xFF0000
#define GREEN  0x00FF00
#define BLUE   0x0000FF
#define YELLOW 0xFFFF00
#define PINK   0xFF1088
#define ORANGE 0xE05800
#define WHITE  0xFFFFFF
#define BLACK  0x000000

const int ledsPerStrip = 22; //8 strips in 4 rows, 2 columns of 22 LEDs each
DMAMEM int displayMemory[ledsPerStrip*6];
int drawingMemory[ledsPerStrip*6];
const int config = WS2811_GRB | WS2811_800kHz;
OctoWS2811 leds(ledsPerStrip, displayMemory, drawingMemory, config);

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

unsigned long previousHeartbeatTime = 0;

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

void setup()
{
  pinMode(13, OUTPUT); //heartbeat LED on teensy board
  Serial1.begin(9600);
  
  state = IDLE;
  
  leds.begin();
  leds.show();
  
  Serial1.println("serial start");
}

void loop()
{
//  Serial1.println("serial running");
  
  //1 Hz heartbeat on pin 13
  unsigned long currentHeartbeatTime = millis();
  
  if(currentHeartbeatTime - previousHeartbeatTime > 500) {
    previousHeartbeatTime = currentHeartbeatTime;
    
    if(digitalRead(13) == HIGH) {
      digitalWrite(13, LOW);
    }
    else {
      digitalWrite(13, HIGH);
    }
  }
  
//  Serial1.println("serial running after heartbeat");
  readSerial(); //decode a message, set all the variables
  
  switch(state) {
    case IDLE:
      break;
    case SHOW_LIFE:
      showLife(lifeValue, lifeColour);
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
  static byte incomingMsg[MAX_PACKET_LEN]; //message buffer, enough elements for the max length of a packet by spec
  static byte msgLength = MAX_PACKET_LEN; //set to longest allowed until we get the actual packet length
  static byte msgPos = 0;
    
  //this reads one byte per call of readSerial. if it was "while" instead of "if" it would read all the bytes
  //but either way it will return if the bytes aren't framing data
  
  if(!Serial.available()){
    return;
  }
    
  incomingMsg[msgPos++] = Serial.read();
    
  if((msgPos == 0 || msgPos == 1) && incomingMsg[msgPos] != 0xAA) { //are the framing bytes in first and second bytes of packet
    return;
  }
  //this should cause it to return immediately whenever it gets a byte while not reading in a packet
  //right?
    
  if(msgPos == 2) { //third byte of packet is the _payload_ length - add 4 to get _packet_ length
    msgLength = incomingMsg[msgPos] + 4;
  }
    
  //surely i want to stop reading immediately if a packet doesn't have the framing bytes
  //or if it isn't addressed to me?
    
  //something similar for the address? like this:
  
  if(msgPos == 3 && incomingMsg[msgPos] != NODE_ADDRESS) { //fourth byte of packet is the address
    return;
  }
    
  //byte is unsigned, so will it truncate when it overflows or do weird negative things?
    
  if(msgPos >= msgLength) { //done reading message - check the checksum, parse
    byte checksum = 0;
    for(byte i = 3; i <= msgLength + 3; i++) {
      checksum = checksum + incomingMsg[i];
    }

    if(~checksum == incomingMsg[msgPos]) {
      //checksum is good, message is addressed to this node, parse the command
      byte parsePos = 4; //commands and data start at 4, after the address
      while(parsePos <= msgLength - 1) {
        switch(incomingMsg[parsePos]) {
          case 'L': //life value
            lifeValue = incomingMsg[parsePos + 1];
            state = SHOW_LIFE;
            parsePos++;
            break;
          case 'l': //life colour
            lifeColour = incomingMsg[parsePos + 1] << 16 + incomingMsg[parsePos + 2] << 8 + incomingMsg[parsePos + 3];
            state = SHOW_LIFE;
            parsePos = parsePos + 3; //skip the index over the data to the next potential command
            break;
          case 'C': //charge value
            chargeValue = incomingMsg[parsePos + 1];
            state = SHOW_CHARGE;
            parsePos++;
            break;
          case 'c': //charge colour
            chargeColour = incomingMsg[parsePos + 1] << 16 + incomingMsg[parsePos + 2] << 8 + incomingMsg[parsePos + 3];
            state = SHOW_CHARGE;
            parsePos = parsePos + 3; //skip the index over the data to the next potential command
            break;
          case 'A': //idle animation pattern
            state = IDLE_ANIM;
            animation = (ANIMATION)incomingMsg[parsePos + 1];
            break;
          case 'a': //idle animation period
            animPeriod = incomingMsg[parsePos + 1];
            break;
        }
      }
    }
  }
}

void showLife(byte lifeValue, long lifeColour) { //display the current life value
  for(int i; i <= lifeValue; i++) {
    leds.setPixel(i, lifeColour);
    leds.setPixel(i + ledsPerStrip * 2, lifeColour);
    leds.setPixel(i + ledsPerStrip * 4, lifeColour);
  }
  
  leds.show();
}

void showCharge(byte chargeValue, long chargeColour) {
  for(int i; i <= chargeValue; i++) {
    leds.setPixel(i + ledsPerStrip * 6, chargeColour);
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

