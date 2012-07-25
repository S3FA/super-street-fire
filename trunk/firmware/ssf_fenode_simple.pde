#define SOL1 3
#define SOL2 5
#define SOL3 6
#define SOL4 9
#define ARMED 4
#define HSI 7
#define DE 10
#define RE 8
#define IRLED 14

#define NODE 12
#define BROADCAST 0xFF
#define FRAME 0xAA

#define MAXSIZE 0x0B

// DEBUG 1 = debug serial output w/USB cable; DEBUG 0 = serial output over RS-485
#define DEBUG 0


void setup() {
  
  pinMode(SOL1, OUTPUT);
  digitalWrite(SOL1, LOW);
  pinMode(SOL2, OUTPUT);
  digitalWrite(SOL2, LOW);
  pinMode(SOL3, OUTPUT);
  digitalWrite(SOL3, LOW);
  pinMode(SOL4, OUTPUT);
  digitalWrite(SOL4, LOW);
  pinMode(HSI, OUTPUT);
  digitalWrite(HSI, LOW);
  pinMode(RE, OUTPUT); // RE receive active low
  if( DEBUG ) {
    digitalWrite(RE, HIGH);
  }
  else {
    digitalWrite(RE, LOW);
  }
  pinMode(DE, OUTPUT); // DE transmit active high
  digitalWrite(DE, LOW);
  
  digitalWrite(ARMED, HIGH);

  Serial.begin(57600);
  
  if( DEBUG ) {
    Serial.println("starting");
  }

}

byte c;
byte param;
int csum;
byte length;
byte i;
byte cmd[MAXSIZE];

void loop() {
  
  if(Serial.available() > 0) {
    c = Serial.read(); // framing byte 1
    if(c == FRAME) {
      do {
      } while( Serial.available() <= 0 );
      c = Serial.read(); // framing byte 2
      if(c == FRAME) {
        do {
        } while( Serial.available() <= 0);
        length = Serial.read(); // length
        if( (length > 0) && (length <= MAXSIZE) ) {
          do {
          } while( Serial.available() < length);
          for(i=0;i<length;i++) {
            cmd[i] = Serial.read();
          }
          for(;i<MAXSIZE;i++) {
            cmd[i] = 0;
          }
          do {
          } while( Serial.available() <= 0);
          csum = Serial.read();
          if( (cmd[0] == NODE) || (cmd[0] == BROADCAST) ) {
            if( DEBUG ) {
              //Serial.println("listening to message for me");
            }
            i = 0;
            //Serial.print(" |");
            for(c=0;c<length;c++) {
              i += cmd[c];
              //Serial.print(" ");
              //Serial.print(cmd[c], HEX);
              //Serial.print(" ");
            }
            //Serial.print("| ");
            c = ~i;
            if( (c != csum) && DEBUG ) {
              Serial.print("bad checksum, received ");
              Serial.print(csum);
              Serial.print(" expected ");
              Serial.println(c);
            }
            if(c == csum) {
              if( DEBUG ) {
                Serial.println("checksum ok ");
              }
            
              c = 1;
              while(c < length) {
                switch(cmd[c]) {
                  case '1' : 
                    c += 1;
                    if( c < length ) {
                      if( cmd[c] == 0 ) {
                        digitalWrite(SOL1, LOW);
                        if( DEBUG ) { Serial.println("effect 1 off"); }
                      }
                      else {
                        digitalWrite(SOL1, HIGH);
                        if( DEBUG ) { Serial.println("effect 1 on"); }
                      }
                      c += 1;
                    }
                    break;
                  case '2' :
                    c += 1;
                    if( c < length ) {
                      if( cmd[c] == 0 ) {
                        digitalWrite(SOL2, LOW);
                        if( DEBUG ) { Serial.println("effect 2 off"); }
                      }
                      else {
                        digitalWrite(SOL2, HIGH);
                        if( DEBUG ) { Serial.println("effect 2 on"); }
                      }
                      c += 1;
                    }
                    break;
                  case '3' :
                    c += 1;
                    if( c < length ) {
                      if( cmd[c] == 0 ) {
                        digitalWrite(SOL3, LOW);
                        if( DEBUG ) { Serial.println("effect 3 off"); }
                      }
                      else {
                        digitalWrite(SOL3, HIGH);
                        if( DEBUG ) { Serial.println("effect 3 on"); }
                      }
                      c += 1;
                    }
                    break;
                case '4' : 
                  c += 1;
                    if( c < length ) {
                      if( cmd[c] == 0 ) {
                        digitalWrite(SOL4, LOW);
                        if( DEBUG ) { Serial.println("effect 4 off"); }
                      }
                      else {
                        digitalWrite(SOL4, HIGH);
                        if( DEBUG ) { Serial.println("effect 4 on"); }
                      }
                      c += 1;
                    }
                    break;
                case 'A' :
                  c += 1;
                  if( c < length ) {
                    if(cmd[c] == '1') {
                      digitalWrite(HSI, HIGH);
                      if( DEBUG ) { Serial.println("hsi on"); }
                    }
                    else {
                      digitalWrite(HSI, LOW);
                      if( DEBUG ) { Serial.println("hsi off"); }
                    }
                    c += 1;
                  }
                  break;
                case 'E' : 
                  c += 1;
                  digitalWrite(SOL1, LOW);
                  digitalWrite(SOL2, LOW);
                  digitalWrite(SOL3, LOW);
                  digitalWrite(SOL4, LOW);
                  digitalWrite(HSI, LOW);
                  if( DEBUG ) { Serial.println("emergency stop"); }
                  break;
                case '?' :
                  c += 1;
                  if( !DEBUG ) {
                    digitalWrite(DE, HIGH);
                    digitalWrite(RE, HIGH);
                  }
                  Serial.print(0xAA, BYTE);
                  Serial.print(0xAA, BYTE);
                  Serial.print(6, BYTE);
                  Serial.print(0, BYTE);
                  Serial.print(NODE, BYTE);
                  Serial.print('S');
                  csum = NODE + 0x53;
                  if(digitalRead(ARMED) == HIGH) { 
                    Serial.print('0');
                    csum += 0x30;
                  }
                  else {
                    Serial.print('1');
                    csum += 0x31;
                  }
                  Serial.print('F');
                  csum += 0x46;
                  Serial.print(0x3F, BYTE);
                  csum += 0x3F;
                  csum = csum % 256;
                  c = ~csum;
                  Serial.print(c, BYTE);
                  Serial.flush();
                  delay(5);
                  if( !DEBUG ) {
                    digitalWrite(DE, LOW);
                    digitalWrite(RE, LOW);
                  }
   
                }  
              }
            }
          }
        }
      }
    }
  }
}
