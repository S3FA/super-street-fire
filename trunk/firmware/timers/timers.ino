/* Timers for super street fire */
/* Peter Rogesr 2013 */

#include <Adafruit_NeoPixel.h>
#include "SSF_Message.h"
#include "Segment.h"
#include "LEDTimer.h"

#define NODE_ADDRESS     35
#define RECV_ENABLE      8

#define BLINK_TIMER_TIME 10

/***********/
/* Globals */
/***********/

MessageBuf messageBuf;
LEDTimer timer;

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
  byte *buf = messageBuf.payload;
  uint32_t colour = 0;

  for (int pos = 0; pos < messageBuf.payloadLen; )
  {
    switch(buf[pos++])
    {
      case 'T': {
        int currTime = (int)buf[pos++];
        boolean blinkingOn = (currTime <= BLINK_TIMER_TIME && currTime > 0);
        /* Set time to display */
        timer.showTime(currTime, blinkingOn);
        break;
      }

      case 't':
        /* Colour to display */
        colour = (buf[pos] << 16) | (buf[pos + 1] << 8) | (buf[pos + 2]);
        timer.setColour(colour);
        pos += 3;
        break;

      default:
        break;
    }
  }
}

void readSerial()
{
  /* Consume as much serial data as available */
  while (Serial.available()) {
    messageBuf.receiveByte(Serial.read());
  }
  messageBuf.update();

  if (messageBuf.complete) {
    /* Process and clear the message */
    processMessage();
    messageBuf.clear();
  }
}

void setup()
{
  Serial.begin(57600, SERIAL_8N1);
  pinMode(RECV_ENABLE, OUTPUT);
  digitalWrite(RECV_ENABLE, LOW);

  timer.begin(); 
  timer.setColour(0xFF0000);
  timer.update();
}

void loop()
{
  readSerial();
  timer.update();

/*  uint32_t r = 128+(int)100*sin(millis()/200.0);
  uint32_t g = 128+(int)100*sin(millis()/200.0);
  uint32_t b = 128+(int)100*sin(millis()/200.0);
  uint32_t col = (r << 16) | (g << 8) | b;
  timer->showTime(millis()/1000);
  timer->setColour(col);
  timer->update();
  Serial.println(r, HEX);
  Serial.println(g, HEX);
  Serial.println(b, HEX);
  Serial.println(col);*/
}

