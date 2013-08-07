/* Timers for super street fire */
/* Peter Rogesr 2013 */

#include <Adafruit_NeoPixel.h>

#include "Segment.h"
#include "LEDTimer.h"

#define NODE_ADDRESS     35
#define RECV_ENABLE      8

#define BLINK_TIMER_TIME 10

#define RED            0xFF0000
#define GREEN          0x00FF00
#define BLUE           0x0000FF
#define YELLOW         0xFFFF00
#define MAGENTA        0xFF00FF
#define PINK           0xFF1088
#define ORANGE         0xE05800
#define WHITE          0xFFFFFF
#define BLACK          0x000000

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
#define NUM_FRAMING_BYTES   (NUM_HEADER_BYTES+1)
#define PAYLOAD_START       NUM_HEADER_BYTES

#define MIN_MESSAGE_LEN  5   // Minumum possible packet length - 0xAA 0xAA length dest checksum
#define MAX_MESSAGE_LEN  12  // Maximum possible packet length - 0xAA 0xAA length dest checksum
#define MAX_PAYLOAD_LEN  (MAX_MESSAGE_LEN - NUM_FRAMING_BYTES)

#define TIMEOUT_IN_MILLIS 800

/***********/
/* Globals */
/***********/

LEDTimer timer;

int i = -1;          // loop counter
uint32_t tempColour; // temporary holder for colour
int tempInt;         // temporary holder for int values

unsigned long messageStartTimeInMillis; // holds the starting time that a message was recieved in

// Serial Read and Message Cache Variables
int payloadLength = -1;
int messageLength = -1;
int checksum      = -1;

byte payloadBuffer[MAX_PAYLOAD_LEN];

/*************/
/* Functions */
/*************/

void processMessage() {

  for (i = 0; i < payloadLength;) {
    switch (payloadBuffer[i++]) {
      
      case 'T':
        timer.showTime(payloadBuffer[i++]/*, (tempByte <= BLINK_TIMER_TIME && tempByte > 0)*/);
        break;

      case 't':
        // Colour to display
        tempColour = (((uint32_t)(payloadBuffer[i])) << 16) | (((uint32_t)payloadBuffer[i + 1]) << 8) | ((uint32_t)payloadBuffer[i + 2]);
        timer.setColour(tempColour);
        i += 3;
        break;

      default:
        break;
    }
  }
}

boolean isTimeout() {
  return (millis() - messageStartTimeInMillis) > TIMEOUT_IN_MILLIS;
}

void updateSerial() {
  
  if (Serial.available() <= 0) {
    return;
  }

  tempInt = Serial.read(); // Read the first framing byte
  if (tempInt != FRAMING_BYTE) {
    return;
  }
  
  // Set the start time for reading the rest of the message, this acts
  // as a comparison value to check for timeouts
  messageStartTimeInMillis = millis();
    
  // Wait for the next framing byte...
  while (Serial.available() <= 0) { if (isTimeout()) { return; } }
    
  tempInt = Serial.read(); // Read the second framing byte
  if (tempInt != FRAMING_BYTE) {
    return;
  }
      
  // Wait for the next byte, which will indicate the length of the payload
  while (Serial.available() <= 0) { if (isTimeout()) { return; } }
    
  // Read the length of the payload...
  payloadLength = Serial.read();
  messageLength = payloadLength + NUM_FRAMING_BYTES;
  
  // If the payload is an invalid length then exit
  if ((payloadLength <= 0) || (payloadLength > MAX_PAYLOAD_LEN)) {
    return;
  }
  
  // Wait until we have the entire payload available on the serial line...
  while (Serial.available() < payloadLength) { if (isTimeout()) { return; } }
  
  // The first byte of the payload buffer will be the destination address - if it isn't
  // addressed to this node then we're outta here
  payloadBuffer[0] = Serial.read();
  if (payloadBuffer[0] != NODE_ADDRESS) {
    return;
  }
  
  // Read in the payload buffer and zero out the rest   
  for (i = 1; i < payloadLength; i++) {
    payloadBuffer[i] = Serial.read();
  }
  for (; i < MAX_PAYLOAD_LEN; i++) {
    payloadBuffer[i] = 0;
  }
  
  // Wait for the final byte in the message: the checksum
  while (Serial.available() <= 0) { if (isTimeout()) { return; } }
  
  // Read in the checksum and then calculate and compare, we don't do further
  // processing on invalid checksums
  checksum = Serial.read();
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

void setup() {
  Serial.begin(57600);
  pinMode(RECV_ENABLE, OUTPUT);
  digitalWrite(RECV_ENABLE, LOW);

  timer.begin(); 
  timer.setColour(YELLOW);
  timer.update();
}

void loop() {
  updateSerial();
  timer.update();
}

