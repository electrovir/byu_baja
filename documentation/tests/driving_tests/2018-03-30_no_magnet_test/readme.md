# Driving Test

## Mar 30, 2018

### Driving Test Without Magnets
First driving test on new car

#### Details

  * **Drivers**: Casey, Nolan, Braden
  * **Sensor Equipment**: me
  * **Faculty**: Dr. Hovanski
  * **Vehicle Number**: 27
  * Only the speedometer was hooked up.
  * Speedometer magnet was not installed near the sensor in order to see if sensor noise could be picked up.
  * Arduino was installed next to the driver's seat inside my Intel case. It wasn't installed behind the firewall due to the lack of a gas tank splash guard.
  * Tablet was folded up inside a pair of pants and put into a bag under the driver's seat.
  * Still no tablet or Arduino mounting.
  * Tablet app has several new improvements.
    * It's possible to save "mini-runs" now or subset of data.
    * All data is time stamped so packet loss does not throw off timing.
    * The app doesn't crash anymore!
  * The Arduino was taped together to prevent the prototype boards from disconnecting from the Arduino headers like it did last time.

#### Results

**No** noise was seen in the data at all. Note the time plot ``hundreds of seconds``. This tracks the passage of time. Sharp slopes indicate packet loss. It is mostly a steady slope, meaning low packet loss.

![data_plots](speed_readings.png)

Everything on the Arduino stayed connected.

#### Data Analysis

After the data was pulled from the tablet, true/false values were replaced with a 1 or 0 (there were actually no false values). Bad lines were removed by using find/replace with the following regular expression:

```
^(?!\d+,\d+,\d+,\d+,\d+,\d+,\d+,\d+\n).*?\n
```

Matches were replaced with blank strings, thus the bad lines were totally removed.

The large ``data.txt`` file was not used due to its large file size. 