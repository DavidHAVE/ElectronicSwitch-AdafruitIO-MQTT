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

//Library ESP8266 untuk koneksi ke Access Point
#include <ESP8266WiFi.h>
//Library untuk koneksi MQTT
#include "Adafruit_MQTT.h"
#include "Adafruit_MQTT_Client.h"

/************************* WiFi Access Point *********************************/

#define WLAN_SSID       "HOME"
#define WLAN_PASS       "1234h0me"
//#define WLAN_SSID       "BENTO KOPI 2"
//#define WLAN_PASS       "kopi hitam"

/************************* Adafruit.io Setup *********************************/

#define AIO_SERVER      "io.adafruit.com"
#define AIO_SERVERPORT  1883                   // use 8883 for SSL
#define AIO_USERNAME    "David0101"
#define AIO_KEY         "2f88f84f5fe84e938631fad0cbb25fdc"

/************ Global State (you don't need to change this!) ******************/

const int switch1Pin = D3; //Pin D1
const int switch2Pin = D4; //Pin D2
//const char SWITCH1_FEED[] PROGMEM = AIO_USERNAME "/feeds/switch1";
//const char SWITCH2_FEED[] PROGMEM = AIO_USERNAME "/feeds/switch2";

// Create an ESP8266 WiFiClient class to connect to the MQTT server.
WiFiClient client;
// or... use WiFiFlientSecure for SSL
//WiFiClientSecure client;

// Setup the MQTT client class by passing in the WiFi client and MQTT server and login details.
Adafruit_MQTT_Client mqtt(&client, AIO_SERVER, AIO_SERVERPORT, AIO_USERNAME, AIO_KEY);

/****************************** Feeds ***************************************/

// Setup a feed called 'photocell' for publishing.
// Notice MQTT paths for AIO follow the form: <username>/feeds/<feedname>
//Adafruit_MQTT_Publish photocell = Adafruit_MQTT_Publish(&mqtt, AIO_USERNAME "/feeds/photocell");

// Setup a feed called 'onoff' for subscribing to changes.
Adafruit_MQTT_Subscribe onoffbutton = Adafruit_MQTT_Subscribe(&mqtt, AIO_USERNAME "/feeds/switch1");
Adafruit_MQTT_Subscribe onoffbutton2 = Adafruit_MQTT_Subscribe(&mqtt, AIO_USERNAME "/feeds/switch2");
//Adafruit_MQTT_Subscribe onoffbutton = Adafruit_MQTT_Subscribe(&mqtt, SWITCH1_FEED);
//Adafruit_MQTT_Subscribe onoffbutton2 = Adafruit_MQTT_Subscribe(&mqtt, SWITCH2_FEED);

Adafruit_MQTT_Publish publishSwitch1 = Adafruit_MQTT_Publish(&mqtt, AIO_USERNAME "/feeds/switch1");
Adafruit_MQTT_Publish publishSwitch2 = Adafruit_MQTT_Publish(&mqtt, AIO_USERNAME "/feeds/switch2");

/*************************** Sketch Code ************************************/

// Bug workaround for Arduino 1.6.6, it seems to need a function declaration
// for some reason (only affects ESP8266, likely an arduino-builder bug).
void MQTT_connect();

void setup() {
  //Memulai komunikasi Serial
  Serial.begin(19200);
  delay(10);

  Serial.println(F("Adafruit MQTT demo"));

  // Connect to WiFi access point.
  Serial.println(); Serial.println();
  Serial.print("Connecting to ");
  Serial.println(WLAN_SSID);

  //Menyambung ke Access Point
  WiFi.begin(WLAN_SSID, WLAN_PASS);
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  Serial.println();

  Serial.println("WiFi connected");
  //Menampilkan Lokal IP Addresss ESP8266
  Serial.println("IP address: "); Serial.println(WiFi.localIP());

  // Setup MQTT subscription for onoff feed.
  mqtt.subscribe(&onoffbutton);
  mqtt.subscribe(&onoffbutton2);

  //Konfigurasi switch pin
  pinMode(switch1Pin, OUTPUT);
  pinMode(switch2Pin, OUTPUT);
  //Dalam keadaan awal, kondisi switch mati
  digitalWrite(switch1Pin, HIGH);
  digitalWrite(switch2Pin, HIGH);
  //pin pullup
  //  pinMode(LED_BUILTIN, OUTPUT);
  //  digitalWrite(LED_BUILTIN, HIGH);
}

uint32_t x = 0;

void loop() {
  // Ensure the connection to the MQTT server is alive (this will make the first
  // connection and automatically reconnect when disconnected).  See the MQTT_connect
  // function definition further below.
  MQTT_connect();

  // this is our 'wait for incoming subscription packets' busy subloop
  // try to spend your time here

  Adafruit_MQTT_Subscribe *subscription;
  //Mendengarkan data yang masuk dengan timeout sebesar 5 detik
  while ((subscription = mqtt.readSubscription(5000))) {
    //Jika subscription adalah switch 1
    if (subscription == &onoffbutton) {
      Serial.print(F("Got1: "));
      //Membaca data yang masuk
      Serial.println((char *)onoffbutton.lastread);
      //Jika data yang masuk sama dengan 1
      if (!strcmp((char*) onoffbutton.lastread, "1")) {
        //Active low logic
        digitalWrite(switch1Pin, LOW); //Menghidupkan switch 1
      } else {
        digitalWrite(switch1Pin, HIGH); //Mematikan switch 1
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
  //
  //   while ((subscription = mqtt.readSubscription(5000))) {
  //    if (subscription == &onoffbutton2) {
  //      Serial.print(F("Got2: "));
  //      Serial.println((char *)onoffbutton2.lastread);
  //    }
  //  }

  // Now we can publish stuff!
  //  Serial.print(F("\nSending photocell val "));
  //  Serial.print(x);
  //  Serial.print("...");
  //  if (! photocell.publish(x++)) {
  //    Serial.println(F("Failed"));
  //  } else {
  //    Serial.println(F("OK!"));
  //  }

  // ping the server to keep the mqtt connection alive
  // NOT required if you are publishing once every KEEPALIVE seconds
  /*
    if(! mqtt.ping()) {
    mqtt.disconnect();
    }
  */
}

// Function to connect and reconnect as necessary to the MQTT server.
// Should be called in the loop function and it will take care if connecting.
void MQTT_connect() {
  int8_t ret;

  // Stop if already connected.
  if (mqtt.connected()) {
    return;
  }

  Serial.print("Connecting to MQTT... ");

  //  uint8_t retries = 3;
  //Jika belum tersambung ke Adafruit MQTT, menyambung kembali
  while ((ret = mqtt.connect()) != 0) { // connect will return 0 for connected
    Serial.println(mqtt.connectErrorString(ret));
    Serial.println("Retrying MQTT connection in 5 seconds...");
    mqtt.disconnect();
    delay(5000);  // wait 5 seconds
    //    retries--;
    //    if (retries == 0) {
    // basically die and wait for WDT to reset me
    //      while (1);
    //    }
  }
  Serial.println("MQTT Connected!");

  if (! publishSwitch1.publish(0)) {
    Serial.println(F("Failed Publish Switch 1"));
  } else {
    Serial.println(F("OK!"));
  }
  if (! publishSwitch2.publish(0)) {
    Serial.println(F("Failed Publish Switch 2"));
  } else {
    Serial.println(F("OK!"));
  }
}
