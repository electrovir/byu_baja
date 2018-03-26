# Pitch and Roll Documentation

## Materials

  * Android device

## Java Code

Android used to have a sensor type for orientation but it's deprecated so now the magnetic field and accelerometer sensors have to be used in conjunction to determine orientation. I found a tutorial online that had already figured out how to do this (this was hard to find): [ssaurel.com](https://www.ssaurel.com/blog/get-android-device-rotation-angles-with-accelerometer-and-geomagnetic-sensors/)



The goal here is just to get the pitch and roll of the car so I removed the azimuth measurement and switched the axes in the ``SensorManager.remapCoordinateSystem`` call. I also bumped up the sensor speed from ``SensorManager.SENSOR_DELAY_NORMAL`` to ``SensorManager.SENSOR_DELAY_FASTEST``.

## Result

This is not working.