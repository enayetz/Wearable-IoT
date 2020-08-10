/*
 * Start Date: November 23 2017
 * End Date: December 12 2017
 * ELE 591: Wearable-Internet of Things (W-IoT)
 * Developed by: Team E-SmartShirt: Muhammad Enayetur Rahman
 * using multiple sensor TMP36 with Arduino UNO and BlueSMiRF to Our Android App: Smartshirt
*/

#include <SoftwareSerial.h>
#include <Arduino.h>

SoftwareSerial BTserial(2, 3); // RX | TX

int tempPin0 = A0;
int tempPin1 = A1;
int tempPin2 = A2;

float voltage, degreesC, degreesF;
float temp1, temp2, temp3, tempAvg;
float tempk[4];

void setup() 
{
  BTserial.begin(115200);  // The Bluetooth Mate defaults to 115200bps
  Serial.begin(9600);
}

void loop()
{ 
  readSensors();
  sendAndroidValues();
  delay(500);
}

void sendAndroidValues()
{
  BTserial.print("{");
  //for loop cycles through 4 sensors and sends values via serial
  for(int k=0; k<4; k++)
  {
    BTserial.print("val");
    BTserial.print(k);
    BTserial.print(":");
    
    BTserial.print(tempk[k]);

    if(k < 3)
    {
       BTserial.print(",");
    }
  }
 BTserial.print("}"); //used as an end of transmission character - used in app for string length
 BTserial.println();
 delay(10);        //added a delay to eliminate missed transmissions
}

float getTemperature(int pin)
{
  voltage = (analogRead(pin) * (5.0/1024));
  delay(10);
  voltage = 0.0;
  voltage = (analogRead(pin) * (5.0/1024));
  delay(10);
  degreesC = (voltage - 0.5) * 100.0;
  degreesF = degreesC * (9.0/5.0) + 32.0;
  return degreesF;
}


void readSensors()
{
  // read the analog in values to the sensor array
  tempk[0] = getTemperature(tempPin0);
  tempk[1] = getTemperature(tempPin1);
  tempk[2] = getTemperature(tempPin2);
  // get the average
  tempk[3] = getAverage(tempk[0], tempk[1], tempk[2]);
}

float getAverage(float temp1, float temp2, float temp3)
{  
  float tempAvg = 0.0;
  tempAvg = (temp1 + temp2 + temp3) / 3;
  Serial.println(tempAvg);
  return tempAvg;
}
