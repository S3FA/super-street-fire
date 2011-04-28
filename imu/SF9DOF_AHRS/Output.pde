
// attempt to get the full dataset on the single serial println 
void printdata(void)
{    
  // hopefully 10 is big enough for any number output?
  char buf1[10];
  char buf2[10];
  char buf3[10];

  dtostrf(ToDeg(roll), 6, 2, buf1);
  dtostrf(ToDeg(pitch), 6, 2, buf2);
  dtostrf(ToDeg(yaw), 6, 2, buf3);

  // "P:roll,pitch,yaw_gyrox,gyroy,gyroz_accelx,accely,accelz_magx,magy,magz_"
  String out = NODE;
  out += buf1;
  out += ",";
  out += buf2;
  out += ",";
  out += buf3;
  
  dtostrf((AN[sensors[0]]-AN_OFFSET[0]), 6, 2, buf1);
  dtostrf((AN[sensors[1]]-AN_OFFSET[1]), 6, 2, buf2);
  dtostrf((AN[sensors[2]]-AN_OFFSET[2]), 6, 2, buf3);

  out += "_";
  out += buf1;
  out += ",";
  out += buf2;
  out += ",";
  out += buf3;

  dtostrf((ACC[0]-AN_OFFSET[3]), 6, 2, buf1);
  dtostrf((ACC[1]-AN_OFFSET[4]), 6, 2, buf2);
  dtostrf((ACC[2]-AN_OFFSET[5]), 6, 2, buf3);

  out += "_";
  out += buf1;
  out += ",";
  out += buf2;
  out += ",";
  out += buf3;

  dtostrf(magnetom_x, 6, 2, buf1);
  dtostrf(magnetom_y, 6, 2, buf2);
  dtostrf(magnetom_z, 6, 2, buf3);

  out += "_";
  out += buf1;
  out += ",";
  out += buf2;
  out += ",";
  out += buf3;
  out += "_";

      Serial.println(out);
}

long convert_to_dec(float x)
{
  return x*10000000;
}



