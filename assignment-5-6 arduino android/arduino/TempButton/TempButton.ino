 /*********************************************************************
 This is an example for our nRF51822 based Bluefruit LE modules

 Pick one up today in the adafruit shop!

 Adafruit invests time and resources providing this open source code,
 please support Adafruit and open-source hardware by purchasing
 products from Adafruit!

 MIT license, check LICENSE for more information
 All text above, and the splash screen below must be included in
 any redistribution
*********************************************************************/

/*
    Please note the long strings of data sent mean the *RTS* pin is
    required with UART to slow down data sent to the Bluefruit LE!
*/

#include <Arduino.h>
#include <SPI.h>
#include "Adafruit_BLE.h"
#include "Adafruit_BluefruitLE_SPI.h"
#include "Adafruit_BluefruitLE_UART.h"

//For temperature measurement:
#include <Adafruit_CircuitPlayground.h>

//For Button:
#include "Button.h"
Button buttonR(4);
Button buttonL(19);
unsigned long previousMillis = 0;

#include "BluefruitConfig.h"

#if SOFTWARE_SERIAL_AVAILABLE
  #include <SoftwareSerial.h>
#endif

// Create the bluefruit object, either software serial...uncomment these lines
/*
SoftwareSerial bluefruitSS = SoftwareSerial(BLUEFRUIT_SWUART_TXD_PIN, BLUEFRUIT_SWUART_RXD_PIN);

Adafruit_BluefruitLE_UART ble(bluefruitSS, BLUEFRUIT_UART_MODE_PIN,
                      BLUEFRUIT_UART_CTS_PIN, BLUEFRUIT_UART_RTS_PIN);
*/

/* ...or hardware serial, which does not need the RTS/CTS pins. Uncomment this line */
Adafruit_BluefruitLE_UART ble(Serial1, BLUEFRUIT_UART_MODE_PIN);

/* ...hardware SPI, using SCK/MOSI/MISO hardware SPI pins and then user selected CS/IRQ/RST */
//Adafruit_BluefruitLE_SPI ble(BLUEFRUIT_SPI_CS, BLUEFRUIT_SPI_IRQ, BLUEFRUIT_SPI_RST);

/* ...software SPI, using SCK/MOSI/MISO user-defined SPI pins and then user selected CS/IRQ/RST */
//Adafruit_BluefruitLE_SPI ble(BLUEFRUIT_SPI_SCK, BLUEFRUIT_SPI_MISO,
//                             BLUEFRUIT_SPI_MOSI, BLUEFRUIT_SPI_CS,
//                             BLUEFRUIT_SPI_IRQ, BLUEFRUIT_SPI_RST);


// A small helper
void error(const __FlashStringHelper*err) {
  Serial.println(err);
  while (1);
}

//Temperature variables:
float tempC, tempF;

//Button Variables:
bool leftButtonPressed;
bool rightButtonPressed;

