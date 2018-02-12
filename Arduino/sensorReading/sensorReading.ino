//
// Speedometer and Tachometer setup
//

// hook up bluetooth module like so:
// bluetooth to arduino
// VCC to 5 V
// GND TO GND
// TX to RX
// RX to TX

#define AVERAGE_COUNT_TACHOMETER_MAX 2
#define AVERAGE_COUNT_SPEEDOMETER_MAX 2
#define TACHOMETER_PIN 7
#define SPEEDOMETER_PIN 8
#define BAUD_RATE 115200
#define ZERO_RPM_TIMEOUT 1000000
#define ZERO_SPEED_TIMEOUT 1000000
#define WHEEL_DIAMETER_INCHES 21.0
// this is the maximum period of bluetooth transmission in milliseconds
#define MIN_PRINT_DELAY 12

enum spinState {TRIGGER, ON, OFF};
spinState currentTachometerState = OFF;
spinState currentSpeedometerState = OFF;
const float SPEEDOMETER_FACTOR = (AVERAGE_COUNT_SPEEDOMETER_MAX/2.0) * 178499.6 * WHEEL_DIAMETER_INCHES;
const unsigned long TACHOMETER_FACTOR = AVERAGE_COUNT_TACHOMETER_MAX/2.0 * 60000000.0;

unsigned long lastTimeTachometer = 0;
unsigned long averageTotalTimeTachometer = 0;
int averageCounterTachometer = 0;
bool zeroedTachometer = false;

unsigned long lastTimeSpeedometer = 0;
unsigned long averageTotalTimeSpeedometer = 0;
int averageCounterSpeedometer = 0;
bool zeroedSpeedometer = false;

int ledStatus = LOW;
int finalRpm = 0;
int finalSpeed = 0;

//
// linear potentiometer setup (suspension position)
//

#define SHOCK_PIN_RIGHT A5
#define SHOCK_PIN_LEFT A4

// These are calculated as percentages of the total possible shock travel reported as integer values.
// Ex: a value of 55 = 55%
unsigned int shockLeft = 0;
unsigned int shockRight = 0;
// used to convert analog input value to a percent
const float SHOCK_FACTOR = (1024.0/100.0);
// the analog inputs can only be read every 100 microseconds
enum shockState {DELAY, READ};
unsigned long lastTimeShock = 0;
shockState currentShockState = READ;

//
// printing speed
//

unsigned long lastTimePrint = 0;

void setup() {
  // initialize serial:
  Serial.begin(BAUD_RATE);
  pinMode(TACHOMETER_PIN, INPUT);
  pinMode(SPEEDOMETER_PIN, INPUT);
  pinMode(LED_BUILTIN, OUTPUT);
  pinMode(SHOCK_PIN_LEFT, INPUT);
  pinMode(SHOCK_PIN_RIGHT, INPUT);
}

void loop() {
  unsigned long newTime = micros();
  readTachometer(newTime);
  readSpeedometer(newTime);
  readPotentiometers(newTime);
//  uncomment the next line for testing max transmission frequency
//  printValues();
}

void readPotentiometers(unsigned long newTime) {
  shockState nextShockState = currentShockState;

  if (currentShockState == DELAY) {
    if (newTime - lastTimeShock > 100) {
      nextShockState = READ;
      lastTimeShock = newTime;
    }
  }
  else if (currentShockState == READ){
    bool print = false;
    unsigned int newShockRight = float(analogRead(SHOCK_PIN_RIGHT)) / SHOCK_FACTOR;
    unsigned int newShockLeft = float(analogRead(SHOCK_PIN_LEFT)) / SHOCK_FACTOR;
    
    nextShockState = DELAY;
    if (newShockRight != shockRight || newShockLeft != shockLeft) {
      print = true;
    }

    shockRight = newShockRight;
    shockLeft = newShockLeft;
    if (print) {
      printValues();
    }
  }

  currentShockState = nextShockState;
}

void readTachometer(unsigned long newTime) {
  spinState nextTachometerState = currentTachometerState;
  int tachometerReading = digitalRead(TACHOMETER_PIN);
  unsigned long difference = newTime - lastTimeTachometer;
  if (currentTachometerState == OFF) {
    if (difference > ZERO_RPM_TIMEOUT && !zeroedTachometer) {
      zeroedTachometer = true;
      finalRpm = 0;
      printValues();
    }
    if (tachometerReading == 1) {
      nextTachometerState = TRIGGER;
    }
  }
  else if (currentTachometerState == TRIGGER) {
    if (tachometerReading == 1) {
      nextTachometerState = ON;
    }
    else {
      nextTachometerState = OFF;
    }
  }
  else if (currentTachometerState == ON) {
    if (tachometerReading == 0) {
      nextTachometerState = OFF;
    }
  }

  if (nextTachometerState == TRIGGER) {
    zeroedTachometer = false;
      averageCounterTachometer = averageCounterTachometer + 1;
      averageTotalTimeTachometer += difference;
      if (averageCounterTachometer == AVERAGE_COUNT_TACHOMETER_MAX) {
        // rpm calcualtions
        finalRpm = TACHOMETER_FACTOR/averageTotalTimeTachometer;
        
        // reading output
        printValues();
        digitalWrite(LED_BUILTIN, !ledStatus);

        // reset timers
        averageTotalTimeTachometer = 0;
        averageCounterTachometer = 0;
      }
      lastTimeTachometer = newTime;
  }
  currentTachometerState = nextTachometerState;
}


void readSpeedometer(unsigned long newTime) {
  spinState nextSpeedometerState = currentSpeedometerState;
  int speedometerReading = digitalRead(SPEEDOMETER_PIN);
  unsigned long difference = newTime - lastTimeSpeedometer;
  if (currentSpeedometerState == OFF) {
    if (difference > ZERO_SPEED_TIMEOUT && !zeroedSpeedometer) {
      zeroedSpeedometer = true;
      finalSpeed = 0;
      printValues();
    }
    if (speedometerReading == 1) {
      nextSpeedometerState = TRIGGER;
    }
  }
  else if (currentSpeedometerState == TRIGGER) {
    if (speedometerReading == 1) {
      nextSpeedometerState = ON;
    }
    else {
      nextSpeedometerState = OFF;
    }
  }
  else if (currentSpeedometerState == ON) {
    if (speedometerReading == 0) {
      nextSpeedometerState = OFF;
    }
  }

  if (nextSpeedometerState == TRIGGER) {
    zeroedSpeedometer = false;
      averageCounterSpeedometer++;
      averageTotalTimeSpeedometer += difference;
      if (averageCounterSpeedometer == AVERAGE_COUNT_SPEEDOMETER_MAX) {
        // rpm calcualtions
        finalSpeed = SPEEDOMETER_FACTOR/averageTotalTimeSpeedometer;
        
        // reading output
        printValues();

        // reset timers
        averageTotalTimeSpeedometer = 0;
        averageCounterSpeedometer = 0;
      }
      lastTimeSpeedometer = newTime;
  }
  currentSpeedometerState = nextSpeedometerState;
}


void printValues() {
  // r: revolutions (rpm)
  // s: speed (mph)
  // sl: left shock position (%)
  // sr: right shock position (%)

  // limit this to 100 Hz otherwise the 
  if (millis() - lastTimePrint > MIN_PRINT_DELAY ) {
    Serial.println("{\"r\":" + String(finalRpm) + " \"s\":" + String(finalSpeed) + " \"sl\":" + String(shockLeft) + " \"sr\":" + String(shockRight) + "}");
    lastTimePrint = millis();
  }
}

