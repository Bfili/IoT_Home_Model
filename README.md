# IoT Home Model

This repository consists of code used in my project of "Smart Home". It has two main parts:
- ESP32 folder containing code wrote in C, which was uploaded on ESP32 with WiFi chip,
- Android folder containing code wrote in Android Studio (mainly in Kotlin language) with all the necessary parts (gradle and layout xml files).
Communication between ESP32 and Android App is maintained via HTTP methods.

## ESP32
ESP32 code is focused on managing Smart Home activity via GPIO ports (lighting, RFID access check, control temperature (it is implemented as a simple two-position controller, which is sufficient for this kind of project and this kind of physical system (PID might've been an overkill)) as well as communicating with local WiFi to exchange data with Android application. 
Whole section is thoroughly commented to make code more readable (I hope it is for others).

Other than ESP32, there were several peripherals used:
- temperature sensor BMP280,
- ultrasound distance sensor HC-SR04 (used as a presence sensor),
- RC522 RFID module.

## Android App
Android app is able to connect to ESP32 through its IP adress and port and exchange data. In it user is able to:
- change IP and port,
- switch the light on/off,
- look up current temperature (via GraphView) and change temperature setpoint,
- check if door is opened (is the correct RFID tag put to reader).

In the app not all strings are extracted. It is done this way because it would be purely cosmetic and wouldn't boost this project's functionality in meaningful way.
