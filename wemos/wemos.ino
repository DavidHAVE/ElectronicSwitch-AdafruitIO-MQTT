/***************************************************
  Adafruit MQTT Library ESP8266 Example

  Must use ESP8266 Arduino from:
    https://github.com/esp8266/Arduino

  Works great with Adafruit's Huzzah ESP board & Feather
  ----> https://www.adafruit.com/product/2471
  ----> https://www.adafruit.com/products/2821

  Adafruit invests time and resources providing this open source code,
  please support Adafruit and open-source hardware by purchasing
  products from Adafruit!

  Written by Tony DiCola for Adafruit Industries.
  MIT license, all text above must be included in any redistribution
 ****************************************************/

#include <ESP8266WiFi.h>
#include "Adafruit_MQTT.h"
#include "Adafruit_MQTT_Client.h"

/************************* WiFi Access Point *********************************/

#define WLAN_SSID       "HOME"
#define WLAN_PASS       "1234h0me"
#define AIO_SERVER      "io.adafruit.com"
#define AIO_SERVERPORT  1883                                    // use 8883 for SSL
#define AIO_USERNAME    "David0101"
#define AIO_KEY         "2f88f84f5fe84e938631fad0cbb25fdc"

/************ Global State (you don't need to change this!) ******************/

const int switch1Pin = D1; //Pin D1
const int switch2Pin = D2; //Pin D2
WiFiClient client;
Adafruit_MQTT_Client mqtt(&client, AIO_SERVER, AIO_SERVERPORT, AIO_USERNAME, AIO_KEY);
const char SWITCH1_FEED[] PROGMEM = AIO_USERNAME "/feeds/switch1";
const char SWITCH2_FEED[] PROGMEM = AIO_USERNAME "/feeds/switch2";

/****************************** Feeds ***************************************/

// Setup a feed called 'photocell' for publishing.
// Notice MQTT paths for AIO follow the form: <username>/feeds/<feedname>
//Adafruit_MQTT_Publish photocell = Adafruit_MQTT_Publish(&mqtt, AIO_USERNAME "/feeds/photocell");

// Setup a feed called 'onoff' for subscribing to changes.


Adafruit_MQTT_Subscribe onoffbutton = Adafruit_MQTT_Subscribe(&mqtt, SWITCH1_FEED);
Adafruit_MQTT_Subscribe onoffbutton2 = Adafruit_MQTT_Subscribe(&mqtt, SWITCH2_FEED);

Adafruit_MQTT_Publish publishSwitch1 = Adafruit_MQTT_Publish(&mqtt, SWITCH1_FEED);
Adafruit_MQTT_Publish publishSwitch2 = Adafruit_MQTT_Publish(&mqtt, SWITCH2_FEED);

/*************************** Sketch Code ************************************/

// Bug workaround for Arduino 1.6.6, it seems to need a function declaration
// for some reason (only affects ESP8266, likely an arduino-builder bug).
void MQTT_connect();

void setup() {
  Serial.begin(19200);
  delay(10);

  Serial.println(F("Kontrol Alat Listrik"));

  Serial.println(); Serial.println();
  Serial.print("Connecting to ");
  Serial.println(WLAN_SSID);

  WiFi.begin(WLAN_SSID, WLAN_PASS);
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  Serial.println();

  Serial.println("WiFi connected");
  Serial.println("IP address: "); Serial.println(WiFi.localIP());

  mqtt.subscribe(&onoffbutton);
  mqtt.subscribe(&onoffbutton2);

  pinMode(switch1Pin, OUTPUT);
  pinMode(switch2Pin, OUTPUT);
  digitalWrite(switch1Pin, HIGH);
  digitalWrite(switch2Pin, HIGH);

//  if (! publishSwitch1.publish(0)) {
//    Serial.println(F("Failed Publish Switch 1"));
//  } else {
//    Serial.println(F("OK!"));
//  }
//  if (! publishSwitch2.publish(0)) {
//    Serial.println(F("Failed Publish Switch 2"));
//  } else {
//    Serial.println(F("OK!"));
//  }
}

boolean initialRead = true;

uint32_t x = 0;

void loop() {
  MQTT_connect();
  Adafruit_MQTT_Subscribe *subscription;

  //  if(initialRead){
  //    subscription = mqtt.readSubscription();
  //     if (subscription == &onoffbutton) {
  //      Serial.print(F("Got1: "));
  //      //Membaca data yang masuk
  //      Serial.println((char *)onoffbutton.lastread);
  //      //Jika data yang masuk sama dengan 1
  //      if (!strcmp((char*) onoffbutton.lastread, "1")) {
  //        //Active low logic
  //        digitalWrite(switch1Pin, LOW);
  //      } else {
  //        digitalWrite(switch1Pin, HIGH);
  //      }
  //      //Jika subscription adalah switch 2
  //    } else if (subscription == &onoffbutton2) {
  //      Serial.print(F("Got2: "));
  //      Serial.println((char *)onoffbutton2.lastread);
  //       if (!strcmp((char*) onoffbutton2.lastread, "1")) {
  //        //Active low logic
  //        digitalWrite(switch2Pin, LOW);
  //      } else {
  //        digitalWrite(switch2Pin, HIGH);
  //      }
  //    } else {
  //      //
  //    }
  //    initialRead = false;
  //  }

  while ((subscription = mqtt.readSubscription(5000))) {
    if (subscription == &onoffbutton) {
      Serial.print(F("Got1: "));
      //Membaca data yang masuk
      Serial.println((char *)onoffbutton.lastread);
      //Jika data yang masuk sama dengan 1
      if (!strcmp((char*) onoffbutton.lastread, "1")) {
        //Active low logic
        digitalWrite(switch1Pin, LOW);
      } else {
        digitalWrite(switch1Pin, HIGH);
      }
      //Jika subscription adalah switch 2
    } else if (subscription == &onoffbutton2) {
      Serial.print(F("Got2: "));
      Serial.println((char *)onoffbutton2.lastread);
      if (!strcmp((char*) onoffbutton2.lastread, "1")) {
        //Active low logic
        digitalWrite(switch2Pin, LOW);
      } else {
        digitalWrite(switch2Pin, HIGH);
      }
    } else {
      //
    }
  }
}


void MQTT_connect() {
  int8_t ret;

  // Stop if already connected.
  if (mqtt.connected()) {
    return;
  }

  Serial.print("Connecting to MQTT... ");

  while ((ret = mqtt.connect()) != 0) {
    Serial.println(mqtt.connectErrorString(ret));
    Serial.println("Retrying MQTT connection in 5 seconds...");
    mqtt.disconnect();
    delay(5000);
  }
  Serial.println("MQTT Connected!");
}
