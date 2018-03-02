package com.electrovir.byubaja;

//MAJOR THANKS TO: https://stackoverflow.com/a/25683571/5500690

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.UUID;


public class BluetoothConnectionFragment extends Fragment {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String BLUETOOTH_MODULE_NAME_KEY = "bluetoothModuleName";
    private static final String LOG_TAG = "BT FRAG";
    // Handler statuses
    private static final int RECEIVE_MESSAGE_STATUS = 1;
    private static final int SOCKET_CLOSED_STATUS = -1;

    private static final int BLUETOOTH_ENABLE_CODE = 1;

    private static final int BLUETOOTH_CHECK_INTERVAL = 100;

    // SPP UUID service
    private static final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private String mBluetoothModuleName;
    Handler bluetoothMessageHandler;
    private BluetoothAdapter mBluetoothAdapter = null;
    // this is merely the paired device parameters. If this is found, that doesn't mean it's connected.
    private BluetoothDevice mBluetoothDevice = null;
    private Context actvitiyContext = null;
    ContinuousBluetoothDeviceCheckThread mContinuousCheckingThread;
    // TODO: figure out what mConnectedThread is and if I can use it for anything
    private BluetoothConnectionThread mConnectedThread;
    private BluetoothConnectionCaller caller = null;
    private BluetoothSocket mBluetoothSocket = null;
    private boolean bluetoothConnected = false;

