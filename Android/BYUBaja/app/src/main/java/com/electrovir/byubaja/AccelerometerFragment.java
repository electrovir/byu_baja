package com.electrovir.byubaja;

// thanks to https://www.ssaurel.com/blog/get-android-device-rotation-angles-with-accelerometer-and-geomagnetic-sensors/

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class AccelerometerFragment extends Fragment implements SensorEventListener {
    private SensorManager mSensorManager;
    private Sensor mAccelerometerSensor;
    private Sensor mMagneticSensor;

    // Gravity rotational data
    private float gravity[];
    // Magnetic rotational data
    private float magnetic[]; //for magnetic rotational data
    private float accelerometerValues[] = new float[3];
    private float magneticValues[] = new float[3];
    private float[] values = new float[3];

    // azimuth, pitch and roll
    private float azimuth;
    private float pitch;
    private float roll;
    private boolean sensorReady = false;
    private static final int AVERAGE_COUNT = 10;
    private float[] pitchValues = new float[AVERAGE_COUNT];
    private float[] rollValues = new float[AVERAGE_COUNT];
    private int valuesIterator = 0;

    private static final long READ_PERIOD_MS = 500;

    private long lastSensorReadTimeMs = 0;

    private AccelerometerCaller caller;
    private Context activityContext;

    class AccelerometerReading {
        float pitch;
        float roll;

        public AccelerometerReading(float pitch, float roll) {
            this.pitch = pitch;
            this.roll = roll;
        }

        public String toString() {
            return "pitch: " + Float.toString(this.pitch) + ", roll: " + Float.toString(this.roll);
        }
    }

    interface AccelerometerCaller {
        void handleAccelerometerInput(float azimuth, float value1, float value2);
    }

    public AccelerometerFragment() {
        // Required empty public constructor
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

//        switch (sensorEvent.sensor.getType()) {
//            case Sensor.TYPE_MAGNETIC_FIELD:
//                mags = sensorEvent.values.clone();
//                break;
//            case Sensor.TYPE_ACCELEROMETER:
//                accels = sensorEvent.values.clone();
//                break;
//        }
//
//        if (mags != null && accels != null) {
//            gravity = new float[9];
//            magnetic = new float[9];
//            SensorManager.getRotationMatrix(gravity, magnetic, accels, mags);
//            float[] outGravity = new float[9];
//            SensorManager.remapCoordinateSystem(gravity, SensorManager.AXIS_X, SensorManager
//                    .AXIS_Z, outGravity);
//            SensorManager.getOrientation(outGravity, values);
//
//            azimuth = values[0];
//            pitch =values[1];
//            roll = values[2];
//            mags = null;
//            accels = null;
//            if (valuesIterator == AVERAGE_COUNT - 1) {
//
//                float averageRoll = MiscTools.sumArray(rollValues) / AVERAGE_COUNT;
//                float averagePitch = MiscTools.sumArray(pitchValues) / AVERAGE_COUNT;
//                caller.handleAccelerometerInput(new AccelerometerReading(averagePitch, averageRoll));
//                valuesIterator = 0;
//            }
//            else {
//                rollValues[valuesIterator] = roll;
//                pitchValues[valuesIterator] = pitch;
//                valuesIterator++;
//            }
//
//        }

        switch (sensorEvent.sensor.getType()) {
            case Sensor.TYPE_MAGNETIC_FIELD:
                magneticValues = sensorEvent.values.clone();
                sensorReady = true;
                break;
            case Sensor.TYPE_ACCELEROMETER:
                accelerometerValues = sensorEvent.values.clone();
        }

        if (magneticValues != null && accelerometerValues != null && sensorReady) {
            sensorReady = false;

            float[] R = new float[16];
            float[] I = new float[16];

            SensorManager.getRotationMatrix(R, I, accelerometerValues, magneticValues);

            float[] actualOrientation = new float[3];
            SensorManager.getOrientation(R, actualOrientation);

            caller.handleAccelerometerInput(actualOrientation[0], actualOrientation[1],
                    actualOrientation[2]);

        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    // I don't know what paramters this needs yet
    public static AccelerometerFragment newInstance() {
        AccelerometerFragment fragment = new AccelerometerFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSensorManager = (SensorManager) this.activityContext.getSystemService(Activity.SENSOR_SERVICE);
        mAccelerometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagneticSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        registerSensorListeners();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // this has no view
        return null;
    }

    public void registerSensorListeners() {
        mSensorManager.registerListener(this, mAccelerometerSensor, SensorManager
                .SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener(this, mMagneticSensor, SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.activityContext = context;
        this.caller = (AccelerometerCaller) context;

    }

    @Override
    public void onDetach() {
        super.onDetach();

        this.caller = null;
    }

    @Override
    public void onResume() {
        super.onResume();

        registerSensorListeners();
    }

    @Override
    public void onPause() {
        super.onPause();

        mSensorManager.unregisterListener(this);
    }
}