/* The service information */
int32_t hrmServiceId;
int32_t hrmMeasureCharId;
int32_t hrmLocationCharId;
/**************************************************************************/
/*!
    @brief  Sets up the HW an the BLE module (this function is called
            automatically on startup)
*/
/**************************************************************************/
void setup(void)
{

  //Button:
  buttonR.on_press(on_pressR);
  buttonL.on_press(on_pressL);

  //Circuit Playground begin:
  CircuitPlayground.begin();

  while (!Serial); // required for Flora & Micro
  delay(500);

  boolean success;

  Serial.begin(115200);
  Serial.println(F("Adafruit Bluefruit Heart Rate Monitor (HRM) Example"));
  Serial.println(F("---------------------------------------------------"));

  randomSeed(micros());

  /* Initialise the module */
  Serial.print(F("Initialising the Bluefruit LE module: "));

  if ( !ble.begin(VERBOSE_MODE) )
  {
    error(F("Couldn't find Bluefruit, make sure it's in CoMmanD mode & check wiring?"));
  }
  Serial.println( F("OK!") );

  /* Perform a factory reset to make sure everything is in a known state */
  Serial.println(F("Performing a factory reset: "));
  if (! ble.factoryReset() ){
       error(F("Couldn't factory reset"));
  }

  /* Disable command echo from Bluefruit */
  ble.echo(false);

  Serial.println("Requesting Bluefruit info:");
  /* Print Bluefruit information */
  ble.info();

  // this line is particularly required for Flora, but is a good idea
  // anyways for the super long lines ahead!
  ble.setInterCharWriteDelay(5); // 5 ms

  /* Change the device name to make it easier to find */
  Serial.println(F("Setting device name to 'UW Thermo-Clicker': "));

  if (! ble.sendCommandCheckOK(F("AT+GAPDEVNAME=UW Thermo-Clicker")) ) {
    error(F("Could not set device name?"));
  }

  /* Add the Heart Rate Service definition */
  /* Service ID should be 1 */
  // Serial.println(F("Adding the Heart Rate Service definition (UUID = 0x180D): "));
  // success = ble.sendCommandWithIntReply( F("AT+GATTADDSERVICE=UUID=0x180D"), &hrmServiceId);
  // if (! success) {
  //   error(F("Could not add HRM service"));
  // }

  /* Add the Temperature Service definition */
  /* Service ID should be 1 */
  Serial.println(F("Adding the Temperature Service definition (UUID = 0x180E): "));
  success = ble.sendCommandWithIntReply( F("AT+GATTADDSERVICE=UUID=0x180E"), &hrmServiceId);
  if (! success) {
    error(F("Could not add Temperature service"));
  }

  /* Add the Heart Rate Measurement characteristic */
  /* Chars ID for Measurement should be 1 */
  // Serial.println(F("Adding the Heart Rate Measurement characteristic (UUID = 0x2A37): "));
  // success = ble.sendCommandWithIntReply( F("AT+GATTADDCHAR=UUID=0x2A37, PROPERTIES=0x10, MIN_LEN=2, MAX_LEN=3, VALUE=00.00-40.00"), &hrmMeasureCharId);
  //   if (! success) {
  //   error(F("Could not add HRM characteristic"));
  // }

  /* Add the Temperature Measurement characteristic */
  /* Chars ID for Measurement should be 1 */
  Serial.println(F("Adding the Temperature Measurement characteristic (UUID = 0x2B37): "));
  success = ble.sendCommandWithIntReply( F("AT+GATTADDCHAR=UUID=0x2B37, PROPERTIES=0x10, MIN_LEN=2, MAX_LEN=3, VALUE=00-40"), &hrmMeasureCharId);
    if (! success) {
    error(F("Could not add Temperature characteristic"));
  }

  /* Add the Body Sensor Location characteristic */
  /* Chars ID for Body should be 2 */
  // Serial.println(F("Adding the Body Sensor Location characteristic (UUID = 0x2A38): "));
  // success = ble.sendCommandWithIntReply( F("AT+GATTADDCHAR=UUID=0x2A38, PROPERTIES=0x02, MIN_LEN=1, VALUE=3"), &hrmLocationCharId);
  //   if (! success) {
  //   error(F("Could not add BSL characteristic"));
  // }
  Serial.println(F("Adding the Body Sensor Location characteristic (UUID = 0x2A38): "));
  success = ble.sendCommandWithIntReply( F("AT+GATTADDCHAR=UUID=0x2A38, PROPERTIES=0x10, MIN_LEN=2, MAX_LEN=20"), &hrmLocationCharId);
    if (! success) {
    error(F("Could not add BSL characteristic"));
  }

  /* Add the Heart Rate Service to the advertising data (needed for Nordic apps to detect the service) */
  Serial.print(F("Adding Heart Rate Service UUID to the advertising payload: "));
  ble.sendCommandCheckOK( F("AT+GAPSETADVDATA=02-01-06-05-02-0d-18-0a-18") );

  /* Reset the device for the new service setting changes to take effect */
  Serial.print(F("Performing a SW reset (service changes require a reset): "));
  ble.reset();

  Serial.println();
}

/** Send randomized heart rate data continuously **/
void loop(void)
{

//Button:
  buttonR.init();
  buttonL.init();
//  int heart_rate = random(50, 100);
//  tempC = CircuitPlayground.temperature();
//  int temp = tempC;
  temp_read(5000);
  
//  Serial.print(F("Updating HRM value to "));
//  Serial.print(heart_rate);
//  Serial.println(F(" BPM"));
  // Serial.print("tempC: ");
  // Serial.print(temp);

  /* Command is sent when \n (\r) or println is called */
  /* AT+GATTCHAR=CharacteristicID,value */
//  ble.print( F("AT+GATTCHAR=") );
//  ble.print( hrmLocationCharId );
//  ble.print( F(",00-") );
//  ble.println(69, HEX);



  //  ble.print( F("AT+GATTCHAR=") );
  //  ble.print( hrmMeasureCharId );
  //  ble.print( F(",00-") );
  //  ble.println(temp, HEX);


  /* Check if command executed OK */
  if ( !ble.waitForOK() )
  {
    Serial.println(F("Failed to get response!"));
  }

  /* Delay before next measurement update */
//  delay(1000);
}
void on_pressR() {
  Serial.println("Right Button Pressed");
  int r = 82;
  ble.print( F("AT+GATTCHAR=") );
  ble.print( hrmLocationCharId );
  ble.print( F(",00-") );
  ble.println(r, HEX);
}
void on_pressL() {
  Serial.println("Left Button Pressed");
  int l = 76;
  ble.print( F("AT+GATTCHAR=") );
  ble.print( hrmLocationCharId );
  ble.print( F(",00-") );
  ble.println(l, HEX);
}
void temp_read(const long interval) {
  unsigned long currentMillis = millis();
  if (currentMillis - previousMillis >= interval) {
    // save the last time you blinked the LED
    previousMillis = currentMillis;

    // // if the LED is off turn it on and vice-versa:
    // if (ledState == LOW) {
    //   ledState = HIGH;
    // } else {
    //   ledState = LOW;
    // }

    // // set the LED with the ledState of the variable:
    // digitalWrite(LED_PIN4, ledState);
    tempC = CircuitPlayground.temperature();
    int temp = tempC;
    Serial.print("tempC: ");
    Serial.print(temp);

    ble.print( F("AT+GATTCHAR=") );
    ble.print( hrmMeasureCharId );
    ble.print( F(",00-") );
    ble.println(temp, HEX);
  }
}
