
#define DEBOUNCE_COUNT_MAX 2

int lastValue = 0;
int debounceCount = 0;
int i = 0;


void setup() {
  //start serial connection
  Serial.begin(57600);
  //configure pin 2 as an input and enable the internal pull-up resistor
  pinMode(2, INPUT);

}

void loop() {
  //read the pushbutton value into a variable
  int sensorValue = digitalRead(2);
  if ((sensorValue == 1 && lastValue == 0) || (sensorValue == 0 && lastValue == 1)) {
    Serial.print(i++);
    Serial.print(" ");
    Serial.println(sensorValue);
  }
  lastValue = sensorValue;
}
