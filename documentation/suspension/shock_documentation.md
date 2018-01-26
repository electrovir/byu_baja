# Suspension Travel Sensor Documentation

## Materials

  * [Arduino](https://www.amazon.com/dp/B01EWOE0UU)
  * Linear Potentiometer (250 mm range)

## Arduino Code

The output of the linear potentiometer ranges from ``0 V - 5 V``, which meets the requirements of the Arduino's analog inputs (max of ``5 V``) thus no step down resistor is needed. The potentiometer's output voltage has been tested by Brady Nash to be consistently linear within its range of ``0 mm - 250 mm``.

Because the Arduino's analog inputs report values on a range of ``0 - 1024`` integer values, each integer change in value corresponds to a change in ``0.24414 mm``.

I'm deciding to report the potentiometer values as percentages. Thus, all is needed is the analog input divided by the max analog reading (``1024``) multiplied by ``100`` so that it's an integer (making life easier and faster).

shockTravel = analogInput / 1024 * 100%

Due to the way **C++** handles number types and arithmetic, I changed the equation to the following to prevent the formula always rounding down to 0:

shockTravel = analogInput / ( 1024.0 / 100.0% )

## Circuit

Connect linear potentiometer up with the following connections. Note that the *shield* is the wire with the circle on the end.

  * Potentiometer : Arduino
  * Black wire : Analog pin A5 or A4
  * Blue wire : GND
  * Brown wire : 5V
  * Shield : none

Pin A4 on the arduino is for the **left** shock and pin A5  is for the **right** shock. This may change in the future.