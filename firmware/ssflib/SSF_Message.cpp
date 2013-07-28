/* MessageBuf.cpp - Peter Rogers 2013 */
#include "SSF_Message.h"

/* Messages (payload + framing) look like this: 
 *
 * 0xAA        ---+
 * 0xAA           |  Header bytes
 * [length]       |
 * [dest]      ---+
 *
 * [command]   ---+  Payload
 * [value]        |
 * ...         ---+
 *
 * [checksum]  ---+  Trailer byte
 *
 */

#define FRAMING_BYTE          0xAA
#define NUM_HEADER_BYTES      4
#define NUM_FRAMING_BYTES     (NUM_HEADER_BYTES+1)
#define PAYLOAD_START         NUM_HEADER_BYTES
#define TIMEOUT               1000

#define DEBUG                 0

MessageBuf::MessageBuf()
{
  payload = buffer + PAYLOAD_START;
  clear();
}

void MessageBuf::clear()
{
  messageLen = 0;
  pos = 0;
  complete = false;
  lastReceivedTime = 0;
}

byte MessageBuf::calculateChecksum()
{
  /* The checksum only applies to the payload contents, not the entire frame */
  byte checksum = 0;
  for(int n = 0; n < payloadLen; n++) {
    checksum += payload[n];
  }
  return ~checksum;
}

void MessageBuf::receiveByte(byte data)
{
  if (complete) {
    /* We're full! Can't accept more data until this buffer is cleared. */
    return;
  }
  lastReceivedTime = millis();

  //  Serial.print("BYTE ");
  //  Serial.println((int)data);

  if (pos <= 1 && data != FRAMING_BYTE) 
  {
    /* We were expecting two framing bytes, but we didn't get them so reset 
     * the buffer */
    clear();
#if DEBUG
    Serial.println("framing bytes not found");
#endif
    return;
  } else if (pos == 2) {
    /* We now have the length of the package (assuming it's correct!) */
    payloadLen = (int)data;
    messageLen = payloadLen + NUM_FRAMING_BYTES;
    /* Validate the message length */
    if (messageLen > MAX_MESSAGE_LEN) {
      clear();
      return;
    }
  } else if (pos == 3) {
    /* Record the destination address */
    destination = (int)data;
  }
  
  buffer[pos++] = data;
  if (messageLen != 0 && pos >= messageLen) {
#if DEBUG
    Serial.print("Complete message: ");
    Serial.print(messageLen);
    Serial.print(" bytes, ");
    Serial.print(payloadLen);
    Serial.print(" payload, ");
    Serial.print(destination);
    Serial.println(" dest");
#endif

    if (calculateChecksum() == buffer[messageLen-1]) {
      /* Checksum success! */
      complete = true;
    } else {
      /* Ignore the error and clear the buffer for the next packet */
#if DEBUG
      Serial.println("checksum failure!");
      Serial.print("calculated == ");
      Serial.println((int)calculateChecksum());
      Serial.print("read == ");
      Serial.println((int)buffer[messageLen-1]);
#endif
      clear();
    }
  }
}

void MessageBuf::update()
{
  if (lastReceivedTime != 0 && millis()-lastReceivedTime > TIMEOUT) {
    /* Timeout while waiting for data */
    clear();
  }
}
