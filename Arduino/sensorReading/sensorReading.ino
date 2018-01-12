#define AVERAGE_COUNT_TACHOMETER_MAX 2
#define AVERAGE_COUNT_SPEEDOMETER_MAX 2
#define TACHOMETER_PIN 7
#define SPEEDOMETER_PIN 8
#define BAUD_RATE 115200
#define ZERO_RPM_TIMEOUT 1000000
#define ZERO_SPEED_TIMEOUT 1000000
#define WHEEL_DIAMETER_INCHES 21

enum spinState {TRIGGER, ON, OFF};
spinState currentTachometerState = OFF;
spinState currentSpeedometerState = OFF;
const float SPEEDOMETER_FACTOR = (AVERAGE_COUNT_SPEEDOMETER_MAX/2) * 178499.6 * WHEEL_DIAMETER_INCHES;
const unsigned long TACHOMETER_FACTOR = AVERAGE_COUNT_TACHOMETER_MAX/2 * 60000000;

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

void setup() {
  // initialize serial:
  Serial.begin(BAUD_RATE);
  pinMode(TACHOMETER_PIN, INPUT);
  pinMode(LED_BUILTIN, OUTPUT);
}

void loop() {
  readTachometer();
  readSpeedometer();
}

void readTachometer() {
  spinState nextTachometerState = currentTachometerState;
  unsigned long newTime = micros();
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


void readSpeedometer() {
  spinState nextSpeedometerState = currentSpeedometerState;
  unsigned long newTime = micros();
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
  Serial.println("r:" + String(finalRpm) + " s:" + String(finalSpeed));
}

