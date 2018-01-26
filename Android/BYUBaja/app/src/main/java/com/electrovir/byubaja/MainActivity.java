package com.electrovir.byubaja;


import android.Manifest;
import android.app.ActionBar;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.electrovir.byubaja.util.FileLog;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.Buffer;

public class MainActivity extends AppCompatActivity implements BluetoothConnectionFragment.BluetoothConnectionCaller {
    private static final String TAG = "BYU_BAJA_MAIN";

    TextView mRpmText;
    TextView mMphText;
    private static final String TAG_BLUETOOTH_FRAGMENT = "bluetoothFragment";
    private BluetoothConnectionFragment mBluetoothFragment;
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
            FileLog.data(TAG, input);

            String rpm = "0";
            String mph = "0";
            if (input == null) {
                rpm = "0";
                mph = "0";
            }
            else {
                String[] parts = input.split(" ");

                String[] rpmParts = parts[0].split(":");
                rpm = rpmParts[1];

                String[] mphParts = parts[1].split(":");
                mph = mphParts[1];
            }

            if (mRpmText != null) {
                mRpmText.setText(rpm);
            }
            if (mMphText != null) {
                mMphText.setText(mph);
            }
        }
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
//        mBluetoothFragment.setCaller(this);
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
        appendLog("WHY AREN'T YOU WORKING");

        // for display the received data from the Arduino
        mRpmText = (TextView) findViewById(R.id.tachometer);
        mMphText = (TextView) findViewById(R.id.speedometer);

        // TODO: make this work with multiple module names, or just rename the module
        // note that HC-05 will be the final module name but I'm developing with an H4S
        addBluetoothFragment("H4S");
//        addBluetoothFragment("HC-05");
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

//    private boolean saveFile() {
//        //https://stackoverflow.com/a/46657146/5500690
//        DownloadManager downloadManager = (DownloadManager) this.getSystemService(DOWNLOAD_SERVICE);
//        downloadManager.addCompletedDownload(logFile.getName(), logFile.getName(), true, "text/plain",logFile.getAbsolutePath(),logFile.length(),true);
//        return true;
//    }

    @Override
    public void onPause() {
        super.onPause();
        FileLog.saveFile(this);
//        if (saveFile()) {
//            Log.i(TAG, "download complete!");
//        }
//        else {
//            Log.i(TAG, "download failed!");
//        }
    }
}