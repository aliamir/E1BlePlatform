package com.example.amir.e1bleplatform;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;


public class ConnectedActivity extends AppCompatActivity {

    private final static String TAG = MainActivity.class.getSimpleName();                   //Activity name for logging messages on the ADB
    public static final String BLE_ADDRESS_MESSAGE_SERVICE = "com.example.amir.e1bleplatform.BLE_ADDRESS_MESSAGE_SERVICE";
    public static final String BLE_NAME_MESSAGE_SERVICE = "com.example.amir.e1bleplatform.BLE_NAME_MESSAGE_SERVICE";

    // Screen Text
    TextView rxAddressView;
    TextView rxNameView;

    // Buttons
    Button CheckConnection;
    FloatingActionButton ClearTxButton;
    FloatingActionButton ClearRxButton;

    // Progress Bar
    ProgressBar ConnStatusProgressBar;
    // BLE Device Class
    BleDevice mBleDevice;
    // Textboxes for Rx/Tx
    TextView RxTextBox;
    EditText TxTextBox;

    // Intent from MainActivity
    Intent serviceIntent;

    // Service info
    private BleConnectionService mBleConnectionService;
    private boolean isServiceBound;
    private ServiceConnection mServiceConnection;

    // BLE States
    private enum BleState {DISCONNECTED, CONNECTED}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connected);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Intent from MainActivity
        Intent intent = getIntent();

        mBleDevice = new BleDevice(intent.getStringExtra(MainActivity.BLE_NAME_MESSAGE),
                intent.getStringExtra(MainActivity.BLE_ADDRESS_MESSAGE));

        // set TextView
        rxAddressView = findViewById(R.id.rx_address);
        rxNameView = findViewById(R.id.rx_name);
        // Set TextBox for Rx/Tx data
        RxTextBox = findViewById(R.id.rx_data_text_box);
        TxTextBox = findViewById(R.id.tx_data_text_box);

        // Set Button
        CheckConnection = findViewById(R.id.conn_status_button);
        CheckConnection.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (isServiceBound) {
                    boolean connectionStatus = mBleConnectionService.getConnectionStatus();
                    if (connectionStatus) {
                        mBleConnectionService.disconnect();
                        ConnStatusProgressBar.setVisibility(View.VISIBLE);
                        // Change text to disconnecting...
                    }
                    else {
                        mBleConnectionService.connect(mBleDevice.getAddress());
                        ConnStatusProgressBar.setVisibility(View.VISIBLE);
                        // Change text to connecting...
                    }
                }
                else {
                    // Service is not bound, so can't disconnect
                }
            }
        });

        ClearTxButton = findViewById(R.id.tx_clear_float_button);
        ClearTxButton.setOnClickListener(new FloatingButtons());

        ClearRxButton = findViewById(R.id.rx_clear_float_button);
        ClearRxButton.setOnClickListener(new FloatingButtons());

        // Set ProgressBar
        ConnStatusProgressBar = findViewById(R.id.conn_status_progressbar);
        ConnStatusProgressBar.setVisibility(View.VISIBLE);

        rxAddressView.setText(mBleDevice.getAddress());
        rxNameView.setText(mBleDevice.getName());

        StartBleConnectionService();
    }

    class FloatingButtons implements View.OnClickListener {
        @Override
        public void onClick(View v){
            switch(v.getId()) {
                case R.id.rx_clear_float_button:
                    RxTextBox.setText("");
                    break;
                case R.id.tx_clear_float_button:
                    TxTextBox.setText("");
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(bleServiceReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(bleServiceReceiver, bleServiceIntentFilter());
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        mBleConnectionService.disconnect();
    }

    /**
     * Description:
     * Start service to maintain BLE connection
     */
    void StartBleConnectionService() {
        // Create Intent to send data
        serviceIntent = new Intent(getApplicationContext(), BleConnectionService.class);
        serviceIntent.putExtra(BLE_NAME_MESSAGE_SERVICE, mBleDevice.getName());
        serviceIntent.putExtra(BLE_ADDRESS_MESSAGE_SERVICE, mBleDevice.getAddress());
        startService(serviceIntent);

        bindService();
    }

    private void bindService() {
        if (mServiceConnection == null) {
            mServiceConnection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    BleConnectionService.BleConnectionBinder myServiceBinder = (BleConnectionService.BleConnectionBinder)service;
                    mBleConnectionService = myServiceBinder.getService();
                    isServiceBound = true;
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    isServiceBound = false;
                }
            };
        }

        bindService(serviceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);

    }

    // ----------------------------------------------------------------------------------------------------------------
    // Intent filter to add Intent values that will be broadcast by the MldpBluetoothService to the bleServiceReceiver BroadcastReceiver
    private static IntentFilter bleServiceIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BleConnectionService.ACTION_BLE_REQ_ENABLE_BT);
        intentFilter.addAction(BleConnectionService.ACTION_BLE_CONNECTED);
        intentFilter.addAction(BleConnectionService.ACTION_BLE_DISCONNECTED);
        intentFilter.addAction(BleConnectionService.ACTION_BLE_DATA_RECEIVED);
        return intentFilter;
    }

    // ----------------------------------------------------------------------------------------------------------------
    // BroadcastReceiver handles various events fired by the MldpBluetoothService service.
    private final BroadcastReceiver bleServiceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
            if (BleConnectionService.ACTION_BLE_CONNECTED.equals(intent.getAction())) {			                //Service has connected to BLE device
            //connectTimeoutHandler.removeCallbacks(abortConnection);                             //Stop the connection timeout handler from calling the runnable to stop the connection attempt
            Log.d(TAG, "Received intent  ACTION_BLE_CONNECTED");
            UpdateConnectionState(BleState.CONNECTED);
            //state = State.CONNECTED;
            //updateConnectionState();                                                            //Update the screen and menus
//            if (attemptingAutoConnect == true) {
//                showAlert.dismiss();
//            }
        }
            else if (BleConnectionService.ACTION_BLE_DISCONNECTED.equals(action)) {		            //Service has disconnected from BLE device
            Log.d(TAG, "Received intent ACTION_BLE_DISCONNECTED");
/*            if (state == State.CONNECTED) {
                showLostConnectionDialog();                                                     //Show dialog to ask to scan for another device
            }
            else {
                if (attemptingAutoConnect == true) {
                    showAlert.dismiss();
                }
                clearUI();
                if (state != State.DISCONNECTING) {                                             //See if we are not deliberately disconnecting
                    showNoConnectDialog();                                                      //Show dialog to ask to scan for another device
                }
            }*/
        UpdateConnectionState(BleState.DISCONNECTED);                                                            //Update the screen and menus
        }
/*            else if (BleConnectionService.ACTION_BLE_DATA_RECEIVED.equals(action)) {		        //Service has found new data available on BLE device
            Log.d(TAG, "Received intent ACTION_BLE_DATA_RECEIVED");
            String data = intent.getStringExtra(MldpBluetoothService.INTENT_EXTRA_SERVICE_DATA); //Get data as a string to display
//                String data = null;
//                try {
//                    data = new String(intent.getByteArrayExtra(MldpBluetoothService.INTENT_EXTRA_SERVICE_DATA), "UTF-8"); // Example for bytes instead of string
//                } catch (UnsupportedEncodingException e) {
//                    e.printStackTrace();
//                }
            if (data != null) {
                textIncoming.append(data);
            }
        }*/
    }
    };

    private void UpdateConnectionState(BleState state) {
        switch (state) {
            case DISCONNECTED:
                CheckConnection.setText(R.string.connect_text);
                ConnStatusProgressBar.setVisibility(View.GONE);
                break;
            case CONNECTED:
                CheckConnection.setText(R.string.disconnect_text);
                ConnStatusProgressBar.setVisibility(View.GONE);
                break;
            default:
                break;
        }
    }
}
