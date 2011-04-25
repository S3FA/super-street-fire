
void printdata(void)
{    
      Serial.print(NODE);
      Serial.print(":");

// "P:roll,pitch,yaw;gyrox,gyroy,gyroz;accelx,accely,accelz;magx,magy,magz"
      #if PRINT_EULER == 1
      Serial.print(ToDeg(roll));
      Serial.print(",");
      Serial.print(ToDeg(pitch));
      Serial.print(",");
      Serial.print(ToDeg(yaw));
      #endif      
      #if PRINT_ANALOGS==1
      Serial.print("_"); 
      Serial.print((AN[sensors[0]]-AN_OFFSET[0]));  //(int)read_adc(0)
      Serial.print(",");
      Serial.print(AN[sensors[1]]-AN_OFFSET[1]); // gyros
      Serial.print(",");
      Serial.print(AN[sensors[2]]-AN_OFFSET[2]);  
      Serial.print("_");
      Serial.print((ACC[0]-AN_OFFSET[3]));
      Serial.print (",");
      Serial.print((ACC[1]-AN_OFFSET[4]));
      Serial.print (",");
      Serial.print((ACC[2]-AN_OFFSET[5]));
      Serial.print("_");
      Serial.print(magnetom_x);
      Serial.print (",");
      Serial.print(magnetom_y);
      Serial.print (",");
      Serial.print(magnetom_z);  
      #endif
      /*#if PRINT_DCM == 1
      Serial.print (",DCM:");
      Serial.print(convert_to_dec(DCM_Matrix[0][0]));
      Serial.print (",");
      Serial.print(convert_to_dec(DCM_Matrix[0][1]));
      Serial.print (",");
      Serial.print(convert_to_dec(DCM_Matrix[0][2]));
      Serial.print (",");
      Serial.print(convert_to_dec(DCM_Matrix[1][0]));
      Serial.print (",");
      Serial.print(convert_to_dec(DCM_Matrix[1][1]));
      Serial.print (",");
      Serial.print(convert_to_dec(DCM_Matrix[1][2]));
      Serial.print (",");
      Serial.print(convert_to_dec(DCM_Matrix[2][0]));
      Serial.print (",");
      Serial.print(convert_to_dec(DCM_Matrix[2][1]));
      Serial.print (",");
      Serial.print(convert_to_dec(DCM_Matrix[2][2]));
      #endif*/
      Serial.println();    
      
}

long convert_to_dec(float x)
{
  return x*10000000;
}

