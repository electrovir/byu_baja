package com.electrovir.byubaja;

import android.app.ActionBar;
import android.os.Environment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.electrovir.byubaja.util.FileLog;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class MainActivity extends AppCompatActivity implements
        BluetoothConnectionFragment.BluetoothConnectionCaller, AccelerometerFragment.AccelerometerCaller {
    private static final String TAG = "BYU_BAJA_MAIN";

    TextView mRpmText;
    TextView mMphText;
    TextView mCoords0;
    TextView mCoords1;
    TextView mCoords2;
    TextView mShockLeftText;
    TextView mShockRightText;
    private static final String TAG_BLUETOOTH_FRAGMENT = "bluetoothFragment";
    private static final String TAG_ACCELEROMETER_FRAGMENT = "accelerometerFragment";

    private BluetoothConnectionFragment mBluetoothFragment;
    private AccelerometerFragment mAccelerometerFragment;
//    private static final String FILE_NAME = "testLog.txt";
//    FileOutputStream fileOutput;
    File logFile;
//    FileWriter logFileWriter;

    public void handleBluetoothInput(String input) {
        final boolean testing = false;
        if (testing) {
            System.out.println(input);
        }
        else {
            // "r:rpm s:mph sl:percent sr:percent
            // (shockRight)
            FileLog.data(TAG, input);

            if (input != null) {
                // 0: rpm
                // 1: mph
                // 2: percent
                // 3: percent
                ReceiveData data;
                try {
                    data = new ObjectMapper().readValue(input, ReceiveData.class);

                    mRpmText.setText(data.r);
                    mMphText.setText(data.s);
                    mShockLeftText.setText(data.sl);
                    mShockRightText.setText(data.sr);
                }
                catch (IOException error) {

                }
            }
        }
    }

    public void handleAccelerometerInput(float value0, float value1, float value2) {
        mCoords0.setText(Float.toString(value0));
        mCoords1.setText(Float.toString(value1));
        mCoords2.setText(Float.toString(value2));
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

    public void appendLog(String text) {
//        int permission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);

//        verifyStoragePermissions(this);

        logFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "byu_baja_log_fie.txt");
        if (!logFile.exists())
        {
            try
            {
                logFile.createNewFile();
            }
            catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        try
        {
            //BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            buf.append(text);
            buf.newLine();
            buf.close();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.instrument_cluster);
        hideStatusBar();

        // for display the received data from the Arduino
        mRpmText = (TextView) findViewById(R.id.tachometer);
        mMphText = (TextView) findViewById(R.id.speedometer);
        mCoords0 = (TextView) findViewById(R.id.textView_0);
        mCoords1 = (TextView) findViewById(R.id.textView_1);
        mCoords2 = (TextView) findViewById(R.id.textView_2);

        mShockRightText = (TextView) findViewById(R.id.shock_left);
        mShockLeftText = (TextView) findViewById(R.id.shock_right);

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
    }

    @Override
    public void onPause() {
        super.onPause();
        FileLog.saveFile(this);
    }
}