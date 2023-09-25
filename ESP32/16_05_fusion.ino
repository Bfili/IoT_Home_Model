#include <WebServer.h>
#include <Adafruit_BMP280.h>
#include <Time.h>
#include <SPI.h>
#include <MFRC522.h>

/*
  --- Server Setup ---
*/
// Replace with proper network credentials
const char* ssid = "ssid";
const char* password = "password";

WebServer server(80);

/*
  --- HC-SR04 Setup ---
*/
const uint8_t trigPin = 12;
const uint8_t echoPin = 14;
const uint8_t lampPin = 16;

bool lampProximityFlag = false;

//define sound speed in cm/uS
#define SOUND_SPEED 0.034

long duration = 0;
float distanceCm = 0;
float timestamp = 0;
clock_t startTime, elapsedTime;

/*
  --- BMP280 Setup ---
*/
Adafruit_BMP280 bmp;
float temperature = 0.0;
float temperatureSetpoint = 27.5; //arbitrary value
float temperatureHisteresysWidth = 0.5;
char stringTemperatureValue[20];
uint8_t heaterEnablePin = 27;

/*
  --- RC522 Setup ---
*/
#define DOOR_OPEN_DURATION 5000
const byte UID[] = {0x43, 0x83, 0xF4, 0x99}; //UID value of given RFID tag

MFRC522 rfid(5, 4);
MFRC522::MIFARE_Key key;
boolean isTheRFIDTagCorrect = false;
unsigned long doorOpenedDuration;

const uint8_t greenLedPin = 17;
const uint8_t redLedPin = 13;

/*
  --- Misc Setup ---
*/
bool lampHTTPFlag = false;

/*
  --- Server HTTP request handler functions ---
*/
void handleGetTemperature()
{
  snprintf(stringTemperatureValue, sizeof(stringTemperatureValue), "%f", temperature);
  server.send(200, "text/plain", stringTemperatureValue);
}

void handleLampBlink()
{
  String lampStateReceived = server.arg("state");

  if(lampStateReceived == "1")
  {
    lampHTTPFlag = true;
  }
  else if(lampStateReceived == "0")
  {
    lampHTTPFlag = false;
  }
  else;

  if(lampHTTPFlag && !lampProximityFlag) 
  {
    server.send(200, "text/plain", "on");
  }
  else if (!lampHTTPFlag && !lampProximityFlag)
  {
    server.send(200, "text/plain", "off");
  }
  else if (lampProximityFlag)
  {
    server.send(200, "text/plain", "corr");
  }
}

void handleTemperatureSetpointChange() 
{
  char stringSetpointChangeValue[5];
  server.arg("temperature").toCharArray(stringSetpointChangeValue, sizeof(stringSetpointChangeValue));
  temperatureSetpoint = atof(stringSetpointChangeValue);
  server.send(200, "text/plain", "Received new Setpoint: " + String(temperatureSetpoint));
}

void handleDoorStatus() {
  int message = 0;
  if(isTheRFIDTagCorrect){
    message = 1;
  }
  else if(isTheRFIDTagCorrect){
    message = 0;
  }
  server.send(200, "text/plain", String(message));
}

