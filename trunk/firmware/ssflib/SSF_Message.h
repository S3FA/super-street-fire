/* MessageBuf.h - Peter Rogers 2013*/
#ifndef __MESSAGE_BUF_H__
#define __MESSAGE_BUF_H__

#include <Arduino.h>

/* Minumum possible packet length - 0xAA 0xAA length dest checksum */
#define MIN_MESSAGE_LEN     5
/* Max payload plus overhead, by spec */
#define MAX_MESSAGE_LEN     15

class MessageBuf {
  public:
    /* The actual bytes of data */
    byte buffer[MAX_MESSAGE_LEN];
    /* The bytes of the payload */
    byte *payload;
    /* Position in the buffer of where to place the next received byte */
    byte pos;
    /* Length of the received message (including header!) */
    int messageLen;
    /* Destination address */
    int destination;
    /* Length of the payload */
    int payloadLen;
    /* Whether a complete message is received in this buffer */
    boolean complete;
    /* The error state (if any) */
    int error;
    /* When the last byte of data was received - used for timeouts. This 
     * is zero if we aren't expecting any data packets. */
    uint32_t lastReceivedTime;
    
    MessageBuf();

    /* Reset the buffer to an empty state */
    void clear();
    /* Call this function whenever you'd like to have the buffer receive 
     * a byte of data */
    void receiveByte(byte b);
    bool checkForTimeout() const;
    /* Calculates and returns the checksum of a completed message */
    byte calculateChecksum();
};

inline bool MessageBuf::checkForTimeout() const {
  return lastReceivedTime != 0 && millis()-lastReceivedTime > TIMEOUT;
}

#endif
