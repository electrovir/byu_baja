# Speedometer and Tachometer Debouncing

## Noise Troubles

The speedometer has lots of noise.

## Debouncing

With a debounce timer, care must be taken to properly set the delay of the debounce timer. The following equations are the limiting factors for the timer. **See the Matlab file ``debounce_timer.m`` for explanations on the variables.**

### Minimum Magnet Length
```MATLAB
Lm_min = 2 * Ts * Da * Vv_maxTarget / Dt;
```

With current parameters, the minimum magnet length is ``5 mm``. I'm choosing ``1 cm`` for the magnet length. Note that the magnetic field fringing may cause the magnet to have a larger are of effect on the sensor than the actual size of the magnet. However, with this small of a magnet I *doubt* it will make much difference.

### Debounce Time

The debounce time limits what speeds can be measured with a given magnet length and sample frequency. I doubt the sample frequency can be cranked up much but that can be experimented with.

```MATLAB
carVelocityMin = Lm * Dt / (Td * Da);
carVelocityMax = (Da * pi - Lm) * Dt / (Td * Da);
```

#### Parameters

  * **Lm**: ``1 cm`` (magnet length)
  * **Dt**: ``21.5"`` (tire diameter)
  * **Da**: ``3"`` (axle diamter)

  
**NOTE** I am concerned that a ``1 cm`` magnet might not be picked up by the sensor at the range it is currently installed with on the car.

#### Result

The following graph is from treating **Td** as the independent variable.

![](debounce_timer.png)

Based on this data, I chose ``90ms`` as the debounce time.

### Max Revolution Time

This is useful for the Aruduino. If this time is exceeded, in between magnet pick ups, the car is considered at rest.

```Matlab
maxRevolutionTime = Dt * pi / carVelocityMin
```

With paramters from the following section, this is ``2.15 s``.