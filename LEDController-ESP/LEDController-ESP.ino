/**
 * Program to accompany LED Controller mobile app for controlling individually addressable LED displays.
 * Helpful information:
 * 
 * -A "frame" is an array of size [numLights] of Colors. frame[i] dictates the color of LED i in that frame. 
 * -Frames should be displayed at regular intervals.
 * -The mobile app sends one whole frame in a continuous stream every time the "proceed" byte is received. This means that a frame will be sent as r,g,b,r,g,b,r,g,b,etc.
 * -ESP should display a frame, save the time in millis, overwrite the displayed frame with the next frame from SerialBT, 
 *     wait until [millisBtwnFrames] after saved time, then repeat.
 */

#include <FastLED.h>
#include "BluetoothSerial.h"

#if !defined(CONFIG_BT_ENABLED) || !defined(CONFIG_BLUEDROID_ENABLED)
#error Bluetooth is not enabled! Please run `make menuconfig` to and enable it
#endif

#define ONBOARD_LED 2

//FastLED things
#define DATA_PIN    27
#define LED_TYPE    WS2812
#define COLOR_ORDER GRB
#define BRIGHTNESS  96
#define FRAMES_PER_SECOND  120
CRGB* leds;

const byte READY_BYTE = 32;
BluetoothSerial SerialBT;
long millisOfLast;
long millisSinceLast;
int numLights = 0;
int millisBtwnFrames = 0;

void callback(esp_spp_cb_event_t event, esp_spp_cb_param_t *param){
  if(event == ESP_SPP_SRV_OPEN_EVT){
    Serial.println("Client Connected!");
  }
 
  if(event == ESP_SPP_CLOSE_EVT ){
    Serial.println("Client disconnected, restarting.");
    FastLED.clear();
    FastLED.show();
    ESP.restart();
  }
}

void setup() {
  Serial.begin(115200);
  pinMode(ONBOARD_LED, OUTPUT);
  SerialBT.begin("ESP32test"); //Bluetooth device name
  SerialBT.register_callback(callback);
  Serial.println("Waiting for bluetooth connection...");
  
  //Wait to receive the number of lights (4bytes) in the display (aka the number of Ints in each frame)
  while(!SerialBT.available());
  numLights = SerialBT.read() << 24;
  while(!SerialBT.available());
  numLights += SerialBT.read() << 16;
  while(!SerialBT.available());
  numLights += SerialBT.read() << 8;
  while(!SerialBT.available());
  numLights += SerialBT.read();
  Serial.print("Num lights: ");
  Serial.println(numLights);

  delay(200);

  //Wait to receive the refresh rate (4bytes) in the display (aka the number of millis between frames)
  while(!SerialBT.available());
  millisBtwnFrames = SerialBT.read() << 24;
  while(!SerialBT.available());
  millisBtwnFrames += SerialBT.read() << 16;
  while(!SerialBT.available());
  millisBtwnFrames += SerialBT.read() << 8;
  while(!SerialBT.available());
  millisBtwnFrames += SerialBT.read();
  Serial.print("Millis btwn frames: ");
  Serial.println(millisBtwnFrames);

  //Allocate space for leds
  leds = (CRGB*)calloc(numLights, sizeof(CRGB));

  //Tell FastLED about the LED strip configuration
  FastLED.addLeds<LED_TYPE,DATA_PIN,COLOR_ORDER>(leds, numLights).setCorrection(TypicalLEDStrip);
  delay(50);

  //Flash lights (LEDs and onboard LED)
  digitalWrite(ONBOARD_LED, HIGH);
  fill_solid(leds, numLights, CRGB::Red);
  FastLED.show();
  delay(300);
  fill_solid(leds, numLights, CRGB::Green);
  FastLED.show();
  delay(300);
  fill_solid(leds, numLights, CRGB::Blue);
  FastLED.show();
  delay(300);
  digitalWrite(ONBOARD_LED, LOW);
  FastLED.clear();
  FastLED.show();
  
  Serial.println("End setup");
}


void loop() {
  //Read in the new frame
  SerialBT.write(READY_BYTE);
  readFrame();
  
  //Wait (block) until it is time to display the new frame
  while(millis() - millisOfLast < millisBtwnFrames);

  //Activate the onboard LED if this frame was delayed, with a 2ms tolerance
  digitalWrite(ONBOARD_LED, LOW);
  if(millis() -  millisOfLast > (millisBtwnFrames + 2)){
    digitalWrite(ONBOARD_LED, HIGH);
    Serial.println(millis() - millisOfLast);
  }


  //Display the new frame
  millisOfLast = millis();
  FastLED.show();
}


/**
 * Overwrite the frame array with new data from serial.
 * This function does not exit until the entire frame has been read.
 */
void readFrame(){
  int lightNum = 0;
  int numBytesRead = 0;
  while(lightNum < numLights){
    if(SerialBT.available()){
      if(numBytesRead % 3 == 0){
        leds[lightNum].r = SerialBT.read();
      } else if(numBytesRead % 3 == 1){
        leds[lightNum].g = SerialBT.read();
      } else if(numBytesRead % 3 == 2){
        leds[lightNum].b = SerialBT.read();
        lightNum++;
      }
      numBytesRead++;
    }
  }
}
