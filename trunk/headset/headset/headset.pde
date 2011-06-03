#include <NewSoftSerial.h>
#include <Brain.h> 
 
NewSoftSerial serialBrain(3,4); 
 
Brain brain(serialBrain);
 
void setup() {
  Serial.begin(57600);
}
 
void loop() {
  if (brain.update()) {
    Serial.print("1H:");
    Serial.println(brain.readCSV());
  }
}

