/*

super street fire - flame effect control system

uses three wifire16 boards:
  flame effects - player 1 color - player 2 color
  
the flame effect board has the xbee + mcu, the other boards are chained.

reads 6 bytes to set all channels, writes one byte as a response.

reads:
  2 bytes for fire control:   16[x x x x x x x x][x x x x x x x x]1
  2 bytes for player 1 color: 16[x x x x x x x x][x x x x x x x x]1
  2 bytes for player 2 color: 16[x x x x x x x x][x x x x x x x x]1

writes:
  1 byte for armed status: + armed, - disarmed

*/

#include <SPI.h>

// pin definitions
#define CLEARPIN 4    // master clear for 74HC595 shift registers
#define LATCHPIN 5    // latch for 74HC595 shift registers
#define OEPIN    6    // output enable for 74HC595 shift registers
#define ARMEDPIN 7    // optoisolator connected to load power
#define DATAPIN  11   // data for 74HC595 shift registers
#define CLOCKPIN 13   // clock for 74HC595 shift registers 

#define bitFlip(x,n)  bitRead(x,n) ? bitClear(x,n) : bitSet(x,n)

char c;

// registers with state value
byte r1 = 0, r2 = 0;  // flame effects
byte r3 = 0, r4 = 0;  // player 1 color
byte r5 = 0, r6 = 0;  // player 2 color

// setup
void setup() {
  
  // set all output pins
  SPI.begin(); // handles DATAPIN and CLOCKPIN
  pinMode(LATCHPIN, OUTPUT);
  pinMode(OEPIN, OUTPUT);
  pinMode(CLEARPIN, OUTPUT);

  // make sure no lines go active until data is shifted out
  digitalWrite(CLEARPIN, HIGH);
  digitalWrite(OEPIN, LOW);

  // clear any lines that were left active
  digitalWrite(LATCHPIN, LOW);
  digitalWrite(OEPIN, HIGH);
  c = SPI.transfer(0);
  c = SPI.transfer(0);
  c = SPI.transfer(0);
  c = SPI.transfer(0);
  c = SPI.transfer(0);
  c = SPI.transfer(0);
  digitalWrite(LATCHPIN, HIGH);
  digitalWrite(OEPIN, LOW);
  
  // activate built-in pull-up resistor 
  digitalWrite(ARMEDPIN, HIGH);

  // start the serial communication with the xbee
  Serial.begin(57600);

}


// main loop
void loop() {

  if(Serial.available() >= 6) {
    r2 = Serial.read();
    r1 = Serial.read();
    r4 = Serial.read();
    r3 = Serial.read();
    r6 = Serial.read();
    r5 = Serial.read();
    digitalWrite(LATCHPIN, LOW);
    digitalWrite(OEPIN, HIGH);
    c = SPI.transfer(r5);
    c = SPI.transfer(r6);
    c = SPI.transfer(r3);
    c = SPI.transfer(r4);
    c = SPI.transfer(r1);
    c = SPI.transfer(r2);
    digitalWrite(LATCHPIN, HIGH);
    digitalWrite(OEPIN, LOW);
    
    // send back armed state as confirmation
    // load power on = low, off = high
    digitalRead(ARMEDPIN) ? Serial.print("-") : Serial.print("+");
  }
  
}