    public BluetoothConnectionFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d(LOG_TAG, "entering onAttach");
        this.actvitiyContext = context;
        this.caller = (BluetoothConnectionCaller) context;
        if (getActivity() == null) {
            Log.e(LOG_TAG, "context is null???");
        }
        this.registerListeners(this.actvitiyContext);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        this.actvitiyContext.unregisterReceiver(this.mReceiver);
        this.caller = null;
    }

    public static BluetoothConnectionFragment newInstance(String bluetoothName) {
        BluetoothConnectionFragment fragment = new BluetoothConnectionFragment();
        Bundle args = new Bundle();
        args.putString(BLUETOOTH_MODULE_NAME_KEY, bluetoothName);
        fragment.setArguments(args);
        return fragment;
    }

    private void updateBluetoothConnectionStatus(boolean connected) {
        bluetoothConnected = connected;
        caller.bluetoothConnectionStatus(connected);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // this will keep the bluetooth connection up and running
        setRetainInstance(true);
        if (getArguments() != null) {
            mBluetoothModuleName = getArguments().getString(BLUETOOTH_MODULE_NAME_KEY);
        }

        Log.d(LOG_TAG, "Creating a new BluetoothConnectionFragment");

        // set up the handler for handling bluetooth messages
        bluetoothMessageHandler = new Handler() {
            public void handleMessage(android.os.Message msg) {
                switch (msg.what) {
                    // if receive massage
                    case RECEIVE_MESSAGE_STATUS:
                        byte[] readBuf = (byte[]) msg.obj;
                        // create string from bytes array
                        String strIncom = new String(readBuf, 0, msg.arg1);
                        StringBuilder sb = new StringBuilder(strIncom);

                        int endOfLineIndex = sb.indexOf("\r\n");

                        if (endOfLineIndex > 0) {
                            String sbprint = sb.substring(0, endOfLineIndex);
                            sb.delete(0, sb.length());
                            if (caller != null) {
                                caller.handleBluetoothInput(sbprint);
                            }
//                            Log.i(LOG_TAG, sbprint);
                        }
                        break;
                    case SOCKET_CLOSED_STATUS:
                        closeSocket();
                        break;
                }
            }
        };

        // get the bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();       // get Bluetooth adapter
        checkBTState(mBluetoothAdapter, mBluetoothModuleName);

        mContinuousCheckingThread = new ContinuousBluetoothDeviceCheckThread(false);
        mContinuousCheckingThread.start();
    }

    public void registerListeners(Context context) {
        // thanks to https://stackoverflow.com/a/4716715/5500690
        IntentFilter bluetoothDeviceChangeFilter = new IntentFilter();
        bluetoothDeviceChangeFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        bluetoothDeviceChangeFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
        bluetoothDeviceChangeFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        context.registerReceiver(this.mReceiver, bluetoothDeviceChangeFilter);
    }

    //The BroadcastReceiver that listens for bluetooth broadcasts
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            String deviceName = device.getName();

            // Device found
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                Log.i(LOG_TAG, "bluetooth action: found: " + deviceName);
            }
            //Device is now connected
            else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                Log.i(LOG_TAG, "bluetooth action: ACL connected: " + deviceName);
                updateBluetoothConnectionStatus(true);

            }
            //Done searching
            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Log.i(LOG_TAG, "bluetooth action: discovery finished: " + deviceName);
            }
            //Device is about to disconnect
            else if (BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED.equals(action)) {
                Log.i(LOG_TAG, "bluetooth action: ACL disconnect requested: " + deviceName);
                if (deviceName.equals(mBluetoothDevice.getName())) {
                    closeSocket();
                }
            }
            //Device has disconnected
            else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                Log.i(LOG_TAG, "bluetooth action: ACL disconnected: " + deviceName);
                updateBluetoothConnectionStatus(false);
            }
        }
    };

    private void closeSocket() {
        try {
            Log.i(LOG_TAG, "Closing bluetooth socket.");
            mBluetoothSocket.close();
        } catch (IOException socketCloseError) {
            Log.e(LOG_TAG, "Socket close error: " + socketCloseError.getMessage());
        }
        finally {
            caller.handleBluetoothInput(null);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // this has no view
        return null;
    }

    private static BluetoothDevice getPairedDeviceByName(String deviceName, BluetoothAdapter adapter) {
        BluetoothDevice desiredDevice = null;
        // this returns a list of all PAIRED devices. This does not mean they are actually connected
        //      or even available for connecting to
        Set<BluetoothDevice> pairedDevices = adapter.getBondedDevices();

        for (BluetoothDevice device : pairedDevices) {
            Log.d(LOG_TAG, "paired device name: " + device.getName());
            if (device.getName().equals(deviceName)) {
                desiredDevice = device;
                Log.i(LOG_TAG, "FOUND THE DEVICE >" + device.getName() + "<");
                break;
            }
        }

        // if no device was found, null is returned
        return desiredDevice;
    }

    private void checkBTState(BluetoothAdapter myAdapter, String moduleName) {
        // Check for Bluetooth support and then check to make sure it is turned on
        // Emulator doesn't support Bluetooth and will return null
        if(myAdapter ==null) {
            caller.bluetoothError("Bluetooth not supported.");
            return;
        }

        if (myAdapter.isEnabled()) {
            Log.d(LOG_TAG, "...Bluetooth ON...");
            mBluetoothDevice = getPairedDeviceByName(moduleName, myAdapter);
        } else {
            Log.d(LOG_TAG, "...Bluetooth OFF...");
            //Prompt user to turn on Bluetooth
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, BLUETOOTH_ENABLE_CODE);
            // do something with the activity result...
        }
    }

    private class ContinuousBluetoothDeviceCheckThread extends Thread {

        private boolean checking = true;

        private ContinuousBluetoothDeviceCheckThread(boolean immediateChecking) {
            this.checking = immediateChecking;
        }

        private void pauseChecking() {
            checking = false;
        }

        private void resumeChecking() {
            checking = true;
        }

        public void run() {
            while(true) {
                if (checking) {
                    Log.i(LOG_TAG, "Continuous bluetooth checking: checking bluetooth connection.");

                    if (mBluetoothSocket == null || !mBluetoothSocket.isConnected() || !bluetoothConnected) {
                        if (mBluetoothSocket == null) {
                            Log.i(LOG_TAG, "Continuous bluetooth checking: null bluetooth socket.");
                        } else {
                            Log.i(LOG_TAG, "Continuous bluetooth checking: bluetooth socket not connected.");
                        }

                        Log.i(LOG_TAG, "Continuous bluetooth checking: attempting to open " +
                                        "bluetooth socket.\n");
                        establishBluetoothConnection(mBluetoothDevice, mBluetoothAdapter, bluetoothMessageHandler);
                    }
                    else {
                        try {
                            Log.i(LOG_TAG, "Continuous bluetooth checking: sleeping");
                            Thread.sleep(BLUETOOTH_CHECK_INTERVAL);
                        } catch (InterruptedException e) {
                            Log.e(LOG_TAG, "Continuous bluetooth checking: error sleeping the thread. exiting continuous checking.");
                            break;
                        }
                    }
                }
            }
        }
    }

    private static class BluetoothConnectionThread extends Thread {
        private final InputStream mmInStream;
//        private final OutputStream mmOutStream;
        private Handler bluetoothMessageHandler;

        private BluetoothConnectionThread(BluetoothSocket socket, Handler resultHandler) {
            InputStream tmpIn = null;
//            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
//                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error creating streams! " + e.getMessage());
            }

            mmInStream = tmpIn;
//            mmOutStream = tmpOut;
            bluetoothMessageHandler = resultHandler;
        }

        public void run() {
            byte[] buffer = new byte[256];  // buffer store for the stream
            int bytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);        // Get number of bytes and message in "buffer"
                    bluetoothMessageHandler.obtainMessage(RECEIVE_MESSAGE_STATUS, bytes, -1, buffer).sendToTarget();     // Send to message queue Handler
                } catch (IOException e) {
                    Log.e(LOG_TAG, "Error reading bluetooth input stream. " + e.getMessage());
                    try {
                        mmInStream.close();
                    }
                    catch (IOException closeError) {
                        Log.e(LOG_TAG, "Error closing bluetooth input stream.");
                    }
                    bluetoothMessageHandler.obtainMessage(SOCKET_CLOSED_STATUS);
                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
//        public void write(String message) {
//            Log.d(LOG_TAG, "...Data to send: " + message + "...");
//            byte[] msgBuffer = message.getBytes();
//            try {
//                mmOutStream.write(msgBuffer);
//            } catch (IOException e) {
//                Log.d(LOG_TAG, "...Error data send: " + e.getMessage() + "...");
//            }
//        }
    }

    private static BluetoothSocket createBluetoothSocket(BluetoothDevice bluetoothDevice) {
        BluetoothSocket bluetoothSocket;
        // Set up a pointer to the remote node using it's address.
//        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(arduinoBluetooth.getAddress());

        // Two things are needed to make a connection:
        //   A MAC address, which we got above.
        //   A Service ID or UUID.  In this case we are using the
        //     UUID for SPP.
        if (bluetoothDevice == null) {
            Log.e(LOG_TAG, "Socket create failed: Bluetooth device is null.");
            return null;
        }
        try {
            Log.d(LOG_TAG, "socket create: attempting to create bluetooth socket");
            bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(SPP_UUID);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Socket create failed: " + e.getMessage() + ".");
            return null;
        }

        return bluetoothSocket;
    }

    public static BluetoothConnectionThread connectToBluetoothSocket(BluetoothSocket bluetoothSocket, BluetoothAdapter bluetoothAdapter, Handler bluetoothMessageHandler) {
        if (bluetoothSocket == null || bluetoothAdapter == null) {
            Log.e(LOG_TAG, "connect to bluetooth socket failed: bluetooth socket or bluetooth adapter are null.");
            return null;
        }

        // Discovery is resource intensive.  Make sure it isn't going on
        // when you attempt to connect and pass your message.
        bluetoothAdapter.cancelDiscovery();

        // Establish the connection.  This will block until it connects.
        try {
            Log.d(LOG_TAG, "connectToBluetoothSocket: attempting to connect to bluetooth socket");
            bluetoothSocket.connect();
            Log.d(LOG_TAG, "connectToBluetoothSocket: connection okay");
        } catch (IOException e) {
            try {
                Log.e(LOG_TAG, "connectToBluetoothSocket: connection failed.");
                bluetoothSocket.close();
            } catch (IOException e2) {
                Log.e(LOG_TAG, "Close socket failed after connection failure " + e2.getMessage() + ".");
                return null;
            }
        }

        // Create a data stream so we can talk to server.
        Log.d(LOG_TAG, "connectToBluetoothSocket: starting connection thread");
        BluetoothConnectionThread connectThread = null;
        if (bluetoothSocket.isConnected()) {
            connectThread = new BluetoothConnectionThread(bluetoothSocket, bluetoothMessageHandler);
            connectThread.start();
        }
        return connectThread;
    }

    private void establishBluetoothConnection(BluetoothDevice bluetoothDevice, BluetoothAdapter bluetoothAdapter, Handler bluetoothMessageHandler) {
        mBluetoothSocket = createBluetoothSocket(bluetoothDevice);
        mConnectedThread = connectToBluetoothSocket(mBluetoothSocket, bluetoothAdapter, bluetoothMessageHandler);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(LOG_TAG, "...entering onResume()...");

        if (mContinuousCheckingThread != null) {
            mContinuousCheckingThread.resumeChecking();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        Log.d(LOG_TAG, "...entering onPause()...");

        if (mContinuousCheckingThread != null) {
            mContinuousCheckingThread.pauseChecking();
        }

        if (mBluetoothSocket == null) {
            return;
        }

        try {
            Log.d(LOG_TAG, "onPause(): attempting to close bluetooth socket");
            mBluetoothSocket.close();
        } catch (IOException e2) {
            caller.bluetoothError("Fatal Error: In onPause() and failed to close socket." + e2.getMessage() + ".");
        }
    }

    interface BluetoothConnectionCaller {
        void handleBluetoothInput(String inputString);
        void bluetoothError(String message);
        void bluetoothConnectionStatus(boolean status);
    }
}
