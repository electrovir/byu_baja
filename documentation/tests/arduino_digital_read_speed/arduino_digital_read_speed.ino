
#define DEBOUNCE_COUNT_MAX 2

int lastValue = 0;
unsigned long lastTime = 0;
int debounceCount = 0;


void setup() {
  //start serial connection
  Serial.begin(57600);
  //configure pin 2 as an input and enable the internal pull-up resistor
  pinMode(2, INPUT);

}

void loop() {
  //read the pushbutton value into a variable
  int sensorValue = digitalRead(2);
  if (sensorValue != lastValue) {
    Serial.print(sensorValue);
    Serial.print("  ");
    lastValue = sensorValue;
    unsigned long newTime = micros();
    Serial.print(newTime - lastTime);
    Serial.print("\n");
  }
  lastTime = newTime;
}
