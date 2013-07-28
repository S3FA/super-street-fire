/* 
 * SSF_Heartbeat.h 
 * Generates a simple LED heartbeat 
 */
#ifndef __HEARTBEAT_H__
#define __HEARTBEAT_H__

#include <Arduino.h>

class Heartbeat
{
 public:
  int pin;
  int period;
  uint32_t previousTime;

  Heartbeat(int pin=13);
  void update();
};

#endif
