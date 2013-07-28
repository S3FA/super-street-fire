/* 
 * Heartbeat.cpp - Peter Rogers 2013 
 */
#include "SSF_Heartbeat.h"

Heartbeat::Heartbeat(int p)
{
  pin = p;
  period = 1000;
  previousTime = 0;
}

void Heartbeat::update()
{
  uint32_t tm = millis();

  if (tm - previousTime > period/2) {
    previousTime = tm;
    digitalWrite(pin, !digitalRead(pin));
  }  
}
