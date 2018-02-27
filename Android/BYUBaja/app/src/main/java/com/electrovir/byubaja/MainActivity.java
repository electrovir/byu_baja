package com.electrovir.byubaja;

import android.app.ActionBar;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import java.io.IOException;

import com.electrovir.byubaja.util.FileLog;


public class MainActivity extends AppCompatActivity implements
        BluetoothConnectionFragment.BluetoothConnectionCaller, AccelerometerFragment.AccelerometerCaller {
    private static final String TAG = "BYU_BAJA_MAIN";

    TextView mRpmText;
    TextView mMphText;
    ProgressBar mTachoDial;
    ProgressBar mShockLeftProgress;
    ProgressBar mShockRightProgress;
    View mBluetoothIndicator;
    boolean bluetoothDeviceConnected = false;
    private static final String TAG_BLUETOOTH_FRAGMENT = "bluetoothFragment";
    private static final String TAG_ACCELEROMETER_FRAGMENT = "accelerometerFragment";

    private static final int DATA_START = 2;
    private static final int MAX_TACH_PROGRESS = 60;
    private static final int MAX_TACH_READING = 4000;
    private static final String LINE_BREAK_CHARACTER = ";";
    private static final String DATA_BREAK_CHARACTER = ",";

    private BluetoothConnectionFragment mBluetoothFragment;
    private AccelerometerFragment mAccelerometerFragment;

    public void handleBluetoothInput(String input) {
        final boolean testing = false;
        if (testing) {
            System.out.println(input);
        }
        else {
            if (input != null) {
                // 0: count
                // 1: ms
                // 2: rpm
                // 3: mph
                // 4: percent
                // 5: percent
                String[] lines = input.trim().split(LINE_BREAK_CHARACTER);

                if (lines.length < 2) {
                    FileLog.e(TAG, "Garbled data:" + input);
                    return;
                }

                String[] data = lines[0].trim().split(DATA_BREAK_CHARACTER);

                if (data.length < 6) {
                    FileLog.e(TAG, "Garbled data:" + input);
                    return;
                }

                String dataLogString =  input.trim().replace(LINE_BREAK_CHARACTER, " " +
                        bluetoothDeviceConnected + " " + FileLog.getTimeStamp() + "\n").trim();

                FileLog.data(TAG, dataLogString);
                try {
                    mRpmText.setText(data[DATA_START]);
                    mTachoDial.setProgress(Integer.valueOf(data[DATA_START]) * MAX_TACH_PROGRESS / MAX_TACH_READING);
                }
                catch (NumberFormatException e) {
                    FileLog.e(TAG, "Invalid value for rpm:" + data[DATA_START]);
                }
                mMphText.setText(data[DATA_START + 1]);
                mShockLeftProgress.setProgress(Integer.parseInt(data[DATA_START + 2]));
                mShockRightProgress.setProgress(Integer.parseInt(data[DATA_START + 3]));
            }
        }
    }

    @Override
    public void bluetoothConnectionStatus(boolean status) {

        bluetoothDeviceConnected = status;

        if (status) {
            mBluetoothIndicator.setBackgroundColor(ContextCompat.getColor(this, R.color
                    .bluetoothConnected));
        }
        else {
            FileLog.i(TAG, "Bluetooth disconnected.");
            mBluetoothIndicator.setBackgroundColor(ContextCompat.getColor(this, R.color
                    .bluetoothDisconnected));
        }
    }

    public void handleAccelerometerInput(float value0, float value1, float value2) {

    }

    public void bluetoothError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void hideStatusBar() {
        View decorView = getWindow().getDecorView();
        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);
        // Remember that you should never show the action bar if the
        // status bar is hidden, so hide that too if necessary.
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
    }

    private void addBluetoothFragment(String moduleName) {
        FragmentManager fm = this.getSupportFragmentManager();

        mBluetoothFragment = (BluetoothConnectionFragment) fm.findFragmentByTag(TAG_BLUETOOTH_FRAGMENT);

        if (mBluetoothFragment == null) {
            mBluetoothFragment = BluetoothConnectionFragment.newInstance(moduleName);
            fm.beginTransaction().add(mBluetoothFragment, TAG_BLUETOOTH_FRAGMENT).commit();
        }
    }

    private void addAccelerometerFragment() {
        FragmentManager fm = this.getSupportFragmentManager();

        mAccelerometerFragment = (AccelerometerFragment) fm.findFragmentByTag
                (TAG_ACCELEROMETER_FRAGMENT);

        if (mAccelerometerFragment == null) {
            mAccelerometerFragment = AccelerometerFragment.newInstance();
            fm.beginTransaction().add(mAccelerometerFragment, TAG_ACCELEROMETER_FRAGMENT).commit();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.instrument_cluster);
//        hideStatusBar();

        // for display the received data from the Arduino
        mRpmText = (TextView) findViewById(R.id.tachometer);
        mMphText = (TextView) findViewById(R.id.speedometer);
        mTachoDial = (ProgressBar) findViewById(R.id.tachometerProgress);
        mBluetoothIndicator = findViewById(R.id.bluetooth_status);

        mShockLeftProgress = (ProgressBar) findViewById(R.id.shock_front_left_position);
        mShockRightProgress = (ProgressBar) findViewById(R.id.shock_front_right_position);

        // TODO: make this work with multiple module names, or just rename the module
        // note that HC-05 will be the final module name but I'm developing with an H4S
        addBluetoothFragment("H4S");
//        addBluetoothFragment("HC-05");

        addAccelerometerFragment();

        try {
            FileLog.setDefaultFiles(this, "byu_baja");
        }
        catch (IOException e) {
            Log.e(TAG, "Error creating file logger: " + e.getMessage());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        hideStatusBar();
    }

    @Override
    public void onPause() {
        super.onPause();
        FileLog.saveFile(this);
    }
}