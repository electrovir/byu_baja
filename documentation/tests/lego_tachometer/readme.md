# Speedometer Accuracy

## Materials

  * Arduino with bluetooth module
  * Tablet
  * Reed switch

## Situation

The speedometer is consistently noisy. I encountered this on my initial design on a dyno run but was able to reduce the noise, as seen below:

![](noiseReduction.png)

(Note that this plot is from the tachometer, but the tachometer and the speedometer use the same code, the speedometer just has a different multiplication factor.)

However, once we completed our [first driving test](../driving_tests/2018-02-17_first_instrumentation_test/) it was apparent that the speedometer is extremeley noisy, as seen below (taken from the driving test results):

![](../driving_tests/2018-02-17_first_instrumentation_test/plots/speedo_raw.png)

Currently the state machine detecting reed switch triggers is simply run in the Arduino's ``loop()`` function. Perhaps moving this into an interrupt will improve the sensor's performance.

After more evaluation of the interrupt code from online, a possible reason why the speedometer is so noisy is the way that it's getting debounced. The original design did not include a timer in the debouncing process.

## Changes for New Setup

The new setup will include a debounce timer. This introduces possible complications since the proper debounce wait time needs to be calculated.

## Setup and Process

This test will compare the two methods (``loop()`` vs ``ISR()``) on a bicycle since that is much easier to manage and we currently don't have an assembled Baja car :laughing:. I suspect the interrupt method will be much more accurate (it's the proper way to do it after all).

  1. 