/*
  --- void Setup ---
*/
void setup() {
  Serial.begin(115200);

  pinMode(trigPin, OUTPUT); // Sets the trigPin as an Output
  pinMode(echoPin, INPUT); // Sets the echoPin as an Input

  pinMode(lampPin, OUTPUT);
  pinMode(redLedPin, OUTPUT);


  /* --- RC522 Setup cont'd --- */
  SPI.begin();
  rfid.PCD_Init();

  pinMode(greenLedPin, OUTPUT);

  /* --- BMP280 Setup cont'd --- */
  while ( !Serial ) delay(100);   // wait for native usb
  unsigned status;
  status = bmp.begin(0x76);

  // Default settings from datasheet
  bmp.setSampling(Adafruit_BMP280::MODE_NORMAL,     // Operating Mode
                  Adafruit_BMP280::SAMPLING_X2,     // Temp. oversampling
                  Adafruit_BMP280::SAMPLING_X16,    // Pressure oversampling
                  Adafruit_BMP280::FILTER_X16,      // Filtering
                  Adafruit_BMP280::STANDBY_MS_500); // Standby time

  pinMode(heaterEnablePin, OUTPUT);
  pinMode(2, OUTPUT);

  /* --- Server Setup cont'd --- */
  // Connect to Wi-Fi network with SSID and password
  WiFi.begin(ssid, password);
  while (WiFi.status() != WL_CONNECTED) 
  {
    delay(1000);
    Serial.println("Connecting to WiFi...");
  }
  
  // Print local IP address and start the server
  Serial.println(WiFi.localIP());

  server.on("/getTemperature", handleGetTemperature);
  server.on("/lampBlink", handleLampBlink);
  server.on("/changeSetpoint", handleTemperatureSetpointChange);
  server.on("/isDoorOpen", handleDoorStatus);

  server.begin();
}

/*
  --- void Loop ---
*/

void loop() {

  // Read temperature from BMP280
  temperature = bmp.readTemperature(); 
  if(temperature < temperatureSetpoint - temperatureHisteresysWidth)
  {
    digitalWrite(heaterEnablePin, HIGH);
    digitalWrite(2, LOW);
  }
  else if(temperature > temperatureSetpoint + temperatureHisteresysWidth)
  {
    digitalWrite(heaterEnablePin, LOW);
    digitalWrite(2, HIGH);
    
  }

  // Clears the trigPin
  digitalWrite(trigPin, LOW);
  delayMicroseconds(2);
  // Sets the trigPin on HIGH state for 10 micro seconds
  digitalWrite(trigPin, HIGH);
  delayMicroseconds(10);
  digitalWrite(trigPin, LOW);
  
  // Reads the echoPin, returns the sound wave travel time in microseconds
  duration = pulseIn(echoPin, HIGH);
  
  // Calculate the distance
  distanceCm = duration * SOUND_SPEED/2;

  // Turn lamp on if something is in front of HC-SR04
  if(distanceCm < 5.5 && distanceCm != 0.00)
  {
    lampProximityFlag = true;
    digitalWrite(lampPin, HIGH);
    startTime = clock();
    timestamp = 0;
  }
  else
  {
    elapsedTime = clock() - startTime;
    timestamp = ((double)elapsedTime) / CLOCKS_PER_SEC;  
     
    if(timestamp >= 0.01)
    {
      digitalWrite(lampPin, LOW);
      lampProximityFlag = false;
    }
  }

  // Toggle lamp by HTTP GET request
  if(lampHTTPFlag && !lampProximityFlag)
  {
    digitalWrite(lampPin, lampHTTPFlag);
  }
  else if(!lampHTTPFlag && !lampProximityFlag)
  {
    digitalWrite(lampPin, lampHTTPFlag);
  }

  // Check if there is RFID in front of reader and if it's the correct one
  if (rfid.PICC_IsNewCardPresent() && rfid.PICC_ReadCardSerial())
  {
    if (rfid.uid.uidByte[0] == UID[0] && 
        rfid.uid.uidByte[1] == UID[1] &&
        rfid.uid.uidByte[2] == UID[2] &&
        rfid.uid.uidByte[3] == UID[3])
    {
      isTheRFIDTagCorrect = true;
      doorOpenedDuration = millis() + DOOR_OPEN_DURATION;
    } else
    {
      isTheRFIDTagCorrect = false;
    }
    rfid.PICC_HaltA();
    rfid.PCD_StopCrypto1();
  }

  if (isTheRFIDTagCorrect && doorOpenedDuration < millis())
  isTheRFIDTagCorrect = false;
  digitalWrite(greenLedPin, isTheRFIDTagCorrect);
  digitalWrite(redLedPin, !isTheRFIDTagCorrect);

  server.handleClient();
}
